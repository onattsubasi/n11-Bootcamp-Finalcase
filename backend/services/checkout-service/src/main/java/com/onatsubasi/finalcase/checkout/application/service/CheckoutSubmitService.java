package com.onatsubasi.finalcase.checkout.application.service;

import com.onatsubasi.finalcase.checkout.application.dto.client.*;
import com.onatsubasi.finalcase.checkout.application.dto.request.CheckoutSubmitRequest;
import com.onatsubasi.finalcase.checkout.application.dto.response.CheckoutDiscountResponse;
import com.onatsubasi.finalcase.checkout.application.dto.response.CheckoutQuoteResponse;
import com.onatsubasi.finalcase.checkout.application.dto.response.CheckoutSubmitResponse;
import com.onatsubasi.finalcase.checkout.application.port.CheckoutEventPublisher;
import com.onatsubasi.finalcase.checkout.domain.entity.CheckoutIdempotencyRecord;
import com.onatsubasi.finalcase.checkout.domain.entity.CheckoutSession;
import com.onatsubasi.finalcase.checkout.domain.enums.CheckoutSagaStepName;
import com.onatsubasi.finalcase.checkout.domain.repository.CheckoutSessionRepository;
import com.onatsubasi.finalcase.checkout.infrastructure.mapper.CheckoutMapper;
import com.onatsubasi.finalcase.common.security.UserContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class CheckoutSubmitService {

        private final CheckoutSessionRepository checkoutSessionRepository;
        private final CheckoutIdempotencyService idempotencyService;
        private final CheckoutRequestHashService requestHashService;
        private final CheckoutQuoteService quoteService;
        private final CheckoutDownstreamGateway downstreamGateway;
        private final CheckoutMapper checkoutMapper;
        private final CheckoutEventPublisher eventPublisher;

        @Transactional
        public CheckoutSubmitResponse submit(
                        UserContext currentUser,
                        CheckoutSubmitRequest request,
                        String idempotencyKey) {
                UUID userId = currentUser.userId();
                log.info(
                                "event=checkout.started userId={} basketId={} idempotencyKeyPresent={} couponPresent={}",
                                userId,
                                request.basketId(),
                                idempotencyKey != null && !idempotencyKey.isBlank(),
                                request.couponCode() != null && !request.couponCode().isBlank());

                String requestHash = requestHashService.hash(request);

                CheckoutIdempotencyRecord idempotencyRecord = idempotencyService.getOrCreateForUpdate(
                                userId,
                                idempotencyKey,
                                requestHash);

                Optional<CheckoutSubmitResponse> storedResponse = idempotencyService
                                .getStoredSubmitResponse(idempotencyRecord);

                if (storedResponse.isPresent()) {
                        log.info(
                                        "event=checkout.idempotent_replay userId={} basketId={} checkoutId={}",
                                        userId,
                                        request.basketId(),
                                        storedResponse.get().checkoutSessionId());

                        return storedResponse.get();
                }

                CheckoutSagaContext context = new CheckoutSagaContext();
                CheckoutSession session = null;

                try {
                        BasketSnapshotClientResponse basket = quoteService.loadBasket(
                                        request.basketId(),
                                        userId);

                        List<CatalogProductSnapshotClientResponse> products = quoteService.loadProductSnapshots(basket);

                        quoteService.validateProductsSellable(basket, products);

                        UserAddressSnapshotClientResponse shippingAddress = downstreamGateway.getAddressSnapshot(
                                        userId,
                                        request.shippingAddressId());

                        UserAddressSnapshotClientResponse billingAddress = request.billingAddressId() == null
                                        ? shippingAddress
                                        : downstreamGateway.getAddressSnapshot(
                                                        userId,
                                                        request.billingAddressId());

                        BigDecimal shippingFee = quoteService.calculateShippingFee(basket);
                        BigDecimal taxAmount = quoteService.calculateTaxAmount(basket);

                        PromotionQuoteClientResponse promotionQuote = downstreamGateway.quotePromotion(
                                        checkoutMapper.toPromotionQuoteRequest(
                                                        basket,
                                                        products,
                                                        shippingFee,
                                                        request.couponCode()));

                        CheckoutQuoteResponse quote = checkoutMapper.toQuoteResponse(
                                        basket,
                                        products,
                                        promotionQuote,
                                        shippingFee,
                                        taxAmount);

                        session = new CheckoutSession(
                                        userId,
                                        basket.basketId(),
                                        idempotencyKey,
                                        requestHash,
                                        quote.money().subtotalAmount(),
                                        quote.money().promotionDiscountAmount(),
                                        quote.money().shippingFee(),
                                        quote.money().shippingDiscountAmount(),
                                        quote.money().taxAmount(),
                                        quote.money().grandTotalAmount(),
                                        quote.money().currency(),
                                        checkoutMapper.toMap(quote));

                        checkoutMapper.applyCheckoutSnapshot(
                                        session,
                                        quote,
                                        shippingAddress,
                                        billingAddress);

                        session.completeStep(CheckoutSagaStepName.BASKET_SNAPSHOT_LOADED);
                        session.completeStep(CheckoutSagaStepName.CATALOG_SNAPSHOTS_LOADED);
                        session.completeStep(CheckoutSagaStepName.ADDRESS_SNAPSHOT_LOADED);
                        session.completeStep(CheckoutSagaStepName.PROMOTION_QUOTED);

                        session = checkoutSessionRepository.save(session);
                        idempotencyRecord.attachCheckout(session.getId());

                        eventPublisher.publishCheckoutSubmitted(session);

                        log.info(
                                        "event=checkout.session_created checkoutId={} userId={} basketId={} grandTotal={}",
                                        session.getId(),
                                        session.getUserId(),
                                        session.getBasketId(),
                                        session.getGrandTotalAmount());

                        InventoryReservationClientResponse inventoryReservation = downstreamGateway.reserveStock(
                                        checkoutMapper.toInventoryReserveRequest(
                                                        session.getId(),
                                                        basket));

                        context.inventoryReservationId = inventoryReservation.reservationId();

                        session.attachInventoryReservation(inventoryReservation.reservationId());
                        session.completeStep(CheckoutSagaStepName.INVENTORY_RESERVED);
                        checkoutSessionRepository.save(session);

                        log.info(
                                        "event=checkout.inventory_reserved checkoutId={} inventoryReservationId={}",
                                        session.getId(),
                                        inventoryReservation.reservationId());

                        OrderClientResponse order = downstreamGateway.createOrder(
                                        checkoutMapper.toCreateOrderRequest(
                                                        session,
                                                        request,
                                                        quote,
                                                        basket,
                                                        products,
                                                        shippingAddress,
                                                        billingAddress,
                                                        inventoryReservation.reservationId(),
                                                        null));

                        context.orderId = order.id();

                        session.attachOrder(order.id(), order.orderNumber());
                        session.completeStep(CheckoutSagaStepName.ORDER_CREATED);
                        checkoutSessionRepository.save(session);

                        log.info(
                                        "event=checkout.order_created checkoutId={} orderId={} orderNumber={}",
                                        session.getId(),
                                        order.id(),
                                        order.orderNumber());

                        PromotionUsageReservationClientResponse promotionUsageReservation = reservePromotionUsageIfNeeded(
                                        order.id(),
                                        userId,
                                        quote.discounts());

                        if (promotionUsageReservation != null) {
                                context.promotionUsageReserved = true;

                                session.attachPromotionUsageReservation(
                                                promotionUsageReservation.id());

                                log.info(
                                                "event=checkout.promotion_reserved checkoutId={} orderId={} promotionUsageReservationId={}",
                                                session.getId(),
                                                order.id(),
                                                promotionUsageReservation.id());
                        }

                        session.completeStep(CheckoutSagaStepName.PROMOTION_RESERVED);
                        checkoutSessionRepository.save(session);

                        PaymentInitializeClientResponse payment = downstreamGateway.initializePayment(
                                        checkoutMapper.toPaymentInitializeRequest(
                                                        session,
                                                        request,
                                                        order));

                        context.paymentInitialized = true;

                        session.attachPaymentAction(
                                        payment.paymentId(),
                                        payment.paymentSessionId(),
                                        payment.redirectUrl(),
                                        checkoutMapper.toMap(payment));

                        session.completeStep(CheckoutSagaStepName.PAYMENT_INITIALIZED);

                        session = checkoutSessionRepository.save(session);

                        eventPublisher.publishCheckoutPaymentPending(session);

                        log.info(
                                        "event=checkout.payment_initialized checkoutId={} orderId={} paymentId={} status={}",
                                        session.getId(),
                                        session.getOrderId(),
                                        payment.paymentId(),
                                        payment.status());

                        CheckoutSubmitResponse response = checkoutMapper.toSubmitResponse(session);

                        idempotencyService.storeSubmitResponse(
                                        idempotencyRecord,
                                        session.getId(),
                                        response);

                        return response;
                } catch (Exception ex) {
                        log.error(
                                        "event=checkout.submit_failed checkoutId={} userId={} basketId={}",
                                        session == null ? null : session.getId(),
                                        userId,
                                        request.basketId(),
                                        ex);

                        if (session != null) {
                                session.markFailed();
                                session.failStep(
                                                CheckoutSagaStepName.CHECKOUT_FAILED,
                                                ex.getMessage());

                                checkoutSessionRepository.save(session);
                                eventPublisher.publishCheckoutFailed(session);

                                compensateSubmitFailure(session, context);
                        }

                        throw ex;
                }
        }

        private PromotionUsageReservationClientResponse reservePromotionUsageIfNeeded(
                        UUID orderId,
                        UUID userId,
                        List<CheckoutDiscountResponse> discounts) {
                if (discounts == null || discounts.isEmpty()) {
                        return null;
                }

                boolean hasRealDiscount = discounts.stream()
                                .anyMatch(discount -> discount.discountAmount().compareTo(BigDecimal.ZERO) > 0
                                                || discount.shippingDiscountAmount().compareTo(BigDecimal.ZERO) > 0);

                if (!hasRealDiscount) {
                        return null;
                }

                return downstreamGateway.reservePromotionUsage(
                                checkoutMapper.toPromotionUsageReserveRequest(
                                                orderId,
                                                userId,
                                                discounts));
        }

        private void compensateSubmitFailure(
                        CheckoutSession session,
                        CheckoutSagaContext context) {
                try {
                        log.warn(
                                        "event=checkout.submit_compensation_started checkoutId={} orderId={} inventoryReservationId={} promotionUsageReservationId={}",
                                        session.getId(),
                                        session.getOrderId(),
                                        session.getInventoryReservationId(),
                                        session.getPromotionUsageReservationId());

                        if (context.paymentInitialized) {
                                return;
                        }

                        if (context.promotionUsageReserved && session.getOrderId() != null) {
                                downstreamGateway.cancelPromotionUsage(session.getOrderId());
                        }

                        if (context.inventoryReservationId != null) {
                                downstreamGateway.releaseReservation(context.inventoryReservationId);
                        }

                        if (context.orderId != null) {
                                downstreamGateway.markOrderPaymentFailed(
                                                context.orderId,
                                                new MarkOrderPaymentFailedClientRequest(
                                                                null,
                                                                null,
                                                                "CHECKOUT_SUBMIT_FAILED",
                                                                null));
                        }

                        session.markCompensated();
                        session.completeStep(CheckoutSagaStepName.CHECKOUT_COMPENSATED);
                        checkoutSessionRepository.save(session);

                        eventPublisher.publishCheckoutCompensated(session);

                        log.info(
                                        "event=checkout.submit_compensation_completed checkoutId={} orderId={}",
                                        session.getId(),
                                        session.getOrderId());
                } catch (Exception compensationEx) {
                        session.markCompensationFailed();
                        session.failStep(
                                        CheckoutSagaStepName.CHECKOUT_COMPENSATED,
                                        compensationEx.getMessage());
                        checkoutSessionRepository.save(session);

                        eventPublisher.publishCheckoutCompensationFailed(session);

                        log.error(
                                        "event=checkout.submit_compensation_failed checkoutId={} orderId={}",
                                        session.getId(),
                                        session.getOrderId(),
                                        compensationEx);
                }
        }

        private static class CheckoutSagaContext {
                private UUID inventoryReservationId;
                private UUID orderId;
                private boolean promotionUsageReserved;
                private boolean paymentInitialized;
        }
}