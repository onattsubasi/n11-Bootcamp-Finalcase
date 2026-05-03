package com.onatsubasi.finalcase.inventory.infrastructure.config;

import jakarta.validation.constraints.Min;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@Getter
@Setter
@Validated
@ConfigurationProperties(prefix = "inventory.reservation")
public class InventoryReservationProperties {

    @Min(1)
    private long defaultTimeoutMinutes = 30;

    @Min(1)
    private int expirationBatchSize = 100;

    @Min(10_000)
    private long expirationFixedDelayMs = 60_000;

    private boolean expirationSchedulerEnabled = true;
}