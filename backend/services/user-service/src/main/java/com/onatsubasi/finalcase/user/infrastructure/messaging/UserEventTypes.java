package com.onatsubasi.finalcase.user.infrastructure.messaging;

public final class UserEventTypes {

    private UserEventTypes() {
    }

    public static final String PROFILE_CREATED = "user.profile.created";
    public static final String PROFILE_UPDATED = "user.profile.updated";

    public static final String ADDRESS_CREATED = "user.address.created";
    public static final String ADDRESS_UPDATED = "user.address.updated";
    public static final String ADDRESS_DELETED = "user.address.deleted";

    public static final String FAVORITE_ADDED = "user.favorite.added";
    public static final String FAVORITE_REMOVED = "user.favorite.removed";

    public static final String PRODUCT_LIST_CREATED = "user.product_list.created";
    public static final String PRODUCT_LIST_UPDATED = "user.product_list.updated";
    public static final String PRODUCT_LIST_DELETED = "user.product_list.deleted";
    public static final String PRODUCT_LIST_ITEM_ADDED = "user.product_list.item_added";
    public static final String PRODUCT_LIST_ITEM_REMOVED = "user.product_list.item_removed";
}
