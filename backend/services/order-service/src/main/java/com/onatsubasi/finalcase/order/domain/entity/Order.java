package com.onatsubasi.finalcase.order.domain.entity;

import com.onatsubasi.finalcase.common.core.exception.BaseException;
import com.onatsubasi.finalcase.order.domain.enums.OrderStatus;
import com.onatsubasi.finalcase.order.domain.enums.OrderStatusChangeSource;
import com.onatsubasi.finalcase.order.domain.exception.OrderErrorCode;
import jakarta.persistence.*;
import lombok.Getter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

@Entity
@Table(
        name = "orders",
        indexes = {
                @Index(name = "idx_orders_order_number", columnList = "order_number", unique = true),
                @Index(name = "idx_orders_checkout_id", columnList = "checkout_id", unique = true),
                @Index(name = "idx_orders_idempotency_key", columnList = "idempotency_key"),
                @Index(name = "idx_orders_user_id", columnList = "user_id"),
                @Index(name = "idx_orders_status", columnList = "status")
        }
)
public class Order {

    @Getter
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Getter
    @Column(name = "order_number", nullable = false, unique = true, length = 80)
    private String orderNumber;

    @Getter
    @Column(name = "checkout_id", nullable = false, unique = true)
    private UUID checkoutId;

    @Getter
    @Column(name = "idempotency_key", length = 120)
    private String idempotencyKey;

    @Getter
    @Column(name = "request_hash", nullable = false, length = 128)
    private String requestHash;

    @Getter
    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Getter
    @Column(name = "basket_id")
    private UUID basketId;

    @Getter
    @Column(name = "inventory_reservation_id")
    private UUID inventoryReservationId;

    @Getter
    @Column(name = "promotion_usage_reservation_id")
    private UUID promotionUsageReservationId;

    @Getter
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private OrderStatus status = OrderStatus.PENDING_PAYMENT;

    @Getter
    @Embedded
    @AttributeOverrides({
            @AttributeOverride(name = "recipientName", column = @Column(name = "shipping_recipient_name", length = 150)),
            @AttributeOverride(name = "recipientPhone", column = @Column(name = "shipping_recipient_phone", length = 30)),
            @AttributeOverride(name = "country", column = @Column(name = "shipping_country", length = 100)),
            @AttributeOverride(name = "city", column = @Column(name = "shipping_city", length = 100)),
            @AttributeOverride(name = "district", column = @Column(name = "shipping_district", length = 100)),
            @AttributeOverride(name = "neighborhood", column = @Column(name = "shipping_neighborhood", length = 150)),
            @AttributeOverride(name = "addressLine1", column = @Column(name = "shipping_address_line_1", length = 500)),
            @AttributeOverride(name = "addressLine2", column = @Column(name = "shipping_address_line_2", length = 500)),
            @AttributeOverride(name = "postalCode", column = @Column(name = "shipping_postal_code", length = 20))
    })
    private OrderAddressSnapshot shippingAddress;

    @Getter
    @Embedded
    @AttributeOverrides({
            @AttributeOverride(name = "recipientName", column = @Column(name = "billing_recipient_name", length = 150)),
            @AttributeOverride(name = "recipientPhone", column = @Column(name = "billing_recipient_phone", length = 30)),
            @AttributeOverride(name = "country", column = @Column(name = "billing_country", length = 100)),
            @AttributeOverride(name = "city", column = @Column(name = "billing_city", length = 100)),
            @AttributeOverride(name = "district", column = @Column(name = "billing_district", length = 100)),
            @AttributeOverride(name = "neighborhood", column = @Column(name = "billing_neighborhood", length = 150)),
            @AttributeOverride(name = "addressLine1", column = @Column(name = "billing_address_line_1", length = 500)),
            @AttributeOverride(name = "addressLine2", column = @Column(name = "billing_address_line_2", length = 500)),
            @AttributeOverride(name = "postalCode", column = @Column(name = "billing_postal_code", length = 20))
    })
    private OrderAddressSnapshot billingAddress;

    @Getter
    @Embedded
    private OrderPaymentSummary paymentSummary = OrderPaymentSummary.empty();

