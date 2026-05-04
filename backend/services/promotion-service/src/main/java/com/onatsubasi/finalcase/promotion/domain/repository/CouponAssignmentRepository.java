package com.onatsubasi.finalcase.promotion.domain.repository;

import com.onatsubasi.finalcase.promotion.domain.enums.CouponAssignmentStatus;
import com.onatsubasi.finalcase.promotion.domain.entity.CouponAssignment;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface CouponAssignmentRepository {

    CouponAssignment save(CouponAssignment assignment);

    Optional<CouponAssignment> findById(UUID assignmentId);

    Optional<CouponAssignment> findByIdForUpdate(UUID assignmentId);

    Optional<CouponAssignment> findByCouponIdAndUserId(UUID couponId, UUID userId);

    Optional<CouponAssignment> findByCouponIdAndUserIdForUpdate(UUID couponId, UUID userId);

    List<CouponAssignment> findByUserIdAndStatus(UUID userId, CouponAssignmentStatus status);

    List<CouponAssignment> findByCouponId(UUID couponId);

    boolean existsByCouponIdAndUserId(UUID couponId, UUID userId);
}