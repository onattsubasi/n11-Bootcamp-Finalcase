package com.onatsubasi.finalcase.catalog.infrastructure.persistence;

import com.onatsubasi.finalcase.catalog.domain.entity.Brand;
import com.onatsubasi.finalcase.catalog.domain.entity.Category;
import com.onatsubasi.finalcase.catalog.domain.entity.Product;
import com.onatsubasi.finalcase.catalog.domain.valueobject.BrandSnapshot;
import com.onatsubasi.finalcase.catalog.domain.valueobject.CategorySnapshot;
import com.onatsubasi.finalcase.catalog.domain.valueobject.Money;
import com.onatsubasi.finalcase.catalog.domain.valueobject.ProductOwnership;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import com.onatsubasi.finalcase.catalog.support.AbstractCatalogIntegrationTest;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

class ProductRepositoryIntegrationTest extends AbstractCatalogIntegrationTest {


    @Autowired
    private JpaProductRepositoryAdapter productRepository;

    @Autowired
    private SpringDataBrandJpaRepository brandJpaRepository;

    @Autowired
    private SpringDataCategoryJpaRepository categoryJpaRepository;

    @Test
    @DisplayName("Should save and find product by SKU")
    void shouldSaveAndFindBySku() {
        // Given
        Brand brand = Brand.create("Brand A", "brand-a", null, null);
        brandJpaRepository.save(brand);

        Category category = Category.createRoot("Category A", "category-a", null, 0);
        categoryJpaRepository.save(category);

        Product product = Product.createDraft(
                "SKU-INT-1",
                "Integration Test Product",
                "integration-test-product",
                "Description",
                Money.of(BigDecimal.valueOf(100), "TRY"),
                BrandSnapshot.from(brand),
                new CategorySnapshot(category.getId(), category.getName(), category.getSlug(), category.getPath(), List.of()),
                ProductOwnership.platform("platform", "Platform Store"),
                List.of(),
                Map.of()
        );

        // When
        productRepository.save(product);
        Optional<Product> found = productRepository.findBySku("SKU-INT-1");

        // Then
        assertThat(found).isPresent();
        assertThat(found.get().getName()).isEqualTo("Integration Test Product");
        assertThat(found.get().getBrand().getName()).isEqualTo("Brand A");
    }
}
