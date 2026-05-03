package com.onatsubasi.finalcase.promotion.application.strategy;

import com.onatsubasi.finalcase.promotion.domain.enums.PromotionType;
import com.onatsubasi.finalcase.promotion.domain.entity.Promotion;

public interface DiscountStrategy {

    PromotionType supports();

    void validateConfig(Promotion promotion);

    DiscountCalculationResult calculate(
            Promotion promotion,
            DiscountCalculationContext context
    );
}