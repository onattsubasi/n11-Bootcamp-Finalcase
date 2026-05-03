package com.onatsubasi.finalcase.promotion.infrastructure.persistence;

import com.onatsubasi.finalcase.promotion.domain.enums.PromotionUsageReservationStatus;
import com.onatsubasi.finalcase.promotion.domain.entity.PromotionUsageReservation;
import jakarta.persistence.LockModeType;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface SpringDataPromotionUsageReservationJpaRepository
        extends JpaRepository<PromotionUsageReservation, UUID> {

    @EntityGraph(attributePaths = "items")
    Optional<PromotionUsageReservation> findById(UUID id);

    @EntityGraph(attributePaths = "items")
    Optional<PromotionUsageReservation> findByIdempotencyKey(String idempotencyKey);

    @EntityGraph(attributePaths = "items")
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
           select r
             from PromotionUsageReservation r
            where r.id = :reservationId
           """)
    Optional<PromotionUsageReservation> findByIdForUpdate(@Param("reservationId") UUID reservationId);

    @EntityGraph(attributePaths = "items")
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
           select r
             from PromotionUsageReservation r
            where r.status = :status
              and r.reservedUntil < :now
            order by r.reservedUntil asc
           """)
    List<PromotionUsageReservation> findExpiredReservationsForUpdate(
            @Param("status") PromotionUsageReservationStatus status,
            @Param("now") Instant now,
            Pageable pageable
    );

    @Query("""
           select count(distinct r)
             from PromotionUsageReservation r
             join r.items i
            where r.userId = :userId
              and i.promotionId = :promotionId
              and r.status in :statuses
           """)
    long countByUserIdAndPromotionIdAndStatuses(
            @Param("userId") UUID userId,
            @Param("promotionId") UUID promotionId,
            @Param("statuses") Collection<PromotionUsageReservationStatus> statuses
    );

    @Query("""
           select count(distinct r)
             from PromotionUsageReservation r
             join r.items i
            where r.userId = :userId
              and i.couponId = :couponId
              and r.status in :statuses
           """)
    long countByUserIdAndCouponIdAndStatuses(
            @Param("userId") UUID userId,
            @Param("couponId") UUID couponId,
            @Param("statuses") Collection<PromotionUsageReservationStatus> statuses
    );
}