package com.onatsubasi.finalcase.basket.infrastructure.mapper;

import com.onatsubasi.finalcase.basket.application.dto.response.BasketItemResponse;
import com.onatsubasi.finalcase.basket.application.dto.response.BasketResponse;
import com.onatsubasi.finalcase.basket.domain.entity.Basket;
import com.onatsubasi.finalcase.basket.domain.entity.BasketItem;
import org.springframework.stereotype.Component;

import java.util.Comparator;
import java.util.List;

@Component
public class BasketMapper {

    public BasketResponse toResponse(Basket basket) {
        List<BasketItemResponse> items = basket.getItems()
                .stream()
                .sorted(Comparator.comparing(BasketItem::getCreatedAt))
                .map(this::toItemResponse)
                .toList();

        return new BasketResponse(
                basket.getId(),
                basket.getUserId(),
                basket.getStatus(),
                basket.getCouponCodeIntent(),
                items,
                basket.itemCount(),
                basket.totalQuantity(),
                basket.isEmpty(),
                basket.getUpdatedAt()
        );
    }

    private BasketItemResponse toItemResponse(BasketItem item) {
        return new BasketItemResponse(
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