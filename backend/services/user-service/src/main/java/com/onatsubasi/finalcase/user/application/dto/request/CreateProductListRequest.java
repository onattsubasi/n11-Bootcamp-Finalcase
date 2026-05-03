package com.onatsubasi.finalcase.user.application.dto.request;

import com.onatsubasi.finalcase.user.domain.enums.ProductListVisibility;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Schema(description = "Request to create a custom product list")
public record CreateProductListRequest(

        @NotBlank(message = "name is required")
        @Size(max = 120)
        String name,

        @Size(max = 500)
        String description,

        ProductListVisibility visibility
) {
}
