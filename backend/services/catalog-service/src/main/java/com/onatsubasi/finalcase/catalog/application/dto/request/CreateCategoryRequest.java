package com.onatsubasi.finalcase.catalog.application.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.util.UUID;

@Schema(description = "Request to create a new category")
public record CreateCategoryRequest(

        @NotBlank
        @Size(max = 120)
        @Schema(
                description = "Category name",
                example = "Phones",
                requiredMode = Schema.RequiredMode.REQUIRED
        )
        String name,

        @Size(max = 140)
        @Schema(
                description = "Optional category slug. If omitted, it is generated from the name.",
                example = "phones"
        )
        String slug,

        @Size(max = 1000)
        @Schema(
                description = "Optional category description",
                example = "Smartphones and mobile devices"
        )
        String description,

        @Schema(
                description = "Optional parent category id. If omitted, category is created as root.",
                example = "3fa85f64-5717-4562-b3fc-2c963f66afa6"
        )
        UUID parentId,

        @Schema(
                description = "Sort order within the same parent category",
                example = "10"
        )
        Integer sortOrder
) {
}