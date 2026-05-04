package com.onatsubasi.finalcase.checkout.application.client;

import com.onatsubasi.finalcase.checkout.application.dto.client.PromotionQuoteClientRequest;
import com.onatsubasi.finalcase.checkout.application.dto.client.PromotionQuoteClientResponse;
import com.onatsubasi.finalcase.checkout.application.dto.client.PromotionUsageReservationClientResponse;
import com.onatsubasi.finalcase.checkout.application.dto.client.PromotionUsageReserveClientRequest;
import com.onatsubasi.finalcase.checkout.application.dto.client.PromotionUsageCancelClientRequest;
import com.onatsubasi.finalcase.checkout.application.dto.client.PromotionUsageRedeemClientRequest;
import com.onatsubasi.finalcase.checkout.infrastructure.config.FeignConfig;
import com.onatsubasi.finalcase.common.core.response.ApiResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;

import java.util.UUID;

@FeignClient(
        name = "promotion-service",
        path = "/internal/promotions",
        configuration = FeignConfig.class
)
public interface PromotionClient {

    @PostMapping("/quote")
    ApiResponse<PromotionQuoteClientResponse> quote(
            @RequestBody PromotionQuoteClientRequest request
    );

    @PostMapping("/usage-reservations")
    ApiResponse<PromotionUsageReservationClientResponse> reserveUsage(
            @RequestHeader("Idempotency-Key") String idempotencyKey,
            @RequestBody PromotionUsageReserveClientRequest request
    );

    @PostMapping("/usage-reservations/{reservationId}/redeem")
    ApiResponse<PromotionUsageReservationClientResponse> redeemUsage(
            @PathVariable UUID reservationId,
            @RequestBody PromotionUsageRedeemClientRequest request
    );

    @PostMapping("/usage-reservations/{reservationId}/cancel")
    ApiResponse<PromotionUsageReservationClientResponse> cancelUsage(
            @PathVariable UUID reservationId,
            @RequestBody PromotionUsageCancelClientRequest request
    );
}
