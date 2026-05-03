package com.onatsubasi.finalcase.promotion.presentation.controller;

import com.onatsubasi.finalcase.promotion.application.dto.response.CouponResponse;
import com.onatsubasi.finalcase.promotion.application.service.CouponAdminService;
import com.onatsubasi.finalcase.promotion.domain.enums.CouponStatus;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = AdminCouponController.class)
@AutoConfigureMockMvc(addFilters = false)
class AdminCouponControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private CouponAdminService couponAdminService;

    @Test
    void createCouponReturnsCreatedResponse() throws Exception {
        UUID promotionId = UUID.randomUUID();
        UUID couponId = UUID.randomUUID();
        when(couponAdminService.createCoupon(any())).thenReturn(response(couponId, promotionId, CouponStatus.ACTIVE));

        String body = """
                {
                  "promotionId": "%s",
                  "code": "WELCOME100",
                  "usageLimit": 100,
                  "perUserUsageLimit": 1
                }
                """.formatted(promotionId);

        mockMvc.perform(post("/api/admin/coupons")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").value(couponId.toString()))
                .andExpect(jsonPath("$.data.code").value("WELCOME100"));
    }

    @Test
    void updateCouponForwardsCouponIdAndPayload() throws Exception {
        UUID promotionId = UUID.randomUUID();
        UUID couponId = UUID.randomUUID();
        when(couponAdminService.updateCoupon(eq(couponId), any())).thenReturn(response(couponId, promotionId, CouponStatus.ACTIVE));

        String body = """
                {
                  "usageLimit": 50,
                  "perUserUsageLimit": 1
                }
                """;

        mockMvc.perform(put("/api/admin/coupons/{couponId}", couponId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.id").value(couponId.toString()));

        verify(couponAdminService).updateCoupon(eq(couponId), any());
    }

    @Test
    void deactivateCouponReturnsUpdatedStatus() throws Exception {
        UUID promotionId = UUID.randomUUID();
        UUID couponId = UUID.randomUUID();
        when(couponAdminService.deactivateCoupon(couponId)).thenReturn(response(couponId, promotionId, CouponStatus.INACTIVE));

        mockMvc.perform(post("/api/admin/coupons/{couponId}/deactivate", couponId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("INACTIVE"));
    }

    private CouponResponse response(UUID couponId, UUID promotionId, CouponStatus status) {
        return new CouponResponse(
                couponId,
                promotionId,
                "WELCOME100",
                status,
                100,
                1,
                0,
                0,
                Instant.now().minusSeconds(60),
                Instant.now().plusSeconds(3600),
                Instant.now(),
                Instant.now()
        );
    }
}
