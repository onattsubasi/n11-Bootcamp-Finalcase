package com.onatsubasi.finalcase.inventory.domain.repository;

import com.onatsubasi.finalcase.inventory.domain.entity.StockMovement;

import java.util.List;
import java.util.UUID;

public interface StockMovementRepository {

    StockMovement save(StockMovement movement);

    List<StockMovement> findByProductId(UUID productId);
}