package com.onatsubasi.finalcase.shipment.application.dto.client;

import java.math.BigDecimal;
import java.util.UUID;

public record OrderItemClientResponse(
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
