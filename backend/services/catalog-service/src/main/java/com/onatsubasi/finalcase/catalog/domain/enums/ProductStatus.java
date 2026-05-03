package com.onatsubasi.finalcase.catalog.domain.enums;

public enum ProductStatus {
    DRAFT,
    ACTIVE,
    SUSPENDED,
    DELETED,

    /*
     * Reserved for the final multi-vendor expansion.
     * In the initial single-vendor release, Admin-created products
     * do not need seller approval.
     */
    PENDING_APPROVAL,
    REJECTED
}