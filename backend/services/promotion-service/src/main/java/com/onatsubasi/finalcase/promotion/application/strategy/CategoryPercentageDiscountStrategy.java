package com.onatsubasi.finalcase.promotion.application.strategy;

import com.onatsubasi.finalcase.promotion.application.dto.internal.PromotionQuoteLineRequest;
import com.onatsubasi.finalcase.promotion.domain.enums.PromotionType;
import com.onatsubasi.finalcase.promotion.domain.entity.Promotion;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Component
public class CategoryPercentageDiscountStrategy implements DiscountStrategy {

    @Override
    public PromotionType supports() {
        return PromotionType.CATEGORY_PERCENTAGE_DISCOUNT;
    }

    @Override
    public void validateConfig(Promotion promotion) {
        RuleConfigReader.requiredUuidList(promotion.getRuleConfig(), "eligibleCategoryIds");
        RuleConfigReader.requiredBigDecimal(promotion.getRuleConfig(), "discountPercentage");
    }

    @Override
    public DiscountCalculationResult calculate(Promotion promotion, DiscountCalculationContext context) {
        List<UUID> categoryIds = RuleConfigReader.requiredUuidList(
                promotion.getRuleConfig(),
                "eligibleCategoryIds"
        );

        BigDecimal eligibleSubtotal = context.items()
                .stream()
                .filter(item -> item.categoryId() != null && categoryIds.contains(item.categoryId()))
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