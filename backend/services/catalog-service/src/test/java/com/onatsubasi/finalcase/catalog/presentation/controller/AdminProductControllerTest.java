package com.onatsubasi.finalcase.catalog.presentation.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.onatsubasi.finalcase.catalog.application.dto.request.CreateProductRequest;
import com.onatsubasi.finalcase.catalog.application.dto.response.ProductResponse;
import com.onatsubasi.finalcase.catalog.application.service.ProductService;
import com.onatsubasi.finalcase.catalog.application.support.CatalogPageResponseMapper;
import com.onatsubasi.finalcase.catalog.domain.enums.ProductStatus;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AdminProductController.class)
@AutoConfigureMockMvc(addFilters = false)
class AdminProductControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private ProductService productService;

    @MockitoBean
    private CatalogPageResponseMapper pageResponseMapper;

    @MockitoBean
    private com.onatsubasi.finalcase.common.security.filter.HeaderAuthenticationFilter headerAuthenticationFilter;

    @Test
    @DisplayName("Should create product when authorized as admin")
    @WithMockUser(roles = "ADMIN")
    void shouldCreateProduct() throws Exception {
        // Given
        UUID productId = UUID.randomUUID();
        CreateProductRequest request = new CreateProductRequest(
                "SKU-123", "Test Product", "test-product", "Desc", BigDecimal.valueOf(100), "TRY",
                UUID.randomUUID(), UUID.randomUUID(), List.of(), Map.of(), true
        );
        ProductResponse response = new ProductResponse(
                productId, "SKU-123", "Test Product", "test-product", "Desc", null, null, null, null, List.of(), Map.of(), ProductStatus.ACTIVE, true, null, null
        );
        when(productService.create(any())).thenReturn(response);

        // When & Then
        mockMvc.perform(post("/api/admin/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.id").value(productId.toString()))
                .andExpect(jsonPath("$.data.sku").value("SKU-123"));
    }

    @Test
    @DisplayName("Should get product by id")
    @WithMockUser(roles = "ADMIN")
    void shouldGetProductById() throws Exception {
        // Given
        UUID productId = UUID.randomUUID();
        ProductResponse response = new ProductResponse(
                productId, "SKU-123", "Test Product", "test-product", "Desc", null, null, null, null, List.of(), Map.of(), ProductStatus.ACTIVE, true, null, null
        );
        when(productService.getById(productId)).thenReturn(response);

        // When & Then
        mockMvc.perform(get("/api/admin/products/{productId}", productId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.id").value(productId.toString()));
    }
}
