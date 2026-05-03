package com.onatsubasi.finalcase.catalog.domain.valueobject;

import com.onatsubasi.finalcase.catalog.domain.enums.ProductOwnerType;
import com.onatsubasi.finalcase.catalog.domain.exception.CatalogErrorCode;
import com.onatsubasi.finalcase.common.core.exception.BaseException;
import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Embeddable
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ProductOwnership {

    @Enumerated(EnumType.STRING)
    @Column(name = "owner_type", nullable = false, length = 30)
    private ProductOwnerType ownerType;

    @Column(name = "owner_store_id", nullable = false, length = 100)
    private String storeId;

    @Column(name = "owner_store_name", nullable = false, length = 200)
    private String storeName;

    private ProductOwnership(
            ProductOwnerType ownerType,
            String storeId,
            String storeName
    ) {
        if (ownerType == null) {
            throw new BaseException(
                    CatalogErrorCode.INVALID_PRODUCT_DATA,
                    "Product owner type is required"
            );
        }

        if (storeId == null || storeId.isBlank()) {
            throw new BaseException(
                    CatalogErrorCode.INVALID_PRODUCT_DATA,
                    "Store id is required"
            );
        }

        if (storeName == null || storeName.isBlank()) {
            throw new BaseException(
                    CatalogErrorCode.INVALID_PRODUCT_DATA,
                    "Store name is required"
            );
        }

        this.ownerType = ownerType;
        this.storeId = storeId.trim();
        this.storeName = storeName.trim();
    }

    public static ProductOwnership platform(
            String platformStoreId,
            String platformStoreName
    ) {
        return new ProductOwnership(
                ProductOwnerType.PLATFORM,
                platformStoreId,
                platformStoreName
        );
    }

    public static ProductOwnership store(
            String storeId,
            String storeName
    ) {
        return new ProductOwnership(
                ProductOwnerType.STORE,
                storeId,
                storeName
        );
    }

    public boolean isPlatformOwned() {
        return ownerType == ProductOwnerType.PLATFORM;
    }
}