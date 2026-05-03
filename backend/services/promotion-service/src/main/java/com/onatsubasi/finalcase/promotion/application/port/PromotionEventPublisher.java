package com.onatsubasi.finalcase.promotion.application.port;

import com.onatsubasi.finalcase.promotion.domain.entity.Coupon;
import com.onatsubasi.finalcase.promotion.domain.entity.CouponAssignment;
import com.onatsubasi.finalcase.promotion.domain.entity.Promotion;
import com.onatsubasi.finalcase.promotion.domain.entity.PromotionUsageReservation;

public interface PromotionEventPublisher {

    void publishPromotionCreated(Promotion promotion);

    void publishPromotionUpdated(Promotion promotion);

    void publishPromotionActivated(Promotion promotion);

    void publishPromotionPaused(Promotion promotion);

    void publishPromotionExpired(Promotion promotion);

    void publishPromotionDeleted(Promotion promotion);

    void publishCouponCreated(Coupon coupon);

    void publishCouponUpdated(Coupon coupon);

    void publishCouponActivated(Coupon coupon);

    void publishCouponDeactivated(Coupon coupon);

    void publishCouponExpired(Coupon coupon);

    void publishCouponAssigned(CouponAssignment assignment);

    void publishUsageReserved(PromotionUsageReservation reservation);

    void publishUsageRedeemed(PromotionUsageReservation reservation);

    void publishUsageCancelled(PromotionUsageReservation reservation);

    void publishUsageExpired(PromotionUsageReservation reservation);
}