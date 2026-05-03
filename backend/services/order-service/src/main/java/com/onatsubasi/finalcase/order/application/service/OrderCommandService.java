package com.onatsubasi.finalcase.order.application.service;

import com.onatsubasi.finalcase.common.core.exception.BaseException;
import com.onatsubasi.finalcase.order.application.dto.internal.*;
import com.onatsubasi.finalcase.order.application.dto.request.CancelOrderRequest;
import com.onatsubasi.finalcase.order.application.dto.response.OrderDetailResponse;
import com.onatsubasi.finalcase.order.application.port.OrderEventPublisher;
import com.onatsubasi.finalcase.order.domain.enums.OrderStatusChangeSource;
import com.onatsubasi.finalcase.order.domain.exception.OrderErrorCode;
import com.onatsubasi.finalcase.order.domain.entity.Order;
import com.onatsubasi.finalcase.order.domain.repository.OrderRepository;
import com.onatsubasi.finalcase.order.infrastructure.mapper.OrderMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class OrderCommandService {

    private final OrderRepository orderRepository;
    private final OrderMapper orderMapper;
    private final OrderNumberGenerator orderNumberGenerator;
    private final OrderEventPublisher eventPublisher;

    @Transactional
    public OrderDetailResponse createOrder(CreateOrderInternalRequest request) {
        validateCreateOrderRequest(request);

        return orderRepository.findByCheckoutIdForUpdate(request.checkoutId())
                .map(existing -> handleExistingOrder(existing, request))
                .orElseGet(() -> createNewOrder(request));
    }

    @Transactional
    public OrderDetailResponse markPaid(UUID orderId, MarkOrderPaidRequest request) {
        Order order = getOrderForUpdate(orderId);

        order.markPaid(
                request.paymentId(),
                request.paymentProvider(),
                request.paymentStatus(),
                request.providerTransactionId(),
                OrderStatusChangeSource.PAYMENT_SERVICE
        );

        Order saved = orderRepository.save(order);
        eventPublisher.publishOrderPaid(saved);
        log.info("Order marked paid orderId={} orderNumber={}", saved.getId(), saved.getOrderNumber());

        return orderMapper.toDetailResponse(saved);
    }

    @Transactional
    public OrderDetailResponse markPaymentFailed(UUID orderId, MarkOrderPaymentFailedRequest request) {
        Order order = getOrderForUpdate(orderId);

        order.markPaymentFailed(
                request.paymentId(),
                request.paymentProvider(),
                request.paymentStatus(),
                request.providerTransactionId(),
                OrderStatusChangeSource.PAYMENT_SERVICE
        );

        Order saved = orderRepository.save(order);
        eventPublisher.publishOrderPaymentFailed(saved);
        log.info("Order marked payment failed orderId={} orderNumber={}", saved.getId(), saved.getOrderNumber());

        return orderMapper.toDetailResponse(saved);
    }

    @Transactional
    public OrderDetailResponse cancelByCustomer(UUID orderId, UUID userId, CancelOrderRequest request) {
        Order order = getOrderForUpdate(orderId);
        validateOwner(order, userId);

        order.cancel(
                OrderStatusChangeSource.CUSTOMER,
                userId.toString(),
                request == null ? null : request.reason()
        );

        Order saved = orderRepository.save(order);
        eventPublisher.publishOrderCancelled(saved);
        log.info("Order cancelled by customer orderId={} userId={}", saved.getId(), userId);

        return orderMapper.toDetailResponse(saved);
    }

    @Transactional
    public OrderDetailResponse cancelByAdmin(UUID orderId, String adminId, CancelOrderRequest request) {
        Order order = getOrderForUpdate(orderId);

        order.cancel(
                OrderStatusChangeSource.ADMIN,
                adminId,
                request == null ? null : request.reason()
        );

        Order saved = orderRepository.save(order);
        eventPublisher.publishOrderCancelled(saved);
        log.info("Order cancelled by admin orderId={} adminId={}", saved.getId(), adminId);

        return orderMapper.toDetailResponse(saved);
    }

    @Transactional
    public OrderDetailResponse cancelInternal(UUID orderId, CancelOrderRequest request) {
        Order order = getOrderForUpdate(orderId);

        order.cancel(
                OrderStatusChangeSource.CHECKOUT_SERVICE,
                null,
                request == null ? null : request.reason()
        );

        Order saved = orderRepository.save(order);
        eventPublisher.publishOrderCancelled(saved);
        log.info("Order cancelled internally orderId={}", saved.getId());

        return orderMapper.toDetailResponse(saved);
    }

    @Transactional
    public OrderDetailResponse markPreparing(UUID orderId) {
        Order order = getOrderForUpdate(orderId);

        order.markPreparing(OrderStatusChangeSource.ADMIN, null, "Order is being prepared");

        Order saved = orderRepository.save(order);
        eventPublisher.publishOrderPreparing(saved);
        log.info("Order marked preparing orderId={}", saved.getId());

        return orderMapper.toDetailResponse(saved);
    }

    @Transactional
    public OrderDetailResponse attachShipmentCreated(UUID orderId, ShipmentCreatedRequest request) {
        Order order = getOrderForUpdate(orderId);

        order.attachShipmentCreated(
                request.shipmentId(),
                request.shipmentNumber(),
                request.carrier(),
                request.trackingNumber(),
                request.shipmentStatus()
        );

        Order saved = orderRepository.save(order);
        log.info("Shipment summary attached to order orderId={} shipmentId={}", saved.getId(), request.shipmentId());

        return orderMapper.toDetailResponse(saved);
    }

    @Transactional
    public OrderDetailResponse markShipped(UUID orderId, MarkOrderShippedRequest request) {
        Order order = getOrderForUpdate(orderId);

        order.markShipped(
                request.carrier(),
                request.trackingNumber(),
                request.shippedAt(),
                OrderStatusChangeSource.SHIPMENT_SERVICE
        );

        Order saved = orderRepository.save(order);
        eventPublisher.publishOrderShipped(saved);
        log.info("Order marked shipped orderId={}", saved.getId());

        return orderMapper.toDetailResponse(saved);
    }

    @Transactional
    public OrderDetailResponse markDelivered(UUID orderId, MarkOrderDeliveredRequest request) {
        Order order = getOrderForUpdate(orderId);

        order.markDelivered(request.deliveredAt(), OrderStatusChangeSource.SHIPMENT_SERVICE);

        Order saved = orderRepository.save(order);
        eventPublisher.publishOrderDelivered(saved);
        log.info("Order marked delivered orderId={}", saved.getId());

        return orderMapper.toDetailResponse(saved);
    }

    private OrderDetailResponse handleExistingOrder(Order existing, CreateOrderInternalRequest request) {
        if (!existing.getRequestHash().equals(request.requestHash())) {
            throw new BaseException(OrderErrorCode.ORDER_IDEMPOTENCY_CONFLICT);
        }
        return orderMapper.toDetailResponse(existing);
    }

    private OrderDetailResponse createNewOrder(CreateOrderInternalRequest request) {
        String orderNumber = orderNumberGenerator.generate();
        Order order = orderMapper.toOrder(orderNumber, request);
        order.assertHasItems();

        Order saved = orderRepository.save(order);
        eventPublisher.publishOrderCreated(saved);
        log.info("Order created orderId={} orderNumber={} checkoutId={}", saved.getId(), saved.getOrderNumber(), saved.getCheckoutId());

        return orderMapper.toDetailResponse(saved);
    }

    private Order getOrderForUpdate(UUID orderId) {
        return orderRepository.findByIdForUpdate(orderId)
                .orElseThrow(() -> new BaseException(OrderErrorCode.ORDER_NOT_FOUND));
    }

    private void validateOwner(Order order, UUID userId) {
        if (userId == null || !order.getUserId().equals(userId)) {
            throw new BaseException(OrderErrorCode.ORDER_ACCESS_DENIED);
        }
    }

    private void validateCreateOrderRequest(CreateOrderInternalRequest request) {
        if (request == null) {
            throw new BaseException(OrderErrorCode.INVALID_ORDER_DATA, "Create order request is required");
        }
        if (request.items() == null || request.items().isEmpty()) {
            throw new BaseException(OrderErrorCode.INVALID_ORDER_ITEM_DATA, "Order must contain at least one item");
        }
    }
}
