package com.onatsubasi.finalcase.checkout.application.service;

import com.onatsubasi.finalcase.checkout.application.client.*;
import com.onatsubasi.finalcase.checkout.application.dto.client.*;
import com.onatsubasi.finalcase.checkout.domain.exception.CheckoutErrorCode;
import com.onatsubasi.finalcase.common.core.exception.BaseException;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class CheckoutDownstreamGateway {

    private final BasketClient basketClient;
    private final CatalogClient catalogClient;
    private final UserClient userClient;
    private final InventoryClient inventoryClient;
    private final PromotionClient promotionClient;
    private final OrderClient orderClient;
    private final PaymentClient paymentClient;
    private final ShipmentClient shipmentClient;
    private final CheckoutDownstreamResponseExtractor extractor;

    @CircuitBreaker(name = "basket-service", fallbackMethod = "basketSnapshotFallback")
    public BasketSnapshotClientResponse getBasketSnapshot(UUID basketId, UUID userId) {
        log.debug(
                "event=checkout.downstream_call service=basket-service operation=getBasketSnapshot basketId={} userId={}",
                basketId,
                userId
        );

        return extractor.extract(
                basketClient.getBasketSnapshot(basketId, userId),
                CheckoutErrorCode.DOWNSTREAM_BASKET_FAILED
        );
    }

    public BasketSnapshotClientResponse basketSnapshotFallback(
            UUID basketId,
            UUID userId,
            Throwable ex
    ) {
        log.warn(
                "event=checkout.downstream_unavailable service=basket-service operation=getBasketSnapshot basketId={} userId={} reason={}",
                basketId,
                userId,
                ex.getMessage()
        );

        throw new BaseException(
                CheckoutErrorCode.DOWNSTREAM_BASKET_FAILED,
                "Basket service is temporarily unavailable"
        );
    }

    @CircuitBreaker(name = "catalog-service", fallbackMethod = "productSnapshotsFallback")
    public List<CatalogProductSnapshotClientResponse> getProductSnapshots(
            CatalogProductSnapshotsClientRequest request
    ) {
        log.debug(
                "event=checkout.downstream_call service=catalog-service operation=getProductSnapshots productCount={}",
                request.productIds() == null ? 0 : request.productIds().size()
        );

        return extractor.extract(
                catalogClient.getProductSnapshots(request),
                CheckoutErrorCode.DOWNSTREAM_CATALOG_FAILED
        );
    }

    public List<CatalogProductSnapshotClientResponse> productSnapshotsFallback(
            CatalogProductSnapshotsClientRequest request,
            Throwable ex
    ) {
        log.warn(
                "event=checkout.downstream_unavailable service=catalog-service operation=getProductSnapshots productCount={} reason={}",
                request.productIds() == null ? 0 : request.productIds().size(),
                ex.getMessage()
        );

        throw new BaseException(
                CheckoutErrorCode.DOWNSTREAM_CATALOG_FAILED,
                "Product data is temporarily unavailable"
        );
    }

    @CircuitBreaker(name = "userService", fallbackMethod = "addressSnapshotFallback")
    public AddressSnapshotsClientResponse getAddressSnapshots(UUID userId, UUID shippingAddressId, UUID billingAddressId) {
        log.debug(
                "event=checkout.downstream_call service=user-service operation=getAddressSnapshots userId={} shippingAddressId={} billingAddressId={}",
                userId,
                shippingAddressId,
                billingAddressId
        );

        return extractor.extract(
                userClient.getAddressSnapshots(new AddressSnapshotClientRequest(
                        userId,
                        shippingAddressId,
                        billingAddressId == null ? shippingAddressId : billingAddressId
                )),
                CheckoutErrorCode.DOWNSTREAM_USER_FAILED
        );
    }

    public AddressSnapshotsClientResponse addressSnapshotFallback(
            UUID userId,
            UUID shippingAddressId,
            UUID billingAddressId,
            Throwable ex
    ) {
        log.warn(
                "event=checkout.downstream_unavailable service=user-service operation=getAddressSnapshots userId={} shippingAddressId={} billingAddressId={} reason={}",
                userId,
                shippingAddressId,
                billingAddressId,
                ex.getMessage()
        );

        throw new BaseException(
                CheckoutErrorCode.DOWNSTREAM_USER_FAILED,
                "Address data is temporarily unavailable"
        );
    }

    @CircuitBreaker(name = "inventory-service", fallbackMethod = "reserveStockFallback")
    public InventoryReservationClientResponse reserveStock(
            InventoryReserveClientRequest request
    ) {
        log.info(
                "event=checkout.inventory_reserve_requested checkoutId={} userId={} itemCount={}",
                request.checkoutId(),
                request.userId(),
                request.items() == null ? 0 : request.items().size()
        );

        return extractor.extract(
                inventoryClient.reserveStock(idempotencyKey("inventory-reserve", request.checkoutId()), request),
                CheckoutErrorCode.DOWNSTREAM_INVENTORY_FAILED
        );
    }

    public InventoryReservationClientResponse reserveStockFallback(
            InventoryReserveClientRequest request,
            Throwable ex
    ) {
        log.warn(
                "event=checkout.downstream_unavailable service=inventory-service operation=reserveStock checkoutId={} reason={}",
                request.checkoutId(),
                ex.getMessage()
        );

        throw new BaseException(
                CheckoutErrorCode.DOWNSTREAM_INVENTORY_FAILED,
                "Stock reservation is temporarily unavailable"
        );
    }

    @CircuitBreaker(name = "inventory-service", fallbackMethod = "confirmReservationFallback")
    public InventoryReservationClientResponse confirmReservation(UUID reservationId, UUID orderId) {
        log.info(
                "event=checkout.inventory_confirm_requested inventoryReservationId={} orderId={}",
                reservationId,
                orderId
        );

        return extractor.extract(
                inventoryClient.confirmReservation(
                        reservationId,
                        new ConfirmReservationClientRequest(orderId)
                ),
                CheckoutErrorCode.DOWNSTREAM_INVENTORY_FAILED
        );
    }

    public InventoryReservationClientResponse confirmReservationFallback(
            UUID reservationId,
            UUID orderId,
            Throwable ex
    ) {
        log.error(
                "event=checkout.inventory_confirm_unavailable inventoryReservationId={} orderId={} reason={}",
                reservationId,
                orderId,
                ex.getMessage()
        );

        throw new BaseException(
                CheckoutErrorCode.DOWNSTREAM_INVENTORY_FAILED,
                "Inventory confirmation is temporarily unavailable"
        );
    }

    @CircuitBreaker(name = "inventory-service", fallbackMethod = "releaseReservationFallback")
    public InventoryReservationClientResponse releaseReservation(UUID reservationId) {
        log.info(
                "event=checkout.inventory_release_requested inventoryReservationId={}",
                reservationId
        );

        return extractor.extract(
                inventoryClient.releaseReservation(
                        reservationId,
                        new ReleaseReservationClientRequest("CHECKOUT_FAILED")
                ),
                CheckoutErrorCode.DOWNSTREAM_INVENTORY_FAILED
        );
    }

    public InventoryReservationClientResponse releaseReservationFallback(
            UUID reservationId,
            Throwable ex
    ) {
        log.error(
                "event=checkout.inventory_release_unavailable inventoryReservationId={} reason={}",
                reservationId,
                ex.getMessage()
        );

        throw new BaseException(
                CheckoutErrorCode.DOWNSTREAM_INVENTORY_FAILED,
                "Inventory release is temporarily unavailable"
        );
    }

    @CircuitBreaker(name = "promotionService", fallbackMethod = "promotionQuoteFallback")
    public PromotionQuoteClientResponse quotePromotion(
            PromotionQuoteClientRequest request
    ) {
        log.info(
                "event=checkout.promotion_quote_requested userId={} couponPresent={}",
                request.userId(),
                hasCoupon(request.couponCode())
        );

        return extractor.extract(
                promotionClient.quote(request),
                CheckoutErrorCode.DOWNSTREAM_PROMOTION_FAILED
        );
    }

    public PromotionQuoteClientResponse promotionQuoteFallback(
            PromotionQuoteClientRequest request,
            Throwable ex
    ) {
        boolean couponPresent = hasCoupon(request.couponCode());

        if (couponPresent) {
            log.warn(
                    "event=checkout.promotion_coupon_validation_unavailable userId={} couponPresent=true reason={}",
                    request.userId(),
                    ex.getMessage()
            );

            throw new BaseException(
                    CheckoutErrorCode.PROMOTION_COUPON_VALIDATION_UNAVAILABLE,
                    "Kupon doğrulama şu anda kullanılamıyor. Lütfen tekrar deneyin."
            );
        }

        log.warn(
                "event=checkout.promotion_degraded_to_no_discount userId={} reason={}",
                request.userId(),
                ex.getMessage()
        );

        return new PromotionQuoteClientResponse(
                BigDecimal.ZERO,
                BigDecimal.ZERO,
                BigDecimal.ZERO,
                nullSafe(request.shippingFee()),
                List.of()
        );
    }

    @CircuitBreaker(name = "promotionService", fallbackMethod = "reservePromotionUsageFallback")
    public PromotionUsageReservationClientResponse reservePromotionUsage(
            PromotionUsageReserveClientRequest request
    ) {
        log.info(
                "event=checkout.promotion_reserve_requested checkoutId={} userId={} itemCount={}",
                request.checkoutId(),
                request.userId(),
                request.items() == null ? 0 : request.items().size()
        );

        return extractor.extract(
                promotionClient.reserveUsage(idempotencyKey("promotion-reserve", request.checkoutId()), request),
                CheckoutErrorCode.DOWNSTREAM_PROMOTION_FAILED
        );
    }

    public PromotionUsageReservationClientResponse reservePromotionUsageFallback(
            PromotionUsageReserveClientRequest request,
            Throwable ex
    ) {
        log.warn(
                "event=checkout.downstream_unavailable service=promotion-service operation=reserveUsage checkoutId={} reason={}",
                request.checkoutId(),
                ex.getMessage()
        );

        throw new BaseException(
                CheckoutErrorCode.DOWNSTREAM_PROMOTION_FAILED,
                "Promotion usage reservation is temporarily unavailable"
        );
    }

    @CircuitBreaker(name = "promotionService", fallbackMethod = "redeemPromotionUsageFallback")
    public PromotionUsageReservationClientResponse redeemPromotionUsage(UUID reservationId, UUID orderId) {
        log.info("event=checkout.promotion_redeem_requested reservationId={} orderId={}", reservationId, orderId);

        return extractor.extract(
                promotionClient.redeemUsage(
                        reservationId,
                        new PromotionUsageRedeemClientRequest(orderId)
                ),
                CheckoutErrorCode.DOWNSTREAM_PROMOTION_FAILED
        );
    }

    public PromotionUsageReservationClientResponse redeemPromotionUsageFallback(
            UUID reservationId,
            UUID orderId,
            Throwable ex
    ) {
        log.error(
                "event=checkout.promotion_redeem_unavailable reservationId={} orderId={} reason={}",
                reservationId,
                orderId,
                ex.getMessage()
        );

        throw new BaseException(
                CheckoutErrorCode.DOWNSTREAM_PROMOTION_FAILED,
                "Promotion redeem is temporarily unavailable"
        );
    }

    @CircuitBreaker(name = "promotionService", fallbackMethod = "cancelPromotionUsageFallback")
    public PromotionUsageReservationClientResponse cancelPromotionUsage(UUID reservationId, String reason) {
        log.info("event=checkout.promotion_cancel_requested reservationId={} reason={}", reservationId, reason);

        return extractor.extract(
                promotionClient.cancelUsage(
                        reservationId,
                        new PromotionUsageCancelClientRequest(reason)
                ),
                CheckoutErrorCode.DOWNSTREAM_PROMOTION_FAILED
        );
    }

    public PromotionUsageReservationClientResponse cancelPromotionUsageFallback(
            UUID reservationId,
            String reason,
            Throwable ex
    ) {
        log.error(
                "event=checkout.promotion_cancel_unavailable reservationId={} cancelReason={} reason={}",
                reservationId,
                reason,
                ex.getMessage()
        );

        throw new BaseException(
                CheckoutErrorCode.DOWNSTREAM_PROMOTION_FAILED,
                "Promotion cancellation is temporarily unavailable"
        );
    }

    @CircuitBreaker(name = "orderService", fallbackMethod = "createOrderFallback")
    public OrderClientResponse createOrder(CreateOrderClientRequest request) {
        log.info(
                "event=checkout.order_create_requested checkoutId={} userId={} basketId={}",
                request.checkoutId(),
                request.userId(),
                request.basketId()
        );

        return extractor.extract(
                orderClient.createOrder(idempotencyKey("order-create", request.checkoutId()), request),
                CheckoutErrorCode.DOWNSTREAM_ORDER_FAILED
        );
    }

    public OrderClientResponse createOrderFallback(
            CreateOrderClientRequest request,
            Throwable ex
    ) {
        log.warn(
                "event=checkout.downstream_unavailable service=order-service operation=createOrder checkoutId={} reason={}",
                request.checkoutId(),
                ex.getMessage()
        );

        throw new BaseException(
                CheckoutErrorCode.DOWNSTREAM_ORDER_FAILED,
                "Order creation is temporarily unavailable"
        );
    }

    @CircuitBreaker(name = "orderService", fallbackMethod = "markOrderPaidFallback")
    public OrderClientResponse markOrderPaid(UUID orderId, MarkOrderPaidClientRequest request) {
        log.info("event=checkout.order_mark_paid_requested orderId={} paymentId={}", orderId, request.paymentId());

        return extractor.extract(
                orderClient.markPaid(orderId, request),
                CheckoutErrorCode.DOWNSTREAM_ORDER_FAILED
        );
    }

    public OrderClientResponse markOrderPaidFallback(
            UUID orderId,
            MarkOrderPaidClientRequest request,
            Throwable ex
    ) {
        log.error(
                "event=checkout.order_mark_paid_unavailable orderId={} paymentId={} reason={}",
                orderId,
                request.paymentId(),
                ex.getMessage()
        );

        throw new BaseException(
                CheckoutErrorCode.DOWNSTREAM_ORDER_FAILED,
                "Order payment confirmation is temporarily unavailable"
        );
    }

    @CircuitBreaker(name = "orderService", fallbackMethod = "markOrderPaymentFailedFallback")
    public OrderClientResponse markOrderPaymentFailed(
            UUID orderId,
            MarkOrderPaymentFailedClientRequest request
    ) {
        log.info(
                "event=checkout.order_mark_payment_failed_requested orderId={} paymentId={}",
                orderId,
                request.paymentId()
        );

        return extractor.extract(
                orderClient.markPaymentFailed(orderId, request),
                CheckoutErrorCode.DOWNSTREAM_ORDER_FAILED
        );
    }

    public OrderClientResponse markOrderPaymentFailedFallback(
            UUID orderId,
            MarkOrderPaymentFailedClientRequest request,
            Throwable ex
    ) {
        log.error(
                "event=checkout.order_mark_payment_failed_unavailable orderId={} paymentId={} reason={}",
                orderId,
                request.paymentId(),
                ex.getMessage()
        );

        throw new BaseException(
                CheckoutErrorCode.DOWNSTREAM_ORDER_FAILED,
                "Order payment failure update is temporarily unavailable"
        );
    }

    @CircuitBreaker(name = "paymentService", fallbackMethod = "initializePaymentFallback")
    public PaymentInitializeClientResponse initializePayment(
            PaymentInitializeClientRequest request
    ) {
        log.info(
                "event=checkout.payment_initialize_requested checkoutId={} orderId={} userId={} amount={} currency={} provider={}",
                request.checkoutId(),
                request.orderId(),
                request.userId(),
                request.amount(),
                request.currency(),
                request.provider()
        );

        return extractor.extract(
                paymentClient.initializePayment(idempotencyKey("payment-initialize", request.checkoutId()), request),
                CheckoutErrorCode.DOWNSTREAM_PAYMENT_FAILED
        );
    }

    public PaymentInitializeClientResponse initializePaymentFallback(
            PaymentInitializeClientRequest request,
            Throwable ex
    ) {
        log.warn(
                "event=checkout.downstream_unavailable service=payment-service operation=initializePayment checkoutId={} orderId={} reason={}",
                request.checkoutId(),
                request.orderId(),
                ex.getMessage()
        );

        throw new BaseException(
                CheckoutErrorCode.DOWNSTREAM_PAYMENT_FAILED,
                "Payment initialization is temporarily unavailable"
        );
    }

    @CircuitBreaker(name = "basket-service", fallbackMethod = "markBasketCheckedOutFallback")
    public void markBasketCheckedOut(UUID basketId, UUID checkoutId, UUID orderId) {
        log.info(
                "event=checkout.basket_mark_checked_out_requested basketId={} checkoutId={} orderId={}",
                basketId,
                checkoutId,
                orderId
        );

        extractor.extract(
                basketClient.markBasketCheckedOut(
                        basketId,
                        new MarkBasketCheckedOutClientRequest(orderId)
                ),
                CheckoutErrorCode.DOWNSTREAM_BASKET_FAILED
        );
    }

    public void markBasketCheckedOutFallback(
            UUID basketId,
            UUID checkoutId,
            UUID orderId,
            Throwable ex
    ) {
        log.error(
                "event=checkout.basket_mark_checked_out_unavailable basketId={} checkoutId={} orderId={} reason={}",
                basketId,
                checkoutId,
                orderId,
                ex.getMessage()
        );

        throw new BaseException(
                CheckoutErrorCode.DOWNSTREAM_BASKET_FAILED,
                "Basket checkout finalization is temporarily unavailable"
        );
    }

    @CircuitBreaker(name = "shipmentService", fallbackMethod = "createShipmentFallback")
    public ShipmentClientResponse createShipmentForOrder(UUID orderId) {
        log.info("event=checkout.shipment_create_requested orderId={}", orderId);

        return extractor.extract(
                shipmentClient.createShipmentForOrder(
                        idempotencyKey("shipment-create", orderId),
                        new CreateShipmentForOrderClientRequest(orderId)
                ),
                CheckoutErrorCode.DOWNSTREAM_SHIPMENT_FAILED
        );
    }

    public ShipmentClientResponse createShipmentFallback(UUID orderId, Throwable ex) {
        log.error(
                "event=checkout.shipment_create_unavailable orderId={} reason={}",
                orderId,
                ex.getMessage()
        );

        throw new BaseException(
                CheckoutErrorCode.DOWNSTREAM_SHIPMENT_FAILED,
                "Shipment creation is temporarily unavailable"
        );
    }

    private String idempotencyKey(String operation, UUID stableId) {
        return "checkout:" + operation + ":" + stableId;
    }

    private boolean hasCoupon(String couponCode) {
        return couponCode != null && !couponCode.isBlank();
    }

    private BigDecimal nullSafe(BigDecimal value) {
        return value == null ? BigDecimal.ZERO : value;
    }
}
