package com.onatsubasi.finalcase.basket.infrastructure.persistence;

import com.onatsubasi.finalcase.basket.domain.enums.BasketStatus;
import com.onatsubasi.finalcase.basket.domain.entity.Basket;
import jakarta.persistence.LockModeType;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface SpringDataBasketJpaRepository extends JpaRepository<Basket, UUID> {

    @EntityGraph(attributePaths = "items")
    Optional<Basket> findByUserIdAndStatus(UUID userId, BasketStatus status);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
           select b
             from Basket b
            where b.userId = :userId
              and b.status = :status
           """)
    Optional<Basket> findByUserIdAndStatusForUpdate(
            @Param("userId") UUID userId,
            @Param("status") BasketStatus status
    );

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
           select b
             from Basket b
            where b.id = :basketId
           """)
    Optional<Basket> findByIdForUpdate(@Param("basketId") UUID basketId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
           select b
             from Basket b
            where b.status = :status
              and b.updatedAt < :cutoff
            order by b.updatedAt asc
           """)
    List<Basket> findOldBasketsForUpdate(
            @Param("status") BasketStatus status,
            @Param("cutoff") Instant cutoff,
            Pageable pageable
    );

    void deleteByUserId(UUID userId);
}