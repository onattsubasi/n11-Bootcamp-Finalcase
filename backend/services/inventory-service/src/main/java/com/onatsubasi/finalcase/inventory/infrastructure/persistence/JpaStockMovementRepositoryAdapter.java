package com.onatsubasi.finalcase.inventory.infrastructure.persistence;

import com.onatsubasi.finalcase.inventory.domain.entity.StockMovement;
import com.onatsubasi.finalcase.inventory.domain.repository.StockMovementRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
@RequiredArgsConstructor
public class JpaStockMovementRepositoryAdapter implements StockMovementRepository {

    private final SpringDataStockMovementJpaRepository springDataRepository;

    @Override
    public StockMovement save(StockMovement movement) {
        return springDataRepository.save(movement);
    }

    @Override
    public List<StockMovement> findByProductId(UUID productId) {
        return springDataRepository.findByProductIdOrderByOccurredAtDesc(productId);
    }
}