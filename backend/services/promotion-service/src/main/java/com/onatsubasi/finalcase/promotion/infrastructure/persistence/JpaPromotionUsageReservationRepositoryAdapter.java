package com.onatsubasi.finalcase.promotion.infrastructure.persistence;

import com.onatsubasi.finalcase.promotion.domain.enums.PromotionUsageReservationStatus;
import com.onatsubasi.finalcase.promotion.domain.entity.PromotionUsageReservation;
import com.onatsubasi.finalcase.promotion.domain.repository.PromotionUsageReservationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
@RequiredArgsConstructor
public class JpaPromotionUsageReservationRepositoryAdapter implements PromotionUsageReservationRepository {

    private final SpringDataPromotionUsageReservationJpaRepository springDataRepository;

    @Override
    public PromotionUsageReservation save(PromotionUsageReservation reservation) {
        return springDataRepository.save(reservation);
    }

    @Override
    public Optional<PromotionUsageReservation> findById(UUID reservationId) {
        return springDataRepository.findById(reservationId);
    }

    @Override
    public Optional<PromotionUsageReservation> findByIdForUpdate(UUID reservationId) {
        return springDataRepository.findByIdForUpdate(reservationId);
    }

    @Override
    public Optional<PromotionUsageReservation> findByIdempotencyKey(String idempotencyKey) {
        return springDataRepository.findByIdempotencyKey(idempotencyKey);
    }

    @Override
    public long countByUserIdAndPromotionIdAndStatuses(
            UUID userId,
            UUID promotionId,
            Collection<PromotionUsageReservationStatus> statuses
    ) {
        return springDataRepository.countByUserIdAndPromotionIdAndStatuses(
                userId,
                promotionId,
                statuses
        );
    }

    @Override
    public long countByUserIdAndCouponIdAndStatuses(
            UUID userId,
            UUID couponId,
            Collection<PromotionUsageReservationStatus> statuses
    ) {
        return springDataRepository.countByUserIdAndCouponIdAndStatuses(
                userId,
                couponId,
                statuses
        );
    }

    @Override
    public List<PromotionUsageReservation> findExpiredReservationsForUpdate(
            PromotionUsageReservationStatus status,
            Instant now,
            int batchSize
    ) {
        return springDataRepository.findExpiredReservationsForUpdate(
                status,
                now,
                PageRequest.of(0, Math.max(batchSize, 1))
        );
    }
}