    @Getter
    @Embedded
    private OrderShipmentSummary shipmentSummary = OrderShipmentSummary.empty();

    @Getter
    @Column(name = "subtotal_amount", nullable = false, precision = 19, scale = 2)
    private BigDecimal subtotalAmount;

    @Getter
    @Column(name = "item_discount_amount", nullable = false, precision = 19, scale = 2)
    private BigDecimal itemDiscountAmount;

    @Getter
    @Column(name = "promotion_discount_amount", nullable = false, precision = 19, scale = 2)
    private BigDecimal promotionDiscountAmount;

    @Getter
    @Column(name = "shipping_fee", nullable = false, precision = 19, scale = 2)
    private BigDecimal shippingFee;

    @Getter
    @Column(name = "shipping_discount_amount", nullable = false, precision = 19, scale = 2)
    private BigDecimal shippingDiscountAmount;

    @Getter
    @Column(name = "tax_amount", nullable = false, precision = 19, scale = 2)
    private BigDecimal taxAmount;

    @Getter
    @Column(name = "grand_total_amount", nullable = false, precision = 19, scale = 2)
    private BigDecimal grandTotalAmount;

    @Getter
    @Column(nullable = false, length = 3)
    private String currency;

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<OrderItem> items = new ArrayList<>();

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<OrderDiscount> discounts = new ArrayList<>();

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<OrderStatusHistory> statusHistory = new ArrayList<>();

    @Version
    private Long version;

    @Getter
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Getter
    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    protected Order() {
    }

    public Order(
            String orderNumber,
            UUID checkoutId,
            String idempotencyKey,
            String requestHash,
            UUID userId,
            UUID basketId,
            UUID inventoryReservationId,
            UUID promotionUsageReservationId,
            OrderAddressSnapshot shippingAddress,
            OrderAddressSnapshot billingAddress,
            BigDecimal subtotalAmount,
            BigDecimal itemDiscountAmount,
            BigDecimal promotionDiscountAmount,
            BigDecimal shippingFee,
            BigDecimal shippingDiscountAmount,
            BigDecimal taxAmount,
            BigDecimal grandTotalAmount,
            String currency
    ) {
        validateRequired(orderNumber, "Order number is required");
        validateUuid(checkoutId, "Checkout id is required");
        validateRequired(requestHash, "Request hash is required");
        validateUuid(userId, "User id is required");
        validateAddress(shippingAddress, "Shipping address is required");
        validateAddress(billingAddress, "Billing address is required");
        validateMoney(subtotalAmount, "Subtotal amount cannot be negative");
        validateMoney(itemDiscountAmount, "Item discount amount cannot be negative");
        validateMoney(promotionDiscountAmount, "Promotion discount amount cannot be negative");
        validateMoney(shippingFee, "Shipping fee cannot be negative");
        validateMoney(shippingDiscountAmount, "Shipping discount amount cannot be negative");
        validateMoney(taxAmount, "Tax amount cannot be negative");
        validateMoney(grandTotalAmount, "Grand total amount cannot be negative");
        validateRequired(currency, "Currency is required");
        validateGrandTotal(subtotalAmount, itemDiscountAmount, promotionDiscountAmount, shippingFee, shippingDiscountAmount, taxAmount, grandTotalAmount);

        this.orderNumber = orderNumber.trim();
        this.checkoutId = checkoutId;
        this.idempotencyKey = normalize(idempotencyKey);
        this.requestHash = requestHash.trim();
        this.userId = userId;
        this.basketId = basketId;
        this.inventoryReservationId = inventoryReservationId;
        this.promotionUsageReservationId = promotionUsageReservationId;
        this.shippingAddress = shippingAddress;
        this.billingAddress = billingAddress;
        this.subtotalAmount = scale(subtotalAmount);
        this.itemDiscountAmount = scale(itemDiscountAmount);
        this.promotionDiscountAmount = scale(promotionDiscountAmount);
        this.shippingFee = scale(shippingFee);
        this.shippingDiscountAmount = scale(shippingDiscountAmount);
        this.taxAmount = scale(taxAmount);
        this.grandTotalAmount = scale(grandTotalAmount);
        this.currency = currency.trim().toUpperCase(Locale.ROOT);
        this.status = OrderStatus.PENDING_PAYMENT;

        addStatusHistory(null, OrderStatus.PENDING_PAYMENT, OrderStatusChangeSource.CHECKOUT_SERVICE, null, "Order created");
    }

