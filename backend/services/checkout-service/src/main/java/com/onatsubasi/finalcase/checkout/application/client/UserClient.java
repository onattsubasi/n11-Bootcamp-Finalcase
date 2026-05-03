package com.onatsubasi.finalcase.checkout.application.client;

import com.onatsubasi.finalcase.checkout.application.dto.client.UserAddressSnapshotClientResponse;
import com.onatsubasi.finalcase.checkout.infrastructure.config.FeignConfig;
import com.onatsubasi.finalcase.common.core.response.ApiResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.UUID;

@FeignClient(
        name = "user-service",
        path = "/internal/users",
        configuration = FeignConfig.class
)
public interface UserClient {

    @GetMapping("/{userId}/addresses/{addressId}/snapshot")
    ApiResponse<UserAddressSnapshotClientResponse> getAddressSnapshot(
            @PathVariable UUID userId,
            @PathVariable UUID addressId
    );
}