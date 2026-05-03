package com.onatsubasi.finalcase.search.application.dto.event;

import java.time.Instant;
import java.util.UUID;

public record CatalogBrandUpdatedPayload(
        UUID brandId,
        String brandName,
        Instant sourceUpdatedAt
) {
}
