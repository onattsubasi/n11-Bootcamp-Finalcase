package com.onatsubasi.finalcase.catalog.infrastructure.persistence;

import com.onatsubasi.finalcase.catalog.domain.entity.Brand;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import com.onatsubasi.finalcase.catalog.support.AbstractCatalogIntegrationTest;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

class BrandRepositoryIntegrationTest extends AbstractCatalogIntegrationTest {


    @Autowired
    private JpaBrandRepositoryAdapter brandRepository;

    @Test
    @DisplayName("Should save and find brand by slug")
    void shouldSaveAndFindBySlug() {
        // Given
        Brand brand = Brand.create("Nike", "nike", "Desc", null);

        // When
        brandRepository.save(brand);
        Optional<Brand> found = brandRepository.findBySlug("nike");

        // Then
        assertThat(found).isPresent();
        assertThat(found.get().getName()).isEqualTo("Nike");
    }
}
