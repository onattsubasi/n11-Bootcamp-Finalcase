package com.onatsubasi.finalcase.basket.presentation.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.onatsubasi.finalcase.basket.application.dto.request.AddBasketItemRequest;
import com.onatsubasi.finalcase.basket.application.dto.request.UpdateCouponIntentRequest;
import com.onatsubasi.finalcase.basket.application.dto.response.BasketResponse;
import com.onatsubasi.finalcase.basket.application.service.BasketService;
import com.onatsubasi.finalcase.basket.domain.enums.BasketStatus;
import com.onatsubasi.finalcase.common.security.UserContext;
import com.onatsubasi.finalcase.common.security.UserContextWebMvcConfig;
import com.onatsubasi.finalcase.common.security.filter.HeaderAuthenticationFilter;
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
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(BasketController.class)
@AutoConfigureMockMvc(addFilters = false)
@Import(UserContextWebMvcConfig.class)
class BasketControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private BasketService basketService;

    @MockitoBean
    private HeaderAuthenticationFilter headerAuthenticationFilter;

    @Test
    @DisplayName("GET /api/customer/basket passes gateway user context to service")
    void shouldGetActiveBasket() throws Exception {
        UUID userId = UUID.randomUUID();
        BasketResponse response = response(userId, 0, true, null);
        when(basketService.getBasket(any(UserContext.class))).thenReturn(response);

        mockMvc.perform(get("/api/customer/basket")
                        .header("X-User-Id", userId.toString())
                        .header("X-User-Email", "customer@example.com")
                        .header("X-User-Roles", "CUSTOMER"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.userId").value(userId.toString()))
                .andExpect(jsonPath("$.data.status").value("ACTIVE"));
    }

    @Test
    @DisplayName("POST /api/customer/basket/items adds product intent")
    void shouldAddItemToBasket() throws Exception {
        UUID userId = UUID.randomUUID();
        AddBasketItemRequest request = new AddBasketItemRequest(UUID.randomUUID(), 2);
        BasketResponse response = response(userId, 2, false, null);
        when(basketService.addItem(any(UserContext.class), any(AddBasketItemRequest.class))).thenReturn(response);

        mockMvc.perform(post("/api/customer/basket/items")
                        .header("X-User-Id", userId.toString())
                        .header("X-User-Roles", "CUSTOMER")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.totalQuantity").value(2));
    }

    @Test
    @DisplayName("DELETE /api/customer/basket is the canonical clear endpoint")
    void shouldClearBasket() throws Exception {
        UUID userId = UUID.randomUUID();

        mockMvc.perform(delete("/api/customer/basket")
                        .header("X-User-Id", userId.toString())
                        .header("X-User-Roles", "CUSTOMER"))
                .andExpect(status().isNoContent());

        verify(basketService).clearBasket(any(UserContext.class));
    }

    @Test
    @DisplayName("PUT /api/customer/basket/coupon-intent stores coupon code as intent only")
    void shouldUpdateCouponIntent() throws Exception {
        UUID userId = UUID.randomUUID();
        UpdateCouponIntentRequest request = new UpdateCouponIntentRequest(" welcome10 ");
        BasketResponse response = response(userId, 1, false, "WELCOME10");
        when(basketService.updateCouponIntent(any(UserContext.class), any(UpdateCouponIntentRequest.class)))
                .thenReturn(response);

        mockMvc.perform(put("/api/customer/basket/coupon-intent")
                        .header("X-User-Id", userId.toString())
                        .header("X-User-Roles", "CUSTOMER")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.couponCodeIntent").value("WELCOME10"));
    }

    private BasketResponse response(UUID userId, int totalQuantity, boolean empty, String couponCodeIntent) {
        return new BasketResponse(
                UUID.randomUUID(),
                userId,
                BasketStatus.ACTIVE,
                couponCodeIntent,
                List.of(),
                empty ? 0 : 1,
                totalQuantity,
                empty,
                Instant.now()
        );
    }
}
