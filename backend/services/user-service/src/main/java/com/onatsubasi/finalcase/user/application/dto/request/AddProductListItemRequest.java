package com.onatsubasi.finalcase.user.application.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.UUID;

@Schema(description = "Request to add a product to a custom product list")
public record AddProductListItemRequest(

        @NotNull(message = "productId is required")
        UUID productId,

        @Size(max = 300)
        String note
) {
}
