package com.onatsubasi.finalcase.basket.infrastructure.scheduler;

import com.onatsubasi.finalcase.basket.application.service.BasketService;
import com.onatsubasi.finalcase.basket.infrastructure.config.BasketCleanupProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.Instant;

@Component
@RequiredArgsConstructor
@Slf4j
@ConditionalOnProperty(
        prefix = "basket.cleanup",
        name = "enabled",
        havingValue = "true"
)
public class AbandonedBasketCleanupScheduler {

    private final BasketService basketService;
    private final BasketCleanupProperties cleanupProperties;

    @Scheduled(fixedDelayString = "${basket.cleanup.fixed-delay-ms:3600000}")
    public void abandonOldActiveBaskets() {
        Instant cutoff = Instant.now()
                .minus(Duration.ofHours(cleanupProperties.getAbandonAfterHours()));

        try {
            MDC.put("eventName", "basket.cleanup.started");

            log.info(
                    "Basket cleanup started, cutoff={}, batchSize={}",
                    cutoff,
                    cleanupProperties.getBatchSize()
            );

            int abandonedCount = basketService.abandonOldActiveBaskets(
                    cutoff,
                    cleanupProperties.getBatchSize()
            );

            MDC.put("eventName", "basket.cleanup.completed");
            log.info("Basket cleanup completed, abandonedCount={}", abandonedCount);
        } catch (Exception ex) {
            MDC.put("eventName", "basket.cleanup.failed");
            log.error("Basket cleanup failed, cutoff={}", cutoff, ex);
        } finally {
            MDC.remove("eventName");
        }
    }
}