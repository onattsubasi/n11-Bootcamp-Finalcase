package com.onatsubasi.finalcase.checkout.infrastructure.mapper;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.onatsubasi.finalcase.checkout.application.dto.client.*;
import com.onatsubasi.finalcase.checkout.application.dto.event.CheckoutChangedEvent;
import com.onatsubasi.finalcase.checkout.application.dto.event.PaymentResultEventMessage;
import com.onatsubasi.finalcase.checkout.application.dto.request.CheckoutPaymentMethodRequest;
import com.onatsubasi.finalcase.checkout.application.dto.request.CheckoutSubmitRequest;
import com.onatsubasi.finalcase.checkout.application.dto.response.*;
import com.onatsubasi.finalcase.checkout.domain.entity.*;
import com.onatsubasi.finalcase.checkout.domain.enums.CheckoutAddressType;
import com.onatsubasi.finalcase.checkout.domain.exception.CheckoutErrorCode;
import com.onatsubasi.finalcase.common.core.exception.BaseException;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Component
public class CheckoutMapper {

    private final ObjectMapper objectMapper;

    public CheckoutMapper(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public CheckoutQuoteResponse toQuoteResponse(
            BasketSnapshotClientResponse basket,
            List<CatalogProductSnapshotClientResponse> products,
            PromotionQuoteClientResponse promotionQuote,
            BigDecimal shippingFee,
            BigDecimal taxAmount
    ) {
        List<CheckoutItemResponse> items = toCheckoutItems(
                basket,
                products,
                promotionQuote
        );

        BigDecimal itemDiscount = items.stream()
                .map(CheckoutItemResponse::lineDiscount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal promotionDiscount = safe(promotionQuote.totalDiscount());
        BigDecimal shippingDiscount = safe(promotionQuote.shippingDiscount());

        BigDecimal grandTotal = safe(basket.subtotalAmount())
                .subtract(itemDiscount)
                .subtract(promotionDiscount)
                .add(safe(shippingFee))
                .subtract(shippingDiscount)
                .add(safe(taxAmount))
                .max(BigDecimal.ZERO);

        return new CheckoutQuoteResponse(
                basket.basketId(),
                basket.userId(),
                new CheckoutMoneyBreakdownResponse(
                        safe(basket.subtotalAmount()),
                        itemDiscount,
                        promotionDiscount,
                        safe(shippingFee),
                        shippingDiscount,
                        safe(taxAmount),
                        grandTotal,
                        basket.currency()
                ),
                items,
                toCheckoutDiscounts(promotionQuote)
        );
    }

    public InventoryReserveClientRequest toInventoryReserveRequest(
            UUID checkoutId,
            BasketSnapshotClientResponse basket
    ) {
        return new InventoryReserveClientRequest(
                checkoutId,
                basket.basketId(),
                basket.userId(),
                basket.items()
                        .stream()
                        .map(item -> new InventoryReserveItemClientRequest(
                                item.productId(),
                                item.quantity()
                        ))
                        .toList()
        );
    }

    public PromotionQuoteClientRequest toPromotionQuoteRequest(
            BasketSnapshotClientResponse basket,
            List<CatalogProductSnapshotClientResponse> products,
            BigDecimal shippingFee,
            String couponCode
    ) {
        return new PromotionQuoteClientRequest(
                basket.userId(),
                safe(basket.subtotalAmount()),
                safe(shippingFee),
                couponCode,
                basket.items()
                        .stream()
                        .map(item -> {
                            CatalogProductSnapshotClientResponse product =
                                    findProduct(products, item.productId());

                            return new PromotionQuoteItemClientRequest(
                                    item.productId(),
                                    product.categoryId(),
                                    product.brandId(),
                                    item.unitPrice(),
                                    item.quantity()
                            );
                        })
                        .toList()
        );
    }

    public PromotionUsageReserveClientRequest toPromotionUsageReserveRequest(
            UUID orderId,
            UUID userId,
            List<CheckoutDiscountResponse> discounts
    ) {
        return new PromotionUsageReserveClientRequest(
                orderId,
                userId,
                discounts.stream()
                        .map(discount -> new PromotionUsageReserveItemClientRequest(
                                discount.promotionId(),
                                discount.couponId(),
                                discount.couponCode(),
                                discount.discountAmount(),
                                discount.shippingDiscountAmount(),
                                discount.description()
                        ))
                        .toList()
        );
    }

    public CreateOrderClientRequest toCreateOrderRequest(
            CheckoutSession checkoutSession,
            CheckoutSubmitRequest submitRequest,
            CheckoutQuoteResponse quote,
            BasketSnapshotClientResponse basket,
            List<CatalogProductSnapshotClientResponse> products,
            UserAddressSnapshotClientResponse shippingAddress,
            UserAddressSnapshotClientResponse billingAddress,
            UUID inventoryReservationId,
            UUID promotionUsageReservationId
    ) {
        return new CreateOrderClientRequest(
                checkoutSession.getId(),
                checkoutSession.getIdempotencyKey(),
                checkoutSession.getRequestHash(),
                checkoutSession.getUserId(),
                basket.basketId(),
                inventoryReservationId,
                promotionUsageReservationId,
                toOrderAddress(shippingAddress),
                toOrderAddress(billingAddress),
                quote.money().subtotalAmount(),
                quote.money().itemDiscountAmount(),
                quote.money().promotionDiscountAmount(),
                quote.money().shippingFee(),
                quote.money().shippingDiscountAmount(),
                quote.money().taxAmount(),
                quote.money().grandTotalAmount(),
                quote.money().currency(),
                toOrderItems(basket, products),
                toOrderDiscounts(quote.discounts())
        );
    }

    public PaymentInitializeClientRequest toPaymentInitializeRequest(
            CheckoutSession checkoutSession,
            CheckoutSubmitRequest submitRequest,
            OrderClientResponse order
    ) {
        CheckoutPaymentMethodRequest paymentMethod = submitRequest.paymentMethod();

        return new PaymentInitializeClientRequest(
                checkoutSession.getId(),
                order.id(),
                checkoutSession.getUserId(),
                order.grandTotalAmount(),
                order.currency(),
                paymentMethod.provider(),
                paymentMethod.methodType(),
                paymentMethod.paymentToken(),
                paymentMethod.shouldUseThreeDSecure()
        );
    }

    public CheckoutSubmitResponse toSubmitResponse(
            CheckoutSession session
    ) {
        return new CheckoutSubmitResponse(
                session.getId(),
                session.getStatus(),
                session.getOrderId(),
                session.getOrderNumber(),
                new CheckoutPaymentActionResponse(
                        session.getPaymentId(),
                        session.getPaymentSessionId(),
                        session.getPaymentRedirectUrl(),
                        null,
                        session.getStatus().name()
                ),
                toMoneyResponse(session)
        );
    }

    public CheckoutSessionResponse toSessionResponse(CheckoutSession session) {
        return new CheckoutSessionResponse(
                session.getId(),
                session.getUserId(),
                session.getBasketId(),
                session.getIdempotencyKey(),
                session.getStatus(),
                session.getInventoryReservationId(),
                session.getPromotionUsageReservationId(),
                session.getOrderId(),
                session.getOrderNumber(),
                session.getPaymentId(),
                session.getPaymentSessionId(),
                session.getPaymentRedirectUrl(),
                session.getShipmentId(),
                toMoneyResponse(session),
                session.getQuoteSnapshot(),
                session.getPaymentActionSnapshot(),
                session.getSagaSteps()
                        .stream()
                        .map(this::toSagaStepResponse)
                        .toList(),
                session.getCreatedAt(),
                session.getUpdatedAt(),
                session.getCompletedAt()
        );
    }

    public CheckoutSagaStepResponse toSagaStepResponse(CheckoutSagaStep step) {
        return new CheckoutSagaStepResponse(
                step.getId(),
                step.getStepName(),
                step.getStatus(),
                step.getErrorMessage(),
                step.getStartedAt(),
                step.getCompletedAt()
        );
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

    public void applyCheckoutSnapshot(
            CheckoutSession session,
            CheckoutQuoteResponse quote,
            UserAddressSnapshotClientResponse shippingAddress,
            UserAddressSnapshotClientResponse billingAddress
    ) {
        session.clearSnapshotLines();

        quote.items().forEach(item -> session.addItem(CheckoutItem.create(
                requiredUuid(item.productId(), "Product id is invalid"),
                item.sku(),
                item.productName(),
                item.slug(),
                item.mainImageUrl(),
                nullableUuid(item.brandId()),
                item.brandName(),
                nullableUuid(item.categoryId()),
                item.categoryName(),
                item.unitPrice(),
                item.quantity(),
                item.lineDiscount(),
                item.currency()
        )));

        session.addAddress(toCheckoutAddress(CheckoutAddressType.SHIPPING, shippingAddress));

        if (billingAddress != null) {
            session.addAddress(toCheckoutAddress(CheckoutAddressType.BILLING, billingAddress));
        }

        quote.discounts().forEach(discount -> session.addDiscount(CheckoutDiscount.create(
                discount.promotionId(),
                discount.couponId(),
                discount.couponCode(),
                discount.promotionName(),
                discount.promotionType(),
                safe(discount.discountAmount()).add(safe(discount.shippingDiscountAmount())),
                quote.money().currency()
        )));
    }

    public CreateShipmentClientRequest toCreateShipmentRequest(
            CheckoutSession session,
            OrderClientResponse order,
            UserAddressSnapshotClientResponse shippingAddress,
            BasketSnapshotClientResponse basket,
            List<CatalogProductSnapshotClientResponse> products
    ) {
        return new CreateShipmentClientRequest(
                order.id(),
                order.userId(),
                order.orderNumber(),
                toOrderAddress(shippingAddress),
                basket.items()
                        .stream()
                        .map(item -> {
                            CatalogProductSnapshotClientResponse product =
                                    findProduct(products, item.productId());

                            return new CreateShipmentItemClientRequest(
                                    item.productId(),
                                    item.sku(),
                                    product.name(),
                                    item.quantity()
                            );
                        })
                        .toList()
        );
    }

    public ShipmentCreatedOrderClientRequest toShipmentCreatedOrderRequest(
            ShipmentClientResponse shipment
    ) {
        return new ShipmentCreatedOrderClientRequest(
                shipment.shipmentId(),
                shipment.shipmentNumber(),
                shipment.carrier(),
                shipment.trackingNumber(),
                shipment.status()
        );
    }

    public MarkOrderPaidClientRequest toMarkOrderPaidRequest(
            PaymentResultEventMessage event
    ) {
        return new MarkOrderPaidClientRequest(
                event.paymentId(),
                event.provider(),
                event.paymentStatus(),
                event.providerTransactionId()
        );
    }

    public MarkOrderPaymentFailedClientRequest toMarkOrderPaymentFailedRequest(
            PaymentResultEventMessage event
    ) {
        return new MarkOrderPaymentFailedClientRequest(
                event.paymentId(),
                event.provider(),
                event.paymentStatus(),
                event.providerTransactionId()
        );
    }

    public CheckoutChangedEvent toChangedEvent(CheckoutSession session) {
        return CheckoutChangedEvent.from(session);
    }

    private CheckoutMoneyBreakdownResponse toMoneyResponse(CheckoutSession session) {
        return new CheckoutMoneyBreakdownResponse(
                session.getSubtotalAmount(),
                session.getItemDiscountAmount(),
                session.getPromotionDiscountAmount(),
                session.getShippingFee(),
                session.getShippingDiscountAmount(),
                session.getTaxAmount(),
                session.getGrandTotalAmount(),
                session.getCurrency()
        );
    }

    private List<CheckoutItemResponse> toCheckoutItems(
            BasketSnapshotClientResponse basket,
            List<CatalogProductSnapshotClientResponse> products,
            PromotionQuoteClientResponse promotionQuote
    ) {
        return basket.items()
                .stream()
                .map(item -> {
                    CatalogProductSnapshotClientResponse product =
                            findProduct(products, item.productId());

                    BigDecimal lineSubtotal = item.unitPrice()
                            .multiply(BigDecimal.valueOf(item.quantity()));

                    return new CheckoutItemResponse(
                            item.productId(),
                            item.sku(),
                            product.name(),
                            product.slug(),
                            product.mainImageUrl(),
                            product.brandId(),
                            product.brandName(),
                            product.categoryId(),
                            product.categoryName(),
                            item.unitPrice(),
                            item.quantity(),
                            lineSubtotal,
                            BigDecimal.ZERO,
                            lineSubtotal,
                            item.currency()
                    );
                })
                .toList();
    }

    private List<CheckoutDiscountResponse> toCheckoutDiscounts(
            PromotionQuoteClientResponse promotionQuote
    ) {
        if (promotionQuote == null || promotionQuote.appliedDiscounts() == null) {
            return List.of();
        }

        return promotionQuote.appliedDiscounts()
                .stream()
                .map(discount -> new CheckoutDiscountResponse(
                        discount.promotionId(),
                        null,
                        discount.couponId(),
                        discount.couponCode(),
                        discount.promotionType(),
                        safe(discount.discountAmount()),
                        safe(discount.shippingDiscountAmount()),
                        discount.description()
                ))
                .toList();
    }

    private List<CreateOrderItemClientRequest> toOrderItems(
            BasketSnapshotClientResponse basket,
            List<CatalogProductSnapshotClientResponse> products
    ) {
        return basket.items()
                .stream()
                .map(item -> {
                    CatalogProductSnapshotClientResponse product =
                            findProduct(products, item.productId());

                    BigDecimal lineSubtotal = item.unitPrice()
                            .multiply(BigDecimal.valueOf(item.quantity()));

                    return new CreateOrderItemClientRequest(
                            item.productId(),
                            item.sku(),
                            product.name(),
                            product.slug(),
                            product.mainImageUrl(),
                            product.brandId(),
                            product.brandName(),
                            product.categoryId(),
                            product.categoryName(),
                            item.unitPrice(),
                            item.quantity(),
                            lineSubtotal,
                            BigDecimal.ZERO,
                            lineSubtotal,
                            item.currency()
                    );
                })
                .toList();
    }

    private List<CreateOrderDiscountClientRequest> toOrderDiscounts(
            List<CheckoutDiscountResponse> discounts
    ) {
        if (discounts == null) {
            return List.of();
        }

        return discounts.stream()
                .map(discount -> new CreateOrderDiscountClientRequest(
                        discount.promotionId(),
                        discount.promotionName(),
                        discount.couponId(),
                        discount.couponCode(),
                        discount.discountAmount(),
                        discount.shippingDiscountAmount()
                ))
                .toList();
    }

    private CheckoutAddress toCheckoutAddress(
            CheckoutAddressType addressType,
            UserAddressSnapshotClientResponse address
    ) {
        return CheckoutAddress.create(
                addressType,
                address.addressId(),
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

    private OrderAddressSnapshotClientRequest toOrderAddress(
            UserAddressSnapshotClientResponse address
    ) {
        return new OrderAddressSnapshotClientRequest(
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

    private CatalogProductSnapshotClientResponse findProduct(
            List<CatalogProductSnapshotClientResponse> products,
            String productId
    ) {
        return products.stream()
                .filter(product -> product.productId().equals(productId))
                .findFirst()
                .orElseThrow(() -> new BaseException(
                        CheckoutErrorCode.DOWNSTREAM_CATALOG_FAILED,
                        "Product snapshot not found for productId: " + productId
                ));
    }

    private UUID requiredUuid(String value, String message) {
        UUID uuid = nullableUuid(value);
        if (uuid == null) {
            throw new BaseException(CheckoutErrorCode.INVALID_CHECKOUT_DATA, message);
        }
        return uuid;
    }

    private UUID nullableUuid(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        try {
            return UUID.fromString(value.trim());
        } catch (IllegalArgumentException ex) {
            return null;
        }
    }

    private BigDecimal safe(BigDecimal value) {
        return value == null ? BigDecimal.ZERO : value;
    }
}