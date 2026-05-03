package com.onatsubasi.finalcase.order.domain.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.Getter;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

@Embeddable
public class OrderShipmentSummary {

    @Getter
    @Column(name = "shipment_id")
    private UUID shipmentId;

    @Getter
    @Column(name = "shipment_number", length = 80)
    private String shipmentNumber;

    @Getter
    @Column(name = "carrier", length = 80)
    private String carrier;

    @Getter
    @Column(name = "tracking_number", length = 150)
    private String trackingNumber;

    @Getter
    @Column(name = "shipment_status", length = 50)
    private String shipmentStatus;

    @Getter
    @Column(name = "shipped_at")
    private Instant shippedAt;

    @Getter
    @Column(name = "delivered_at")
    private Instant deliveredAt;

    protected OrderShipmentSummary() {
    }

    public static OrderShipmentSummary empty() {
        return new OrderShipmentSummary(null, null, null, null, null, null, null);
    }

    public OrderShipmentSummary(UUID shipmentId, String shipmentNumber, String carrier, String trackingNumber, String shipmentStatus, Instant shippedAt, Instant deliveredAt) {
        this.shipmentId = shipmentId;
        this.shipmentNumber = normalize(shipmentNumber);
        this.carrier = normalize(carrier);
        this.trackingNumber = normalize(trackingNumber);
        this.shipmentStatus = normalize(shipmentStatus);
        this.shippedAt = shippedAt;
        this.deliveredAt = deliveredAt;
    }

    public boolean hasShipment() {
        return shipmentId != null;
    }

    public boolean sameShipment(UUID candidateShipmentId) {
        return Objects.equals(shipmentId, candidateShipmentId);
    }

    public void updateCreated(UUID shipmentId, String shipmentNumber, String carrier, String trackingNumber, String shipmentStatus) {
        this.shipmentId = shipmentId;
        this.shipmentNumber = normalize(shipmentNumber);
        this.carrier = normalize(carrier);
        this.trackingNumber = normalize(trackingNumber);
        this.shipmentStatus = normalize(shipmentStatus == null ? "CREATED" : shipmentStatus);
    }

    public void markShipped(String carrier, String trackingNumber, Instant shippedAt) {
        this.carrier = normalize(carrier);
        this.trackingNumber = normalize(trackingNumber);
        this.shipmentStatus = "SHIPPED";
        this.shippedAt = shippedAt == null ? Instant.now() : shippedAt;
    }

    public void markDelivered(Instant deliveredAt) {
        this.shipmentStatus = "DELIVERED";
        this.deliveredAt = deliveredAt == null ? Instant.now() : deliveredAt;
    }

    private String normalize(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }
}
