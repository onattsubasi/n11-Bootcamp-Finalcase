package com.onatsubasi.finalcase.promotion;

import com.onatsubasi.finalcase.promotion.application.dto.internal.PromotionQuoteLineRequest;
import com.onatsubasi.finalcase.promotion.domain.enums.PromotionType;
import com.onatsubasi.finalcase.promotion.domain.entity.Coupon;
import com.onatsubasi.finalcase.promotion.domain.entity.CouponAssignment;
import com.onatsubasi.finalcase.promotion.domain.entity.Promotion;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public final class TestDataFactory {

    private TestDataFactory() {
    }

    public static Promotion activePromotion(UUID promotionId, PromotionType type, Map<String, Object> config) {
        Promotion promotion = Promotion.create(
                "Test " + type.name(),
                "Test promotion",
                type,
                false,
                false,
                10,
                config,
                10,
                3,
                Instant.now().minusSeconds(60),
                Instant.now().plusSeconds(3600)
        );
        ReflectionTestUtils.setField(promotion, "id", promotionId);
        promotion.activate();
        return promotion;
    }

    public static Promotion activeCouponPromotion(UUID promotionId, PromotionType type, Map<String, Object> config) {
        Promotion promotion = Promotion.create(
                "Coupon " + type.name(),
                "Coupon promotion",
                type,
                true,
                false,
                20,
                config,
                10,
                3,
                Instant.now().minusSeconds(60),
                Instant.now().plusSeconds(3600)
        );
        ReflectionTestUtils.setField(promotion, "id", promotionId);
        promotion.activate();
        return promotion;
    }

    public static Coupon activeCoupon(UUID couponId, String code, Promotion promotion) {
        Coupon coupon = Coupon.create(
                code,
                promotion,
                10,
                2,
                Instant.now().minusSeconds(60),
                Instant.now().plusSeconds(3600)
        );
        ReflectionTestUtils.setField(coupon, "id", couponId);
        return coupon;
    }

    public static CouponAssignment assignedCoupon(UUID assignmentId, Coupon coupon, UUID userId) {
        CouponAssignment assignment = CouponAssignment.assign(
                coupon,
                userId,
                Instant.now().plusSeconds(3600)
        );
        ReflectionTestUtils.setField(assignment, "id", assignmentId);
        return assignment;
    }

    public static Map<String, Object> percentageConfig(String percentage) {
        Map<String, Object> config = new HashMap<>();
        config.put("discountPercentage", percentage);
        return config;
    }

    public static Map<String, Object> fixedAmountConfig(String amount) {
        Map<String, Object> config = new HashMap<>();
        config.put("discountAmount", amount);
        return config;
    }

    public static Map<String, Object> categoryPercentageConfig(UUID categoryId, String percentage) {
        Map<String, Object> config = new HashMap<>();
        config.put("eligibleCategoryIds", List.of(categoryId.toString()));
        config.put("discountPercentage", percentage);
        return config;
    }

    public static PromotionQuoteLineRequest line(UUID productId, UUID categoryId, UUID brandId, String price, int quantity) {
        return new PromotionQuoteLineRequest(
                productId,
                categoryId,
                brandId,
                new BigDecimal(price),
                quantity
        );
    }
}
