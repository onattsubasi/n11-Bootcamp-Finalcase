package com.onatsubasi.finalcase.catalog.domain.exception;

import com.onatsubasi.finalcase.common.core.exception.ErrorCode;

public enum CatalogErrorCode implements ErrorCode {

    PRODUCT_NOT_FOUND("CATALOG-001", "Product not found", 404),
    CATEGORY_NOT_FOUND("CATALOG-002", "Category not found", 404),
    BRAND_NOT_FOUND("CATALOG-003", "Brand not found", 404),

    PRODUCT_SKU_ALREADY_EXISTS("CATALOG-004", "Product SKU already exists", 409),
    PRODUCT_SLUG_ALREADY_EXISTS("CATALOG-005", "Product slug already exists", 409),
    CATEGORY_SLUG_ALREADY_EXISTS("CATALOG-006", "Category slug already exists", 409),
    CATEGORY_PATH_ALREADY_EXISTS("CATALOG-007", "Category path already exists", 409),
    BRAND_SLUG_ALREADY_EXISTS("CATALOG-008", "Brand slug already exists", 409),
    DUPLICATE_CATALOG_RECORD("CATALOG-009", "Duplicate catalog record", 409),

    INVALID_PRODUCT_DATA("CATALOG-020", "Invalid product data", 400),
    INVALID_CATEGORY_DATA("CATALOG-021", "Invalid category data", 400),
    INVALID_BRAND_DATA("CATALOG-022", "Invalid brand data", 400),
    INVALID_PRICE("CATALOG-023", "Invalid product price", 400),
    INVALID_PRODUCT_STATUS_TRANSITION("CATALOG-024", "Invalid product status transition", 400),
    INVALID_CATEGORY_STATUS_TRANSITION("CATALOG-025", "Invalid category status transition", 400),
    INVALID_BRAND_STATUS_TRANSITION("CATALOG-026", "Invalid brand status transition", 400),

    CATEGORY_IN_USE("CATALOG-030", "Category is used by active products", 409),
    BRAND_IN_USE("CATALOG-031", "Brand is used by active products", 409),

    PRODUCT_NOT_ACTIVE("CATALOG-040", "Product is not active", 409),
    CATEGORY_NOT_ACTIVE("CATALOG-041", "Category is not active", 409),
    BRAND_NOT_ACTIVE("CATALOG-042", "Brand is not active", 409),

    CATALOG_STORAGE_ERROR("CATALOG-999", "Catalog storage error", 500);

    private final String code;
    private final String defaultMessage;
    private final int httpStatus;

    CatalogErrorCode(String code, String defaultMessage, int httpStatus) {
        this.code = code;
        this.defaultMessage = defaultMessage;
        this.httpStatus = httpStatus;
    }

    @Override
    public String code() {
        return code;
    }

    @Override
    public String defaultMessage() {
        return defaultMessage;
    }

    @Override
    public int httpStatus() {
        return httpStatus;
    }
}