package com.onatsubasi.finalcase.promotion.infrastructure.persistence;

import com.onatsubasi.finalcase.promotion.domain.enums.CouponAssignmentStatus;
import com.onatsubasi.finalcase.promotion.domain.entity.CouponAssignment;
import com.onatsubasi.finalcase.promotion.domain.repository.CouponAssignmentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
@RequiredArgsConstructor
public class JpaCouponAssignmentRepositoryAdapter implements CouponAssignmentRepository {

    private final SpringDataCouponAssignmentJpaRepository springDataRepository;

    @Override
    public CouponAssignment save(CouponAssignment assignment) {
        return springDataRepository.save(assignment);
    }

    @Override
    public Optional<CouponAssignment> findById(UUID assignmentId) {
        return springDataRepository.findById(assignmentId);
    }

    @Override
    public Optional<CouponAssignment> findByIdForUpdate(UUID assignmentId) {
        return springDataRepository.findByIdForUpdate(assignmentId);
    }

    @Override
    public Optional<CouponAssignment> findByCouponIdAndUserId(UUID couponId, UUID userId) {
        return springDataRepository.findByCouponIdAndUserId(couponId, userId);
    }

    @Override
    public Optional<CouponAssignment> findByCouponIdAndUserIdForUpdate(UUID couponId, UUID userId) {
        return springDataRepository.findByCouponIdAndUserIdForUpdate(couponId, userId);
    }

    @Override
    public List<CouponAssignment> findByUserIdAndStatus(UUID userId, CouponAssignmentStatus status) {
        return springDataRepository.findByUserIdAndStatus(userId, status);
    }

    @Override
    public List<CouponAssignment> findByCouponId(UUID couponId) {
        return springDataRepository.findByCouponId(couponId);
    }

    @Override
    public boolean existsByCouponIdAndUserId(UUID couponId, UUID userId) {
        return springDataRepository.existsByCouponIdAndUserId(couponId, userId);
    }
}
