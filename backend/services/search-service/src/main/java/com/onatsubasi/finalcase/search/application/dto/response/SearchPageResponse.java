package com.onatsubasi.finalcase.search.application.dto.response;

import java.util.List;

public record SearchPageResponse<T>(
        List<T> items,
        int page,
        int size,
        long totalElements,
        int totalPages,
        boolean first,
        boolean last
) {
}
