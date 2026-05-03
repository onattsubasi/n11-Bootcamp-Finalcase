package com.onatsubasi.finalcase.promotion.application.dto.internal;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Schema(description = "Internal request to calculate promotion quote without mutating usage counters")
public record PromotionQuoteRequest(

        @NotNull(message = "userId is required")
        UUID userId,

        @Size(max = 80)
        String couponCode,

        @NotEmpty(message = "items cannot be empty")
        @Valid
        List<PromotionQuoteLineRequest> items,

        BigDecimal shippingFee,

        String currency
) {
}