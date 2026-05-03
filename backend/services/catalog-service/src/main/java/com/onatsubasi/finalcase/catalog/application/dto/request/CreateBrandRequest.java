package com.onatsubasi.finalcase.catalog.application.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Schema(description = "Request to create a new brand")
public record CreateBrandRequest(

        @NotBlank
        @Size(max = 120)
        @Schema(
                description = "Brand name",
                example = "Apple",
                requiredMode = Schema.RequiredMode.REQUIRED
        )
        String name,

        @Size(max = 140)
        @Schema(
                description = "Optional brand slug. If omitted, it is generated from the name.",
                example = "apple"
        )
        String slug,

        @Size(max = 1000)
        @Schema(
                description = "Optional brand description",
                example = "Consumer electronics brand"
        )
        String description,

        @Size(max = 1000)
        @Schema(
                description = "Optional brand logo URL",
                example = "https://cdn.example.com/brands/apple.png"
        )
        String logoUrl
) {
}