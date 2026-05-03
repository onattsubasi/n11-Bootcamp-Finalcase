package com.onatsubasi.finalcase.shipment.application.service;

import com.onatsubasi.finalcase.common.core.exception.BaseException;
import com.onatsubasi.finalcase.common.core.response.ApiResponse;
import com.onatsubasi.finalcase.shipment.application.client.OrderClient;
import com.onatsubasi.finalcase.shipment.application.dto.client.MarkOrderDeliveredClientRequest;
import com.onatsubasi.finalcase.shipment.application.dto.client.MarkOrderShippedClientRequest;
import com.onatsubasi.finalcase.shipment.application.dto.client.OrderDetailClientResponse;
import com.onatsubasi.finalcase.shipment.application.dto.client.ShipmentCreatedOrderClientRequest;
import com.onatsubasi.finalcase.shipment.domain.exception.ShipmentErrorCode;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class ShipmentOrderGateway {

    private static final String ORDER_SERVICE_CIRCUIT_BREAKER = "order-service";

    private final OrderClient orderClient;

    @CircuitBreaker(name = ORDER_SERVICE_CIRCUIT_BREAKER, fallbackMethod = "getOrderFallback")
    public OrderDetailClientResponse getOrder(UUID orderId) {
        log.debug("event=shipment.order_get_requested orderId={}", orderId);

        return extract(
                orderClient.getById(orderId),
                ShipmentErrorCode.ORDER_SERVICE_UNAVAILABLE
        );
    }

    public OrderDetailClientResponse getOrderFallback(UUID orderId, Throwable ex) {
        log.warn(
                "event=shipment.order_get_unavailable orderId={} reason={}",
                orderId,
                ex.getMessage()
        );

        throw new BaseException(
                ShipmentErrorCode.ORDER_SERVICE_UNAVAILABLE,
                "Order service is temporarily unavailable"
        );
    }

    @CircuitBreaker(name = ORDER_SERVICE_CIRCUIT_BREAKER, fallbackMethod = "updateShipmentCreatedFallback")
    public void updateShipmentCreated(UUID orderId, ShipmentCreatedOrderClientRequest request) {
        log.info(
                "event=shipment.order_sync_created_requested orderId={} shipmentId={} shipmentNumber={}",
                orderId,
                request.shipmentId(),
                request.shipmentNumber()
        );

        extract(
                orderClient.updateShipmentCreated(
                        orderId,
                        "shipment:" + request.shipmentId() + ":order-shipment-created",
                        request
                ),
                ShipmentErrorCode.ORDER_SYNC_FAILED
        );
    }

    public void updateShipmentCreatedFallback(UUID orderId, ShipmentCreatedOrderClientRequest request, Throwable ex) {
        log.error(
                "event=shipment.order_sync_created_failed orderId={} shipmentId={} reason={}",
                orderId,
                request.shipmentId(),
                ex.getMessage()
        );

        throw new BaseException(
                ShipmentErrorCode.ORDER_SYNC_FAILED,
                "Failed to synchronize shipment creation with Order Service"
        );
    }

    @CircuitBreaker(name = ORDER_SERVICE_CIRCUIT_BREAKER, fallbackMethod = "markOrderShippedFallback")
    public void markOrderShipped(UUID orderId, MarkOrderShippedClientRequest request) {
        log.info(
                "event=shipment.order_mark_shipped_requested orderId={} trackingNumber={}",
                orderId,
                request.trackingNumber()
        );

        extract(
                orderClient.markShipped(
                        orderId,
                        "shipment:" + orderId + ":mark-shipped",
                        request
                ),
                ShipmentErrorCode.ORDER_SYNC_FAILED
        );
    }

    public void markOrderShippedFallback(UUID orderId, MarkOrderShippedClientRequest request, Throwable ex) {
        log.error(
                "event=shipment.order_mark_shipped_failed orderId={} trackingNumber={} reason={}",
                orderId,
                request.trackingNumber(),
                ex.getMessage()
        );

        throw new BaseException(
                ShipmentErrorCode.ORDER_SYNC_FAILED,
                "Failed to mark order as shipped"
        );
    }

    @CircuitBreaker(name = ORDER_SERVICE_CIRCUIT_BREAKER, fallbackMethod = "markOrderDeliveredFallback")
    public void markOrderDelivered(UUID orderId, MarkOrderDeliveredClientRequest request) {
        log.info(
                "event=shipment.order_mark_delivered_requested orderId={} deliveredAt={}",
                orderId,
                request.deliveredAt()
        );

        extract(
                orderClient.markDelivered(
                        orderId,
                        "shipment:" + orderId + ":mark-delivered",
                        request
                ),
                ShipmentErrorCode.ORDER_SYNC_FAILED
        );
    }

    public void markOrderDeliveredFallback(UUID orderId, MarkOrderDeliveredClientRequest request, Throwable ex) {
        log.error(
                "event=shipment.order_mark_delivered_failed orderId={} deliveredAt={} reason={}",
                orderId,
                request.deliveredAt(),
                ex.getMessage()
        );

        throw new BaseException(
                ShipmentErrorCode.ORDER_SYNC_FAILED,
                "Failed to mark order as delivered"
        );
    }

    private <T> T extract(ApiResponse<T> response, ShipmentErrorCode errorCode) {
        if (response == null || response.data() == null) {
            throw new BaseException(errorCode);
        }

        return response.data();
    }
}
