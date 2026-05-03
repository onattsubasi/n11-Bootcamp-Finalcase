package com.onatsubasi.finalcase.promotion.application.service;

import com.onatsubasi.finalcase.common.core.exception.BaseException;
import com.onatsubasi.finalcase.promotion.application.dto.internal.PromotionQuoteRequest;
import com.onatsubasi.finalcase.promotion.application.dto.response.AppliedDiscountResponse;
import com.onatsubasi.finalcase.promotion.application.dto.response.PromotionQuoteResponse;
import com.onatsubasi.finalcase.promotion.application.strategy.DiscountCalculationContext;
import com.onatsubasi.finalcase.promotion.application.strategy.DiscountCalculationResult;
import com.onatsubasi.finalcase.promotion.application.strategy.DiscountMath;
import com.onatsubasi.finalcase.promotion.application.strategy.DiscountStrategyFactory;
import com.onatsubasi.finalcase.promotion.domain.exception.PromotionErrorCode;
import com.onatsubasi.finalcase.promotion.domain.entity.Coupon;
import com.onatsubasi.finalcase.promotion.domain.entity.CouponAssignment;
import com.onatsubasi.finalcase.promotion.domain.entity.Promotion;
import com.onatsubasi.finalcase.promotion.domain.repository.CouponAssignmentRepository;
import com.onatsubasi.finalcase.promotion.domain.repository.CouponRepository;
import com.onatsubasi.finalcase.promotion.domain.repository.PromotionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class PromotionQuoteService {

    private final PromotionRepository promotionRepository;
    private final CouponRepository couponRepository;
    private final CouponAssignmentRepository assignmentRepository;
    private final DiscountStrategyFactory strategyFactory;

    @Transactional(readOnly = true)
    public PromotionQuoteResponse quote(PromotionQuoteRequest request) {
        validateRequest(request);

        try {
            MDC.put("eventName", "promotion.quote.started");
            log.info("Promotion quote started, userId={}, hasCoupon={}",
                    request.userId(),
                    request.couponCode() != null && !request.couponCode().isBlank());

            BigDecimal subtotal = request.items()
                    .stream()
                    .map(item -> item.subtotal())
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            BigDecimal shippingFee = DiscountMath.money(request.shippingFee());
            String currency = request.currency() == null || request.currency().isBlank()
                    ? "TRY"
                    : request.currency().trim().toUpperCase(Locale.ROOT);

            DiscountCalculationContext context = new DiscountCalculationContext(
                    request.userId(),
                    request.items(),
                    subtotal,
                    shippingFee,
                    currency
            );

            List<AppliedDiscountResponse> eligibleDiscounts = new ArrayList<>();
            List<String> ineligibleReasons = new ArrayList<>();

            for (Promotion promotion : candidatePromotions(request)) {
                try {
                    promotion.validateApplicableAt(Instant.now());

                    DiscountCalculationResult result = strategyFactory
                            .getStrategy(promotion.getType())
                            .calculate(promotion, context);

                    if (!result.hasDiscount()) {
                        continue;
                    }

                    CouponContext couponContext = resolveCouponContextIfNeeded(request, promotion);

                    eligibleDiscounts.add(toAppliedDiscount(result, couponContext));
                } catch (BaseException ex) {
                    ineligibleReasons.add(promotion.getId() + ":" + ex.getErrorCode().code());
                }
            }

            List<AppliedDiscountResponse> selectedDiscounts = selectDiscounts(eligibleDiscounts);

            BigDecimal totalDiscount = selectedDiscounts.stream()
                    .map(AppliedDiscountResponse::discountAmount)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            BigDecimal shippingDiscount = selectedDiscounts.stream()
                    .map(AppliedDiscountResponse::shippingDiscountAmount)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            BigDecimal payableAmount = subtotal
                    .add(shippingFee)
                    .subtract(totalDiscount)
                    .subtract(shippingDiscount)
                    .max(BigDecimal.ZERO);

            MDC.put("eventName", "promotion.quote.calculated");
            log.info("Promotion quote calculated, userId={}, eligibleCount={}, selectedCount={}",
                    request.userId(),
                    eligibleDiscounts.size(),
                    selectedDiscounts.size());

            return new PromotionQuoteResponse(
                    request.userId(),
                    normalizeCouponCodeOrNull(request.couponCode()),
                    DiscountMath.money(subtotal),
                    shippingFee,
                    DiscountMath.money(totalDiscount),
                    DiscountMath.money(shippingDiscount),
                    DiscountMath.money(payableAmount),
                    currency,
                    eligibleDiscounts,
                    selectedDiscounts,
                    ineligibleReasons,
                    Instant.now()
            );
        } finally {
            MDC.remove("eventName");
        }
    }


    private void validateRequest(PromotionQuoteRequest request) {
        if (request == null || request.userId() == null) {
            throw new BaseException(PromotionErrorCode.INVALID_USER_ID);
        }

        if (request.items() == null || request.items().isEmpty()) {
            throw new BaseException(
                    PromotionErrorCode.PROMOTION_QUOTE_FAILED,
                    "Promotion quote must contain at least one item"
            );
        }
    }

    private List<Promotion> candidatePromotions(PromotionQuoteRequest request) {
        List<Promotion> promotions = new ArrayList<>(
                promotionRepository.findActivePromotionsAt(Instant.now())
                        .stream()
                        .filter(promotion -> !promotion.isCouponRequired())
                        .toList()
        );

        if (request.couponCode() != null && !request.couponCode().isBlank()) {
            Coupon coupon = couponRepository.findByCode(Coupon.normalizeCode(request.couponCode()))
                    .orElseThrow(() -> new BaseException(PromotionErrorCode.COUPON_NOT_FOUND));

            coupon.validateApplicableAt(Instant.now());
            coupon.getPromotion().validateApplicableAt(Instant.now());

            promotions.add(coupon.getPromotion());
        }

        return promotions.stream()
                .sorted(
                        Comparator.comparing(Promotion::getPriority).reversed()
                                .thenComparing(Promotion::getCreatedAt)
                )
                .toList();
    }

    private CouponContext resolveCouponContextIfNeeded(
            PromotionQuoteRequest request,
            Promotion promotion
    ) {
        if (!promotion.isCouponRequired()) {
            return CouponContext.empty();
        }

        String couponCode = normalizeCouponCodeOrNull(request.couponCode());

        if (couponCode == null) {
            throw new BaseException(PromotionErrorCode.COUPON_NOT_FOUND);
        }

        Coupon coupon = couponRepository.findByCode(couponCode)
                .orElseThrow(() -> new BaseException(PromotionErrorCode.COUPON_NOT_FOUND));

        if (!coupon.getPromotion().getId().equals(promotion.getId())) {
            throw new BaseException(PromotionErrorCode.COUPON_NOT_FOUND);
        }

        CouponAssignment assignment = assignmentRepository
                .findByCouponIdAndUserId(coupon.getId(), request.userId())
                .orElse(null);

        if (assignment != null) {
            assignment.validateUsableAt(Instant.now());
            return CouponContext.of(coupon, assignment);
        }

        return CouponContext.of(coupon, null);
    }

    private List<AppliedDiscountResponse> selectDiscounts(List<AppliedDiscountResponse> eligibleDiscounts) {
        return eligibleDiscounts.stream()
                .max(Comparator.comparing(AppliedDiscountResponse::totalDiscountAmount))
                .map(List::of)
                .orElse(List.of());
    }

    private AppliedDiscountResponse toAppliedDiscount(
            DiscountCalculationResult result,
            CouponContext couponContext
    ) {
        return new AppliedDiscountResponse(
                result.promotionId(),
                couponContext.couponId(),
                couponContext.assignmentId(),
                couponContext.couponCode(),
                result.promotionType(),
                result.description(),
                DiscountMath.money(result.discountAmount()),
                DiscountMath.money(result.shippingDiscountAmount()),
                DiscountMath.money(result.totalDiscountAmount())
        );
    }

    private String normalizeCouponCodeOrNull(String value) {
        return value == null || value.isBlank()
                ? null
                : Coupon.normalizeCode(value);
    }

    public record CouponContext(
            UUID couponId,
            UUID assignmentId,
            String couponCode
    ) {

        static CouponContext empty() {
            return new CouponContext(null, null, null);
        }

        static CouponContext of(Coupon coupon, CouponAssignment assignment) {
            return new CouponContext(
                    coupon.getId(),
                    assignment == null ? null : assignment.getId(),
                    coupon.getCode()
            );
        }
    }
}