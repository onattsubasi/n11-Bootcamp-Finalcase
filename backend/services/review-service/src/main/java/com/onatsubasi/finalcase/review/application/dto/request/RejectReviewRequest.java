package com.onatsubasi.finalcase.review.application.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Size;

@Schema(description = "Request to reject a review")
public record RejectReviewRequest(

        @Size(max = 500)
        String note
) {
}
