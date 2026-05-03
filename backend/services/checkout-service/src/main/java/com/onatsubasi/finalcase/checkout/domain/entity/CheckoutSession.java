package com.onatsubasi.finalcase.checkout.domain.entity;

import com.onatsubasi.finalcase.checkout.domain.enums.CheckoutCompensationStatus;
import com.onatsubasi.finalcase.checkout.domain.enums.CheckoutSagaStepName;
import com.onatsubasi.finalcase.checkout.domain.enums.CheckoutSagaStepStatus;
import com.onatsubasi.finalcase.checkout.domain.enums.CheckoutStatus;
import com.onatsubasi.finalcase.checkout.domain.exception.CheckoutErrorCode;
import com.onatsubasi.finalcase.common.core.exception.BaseException;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.util.*;

@Getter
@Entity
@Table(
        name = "checkout_sessions",
        indexes = {
                @Index(name = "idx_checkout_sessions_user_id", columnList = "user_id"),
                @Index(name = "idx_checkout_sessions_basket_id", columnList = "basket_id"),
                @Index(name = "idx_checkout_sessions_order_id", columnList = "order_id"),
                @Index(name = "idx_checkout_sessions_payment_id", columnList = "payment_id"),
                @Index(name = "idx_checkout_sessions_status", columnList = "status"),
                @Index(name = "idx_checkout_sessions_idempotency_key", columnList = "idempotency_key"),
                @Index(name = "idx_checkout_sessions_expires_at", columnList = "expires_at")
        },
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_checkout_sessions_idempotency_key",
                        columnNames = "idempotency_key"
                )
        }
)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class CheckoutSession {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(nullable = false, updatable = false)
    private UUID id;

    @Column(name = "user_id", nullable = false, updatable = false)
    private UUID userId;

    @Column(name = "basket_id")
    private UUID basketId;

    @Column(name = "order_id")
    private UUID orderId;

    @Column(name = "order_number", length = 80)
    private String orderNumber;

    @Column(name = "payment_id")
    private UUID paymentId;

    @Column(name = "payment_attempt_id")
    private UUID paymentAttemptId;

    @Column(name = "payment_session_id", length = 180)
    private String paymentSessionId;

    @Column(name = "payment_redirect_url", length = 1000)
    private String paymentRedirectUrl;

    @Column(name = "inventory_reservation_id")
    private UUID inventoryReservationId;

    @Column(name = "promotion_usage_reservation_id")
    private UUID promotionUsageReservationId;

    @Column(name = "shipment_id")
    private UUID shipmentId;

    @Column(name = "idempotency_key", nullable = false, length = 120)
    private String idempotencyKey;

    @Column(name = "request_hash", nullable = false, length = 128)
    private String requestHash;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private CheckoutStatus status;

    @Enumerated(EnumType.STRING)
    @Column(name = "compensation_status", nullable = false, length = 50)
    private CheckoutCompensationStatus compensationStatus = CheckoutCompensationStatus.NOT_REQUIRED;

    @Column(nullable = false, length = 10)
    private String currency;

    @Column(name = "subtotal_amount", nullable = false, precision = 19, scale = 2)
    private BigDecimal subtotalAmount = zero();

    @Column(name = "item_discount_amount", nullable = false, precision = 19, scale = 2)
    private BigDecimal itemDiscountAmount = zero();

    @Column(name = "promotion_discount_amount", nullable = false, precision = 19, scale = 2)
    private BigDecimal promotionDiscountAmount = zero();

    @Column(name = "shipping_fee", nullable = false, precision = 19, scale = 2)
    private BigDecimal shippingFee = zero();

    @Column(name = "shipping_discount_amount", nullable = false, precision = 19, scale = 2)
    private BigDecimal shippingDiscountAmount = zero();

    @Column(name = "tax_amount", nullable = false, precision = 19, scale = 2)
    private BigDecimal taxAmount = zero();

    @Column(name = "grand_total_amount", nullable = false, precision = 19, scale = 2)
    private BigDecimal grandTotalAmount = zero();

    @Column(name = "failure_code", length = 100)
    private String failureCode;

    @Column(name = "failure_message", length = 1000)
    private String failureMessage;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "quote_snapshot", columnDefinition = "jsonb")
    private Map<String, Object> quoteSnapshot = new LinkedHashMap<>();

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "payment_action_snapshot", columnDefinition = "jsonb")
    private Map<String, Object> paymentActionSnapshot = new LinkedHashMap<>();

    @Column(name = "expires_at")
    private Instant expiresAt;

    @Column(name = "payment_initialized_at")
    private Instant paymentInitializedAt;

    @Column(name = "completed_at")
    private Instant completedAt;

    @Column(name = "failed_at")
    private Instant failedAt;

    @Column(name = "cancelled_at")
    private Instant cancelledAt;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @Version
    @Column(nullable = false)
    private Long version;

    @OneToMany(
            mappedBy = "checkoutSession",
            cascade = CascadeType.ALL,
            orphanRemoval = true
    )
    private List<CheckoutItem> items = new ArrayList<>();

    @OneToMany(
            mappedBy = "checkoutSession",
            cascade = CascadeType.ALL,
            orphanRemoval = true
    )
    private List<CheckoutAddress> addresses = new ArrayList<>();

    @OneToMany(
            mappedBy = "checkoutSession",
            cascade = CascadeType.ALL,
            orphanRemoval = true
    )
    private List<CheckoutDiscount> discounts = new ArrayList<>();

    @OneToMany(
            mappedBy = "checkoutSession",
            cascade = CascadeType.ALL,
            orphanRemoval = true
    )
    private List<CheckoutSagaStep> sagaSteps = new ArrayList<>();

    private CheckoutSession(
            UUID userId,
            String idempotencyKey,
            String requestHash,
            String currency,
            Instant expiresAt
    ) {
        validateUuid(userId, "User id is required");
        validateRequired(idempotencyKey, "Idempotency key is required");
        validateRequired(requestHash, "Request hash is required");
        validateCurrency(currency);

        this.userId = userId;
        this.idempotencyKey = normalize(idempotencyKey, 120);
        this.requestHash = normalize(requestHash, 128);
        this.currency = currency.trim().toUpperCase(Locale.ROOT);
        this.status = CheckoutStatus.STARTED;
        this.compensationStatus = CheckoutCompensationStatus.NOT_REQUIRED;
        this.expiresAt = expiresAt;
        this.createdAt = Instant.now();
        this.updatedAt = this.createdAt;
    }

    public static CheckoutSession start(
            UUID userId,
            String idempotencyKey,
            String requestHash,
            String currency,
            Instant expiresAt
    ) {
        return new CheckoutSession(
                userId,
                idempotencyKey,
                requestHash,
                currency,
                expiresAt
        );
    }

    /**
     * Transitional constructor for old generated application code.
     * New application layer should use start(...) and attach snapshots explicitly.
     */
    public CheckoutSession(
            UUID userId,
            UUID basketId,
            String idempotencyKey,
            String requestHash,
            BigDecimal subtotalAmount,
            BigDecimal promotionDiscountAmount,
            BigDecimal shippingFee,
            BigDecimal shippingDiscountAmount,
            BigDecimal taxAmount,
            BigDecimal grandTotalAmount,
            String currency,
            Map<String, Object> quoteSnapshot
    ) {
        this(
                userId,
                idempotencyKey,
                requestHash,
                currency,
                null
        );

        this.status = CheckoutStatus.SUBMITTED;
        this.basketId = basketId;
        replaceTotals(
                subtotalAmount,
                zero(),
                promotionDiscountAmount,
                shippingFee,
                shippingDiscountAmount,
                taxAmount,
                grandTotalAmount
        );
        this.quoteSnapshot = normalizeMap(quoteSnapshot);
    }

    public void attachBasket(UUID basketId) {
        ensureNotTerminal();
        validateUuid(basketId, "Basket id is required");

        this.basketId = basketId;
        touch();
    }

    public void replaceTotals(
            BigDecimal subtotalAmount,
            BigDecimal itemDiscountAmount,
            BigDecimal promotionDiscountAmount,
            BigDecimal shippingFee,
            BigDecimal shippingDiscountAmount,
            BigDecimal taxAmount,
            BigDecimal grandTotalAmount
    ) {
        validateMoney(subtotalAmount, "Subtotal amount cannot be negative");
        validateMoney(itemDiscountAmount, "Item discount amount cannot be negative");
        validateMoney(promotionDiscountAmount, "Promotion discount amount cannot be negative");
        validateMoney(shippingFee, "Shipping fee cannot be negative");
        validateMoney(shippingDiscountAmount, "Shipping discount amount cannot be negative");
        validateMoney(taxAmount, "Tax amount cannot be negative");
        validateMoney(grandTotalAmount, "Grand total amount cannot be negative");

        BigDecimal expectedGrandTotal = money(subtotalAmount)
                .subtract(money(itemDiscountAmount))
                .subtract(money(promotionDiscountAmount))
                .add(money(shippingFee))
                .subtract(money(shippingDiscountAmount))
                .add(money(taxAmount));

        if (expectedGrandTotal.compareTo(money(grandTotalAmount)) != 0) {
            throw new BaseException(
                    CheckoutErrorCode.INVALID_CHECKOUT_TOTALS,
                    "Grand total does not match checkout formula"
            );
        }

        this.subtotalAmount = money(subtotalAmount);
        this.itemDiscountAmount = money(itemDiscountAmount);
        this.promotionDiscountAmount = money(promotionDiscountAmount);
        this.shippingFee = money(shippingFee);
        this.shippingDiscountAmount = money(shippingDiscountAmount);
        this.taxAmount = money(taxAmount);
        this.grandTotalAmount = money(grandTotalAmount);
        touch();
    }

    public void replaceQuoteSnapshot(Map<String, Object> quoteSnapshot) {
        this.quoteSnapshot = normalizeMap(quoteSnapshot);
        touch();
    }

    public void clearSnapshotLines() {
        this.items.clear();
        this.addresses.clear();
        this.discounts.clear();
        touch();
    }

    public void addItem(CheckoutItem item) {
        ensureNotTerminal();

        if (item == null) {
            throw new BaseException(CheckoutErrorCode.INVALID_CHECKOUT_DATA, "Checkout item is required");
        }

        item.assignTo(this);
        this.items.add(item);
        touch();
    }

    public void addAddress(CheckoutAddress address) {
        ensureNotTerminal();

        if (address == null) {
            throw new BaseException(CheckoutErrorCode.INVALID_CHECKOUT_DATA, "Checkout address is required");
        }

        address.assignTo(this);
        this.addresses.add(address);
        touch();
    }

    public void addDiscount(CheckoutDiscount discount) {
        ensureNotTerminal();

        if (discount == null) {
            throw new BaseException(CheckoutErrorCode.INVALID_CHECKOUT_DATA, "Checkout discount is required");
        }

        discount.assignTo(this);
        this.discounts.add(discount);
        touch();
    }

    public void attachInventoryReservation(UUID inventoryReservationId) {
        ensureBeforeCompletion();
        validateUuid(inventoryReservationId, "Inventory reservation id is required");

        this.inventoryReservationId = inventoryReservationId;
        completeStep(CheckoutSagaStepName.INVENTORY_RESERVED, inventoryReservationId.toString());
        touch();
    }

    public void attachPromotionUsageReservation(UUID promotionUsageReservationId) {
        ensureBeforeCompletion();

        if (promotionUsageReservationId == null) {
            return;
        }

        this.promotionUsageReservationId = promotionUsageReservationId;
        completeStep(CheckoutSagaStepName.PROMOTION_RESERVED, promotionUsageReservationId.toString());
        touch();
    }

    public void attachOrder(UUID orderId, String orderNumber) {
        ensureBeforeCompletion();
        validateUuid(orderId, "Order id is required");
        validateRequired(orderNumber, "Order number is required");

        this.orderId = orderId;
        this.orderNumber = normalize(orderNumber, 80);
        completeStep(CheckoutSagaStepName.ORDER_CREATED, orderId.toString());
        touch();
    }

    public void attachPaymentAction(
            UUID paymentId,
            UUID paymentAttemptId,
            String paymentSessionId,
            String paymentRedirectUrl,
            Map<String, Object> paymentActionSnapshot
    ) {
        ensureBeforeCompletion();
        validateUuid(paymentId, "Payment id is required");

        this.paymentId = paymentId;
        this.paymentAttemptId = paymentAttemptId;
        this.paymentSessionId = normalize(paymentSessionId, 180);
        this.paymentRedirectUrl = normalize(paymentRedirectUrl, 1000);
        this.paymentActionSnapshot = normalizeMap(paymentActionSnapshot);
        this.paymentInitializedAt = Instant.now();
        this.status = CheckoutStatus.PAYMENT_PENDING;
        completeStep(CheckoutSagaStepName.PAYMENT_INITIALIZED, paymentId.toString());
        touch();
    }

    /**
     * Transitional method for older application code.
     */
    public void attachPaymentAction(
            UUID paymentId,
            String paymentSessionId,
            String paymentRedirectUrl,
            Map<String, Object> paymentActionSnapshot
    ) {
        attachPaymentAction(
                paymentId,
                null,
                paymentSessionId,
                paymentRedirectUrl,
                paymentActionSnapshot
        );
    }

    public void markPaymentPending(
            UUID inventoryReservationId,
            UUID promotionUsageReservationId,
            UUID orderId,
            String orderNumber,
            UUID paymentId,
            String paymentSessionId,
            String paymentRedirectUrl,
            Map<String, Object> paymentActionSnapshot
    ) {
        if (status == CheckoutStatus.PAYMENT_PENDING) {
            return;
        }

        attachInventoryReservation(inventoryReservationId);
        attachPromotionUsageReservation(promotionUsageReservationId);
        attachOrder(orderId, orderNumber);
        attachPaymentAction(paymentId, null, paymentSessionId, paymentRedirectUrl, paymentActionSnapshot);
    }

    public void markPaymentSucceeded() {
        if (status == CheckoutStatus.PAYMENT_SUCCEEDED || status == CheckoutStatus.COMPLETED) {
            return;
        }

        if (status != CheckoutStatus.PAYMENT_PENDING && status != CheckoutStatus.FINALIZATION_FAILED) {
            throw new BaseException(CheckoutErrorCode.CHECKOUT_INVALID_STATUS);
        }

        this.status = CheckoutStatus.PAYMENT_SUCCEEDED;
        completeStep(CheckoutSagaStepName.PAYMENT_SUCCEEDED_RECEIVED, paymentIdText());
        touch();
    }

    public void markPaymentFailed(String failureCode, String failureMessage) {
        if (status == CheckoutStatus.PAYMENT_FAILED
                || status == CheckoutStatus.COMPENSATED
                || status == CheckoutStatus.FAILED) {
            return;
        }

        if (status == CheckoutStatus.COMPLETED) {
            throw new BaseException(CheckoutErrorCode.CHECKOUT_ALREADY_COMPLETED);
        }

        this.status = CheckoutStatus.PAYMENT_FAILED;
        this.failureCode = normalize(failureCode, 100);
        this.failureMessage = normalize(failureMessage, 1000);
        this.failedAt = Instant.now();
        completeStep(CheckoutSagaStepName.PAYMENT_FAILED_RECEIVED, paymentIdText());
        touch();
    }

    public void markCompensationPending(String failureCode, String failureMessage) {
        if (status == CheckoutStatus.COMPLETED) {
            throw new BaseException(CheckoutErrorCode.CHECKOUT_ALREADY_COMPLETED);
        }

        this.status = CheckoutStatus.COMPENSATION_PENDING;
        this.compensationStatus = CheckoutCompensationStatus.PENDING;
        this.failureCode = normalize(failureCode, 100);
        this.failureMessage = normalize(failureMessage, 1000);
        completeStep(CheckoutSagaStepName.CHECKOUT_COMPENSATION_PENDING, null);
        touch();
    }

    public void markCompleted(UUID shipmentId) {
        if (status == CheckoutStatus.COMPLETED) {
            return;
        }

        if (status != CheckoutStatus.PAYMENT_SUCCEEDED
                && status != CheckoutStatus.PAYMENT_PENDING
                && status != CheckoutStatus.FINALIZATION_FAILED) {
            throw new BaseException(CheckoutErrorCode.CHECKOUT_INVALID_STATUS);
        }

        this.shipmentId = shipmentId;
        this.status = CheckoutStatus.COMPLETED;
        this.compensationStatus = CheckoutCompensationStatus.NOT_REQUIRED;
        this.completedAt = Instant.now();
        completeStep(CheckoutSagaStepName.CHECKOUT_COMPLETED, null);
        touch();
    }

    public void markFailed(String failureCode, String failureMessage) {
        if (status == CheckoutStatus.FAILED) {
            return;
        }

        if (status == CheckoutStatus.COMPLETED) {
            throw new BaseException(CheckoutErrorCode.CHECKOUT_ALREADY_COMPLETED);
        }

        this.status = CheckoutStatus.FAILED;
        this.failureCode = normalize(failureCode, 100);
        this.failureMessage = normalize(failureMessage, 1000);
        this.failedAt = Instant.now();
        failStep(CheckoutSagaStepName.CHECKOUT_FAILED, failureCode, failureMessage);
        touch();
    }

    /**
     * Transitional method for older application code.
     */
    public void markFailed() {
        markFailed(null, null);
    }

    public void markCompensated() {
        if (status == CheckoutStatus.COMPENSATED) {
            return;
        }

        if (status == CheckoutStatus.COMPLETED) {
            throw new BaseException(CheckoutErrorCode.CHECKOUT_ALREADY_COMPLETED);
        }

        this.status = CheckoutStatus.COMPENSATED;
        this.compensationStatus = CheckoutCompensationStatus.SUCCEEDED;
        completeStep(CheckoutSagaStepName.CHECKOUT_COMPENSATED, null);
        touch();
    }

    public void markFinalizationFailed(String failureCode, String failureMessage) {
        if (status == CheckoutStatus.COMPLETED) {
            return;
        }

        this.status = CheckoutStatus.FINALIZATION_FAILED;
        this.failureCode = normalize(failureCode, 100);
        this.failureMessage = normalize(failureMessage, 1000);
        failStep(CheckoutSagaStepName.FINALIZATION_FAILED, failureCode, failureMessage);
        touch();
    }

    /**
     * Transitional method for older application code.
     */
    public void markFinalizationFailed() {
        markFinalizationFailed(null, null);
    }

    public void markCompensationFailed(String failureCode, String failureMessage) {
        if (status == CheckoutStatus.COMPLETED) {
            throw new BaseException(CheckoutErrorCode.CHECKOUT_ALREADY_COMPLETED);
        }

        this.status = CheckoutStatus.COMPENSATION_FAILED;
        this.compensationStatus = CheckoutCompensationStatus.FAILED;
        this.failureCode = normalize(failureCode, 100);
        this.failureMessage = normalize(failureMessage, 1000);
        failStep(CheckoutSagaStepName.COMPENSATION_FAILED, failureCode, failureMessage);
        touch();
    }

    /**
     * Transitional method for older application code.
     */
    public void markCompensationFailed() {
        markCompensationFailed(null, null);
    }

    public void cancel(String reason) {
        if (status == CheckoutStatus.COMPLETED) {
            throw new BaseException(CheckoutErrorCode.CHECKOUT_ALREADY_COMPLETED);
        }

        if (isTerminal()) {
            return;
        }

        this.status = CheckoutStatus.CANCELLED;
        this.cancelledAt = Instant.now();
        this.failureMessage = normalize(reason, 1000);
        completeStep(CheckoutSagaStepName.CHECKOUT_CANCELLED, null);
        touch();
    }

    /**
     * Transitional method for older application code.
     */
    public void cancel() {
        cancel(null);
    }

    public void expire() {
        if (isTerminal()) {
            return;
        }

        this.status = CheckoutStatus.EXPIRED;
        this.failedAt = Instant.now();
        completeStep(CheckoutSagaStepName.CHECKOUT_EXPIRED, null);
        touch();
    }

    public void addStep(CheckoutSagaStepName stepName) {
        CheckoutSagaStep step = CheckoutSagaStep.start(stepName);
        step.assignTo(this);
        sagaSteps.add(step);
        touch();
    }

    public void completeStep(CheckoutSagaStepName stepName) {
        completeStep(stepName, null);
    }

    public void completeStep(CheckoutSagaStepName stepName, String externalReferenceId) {
        findLatestStep(stepName)
                .ifPresentOrElse(
                        step -> step.succeed(externalReferenceId),
                        () -> {
                            CheckoutSagaStep step = CheckoutSagaStep.start(stepName);
                            step.assignTo(this);
                            step.succeed(externalReferenceId);
                            sagaSteps.add(step);
                        }
                );

        touch();
    }

    public void failStep(
            CheckoutSagaStepName stepName,
            String errorMessage
    ) {
        failStep(stepName, null, errorMessage);
    }

    public void failStep(
            CheckoutSagaStepName stepName,
            String errorCode,
            String errorMessage
    ) {
        findLatestStep(stepName)
                .ifPresentOrElse(
                        step -> step.fail(errorCode, errorMessage),
                        () -> {
                            CheckoutSagaStep step = CheckoutSagaStep.start(stepName);
                            step.assignTo(this);
                            step.fail(errorCode, errorMessage);
                            sagaSteps.add(step);
                        }
                );

        touch();
    }

    public void compensateStep(CheckoutSagaStepName stepName, String externalReferenceId) {
        findLatestStep(stepName)
                .ifPresentOrElse(
                        step -> step.compensate(externalReferenceId),
                        () -> {
                            CheckoutSagaStep step = CheckoutSagaStep.start(stepName);
                            step.assignTo(this);
                            step.compensate(externalReferenceId);
                            sagaSteps.add(step);
                        }
                );

        touch();
    }

    public boolean isStepSucceeded(CheckoutSagaStepName stepName) {
        return sagaSteps.stream()
                .anyMatch(step -> step.getStepName() == stepName
                        && (step.getStatus() == CheckoutSagaStepStatus.SUCCEEDED
                        || step.getStatus() == CheckoutSagaStepStatus.COMPLETED));
    }

    public boolean isStepCompleted(CheckoutSagaStepName stepName) {
        return isStepSucceeded(stepName);
    }

    public boolean isTerminal() {
        return status == CheckoutStatus.COMPLETED
                || status == CheckoutStatus.FAILED
                || status == CheckoutStatus.COMPENSATED
                || status == CheckoutStatus.CANCELLED
                || status == CheckoutStatus.EXPIRED;
    }

    public boolean isOwnedBy(UUID currentUserId) {
        return currentUserId != null && currentUserId.equals(userId);
    }

    public void assertOwnedBy(UUID currentUserId) {
        if (!isOwnedBy(currentUserId)) {
            throw new BaseException(CheckoutErrorCode.CHECKOUT_ACCESS_DENIED);
        }
    }

    public boolean isExpiredAt(Instant now) {
        return expiresAt != null
                && now != null
                && now.isAfter(expiresAt)
                && !isTerminal();
    }

    public boolean isSameRequestHash(String requestHash) {
        return requestHash != null && this.requestHash.equals(requestHash);
    }

    public List<CheckoutItem> getItems() {
        return items == null ? List.of() : List.copyOf(items);
    }

    public List<CheckoutAddress> getAddresses() {
        return addresses == null ? List.of() : List.copyOf(addresses);
    }

    public List<CheckoutDiscount> getDiscounts() {
        return discounts == null ? List.of() : List.copyOf(discounts);
    }

    public List<CheckoutSagaStep> getSagaSteps() {
        return sagaSteps == null ? List.of() : List.copyOf(sagaSteps);
    }

    public Map<String, Object> getQuoteSnapshot() {
        return quoteSnapshot == null ? Map.of() : Map.copyOf(quoteSnapshot);
    }

    public Map<String, Object> getPaymentActionSnapshot() {
        return paymentActionSnapshot == null ? Map.of() : Map.copyOf(paymentActionSnapshot);
    }

    private Optional<CheckoutSagaStep> findLatestStep(CheckoutSagaStepName stepName) {
        if (sagaSteps == null || sagaSteps.isEmpty()) {
            return Optional.empty();
        }

        List<CheckoutSagaStep> matching = sagaSteps.stream()
                .filter(step -> step.getStepName() == stepName)
                .toList();

        if (matching.isEmpty()) {
            return Optional.empty();
        }

        return Optional.of(matching.get(matching.size() - 1));
    }

    private void ensureNotTerminal() {
        if (isTerminal()) {
            throw new BaseException(CheckoutErrorCode.CHECKOUT_INVALID_STATUS);
        }
    }

    private void ensureBeforeCompletion() {
        if (status == CheckoutStatus.COMPLETED) {
            throw new BaseException(CheckoutErrorCode.CHECKOUT_ALREADY_COMPLETED);
        }

        if (status == CheckoutStatus.CANCELLED || status == CheckoutStatus.EXPIRED) {
            throw new BaseException(CheckoutErrorCode.CHECKOUT_INVALID_STATUS);
        }
    }

    private String paymentIdText() {
        return paymentId == null ? null : paymentId.toString();
    }

    private void validateUuid(UUID value, String message) {
        if (value == null) {
            throw new BaseException(CheckoutErrorCode.INVALID_CHECKOUT_DATA, message);
        }
    }

    private void validateRequired(String value, String message) {
        if (value == null || value.isBlank()) {
            throw new BaseException(CheckoutErrorCode.INVALID_CHECKOUT_DATA, message);
        }
    }

    private void validateCurrency(String currency) {
        if (currency == null || currency.isBlank() || currency.trim().length() < 3) {
            throw new BaseException(
                    CheckoutErrorCode.INVALID_CHECKOUT_DATA,
                    "Currency is required"
            );
        }
    }

    private void validateMoney(BigDecimal value, String message) {
        if (value == null || value.compareTo(BigDecimal.ZERO) < 0) {
            throw new BaseException(CheckoutErrorCode.INVALID_CHECKOUT_TOTALS, message);
        }
    }

    private BigDecimal money(BigDecimal value) {
        return value.setScale(2, RoundingMode.HALF_UP);
    }

    private static BigDecimal zero() {
        return BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
    }

    private String normalize(String value, int maxLength) {
        if (value == null || value.isBlank()) {
            return null;
        }

        String normalized = value.trim();

        return normalized.length() > maxLength
                ? normalized.substring(0, maxLength)
                : normalized;
    }

    private Map<String, Object> normalizeMap(Map<String, Object> value) {
        if (value == null || value.isEmpty()) {
            return new LinkedHashMap<>();
        }

        return new LinkedHashMap<>(value);
    }

    private void touch() {
        this.updatedAt = Instant.now();
    }

    @PrePersist
    protected void prePersist() {
        if (status == null) {
            status = CheckoutStatus.STARTED;
        }

        if (compensationStatus == null) {
            compensationStatus = CheckoutCompensationStatus.NOT_REQUIRED;
        }

        if (currency != null) {
            currency = currency.trim().toUpperCase(Locale.ROOT);
        }

        if (quoteSnapshot == null) {
            quoteSnapshot = new LinkedHashMap<>();
        }

        if (paymentActionSnapshot == null) {
            paymentActionSnapshot = new LinkedHashMap<>();
        }

        if (items == null) {
            items = new ArrayList<>();
        }

        if (addresses == null) {
            addresses = new ArrayList<>();
        }

        if (discounts == null) {
            discounts = new ArrayList<>();
        }

        if (sagaSteps == null) {
            sagaSteps = new ArrayList<>();
        }

        if (createdAt == null) {
            createdAt = Instant.now();
        }

        if (updatedAt == null) {
            updatedAt = createdAt;
        }
    }

    @PreUpdate
    protected void preUpdate() {
        updatedAt = Instant.now();
    }
}
