package com.onatsubasi.finalcase.search.domain.exception;

import com.onatsubasi.finalcase.common.core.exception.ErrorCode;

public enum SearchErrorCode implements ErrorCode {

    PRODUCT_SEARCH_DOCUMENT_NOT_FOUND("SEARCH-001", "Product search document not found", 404),
    INVALID_SEARCH_REQUEST("SEARCH-002", "Invalid search request", 400),
    SEARCH_QUERY_FAILED("SEARCH-003", "Search query failed", 409),

    PROJECTION_EVENT_ALREADY_PROCESSED("SEARCH-004", "Projection event already processed", 409),
    INVALID_SEARCH_DOCUMENT_DATA("SEARCH-005", "Invalid search document data", 400),
    INVALID_EVENT_DATA("SEARCH-006", "Invalid projection event data", 400),

    INDEX_REBUILD_FAILED("SEARCH-010", "Search index rebuild failed", 502),
    CATALOG_REBUILD_SOURCE_UNAVAILABLE("SEARCH-011", "Catalog rebuild source is unavailable", 503),

    INVALID_PRODUCT_ID("SEARCH-020", "Product id is required", 400),
    INVALID_BRAND_ID("SEARCH-021", "Brand id is invalid", 400),
    INVALID_CATEGORY_ID("SEARCH-022", "Category id is invalid", 400),
    INVALID_PRICE_RANGE("SEARCH-023", "Invalid price range", 400),
    INVALID_SORT("SEARCH-024", "Invalid search sort", 400),

    SEARCH_STORAGE_ERROR("SEARCH-999", "Search storage error", 500);

    private final String code;
    private final String defaultMessage;
    private final int httpStatus;

    SearchErrorCode(String code, String defaultMessage, int httpStatus) {
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