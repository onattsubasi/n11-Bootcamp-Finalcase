package com.onatsubasi.finalcase.order.infrastructure.mapper;

import com.onatsubasi.finalcase.order.application.dto.internal.CreateOrderDiscountRequest;
import com.onatsubasi.finalcase.order.application.dto.internal.CreateOrderInternalRequest;
import com.onatsubasi.finalcase.order.application.dto.internal.CreateOrderItemRequest;
import com.onatsubasi.finalcase.order.application.dto.internal.OrderAddressSnapshotRequest;
import com.onatsubasi.finalcase.order.application.dto.response.*;
import com.onatsubasi.finalcase.order.domain.entity.*;
import com.onatsubasi.finalcase.order.domain.enums.OrderAddressType;
import org.springframework.stereotype.Component;

@Component
public class OrderMapper {

    public Order toOrder(
            String orderNumber,
            CreateOrderInternalRequest request
    ) {
        Order order = new Order(
                orderNumber,
                request.checkoutId(),
                request.idempotencyKey(),
                request.requestHash(),
                request.userId(),
                request.basketId(),
                request.inventoryReservationId(),
                request.promotionUsageReservationId(),
                toAddressSnapshot(request.shippingAddress()),
                toAddressSnapshot(request.billingAddress()),
                request.subtotalAmount(),
                request.itemDiscountAmount(),
                request.promotionDiscountAmount(),
                request.shippingFee(),
                request.shippingDiscountAmount(),
                request.taxAmount(),
                request.grandTotalAmount(),
                request.currency()
        );

        request.items()
                .stream()
                .map(this::toOrderItem)
                .forEach(order::addItem);

        if (request.discounts() != null) {
            request.discounts()
                    .stream()
                    .map(this::toOrderDiscount)
                    .forEach(order::addDiscount);
        }

        return order;
    }

    public OrderAddressSnapshot toAddressSnapshot(
            OrderAddressSnapshotRequest request
    ) {
        return new OrderAddressSnapshot(
                request.recipientName(),
                request.recipientPhone(),
                request.country(),
                request.city(),
                request.district(),
                request.neighborhood(),
                request.addressLine1(),
                request.addressLine2(),
                request.postalCode()
        );
    }

    public OrderItem toOrderItem(CreateOrderItemRequest request) {
        return new OrderItem(
                request.productId(),
                request.sku(),
                request.productName(),
                request.slug(),
                request.mainImageUrl(),
                request.brandId(),
                request.brandName(),
                request.categoryId(),
                request.categoryName(),
                request.unitPrice(),
                request.quantity(),
                request.lineSubtotal(),
                request.lineDiscount(),
                request.lineTotal(),
                request.currency()
        );
    }

    public OrderDiscount toOrderDiscount(CreateOrderDiscountRequest request) {
        return new OrderDiscount(
                request.promotionId(),
                request.promotionName(),
                request.couponId(),
                request.couponCode(),
                request.discountAmount(),
                request.shippingDiscountAmount()
        );
    }

    public OrderSummaryResponse toSummaryResponse(Order order) {
        return new OrderSummaryResponse(
                order.getId(),
                order.getOrderNumber(),
                order.getCheckoutId(),
                order.getUserId(),
                order.getStatus(),
                order.getGrandTotalAmount(),
                order.getCurrency(),
                order.getCreatedAt()
        );
    }

    public OrderDetailResponse toDetailResponse(Order order) {
        return new OrderDetailResponse(
                order.getId(),
                order.getOrderNumber(),
                order.getCheckoutId(),
                order.getUserId(),
                order.getBasketId(),
                order.getInventoryReservationId(),
                order.getPromotionUsageReservationId(),
                order.getStatus(),
                toAddressResponse(OrderAddressType.SHIPPING, order.getShippingAddress()),
                toAddressResponse(OrderAddressType.BILLING, order.getBillingAddress()),
                toPaymentSummaryResponse(order.getPaymentSummary()),
                toShipmentSummaryResponse(order.getShipmentSummary()),
                order.getSubtotalAmount(),
                order.getItemDiscountAmount(),
                order.getPromotionDiscountAmount(),
                order.getShippingFee(),
                order.getShippingDiscountAmount(),
                order.getTaxAmount(),
                order.getGrandTotalAmount(),
                order.getCurrency(),
                order.getItems()
                        .stream()
                        .map(this::toItemResponse)
                        .toList(),
                order.getDiscounts()
                        .stream()
                        .map(this::toDiscountResponse)
                        .toList(),
                order.getStatusHistory()
                        .stream()
                        .map(this::toStatusHistoryResponse)
                        .toList(),
                order.getCreatedAt(),
                order.getUpdatedAt()
        );
    }

    public OrderAddressSnapshotResponse toAddressResponse(
            OrderAddressType type,
            OrderAddressSnapshot address
    ) {
        if (address == null) {
            return null;
        }

        return new OrderAddressSnapshotResponse(
                type,
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

    public OrderPaymentSummaryResponse toPaymentSummaryResponse(
            OrderPaymentSummary paymentSummary
    ) {
        if (paymentSummary == null) {
            return null;
        }

        return new OrderPaymentSummaryResponse(
                paymentSummary.getPaymentId(),
                paymentSummary.getPaymentProvider(),
                paymentSummary.getPaymentStatus(),
                paymentSummary.getProviderTransactionId()
        );
    }

    public OrderShipmentSummaryResponse toShipmentSummaryResponse(
            OrderShipmentSummary shipmentSummary
    ) {
        if (shipmentSummary == null) {
            return null;
        }

        return new OrderShipmentSummaryResponse(
                shipmentSummary.getShipmentId(),
                shipmentSummary.getShipmentNumber(),
                shipmentSummary.getCarrier(),
                shipmentSummary.getTrackingNumber(),
                shipmentSummary.getShipmentStatus(),
                shipmentSummary.getShippedAt(),
                shipmentSummary.getDeliveredAt()
        );
    }

    public OrderItemResponse toItemResponse(OrderItem item) {
        return new OrderItemResponse(
                item.getId(),
                item.getProductId(),
                item.getSku(),
                item.getProductName(),
                item.getSlug(),
                item.getMainImageUrl(),
                item.getBrandId(),
                item.getBrandName(),
                item.getCategoryId(),
                item.getCategoryName(),
                item.getUnitPrice(),
                item.getQuantity(),
                item.getLineSubtotal(),
                item.getLineDiscount(),
                item.getLineTotal(),
                item.getCurrency()
        );
    }

    public OrderDiscountResponse toDiscountResponse(OrderDiscount discount) {
        return new OrderDiscountResponse(
                discount.getId(),
                discount.getPromotionId(),
                discount.getPromotionName(),
                discount.getCouponId(),
                discount.getCouponCode(),
                discount.getDiscountAmount(),
                discount.getShippingDiscountAmount()
        );
    }

    public OrderStatusHistoryResponse toStatusHistoryResponse(
            OrderStatusHistory history
    ) {
        return new OrderStatusHistoryResponse(
                history.getId(),
                history.getFromStatus(),
                history.getToStatus(),
                history.getSource(),
                history.getChangedBy(),
                history.getReason(),
                history.getCreatedAt()
        );
    }
}
