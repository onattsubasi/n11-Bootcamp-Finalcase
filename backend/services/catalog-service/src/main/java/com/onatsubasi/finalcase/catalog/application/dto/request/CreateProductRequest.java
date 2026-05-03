package com.onatsubasi.finalcase.catalog.application.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Schema(description = "Request to create a product in the catalog")
public record CreateProductRequest(

        @NotBlank
        @Size(max = 120)
        @Schema(
                description = "Product SKU. Must be unique.",
                example = "IPHONE-15-128-BLACK",
                requiredMode = Schema.RequiredMode.REQUIRED
        )
        String sku,

        @NotBlank
        @Size(max = 200)
        @Schema(
                description = "Product name",
                example = "iPhone 15 128GB Black",
                requiredMode = Schema.RequiredMode.REQUIRED
        )
        String name,

        @Size(max = 220)
        @Schema(
                description = "Optional product slug. If omitted, it is generated from name.",
                example = "iphone-15-128gb-black"
        )
        String slug,

        @Size(max = 5000)
        @Schema(
                description = "Product description",
                example = "Apple iPhone 15 with 128GB storage."
        )
        String description,

        @NotNull
        @DecimalMin(value = "0.01")
        @Schema(
                description = "Base price amount",
                example = "49999.90",
                requiredMode = Schema.RequiredMode.REQUIRED
        )
        BigDecimal priceAmount,

        @Size(min = 3, max = 3)
        @Schema(
                description = "ISO-4217 currency code. Defaults to TRY if omitted.",
                example = "TRY"
        )
        String currency,

        @NotNull
        @Schema(
                description = "Brand id",
                example = "3fa85f64-5717-4562-b3fc-2c963f66afa6",
                requiredMode = Schema.RequiredMode.REQUIRED
        )
        UUID brandId,

        @NotNull
        @Schema(
                description = "Category id",
                example = "4fa85f64-5717-4562-b3fc-2c963f66afa6",
                requiredMode = Schema.RequiredMode.REQUIRED
        )
        UUID categoryId,

        @Valid
        @Schema(description = "Product image list")
        List<ProductImageRequest> images,

        @Schema(
                description = "Flexible product attributes stored as JSONB",
                example = "{\"color\":\"Black\",\"storage\":\"128GB\"}"
        )
        Map<String, String> attributes,

        @Schema(
                description = "If true, product is activated immediately after creation.",
                example = "false"
        )
        Boolean publish
) {
}