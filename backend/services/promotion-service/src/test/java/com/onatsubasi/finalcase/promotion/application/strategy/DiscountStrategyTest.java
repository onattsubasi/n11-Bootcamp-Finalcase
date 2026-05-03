package com.onatsubasi.finalcase.promotion.application.strategy;

import com.onatsubasi.finalcase.common.core.exception.BaseException;
import com.onatsubasi.finalcase.promotion.TestDataFactory;
import com.onatsubasi.finalcase.promotion.application.dto.internal.PromotionQuoteLineRequest;
import com.onatsubasi.finalcase.promotion.domain.enums.PromotionType;
import com.onatsubasi.finalcase.promotion.domain.exception.PromotionErrorCode;
import com.onatsubasi.finalcase.promotion.domain.entity.Promotion;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class DiscountStrategyTest {

    @Test
    void percentageDiscountHonorsMaxDiscountAmount() {
        UUID promotionId = UUID.randomUUID();
        Promotion promotion = TestDataFactory.activePromotion(
                promotionId,
                PromotionType.PERCENTAGE_DISCOUNT,
                java.util.Map.of(
                        "discountPercentage", "20",
                        "maxDiscountAmount", "100"
                )
        );
        DiscountCalculationContext context = contextWithSubtotal("1000");

        DiscountCalculationResult result = new PercentageDiscountStrategy().calculate(promotion, context);

        assertThat(result.promotionId()).isEqualTo(promotionId);
        assertThat(result.discountAmount()).isEqualByComparingTo("100.00");
        assertThat(result.totalDiscountAmount()).isEqualByComparingTo("100.00");
    }

    @Test
    void fixedAmountDiscountRejectsBasketUnderMinimumAmount() {
        Promotion promotion = TestDataFactory.activePromotion(
                UUID.randomUUID(),
                PromotionType.FIXED_AMOUNT_DISCOUNT,
                java.util.Map.of(
                        "discountAmount", "150",
                        "minimumOrderAmount", "1000"
                )
        );
        DiscountCalculationContext context = contextWithSubtotal("500");

        assertThatThrownBy(() -> new FixedAmountDiscountStrategy().calculate(promotion, context))
                .isInstanceOf(BaseException.class)
                .extracting(ex -> ((BaseException) ex).getErrorCode())
                .isEqualTo(PromotionErrorCode.MINIMUM_BASKET_AMOUNT_NOT_MET);
    }

    @Test
    void categoryStrategyDiscountsOnlyMatchingCategorySubtotal() {
        UUID categoryId = UUID.randomUUID();
        Promotion promotion = TestDataFactory.activePromotion(
                UUID.randomUUID(),
                PromotionType.CATEGORY_PERCENTAGE_DISCOUNT,
                TestDataFactory.categoryPercentageConfig(categoryId, "10")
        );
        DiscountCalculationContext context = new DiscountCalculationContext(
                UUID.randomUUID(),
                List.of(
                        TestDataFactory.line(UUID.randomUUID(), categoryId, null, "1000", 1),
                        TestDataFactory.line(UUID.randomUUID(), UUID.randomUUID(), null, "1000", 1)
                ),
                new BigDecimal("2000.00"),
                BigDecimal.ZERO,
                "TRY"
        );

        DiscountCalculationResult result = new CategoryPercentageDiscountStrategy().calculate(promotion, context);

        assertThat(result.discountAmount()).isEqualByComparingTo("100.00");
    }

    private DiscountCalculationContext contextWithSubtotal(String subtotal) {
        PromotionQuoteLineRequest line = TestDataFactory.line(
                UUID.randomUUID(),
                UUID.randomUUID(),
                UUID.randomUUID(),
                subtotal,
                1
        );
        return new DiscountCalculationContext(
                UUID.randomUUID(),
                List.of(line),
                new BigDecimal(subtotal),
                BigDecimal.ZERO,
                "TRY"
        );
    }
}
