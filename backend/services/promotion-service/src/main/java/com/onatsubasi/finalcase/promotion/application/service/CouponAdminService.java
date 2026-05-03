package com.onatsubasi.finalcase.promotion.application.service;

import com.onatsubasi.finalcase.common.core.exception.BaseException;
import com.onatsubasi.finalcase.promotion.application.dto.request.AssignCouponRequest;
import com.onatsubasi.finalcase.promotion.application.dto.request.CreateCouponBatchRequest;
import com.onatsubasi.finalcase.promotion.application.dto.request.CreateCouponRequest;
import com.onatsubasi.finalcase.promotion.application.dto.request.UpdateCouponRequest;
import com.onatsubasi.finalcase.promotion.application.dto.response.CouponAssignmentResponse;
import com.onatsubasi.finalcase.promotion.application.dto.response.CouponResponse;
import com.onatsubasi.finalcase.promotion.application.port.PromotionEventPublisher;
import com.onatsubasi.finalcase.promotion.domain.exception.PromotionErrorCode;
import com.onatsubasi.finalcase.promotion.domain.entity.Coupon;
import com.onatsubasi.finalcase.promotion.domain.entity.CouponAssignment;
import com.onatsubasi.finalcase.promotion.domain.entity.Promotion;
import com.onatsubasi.finalcase.promotion.domain.repository.CouponAssignmentRepository;
import com.onatsubasi.finalcase.promotion.domain.repository.CouponRepository;
import com.onatsubasi.finalcase.promotion.domain.repository.PromotionRepository;
import com.onatsubasi.finalcase.promotion.infrastructure.mapper.PromotionMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class CouponAdminService {

    private static final String ALPHABET = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789";
    private static final SecureRandom RANDOM = new SecureRandom();

    private final PromotionRepository promotionRepository;
    private final CouponRepository couponRepository;
    private final CouponAssignmentRepository assignmentRepository;
    private final PromotionMapper promotionMapper;
    private final PromotionEventPublisher eventPublisher;

    @Transactional
    public CouponResponse createCoupon(CreateCouponRequest request) {
        try {
            MDC.put("eventName", "coupon.create.started");
            log.info("Coupon creation started, promotionId={}", request.promotionId());

            String normalizedCode = Coupon.normalizeCode(request.code());

            if (couponRepository.existsByCode(normalizedCode)) {
                throw new BaseException(PromotionErrorCode.COUPON_ALREADY_EXISTS);
            }

            Promotion promotion = promotionRepository.findById(request.promotionId())
                    .orElseThrow(() -> new BaseException(PromotionErrorCode.PROMOTION_NOT_FOUND));

            Coupon coupon = Coupon.create(
                    normalizedCode,
                    promotion,
                    request.usageLimit(),
                    request.perUserUsageLimit(),
                    request.startsAt(),
                    request.endsAt()
            );

            Coupon saved = couponRepository.save(coupon);
            eventPublisher.publishCouponCreated(saved);

            MDC.put("eventName", "coupon.created");
            log.info("Coupon created, couponId={}, promotionId={}", saved.getId(), promotion.getId());

            return promotionMapper.toResponse(saved);
        } catch (BaseException ex) {
            logBusinessFailure("coupon.create.failed", ex);
            throw ex;
        } finally {
            clearMdc();
        }
    }


    @Transactional
    public CouponResponse updateCoupon(UUID couponId, UpdateCouponRequest request) {
        try {
            MDC.put("eventName", "coupon.update.started");
            log.info("Coupon update started, couponId={}", couponId);

            Coupon coupon = couponRepository.findById(couponId)
                    .orElseThrow(() -> new BaseException(PromotionErrorCode.COUPON_NOT_FOUND));

            coupon.update(
                    request.usageLimit(),
                    request.perUserUsageLimit(),
                    request.startsAt(),
                    request.endsAt()
            );

            Coupon saved = couponRepository.save(coupon);
            eventPublisher.publishCouponUpdated(saved);

            MDC.put("eventName", "coupon.updated");
            log.info("Coupon updated, couponId={}", saved.getId());

            return promotionMapper.toResponse(saved);
        } catch (BaseException ex) {
            logBusinessFailure("coupon.update.failed", ex);
            throw ex;
        } finally {
            clearMdc();
        }
    }

    @Transactional
    public CouponResponse activateCoupon(UUID couponId) {
        try {
            MDC.put("eventName", "coupon.activate.started");
            log.info("Coupon activate started, couponId={}", couponId);

            Coupon coupon = couponRepository.findById(couponId)
                    .orElseThrow(() -> new BaseException(PromotionErrorCode.COUPON_NOT_FOUND));

            coupon.activate();

            Coupon saved = couponRepository.save(coupon);
            eventPublisher.publishCouponActivated(saved);

            MDC.put("eventName", "coupon.activated");
            log.info("Coupon activated, couponId={}", saved.getId());

            return promotionMapper.toResponse(saved);
        } catch (BaseException ex) {
            logBusinessFailure("coupon.activate.failed", ex);
            throw ex;
        } finally {
            clearMdc();
        }
    }

    @Transactional
    public CouponResponse deactivateCoupon(UUID couponId) {
        try {
            MDC.put("eventName", "coupon.deactivate.started");
            log.info("Coupon deactivate started, couponId={}", couponId);

            Coupon coupon = couponRepository.findById(couponId)
                    .orElseThrow(() -> new BaseException(PromotionErrorCode.COUPON_NOT_FOUND));

            coupon.deactivate();

            Coupon saved = couponRepository.save(coupon);
            eventPublisher.publishCouponDeactivated(saved);

            MDC.put("eventName", "coupon.deactivated");
            log.info("Coupon deactivated, couponId={}", saved.getId());

            return promotionMapper.toResponse(saved);
        } catch (BaseException ex) {
            logBusinessFailure("coupon.deactivate.failed", ex);
            throw ex;
        } finally {
            clearMdc();
        }
    }

    @Transactional
    public CouponResponse expireCoupon(UUID couponId) {
        try {
            MDC.put("eventName", "coupon.expire.started");
            log.info("Coupon expire started, couponId={}", couponId);

            Coupon coupon = couponRepository.findById(couponId)
                    .orElseThrow(() -> new BaseException(PromotionErrorCode.COUPON_NOT_FOUND));

            coupon.expire();

            Coupon saved = couponRepository.save(coupon);
            eventPublisher.publishCouponExpired(saved);

            MDC.put("eventName", "coupon.expired");
            log.info("Coupon expired, couponId={}", saved.getId());

            return promotionMapper.toResponse(saved);
        } catch (BaseException ex) {
            logBusinessFailure("coupon.expire.failed", ex);
            throw ex;
        } finally {
            clearMdc();
        }
    }

    @Transactional
    public List<CouponResponse> createCouponBatch(CreateCouponBatchRequest request) {
        List<CouponResponse> responses = new ArrayList<>();

        for (int i = 0; i < request.count(); i++) {
            String code = request.codePrefix() + "-" + randomSuffix(8);

            responses.add(createCoupon(new CreateCouponRequest(
                    request.promotionId(),
                    code,
                    request.usageLimit(),
                    request.perUserUsageLimit(),
                    request.startsAt(),
                    request.endsAt()
            )));
        }

        return responses;
    }

    @Transactional
    public CouponAssignmentResponse assignCoupon(AssignCouponRequest request) {
        try {
            MDC.put("eventName", "coupon.assign.started");
            log.info("Coupon assignment started, couponId={}, userId={}", request.couponId(), request.userId());

            if (assignmentRepository.existsByCouponIdAndUserId(request.couponId(), request.userId())) {
                CouponAssignment existing = assignmentRepository
                        .findByCouponIdAndUserId(request.couponId(), request.userId())
                        .orElseThrow(() -> new BaseException(PromotionErrorCode.COUPON_ASSIGNMENT_NOT_FOUND));

                return promotionMapper.toResponse(existing);
            }

            Coupon coupon = couponRepository.findById(request.couponId())
                    .orElseThrow(() -> new BaseException(PromotionErrorCode.COUPON_NOT_FOUND));

            CouponAssignment assignment = CouponAssignment.assign(
                    coupon,
                    request.userId(),
                    request.expiresAt()
            );

            CouponAssignment saved = assignmentRepository.save(assignment);
            eventPublisher.publishCouponAssigned(saved);

            MDC.put("eventName", "coupon.assigned");
            log.info("Coupon assigned, assignmentId={}, couponId={}, userId={}",
                    saved.getId(),
                    coupon.getId(),
                    saved.getUserId());

            return promotionMapper.toResponse(saved);
        } catch (BaseException ex) {
            logBusinessFailure("coupon.assign.failed", ex);
            throw ex;
        } finally {
            clearMdc();
        }
    }

    @Transactional(readOnly = true)
    public List<CouponResponse> listCouponsByPromotion(UUID promotionId) {
        return couponRepository.findByPromotionId(promotionId)
                .stream()
                .map(promotionMapper::toResponse)
                .toList();
    }

    private String randomSuffix(int length) {
        StringBuilder builder = new StringBuilder(length);

        for (int i = 0; i < length; i++) {
            builder.append(ALPHABET.charAt(RANDOM.nextInt(ALPHABET.length())));
        }

        return builder.toString();
    }

    private void logBusinessFailure(String eventName, BaseException ex) {
        MDC.put("eventName", eventName);
        MDC.put("errorCode", ex.getErrorCode().code());
        log.warn("Coupon admin operation failed, errorCode={}", ex.getErrorCode().code());
    }

    private void clearMdc() {
        MDC.remove("eventName");
        MDC.remove("errorCode");
    }
}
