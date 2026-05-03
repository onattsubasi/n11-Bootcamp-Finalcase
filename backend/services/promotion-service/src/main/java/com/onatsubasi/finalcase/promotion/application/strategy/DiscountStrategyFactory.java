package com.onatsubasi.finalcase.promotion.application.strategy;

import com.onatsubasi.finalcase.common.core.exception.BaseException;
import com.onatsubasi.finalcase.promotion.domain.enums.PromotionType;
import com.onatsubasi.finalcase.promotion.domain.exception.PromotionErrorCode;
import org.springframework.stereotype.Component;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;

@Component
public class DiscountStrategyFactory {

    private final Map<PromotionType, DiscountStrategy> strategies;

    public DiscountStrategyFactory(List<DiscountStrategy> strategyList) {
        this.strategies = new EnumMap<>(PromotionType.class);

        for (DiscountStrategy strategy : strategyList) {
            this.strategies.put(strategy.supports(), strategy);
        }
    }

    public DiscountStrategy getStrategy(PromotionType type) {
        DiscountStrategy strategy = strategies.get(type);

        if (strategy == null) {
            throw new BaseException(
                    PromotionErrorCode.INVALID_PROMOTION_DATA,
                    "Unsupported promotion type: " + type
            );
        }

        return strategy;
    }
}