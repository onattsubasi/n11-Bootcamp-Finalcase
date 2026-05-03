package com.onatsubasi.finalcase.user.application.dto.response;

import com.onatsubasi.finalcase.user.domain.enums.ProductListVisibility;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record ProductListResponse(
        UUID id,
        UUID userId,
        String name,
        String description,
        ProductListVisibility visibility,
        List<ProductListItemResponse> items,
        Instant createdAt,
        Instant updatedAt
) {
}
