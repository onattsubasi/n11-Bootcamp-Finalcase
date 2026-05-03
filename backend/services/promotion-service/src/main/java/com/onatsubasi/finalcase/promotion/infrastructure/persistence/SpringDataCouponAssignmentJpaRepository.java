package com.onatsubasi.finalcase.promotion.infrastructure.persistence;

import com.onatsubasi.finalcase.promotion.domain.enums.CouponAssignmentStatus;
import com.onatsubasi.finalcase.promotion.domain.entity.CouponAssignment;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface SpringDataCouponAssignmentJpaRepository extends JpaRepository<CouponAssignment, UUID> {

    @EntityGraph(attributePaths = {"coupon", "coupon.promotion"})
    Optional<CouponAssignment> findByCouponIdAndUserId(UUID couponId, UUID userId);

    @EntityGraph(attributePaths = {"coupon", "coupon.promotion"})
    List<CouponAssignment> findByUserIdAndStatus(UUID userId, CouponAssignmentStatus status);

    @EntityGraph(attributePaths = {"coupon", "coupon.promotion"})
    List<CouponAssignment> findByCouponId(UUID couponId);

    boolean existsByCouponIdAndUserId(UUID couponId, UUID userId);

    @EntityGraph(attributePaths = {"coupon", "coupon.promotion"})
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
           select a
             from CouponAssignment a
            where a.id = :assignmentId
           """)
    Optional<CouponAssignment> findByIdForUpdate(@Param("assignmentId") UUID assignmentId);

    @EntityGraph(attributePaths = {"coupon", "coupon.promotion"})
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
           select a
             from CouponAssignment a
            where a.coupon.id = :couponId
              and a.userId = :userId
           """)
    Optional<CouponAssignment> findByCouponIdAndUserIdForUpdate(
            @Param("couponId") UUID couponId,
            @Param("userId") UUID userId
    );
}