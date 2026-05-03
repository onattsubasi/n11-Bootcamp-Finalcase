package com.onatsubasi.finalcase.user.application.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

@Schema(description = "Request to add a product to favorites")
public record AddFavoriteProductRequest(

        @NotNull(message = "productId is required")
        UUID productId
) {
}
