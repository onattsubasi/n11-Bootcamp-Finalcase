package com.onatsubasi.finalcase.review.application.dto.response;

import com.onatsubasi.finalcase.review.domain.enums.ReviewVoteType;

import java.time.Instant;
import java.util.UUID;

public record ReviewVoteResponse(
        UUID voteId,
        UUID reviewId,
        UUID userId,
        ReviewVoteType voteType,
        int helpfulCount,
        int unhelpfulCount,
        Instant createdAt,
        Instant updatedAt
) {
}