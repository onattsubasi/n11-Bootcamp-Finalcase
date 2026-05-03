package com.onatsubasi.finalcase.checkout.application.dto.response;

import java.math.BigDecimal;

public record CheckoutItemResponse(
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
