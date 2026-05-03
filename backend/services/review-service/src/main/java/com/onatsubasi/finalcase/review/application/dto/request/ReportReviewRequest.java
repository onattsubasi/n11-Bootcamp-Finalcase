package com.onatsubasi.finalcase.review.application.dto.request;

import com.onatsubasi.finalcase.review.domain.enums.ReviewReportReason;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

@Schema(description = "Request to report a review")
public record ReportReviewRequest(

        @NotNull(message = "reason is required")
        ReviewReportReason reason,

        @Size(max = 1000)
        String description
) {
}