package com.onatsubasi.finalcase.payment.application.dto.provider;

import lombok.Builder;

import java.math.BigDecimal;

@Builder
public record ProviderBasketItemInfo(
        String id,
        String name,
        String categoryName,
        String itemType,
        BigDecimal price
) {
}