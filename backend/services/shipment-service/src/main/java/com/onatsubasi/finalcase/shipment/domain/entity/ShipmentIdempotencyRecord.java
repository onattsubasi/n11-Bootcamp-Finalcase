package com.onatsubasi.finalcase.shipment.domain.entity;

import com.onatsubasi.finalcase.common.core.exception.BaseException;
import com.onatsubasi.finalcase.shipment.domain.exception.ShipmentErrorCode;
import jakarta.persistence.Column;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.annotations.UpdateTimestamp;
import org.hibernate.type.SqlTypes;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Entity
@Table(
        name = "shipment_idempotency_records",
        indexes = {
                @Index(name = "idx_shipment_idempotency_key", columnList = "idempotency_key", unique = true),
                @Index(name = "idx_shipment_idempotency_shipment_id", columnList = "shipment_id"),
                @Index(name = "idx_shipment_idempotency_order_id", columnList = "order_id")
        }
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EqualsAndHashCode(of = "id")
public class ShipmentIdempotencyRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "idempotency_key", nullable = false, unique = true, length = 120)
    private String idempotencyKey;

    @Column(name = "request_hash", nullable = false, length = 128)
    private String requestHash;

    @Column(name = "order_id")
    private UUID orderId;

    @Column(name = "shipment_id")
    private UUID shipmentId;

    @Column(name = "http_status")
    private Integer httpStatus;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "response_payload", columnDefinition = "jsonb")
    private Map<String, Object> responsePayload = new HashMap<>();

    @Column(name = "locked_until")
    private Instant lockedUntil;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    public ShipmentIdempotencyRecord(
            String idempotencyKey,
            String requestHash,
            Instant lockedUntil
    ) {
        validateRequired(idempotencyKey, "Idempotency key is required");
        validateRequired(requestHash, "Request hash is required");

        this.idempotencyKey = idempotencyKey.trim();
        this.requestHash = requestHash.trim();
        this.lockedUntil = lockedUntil;
        this.responsePayload = new HashMap<>();
    }

    public void validateSameRequest(String requestHash) {
        if (requestHash == null || !this.requestHash.equals(requestHash)) {
            throw new BaseException(ShipmentErrorCode.SHIPMENT_IDEMPOTENCY_CONFLICT);
        }
    }

    public boolean hasStoredResponse() {
        return httpStatus != null
                && responsePayload != null
                && !responsePayload.isEmpty();
    }

    public void attachShipment(UUID orderId, UUID shipmentId) {
        this.orderId = orderId;
        this.shipmentId = shipmentId;
    }

    public void storeResponse(
            int httpStatus,
            Map<String, Object> responsePayload
    ) {
        this.httpStatus = httpStatus;
        this.responsePayload = responsePayload == null
                ? new HashMap<>()
                : new HashMap<>(responsePayload);
    }

    private void validateRequired(String value, String message) {
        if (value == null || value.isBlank()) {
            throw new BaseException(ShipmentErrorCode.INVALID_SHIPMENT_DATA, message);
        }
    }
}