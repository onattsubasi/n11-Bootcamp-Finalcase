package com.onatsubasi.finalcase.payment.presentation.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.onatsubasi.finalcase.payment.application.dto.request.IyzicoCheckoutFormCallbackRequest;
import com.onatsubasi.finalcase.payment.application.dto.response.PaymentDetailResponse;
import com.onatsubasi.finalcase.payment.application.service.PaymentCallbackService;
import com.onatsubasi.finalcase.payment.domain.enums.PaymentMethod;
import com.onatsubasi.finalcase.payment.domain.enums.PaymentProviderCode;
import com.onatsubasi.finalcase.payment.domain.enums.PaymentStatus;
import com.onatsubasi.finalcase.payment.presentation.exception.GlobalExceptionHandler;
import com.onatsubasi.finalcase.payment.support.PaymentTestData;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(IyzicoCallbackController.class)
@AutoConfigureMockMvc(addFilters = false)
@Import(GlobalExceptionHandler.class)
class IyzicoCallbackControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private PaymentCallbackService paymentCallbackService;

    @Test
    void callbackWorksOnCanonicalProviderPath() throws Exception {
        when(paymentCallbackService.handleIyzicoCheckoutFormCallback(any()))
                .thenReturn(detailResponse(PaymentStatus.SUCCEEDED));

        mockMvc.perform(post("/api/payments/providers/iyzico/checkout-form/callback")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                new IyzicoCheckoutFormCallbackRequest("token-1", "success", "conversation-1")
                        )))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.status").value("SUCCEEDED"));
    }

    @Test
    void callbackKeepsLegacyAliasToAvoidBreakingExistingProviderConfig() throws Exception {
        when(paymentCallbackService.handleIyzicoCheckoutFormCallback(any()))
                .thenReturn(detailResponse(PaymentStatus.SUCCEEDED));

        mockMvc.perform(post("/api/payments/iyzico/callback")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                new IyzicoCheckoutFormCallbackRequest("token-1", "success", null)
                        )))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("SUCCEEDED"));
    }

    @Test
    void callbackRejectsBlankToken() throws Exception {
        mockMvc.perform(post("/api/payments/providers/iyzico/checkout-form/callback")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                new IyzicoCheckoutFormCallbackRequest(" ", "success", null)
                        )))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false));
    }

    private PaymentDetailResponse detailResponse(PaymentStatus status) {
        return new PaymentDetailResponse(
                PaymentTestData.PAYMENT_ID,
                PaymentTestData.CHECKOUT_ID,
                PaymentTestData.ORDER_ID,
                "ORD-20260501-000001",
                PaymentTestData.USER_ID,
                PaymentProviderCode.IYZICO,
                PaymentMethod.CHECKOUT_FORM,
                status,
                new BigDecimal("100.00"),
                new BigDecimal("100.00"),
                BigDecimal.ZERO,
                "TRY",
                "provider-payment-1",
                "provider-tx-1",
                "conversation-1",
                "SUCCESS",
                null,
                List.of(),
                null,
                null,
                null
        );
    }
}