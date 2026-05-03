package com.onatsubasi.finalcase.review.application.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Size;

@Schema(description = "Request to resolve or dismiss a review report")
public record ResolveReviewReportRequest(

        @Size(max = 1000)
        String note
) {
}