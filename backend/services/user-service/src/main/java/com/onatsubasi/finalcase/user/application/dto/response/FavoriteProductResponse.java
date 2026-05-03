package com.onatsubasi.finalcase.user.application.dto.response;

import java.time.Instant;
import java.util.UUID;

public record FavoriteProductResponse(
        UUID id,
        UUID userId,
        UUID productId,
        Instant createdAt
) {
}
