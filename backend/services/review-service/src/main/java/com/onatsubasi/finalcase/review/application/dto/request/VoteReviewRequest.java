package com.onatsubasi.finalcase.review.application.dto.request;

import com.onatsubasi.finalcase.review.domain.enums.ReviewVoteType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

@Schema(description = "Request to vote a review helpful or unhelpful")
public record VoteReviewRequest(

        @NotNull(message = "voteType is required")
        ReviewVoteType voteType
) {
}