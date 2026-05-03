package com.onatsubasi.finalcase.basket.infrastructure.messaging;

public final class BasketEventTypes {

    private BasketEventTypes() {
    }

    public static final String BASKET_CREATED = "basket.created";
    public static final String ITEM_ADDED = "basket.item.added";
    public static final String ITEM_QUANTITY_UPDATED = "basket.item.quantity_updated";
    public static final String ITEM_REMOVED = "basket.item.removed";
    public static final String BASKET_CLEARED = "basket.cleared";
    public static final String COUPON_INTENT_UPDATED = "basket.coupon_intent.updated";
    public static final String COUPON_INTENT_CLEARED = "basket.coupon_intent.cleared";
    public static final String BASKET_CHECKED_OUT = "basket.checked_out";
}