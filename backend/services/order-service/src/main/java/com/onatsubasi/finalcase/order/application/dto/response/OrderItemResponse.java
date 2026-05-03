package com.onatsubasi.finalcase.order.application.dto.response;

import java.math.BigDecimal;
import java.util.UUID;

public record OrderItemResponse(
        UUID id,
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