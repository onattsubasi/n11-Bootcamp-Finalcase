package com.onatsubasi.finalcase.search.application.dto.event;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record PromotionProjectionPayload(
        UUID productId,
        boolean hasActivePromotion,
        boolean hasDiscount,
        BigDecimal discountedPrice,
        String promotionBadge,
        Instant promotionUpdatedAt
) {
}
