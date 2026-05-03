package com.onatsubasi.finalcase.catalog.infrastructure.persistence;

import com.onatsubasi.finalcase.catalog.domain.entity.Category;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import com.onatsubasi.finalcase.catalog.support.AbstractCatalogIntegrationTest;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

class CategoryRepositoryIntegrationTest extends AbstractCatalogIntegrationTest {


    @Autowired
    private JpaCategoryRepositoryAdapter categoryRepository;

    @Test
    @DisplayName("Should save and find category by slug")
    void shouldSaveAndFindBySlug() {
        // Given
        Category category = Category.createRoot("Electronics", "electronics", "Desc", 0);

        // When
        categoryRepository.save(category);
        Optional<Category> found = categoryRepository.findBySlug("electronics");

        // Then
        assertThat(found).isPresent();
        assertThat(found.get().getName()).isEqualTo("Electronics");
    }
}
