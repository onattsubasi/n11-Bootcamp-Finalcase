package com.onatsubasi.finalcase.promotion.presentation.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.onatsubasi.finalcase.promotion.application.dto.response.PromotionQuoteResponse;
import com.onatsubasi.finalcase.promotion.application.dto.response.PromotionUsageReservationResponse;
import com.onatsubasi.finalcase.promotion.application.service.PromotionQuoteService;
import com.onatsubasi.finalcase.promotion.application.service.PromotionUsageReservationService;
import com.onatsubasi.finalcase.promotion.domain.enums.PromotionUsageReservationStatus;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = InternalPromotionController.class)
@AutoConfigureMockMvc(addFilters = false)
class InternalPromotionControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private PromotionQuoteService promotionQuoteService;

    @MockitoBean
    private PromotionUsageReservationService reservationService;

    @Test
    void quoteReturnsCalculatedPromotionQuote() throws Exception {
        UUID userId = UUID.randomUUID();
        when(promotionQuoteService.quote(any())).thenReturn(new PromotionQuoteResponse(
                userId,
                null,
                new BigDecimal("1000.00"),
                BigDecimal.ZERO.setScale(2),
                new BigDecimal("100.00"),
                BigDecimal.ZERO.setScale(2),
                new BigDecimal("900.00"),
                "TRY",
                List.of(),
                List.of(),
                List.of(),
                Instant.now()
        ));

        String body = """
                {
                  "userId": "%s",
                  "items": [
                    {
                      "productId": "%s",
                      "categoryId": "%s",
                      "brandId": "%s",
                      "unitPrice": 1000,
                      "quantity": 1
                    }
                  ],
                  "shippingFee": 0,
                  "currency": "TRY"
                }
                """.formatted(userId, UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID());

        mockMvc.perform(post("/internal/promotions/quote")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.userId").value(userId.toString()))
                .andExpect(jsonPath("$.data.payableAmount").value(900.00));
    }

    @Test
    void quoteRejectsEmptyItemListThroughBeanValidation() throws Exception {
        String body = """
                {
                  "userId": "%s",
                  "items": [],
                  "currency": "TRY"
                }
                """.formatted(UUID.randomUUID());

        mockMvc.perform(post("/internal/promotions/quote")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.errors", hasSize(1)));
    }

    @Test
    void reserveForwardsIdempotencyKeyToApplicationService() throws Exception {
        UUID reservationId = UUID.randomUUID();
        UUID checkoutId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        when(reservationService.reserve(eq("idem-123"), any())).thenReturn(new PromotionUsageReservationResponse(
                reservationId,
                "idem-123",
                checkoutId,
                userId,
                null,
                PromotionUsageReservationStatus.RESERVED,
                Instant.now().plusSeconds(1800),
                null,
                null,
                null,
                null,
                List.of(),
                Instant.now(),
                Instant.now()
        ));

        String body = """
                {
                  "checkoutId": "%s",
                  "userId": "%s",
                  "items": [
                    {
                      "productId": "%s",
                      "categoryId": "%s",
                      "brandId": "%s",
                      "unitPrice": 1000,
                      "quantity": 1
                    }
                  ],
                  "shippingFee": 0,
                  "currency": "TRY"
                }
                """.formatted(checkoutId, userId, UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID());

        mockMvc.perform(post("/internal/promotions/usage-reservations")
                        .header("Idempotency-Key", "idem-123")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.id").value(reservationId.toString()))
                .andExpect(jsonPath("$.data.status").value("RESERVED"));

        verify(reservationService).reserve(eq("idem-123"), any());
    }
}
