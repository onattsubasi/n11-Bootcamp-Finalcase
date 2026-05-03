package com.onatsubasi.finalcase.catalog.application.service;

import com.onatsubasi.finalcase.catalog.application.dto.request.CreateBrandRequest;
import com.onatsubasi.finalcase.catalog.application.dto.response.BrandResponse;
import com.onatsubasi.finalcase.catalog.application.port.CatalogEventPublisher;
import com.onatsubasi.finalcase.catalog.application.support.SlugGenerator;
import com.onatsubasi.finalcase.catalog.domain.exception.CatalogErrorCode;
import com.onatsubasi.finalcase.catalog.domain.entity.Brand;
import com.onatsubasi.finalcase.catalog.domain.repository.BrandRepository;
import com.onatsubasi.finalcase.catalog.domain.repository.ProductRepository;
import com.onatsubasi.finalcase.catalog.infrastructure.mapper.BrandMapper;
import com.onatsubasi.finalcase.common.core.exception.BaseException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BrandServiceTest {

    @Mock
    private BrandRepository brandRepository;

    @Mock
    private ProductRepository productRepository;

    @Mock
    private BrandMapper brandMapper;

    @Mock
    private SlugGenerator slugGenerator;

    @Mock
    private CatalogEventPublisher eventPublisher;

    @InjectMocks
    private BrandService brandService;

    @Test
    @DisplayName("Should create brand successfully")
    void shouldCreateBrand() {
        // Given
        CreateBrandRequest request = new CreateBrandRequest("Apple", "apple", "Description", "http://logo.url");
        when(slugGenerator.generate(any(), any(), any())).thenReturn("apple");
        when(brandRepository.existsBySlug(anyString())).thenReturn(false);
        
        Brand savedBrand = mock(Brand.class);
        when(brandRepository.save(any(Brand.class))).thenReturn(savedBrand);
        when(brandMapper.toResponse(any())).thenReturn(mock(BrandResponse.class));

        // When
        BrandResponse response = brandService.create(request);

        // Then
        assertThat(response).isNotNull();
        verify(brandRepository).save(any(Brand.class));
        verify(eventPublisher).publishBrandCreated(any(Brand.class));
    }

    @Test
    @DisplayName("Should throw exception when brand slug exists")
    void shouldThrowWhenSlugExists() {
        // Given
        CreateBrandRequest request = new CreateBrandRequest("Apple", "apple", "Description", "http://logo.url");
        when(slugGenerator.generate(any(), any(), any())).thenReturn("apple");
        when(brandRepository.existsBySlug(anyString())).thenReturn(true);

        // When & Then
        assertThatThrownBy(() -> brandService.create(request))
                .isInstanceOf(BaseException.class)
                .hasFieldOrPropertyWithValue("errorCode", CatalogErrorCode.BRAND_SLUG_ALREADY_EXISTS);
    }
}
