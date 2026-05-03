package com.onatsubasi.finalcase.basket.domain.entity;

import com.onatsubasi.finalcase.basket.domain.enums.BasketStatus;
import com.onatsubasi.finalcase.basket.domain.exception.BasketErrorCode;
import com.onatsubasi.finalcase.common.core.exception.BaseException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class BasketDomainTest {

    @Test
    @DisplayName("addItem creates a new item, then increments the same product instead of duplicating it")
    void addItemCreatesAndIncrementsExistingProduct() {
        UUID userId = UUID.randomUUID();
        UUID productId = UUID.randomUUID();
        Basket basket = Basket.empty(userId);

        basket.addItem(productId, 2);
        basket.addItem(productId, 3);

        assertThat(basket.itemCount()).isEqualTo(1);
        assertThat(basket.totalQuantity()).isEqualTo(5);
        assertThat(basket.getItems().getFirst().getProductId()).isEqualTo(productId);
    }

    @Test
    @DisplayName("quantity above max is rejected before persistence")
    void quantityAboveMaxIsRejected() {
        Basket basket = Basket.empty(UUID.randomUUID());

        assertThatThrownBy(() -> basket.addItem(UUID.randomUUID(), BasketItem.MAX_QUANTITY + 1))
                .isInstanceOfSatisfying(BaseException.class,
                        ex -> assertThat(ex.getErrorCode()).isEqualTo(BasketErrorCode.BASKET_ITEM_QUANTITY_LIMIT_EXCEEDED));
    }

    @Test
    @DisplayName("clear removes items but keeps basket active for continued shopping")
    void clearKeepsBasketActive() {
        Basket basket = Basket.empty(UUID.randomUUID());
        basket.addItem(UUID.randomUUID(), 2);

        basket.clear();

        assertThat(basket.isEmpty()).isTrue();
        assertThat(basket.getStatus()).isEqualTo(BasketStatus.ACTIVE);
        assertThat(basket.getClearedAt()).isNotNull();
    }

    @Test
    @DisplayName("markCheckedOut is idempotent for the same order id")
    void markCheckedOutIsIdempotentForSameOrder() {
        Basket basket = Basket.empty(UUID.randomUUID());
        UUID orderId = UUID.randomUUID();
        basket.addItem(UUID.randomUUID(), 1);

        basket.markCheckedOut(orderId);
        basket.markCheckedOut(orderId);

        assertThat(basket.getStatus()).isEqualTo(BasketStatus.CHECKED_OUT);
        assertThat(basket.getOrderId()).isEqualTo(orderId);
        assertThat(basket.getCheckedOutAt()).isNotNull();
    }

    @Test
    @DisplayName("checked out basket rejects a different order id")
    void checkedOutBasketRejectsDifferentOrderId() {
        Basket basket = Basket.empty(UUID.randomUUID());
        basket.addItem(UUID.randomUUID(), 1);
        basket.markCheckedOut(UUID.randomUUID());

        assertThatThrownBy(() -> basket.markCheckedOut(UUID.randomUUID()))
                .isInstanceOfSatisfying(BaseException.class,
                        ex -> assertThat(ex.getErrorCode()).isEqualTo(BasketErrorCode.BASKET_ALREADY_CHECKED_OUT));
    }
}
