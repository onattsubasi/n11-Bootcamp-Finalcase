package com.onatsubasi.finalcase.checkout.application.service;

import com.onatsubasi.finalcase.checkout.application.dto.client.*;
import com.onatsubasi.finalcase.checkout.application.dto.request.CheckoutQuoteRequest;
import com.onatsubasi.finalcase.checkout.application.dto.response.CheckoutQuoteResponse;
import com.onatsubasi.finalcase.checkout.domain.exception.CheckoutErrorCode;
import com.onatsubasi.finalcase.checkout.infrastructure.mapper.CheckoutMapper;
import com.onatsubasi.finalcase.common.core.exception.BaseException;
import com.onatsubasi.finalcase.common.security.UserContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class CheckoutQuoteService {

        private final CheckoutDownstreamGateway downstreamGateway;
        private final CheckoutMapper checkoutMapper;

        @Transactional(readOnly = true)
        public CheckoutQuoteResponse quote(
                        UserContext currentUser,
                        CheckoutQuoteRequest request) {
                UUID userId = currentUser.userId();
                log.info(
                                "event=checkout.quote.started userId={} basketId={} couponPresent={}",
                                userId,
                                request.basketId(),
                                request.couponCode() != null && !request.couponCode().isBlank());

                BasketSnapshotClientResponse basket = loadBasket(
                                request.basketId(),
                                userId);

                List<CatalogProductSnapshotClientResponse> products = loadProductSnapshots(basket);

                validateProductsSellable(basket, products);

                BigDecimal shippingFee = calculateShippingFee(basket);
                BigDecimal taxAmount = calculateTaxAmount(basket);

                PromotionQuoteClientResponse promotionQuote = downstreamGateway.quotePromotion(
                                checkoutMapper.toPromotionQuoteRequest(
                                                basket,
                                                products,
                                                shippingFee,
                                                request.couponCode()));

                CheckoutQuoteResponse response = checkoutMapper.toQuoteResponse(
                                basket,
                                products,
                                promotionQuote,
                                shippingFee,
                                taxAmount);

                log.info(
                                "event=checkout.quote.created userId={} basketId={} subtotal={} grandTotal={} currency={}",
                                userId,
                                request.basketId(),
                                response.money().subtotalAmount(),
                                response.money().grandTotalAmount(),
                                response.money().currency());

                return response;
        }

        public BasketSnapshotClientResponse loadBasket(UUID basketId, UUID userId) {
                BasketSnapshotClientResponse basket = downstreamGateway.getBasketSnapshot(basketId, userId);

                if (!basket.userId().equals(userId)) {
                        log.warn(
                                        "event=checkout.basket_access_denied requestedUserId={} basketUserId={} basketId={}",
                                        userId,
                                        basket.userId(),
                                        basketId);

                        throw new BaseException(CheckoutErrorCode.CHECKOUT_ACCESS_DENIED);
                }

                if (basket.items() == null || basket.items().isEmpty()) {
                        throw new BaseException(
                                        CheckoutErrorCode.CHECKOUT_BASKET_EMPTY,
                                        "Basket is empty");
                }

                return basket;
        }

        public List<CatalogProductSnapshotClientResponse> loadProductSnapshots(
                        BasketSnapshotClientResponse basket) {
                List<UUID> productIds = basket.items()
                                .stream()
                                .map(BasketItemClientResponse::productId)
                                .map(UUID::fromString)
                                .distinct()
                                .toList();

                return downstreamGateway.getProductSnapshots(
                                new CatalogProductSnapshotsClientRequest(productIds));
        }

        public void validateProductsSellable(
                        BasketSnapshotClientResponse basket,
                        List<CatalogProductSnapshotClientResponse> products) {
                for (BasketItemClientResponse item : basket.items()) {
                        CatalogProductSnapshotClientResponse product = products.stream()
                                        .filter(candidate -> candidate.productId().equals(item.productId()))
                                        .findFirst()
                                        .orElseThrow(() -> new BaseException(
                                                        CheckoutErrorCode.DOWNSTREAM_CATALOG_FAILED,
                                                        "Product snapshot not found: " + item.productId()));

                        if (!product.active()) {
                                log.warn(
                                                "event=checkout.product_not_sellable productId={} basketId={}",
                                                item.productId(),
                                                basket.basketId());

                                throw new BaseException(
                                                CheckoutErrorCode.CHECKOUT_PRODUCT_NOT_SELLABLE,
                                                "Product is not sellable: " + item.productId());
                        }

                        if (product.price().compareTo(item.unitPrice()) != 0) {
                                log.warn(
                                                "event=checkout.product_price_changed productId={} basketPrice={} catalogPrice={}",
                                                item.productId(),
                                                item.unitPrice(),
                                                product.price());

                                throw new BaseException(
                                                CheckoutErrorCode.INVALID_CHECKOUT_TOTALS,
                                                "Product price changed: " + item.productId());
                        }
                }
        }

        public BigDecimal calculateShippingFee(BasketSnapshotClientResponse basket) {
                BigDecimal subtotal = basket.subtotalAmount();

                if (subtotal.compareTo(BigDecimal.valueOf(1000)) >= 0) {
                        return BigDecimal.ZERO;
                }

                return BigDecimal.valueOf(49.90);
        }

        public BigDecimal calculateTaxAmount(BasketSnapshotClientResponse basket) {
                return BigDecimal.ZERO;
        }
}
