package com.onatsubasi.finalcase.order.application.service;

import com.onatsubasi.finalcase.common.core.exception.BaseException;
import com.onatsubasi.finalcase.order.domain.exception.OrderErrorCode;
import com.onatsubasi.finalcase.order.domain.repository.OrderRepository;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.util.UUID;

@Component
public class OrderNumberGenerator {

    private static final DateTimeFormatter FORMATTER =
            DateTimeFormatter.ofPattern("yyyyMMddHHmmss")
                    .withZone(ZoneOffset.UTC);

    private final OrderRepository orderRepository;

    public OrderNumberGenerator(OrderRepository orderRepository) {
        this.orderRepository = orderRepository;
    }

    public String generate() {
        for (int attempt = 0; attempt < 5; attempt++) {
            String candidate = "ORD-" +
                    FORMATTER.format(Instant.now()) +
                    "-" +
                    randomSuffix();

            if (!orderRepository.existsByOrderNumber(candidate)) {
                return candidate;
            }
        }

        throw new BaseException(OrderErrorCode.ORDER_NUMBER_GENERATION_FAILED);
    }

    private String randomSuffix() {
        return UUID.randomUUID()
                .toString()
                .replace("-", "")
                .substring(0, 8)
                .toUpperCase(Locale.ROOT);
    }
}