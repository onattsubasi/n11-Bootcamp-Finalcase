package com.onatsubasi.finalcase.catalog.presentation.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.onatsubasi.finalcase.catalog.application.dto.request.ProductSnapshotRequest;
import com.onatsubasi.finalcase.catalog.application.dto.response.ProductSnapshotResponse;
import com.onatsubasi.finalcase.catalog.application.service.ProductService;
import com.onatsubasi.finalcase.catalog.domain.enums.ProductStatus;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(InternalCatalogController.class)
@AutoConfigureMockMvc(addFilters = false)
class InternalCatalogControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private ProductService productService;

    @MockitoBean
    private com.onatsubasi.finalcase.common.security.filter.HeaderAuthenticationFilter headerAuthenticationFilter;

    @Test
    @DisplayName("Should get product snapshots")
    void shouldGetProductSnapshots() throws Exception {
        // Given
        UUID productId = UUID.randomUUID();
        ProductSnapshotRequest request = new ProductSnapshotRequest(List.of(productId));
        
        ProductSnapshotResponse response = new ProductSnapshotResponse(
                productId.toString(), "SKU", "Name", "slug", ProductStatus.ACTIVE, true,
                new ProductSnapshotResponse.MoneySnapshot(BigDecimal.valueOf(100), "TRY"),
                null, null, null, null
        );

        when(productService.getSnapshots(any(ProductSnapshotRequest.class))).thenReturn(List.of(response));

        // When & Then
        mockMvc.perform(post("/internal/catalog/products/snapshots")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data[0].productId").value(productId.toString()));
    }
    @Test
    @DisplayName("Should support canonical singular product snapshot endpoint")
    void shouldSupportSingularProductSnapshotEndpoint() throws Exception {
        UUID productId = UUID.randomUUID();
        ProductSnapshotRequest request = new ProductSnapshotRequest(List.of(productId));

        ProductSnapshotResponse response = new ProductSnapshotResponse(
                productId.toString(), "SKU", "Name", "slug", ProductStatus.ACTIVE, true,
                new ProductSnapshotResponse.MoneySnapshot(BigDecimal.valueOf(100), "TRY"),
                null, null, null, null
        );

        when(productService.getSnapshots(any(ProductSnapshotRequest.class))).thenReturn(List.of(response));

        mockMvc.perform(post("/internal/catalog/products/snapshot")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].productId").value(productId.toString()));
    }

}
