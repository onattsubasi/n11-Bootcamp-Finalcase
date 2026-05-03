package com.onatsubasi.finalcase.inventory.infrastructure.scheduler;

import com.onatsubasi.finalcase.inventory.application.service.InventoryReservationService;
import com.onatsubasi.finalcase.inventory.infrastructure.config.InventoryReservationProperties;
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
        prefix = "inventory.reservation",
        name = "expiration-scheduler-enabled",
        havingValue = "true",
        matchIfMissing = true
)
public class ExpiredReservationScheduler {

    private final InventoryReservationService reservationService;
    private final InventoryReservationProperties reservationProperties;

    @Scheduled(fixedDelayString = "${inventory.reservation.expiration-fixed-delay-ms:60000}")
    public void expireOldReservations() {
        Instant now = Instant.now();

        try {
            MDC.put("eventName", "inventory.reservation.expiration.started");

            log.info(
                    "Inventory reservation expiration started, now={}, batchSize={}",
                    now,
                    reservationProperties.getExpirationBatchSize()
            );

            int expiredCount = reservationService.expireReservations(
                    now,
                    reservationProperties.getExpirationBatchSize()
            );

            MDC.put("eventName", "inventory.reservation.expiration.completed");
            log.info("Inventory reservation expiration completed, expiredCount={}", expiredCount);
        } catch (Exception ex) {
            MDC.put("eventName", "inventory.reservation.expiration.failed");
            log.error("Inventory reservation expiration failed, now={}", now, ex);
        } finally {
            MDC.remove("eventName");
        }
    }
}