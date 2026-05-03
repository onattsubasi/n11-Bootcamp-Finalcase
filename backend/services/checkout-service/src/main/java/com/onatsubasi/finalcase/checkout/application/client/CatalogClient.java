package com.onatsubasi.finalcase.checkout.application.client;

import com.onatsubasi.finalcase.checkout.application.dto.client.CatalogProductSnapshotClientResponse;
import com.onatsubasi.finalcase.checkout.application.dto.client.CatalogProductSnapshotsClientRequest;
import com.onatsubasi.finalcase.checkout.infrastructure.config.FeignConfig;
import com.onatsubasi.finalcase.common.core.response.ApiResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;

@FeignClient(
        name = "catalog-service",
        path = "/internal/catalog",
        configuration = FeignConfig.class
)
public interface CatalogClient {

    @PostMapping("/products/snapshots")
    ApiResponse<List<CatalogProductSnapshotClientResponse>> getProductSnapshots(
            @RequestBody CatalogProductSnapshotsClientRequest request
    );
}