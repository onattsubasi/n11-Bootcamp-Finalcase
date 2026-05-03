package com.onatsubasi.finalcase.catalog.presentation.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.onatsubasi.finalcase.catalog.application.dto.request.CreateBrandRequest;
import com.onatsubasi.finalcase.catalog.application.dto.request.UpdateBrandRequest;
import com.onatsubasi.finalcase.catalog.application.dto.response.BrandResponse;
import com.onatsubasi.finalcase.catalog.application.service.BrandService;
import com.onatsubasi.finalcase.catalog.application.support.CatalogPageResponseMapper;
import com.onatsubasi.finalcase.catalog.domain.enums.CatalogStatus;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AdminBrandController.class)
@AutoConfigureMockMvc(addFilters = false)
class AdminBrandControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private BrandService brandService;

    @MockitoBean
    private CatalogPageResponseMapper pageResponseMapper;

    @MockitoBean
    private com.onatsubasi.finalcase.common.security.filter.HeaderAuthenticationFilter headerAuthenticationFilter;

    @Test
    @DisplayName("Should create brand")
    void shouldCreateBrand() throws Exception {
        // Given
        CreateBrandRequest request = new CreateBrandRequest("Apple", "apple", "Technology company", null);
        BrandResponse response = new BrandResponse(UUID.randomUUID(), "Apple", "apple", "Technology company", null, CatalogStatus.ACTIVE, true, Instant.now(), Instant.now());

        when(brandService.create(any(CreateBrandRequest.class))).thenReturn(response);

        // When & Then
        mockMvc.perform(post("/api/admin/brands")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.name").value("Apple"));
    }

    @Test
    @DisplayName("Should update brand")
    void shouldUpdateBrand() throws Exception {
        // Given
        UUID brandId = UUID.randomUUID();
        UpdateBrandRequest request = new UpdateBrandRequest("Apple Inc", "apple-inc", "Global tech giant", null);
        BrandResponse response = new BrandResponse(brandId, "Apple Inc", "apple-inc", "Global tech giant", null, CatalogStatus.ACTIVE, true, Instant.now(), Instant.now());

        when(brandService.update(eq(brandId), any(UpdateBrandRequest.class))).thenReturn(response);

        // When & Then
        mockMvc.perform(put("/api/admin/brands/" + brandId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.name").value("Apple Inc"));
    }

    @Test
    @DisplayName("Should get brand by id")
    void shouldGetBrandById() throws Exception {
        // Given
        UUID brandId = UUID.randomUUID();
        BrandResponse response = new BrandResponse(brandId, "Apple", "apple", "Tech", null, CatalogStatus.ACTIVE, true, Instant.now(), Instant.now());

        when(brandService.getById(brandId)).thenReturn(response);

        // When & Then
        mockMvc.perform(get("/api/admin/brands/" + brandId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").value(brandId.toString()));
    }

    @Test
    @DisplayName("Should delete brand")
    void shouldDeleteBrand() throws Exception {
        // Given
        UUID brandId = UUID.randomUUID();

        // When & Then
        mockMvc.perform(delete("/api/admin/brands/" + brandId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Brand deleted successfully"));
    }
}
