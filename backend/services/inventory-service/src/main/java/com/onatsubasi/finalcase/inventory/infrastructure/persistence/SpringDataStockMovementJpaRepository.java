package com.onatsubasi.finalcase.inventory.infrastructure.persistence;

import com.onatsubasi.finalcase.inventory.domain.entity.StockMovement;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface SpringDataStockMovementJpaRepository extends JpaRepository<StockMovement, UUID> {

    List<StockMovement> findByProductIdOrderByOccurredAtDesc(UUID productId);
}