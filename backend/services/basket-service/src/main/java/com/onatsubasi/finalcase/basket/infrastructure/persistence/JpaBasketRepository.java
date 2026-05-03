package com.onatsubasi.finalcase.basket.infrastructure.persistence;

import com.onatsubasi.finalcase.basket.domain.enums.BasketStatus;
import com.onatsubasi.finalcase.basket.domain.entity.Basket;
import com.onatsubasi.finalcase.basket.domain.repository.BasketRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
@RequiredArgsConstructor
public class JpaBasketRepository implements BasketRepository {

    private final SpringDataBasketJpaRepository springDataRepository;

    @Override
    public Basket save(Basket basket) {
        return springDataRepository.save(basket);
    }

    @Override
    public Basket saveAndFlush(Basket basket) {
        return springDataRepository.saveAndFlush(basket);
    }

    @Override
    public Optional<Basket> findById(UUID basketId) {
        return springDataRepository.findById(basketId);
    }

    @Override
    public Optional<Basket> findByIdForUpdate(UUID basketId) {
        return springDataRepository.findByIdForUpdate(basketId);
    }

    @Override
    public Optional<Basket> findByUserIdAndStatus(UUID userId, BasketStatus status) {
        return springDataRepository.findByUserIdAndStatus(userId, status);
    }

    @Override
    public Optional<Basket> findByUserIdAndStatusForUpdate(UUID userId, BasketStatus status) {
        return springDataRepository.findByUserIdAndStatusForUpdate(userId, status);
    }

    @Override
    public List<Basket> findOldActiveBasketsForUpdate(Instant cutoff, int batchSize) {
        return springDataRepository.findOldBasketsForUpdate(
                BasketStatus.ACTIVE,
                cutoff,
                PageRequest.of(0, Math.max(batchSize, 1))
        );
    }

    @Override
    public void deleteByUserId(UUID userId) {
        springDataRepository.deleteByUserId(userId);
    }
}