package com.onatsubasi.finalcase.search.application.dto.event;

import java.time.Instant;
import java.util.UUID;

public record CatalogProductDeletedPayload(
        UUID productId,
        Instant sourceUpdatedAt
) {
}
