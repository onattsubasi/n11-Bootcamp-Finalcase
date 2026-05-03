package com.onatsubasi.finalcase.promotion.application.strategy;

import com.onatsubasi.finalcase.promotion.application.dto.internal.PromotionQuoteLineRequest;
import com.onatsubasi.finalcase.promotion.domain.enums.PromotionType;
import com.onatsubasi.finalcase.promotion.domain.entity.Promotion;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Component
public class BrandPercentageDiscountStrategy implements DiscountStrategy {

    @Override
    public PromotionType supports() {
        return PromotionType.BRAND_PERCENTAGE_DISCOUNT;
    }

    @Override
    public void validateConfig(Promotion promotion) {
        RuleConfigReader.requiredUuidList(promotion.getRuleConfig(), "eligibleBrandIds");
        RuleConfigReader.requiredBigDecimal(promotion.getRuleConfig(), "discountPercentage");
    }

    @Override
    public DiscountCalculationResult calculate(Promotion promotion, DiscountCalculationContext context) {
        List<UUID> brandIds = RuleConfigReader.requiredUuidList(
                promotion.getRuleConfig(),
                "eligibleBrandIds"
        );

        BigDecimal eligibleSubtotal = context.items()
                .stream()
                .filter(item -> item.brandId() != null && brandIds.contains(item.brandId()))
                .map(PromotionQuoteLineRequest::subtotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal discount = DiscountMath.percentage(
                eligibleSubtotal,
                RuleConfigReader.requiredBigDecimal(promotion.getRuleConfig(), "discountPercentage"),
                RuleConfigReader.optionalBigDecimal(promotion.getRuleConfig(), "maxDiscountAmount")
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