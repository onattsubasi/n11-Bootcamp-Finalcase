package com.onatsubasi.finalcase.shipment.application.service;

import com.onatsubasi.finalcase.common.core.exception.BaseException;
import com.onatsubasi.finalcase.shipment.application.dto.provider.CarrierCancelShipmentResult;
import com.onatsubasi.finalcase.shipment.application.dto.provider.CarrierCreateShipmentResult;
import com.onatsubasi.finalcase.shipment.application.dto.client.OrderDetailClientResponse;
import com.onatsubasi.finalcase.shipment.application.dto.request.CancelShipmentRequest;
import com.onatsubasi.finalcase.shipment.application.dto.request.ChangeShipmentStatusRequest;
import com.onatsubasi.finalcase.shipment.application.dto.request.CreateShipmentForOrderRequest;
import com.onatsubasi.finalcase.shipment.application.dto.request.UpdateShipmentTrackingRequest;
import com.onatsubasi.finalcase.shipment.application.dto.response.ShipmentDetailResponse;
import com.onatsubasi.finalcase.shipment.application.port.ShipmentCarrierPort;
import com.onatsubasi.finalcase.shipment.application.port.ShipmentEventPublisher;
import com.onatsubasi.finalcase.shipment.infrastructure.config.ShipmentServiceProperties;
import com.onatsubasi.finalcase.shipment.domain.entity.Shipment;
import com.onatsubasi.finalcase.shipment.domain.entity.ShipmentIdempotencyRecord;
import com.onatsubasi.finalcase.shipment.domain.enums.ShipmentCarrier;
import com.onatsubasi.finalcase.shipment.domain.enums.ShipmentStatus;
import com.onatsubasi.finalcase.shipment.domain.enums.ShipmentStatusChangeSource;
import com.onatsubasi.finalcase.shipment.domain.exception.ShipmentErrorCode;
import com.onatsubasi.finalcase.shipment.domain.repository.ShipmentRepository;
import com.onatsubasi.finalcase.shipment.infrastructure.mapper.ShipmentMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class ShipmentCommandService {

    private final ShipmentRepository shipmentRepository;
    private final ShipmentRequestHashService requestHashService;
    private final ShipmentIdempotencyService idempotencyService;
    private final ShipmentNumberGenerator shipmentNumberGenerator;
    private final ShipmentOrderGateway orderGateway;
    private final ShipmentCarrierFactory carrierFactory;
    private final ShipmentServiceProperties properties;
    private final ShipmentMapper shipmentMapper;
    private final ShipmentEventPublisher eventPublisher;

    @Transactional
    public ShipmentDetailResponse createShipmentForOrder(
            String idempotencyKey,
            CreateShipmentForOrderRequest request
    ) {
        log.info(
                "event=shipment.create_requested orderId={} carrierOverride={} idempotencyKeyPresent={}",
                request.orderId(),
                request.carrier(),
                idempotencyKey != null && !idempotencyKey.isBlank()
        );

        String requestHash = requestHashService.hash(request);

        ShipmentIdempotencyRecord idempotencyRecord =
                idempotencyService.getOrCreateForUpdate(
                        idempotencyKey,
                        requestHash
                );

        Optional<ShipmentDetailResponse> storedResponse =
                idempotencyService.getStoredShipmentResponse(idempotencyRecord);

        if (storedResponse.isPresent()) {
            log.info(
                    "event=shipment.idempotent_replay orderId={} shipmentId={}",
                    request.orderId(),
                    storedResponse.get().id()
            );

            return storedResponse.get();
        }

        Optional<Shipment> existingShipment =
                shipmentRepository.findByOrderIdForUpdate(request.orderId());

        ShipmentCarrier carrier = request.carrier() == null
                ? properties.getDefaultCarrier()
                : request.carrier();

        if (existingShipment.isPresent()) {
            Shipment existing = existingShipment.get();

            if (request.carrier() != null && existing.getCarrier() != carrier) {
                throw new BaseException(
                        ShipmentErrorCode.SHIPMENT_ALREADY_EXISTS,
                        "Shipment already exists for order with carrier " + existing.getCarrier()
                );
            }

            ShipmentDetailResponse response = shipmentMapper.toDetailResponse(existing);

            idempotencyService.storeShipmentResponse(
                    idempotencyRecord,
                    existing,
                    response
            );

            return response;
        }

        OrderDetailClientResponse order = orderGateway.getOrder(request.orderId());
        validateOrderReadyForShipment(order);

        String shipmentNumber = shipmentNumberGenerator.generate();

        Shipment shipment = shipmentMapper.toShipment(
                shipmentNumber,
                carrier,
                order
        );

        shipment = shipmentRepository.save(shipment);

        ShipmentCarrierPort carrierPort = carrierFactory.getCarrier(carrier);

        CarrierCreateShipmentResult carrierResult = carrierPort.createShipment(
                shipmentMapper.toCarrierCreateCommand(shipment)
        );

        if (!carrierResult.success()) {
            throw new BaseException(
                    ShipmentErrorCode.SHIPMENT_CARRIER_OPERATION_FAILED,
                    carrierResult.failureReason()
            );
        }

        shipment.attachCarrierResult(
                carrierResult.carrierShipmentId(),
                carrierResult.trackingNumber(),
                carrierResult.trackingUrl(),
                carrierResult.labelUrl(),
                carrierResult.carrierStatus()
        );

        if (properties.isAutoMarkReadyToShip()) {
            shipment.markReadyToShip(
                    ShipmentStatusChangeSource.SYSTEM,
                    null,
                    "Auto marked as ready to ship"
            );
        }

        Shipment saved = shipmentRepository.save(shipment);

        orderGateway.updateShipmentCreated(
                saved.getOrderId(),
                shipmentMapper.toShipmentCreatedOrderRequest(saved)
        );

        eventPublisher.publishShipmentCreated(saved);

        if (saved.getStatus() == ShipmentStatus.READY_TO_SHIP) {
            eventPublisher.publishShipmentReadyToShip(saved);
        }

        ShipmentDetailResponse response = shipmentMapper.toDetailResponse(saved);

        idempotencyService.storeShipmentResponse(
                idempotencyRecord,
                saved,
                response
        );

        log.info(
                "event=shipment.created shipmentId={} shipmentNumber={} orderId={} carrier={} status={}",
                saved.getId(),
                saved.getShipmentNumber(),
                saved.getOrderId(),
                saved.getCarrier(),
                saved.getStatus()
        );

        return response;
    }

    @Transactional
    public ShipmentDetailResponse updateTracking(
            UUID shipmentId,
            String changedBy,
            UpdateShipmentTrackingRequest request
    ) {
        Shipment shipment = shipmentRepository.findByIdForUpdate(shipmentId)
                .orElseThrow(() -> new BaseException(ShipmentErrorCode.SHIPMENT_NOT_FOUND));

        shipment.updateTracking(
                request.trackingNumber(),
                request.trackingUrl(),
                ShipmentStatusChangeSource.ADMIN,
                changedBy,
                request.reason()
        );

        Shipment saved = shipmentRepository.save(shipment);

        log.info(
                "event=shipment.tracking_updated shipmentId={} orderId={} trackingNumber={}",
                saved.getId(),
                saved.getOrderId(),
                saved.getTrackingNumber()
        );

        return shipmentMapper.toDetailResponse(saved);
    }

    @Transactional
    public ShipmentDetailResponse changeStatus(
            UUID shipmentId,
            String changedBy,
            ChangeShipmentStatusRequest request
    ) {
        Shipment shipment = shipmentRepository.findByIdForUpdate(shipmentId)
                .orElseThrow(() -> new BaseException(ShipmentErrorCode.SHIPMENT_NOT_FOUND));

        ShipmentStatus previousStatus = shipment.getStatus();
        ShipmentStatus targetStatus = request.status();

        switch (targetStatus) {
            case READY_TO_SHIP -> shipment.markReadyToShip(
                    ShipmentStatusChangeSource.ADMIN,
                    changedBy,
                    request.reason()
            );

            case SHIPPED -> shipment.markShipped(
                    request.trackingNumber(),
                    request.trackingUrl(),
                    ShipmentStatusChangeSource.ADMIN,
                    changedBy,
                    request.reason()
            );

            case IN_TRANSIT -> shipment.markInTransit(
                    ShipmentStatusChangeSource.ADMIN,
                    changedBy,
                    request.reason()
            );

            case OUT_FOR_DELIVERY -> shipment.markOutForDelivery(
                    ShipmentStatusChangeSource.ADMIN,
                    changedBy,
                    request.reason()
            );

            case DELIVERED -> shipment.markDelivered(
                    ShipmentStatusChangeSource.ADMIN,
                    changedBy,
                    request.reason()
            );

            case DELIVERY_FAILED -> shipment.markDeliveryFailed(
                    request.failureReason(),
                    ShipmentStatusChangeSource.ADMIN,
                    changedBy
            );

            case CANCELLED -> shipment.cancel(
                    ShipmentStatusChangeSource.ADMIN,
                    changedBy,
                    request.reason()
            );

            case CREATED -> throw new BaseException(
                    ShipmentErrorCode.SHIPMENT_INVALID_STATUS_TRANSITION,
                    "Cannot manually transition shipment back to CREATED"
            );
        }

        Shipment saved = shipmentRepository.save(shipment);

        boolean statusChanged = previousStatus != saved.getStatus();

        if (statusChanged) {
            publishStatusEvent(saved);

            if (saved.getStatus() == ShipmentStatus.SHIPPED) {
                orderGateway.markOrderShipped(
                        saved.getOrderId(),
                        shipmentMapper.toMarkOrderShippedRequest(saved)
                );
            }

            if (saved.getStatus() == ShipmentStatus.DELIVERED) {
                orderGateway.markOrderDelivered(
                        saved.getOrderId(),
                        shipmentMapper.toMarkOrderDeliveredRequest(saved)
                );
            }
        }

        log.info(
                "event=shipment.status_changed shipmentId={} orderId={} status={}",
                saved.getId(),
                saved.getOrderId(),
                saved.getStatus()
        );

        return shipmentMapper.toDetailResponse(saved);
    }

    @Transactional
    public ShipmentDetailResponse cancelShipment(
            UUID shipmentId,
            String changedBy,
            CancelShipmentRequest request
    ) {
        Shipment shipment = shipmentRepository.findByIdForUpdate(shipmentId)
                .orElseThrow(() -> new BaseException(ShipmentErrorCode.SHIPMENT_NOT_FOUND));

        ShipmentCarrierPort carrierPort = carrierFactory.getCarrier(shipment.getCarrier());

        CarrierCancelShipmentResult carrierResult = carrierPort.cancelShipment(
                shipmentMapper.toCarrierCancelCommand(shipment, request)
        );

        if (!carrierResult.success()) {
            throw new BaseException(
                    ShipmentErrorCode.SHIPMENT_CARRIER_OPERATION_FAILED,
                    carrierResult.failureReason()
            );
        }

        shipment.cancel(
                ShipmentStatusChangeSource.ADMIN,
                changedBy,
                request == null ? null : request.reason()
        );

        Shipment saved = shipmentRepository.save(shipment);

        eventPublisher.publishShipmentCancelled(saved);

        log.info(
                "event=shipment.cancelled shipmentId={} orderId={} carrierStatus={}",
                saved.getId(),
                saved.getOrderId(),
                carrierResult.carrierStatus()
        );

        return shipmentMapper.toDetailResponse(saved);
    }

    private void validateOrderReadyForShipment(OrderDetailClientResponse order) {
        if (order == null || order.status() == null || order.status().isBlank()) {
            throw new BaseException(ShipmentErrorCode.ORDER_NOT_READY_FOR_SHIPMENT);
        }

        String status = order.status().trim().toUpperCase();

        if (!status.equals("PAID") && !status.equals("PREPARING")) {
            throw new BaseException(
                    ShipmentErrorCode.ORDER_NOT_READY_FOR_SHIPMENT,
                    "Shipment can only be created for PAID or PREPARING orders. Current status: " + order.status()
            );
        }
    }

    private void publishStatusEvent(Shipment shipment) {
        switch (shipment.getStatus()) {
            case CREATED -> eventPublisher.publishShipmentCreated(shipment);
            case READY_TO_SHIP -> eventPublisher.publishShipmentReadyToShip(shipment);
            case SHIPPED -> eventPublisher.publishShipmentShipped(shipment);
            case IN_TRANSIT -> eventPublisher.publishShipmentInTransit(shipment);
            case OUT_FOR_DELIVERY -> eventPublisher.publishShipmentOutForDelivery(shipment);
            case DELIVERED -> eventPublisher.publishShipmentDelivered(shipment);
            case DELIVERY_FAILED -> eventPublisher.publishShipmentDeliveryFailed(shipment);
            case CANCELLED -> eventPublisher.publishShipmentCancelled(shipment);
        }
    }
}