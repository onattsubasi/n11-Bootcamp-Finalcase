package com.onatsubasi.finalcase.search.domain.entity;

import com.onatsubasi.finalcase.common.core.exception.BaseException;
import com.onatsubasi.finalcase.search.domain.exception.SearchErrorCode;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.UUID;

@Getter
@Entity
@Table(
        name = "processed_search_events",
        indexes = {
                @Index(name = "idx_processed_search_events_event_id", columnList = "event_id", unique = true),
                @Index(name = "idx_processed_search_events_event_type", columnList = "event_type"),
                @Index(name = "idx_processed_search_events_aggregate_id", columnList = "aggregate_id"),
                @Index(name = "idx_processed_search_events_processed_at", columnList = "processed_at")
        },
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_processed_search_events_event_id",
                        columnNames = "event_id"
                )
        }
)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ProcessedSearchEvent {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(nullable = false, updatable = false)
    private UUID id;

    @Column(name = "event_id", nullable = false, unique = true, length = 120)
    private String eventId;

    @Column(name = "event_type", nullable = false, length = 150)
    private String eventType;

    @Column(name = "source_service", length = 100)
    private String sourceService;

    @Column(name = "aggregate_id", length = 120)
    private String aggregateId;

    @Column(name = "correlation_id", length = 120)
    private String correlationId;

    @Column(name = "occurred_at")
    private Instant occurredAt;

    @Column(name = "processed_at", nullable = false)
    private Instant processedAt;

    private ProcessedSearchEvent(
            String eventId,
            String eventType,
            String sourceService,
            String aggregateId,
            String correlationId,
            Instant occurredAt
    ) {
        validateRequired(eventId, "Event id is required");
        validateRequired(eventType, "Event type is required");

        this.eventId = eventId.trim();
        this.eventType = eventType.trim();
        this.sourceService = normalize(sourceService, 100);
        this.aggregateId = normalize(aggregateId, 120);
        this.correlationId = normalize(correlationId, 120);
        this.occurredAt = occurredAt;
        this.processedAt = Instant.now();
    }

    public static ProcessedSearchEvent create(
            String eventId,
            String eventType,
            String sourceService,
            String aggregateId,
            String correlationId,
            Instant occurredAt
    ) {
        return new ProcessedSearchEvent(
                eventId,
                eventType,
                sourceService,
                aggregateId,
                correlationId,
                occurredAt
        );
    }

    private void validateRequired(String value, String message) {
        if (value == null || value.isBlank()) {
            throw new BaseException(SearchErrorCode.INVALID_EVENT_DATA, message);
        }
    }

    private String normalize(String value, int maxLength) {
        if (value == null || value.isBlank()) {
            return null;
        }

        String normalized = value.trim();

        return normalized.length() > maxLength
                ? normalized.substring(0, maxLength)
                : normalized;
    }

    @PrePersist
    protected void prePersist() {
        if (processedAt == null) {
            processedAt = Instant.now();
        }
    }
}