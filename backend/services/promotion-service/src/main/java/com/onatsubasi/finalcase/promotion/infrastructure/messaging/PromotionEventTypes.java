package com.onatsubasi.finalcase.promotion.infrastructure.messaging;

public final class PromotionEventTypes {

    private PromotionEventTypes() {
    }

    public static final String PROMOTION_CREATED = "promotion.created";
    public static final String PROMOTION_UPDATED = "promotion.updated";
    public static final String PROMOTION_ACTIVATED = "promotion.activated";
    public static final String PROMOTION_PAUSED = "promotion.paused";
    public static final String PROMOTION_EXPIRED = "promotion.expired";
    public static final String PROMOTION_DELETED = "promotion.deleted";

    public static final String COUPON_CREATED = "promotion.coupon.created";
    public static final String COUPON_UPDATED = "promotion.coupon.updated";
    public static final String COUPON_ACTIVATED = "promotion.coupon.activated";
    public static final String COUPON_DEACTIVATED = "promotion.coupon.deactivated";
    public static final String COUPON_EXPIRED = "promotion.coupon.expired";
    public static final String COUPON_ASSIGNED = "promotion.coupon.assigned";

    public static final String USAGE_RESERVED = "promotion.usage.reserved";
    public static final String USAGE_REDEEMED = "promotion.usage.redeemed";
    public static final String USAGE_CANCELLED = "promotion.usage.cancelled";
    public static final String USAGE_EXPIRED = "promotion.usage.expired";
}