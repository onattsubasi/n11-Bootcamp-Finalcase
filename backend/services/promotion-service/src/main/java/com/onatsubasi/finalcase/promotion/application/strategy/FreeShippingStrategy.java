package com.onatsubasi.finalcase.promotion.application.strategy;

import com.onatsubasi.finalcase.common.core.exception.BaseException;
import com.onatsubasi.finalcase.promotion.domain.enums.PromotionType;
import com.onatsubasi.finalcase.promotion.domain.exception.PromotionErrorCode;
import com.onatsubasi.finalcase.promotion.domain.entity.Promotion;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.Map;

@Component
public class FreeShippingStrategy implements DiscountStrategy {

    @Override
    public PromotionType supports() {
        return PromotionType.FREE_SHIPPING;
    }

    @Override
    public void validateConfig(Promotion promotion) {
        RuleConfigReader.optionalBigDecimal(promotion.getRuleConfig(), "minimumOrderAmount");
    }

    @Override
    public DiscountCalculationResult calculate(Promotion promotion, DiscountCalculationContext context) {
        Map<String, Object> config = promotion.getRuleConfig();

        BigDecimal minimumOrderAmount = RuleConfigReader.optionalBigDecimal(config, "minimumOrderAmount");
        BigDecimal maxShippingDiscountAmount = RuleConfigReader.optionalBigDecimal(config, "maxShippingDiscountAmount");

        if (minimumOrderAmount != null && context.subtotal().compareTo(minimumOrderAmount) < 0) {
            throw new BaseException(PromotionErrorCode.MINIMUM_BASKET_AMOUNT_NOT_MET);
        }

        BigDecimal shippingFee = DiscountMath.money(context.shippingFee());
        BigDecimal shippingDiscount = maxShippingDiscountAmount == null
                ? shippingFee
                : shippingFee.min(maxShippingDiscountAmount);

        return new DiscountCalculationResult(
                promotion.getId(),
                promotion.getType(),
                promotion.getName(),
                DiscountMath.money(BigDecimal.ZERO),
                DiscountMath.money(shippingDiscount)
        );
    }
}