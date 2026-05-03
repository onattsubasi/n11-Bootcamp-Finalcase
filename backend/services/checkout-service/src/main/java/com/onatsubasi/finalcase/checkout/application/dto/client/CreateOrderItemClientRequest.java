package com.onatsubasi.finalcase.checkout.application.dto.client;

import java.math.BigDecimal;

public record CreateOrderItemClientRequest(
        String productId,
        String sku,
        String productName,
        String slug,
        String mainImageUrl,
        String brandId,
        String brandName,
        String categoryId,
        String categoryName,
        BigDecimal unitPrice,
        int quantity,
        BigDecimal lineSubtotal,
        BigDecimal lineDiscount,
        BigDecimal lineTotal,
        String currency
) {
}