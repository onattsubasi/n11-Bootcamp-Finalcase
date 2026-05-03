package com.onatsubasi.finalcase.checkout.application.dto.client;

import java.util.List;

public record CatalogProductSnapshotsClientRequest(
        List<String> productIds
) {
}
