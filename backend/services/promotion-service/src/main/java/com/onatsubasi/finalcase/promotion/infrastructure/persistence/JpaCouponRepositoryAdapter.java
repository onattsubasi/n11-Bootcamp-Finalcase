package com.onatsubasi.finalcase.promotion.infrastructure.persistence;

import com.onatsubasi.finalcase.promotion.domain.entity.Coupon;
import com.onatsubasi.finalcase.promotion.domain.repository.CouponRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
@RequiredArgsConstructor
public class JpaCouponRepositoryAdapter implements CouponRepository {

    private final SpringDataCouponJpaRepository springDataRepository;

    @Override
    public Coupon save(Coupon coupon) {
        return springDataRepository.save(coupon);
    }

    @Override
    public Optional<Coupon> findById(UUID couponId) {
        return springDataRepository.findById(couponId);
    }

    @Override
    public Optional<Coupon> findByIdForUpdate(UUID couponId) {
        return springDataRepository.findByIdForUpdate(couponId);
    }

    @Override
    public Optional<Coupon> findByCode(String code) {
        return springDataRepository.findByCode(code);
    }

    @Override
    public Optional<Coupon> findByCodeForUpdate(String code) {
        return springDataRepository.findByCodeForUpdate(code);
    }

    @Override
    public List<Coupon> findByPromotionId(UUID promotionId) {
        return springDataRepository.findByPromotionId(promotionId);
    }

    @Override
    public boolean existsByCode(String code) {
        return springDataRepository.existsByCode(code);
    }
}
