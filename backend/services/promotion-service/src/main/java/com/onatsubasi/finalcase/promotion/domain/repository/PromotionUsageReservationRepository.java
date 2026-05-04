package com.onatsubasi.finalcase.promotion.domain.repository;

import com.onatsubasi.finalcase.promotion.domain.enums.PromotionUsageReservationStatus;
import com.onatsubasi.finalcase.promotion.domain.entity.PromotionUsageReservation;

import java.time.Instant;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface PromotionUsageReservationRepository {

    PromotionUsageReservation save(PromotionUsageReservation reservation);

    Optional<PromotionUsageReservation> findById(UUID reservationId);

    Optional<PromotionUsageReservation> findByIdForUpdate(UUID reservationId);

    Optional<PromotionUsageReservation> findByIdempotencyKey(String idempotencyKey);

    long countByUserIdAndPromotionIdAndStatuses(
            UUID userId,
            UUID promotionId,
            Collection<PromotionUsageReservationStatus> statuses
    );

    long countByUserIdAndCouponIdAndStatuses(
            UUID userId,
            UUID couponId,
            Collection<PromotionUsageReservationStatus> statuses
    );

    List<PromotionUsageReservation> findExpiredReservationsForUpdate(
            PromotionUsageReservationStatus status,
            Instant now,
            int batchSize
    );
}