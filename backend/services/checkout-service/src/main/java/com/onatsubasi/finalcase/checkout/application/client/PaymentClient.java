package com.onatsubasi.finalcase.checkout.application.client;

import com.onatsubasi.finalcase.checkout.application.dto.client.PaymentInitializeClientRequest;
import com.onatsubasi.finalcase.checkout.application.dto.client.PaymentInitializeClientResponse;
import com.onatsubasi.finalcase.checkout.infrastructure.config.FeignConfig;
import com.onatsubasi.finalcase.common.core.response.ApiResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;

@FeignClient(
        name = "payment-service",
        path = "/internal/payments",
        configuration = FeignConfig.class
)
public interface PaymentClient {

    @PostMapping("/initialize")
    ApiResponse<PaymentInitializeClientResponse> initializePayment(
            @RequestHeader("Idempotency-Key") String idempotencyKey,
            @RequestBody PaymentInitializeClientRequest request
    );
}
