package com.onatsubasi.finalcase.basket.infrastructure.config;

import jakarta.validation.constraints.Min;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@Getter
@Setter
@Validated
@ConfigurationProperties(prefix = "basket.cleanup")
public class BasketCleanupProperties {

    /**
     * Enables abandoned basket cleanup scheduler.
     */
    private boolean enabled = false;

    /**
     * Active baskets older than this value are marked ABANDONED.
     */
    @Min(1)
    private long abandonAfterHours = 720;

    /**
     * Maximum basket count processed in one scheduler run.
     */
    @Min(1)
    private int batchSize = 100;

    /**
     * Scheduler fixed delay in milliseconds.
     */
    @Min(10_000)
    private long fixedDelayMs = 3_600_000;
}