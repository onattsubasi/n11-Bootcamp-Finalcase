package com.onatsubasi.finalcase.inventory.domain.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.UUID;

@Getter
@Entity
@Table(
        name = "inventory_processed_events",
        indexes = {
                @Index(name = "idx_inventory_processed_events_event_type", columnList = "event_type"),
                @Index(name = "idx_inventory_processed_events_processed_at", columnList = "processed_at")
        },
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_inventory_processed_events_event_id",
                        columnNames = "event_id"
                )
        }
)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class InventoryProcessedEvent {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(nullable = false, updatable = false)
    private UUID id;

    @Column(name = "event_id", nullable = false, updatable = false, length = 120)
    private String eventId;

    @Column(name = "event_type", nullable = false, updatable = false, length = 120)
    private String eventType;

    @Column(name = "processed_at", nullable = false, updatable = false)
    private Instant processedAt;

    private InventoryProcessedEvent(String eventId, String eventType) {
        this.eventId = eventId;
        this.eventType = eventType;
        this.processedAt = Instant.now();
    }

    public static InventoryProcessedEvent markProcessed(String eventId, String eventType) {
        return new InventoryProcessedEvent(eventId, eventType);
    }

    @PrePersist
    protected void prePersist() {
        if (processedAt == null) {
            processedAt = Instant.now();
        }
    }
}