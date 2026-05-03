package com.onatsubasi.finalcase.user.application.dto.response;

import java.time.Instant;
import java.util.UUID;

public record ProductListItemResponse(
        UUID id,
        UUID productId,
        String note,
        Instant createdAt
) {
}
