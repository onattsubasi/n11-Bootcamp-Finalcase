package com.onatsubasi.finalcase.common.event;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record EventEnvelope<T>(
        String eventId,
        String eventType,
        int version,
        String source,
        String correlationId,
        Instant occurredAt,
        T payload,
        Map<String, String> metadata
) {

    public EventEnvelope {
        eventId = eventId == null || eventId.isBlank()
                ? UUID.randomUUID().toString()
                : eventId;

        if (eventType == null || eventType.isBlank()) {
            throw new IllegalArgumentException("eventType must not be blank");
        }

        if (source == null || source.isBlank()) {
            throw new IllegalArgumentException("source must not be blank");
        }

        version = version <= 0 ? 1 : version;

        occurredAt = occurredAt == null
                ? Instant.now()
                : occurredAt;

        metadata = metadata == null || metadata.isEmpty()
                ? null
                : Map.copyOf(metadata);
    }

    public static <T> EventEnvelope<T> of(
            String eventType,
            String source,
            T payload
    ) {
        return new EventEnvelope<>(
                null,
                eventType,
                1,
                source,
                null,
                Instant.now(),
                payload,
                null
        );
    }

    public static <T> EventEnvelope<T> of(
            String eventType,
            String source,
            String correlationId,
            T payload
    ) {
        return new EventEnvelope<>(
                null,
                eventType,
                1,
                source,
                correlationId,
                Instant.now(),
                payload,
                null
        );
    }

    public static <T> EventEnvelope<T> of(
            String eventType,
            int version,
            String source,
            String correlationId,
            T payload,
            Map<String, String> metadata
    ) {
        return new EventEnvelope<>(
                null,
                eventType,
                version,
                source,
                correlationId,
                Instant.now(),
                payload,
                metadata
        );
    }
}