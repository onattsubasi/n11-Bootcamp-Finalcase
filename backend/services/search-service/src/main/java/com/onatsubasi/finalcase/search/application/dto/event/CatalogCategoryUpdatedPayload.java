package com.onatsubasi.finalcase.search.application.dto.event;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record CatalogCategoryUpdatedPayload(
        UUID categoryId,
        String categoryName,
        List<String> categoryPath,
        Instant sourceUpdatedAt
) {
}
