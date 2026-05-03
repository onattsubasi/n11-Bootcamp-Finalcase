package com.onatsubasi.finalcase.checkout.presentation.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.onatsubasi.finalcase.checkout.application.dto.client.BasketSnapshotClientResponse;
import com.onatsubasi.finalcase.checkout.application.dto.client.CatalogProductSnapshotClientResponse;
import com.onatsubasi.finalcase.checkout.application.dto.client.PromotionQuoteClientResponse;
import com.onatsubasi.finalcase.checkout.application.dto.request.CheckoutQuoteRequest;
import com.onatsubasi.finalcase.checkout.application.dto.request.CheckoutSubmitRequest;
import com.onatsubasi.finalcase.checkout.application.dto.response.CheckoutQuoteResponse;
import com.onatsubasi.finalcase.checkout.application.dto.response.CheckoutSubmitResponse;
import com.onatsubasi.finalcase.checkout.application.service.CheckoutQueryService;
import com.onatsubasi.finalcase.checkout.application.service.CheckoutQuoteService;
import com.onatsubasi.finalcase.checkout.application.service.CheckoutSubmitService;
import com.onatsubasi.finalcase.checkout.infrastructure.mapper.CheckoutMapper;
import com.onatsubasi.finalcase.checkout.presentation.exception.GlobalExceptionHandler;
import com.onatsubasi.finalcase.checkout.support.CheckoutTestFixtures;
import com.onatsubasi.finalcase.common.core.http.PlatformHeaders;
import com.onatsubasi.finalcase.common.security.UserContext;
import com.onatsubasi.finalcase.common.security.UserContextWebMvcConfig;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(CustomerCheckoutController.class)
@AutoConfigureMockMvc(addFilters = false)
@Import({UserContextWebMvcConfig.class, GlobalExceptionHandler.class})
class CustomerCheckoutControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private CheckoutQuoteService checkoutQuoteService;

    @MockitoBean
    private CheckoutSubmitService checkoutSubmitService;

    @MockitoBean
    private CheckoutQueryService checkoutQueryService;

    @Test
    void quote_resolvesGatewayUserHeadersAndReturnsApiResponseEnvelope() throws Exception {
        UUID userId = UUID.randomUUID();
        UUID basketId = UUID.randomUUID();
        UUID addressId = UUID.randomUUID();
        String productId = UUID.randomUUID().toString();
        CheckoutQuoteRequest request = CheckoutTestFixtures.quoteRequest(basketId, addressId);
        CheckoutQuoteResponse quoteResponse = quoteResponse(userId, basketId, productId);

        when(checkoutQuoteService.quote(any(UserContext.class), any(CheckoutQuoteRequest.class)))
                .thenReturn(quoteResponse);

        mockMvc.perform(post("/api/customer/checkout/quote")
                        .header(PlatformHeaders.X_USER_ID, userId.toString())
                        .header(PlatformHeaders.X_USER_EMAIL, "customer@example.com")
                        .header(PlatformHeaders.X_USER_ROLES, "CUSTOMER")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.basketId").value(basketId.toString()))
                .andExpect(jsonPath("$.data.items[0].productId").value(productId));

        ArgumentCaptor<UserContext> userCaptor = ArgumentCaptor.forClass(UserContext.class);
        verify(checkoutQuoteService).quote(userCaptor.capture(), eq(request));
        assertThat(userCaptor.getValue().userId()).isEqualTo(userId);
        assertThat(userCaptor.getValue().isCustomer()).isTrue();
    }

    @Test
    void submit_requiresValidPaymentMethodPayloadBeforeCallingApplicationService() throws Exception {
        UUID userId = UUID.randomUUID();
        String invalidBody = """
                {
                  "basketId": "%s",
                  "shippingAddressId": "%s",
                  "paymentMethod": null
                }
                """.formatted(UUID.randomUUID(), UUID.randomUUID());

        mockMvc.perform(post("/api/customer/checkout/submit")
                        .header(PlatformHeaders.X_USER_ID, userId.toString())
                        .header(PlatformHeaders.X_USER_EMAIL, "customer@example.com")
                        .header(PlatformHeaders.X_USER_ROLES, "CUSTOMER")
                        .header("Idempotency-Key", UUID.randomUUID().toString())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidBody))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errorCode").exists());
    }

    @Test
    void submit_passesIdempotencyKeyToApplicationService() throws Exception {
        UUID userId = UUID.randomUUID();
        UUID basketId = UUID.randomUUID();
        UUID addressId = UUID.randomUUID();
        UUID checkoutId = UUID.randomUUID();
        UUID orderId = UUID.randomUUID();
        UUID paymentId = UUID.randomUUID();
        String idempotencyKey = UUID.randomUUID().toString();
        CheckoutSubmitRequest request = CheckoutTestFixtures.submitRequest(basketId, addressId);
        CheckoutSubmitResponse response = CheckoutTestFixtures.submitResponse(checkoutId, orderId, paymentId);

        when(checkoutSubmitService.submit(any(UserContext.class), any(CheckoutSubmitRequest.class), eq(idempotencyKey)))
                .thenReturn(response);

        mockMvc.perform(post("/api/customer/checkout/submit")
                        .header(PlatformHeaders.X_USER_ID, userId.toString())
                        .header(PlatformHeaders.X_USER_EMAIL, "customer@example.com")
                        .header(PlatformHeaders.X_USER_ROLES, "CUSTOMER")
                        .header("Idempotency-Key", idempotencyKey)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.checkoutSessionId").value(checkoutId.toString()))
                .andExpect(jsonPath("$.data.orderId").value(orderId.toString()));

        verify(checkoutSubmitService).submit(any(UserContext.class), eq(request), eq(idempotencyKey));
    }

    private static CheckoutQuoteResponse quoteResponse(UUID userId, UUID basketId, String productId) {
        CheckoutMapper mapper = new CheckoutMapper(new ObjectMapper());
        BasketSnapshotClientResponse basket = CheckoutTestFixtures.basket(userId, basketId, productId, "500.00", 1);
        List<CatalogProductSnapshotClientResponse> products = List.of(CheckoutTestFixtures.product(productId, "500.00", true));
        PromotionQuoteClientResponse promotion = CheckoutTestFixtures.noDiscountQuote("500.00", "49.90");
        return mapper.toQuoteResponse(basket, products, promotion, CheckoutTestFixtures.money("49.90"), java.math.BigDecimal.ZERO);
    }
}
