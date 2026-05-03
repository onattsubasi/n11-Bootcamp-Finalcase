package com.onatsubasi.finalcase.notification.application.service;

import com.onatsubasi.finalcase.notification.domain.enums.ProcessedNotificationEventStatus;
import com.onatsubasi.finalcase.notification.domain.entity.NotificationProcessedEvent;
import com.onatsubasi.finalcase.notification.domain.repository.NotificationProcessedEventRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationEventProcessingService {

    private final NotificationProcessedEventRepository processedEventRepository;

    public boolean shouldProcess(String eventId) {
        if (eventId == null || eventId.isBlank()) {
            return true;
        }

        return processedEventRepository.findByEventId(eventId)
                .map(existing -> {
                    if (existing.getStatus() == ProcessedNotificationEventStatus.FAILED) {
                        log.info(
                                "event=notification.event_retry_after_failure eventId={} status={}",
                                eventId,
                                existing.getStatus()
                        );
                        return true;
                    }

                    log.info(
                            "event=notification.event_duplicate eventId={} status={}",
                            eventId,
                            existing.getStatus()
                    );
                    return false;
                })
                .orElse(true);
    }

    public void markProcessed(String eventId, String eventType) {
        mark(eventId, eventType, ProcessedNotificationEventStatus.PROCESSED, null);
    }

    public void markSkipped(String eventId, String eventType, String reason) {
        mark(eventId, eventType, ProcessedNotificationEventStatus.SKIPPED, reason);
    }

    public void markFailed(String eventId, String eventType, String errorMessage) {
        mark(eventId, eventType, ProcessedNotificationEventStatus.FAILED, errorMessage);
    }

    private void mark(
            String eventId,
            String eventType,
            ProcessedNotificationEventStatus status,
            String errorMessage
    ) {
        if (eventId == null || eventId.isBlank()) {
            return;
        }

        try {
            NotificationProcessedEvent event = processedEventRepository.findByEventId(eventId)
                    .map(existing -> {
                        existing.updateStatus(status, errorMessage);
                        return existing;
                    })
                    .orElseGet(() -> new NotificationProcessedEvent(
                            eventId,
                            eventType,
                            status,
                            errorMessage
                    ));

            processedEventRepository.save(event);
        } catch (DataIntegrityViolationException ex) {
            log.info(
                    "event=notification.event_mark_duplicate_race eventId={} status={}",
                    eventId,
                    status
            );
        }
    }
}
