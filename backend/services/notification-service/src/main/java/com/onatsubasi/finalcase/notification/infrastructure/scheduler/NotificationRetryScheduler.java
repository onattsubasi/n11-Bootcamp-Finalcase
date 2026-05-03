package com.onatsubasi.finalcase.notification.infrastructure.scheduler;

import com.onatsubasi.finalcase.notification.application.service.NotificationDeliveryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class NotificationRetryScheduler {

    private final NotificationDeliveryService deliveryService;

    @Scheduled(fixedDelayString = "${notification.retry-scheduler-delay-ms:60000}")
    public void retryDueDeliveries() {
        log.debug("event=notification.retry_scheduler_tick");

        deliveryService.retryDueDeliveries();
    }
}