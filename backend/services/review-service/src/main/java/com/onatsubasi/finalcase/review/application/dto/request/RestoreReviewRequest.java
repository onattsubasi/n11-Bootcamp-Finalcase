package com.onatsubasi.finalcase.review.application.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Size;

@Schema(description = "Request to restore a hidden review")
public record RestoreReviewRequest(

        @Size(max = 500)
        String note
) {
}
