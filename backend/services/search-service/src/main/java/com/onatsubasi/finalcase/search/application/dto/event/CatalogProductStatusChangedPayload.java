package com.onatsubasi.finalcase.search.application.dto.event;

import com.onatsubasi.finalcase.search.domain.enums.ProductSearchStatus;

import java.time.Instant;
import java.util.UUID;

public record CatalogProductStatusChangedPayload(
        UUID productId,
        ProductSearchStatus status,
        boolean visible,
        Instant sourceUpdatedAt
) {
}
