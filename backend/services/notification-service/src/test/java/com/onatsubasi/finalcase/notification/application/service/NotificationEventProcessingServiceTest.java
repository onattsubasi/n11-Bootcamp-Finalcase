package com.onatsubasi.finalcase.notification.application.service;

import com.onatsubasi.finalcase.notification.domain.enums.ProcessedNotificationEventStatus;
import com.onatsubasi.finalcase.notification.domain.entity.NotificationProcessedEvent;
import com.onatsubasi.finalcase.notification.domain.repository.NotificationProcessedEventRepository;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class NotificationEventProcessingServiceTest {

    private final NotificationProcessedEventRepository repository = mock(NotificationProcessedEventRepository.class);
    private final NotificationEventProcessingService service = new NotificationEventProcessingService(repository);

    @Test
    void skipsAlreadyProcessedEvent() {
        when(repository.findByEventId("evt-1"))
                .thenReturn(Optional.of(new NotificationProcessedEvent("evt-1", "order.paid", ProcessedNotificationEventStatus.PROCESSED, null)));

        assertThat(service.shouldProcess("evt-1")).isFalse();
    }

    @Test
    void allowsRetryForPreviouslyFailedEvent() {
        when(repository.findByEventId("evt-1"))
                .thenReturn(Optional.of(new NotificationProcessedEvent("evt-1", "order.paid", ProcessedNotificationEventStatus.FAILED, "boom")));

        assertThat(service.shouldProcess("evt-1")).isTrue();
    }

    @Test
    void updatesExistingFailedRecordWhenMarkedProcessed() {
        NotificationProcessedEvent existing = new NotificationProcessedEvent("evt-1", "order.paid", ProcessedNotificationEventStatus.FAILED, "boom");
        when(repository.findByEventId("evt-1")).thenReturn(Optional.of(existing));
        when(repository.save(any(NotificationProcessedEvent.class))).thenAnswer(invocation -> invocation.getArgument(0));

        service.markProcessed("evt-1", "order.paid");

        assertThat(existing.getStatus()).isEqualTo(ProcessedNotificationEventStatus.PROCESSED);
        assertThat(existing.getErrorMessage()).isNull();
        verify(repository).save(existing);
    }
}
