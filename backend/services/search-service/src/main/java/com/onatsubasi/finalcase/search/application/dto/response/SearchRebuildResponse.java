package com.onatsubasi.finalcase.search.application.dto.response;

import java.time.Instant;

public record SearchRebuildResponse(
        int processedCount,
        int upsertedCount,
        int skippedCount,
        Instant startedAt,
        Instant finishedAt
) {
}
