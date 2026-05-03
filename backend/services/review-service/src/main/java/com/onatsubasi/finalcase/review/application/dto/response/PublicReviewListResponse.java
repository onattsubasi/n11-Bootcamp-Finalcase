package com.onatsubasi.finalcase.review.application.dto.response;

import java.util.List;

public record PublicReviewListResponse(
        ProductRatingSummaryResponse summary,
        List<PublicReviewResponse> items,
        int page,
        int size,
        long totalElements,
        int totalPages,
        boolean first,
        boolean last
) {
}
