package com.onatsubasi.finalcase.promotion.application.strategy;

import com.onatsubasi.finalcase.common.core.exception.BaseException;
import com.onatsubasi.finalcase.promotion.domain.exception.PromotionErrorCode;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public final class RuleConfigReader {

    private RuleConfigReader() {
    }

    public static BigDecimal requiredBigDecimal(Map<String, Object> config, String key) {
        BigDecimal value = optionalBigDecimal(config, key);

        if (value == null) {
            throw new BaseException(
                    PromotionErrorCode.INVALID_PROMOTION_RULE_CONFIG,
                    "Missing required rule config key: " + key
            );
        }

        return value;
    }

    public static BigDecimal optionalBigDecimal(Map<String, Object> config, String key) {
        Object value = config.get(key);

        if (value == null) {
            return null;
        }

        if (value instanceof Number number) {
            return new BigDecimal(number.toString());
        }

        if (value instanceof String text && !text.isBlank()) {
            return new BigDecimal(text.trim());
        }

        throw new BaseException(
                PromotionErrorCode.INVALID_PROMOTION_RULE_CONFIG,
                "Invalid decimal rule config key: " + key
        );
    }

    public static List<UUID> requiredUuidList(Map<String, Object> config, String key) {
        Object value = config.get(key);

        if (!(value instanceof List<?> list) || list.isEmpty()) {
            throw new BaseException(
                    PromotionErrorCode.INVALID_PROMOTION_RULE_CONFIG,
                    "Missing required UUID list rule config key: " + key
            );
        }

        return list.stream()
                .map(Object::toString)
                .map(UUID::fromString)
                .toList();
    }
}
