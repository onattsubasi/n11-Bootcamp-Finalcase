package com.onatsubasi.finalcase.payment.presentation.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.onatsubasi.finalcase.payment.application.dto.response.PaymentInitializeResponse;
import com.onatsubasi.finalcase.payment.application.service.PaymentCommandService;
import com.onatsubasi.finalcase.payment.application.service.PaymentQueryService;
import com.onatsubasi.finalcase.payment.domain.enums.PaymentAttemptStatus;
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

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(InternalPaymentController.class)
@AutoConfigureMockMvc(addFilters = false)
@Import(GlobalExceptionHandler.class)
class InternalPaymentControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private PaymentCommandService paymentCommandService;

    @MockitoBean
    private PaymentQueryService paymentQueryService;

    @Test
    void initializePaymentReturnsAcceptedAndDelegatesWithIdempotencyKey() throws Exception {
        PaymentInitializeResponse response = new PaymentInitializeResponse(
                PaymentTestData.PAYMENT_ID,
                PaymentTestData.PAYMENT_ID,
                PaymentTestData.ORDER_ID,
                PaymentTestData.CHECKOUT_ID,
                PaymentProviderCode.IYZICO,
                PaymentMethod.CHECKOUT_FORM,
                PaymentStatus.WAITING_PROVIDER_ACTION,
                PaymentAttemptStatus.WAITING_PROVIDER_ACTION,
                "token-1",
                "https://pay.example.com",
                null,
                new BigDecimal("100.00"),
                new BigDecimal("100.00"),
                "TRY"
        );

        when(paymentCommandService.initializePayment(any(), eq("payment-idem-1")))
                .thenReturn(response);

        mockMvc.perform(post("/internal/payments/initialize")
                        .header("Idempotency-Key", "payment-idem-1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(PaymentTestData.initializePaymentRequest())))
                .andExpect(status().isAccepted())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.providerToken").value("token-1"))
                .andExpect(jsonPath("$.data.status").value("WAITING_PROVIDER_ACTION"));

        verify(paymentCommandService).initializePayment(any(), eq("payment-idem-1"));
    }

    @Test
    void initializePaymentRejectsInvalidRequestBeforeServiceCall() throws Exception {
        String invalidPayload = """
                {
                  "checkoutId": null,
                  "orderId": null,
                  "orderNumber": "",
                  "userId": null,
                  "amount": 0,
                  "currency": "TR",
                  "provider": "IYZICO",
                  "method": "CHECKOUT_FORM",
                  "basketItems": []
                }
                """;

        mockMvc.perform(post("/internal/payments/initialize")
                        .header("Idempotency-Key", "payment-idem-1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidPayload))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false));
    }
}