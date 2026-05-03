package com.onatsubasi.finalcase.inventory.presentation.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.onatsubasi.finalcase.common.security.UserContextWebMvcConfig;
import com.onatsubasi.finalcase.common.security.filter.HeaderAuthenticationFilter;
import com.onatsubasi.finalcase.inventory.application.dto.request.CreateInventoryItemRequest;
import com.onatsubasi.finalcase.inventory.application.dto.response.InventoryItemResponse;
import com.onatsubasi.finalcase.inventory.application.service.InventoryAdminService;
import com.onatsubasi.finalcase.inventory.domain.enums.InventoryItemStatus;
import com.onatsubasi.finalcase.inventory.domain.enums.StockStatus;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AdminInventoryController.class)
@AutoConfigureMockMvc(addFilters = false)
@Import(UserContextWebMvcConfig.class)
class AdminInventoryControllerTest {

    @MockitoBean
    private HeaderAuthenticationFilter headerAuthenticationFilter;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private InventoryAdminService inventoryAdminService;

    @Test
    @DisplayName("Should create inventory item")
    void shouldCreateInventoryItem() throws Exception {
        // Given
        UUID productId = UUID.randomUUID();
        CreateInventoryItemRequest request = new CreateInventoryItemRequest(productId, 100, 10);
        
        InventoryItemResponse response = new InventoryItemResponse(
                UUID.randomUUID(),
                productId,
                100,
                0,
                100,
                10,
                InventoryItemStatus.ACTIVE,
                StockStatus.IN_STOCK,
                Instant.now(),
                Instant.now()
        );

        when(inventoryAdminService.createInventoryItem(any(), any(CreateInventoryItemRequest.class)))
                .thenReturn(response);

        // When & Then
        mockMvc.perform(post("/api/admin/inventory/items")
                        .header("X-User-Id", UUID.randomUUID().toString())
                        .header("X-User-Roles", "ADMIN")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.productId").value(productId.toString()))
                .andExpect(jsonPath("$.data.totalQuantity").value(100));
    }

    @Test
    @DisplayName("Should get inventory item by product id")
    void shouldGetByProductId() throws Exception {
        // Given
        UUID productId = UUID.randomUUID();
        InventoryItemResponse response = new InventoryItemResponse(
                UUID.randomUUID(),
                productId,
                100,
                0,
                100,
                10,
                InventoryItemStatus.ACTIVE,
                StockStatus.IN_STOCK,
                Instant.now(),
                Instant.now()
        );

        when(inventoryAdminService.getByProductId(productId)).thenReturn(response);

        // When & Then
        mockMvc.perform(get("/api/admin/inventory/products/" + productId)
                        .header("X-User-Id", UUID.randomUUID().toString())
                        .header("X-User-Roles", "ADMIN"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.productId").value(productId.toString()));
    }
}
