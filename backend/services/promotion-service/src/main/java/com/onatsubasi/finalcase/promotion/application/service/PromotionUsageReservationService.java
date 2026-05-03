package com.onatsubasi.finalcase.promotion.application.service;

import com.onatsubasi.finalcase.common.core.exception.BaseException;
import com.onatsubasi.finalcase.promotion.application.dto.internal.*;
import com.onatsubasi.finalcase.promotion.application.dto.response.AppliedDiscountResponse;
import com.onatsubasi.finalcase.promotion.application.dto.response.PromotionQuoteResponse;
import com.onatsubasi.finalcase.promotion.application.dto.response.PromotionUsageReservationResponse;
import com.onatsubasi.finalcase.promotion.application.port.PromotionEventPublisher;
import com.onatsubasi.finalcase.promotion.domain.enums.PromotionUsageCancelReason;
import com.onatsubasi.finalcase.promotion.domain.enums.PromotionUsageReservationStatus;
import com.onatsubasi.finalcase.promotion.domain.exception.PromotionErrorCode;
import com.onatsubasi.finalcase.promotion.domain.entity.*;
import com.onatsubasi.finalcase.promotion.domain.repository.CouponAssignmentRepository;
import com.onatsubasi.finalcase.promotion.domain.repository.CouponRepository;
import com.onatsubasi.finalcase.promotion.domain.repository.PromotionRepository;
import com.onatsubasi.finalcase.promotion.domain.repository.PromotionUsageReservationRepository;
import com.onatsubasi.finalcase.promotion.infrastructure.config.PromotionUsageReservationProperties;
import com.onatsubasi.finalcase.promotion.infrastructure.mapper.PromotionMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class PromotionUsageReservationService {

    private final PromotionUsageReservationProperties reservationProperties;
    private final PromotionQuoteService quoteService;
    private final PromotionRepository promotionRepository;
    private final CouponRepository couponRepository;
    private final CouponAssignmentRepository assignmentRepository;
    private final PromotionUsageReservationRepository reservationRepository;
    private final PromotionMapper promotionMapper;
    private final PromotionEventPublisher eventPublisher;

    @Transactional
    public PromotionUsageReservationResponse reserve(
            String idempotencyKey,
            ReservePromotionUsageRequest request
    ) {
        validateReserveRequest(request);

        try {
            MDC.put("eventName", "promotion.usage.reserve.started");
            log.info("Promotion usage reservation started, checkoutId={}, userId={}",
                    request.checkoutId(),
                    request.userId());

            String normalizedIdempotencyKey = requireIdempotencyKey(idempotencyKey);
            String requestHash = requestHash(request);

            PromotionUsageReservation existing = reservationRepository
                    .findByIdempotencyKey(normalizedIdempotencyKey)
                    .orElse(null);

            if (existing != null) {
                existing.assertSameRequestHash(requestHash);
                return promotionMapper.toResponse(existing);
            }

            PromotionQuoteResponse quote = quoteService.quote(toQuoteRequest(request));

            List<AppliedDiscountResponse> selectedDiscounts = selectDiscountsForReservation(
                    quote.selectedDiscounts(),
                    request.selectedPromotionIds()
            );

            if (selectedDiscounts.isEmpty()) {
                throw new BaseException(
                        PromotionErrorCode.PROMOTION_QUOTE_FAILED,
                        "No eligible discount found to reserve"
                );
            }

            PromotionUsageReservation reservation = PromotionUsageReservation.create(
                    normalizedIdempotencyKey,
                    requestHash,
                    request.checkoutId(),
                    request.userId(),
                    Instant.now().plusSeconds(reservationProperties.getDefaultTimeoutMinutes() * 60)
            );

            Map<UUID, Promotion> lockedPromotions = lockPromotions(selectedDiscounts);

            for (AppliedDiscountResponse discount : selectedDiscounts) {
                Promotion promotion = lockedPromotions.get(discount.promotionId());

                if (promotion == null) {
                    throw new BaseException(PromotionErrorCode.PROMOTION_NOT_FOUND);
                }

                promotion.validateApplicableAt(Instant.now());
                enforcePerUserPromotionLimit(promotion, request.userId());
                promotion.reserveUsage();
                promotionRepository.save(promotion);

                Coupon coupon = null;
                CouponAssignment assignment = null;

                if (discount.couponId() != null) {
                    coupon = couponRepository.findByIdForUpdate(discount.couponId())
                            .orElseThrow(() -> new BaseException(PromotionErrorCode.COUPON_NOT_FOUND));

                    coupon.validateApplicableAt(Instant.now());
                    enforcePerUserCouponLimit(coupon, request.userId());
                    coupon.reserveUsage();
                    couponRepository.save(coupon);

                    assignment = assignmentRepository
                            .findByCouponIdAndUserIdForUpdate(coupon.getId(), request.userId())
                            .orElse(null);

                    if (assignment != null) {
                        assignment.reserve();
                        assignmentRepository.save(assignment);
                    }
                }

                reservation.addItem(PromotionUsageReservationItem.create(
                        promotion.getId(),
                        coupon == null ? null : coupon.getId(),
                        assignment == null ? null : assignment.getId(),
                        coupon == null ? null : coupon.getCode(),
                        discount.discountAmount(),
                        discount.shippingDiscountAmount(),
                        discount.description()
                ));
            }

            reservation.assertNotEmpty();

            PromotionUsageReservation saved = reservationRepository.save(reservation);
            eventPublisher.publishUsageReserved(saved);

            MDC.put("eventName", "promotion.usage.reserved");
            log.info("Promotion usage reserved, reservationId={}, checkoutId={}, userId={}",
                    saved.getId(),
                    saved.getCheckoutId(),
                    saved.getUserId());

            return promotionMapper.toResponse(saved);
        } catch (BaseException ex) {
            logBusinessFailure("promotion.usage.reserve.failed", ex);
            throw ex;
        } finally {
            clearMdc();
        }
    }

    @Transactional
    public PromotionUsageReservationResponse redeem(
            UUID reservationId,
            RedeemPromotionUsageRequest request
    ) {
        try {
            MDC.put("eventName", "promotion.usage.redeem.started");
            log.info("Promotion usage redeem started, reservationId={}, orderId={}",
                    reservationId,
                    request.orderId());

            PromotionUsageReservation reservation = reservationRepository.findByIdForUpdate(reservationId)
                    .orElseThrow(() -> new BaseException(PromotionErrorCode.PROMOTION_USAGE_RESERVATION_NOT_FOUND));

            if (reservation.getStatus() == PromotionUsageReservationStatus.REDEEMED
                    && request.orderId().equals(reservation.getOrderId())) {
                return promotionMapper.toResponse(reservation);
            }

            boolean changed = reservation.redeem(request.orderId());

            if (changed) {
                redeemCounters(reservation);
                PromotionUsageReservation saved = reservationRepository.save(reservation);
                eventPublisher.publishUsageRedeemed(saved);

                MDC.put("eventName", "promotion.usage.redeemed");
                log.info("Promotion usage redeemed, reservationId={}, orderId={}",
                        saved.getId(),
                        saved.getOrderId());

                return promotionMapper.toResponse(saved);
            }

            return promotionMapper.toResponse(reservation);
        } catch (BaseException ex) {
            logBusinessFailure("promotion.usage.redeem.failed", ex);
            throw ex;
        } finally {
            clearMdc();
        }
    }

    @Transactional
    public PromotionUsageReservationResponse cancel(
            UUID reservationId,
            CancelPromotionUsageRequest request
    ) {
        try {
            MDC.put("eventName", "promotion.usage.cancel.started");
            log.info("Promotion usage cancel started, reservationId={}, reason={}",
                    reservationId,
                    request.reason());

            PromotionUsageReservation reservation = reservationRepository.findByIdForUpdate(reservationId)
                    .orElseThrow(() -> new BaseException(PromotionErrorCode.PROMOTION_USAGE_RESERVATION_NOT_FOUND));

            boolean changed = reservation.cancel(
                    request.reason() == null ? PromotionUsageCancelReason.UNKNOWN : request.reason()
            );

            if (changed) {
                releaseCounters(reservation);

                PromotionUsageReservation saved = reservationRepository.save(reservation);
                eventPublisher.publishUsageCancelled(saved);

                MDC.put("eventName", "promotion.usage.cancelled");
                log.info("Promotion usage cancelled, reservationId={}, reason={}",
                        saved.getId(),
                        saved.getCancelReason());

                return promotionMapper.toResponse(saved);
            }

            return promotionMapper.toResponse(reservation);
        } catch (BaseException ex) {
            logBusinessFailure("promotion.usage.cancel.failed", ex);
            throw ex;
        } finally {
            clearMdc();
        }
    }

    @Transactional
    public int expireReservations(Instant now, int batchSize) {
        Instant referenceTime = now == null ? Instant.now() : now;

        List<PromotionUsageReservation> expiredReservations =
                reservationRepository.findExpiredReservationsForUpdate(
                        PromotionUsageReservationStatus.RESERVED,
                        referenceTime,
                        Math.max(batchSize, 1)
                );

        for (PromotionUsageReservation reservation : expiredReservations) {
            boolean changed = reservation.expire();

            if (!changed) {
                continue;
            }

            releaseCounters(reservation);

            PromotionUsageReservation saved = reservationRepository.save(reservation);
            eventPublisher.publishUsageExpired(saved);
        }

        if (!expiredReservations.isEmpty()) {
            MDC.put("eventName", "promotion.usage.expired.cleanup");
            log.info("Promotion usage reservations expired, count={}", expiredReservations.size());
            MDC.remove("eventName");
        }

        return expiredReservations.size();
    }

    @Transactional(readOnly = true)
    public PromotionUsageReservationResponse getReservation(UUID reservationId) {
        PromotionUsageReservation reservation = reservationRepository.findById(reservationId)
                .orElseThrow(() -> new BaseException(PromotionErrorCode.PROMOTION_USAGE_RESERVATION_NOT_FOUND));

        return promotionMapper.toResponse(reservation);
    }


    private void validateReserveRequest(ReservePromotionUsageRequest request) {
        if (request == null) {
            throw new BaseException(
                    PromotionErrorCode.INVALID_PROMOTION_USAGE_RESERVATION,
                    "Promotion usage reservation request is required"
            );
        }

        if (request.checkoutId() == null) {
            throw new BaseException(PromotionErrorCode.INVALID_CHECKOUT_ID);
        }

        if (request.userId() == null) {
            throw new BaseException(PromotionErrorCode.INVALID_USER_ID);
        }

        if (request.items() == null || request.items().isEmpty()) {
            throw new BaseException(
                    PromotionErrorCode.INVALID_PROMOTION_USAGE_RESERVATION,
                    "Promotion usage reservation must contain at least one item"
            );
        }
    }

    private void redeemCounters(PromotionUsageReservation reservation) {
        for (var item : reservation.getItems()) {
            Promotion promotion = promotionRepository.findByIdForUpdate(item.getPromotionId())
                    .orElseThrow(() -> new BaseException(PromotionErrorCode.PROMOTION_NOT_FOUND));

            promotion.redeemReservedUsage();
            promotionRepository.save(promotion);

            if (item.getCouponId() != null) {
                Coupon coupon = couponRepository.findByIdForUpdate(item.getCouponId())
                        .orElseThrow(() -> new BaseException(PromotionErrorCode.COUPON_NOT_FOUND));

                coupon.redeemReservedUsage();
                couponRepository.save(coupon);
            }

            if (item.getCouponAssignmentId() != null) {
                CouponAssignment assignment = assignmentRepository.findByIdForUpdate(item.getCouponAssignmentId())
                        .orElseThrow(() -> new BaseException(PromotionErrorCode.COUPON_ASSIGNMENT_NOT_FOUND));

                assignment.redeem();
                assignmentRepository.save(assignment);
            }
        }
    }

    private void releaseCounters(PromotionUsageReservation reservation) {
        for (var item : reservation.getItems()) {
            Promotion promotion = promotionRepository.findByIdForUpdate(item.getPromotionId())
                    .orElseThrow(() -> new BaseException(PromotionErrorCode.PROMOTION_NOT_FOUND));

            promotion.releaseReservedUsage();
            promotionRepository.save(promotion);

            if (item.getCouponId() != null) {
                Coupon coupon = couponRepository.findByIdForUpdate(item.getCouponId())
                        .orElseThrow(() -> new BaseException(PromotionErrorCode.COUPON_NOT_FOUND));

                coupon.releaseReservedUsage();
                couponRepository.save(coupon);
            }

            if (item.getCouponAssignmentId() != null) {
                CouponAssignment assignment = assignmentRepository.findByIdForUpdate(item.getCouponAssignmentId())
                        .orElseThrow(() -> new BaseException(PromotionErrorCode.COUPON_ASSIGNMENT_NOT_FOUND));

                assignment.cancelReservation();
                assignmentRepository.save(assignment);
            }
        }
    }

    private Map<UUID, Promotion> lockPromotions(List<AppliedDiscountResponse> discounts) {
        List<UUID> promotionIds = discounts.stream()
                .map(AppliedDiscountResponse::promotionId)
                .distinct()
                .sorted()
                .toList();

        return promotionRepository.findAllByIdsForUpdate(promotionIds)
                .stream()
                .collect(Collectors.toMap(
                        Promotion::getId,
                        promotion -> promotion,
                        (left, right) -> left,
                        LinkedHashMap::new
                ));
    }

    private void enforcePerUserPromotionLimit(Promotion promotion, UUID userId) {
        if (promotion.getPerUserUsageLimit() == null) {
            return;
        }

        long currentUsage = reservationRepository.countByUserIdAndPromotionIdAndStatuses(
                userId,
                promotion.getId(),
                List.of(
                        PromotionUsageReservationStatus.RESERVED,
                        PromotionUsageReservationStatus.REDEEMED
                )
        );

        if (currentUsage + 1 > promotion.getPerUserUsageLimit()) {
            throw new BaseException(PromotionErrorCode.PROMOTION_PER_USER_LIMIT_EXCEEDED);
        }
    }

    private void enforcePerUserCouponLimit(Coupon coupon, UUID userId) {
        if (coupon.getPerUserUsageLimit() == null) {
            return;
        }

        long currentUsage = reservationRepository.countByUserIdAndCouponIdAndStatuses(
                userId,
                coupon.getId(),
                List.of(
                        PromotionUsageReservationStatus.RESERVED,
                        PromotionUsageReservationStatus.REDEEMED
                )
        );

        if (currentUsage + 1 > coupon.getPerUserUsageLimit()) {
            throw new BaseException(PromotionErrorCode.COUPON_PER_USER_LIMIT_EXCEEDED);
        }
    }

    private List<AppliedDiscountResponse> selectDiscountsForReservation(
            List<AppliedDiscountResponse> selectedDiscounts,
            List<UUID> requestedPromotionIds
    ) {
        if (requestedPromotionIds == null || requestedPromotionIds.isEmpty()) {
            return selectedDiscounts == null ? List.of() : selectedDiscounts;
        }

        Set<UUID> requestedIds = new HashSet<>(requestedPromotionIds);

        return selectedDiscounts.stream()
                .filter(discount -> requestedIds.contains(discount.promotionId()))
                .toList();
    }

    private PromotionQuoteRequest toQuoteRequest(ReservePromotionUsageRequest request) {
        return new PromotionQuoteRequest(
                request.userId(),
                request.couponCode(),
                request.items(),
                request.shippingFee(),
                request.currency()
        );
    }

    private String requireIdempotencyKey(String idempotencyKey) {
        if (idempotencyKey == null || idempotencyKey.isBlank()) {
            throw new BaseException(PromotionErrorCode.INVALID_IDEMPOTENCY_KEY);
        }

        return idempotencyKey.trim();
    }

    private String requestHash(ReservePromotionUsageRequest request) {
        String canonical = "checkoutId=" + request.checkoutId()
                + "|userId=" + request.userId()
                + "|couponCode=" + (request.couponCode() == null ? "" : request.couponCode().trim().toUpperCase(Locale.ROOT))
                + "|shippingFee=" + request.shippingFee()
                + "|currency=" + request.currency()
                + "|items=" + request.items()
                .stream()
                .sorted(Comparator.comparing(PromotionQuoteLineRequest::productId))
                .map(item -> item.productId()
                        + ":" + item.categoryId()
                        + ":" + item.brandId()
                        + ":" + item.unitPrice()
                        + ":" + item.quantity())
                .collect(Collectors.joining(","));

        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            return HexFormat.of().formatHex(digest.digest(canonical.getBytes(StandardCharsets.UTF_8)));
        } catch (Exception ex) {
            throw new BaseException(PromotionErrorCode.INVALID_REQUEST_HASH);
        }
    }

    private void logBusinessFailure(String eventName, BaseException ex) {
        MDC.put("eventName", eventName);
        MDC.put("errorCode", ex.getErrorCode().code());
        log.warn("Promotion usage operation failed, errorCode={}", ex.getErrorCode().code());
    }

    private void clearMdc() {
        MDC.remove("eventName");
        MDC.remove("errorCode");
    }
}