package com.onatsubasi.finalcase.checkout.application.dto.client;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.math.BigDecimal;

@JsonIgnoreProperties(ignoreUnknown = true)
public record BasketItemClientResponse(
        String productId,
        String sku,
        int quantity,
        @JsonAlias("unitPriceSnapshot")
        BigDecimal unitPrice,
        @JsonAlias("snapshotCurrency")
        String currency
) {
    public BigDecimal unitPrice() {
        return unitPrice == null ? BigDecimal.ZERO : unitPrice;
    }
}