    public void addItem(OrderItem item) {
        if (item == null) {
            throw new BaseException(OrderErrorCode.INVALID_ORDER_ITEM_DATA);
        }
        items.add(item);
        item.assignTo(this);
    }

    public void addDiscount(OrderDiscount discount) {
        if (discount == null) {
            throw new BaseException(OrderErrorCode.INVALID_ORDER_DISCOUNT);
        }
        discounts.add(discount);
        discount.assignTo(this);
    }

    public void assertHasItems() {
        if (items.isEmpty()) {
            throw new BaseException(OrderErrorCode.INVALID_ORDER_ITEM_DATA, "Order must contain at least one item");
        }
    }

    public void markPaid(UUID paymentId, String paymentProvider, String paymentStatus, String providerTransactionId, OrderStatusChangeSource source) {
        if (status == OrderStatus.PAID) {
            updatePaymentSummaryIfMissing(paymentId, paymentProvider, paymentStatus, providerTransactionId);
            return;
        }
        transitionTo(OrderStatus.PAID, source, null, "Payment succeeded");
        paymentSummary.update(paymentId, paymentProvider, paymentStatus, providerTransactionId);
    }

    public void markPaymentFailed(UUID paymentId, String paymentProvider, String paymentStatus, String providerTransactionId, OrderStatusChangeSource source) {
        if (status == OrderStatus.PAYMENT_FAILED) {
            updatePaymentSummaryIfMissing(paymentId, paymentProvider, paymentStatus, providerTransactionId);
            return;
        }
        transitionTo(OrderStatus.PAYMENT_FAILED, source, null, "Payment failed");
        paymentSummary.update(paymentId, paymentProvider, paymentStatus, providerTransactionId);
    }

    public void cancel(OrderStatusChangeSource source, String changedBy, String reason) {
        if (status == OrderStatus.CANCELLED) {
            return;
        }
        transitionTo(OrderStatus.CANCELLED, source, changedBy, reason == null ? "Order cancelled" : reason);
    }

    public void markPreparing(OrderStatusChangeSource source, String changedBy, String reason) {
        if (status == OrderStatus.PREPARING) {
            return;
        }
        transitionTo(OrderStatus.PREPARING, source, changedBy, reason == null ? "Order preparing" : reason);
    }

    public void attachShipmentCreated(UUID shipmentId, String shipmentNumber, String carrier, String trackingNumber, String shipmentStatus) {
        if (shipmentSummary.hasShipment() && !shipmentSummary.sameShipment(shipmentId)) {
            throw new BaseException(OrderErrorCode.ORDER_SHIPMENT_ALREADY_ATTACHED);
        }
        if (status == OrderStatus.PENDING_PAYMENT || status == OrderStatus.PAYMENT_FAILED || status == OrderStatus.CANCELLED) {
            throw new BaseException(OrderErrorCode.ORDER_INVALID_STATUS_TRANSITION, "Shipment can be attached only after successful payment");
        }
        shipmentSummary.updateCreated(shipmentId, shipmentNumber, carrier, trackingNumber, shipmentStatus);
    }

    public void markShipped(String carrier, String trackingNumber, Instant shippedAt, OrderStatusChangeSource source) {
        if (status == OrderStatus.SHIPPED) {
            shipmentSummary.markShipped(carrier, trackingNumber, shippedAt);
            return;
        }
        transitionTo(OrderStatus.SHIPPED, source, null, "Order shipped");
        shipmentSummary.markShipped(carrier, trackingNumber, shippedAt);
    }

    public void markDelivered(Instant deliveredAt, OrderStatusChangeSource source) {
        if (status == OrderStatus.DELIVERED) {
            shipmentSummary.markDelivered(deliveredAt);
            return;
        }
        transitionTo(OrderStatus.DELIVERED, source, null, "Order delivered");
        shipmentSummary.markDelivered(deliveredAt);
    }

