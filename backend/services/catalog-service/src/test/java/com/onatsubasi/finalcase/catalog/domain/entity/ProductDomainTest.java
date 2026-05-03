package com.onatsubasi.finalcase.catalog.domain.entity;

import com.onatsubasi.finalcase.catalog.domain.enums.ProductOwnerType;
import com.onatsubasi.finalcase.catalog.domain.enums.ProductStatus;
import com.onatsubasi.finalcase.catalog.domain.exception.CatalogErrorCode;
import com.onatsubasi.finalcase.catalog.domain.valueobject.BrandSnapshot;
import com.onatsubasi.finalcase.catalog.domain.valueobject.CategorySnapshot;
import com.onatsubasi.finalcase.catalog.domain.valueobject.Money;
import com.onatsubasi.finalcase.catalog.domain.valueobject.ProductOwnership;
import com.onatsubasi.finalcase.common.core.exception.BaseException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ProductDomainTest {

    @Test
    @DisplayName("Should create draft product with platform ownership")
    void shouldCreateDraftProductWithPlatformOwnership() {
        Product product = sampleProduct();

        assertThat(product.getStatus()).isEqualTo(ProductStatus.DRAFT);
        assertThat(product.isSellable()).isFalse();
        assertThat(product.getOwnership().getOwnerType()).isEqualTo(ProductOwnerType.PLATFORM);
        assertThat(product.getBasePrice().getCurrency()).isEqualTo("TRY");
    }

    @Test
    @DisplayName("Should activate product and make it sellable")
    void shouldActivateProduct() {
        Product product = sampleProduct();

        product.activate();

        assertThat(product.getStatus()).isEqualTo(ProductStatus.ACTIVE);
        assertThat(product.isSellable()).isTrue();
    }

    @Test
    @DisplayName("Should reject invalid draft to suspend transition")
    void shouldRejectInvalidSuspendTransition() {
        Product product = sampleProduct();

        assertThatThrownBy(product::suspend)
                .isInstanceOf(BaseException.class)
                .hasFieldOrPropertyWithValue("errorCode", CatalogErrorCode.INVALID_PRODUCT_STATUS_TRANSITION);
    }

    @Test
    @DisplayName("Should protect attribute map from external mutation")
    void shouldProtectAttributeMapFromExternalMutation() {
        Product product = sampleProduct();

        assertThatThrownBy(() -> product.getAttributes().put("ram", "8GB"))
                .isInstanceOf(UnsupportedOperationException.class);
    }

    private Product sampleProduct() {
        return Product.createDraft(
                "SKU-DOMAIN-1",
                "Domain Product",
                "domain-product",
                "Domain description",
                Money.of(BigDecimal.valueOf(100), "TRY"),
                new BrandSnapshot(UUID.randomUUID(), "Brand", "brand"),
                new CategorySnapshot(UUID.randomUUID(), "Category", "category", "category", List.of()),
                ProductOwnership.platform("platform-store", "Platform Store"),
                List.of(),
                Map.of("color", "Black")
        );
    }
}
