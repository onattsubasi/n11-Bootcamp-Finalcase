package com.onatsubasi.finalcase.promotion.infrastructure.persistence;

import com.onatsubasi.finalcase.promotion.domain.enums.PromotionStatus;
import com.onatsubasi.finalcase.promotion.domain.entity.Promotion;
import com.onatsubasi.finalcase.promotion.domain.repository.PromotionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
@RequiredArgsConstructor
public class JpaPromotionRepositoryAdapter implements PromotionRepository {

    private final SpringDataPromotionJpaRepository springDataRepository;

    @Override
    public Promotion save(Promotion promotion) {
        return springDataRepository.save(promotion);
    }

    @Override
    public Optional<Promotion> findById(UUID promotionId) {
        return springDataRepository.findById(promotionId);
    }

    @Override
    public Optional<Promotion> findByIdForUpdate(UUID promotionId) {
        return springDataRepository.findByIdForUpdate(promotionId);
    }

    @Override
    public List<Promotion> findAllByIdsForUpdate(Collection<UUID> promotionIds) {
        if (promotionIds == null || promotionIds.isEmpty()) {
            return List.of();
        }

        return springDataRepository.findAllByIdsForUpdate(promotionIds);
    }

    @Override
    public List<Promotion> findByStatus(PromotionStatus status) {
        return springDataRepository.findByStatus(status);
    }

    @Override
    public List<Promotion> findAll() {
        return springDataRepository.findAll();
    }

    @Override
    public List<Promotion> findActivePromotionsAt(Instant now) {
        return springDataRepository.findActivePromotionsAt(now);
    }

    @Override
    public boolean existsByName(String name) {
        return springDataRepository.existsByName(name);
    }
}
