package com.onatsubasi.finalcase.inventory.presentation.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.onatsubasi.finalcase.common.security.UserContextWebMvcConfig;
import com.onatsubasi.finalcase.inventory.application.dto.internal.ConfirmReservationRequest;
import com.onatsubasi.finalcase.inventory.application.dto.internal.ReserveStockRequest;
import com.onatsubasi.finalcase.inventory.application.dto.response.ReservationStatusResponse;
import com.onatsubasi.finalcase.inventory.application.dto.response.StockReservationResponse;
import com.onatsubasi.finalcase.inventory.application.service.InventoryAdminService;
import com.onatsubasi.finalcase.inventory.application.service.InventoryReservationService;
import com.onatsubasi.finalcase.inventory.domain.enums.StockReservationStatus;
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
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(InternalInventoryController.class)
@AutoConfigureMockMvc(addFilters = false)
@Import(UserContextWebMvcConfig.class)
class InternalInventoryControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private InventoryReservationService inventoryReservationService;

    @MockitoBean
    private InventoryAdminService inventoryAdminService;

    @MockitoBean
    private com.onatsubasi.finalcase.common.security.filter.HeaderAuthenticationFilter headerAuthenticationFilter;

    @Test
    @DisplayName("Should reserve stock")
    void shouldReserveStock() throws Exception {
        // Given
        UUID productId = UUID.randomUUID();
        ReserveStockRequest request = new ReserveStockRequest(
                UUID.randomUUID(),
                UUID.randomUUID(),
                List.of(new com.onatsubasi.finalcase.inventory.application.dto.internal.ReserveStockItemRequest(productId, 1))
        );
        StockReservationResponse response = new StockReservationResponse(
                UUID.randomUUID(), "test-key", UUID.randomUUID(), UUID.randomUUID(), null, 
                StockReservationStatus.RESERVED, Instant.now(), null, null, null, List.of(), Instant.now(), Instant.now()
        );

        when(inventoryReservationService.reserveStock(any(), any())).thenReturn(response);

        // When & Then
        mockMvc.perform(post("/internal/inventory/reservations")
                        .header("Idempotency-Key", "test-key")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    @DisplayName("Should confirm reservation")
    void shouldConfirmReservation() throws Exception {
        // Given
        UUID reservationId = UUID.randomUUID();
        ConfirmReservationRequest request = new ConfirmReservationRequest(UUID.randomUUID());
        ReservationStatusResponse response = new ReservationStatusResponse(
                reservationId, UUID.randomUUID(), UUID.randomUUID(), StockReservationStatus.CONFIRMED,
                Instant.now(), Instant.now(), null, null
        );

        when(inventoryReservationService.confirmReservation(eq(reservationId), any())).thenReturn(response);

        // When & Then
        mockMvc.perform(post("/internal/inventory/reservations/{reservationId}/confirm", reservationId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("CONFIRMED"));
    }
}
