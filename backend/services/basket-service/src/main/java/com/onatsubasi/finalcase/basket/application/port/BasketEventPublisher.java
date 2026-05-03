package com.onatsubasi.finalcase.basket.application.port;

import com.onatsubasi.finalcase.basket.domain.entity.Basket;

import java.util.UUID;

public interface BasketEventPublisher {

    void publishBasketCreated(Basket basket);

    void publishItemAdded(Basket basket, UUID productId);

    void publishItemQuantityUpdated(Basket basket, UUID productId);

    void publishItemRemoved(Basket basket, UUID productId);

    void publishBasketCleared(Basket basket);

    void publishCouponIntentUpdated(Basket basket);

    void publishCouponIntentCleared(Basket basket);

    void publishBasketCheckedOut(Basket basket);
}