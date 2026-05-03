package com.onatsubasi.finalcase.search.infrastructure.messaging;

public final class SearchConsumedEventTypes {

    private SearchConsumedEventTypes() {
    }

    public static final String CATALOG_PRODUCT_CREATED = "catalog.product.created";
    public static final String CATALOG_PRODUCT_UPDATED = "catalog.product.updated";
    public static final String CATALOG_PRODUCT_DELETED = "catalog.product.deleted";
    public static final String CATALOG_PRODUCT_ACTIVATED = "catalog.product.activated";
    public static final String CATALOG_PRODUCT_DEACTIVATED = "catalog.product.deactivated";
    public static final String CATALOG_PRODUCT_STATUS_CHANGED = "catalog.product.status_changed";
    public static final String CATALOG_CATEGORY_UPDATED = "catalog.category.updated";
    public static final String CATALOG_BRAND_UPDATED = "catalog.brand.updated";

    public static final String INVENTORY_STOCK_UPDATED = "inventory.stock.updated";
    public static final String INVENTORY_STOCK_LOW = "inventory.stock.low";
    public static final String INVENTORY_BACK_IN_STOCK = "inventory.stock.back_in_stock";
    public static final String INVENTORY_OUT_OF_STOCK = "inventory.stock.out_of_stock";

    public static final String PROMOTION_PRODUCT_PROJECTION_UPDATED = "promotion.product_projection.updated";
    public static final String PROMOTION_PRODUCT_PROJECTION_CLEARED = "promotion.product_projection.cleared";

    public static final String REVIEW_RATING_SUMMARY_UPDATED = "review.rating_summary.updated";
}
