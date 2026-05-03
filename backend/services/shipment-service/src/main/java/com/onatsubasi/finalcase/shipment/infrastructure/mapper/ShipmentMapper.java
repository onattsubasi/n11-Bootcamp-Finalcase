package com.onatsubasi.finalcase.shipment.infrastructure.mapper;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.onatsubasi.finalcase.common.core.exception.BaseException;
import com.onatsubasi.finalcase.shipment.application.dto.client.*;
import com.onatsubasi.finalcase.shipment.application.dto.event.ShipmentChangedEvent;
import com.onatsubasi.finalcase.shipment.application.dto.provider.CarrierCancelShipmentCommand;
import com.onatsubasi.finalcase.shipment.application.dto.provider.CarrierCreateShipmentCommand;
import com.onatsubasi.finalcase.shipment.application.dto.request.CancelShipmentRequest;
import com.onatsubasi.finalcase.shipment.application.dto.response.*;
import com.onatsubasi.finalcase.shipment.domain.entity.Shipment;
import com.onatsubasi.finalcase.shipment.domain.entity.ShipmentAddressSnapshot;
import com.onatsubasi.finalcase.shipment.domain.entity.ShipmentItem;
import com.onatsubasi.finalcase.shipment.domain.entity.ShipmentStatusHistory;
import com.onatsubasi.finalcase.shipment.domain.enums.ShipmentCarrier;
import com.onatsubasi.finalcase.shipment.domain.exception.ShipmentErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class ShipmentMapper {

    private final ObjectMapper objectMapper;

    public Shipment toShipment(
            String shipmentNumber,
            ShipmentCarrier carrier,
            OrderDetailClientResponse order
    ) {
        Shipment shipment = new Shipment(
                shipmentNumber,
                order.id(),
                order.orderNumber(),
                order.userId(),
                carrier,
                toAddressSnapshot(order.shippingAddress())
        );

        order.items()
                .stream()
                .map(this::toShipmentItem)
                .forEach(shipment::addItem);

        return shipment;
    }

    public ShipmentAddressSnapshot toAddressSnapshot(
            OrderAddressClientResponse address
    ) {
        if (address == null) {
            throw new BaseException(ShipmentErrorCode.INVALID_SHIPMENT_ADDRESS);
        }

        return new ShipmentAddressSnapshot(
                address.recipientName(),
                address.recipientPhone(),
                address.country(),
                address.city(),
                address.district(),
                address.neighborhood(),
                address.addressLine1(),
                address.addressLine2(),
                address.postalCode()
        );
    }

    public ShipmentItem toShipmentItem(OrderItemClientResponse item) {
        return new ShipmentItem(
                item.productId(),
                item.sku(),
                item.productName(),
                item.quantity()
        );
    }

    public ShipmentSummaryResponse toSummaryResponse(Shipment shipment) {
        return new ShipmentSummaryResponse(
                shipment.getId(),
                shipment.getShipmentNumber(),
                shipment.getOrderId(),
                shipment.getOrderNumber(),
                shipment.getUserId(),
                shipment.getCarrier(),
                shipment.getStatus(),
                shipment.getTrackingNumber(),
                shipment.getTrackingUrl(),
                shipment.getCreatedAt(),
                shipment.getShippedAt(),
                shipment.getDeliveredAt()
        );
    }

    public ShipmentDetailResponse toDetailResponse(Shipment shipment) {
        return new ShipmentDetailResponse(
                shipment.getId(),
                shipment.getShipmentNumber(),
                shipment.getOrderId(),
                shipment.getOrderNumber(),
                shipment.getUserId(),
                shipment.getCarrier(),
                shipment.getStatus(),
                shipment.getCarrierShipmentId(),
                shipment.getTrackingNumber(),
                shipment.getTrackingUrl(),
                shipment.getLabelUrl(),
                shipment.getCarrierStatus(),
                shipment.getFailureReason(),
                toAddressResponse(shipment.getShippingAddress()),
                shipment.getItems()
                        .stream()
                        .map(this::toItemResponse)
                        .toList(),
                shipment.getStatusHistory()
                        .stream()
                        .map(this::toStatusHistoryResponse)
                        .toList(),
                shipment.getCreatedAt(),
                shipment.getUpdatedAt(),
                shipment.getReadyToShipAt(),
                shipment.getShippedAt(),
                shipment.getInTransitAt(),
                shipment.getOutForDeliveryAt(),
                shipment.getDeliveredAt(),
                shipment.getDeliveryFailedAt(),
                shipment.getCancelledAt()
        );
    }

    public ShipmentAddressResponse toAddressResponse(
            ShipmentAddressSnapshot address
    ) {
        if (address == null) {
            return null;
        }

        return new ShipmentAddressResponse(
                address.getRecipientName(),
                address.getRecipientPhone(),
                address.getCountry(),
                address.getCity(),
                address.getDistrict(),
                address.getNeighborhood(),
                address.getAddressLine1(),
                address.getAddressLine2(),
                address.getPostalCode()
        );
    }

    public ShipmentItemResponse toItemResponse(ShipmentItem item) {
        return new ShipmentItemResponse(
                item.getId(),
                item.getProductId(),
                item.getSku(),
                item.getProductName(),
                item.getQuantity()
        );
    }

    public ShipmentStatusHistoryResponse toStatusHistoryResponse(
            ShipmentStatusHistory history
    ) {
        return new ShipmentStatusHistoryResponse(
                history.getId(),
                history.getFromStatus(),
                history.getToStatus(),
                history.getSource(),
                history.getChangedBy(),
                history.getReason(),
                history.getCreatedAt()
        );
    }

    public CarrierCreateShipmentCommand toCarrierCreateCommand(
            Shipment shipment
    ) {
        return CarrierCreateShipmentCommand.builder()
                .shipmentId(shipment.getId())
                .shipmentNumber(shipment.getShipmentNumber())
                .orderId(shipment.getOrderId())
                .orderNumber(shipment.getOrderNumber())
                .userId(shipment.getUserId())
                .carrier(shipment.getCarrier())
                .shippingAddress(shipment.getShippingAddress())
                .items(shipment.getItems())
                .build();
    }

    public CarrierCancelShipmentCommand toCarrierCancelCommand(
            Shipment shipment,
            CancelShipmentRequest request
    ) {
        return CarrierCancelShipmentCommand.builder()
                .shipmentId(shipment.getId())
                .shipmentNumber(shipment.getShipmentNumber())
                .carrierShipmentId(shipment.getCarrierShipmentId())
                .trackingNumber(shipment.getTrackingNumber())
                .reason(request == null ? null : request.reason())
                .build();
    }

    public ShipmentCreatedOrderClientRequest toShipmentCreatedOrderRequest(
            Shipment shipment
    ) {
        return new ShipmentCreatedOrderClientRequest(
                shipment.getId(),
                shipment.getShipmentNumber(),
                shipment.getCarrier().name(),
                shipment.getTrackingNumber(),
                shipment.getStatus().name()
        );
    }

    public MarkOrderShippedClientRequest toMarkOrderShippedRequest(
            Shipment shipment
    ) {
        return new MarkOrderShippedClientRequest(
                shipment.getCarrier().name(),
                shipment.getTrackingNumber(),
                shipment.getShippedAt()
        );
    }

    public MarkOrderDeliveredClientRequest toMarkOrderDeliveredRequest(
            Shipment shipment
    ) {
        return new MarkOrderDeliveredClientRequest(
                shipment.getDeliveredAt()
        );
    }

    public ShipmentChangedEvent toChangedEvent(Shipment shipment) {
        return ShipmentChangedEvent.from(shipment);
    }

    public Map<String, Object> toMap(Object value) {
        if (value == null) {
            return new HashMap<>();
        }

        return objectMapper.convertValue(
                value,
                new TypeReference<Map<String, Object>>() {
                }
        );
    }
}