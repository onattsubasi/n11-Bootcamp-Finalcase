package com.onatsubasi.finalcase.promotion.application.strategy;

import com.onatsubasi.finalcase.common.core.exception.BaseException;
import com.onatsubasi.finalcase.promotion.domain.enums.PromotionType;
import com.onatsubasi.finalcase.promotion.domain.exception.PromotionErrorCode;
import com.onatsubasi.finalcase.promotion.domain.entity.Promotion;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.Map;

@Component
public class FixedAmountDiscountStrategy implements DiscountStrategy {

    @Override
    public PromotionType supports() {
        return PromotionType.FIXED_AMOUNT_DISCOUNT;
    }

    @Override
    public void validateConfig(Promotion promotion) {
        RuleConfigReader.requiredBigDecimal(promotion.getRuleConfig(), "discountAmount");
    }

    @Override
    public DiscountCalculationResult calculate(Promotion promotion, DiscountCalculationContext context) {
        Map<String, Object> config = promotion.getRuleConfig();

        BigDecimal minimumOrderAmount = RuleConfigReader.optionalBigDecimal(config, "minimumOrderAmount");
        BigDecimal discountAmount = RuleConfigReader.requiredBigDecimal(config, "discountAmount");

        if (minimumOrderAmount != null && context.subtotal().compareTo(minimumOrderAmount) < 0) {
            throw new BaseException(PromotionErrorCode.MINIMUM_BASKET_AMOUNT_NOT_MET);
        }

        BigDecimal discount = discountAmount.min(context.subtotal());

        return new DiscountCalculationResult(
                promotion.getId(),
                promotion.getType(),
                promotion.getName(),
                DiscountMath.money(discount),
                DiscountMath.money(BigDecimal.ZERO)
        );
    }
}