package com.onatsubasi.finalcase.search.application.service;

import com.onatsubasi.finalcase.common.event.EventEnvelope;
import com.onatsubasi.finalcase.search.application.dto.event.InventoryStockProjectionPayload;
import com.onatsubasi.finalcase.search.domain.enums.StockStatus;
import com.onatsubasi.finalcase.search.domain.entity.ProcessedSearchEvent;
import com.onatsubasi.finalcase.search.domain.repository.ProcessedSearchEventRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SearchEventProcessingServiceTest {

    @Mock
    private ProcessedSearchEventRepository processedEventRepository;

    @InjectMocks
    private SearchEventProcessingService service;

    @Test
    void processOnceRunsProjectionAndStoresProcessedEvent() {
        UUID productId = UUID.randomUUID();
        EventEnvelope<InventoryStockProjectionPayload> envelope = new EventEnvelope<>(
                "evt-1",
                "inventory.stock.updated",
                1,
                "inventory-service",
                "corr-1",
                Instant.parse("2026-05-01T12:00:00Z"),
                new InventoryStockProjectionPayload(productId, 8, StockStatus.IN_STOCK, Instant.now()),
                Map.of()
        );
        when(processedEventRepository.existsByEventId("evt-1")).thenReturn(false);
        when(processedEventRepository.save(any(ProcessedSearchEvent.class))).thenAnswer(invocation -> invocation.getArgument(0));
        AtomicInteger calls = new AtomicInteger();

        boolean processed = service.processOnce(envelope, calls::incrementAndGet);

        ArgumentCaptor<ProcessedSearchEvent> captor = ArgumentCaptor.forClass(ProcessedSearchEvent.class);
        verify(processedEventRepository).save(captor.capture());
        assertThat(processed).isTrue();
        assertThat(calls).hasValue(1);
        assertThat(captor.getValue().getEventId()).isEqualTo("evt-1");
        assertThat(captor.getValue().getAggregateId()).isEqualTo(productId.toString());
    }

    @Test
    void processOnceSkipsDuplicateEventWithoutRunningProjection() {
        EventEnvelope<String> envelope = new EventEnvelope<>(
                "evt-duplicate",
                "catalog.product.updated",
                1,
                "catalog-service",
                null,
                Instant.now(),
                "payload",
                null
        );
        when(processedEventRepository.existsByEventId("evt-duplicate")).thenReturn(true);
        AtomicInteger calls = new AtomicInteger();

        boolean processed = service.processOnce(envelope, calls::incrementAndGet);

        assertThat(processed).isFalse();
        assertThat(calls).hasValue(0);
        verify(processedEventRepository, never()).save(any());
    }
}
