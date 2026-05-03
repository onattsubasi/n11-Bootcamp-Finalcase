package com.onatsubasi.finalcase.promotion.infrastructure.persistence;

import com.onatsubasi.finalcase.promotion.domain.enums.PromotionStatus;
import com.onatsubasi.finalcase.promotion.domain.entity.Promotion;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface SpringDataPromotionJpaRepository extends JpaRepository<Promotion, UUID> {

    List<Promotion> findByStatus(PromotionStatus status);

    boolean existsByName(String name);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
           select p
             from Promotion p
            where p.id = :promotionId
           """)
    Optional<Promotion> findByIdForUpdate(@Param("promotionId") UUID promotionId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
           select p
             from Promotion p
            where p.id in :promotionIds
            order by p.id asc
           """)
    List<Promotion> findAllByIdsForUpdate(@Param("promotionIds") Collection<UUID> promotionIds);

    @Query("""
           select p
             from Promotion p
            where p.status = 'ACTIVE'
              and p.startsAt <= :now
              and (p.endsAt is null or p.endsAt >= :now)
            order by p.priority desc, p.createdAt asc
           """)
    List<Promotion> findActivePromotionsAt(@Param("now") Instant now);
}