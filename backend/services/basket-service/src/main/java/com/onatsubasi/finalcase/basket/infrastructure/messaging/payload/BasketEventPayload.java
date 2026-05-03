package com.onatsubasi.finalcase.basket.infrastructure.messaging.payload;

import com.onatsubasi.finalcase.basket.domain.enums.BasketItemStatus;
import com.onatsubasi.finalcase.basket.domain.enums.BasketStatus;
import com.onatsubasi.finalcase.basket.domain.entity.Basket;
import com.onatsubasi.finalcase.basket.domain.entity.BasketItem;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;

public record BasketEventPayload(
        UUID basketId,
        UUID userId,
        BasketStatus status,
        String couponCodeIntent,
        UUID orderId,
        Instant checkedOutAt,
        int itemCount,
        int totalQuantity,
        List<ItemPayload> items,
        UUID affectedProductId,
        Instant createdAt,
        Instant updatedAt
) {

    public record ItemPayload(
            UUID basketItemId,
            UUID productId,
            int quantity,
            BasketItemStatus itemStatus,
            String staleReason,
            String productNameSnapshot,
            String imageUrlSnapshot,
            BigDecimal unitPriceSnapshot,
            String snapshotCurrency,
            Instant createdAt,
            Instant updatedAt
    ) {

        public static ItemPayload from(BasketItem item) {
            return new ItemPayload(
                    item.getId(),
                    item.getProductId(),
                    item.getQuantity(),
                    item.getItemStatus(),
                    item.getStaleReason(),
                    item.getProductNameSnapshot(),
                    item.getImageUrlSnapshot(),
                    item.getUnitPriceSnapshot(),
                    item.getSnapshotCurrency(),
                    item.getCreatedAt(),
                    item.getUpdatedAt()
            );
        }
    }

    public static BasketEventPayload from(Basket basket) {
        return from(basket, null);
    }

    public static BasketEventPayload from(Basket basket, UUID affectedProductId) {
        List<ItemPayload> itemPayloads = basket.getItems()
                .stream()
                .sorted(Comparator.comparing(BasketItem::getCreatedAt))
                .map(ItemPayload::from)
                .toList();

        return new BasketEventPayload(
                basket.getId(),
                basket.getUserId(),
                basket.getStatus(),
                basket.getCouponCodeIntent(),
                basket.getOrderId(),
                basket.getCheckedOutAt(),
                basket.itemCount(),
                basket.totalQuantity(),
                itemPayloads,
                affectedProductId,
                basket.getCreatedAt(),
                basket.getUpdatedAt()
        );
    }
}