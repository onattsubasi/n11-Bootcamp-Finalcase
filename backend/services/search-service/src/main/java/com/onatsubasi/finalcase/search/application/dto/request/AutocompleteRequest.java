package com.onatsubasi.finalcase.search.application.dto.request;

import jakarta.validation.constraints.NotBlank;

public record AutocompleteRequest(
        @NotBlank(message = "Query is required")
        String q,

        Integer limit
) {
    public int safeLimit() {
        if (limit == null || limit <= 0) {
            return 10;
        }

        return Math.min(limit, 20);
    }

    public String normalizedQuery() {
        return q.trim();
    }
}