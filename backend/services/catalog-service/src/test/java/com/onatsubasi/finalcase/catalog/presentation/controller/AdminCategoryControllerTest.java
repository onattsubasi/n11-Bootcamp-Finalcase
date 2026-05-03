package com.onatsubasi.finalcase.catalog.presentation.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.onatsubasi.finalcase.catalog.application.dto.request.CreateCategoryRequest;
import com.onatsubasi.finalcase.catalog.application.dto.response.CategoryResponse;
import com.onatsubasi.finalcase.catalog.application.service.CategoryService;
import com.onatsubasi.finalcase.catalog.application.support.CatalogPageResponseMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AdminCategoryController.class)
@AutoConfigureMockMvc(addFilters = false)
class AdminCategoryControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private CategoryService categoryService;

    @MockitoBean
    private CatalogPageResponseMapper pageResponseMapper;

    @MockitoBean
    private com.onatsubasi.finalcase.common.security.filter.HeaderAuthenticationFilter headerAuthenticationFilter;

    @Test
    @DisplayName("Should create root category when authorized as admin")
    @WithMockUser(roles = "ADMIN")
    void shouldCreateRootCategory() throws Exception {
        // Given
        UUID categoryId = UUID.randomUUID();
        CreateCategoryRequest request = new CreateCategoryRequest(
                "Electronics", "electronics", "Electronics Description", null, 0
        );
        CategoryResponse response = new CategoryResponse(
                categoryId, "Electronics", "electronics", "Electronics Description", null, "electronics", 0, com.onatsubasi.finalcase.catalog.domain.enums.CatalogStatus.ACTIVE, true, 0, null, null
        );
        when(categoryService.create(any())).thenReturn(response);

        // When & Then
        mockMvc.perform(post("/api/admin/categories")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.id").value(categoryId.toString()))
                .andExpect(jsonPath("$.data.name").value("Electronics"));
    }
}
