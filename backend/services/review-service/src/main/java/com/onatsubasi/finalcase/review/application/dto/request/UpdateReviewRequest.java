package com.onatsubasi.finalcase.review.application.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;

import java.util.List;

@Schema(description = "Request to update own review")
public record UpdateReviewRequest(

        @Min(value = 1, message = "rating must be between 1 and 5")
        @Max(value = 5, message = "rating must be between 1 and 5")
        int rating,

        @Size(max = 150)
        String title,

        @Size(max = 5000)
        String comment,

        @Valid
        @Size(max = 5, message = "review cannot contain more than 5 images")
        List<ReviewImageRequest> images
) {
}