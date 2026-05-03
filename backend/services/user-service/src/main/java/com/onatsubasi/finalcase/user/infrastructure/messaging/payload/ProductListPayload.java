package com.onatsubasi.finalcase.user.infrastructure.messaging.payload;

import com.onatsubasi.finalcase.user.domain.enums.ProductListVisibility;
import com.onatsubasi.finalcase.user.domain.entity.ProductList;
import com.onatsubasi.finalcase.user.domain.entity.ProductListItem;

import java.time.Instant;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;

public record ProductListPayload(
        UUID listId,
        UUID userId,
        String name,
        ProductListVisibility visibility,
        boolean deleted,
        List<ItemPayload> items,
        UUID affectedProductId,
        Instant createdAt,
        Instant updatedAt
) {

    public record ItemPayload(
            UUID itemId,
            UUID productId,
            Instant createdAt
    ) {

        public static ItemPayload from(ProductListItem item) {
            return new ItemPayload(
                    item.getId(),
                    item.getProductId(),
                    item.getCreatedAt()
            );
        }
    }

    public static ProductListPayload from(ProductList productList) {
        return from(productList, null);
    }

    public static ProductListPayload from(ProductList productList, UUID affectedProductId) {
        List<ItemPayload> items = productList.getItems()
                .stream()
                .sorted(Comparator.comparing(ProductListItem::getCreatedAt))
                .map(ItemPayload::from)
                .toList();

        return new ProductListPayload(
                productList.getId(),
                productList.getUserId(),
                productList.getName(),
                productList.getVisibility(),
                productList.isDeleted(),
                items,
                affectedProductId,
                productList.getCreatedAt(),
                productList.getUpdatedAt()
        );
    }
}
