package com.onatsubasi.finalcase.catalog.infrastructure.messaging;

public final class CatalogEventTypes {

    private CatalogEventTypes() {
    }

    public static final String PRODUCT_CREATED = "catalog.product.created";
    public static final String PRODUCT_UPDATED = "catalog.product.updated";
    public static final String PRODUCT_PRICE_CHANGED = "catalog.product.price_changed";
    public static final String PRODUCT_STATUS_CHANGED = "catalog.product.status_changed";
    public static final String PRODUCT_DELETED = "catalog.product.deleted";

    public static final String CATEGORY_CREATED = "catalog.category.created";
    public static final String CATEGORY_UPDATED = "catalog.category.updated";
    public static final String CATEGORY_STATUS_CHANGED = "catalog.category.status_changed";

    public static final String BRAND_CREATED = "catalog.brand.created";
    public static final String BRAND_UPDATED = "catalog.brand.updated";
    public static final String BRAND_STATUS_CHANGED = "catalog.brand.status_changed";
}