package com.onatsubasi.finalcase.basket.application.service;

import com.onatsubasi.finalcase.basket.application.dto.internal.MarkBasketCheckedOutRequest;
import com.onatsubasi.finalcase.basket.application.dto.internal.MarkBasketCheckedOutResponse;
import com.onatsubasi.finalcase.basket.application.dto.request.AddBasketItemRequest;
import com.onatsubasi.finalcase.basket.application.dto.request.UpdateBasketItemQuantityRequest;
import com.onatsubasi.finalcase.basket.application.dto.request.UpdateCouponIntentRequest;
import com.onatsubasi.finalcase.basket.application.dto.response.BasketResponse;
import com.onatsubasi.finalcase.basket.application.port.BasketEventPublisher;
import com.onatsubasi.finalcase.basket.domain.enums.BasketStatus;
import com.onatsubasi.finalcase.basket.domain.exception.BasketErrorCode;
import com.onatsubasi.finalcase.basket.domain.entity.Basket;
import com.onatsubasi.finalcase.basket.domain.repository.BasketRepository;
import com.onatsubasi.finalcase.basket.infrastructure.mapper.BasketMapper;
import com.onatsubasi.finalcase.common.core.exception.BaseException;
import com.onatsubasi.finalcase.common.security.UserContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class BasketService {

    private final BasketRepository basketRepository;
    private final BasketMapper basketMapper;
    private final BasketEventPublisher eventPublisher;

    @Transactional
    public BasketResponse getBasket(UserContext userContext) {
        UUID userId = requireUserId(userContext);
        Basket basket = getOrCreateActiveBasketForWrite(userId);

        return basketMapper.toResponse(basket);
    }

    @Transactional(readOnly = true)
    public BasketResponse getBasket(UUID userId) {
        Basket basket = basketRepository.findByUserIdAndStatus(userId, BasketStatus.ACTIVE)
                .orElseThrow(() -> new BaseException(BasketErrorCode.BASKET_NOT_FOUND));

        if (basket.isEmpty()) {
            throw new BaseException(BasketErrorCode.BASKET_EMPTY);
        }

        return basketMapper.toResponse(basket);
    }

    @Transactional
    public BasketResponse addItem(UserContext userContext, AddBasketItemRequest request) {
        try {
            UUID userId = requireUserId(userContext);

            MDC.put("eventName", "basket.item.add.started");
            log.info(
                    "Basket add item started, userId={}, productId={}, quantity={}",
                    userId,
                    request.productId(),
                    request.quantity()
            );

            Basket basket = getOrCreateActiveBasketForWrite(userId);

            basket.addItem(request.productId(), request.quantity());

            Basket savedBasket = basketRepository.save(basket);
            eventPublisher.publishItemAdded(savedBasket, request.productId());

            logEvent(
                    "basket.item.added",
                    "Basket item added, basketId={}, userId={}, productId={}, quantity={}",
                    savedBasket.getId(),
                    userId,
                    request.productId(),
                    request.quantity()
            );

            return basketMapper.toResponse(savedBasket);
        } catch (BaseException ex) {
            logBusinessFailure("basket.item.add.failed", ex);
            throw ex;
        } finally {
            clearMdc();
        }
    }

    @Transactional
    public BasketResponse updateItemQuantity(
            UserContext userContext,
            UUID productId,
            UpdateBasketItemQuantityRequest request
    ) {
        try {
            UUID userId = requireUserId(userContext);

            MDC.put("eventName", "basket.item.quantity_update.started");
            log.info(
                    "Basket item quantity update started, userId={}, productId={}, quantity={}",
                    userId,
                    productId,
                    request.quantity()
            );

            Basket basket = findActiveBasketForUpdate(userId);

            basket.updateItemQuantity(productId, request.quantity());

            Basket savedBasket = basketRepository.save(basket);
            eventPublisher.publishItemQuantityUpdated(savedBasket, productId);

            logEvent(
                    "basket.item.quantity_updated",
                    "Basket item quantity updated, basketId={}, userId={}, productId={}, quantity={}",
                    savedBasket.getId(),
                    userId,
                    productId,
                    request.quantity()
            );

            return basketMapper.toResponse(savedBasket);
        } catch (BaseException ex) {
            logBusinessFailure("basket.item.quantity_update.failed", ex);
            throw ex;
        } finally {
            clearMdc();
        }
    }

    @Transactional
    public void removeItem(UserContext userContext, UUID productId) {
        try {
            UUID userId = requireUserId(userContext);

            MDC.put("eventName", "basket.item.remove.started");
            log.info("Basket item remove started, userId={}, productId={}", userId, productId);

            basketRepository.findByUserIdAndStatusForUpdate(userId, BasketStatus.ACTIVE)
                    .ifPresent(basket -> removeItemIfPresent(basket, productId, userId));
        } catch (BaseException ex) {
            logBusinessFailure("basket.item.remove.failed", ex);
            throw ex;
        } finally {
            clearMdc();
        }
    }

    @Transactional
    public void clearBasket(UserContext userContext) {
        UUID userId = requireUserId(userContext);
        clearActiveBasket(userId, "basket.clear.requested");
    }

    @Transactional
    public void clearBasket(UUID userId) {
        clearActiveBasket(userId, "basket.internal.clear.requested");
    }

    @Transactional
    public BasketResponse updateCouponIntent(
            UserContext userContext,
            UpdateCouponIntentRequest request
    ) {
        try {
            UUID userId = requireUserId(userContext);

            MDC.put("eventName", "basket.coupon_intent.update.started");
            log.info("Basket coupon intent update started, userId={}", userId);

            Basket basket = getOrCreateActiveBasketForWrite(userId);
            basket.setCouponCodeIntent(request.couponCodeIntent());

            Basket savedBasket = basketRepository.save(basket);
            eventPublisher.publishCouponIntentUpdated(savedBasket);

            logEvent(
                    "basket.coupon_intent.updated",
                    "Basket coupon intent updated, basketId={}, userId={}",
                    savedBasket.getId(),
                    userId
            );

            return basketMapper.toResponse(savedBasket);
        } catch (BaseException ex) {
            logBusinessFailure("basket.coupon_intent.update.failed", ex);
            throw ex;
        } finally {
            clearMdc();
        }
    }

    @Transactional
    public void clearCouponIntent(UserContext userContext) {
        try {
            UUID userId = requireUserId(userContext);

            MDC.put("eventName", "basket.coupon_intent.clear.started");
            log.info("Basket coupon intent clear started, userId={}", userId);

            basketRepository.findByUserIdAndStatusForUpdate(userId, BasketStatus.ACTIVE)
                    .ifPresent(basket -> {
                        basket.clearCouponCodeIntent();

                        Basket savedBasket = basketRepository.save(basket);
                        eventPublisher.publishCouponIntentCleared(savedBasket);

                        logEvent(
                                "basket.coupon_intent.cleared",
                                "Basket coupon intent cleared, basketId={}, userId={}",
                                savedBasket.getId(),
                                userId
                        );
                    });
        } catch (BaseException ex) {
            logBusinessFailure("basket.coupon_intent.clear.failed", ex);
            throw ex;
        } finally {
            clearMdc();
        }
    }

    @Transactional
    public MarkBasketCheckedOutResponse markBasketCheckedOut(
            UUID basketId,
            MarkBasketCheckedOutRequest request
    ) {
        try {
            MDC.put("eventName", "basket.checkout.mark.started");
            log.info("Basket checkout marking started, basketId={}, orderId={}", basketId, request.orderId());

            Basket basket = basketRepository.findByIdForUpdate(basketId)
                    .orElseThrow(() -> new BaseException(BasketErrorCode.BASKET_NOT_FOUND));

            basket.markCheckedOut(request.orderId());

            Basket savedBasket = basketRepository.save(basket);
            eventPublisher.publishBasketCheckedOut(savedBasket);

            logEvent(
                    "basket.checked_out",
                    "Basket marked checked out, basketId={}, userId={}, orderId={}",
                    savedBasket.getId(),
                    savedBasket.getUserId(),
                    request.orderId()
            );

            return new MarkBasketCheckedOutResponse(
                    savedBasket.getId(),
                    savedBasket.getOrderId(),
                    savedBasket.getStatus(),
                    savedBasket.getCheckedOutAt()
            );
        } catch (BaseException ex) {
            logBusinessFailure("basket.checkout.mark.failed", ex);
            throw ex;
        } finally {
            clearMdc();
        }
    }

    @Transactional
    public int abandonOldActiveBaskets(Instant cutoff, int batchSize) {
        List<Basket> oldActiveBaskets = basketRepository.findOldActiveBasketsForUpdate(cutoff, batchSize);

        oldActiveBaskets.forEach(Basket::markAbandoned);

        if (!oldActiveBaskets.isEmpty()) {
            logEvent(
                    "basket.abandoned.cleanup",
                    "Old active baskets marked abandoned, count={}, cutoff={}",
                    oldActiveBaskets.size(),
                    cutoff
            );
        }

        return oldActiveBaskets.size();
    }

    private void removeItemIfPresent(Basket basket, UUID productId, UUID userId) {
        boolean itemExists = basket.getItems()
                .stream()
                .anyMatch(item -> item.getProductId().equals(productId));

        if (!itemExists) {
            return;
        }

        basket.removeItem(productId);

        Basket savedBasket = basketRepository.save(basket);
        eventPublisher.publishItemRemoved(savedBasket, productId);

        logEvent(
                "basket.item.removed",
                "Basket item removed, basketId={}, userId={}, productId={}",
                savedBasket.getId(),
                userId,
                productId
        );
    }

    private void clearActiveBasket(UUID userId, String startEventName) {
        try {
            MDC.put("eventName", startEventName);
            log.info("Basket clear started, userId={}", userId);

            basketRepository.findByUserIdAndStatusForUpdate(userId, BasketStatus.ACTIVE)
                    .ifPresent(basket -> {
                        boolean changed = !basket.isEmpty() || basket.getCouponCodeIntent() != null;

                        basket.clear();
                        Basket savedBasket = basketRepository.save(basket);

                        if (changed) {
                            eventPublisher.publishBasketCleared(savedBasket);
                        }

                        logEvent(
                                "basket.cleared",
                                "Basket cleared, basketId={}, userId={}",
                                savedBasket.getId(),
                                userId
                        );
                    });
        } catch (BaseException ex) {
            logBusinessFailure("basket.clear.failed", ex);
            throw ex;
        } finally {
            clearMdc();
        }
    }

    private Basket getOrCreateActiveBasketForWrite(UUID userId) {
        return basketRepository.findByUserIdAndStatusForUpdate(userId, BasketStatus.ACTIVE)
                .orElseGet(() -> createActiveBasketSafely(userId));
    }

    private Basket createActiveBasketSafely(UUID userId) {
        try {
            Basket createdBasket = basketRepository.saveAndFlush(Basket.empty(userId));
            eventPublisher.publishBasketCreated(createdBasket);

            logEvent(
                    "basket.created",
                    "Active basket created, basketId={}, userId={}",
                    createdBasket.getId(),
                    userId
            );

            return createdBasket;
        } catch (DataIntegrityViolationException ex) {
            return basketRepository.findByUserIdAndStatusForUpdate(userId, BasketStatus.ACTIVE)
                    .orElseThrow(() -> new BaseException(BasketErrorCode.BASKET_STORAGE_ERROR));
        }
    }

    private Basket findActiveBasketForUpdate(UUID userId) {
        return basketRepository.findByUserIdAndStatusForUpdate(userId, BasketStatus.ACTIVE)
                .orElseThrow(() -> new BaseException(BasketErrorCode.BASKET_NOT_FOUND));
    }

    private UUID requireUserId(UserContext userContext) {
        if (userContext == null || !userContext.isAuthenticated()) {
            throw new BaseException(BasketErrorCode.UNAUTHENTICATED_BASKET_ACCESS);
        }

        return userContext.userId();
    }

    private void logEvent(String eventName, String message, Object... arguments) {
        String previousEventName = MDC.get("eventName");

        try {
            MDC.put("eventName", eventName);
            log.info(message, arguments);
        } finally {
            if (previousEventName == null) {
                MDC.remove("eventName");
            } else {
                MDC.put("eventName", previousEventName);
            }
        }
    }

    private void logBusinessFailure(String eventName, BaseException ex) {
        MDC.put("eventName", eventName);
        MDC.put("errorCode", ex.getErrorCode().code());
        log.warn("Basket operation failed, errorCode={}", ex.getErrorCode().code());
    }

    private void clearMdc() {
        MDC.remove("eventName");
        MDC.remove("errorCode");
    }
}