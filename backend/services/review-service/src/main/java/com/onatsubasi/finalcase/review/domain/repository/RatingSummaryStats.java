package com.onatsubasi.finalcase.review.domain.repository;

public record RatingSummaryStats(
        long rating1Count,
        long rating2Count,
        long rating3Count,
        long rating4Count,
        long rating5Count
) {
}