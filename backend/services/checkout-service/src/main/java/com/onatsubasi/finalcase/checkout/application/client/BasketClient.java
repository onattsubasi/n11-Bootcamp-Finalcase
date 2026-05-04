package com.onatsubasi.finalcase.checkout.application.client;

import com.onatsubasi.finalcase.checkout.application.dto.client.BasketSnapshotClientResponse;
import com.onatsubasi.finalcase.checkout.application.dto.client.MarkBasketCheckedOutClientRequest;
import com.onatsubasi.finalcase.checkout.infrastructure.config.FeignConfig;
import com.onatsubasi.finalcase.common.core.response.ApiResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.UUID;

@FeignClient(
        name = "basket-service",
        path = "/internal/baskets",
        configuration = FeignConfig.class
)
public interface BasketClient {

    @GetMapping("/{basketId}/snapshot")
    ApiResponse<BasketSnapshotClientResponse> getBasketSnapshot(
            @PathVariable UUID basketId,
            @RequestParam UUID userId
    );

    @PostMapping("/{basketId}/mark-checked-out")
    ApiResponse<Void> markBasketCheckedOut(
            @PathVariable UUID basketId,
            @RequestBody MarkBasketCheckedOutClientRequest request
    );
}
