package com.onatsubasi.finalcase.catalog.domain.valueobject;

import com.onatsubasi.finalcase.catalog.domain.exception.CatalogErrorCode;
import com.onatsubasi.finalcase.common.core.exception.BaseException;
import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Currency;
import java.util.Locale;

@Getter
@Embeddable
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Money {

    @Column(name = "amount", nullable = false, precision = 19, scale = 2)
    private BigDecimal amount;

    @Column(name = "currency", nullable = false, length = 3)
    private String currency;

    private Money(BigDecimal amount, String currency) {
        validateAmount(amount);
        validateCurrency(currency);

        this.amount = amount.setScale(2, RoundingMode.HALF_UP);
        this.currency = currency.trim().toUpperCase(Locale.ROOT);
    }

    public static Money of(BigDecimal amount, String currency) {
        return new Money(amount, currency);
    }

    private void validateAmount(BigDecimal amount) {
        if (amount == null || amount.signum() <= 0) {
            throw new BaseException(
                    CatalogErrorCode.INVALID_PRICE,
                    "Money amount must be greater than zero"
            );
        }
    }

    private void validateCurrency(String currency) {
        if (currency == null || currency.isBlank()) {
            throw new BaseException(
                    CatalogErrorCode.INVALID_PRICE,
                    "Currency is required"
            );
        }

        String normalized = currency.trim().toUpperCase(Locale.ROOT);

        if (normalized.length() != 3) {
            throw new BaseException(
                    CatalogErrorCode.INVALID_PRICE,
                    "Currency must be ISO-4217 code"
            );
        }

        try {
            Currency.getInstance(normalized);
        } catch (IllegalArgumentException ex) {
            throw new BaseException(
                    CatalogErrorCode.INVALID_PRICE,
                    "Unsupported currency code"
            );
        }
    }
}