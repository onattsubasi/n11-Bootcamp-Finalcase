package com.onatsubasi.finalcase.order.application.service;

import com.onatsubasi.finalcase.common.core.exception.BaseException;
import com.onatsubasi.finalcase.order.application.dto.internal.OrderReviewEligibilityResponse;
import com.onatsubasi.finalcase.order.application.dto.internal.VerifyPurchaseInternalResponse;
import com.onatsubasi.finalcase.order.application.dto.response.OrderDetailResponse;
import com.onatsubasi.finalcase.order.application.dto.response.OrderSummaryResponse;
import com.onatsubasi.finalcase.order.domain.enums.OrderStatus;
import com.onatsubasi.finalcase.order.domain.exception.OrderErrorCode;
import com.onatsubasi.finalcase.order.domain.entity.Order;
import com.onatsubasi.finalcase.order.domain.entity.OrderItem;
import com.onatsubasi.finalcase.order.domain.repository.OrderRepository;
import com.onatsubasi.finalcase.order.infrastructure.mapper.OrderMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class OrderQueryService {

    private final OrderRepository orderRepository;
    private final OrderMapper orderMapper;

    @Transactional(readOnly = true)
    public OrderDetailResponse getByIdForInternal(UUID orderId) {
        return orderMapper.toDetailResponse(getOrder(orderId));
    }

    @Transactional(readOnly = true)
    public OrderDetailResponse getByIdForCustomer(UUID orderId, UUID userId) {
        Order order = getOrder(orderId);
        if (!order.getUserId().equals(userId)) {
            throw new BaseException(OrderErrorCode.ORDER_ACCESS_DENIED);
        }
        return orderMapper.toDetailResponse(order);
    }

    @Transactional(readOnly = true)
    public OrderDetailResponse getByIdForAdmin(UUID orderId) {
        return orderMapper.toDetailResponse(getOrder(orderId));
    }

    @Transactional(readOnly = true)
    public OrderDetailResponse getByOrderNumberForAdmin(String orderNumber) {
        Order order = orderRepository.findByOrderNumber(orderNumber)
                .orElseThrow(() -> new BaseException(OrderErrorCode.ORDER_NOT_FOUND));
        return orderMapper.toDetailResponse(order);
    }

    @Transactional(readOnly = true)
    public OrderDetailResponse getByCheckoutIdForInternal(UUID checkoutId) {
        Order order = orderRepository.findByCheckoutId(checkoutId)
                .orElseThrow(() -> new BaseException(OrderErrorCode.ORDER_NOT_FOUND));
        return orderMapper.toDetailResponse(order);
    }

    @Transactional(readOnly = true)
    public OrderReviewEligibilityResponse verifyReviewEligibility(UUID orderId, UUID orderItemId, UUID userId, String productId) {
        Order order = getOrder(orderId);

        if (!order.getUserId().equals(userId)) {
            return ineligible(order, orderItemId, userId, productId, "ORDER_DOES_NOT_BELONG_TO_USER");
        }

        if (order.getStatus() != OrderStatus.DELIVERED) {
            return ineligible(order, orderItemId, userId, productId, "ORDER_NOT_DELIVERED");
        }

        OrderItem item = order.getItems()
                .stream()
                .filter(orderItem -> orderItem.getId() != null && orderItem.getId().equals(orderItemId))
                .findFirst()
                .orElse(null);

        if (item == null) {
            return ineligible(order, orderItemId, userId, productId, "ORDER_ITEM_NOT_FOUND");
        }

        if (productId == null || !item.getProductId().equals(productId)) {
            return ineligible(order, orderItemId, userId, productId, "PRODUCT_ID_DOES_NOT_MATCH_ORDER_ITEM");
        }

        return new OrderReviewEligibilityResponse(orderId, orderItemId, order.getOrderNumber(), userId, productId, true, null);
    }

    @Transactional(readOnly = true)
    public Page<OrderSummaryResponse> getMyOrders(UUID userId, int page, int size) {
        return orderRepository.findByUserId(userId, pageable(page, size))
                .map(orderMapper::toSummaryResponse);
    }

    @Transactional(readOnly = true)
    public Page<OrderSummaryResponse> getAllOrders(int page, int size) {
        return orderRepository.findAll(pageable(page, size))
                .map(orderMapper::toSummaryResponse);
    }

    private Order getOrder(UUID orderId) {
        return orderRepository.findById(orderId)
                .orElseThrow(() -> new BaseException(OrderErrorCode.ORDER_NOT_FOUND));
    }

    private OrderReviewEligibilityResponse ineligible(Order order, UUID orderItemId, UUID userId, String productId, String reason) {
        log.debug("Review eligibility rejected orderId={} orderItemId={} reason={}", order.getId(), orderItemId, reason);
        return new OrderReviewEligibilityResponse(order.getId(), orderItemId, order.getOrderNumber(), userId, productId, false, reason);
    }

    private Pageable pageable(int page, int size) {
        return PageRequest.of(
                Math.max(page, 0),
                Math.min(Math.max(size, 1), 100),
                Sort.by(Sort.Direction.DESC, "createdAt")
        );
    }
    @Transactional(readOnly = true)
    public VerifyPurchaseInternalResponse verifyDeliveredPurchase(UUID userId, UUID productId) {
        if (userId == null || productId == null) {
            return VerifyPurchaseInternalResponse.notVerified();
        }

        String requestedProductId = productId.toString();

        Page<OrderSummaryResponse> ignored = null;

        var orders = orderRepository.findByUserId(
                userId,
                PageRequest.of(
                        0,
                        100,
                        Sort.by(Sort.Direction.DESC, "createdAt")
                )
        ).getContent();

        for (Order order : orders) {
            if (order.getStatus() != OrderStatus.DELIVERED) {
                continue;
            }

            if (order.getItems() == null) {
                continue;
            }

            for (OrderItem item : order.getItems()) {
                if (item == null || item.getProductId() == null) {
                    continue;
                }

                if (requestedProductId.equals(String.valueOf(item.getProductId()))) {
                    return VerifyPurchaseInternalResponse.verified(
                            order.getId(),
                            item.getId(),
                            order.getOrderNumber(),
                            null
                    );
                }
            }
        }

        return VerifyPurchaseInternalResponse.notVerified();
    }


}
