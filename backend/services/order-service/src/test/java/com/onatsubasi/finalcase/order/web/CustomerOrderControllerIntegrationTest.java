package com.onatsubasi.finalcase.order.web;

import com.onatsubasi.finalcase.common.security.UserContextWebMvcConfig;
import com.onatsubasi.finalcase.order.application.service.OrderCommandService;
import com.onatsubasi.finalcase.order.application.service.OrderQueryService;
import com.onatsubasi.finalcase.order.domain.enums.OrderStatus;
import com.onatsubasi.finalcase.order.presentation.controller.CustomerOrderController;
import com.onatsubasi.finalcase.order.presentation.exception.GlobalExceptionHandler;
import com.onatsubasi.finalcase.order.support.OrderTestData;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.PageImpl;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(CustomerOrderController.class)
@AutoConfigureMockMvc(addFilters = false)
@Import({UserContextWebMvcConfig.class, GlobalExceptionHandler.class})
class CustomerOrderControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private OrderQueryService orderQueryService;

    @MockitoBean
    private OrderCommandService orderCommandService;

    @Test
    void listMyOrdersUsesGatewayInjectedUserId() throws Exception {
        when(orderQueryService.getMyOrders(eq(OrderTestData.USER_ID), eq(0), eq(20)))
                .thenReturn(new PageImpl<>(List.of()));

        mockMvc.perform(get("/api/customer/orders")
                        .header("X-User-Id", OrderTestData.USER_ID.toString())
                        .header("X-User-Email", "user@example.com")
                        .header("X-User-Roles", "CUSTOMER"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    void getMyOrderReturnsDetail() throws Exception {
        when(orderQueryService.getByIdForCustomer(OrderTestData.ORDER_ID, OrderTestData.USER_ID))
                .thenReturn(OrderTestData.detailResponse(OrderStatus.PAID));

        mockMvc.perform(get("/api/customer/orders/{orderId}", OrderTestData.ORDER_ID)
                        .header("X-User-Id", OrderTestData.USER_ID.toString())
                        .header("X-User-Email", "user@example.com")
                        .header("X-User-Roles", "CUSTOMER"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.id").value(OrderTestData.ORDER_ID.toString()))
                .andExpect(jsonPath("$.data.status").value("PAID"));
    }

    @Test
    void cancelMyOrderAcceptsOptionalReason() throws Exception {
        when(orderCommandService.cancelByCustomer(
                eq(OrderTestData.ORDER_ID),
                eq(OrderTestData.USER_ID),
                org.mockito.ArgumentMatchers.any()
        )).thenReturn(OrderTestData.detailResponse(OrderStatus.CANCELLED));

        mockMvc.perform(post("/api/customer/orders/{orderId}/cancel", OrderTestData.ORDER_ID)
                        .header("X-User-Id", OrderTestData.USER_ID.toString())
                        .header("X-User-Email", "user@example.com")
                        .header("X-User-Roles", "CUSTOMER")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"reason\":\"changed my mind\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("CANCELLED"));
    }
}