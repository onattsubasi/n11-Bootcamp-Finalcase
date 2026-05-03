package com.onatsubasi.finalcase.order.web;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.onatsubasi.finalcase.order.application.dto.internal.OrderReviewEligibilityResponse;
import com.onatsubasi.finalcase.order.application.service.OrderCommandService;
import com.onatsubasi.finalcase.order.application.service.OrderQueryService;
import com.onatsubasi.finalcase.order.domain.enums.OrderStatus;
import com.onatsubasi.finalcase.order.presentation.controller.InternalOrderController;
import com.onatsubasi.finalcase.order.presentation.exception.GlobalExceptionHandler;
import com.onatsubasi.finalcase.order.support.OrderTestData;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(InternalOrderController.class)
@AutoConfigureMockMvc(addFilters = false)
@Import(GlobalExceptionHandler.class)
class InternalOrderControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private OrderCommandService orderCommandService;

    @MockitoBean
    private OrderQueryService orderQueryService;

    @Test
    void createOrderReturnsCreated() throws Exception {
        when(orderCommandService.createOrder(OrderTestData.createOrderRequest()))
                .thenReturn(OrderTestData.detailResponse(OrderStatus.PENDING_PAYMENT));

        mockMvc.perform(post("/internal/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(OrderTestData.createOrderRequest())))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.status").value("PENDING_PAYMENT"));
    }

    @Test
    void createOrderValidatesRequiredItems() throws Exception {
        String invalidPayload = """
                {
                  "checkoutId": "44444444-4444-4444-4444-444444444444",
                  "requestHash": "hash-1",
                  "userId": "11111111-1111-1111-1111-111111111111",
                  "subtotalAmount": 0,
                  "itemDiscountAmount": 0,
                  "promotionDiscountAmount": 0,
                  "shippingFee": 0,
                  "shippingDiscountAmount": 0,
                  "taxAmount": 0,
                  "grandTotalAmount": 0,
                  "currency": "TRY",
                  "items": []
                }
                """;

        mockMvc.perform(post("/internal/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidPayload))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    void markPaidDelegatesToCommandService() throws Exception {
        when(orderCommandService.markPaid(eq(OrderTestData.ORDER_ID), eq(OrderTestData.markPaidRequest())))
                .thenReturn(OrderTestData.detailResponse(OrderStatus.PAID));

        mockMvc.perform(post("/internal/orders/{orderId}/mark-paid", OrderTestData.ORDER_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(OrderTestData.markPaidRequest())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("PAID"));
    }

    @Test
    void verifyReviewEligibilityReturnsEligibilityPayload() throws Exception {
        OrderReviewEligibilityResponse eligibility = new OrderReviewEligibilityResponse(
                OrderTestData.ORDER_ID,
                OrderTestData.ORDER_ITEM_ID,
                "ORD-20260503-000001",
                OrderTestData.USER_ID,
                "product-1",
                true,
                null
        );

        when(orderQueryService.verifyReviewEligibility(
                OrderTestData.ORDER_ID,
                OrderTestData.ORDER_ITEM_ID,
                OrderTestData.USER_ID,
                "product-1"
        )).thenReturn(eligibility);

        mockMvc.perform(get("/internal/orders/{orderId}/items/{orderItemId}/review-eligibility",
                        OrderTestData.ORDER_ID,
                        OrderTestData.ORDER_ITEM_ID)
                        .param("userId", OrderTestData.USER_ID.toString())
                        .param("productId", "product-1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.eligible").value(true));
    }
}