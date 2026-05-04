package com.onatsubasi.finalcase.promotion.domain.repository;

import com.onatsubasi.finalcase.promotion.domain.enums.PromotionStatus;
import com.onatsubasi.finalcase.promotion.domain.entity.Promotion;

import java.time.Instant;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface PromotionRepository {

    Promotion save(Promotion promotion);

    Optional<Promotion> findById(UUID promotionId);

    Optional<Promotion> findByIdForUpdate(UUID promotionId);

    List<Promotion> findAllByIdsForUpdate(Collection<UUID> promotionIds);

    List<Promotion> findByStatus(PromotionStatus status);

    List<Promotion> findAll();

    List<Promotion> findActivePromotionsAt(Instant now);

    boolean existsByName(String name);
}