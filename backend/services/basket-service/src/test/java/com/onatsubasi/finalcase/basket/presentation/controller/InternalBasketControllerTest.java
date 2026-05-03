package com.onatsubasi.finalcase.basket.presentation.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.onatsubasi.finalcase.basket.application.dto.internal.MarkBasketCheckedOutRequest;
import com.onatsubasi.finalcase.basket.application.dto.internal.MarkBasketCheckedOutResponse;
import com.onatsubasi.finalcase.basket.application.dto.response.BasketResponse;
import com.onatsubasi.finalcase.basket.application.service.BasketService;
import com.onatsubasi.finalcase.basket.domain.enums.BasketStatus;
import com.onatsubasi.finalcase.common.security.filter.HeaderAuthenticationFilter;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(InternalBasketController.class)
@AutoConfigureMockMvc(addFilters = false)
class InternalBasketControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private BasketService basketService;

    @MockitoBean
    private HeaderAuthenticationFilter headerAuthenticationFilter;

    @Test
    @DisplayName("GET /internal/baskets/users/{userId}/active returns checkout basket snapshot")
    void shouldReturnActiveBasketSnapshot() throws Exception {
        UUID userId = UUID.randomUUID();
        BasketResponse response = new BasketResponse(
                UUID.randomUUID(), userId, BasketStatus.ACTIVE, null, List.of(), 1, 2, false, Instant.now()
        );
        when(basketService.getBasket(userId)).thenReturn(response);

        mockMvc.perform(get("/internal/baskets/users/{userId}/active", userId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.userId").value(userId.toString()))
                .andExpect(jsonPath("$.data.empty").value(false));
    }

    @Test
    @DisplayName("POST /internal/baskets/{basketId}/mark-checked-out marks basket lifecycle")
    void shouldMarkBasketCheckedOut() throws Exception {
        UUID basketId = UUID.randomUUID();
        UUID orderId = UUID.randomUUID();
        MarkBasketCheckedOutRequest request = new MarkBasketCheckedOutRequest(orderId);
        MarkBasketCheckedOutResponse response = new MarkBasketCheckedOutResponse(
                basketId, orderId, BasketStatus.CHECKED_OUT, Instant.now()
        );
        when(basketService.markBasketCheckedOut(eq(basketId), any(MarkBasketCheckedOutRequest.class)))
                .thenReturn(response);

        mockMvc.perform(post("/internal/baskets/{basketId}/mark-checked-out", basketId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.basketId").value(basketId.toString()))
                .andExpect(jsonPath("$.data.orderId").value(orderId.toString()))
                .andExpect(jsonPath("$.data.status").value("CHECKED_OUT"));
    }
}
