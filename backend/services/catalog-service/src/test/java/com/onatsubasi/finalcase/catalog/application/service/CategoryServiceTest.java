package com.onatsubasi.finalcase.catalog.application.service;

import com.onatsubasi.finalcase.catalog.application.dto.request.CreateCategoryRequest;
import com.onatsubasi.finalcase.catalog.application.dto.response.CategoryResponse;
import com.onatsubasi.finalcase.catalog.application.port.CatalogEventPublisher;
import com.onatsubasi.finalcase.catalog.application.support.SlugGenerator;
import com.onatsubasi.finalcase.catalog.domain.exception.CatalogErrorCode;
import com.onatsubasi.finalcase.catalog.domain.entity.Category;
import com.onatsubasi.finalcase.catalog.domain.repository.CategoryRepository;
import com.onatsubasi.finalcase.catalog.domain.repository.ProductRepository;
import com.onatsubasi.finalcase.catalog.infrastructure.mapper.CategoryMapper;
import com.onatsubasi.finalcase.common.core.exception.BaseException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CategoryServiceTest {

    @Mock
    private CategoryRepository categoryRepository;

    @Mock
    private ProductRepository productRepository;

    @Mock
    private CategoryMapper categoryMapper;

    @Mock
    private CatalogEventPublisher eventPublisher;

    @Mock
    private SlugGenerator slugGenerator;

    @InjectMocks
    private CategoryService categoryService;

    @Test
    @DisplayName("Should create root category successfully")
    void shouldCreateRootCategory() {
        // Given
        CreateCategoryRequest request = new CreateCategoryRequest("Electronics", "electronics", "Desc", null, 1);
        when(slugGenerator.generate(any(), any(), any())).thenReturn("electronics");
        when(categoryRepository.existsBySlug(anyString())).thenReturn(false);
        when(categoryRepository.existsByPath(anyString())).thenReturn(false);
        
        Category savedCategory = mock(Category.class);
        when(categoryRepository.save(any(Category.class))).thenReturn(savedCategory);
        when(categoryMapper.toResponse(any())).thenReturn(mock(CategoryResponse.class));

        // When
        CategoryResponse response = categoryService.create(request);

        // Then
        assertThat(response).isNotNull();
        verify(categoryRepository).save(any(Category.class));
        verify(eventPublisher).publishCategoryCreated(any(Category.class));
    }

    @Test
    @DisplayName("Should create child category successfully")
    void shouldCreateChildCategory() {
        // Given
        UUID parentId = UUID.randomUUID();
        CreateCategoryRequest request = new CreateCategoryRequest("Phones", "phones", "Desc", parentId, 1);
        
        Category parent = mock(Category.class);
        when(parent.getId()).thenReturn(parentId);
        when(parent.getPath()).thenReturn("electronics");
        when(parent.getLevel()).thenReturn(0);
        when(parent.isActive()).thenReturn(true);

        when(slugGenerator.generate(any(), any(), any())).thenReturn("phones");
        when(categoryRepository.existsBySlug(anyString())).thenReturn(false);
        when(categoryRepository.findById(parentId)).thenReturn(Optional.of(parent));
        when(categoryRepository.existsByPath("electronics/phones")).thenReturn(false);

        Category savedCategory = mock(Category.class);
        when(categoryRepository.save(any(Category.class))).thenReturn(savedCategory);
        when(categoryMapper.toResponse(any())).thenReturn(mock(CategoryResponse.class));

        // When
        CategoryResponse response = categoryService.create(request);

        // Then
        assertThat(response).isNotNull();
        verify(categoryRepository).save(any(Category.class));
    }

    @Test
    @DisplayName("Should throw exception when category has children during delete")
    void shouldThrowWhenDeletingCategoryWithChildren() {
        // Given
        UUID categoryId = UUID.randomUUID();
        Category category = mock(Category.class);
        when(categoryRepository.findById(categoryId)).thenReturn(Optional.of(category));
        when(categoryRepository.findChildren(categoryId)).thenReturn(List.of(mock(Category.class)));

        // When & Then
        assertThatThrownBy(() -> categoryService.delete(categoryId))
                .isInstanceOf(BaseException.class)
                .hasFieldOrPropertyWithValue("errorCode", CatalogErrorCode.INVALID_CATEGORY_DATA);
    }
}
