package com.onatsubasi.finalcase.promotion.application.strategy;

import java.math.BigDecimal;
import java.math.RoundingMode;

public final class DiscountMath {

    private static final BigDecimal ONE_HUNDRED = BigDecimal.valueOf(100);

    private DiscountMath() {
    }

    public static BigDecimal percentage(BigDecimal base, BigDecimal percentage, BigDecimal maxDiscount) {
        BigDecimal discount = base
                .multiply(percentage)
                .divide(ONE_HUNDRED, 2, RoundingMode.HALF_UP);

        if (maxDiscount != null && discount.compareTo(maxDiscount) > 0) {
            return money(maxDiscount);
        }

        return money(discount);
    }

    public static BigDecimal money(BigDecimal value) {
        if (value == null) {
            return BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
        }

        return value.max(BigDecimal.ZERO).setScale(2, RoundingMode.HALF_UP);
    }
}
