package com.onatsubasi.finalcase.promotion.domain.repository;

import com.onatsubasi.finalcase.promotion.domain.entity.Coupon;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface CouponRepository {

    Coupon save(Coupon coupon);

    Optional<Coupon> findById(UUID couponId);

    Optional<Coupon> findByIdForUpdate(UUID couponId);

    Optional<Coupon> findByCode(String code);

    Optional<Coupon> findByCodeForUpdate(String code);

    List<Coupon> findByPromotionId(UUID promotionId);

    boolean existsByCode(String code);
}