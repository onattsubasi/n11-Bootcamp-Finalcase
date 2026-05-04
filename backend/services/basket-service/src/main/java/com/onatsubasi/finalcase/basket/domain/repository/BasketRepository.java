package com.onatsubasi.finalcase.basket.domain.repository;

import com.onatsubasi.finalcase.basket.domain.enums.BasketStatus;
import com.onatsubasi.finalcase.basket.domain.entity.Basket;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface BasketRepository {

    Basket save(Basket basket);

    Basket saveAndFlush(Basket basket);

    Optional<Basket> findById(UUID basketId);

    Optional<Basket> findByIdForUpdate(UUID basketId);

    Optional<Basket> findByUserIdAndStatus(UUID userId, BasketStatus status);

    Optional<Basket> findByUserIdAndStatusForUpdate(UUID userId, BasketStatus status);

    List<Basket> findOldActiveBasketsForUpdate(Instant cutoff, int batchSize);

    void deleteByUserId(UUID userId);
}