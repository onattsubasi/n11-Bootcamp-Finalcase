package com.onatsubasi.finalcase.catalog.application.service;

import com.onatsubasi.finalcase.catalog.application.dto.request.CreateProductRequest;
import com.onatsubasi.finalcase.catalog.application.dto.response.ProductResponse;
import com.onatsubasi.finalcase.catalog.application.port.CatalogEventPublisher;
import com.onatsubasi.finalcase.catalog.application.support.SlugGenerator;
import com.onatsubasi.finalcase.catalog.domain.exception.CatalogErrorCode;
import com.onatsubasi.finalcase.catalog.domain.entity.Brand;
import com.onatsubasi.finalcase.catalog.domain.entity.Category;
import com.onatsubasi.finalcase.catalog.domain.entity.Product;
import com.onatsubasi.finalcase.catalog.domain.repository.ProductRepository;
import com.onatsubasi.finalcase.catalog.domain.valueobject.CategorySnapshot;
import com.onatsubasi.finalcase.catalog.infrastructure.config.CatalogProperties;
import com.onatsubasi.finalcase.catalog.infrastructure.mapper.ProductMapper;
import com.onatsubasi.finalcase.common.core.exception.BaseException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProductServiceTest {

    @Mock
    private ProductRepository productRepository;

    @Mock
    private BrandService brandService;

    @Mock
    private CategoryService categoryService;

    @Mock
    private ProductMapper productMapper;

    @Mock
    private CatalogEventPublisher eventPublisher;

    @Mock
    private SlugGenerator slugGenerator;

    @Mock
    private CatalogProperties catalogProperties;

    @Mock
    private Brand brand;

    @Mock
    private Category category;

    @Mock
    private CatalogProperties.PlatformStore platformStore;

    @InjectMocks
    private ProductService productService;

    @Test
    @DisplayName("Should create product successfully")
    void shouldCreateProduct() {
        // Given
        CreateProductRequest request = createProductRequest();
        
        when(slugGenerator.generate(any(), any(), any())).thenReturn("test-product-slug");
        when(productRepository.existsBySku(anyString())).thenReturn(false);
        when(productRepository.existsBySlug(anyString())).thenReturn(false);
        
        when(brandService.getActiveBrandOrThrow(any())).thenReturn(brand);
        when(brand.getId()).thenReturn(UUID.randomUUID());
        when(brand.getName()).thenReturn("Test Brand");
        when(brand.getSlug()).thenReturn("test-brand");

        when(categoryService.getActiveCategoryOrThrow(any())).thenReturn(category);
        when(categoryService.buildSnapshot(any())).thenReturn(mock(CategorySnapshot.class));
        
        when(catalogProperties.platformStore()).thenReturn(platformStore);
        when(platformStore.id()).thenReturn("platform-store");
        when(platformStore.name()).thenReturn("Platform Store");
        
        Product savedProduct = mock(Product.class);
        when(productRepository.save(any(Product.class))).thenReturn(savedProduct);
        when(productMapper.toResponse(any())).thenReturn(mock(ProductResponse.class));

        // When
        ProductResponse response = productService.create(request);

        // Then
        assertThat(response).isNotNull();
        verify(productRepository).save(any(Product.class));
        verify(eventPublisher).publishProductCreated(any(Product.class));
    }

    @Test
    @DisplayName("Should throw exception when SKU already exists")
    void shouldThrowWhenSkuExists() {
        // Given
        CreateProductRequest request = createProductRequest();
        when(slugGenerator.generate(any(), any(), any())).thenReturn("test-product-slug");
        when(productRepository.existsBySku(anyString())).thenReturn(true);

        // When & Then
        assertThatThrownBy(() -> productService.create(request))
                .isInstanceOf(BaseException.class)
                .hasFieldOrPropertyWithValue("errorCode", CatalogErrorCode.PRODUCT_SKU_ALREADY_EXISTS);
    }

    private CreateProductRequest createProductRequest() {
        return new CreateProductRequest(
                "SKU-123",
                "Test Product",
                "test-product",
                "Description",
                BigDecimal.valueOf(100),
                "TRY",
                UUID.randomUUID(),
                UUID.randomUUID(),
                List.of(),
                Map.of(),
                true
        );
    }
}
