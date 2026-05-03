package com.onatsubasi.finalcase.review.application.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Size;

@Schema(description = "Request used by admin to moderate a review")
public record ModerateReviewRequest(
        @Size(max = 1000, message = "Moderation note cannot exceed 1000 characters")
        String note,

        @Size(max = 1000, message = "Moderation reason cannot exceed 1000 characters")
        String reason
) {
}