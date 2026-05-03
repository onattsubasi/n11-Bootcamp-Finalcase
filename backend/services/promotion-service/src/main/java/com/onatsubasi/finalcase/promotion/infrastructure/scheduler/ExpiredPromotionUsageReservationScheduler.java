package com.onatsubasi.finalcase.promotion.infrastructure.scheduler;

import com.onatsubasi.finalcase.promotion.application.service.PromotionUsageReservationService;
import com.onatsubasi.finalcase.promotion.infrastructure.config.PromotionUsageReservationProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Instant;

@Component
@RequiredArgsConstructor
@Slf4j
@ConditionalOnProperty(
        prefix = "promotion.usage-reservation",
        name = "expiration-scheduler-enabled",
        havingValue = "true",
        matchIfMissing = true
)
public class ExpiredPromotionUsageReservationScheduler {

    private final PromotionUsageReservationService reservationService;
    private final PromotionUsageReservationProperties properties;

    @Scheduled(fixedDelayString = "${promotion.usage-reservation.expiration-fixed-delay-ms:60000}")
    public void expireOldReservations() {
        Instant now = Instant.now();

        try {
            MDC.put("eventName", "promotion.usage.expiration.started");

            log.info(
                    "Promotion usage reservation expiration started, now={}, batchSize={}",
                    now,
                    properties.getExpirationBatchSize()
            );

            int expiredCount = reservationService.expireReservations(
                    now,
                    properties.getExpirationBatchSize()
            );

            MDC.put("eventName", "promotion.usage.expiration.completed");
            log.info("Promotion usage reservation expiration completed, expiredCount={}", expiredCount);
        } catch (Exception ex) {
            MDC.put("eventName", "promotion.usage.expiration.failed");
            log.error("Promotion usage reservation expiration failed, now={}", now, ex);
        } finally {
            MDC.remove("eventName");
        }
    }
}
