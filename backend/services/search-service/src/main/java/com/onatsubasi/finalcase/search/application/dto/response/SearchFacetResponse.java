package com.onatsubasi.finalcase.search.application.dto.response;

import java.util.List;

public record SearchFacetResponse(
        List<FacetBucketResponse> brands,
        List<FacetBucketResponse> categories,
        List<FacetBucketResponse> stockStatuses,
        List<FacetBucketResponse> priceRanges
) {
}