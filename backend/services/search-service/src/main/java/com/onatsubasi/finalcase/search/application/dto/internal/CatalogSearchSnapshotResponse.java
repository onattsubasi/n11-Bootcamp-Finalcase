package com.onatsubasi.finalcase.search.application.dto.internal;

import com.onatsubasi.finalcase.search.application.dto.event.CatalogProductProjectionPayload;

import java.util.List;

public record CatalogSearchSnapshotResponse(
        List<CatalogProductProjectionPayload> items,
        int page,
        int size,
        long totalElements,
        int totalPages,
        boolean last
) {
}
