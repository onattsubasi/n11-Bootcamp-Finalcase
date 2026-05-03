package com.onatsubasi.finalcase.promotion.application.strategy;

import com.onatsubasi.finalcase.common.core.exception.BaseException;
import com.onatsubasi.finalcase.promotion.domain.enums.PromotionType;
import com.onatsubasi.finalcase.promotion.domain.exception.PromotionErrorCode;
import com.onatsubasi.finalcase.promotion.domain.entity.Promotion;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.Map;

@Component
public class PercentageDiscountStrategy implements DiscountStrategy {

    @Override
    public PromotionType supports() {
        return PromotionType.PERCENTAGE_DISCOUNT;
    }

    @Override
    public void validateConfig(Promotion promotion) {
        Map<String, Object> config = promotion.getRuleConfig();
        RuleConfigReader.requiredBigDecimal(config, "discountPercentage");
    }

    @Override
    public DiscountCalculationResult calculate(Promotion promotion, DiscountCalculationContext context) {
        Map<String, Object> config = promotion.getRuleConfig();

        BigDecimal minimumOrderAmount = RuleConfigReader.optionalBigDecimal(config, "minimumOrderAmount");
        BigDecimal discountPercentage = RuleConfigReader.requiredBigDecimal(config, "discountPercentage");
        BigDecimal maxDiscountAmount = RuleConfigReader.optionalBigDecimal(config, "maxDiscountAmount");

        if (minimumOrderAmount != null && context.subtotal().compareTo(minimumOrderAmount) < 0) {
            throw new BaseException(PromotionErrorCode.MINIMUM_BASKET_AMOUNT_NOT_MET);
        }

        BigDecimal discount = DiscountMath.percentage(
                context.subtotal(),
                discountPercentage,
                maxDiscountAmount
        );

        return new DiscountCalculationResult(
                promotion.getId(),
                promotion.getType(),
                promotion.getName(),
                discount,
                DiscountMath.money(BigDecimal.ZERO)
        );
    }
}