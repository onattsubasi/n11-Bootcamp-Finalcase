package com.onatsubasi.finalcase.checkout.application.client;

import com.onatsubasi.finalcase.checkout.application.dto.client.AddressSnapshotClientRequest;
import com.onatsubasi.finalcase.checkout.application.dto.client.AddressSnapshotsClientResponse;
import com.onatsubasi.finalcase.checkout.application.dto.client.UserAddressSnapshotClientResponse;
import com.onatsubasi.finalcase.checkout.infrastructure.config.FeignConfig;
import com.onatsubasi.finalcase.common.core.response.ApiResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.UUID;

@FeignClient(
        name = "user-service",
        path = "/internal/users",
        configuration = FeignConfig.class
)
public interface UserClient {

    @PostMapping("/address-snapshots")
    ApiResponse<AddressSnapshotsClientResponse> getAddressSnapshots(
            @RequestBody AddressSnapshotClientRequest request
    );
}
