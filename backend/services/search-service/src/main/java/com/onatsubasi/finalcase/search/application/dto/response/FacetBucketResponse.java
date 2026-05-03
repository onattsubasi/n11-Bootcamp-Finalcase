package com.onatsubasi.finalcase.search.application.dto.response;

public record FacetBucketResponse(
        String value,
        String label,
        long count
) {
}
