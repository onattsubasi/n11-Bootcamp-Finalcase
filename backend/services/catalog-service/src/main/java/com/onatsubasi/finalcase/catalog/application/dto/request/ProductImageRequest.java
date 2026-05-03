package com.onatsubasi.finalcase.catalog.application.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Schema(description = "Product image request")
public record ProductImageRequest(

        @NotBlank
        @Size(max = 1000)
        @Schema(
                description = "Product image URL",
                example = "https://cdn.example.com/products/iphone-15-main.jpg",
                requiredMode = Schema.RequiredMode.REQUIRED
        )
        String url,

        @Schema(
                description = "Image sort order",
                example = "0"
        )
        Integer sortOrder,

        @Schema(
                description = "Whether this image is the main image",
                example = "true"
        )
        Boolean main
) {
}