    public List<OrderItem> getItems() {
        return List.copyOf(items);
    }

    public List<OrderDiscount> getDiscounts() {
        return List.copyOf(discounts);
    }

    public List<OrderStatusHistory> getStatusHistory() {
        return List.copyOf(statusHistory);
    }

    private void transitionTo(OrderStatus targetStatus, OrderStatusChangeSource source, String changedBy, String reason) {
        if (!canTransition(status, targetStatus)) {
            throw new BaseException(OrderErrorCode.ORDER_INVALID_STATUS_TRANSITION, "Cannot transition order from " + status + " to " + targetStatus);
        }
        OrderStatus previous = this.status;
        this.status = targetStatus;
        addStatusHistory(previous, targetStatus, source, changedBy, reason);
    }

    private boolean canTransition(OrderStatus from, OrderStatus to) {
        if (from == to) {
            return true;
        }
        return switch (from) {
            case PENDING_PAYMENT -> to == OrderStatus.PAID || to == OrderStatus.PAYMENT_FAILED || to == OrderStatus.CANCELLED;
            case PAYMENT_FAILED -> to == OrderStatus.CANCELLED;
            case PAID -> to == OrderStatus.PREPARING || to == OrderStatus.CANCELLED || to == OrderStatus.SHIPPED;
            case PREPARING -> to == OrderStatus.SHIPPED || to == OrderStatus.CANCELLED;
            case SHIPPED -> to == OrderStatus.DELIVERED;
            case DELIVERED -> to == OrderStatus.RETURN_REQUESTED;
            case RETURN_REQUESTED -> to == OrderStatus.RETURN_APPROVED || to == OrderStatus.RETURN_REJECTED;
            case RETURN_APPROVED -> to == OrderStatus.REFUNDED;
            case CANCELLED, REFUNDED, RETURN_REJECTED -> false;
        };
    }

    private void addStatusHistory(OrderStatus from, OrderStatus to, OrderStatusChangeSource source, String changedBy, String reason) {
        statusHistory.add(new OrderStatusHistory(this, from, to, source, changedBy, reason));
    }

    private void updatePaymentSummaryIfMissing(UUID paymentId, String paymentProvider, String paymentStatus, String providerTransactionId) {
        if (paymentSummary.getPaymentId() == null && paymentId != null) {
            paymentSummary.update(paymentId, paymentProvider, paymentStatus, providerTransactionId);
        }
    }

    private void validateGrandTotal(BigDecimal subtotalAmount, BigDecimal itemDiscountAmount, BigDecimal promotionDiscountAmount, BigDecimal shippingFee, BigDecimal shippingDiscountAmount, BigDecimal taxAmount, BigDecimal grandTotalAmount) {
        BigDecimal expected = scale(subtotalAmount)
                .subtract(scale(itemDiscountAmount))
                .subtract(scale(promotionDiscountAmount))
                .add(scale(shippingFee))
                .subtract(scale(shippingDiscountAmount))
                .add(scale(taxAmount));
        if (expected.compareTo(scale(grandTotalAmount)) != 0) {
            throw new BaseException(OrderErrorCode.INVALID_ORDER_TOTALS, "Grand total amount does not match order monetary breakdown");
        }
    }

    private void validateUuid(UUID value, String message) {
        if (value == null) {
            throw new BaseException(OrderErrorCode.INVALID_ORDER_DATA, message);
        }
    }

    private void validateAddress(OrderAddressSnapshot value, String message) {
        if (value == null) {
            throw new BaseException(OrderErrorCode.INVALID_ORDER_ADDRESS, message);
        }
    }

    private void validateMoney(BigDecimal value, String message) {
        if (value == null || value.compareTo(BigDecimal.ZERO) < 0) {
            throw new BaseException(OrderErrorCode.INVALID_ORDER_TOTALS, message);
        }
    }

    private void validateRequired(String value, String message) {
        if (value == null || value.isBlank()) {
            throw new BaseException(OrderErrorCode.INVALID_ORDER_DATA, message);
        }
    }

    private BigDecimal scale(BigDecimal value) {
        return value.setScale(2);
    }

    private String normalize(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }
}
