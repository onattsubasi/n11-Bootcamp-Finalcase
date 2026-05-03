package com.onatsubasi.finalcase.search.application.event;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.List;
import java.util.UUID;

@JsonIgnoreProperties(ignoreUnknown = true)
public record PromotionProjectionChangedMessage(
        UUID promotionId,
        String promotionName,
        String type,
        String status,
        List<String> affectedProductIds,
        List<String> affectedCategoryIds,
        List<String> affectedBrandIds,
        String promotionBadge
) {
}