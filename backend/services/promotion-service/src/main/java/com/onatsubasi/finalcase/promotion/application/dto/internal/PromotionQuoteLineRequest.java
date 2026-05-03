package com.onatsubasi.finalcase.promotion.application.dto.internal;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.util.UUID;

@Schema(description = "Authoritative product line snapshot supplied by Checkout")
public record PromotionQuoteLineRequest(

        @NotNull(message = "productId is required")
        UUID productId,

        UUID categoryId,

        UUID brandId,

        @NotNull(message = "unitPrice is required")
        @DecimalMin(value = "0.00", inclusive = true)
        BigDecimal unitPrice,

        @Min(value = 1, message = "quantity must be greater than zero")
        int quantity
) {

    public BigDecimal subtotal() {
        return unitPrice.multiply(BigDecimal.valueOf(quantity));
    }
}
