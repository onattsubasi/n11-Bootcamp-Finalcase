package com.onatsubasi.finalcase.checkout.application.dto.client;

import java.util.List;
import java.util.UUID;

public record PromotionUsageReserveClientRequest(
        UUID orderId,
        UUID userId,
        List<PromotionUsageReserveItemClientRequest> appliedDiscounts
) {
}