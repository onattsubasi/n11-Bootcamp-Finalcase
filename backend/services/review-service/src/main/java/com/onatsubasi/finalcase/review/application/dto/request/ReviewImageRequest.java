package com.onatsubasi.finalcase.review.application.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Schema(description = "Review image reference")
public record ReviewImageRequest(

        @NotBlank(message = "url is required")
        @Size(max = 1000)
        String url,

        Integer sortOrder
) {
}
