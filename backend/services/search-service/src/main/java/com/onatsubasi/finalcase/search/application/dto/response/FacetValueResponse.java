package com.onatsubasi.finalcase.search.application.dto.response;

public record FacetValueResponse(
        String value,
        String label,
        long count
) {
}