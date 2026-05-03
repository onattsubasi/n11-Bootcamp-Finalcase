package com.onatsubasi.finalcase.promotion.infrastructure.mapper;

import com.onatsubasi.finalcase.promotion.application.dto.response.*;
import com.onatsubasi.finalcase.promotion.domain.entity.*;
import org.springframework.stereotype.Component;

import java.util.Comparator;
import java.util.List;

@Component
public class PromotionMapper {

    public PromotionResponse toResponse(Promotion promotion) {
        return new PromotionResponse(
                promotion.getId(),
                promotion.getName(),
                promotion.getDescription(),
                promotion.getType(),
                promotion.getStatus(),
                promotion.isCouponRequired(),
                promotion.isStackable(),
                promotion.getPriority(),
                promotion.getRuleConfig(),
                promotion.getGlobalUsageLimit(),
                promotion.getPerUserUsageLimit(),
                promotion.getReservedUsageCount(),
                promotion.getRedeemedUsageCount(),
                promotion.getStartsAt(),
                promotion.getEndsAt(),
                promotion.getCreatedAt(),
                promotion.getUpdatedAt()
        );
    }

    public CouponResponse toResponse(Coupon coupon) {
        return new CouponResponse(
                coupon.getId(),
                coupon.getPromotion().getId(),
                coupon.getCode(),
                coupon.getStatus(),
                coupon.getUsageLimit(),
                coupon.getPerUserUsageLimit(),
                coupon.getReservedUsageCount(),
                coupon.getRedeemedUsageCount(),
                coupon.getStartsAt(),
                coupon.getEndsAt(),
                coupon.getCreatedAt(),
                coupon.getUpdatedAt()
        );
    }

    public CouponAssignmentResponse toResponse(CouponAssignment assignment) {
        Coupon coupon = assignment.getCoupon();

        return new CouponAssignmentResponse(
                assignment.getId(),
                coupon.getId(),
                coupon.getCode(),
                coupon.getPromotion().getId(),
                assignment.getUserId(),
                assignment.getStatus(),
                assignment.getAssignedAt(),
                assignment.getExpiresAt(),
                assignment.getReservedAt(),
                assignment.getRedeemedAt(),
                assignment.getCancelledAt(),
                assignment.getExpiredAt(),
                assignment.getUpdatedAt()
        );
    }

    public PromotionUsageReservationResponse toResponse(PromotionUsageReservation reservation) {
        List<PromotionUsageReservationItemResponse> items = reservation.getItems()
                .stream()
                .sorted(Comparator.comparing(PromotionUsageReservationItem::getCreatedAt))
                .map(this::toResponse)
                .toList();

        return new PromotionUsageReservationResponse(
                reservation.getId(),
                reservation.getIdempotencyKey(),
                reservation.getCheckoutId(),
                reservation.getUserId(),
                reservation.getOrderId(),
                reservation.getStatus(),
                reservation.getReservedUntil(),
                reservation.getRedeemedAt(),
                reservation.getCancelledAt(),
                reservation.getExpiredAt(),
                reservation.getCancelReason(),
                items,
                reservation.getCreatedAt(),
                reservation.getUpdatedAt()
        );
    }

    public PromotionUsageReservationItemResponse toResponse(PromotionUsageReservationItem item) {
        return new PromotionUsageReservationItemResponse(
                item.getId(),
                item.getPromotionId(),
                item.getCouponId(),
                item.getCouponAssignmentId(),
                item.getCouponCode(),
                item.getDiscountAmount(),
                item.getShippingDiscountAmount(),
                item.totalDiscountAmount(),
                item.getDescription(),
                item.getCreatedAt()
        );
    }
}