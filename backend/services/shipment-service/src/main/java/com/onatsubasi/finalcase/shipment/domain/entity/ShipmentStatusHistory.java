package com.onatsubasi.finalcase.shipment.domain.entity;

import com.onatsubasi.finalcase.common.core.exception.BaseException;
import com.onatsubasi.finalcase.shipment.domain.enums.ShipmentStatus;
import com.onatsubasi.finalcase.shipment.domain.enums.ShipmentStatusChangeSource;
import com.onatsubasi.finalcase.shipment.domain.exception.ShipmentErrorCode;
import jakarta.persistence.Column;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import jakarta.persistence.*;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(
        name = "shipment_status_history",
        indexes = {
                @Index(name = "idx_shipment_status_history_shipment_id", columnList = "shipment_id"),
                @Index(name = "idx_shipment_status_history_created_at", columnList = "created_at")
        }
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EqualsAndHashCode(of = "id")
public class ShipmentStatusHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "shipment_id", nullable = false)
    private Shipment shipment;

    @Enumerated(EnumType.STRING)
    @Column(name = "from_status", length = 50)
    private ShipmentStatus fromStatus;

    @Enumerated(EnumType.STRING)
    @Column(name = "to_status", nullable = false, length = 50)
    private ShipmentStatus toStatus;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private ShipmentStatusChangeSource source;

    @Column(name = "changed_by", length = 100)
    private String changedBy;

    @Column(length = 500)
    private String reason;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    public ShipmentStatusHistory(
            Shipment shipment,
            ShipmentStatus fromStatus,
            ShipmentStatus toStatus,
            ShipmentStatusChangeSource source,
            String changedBy,
            String reason
    ) {
        if (shipment == null) {
            throw new BaseException(
                    ShipmentErrorCode.INVALID_SHIPMENT_DATA,
                    "Shipment status history must belong to shipment"
            );
        }

        if (toStatus == null) {
            throw new BaseException(
                    ShipmentErrorCode.INVALID_SHIPMENT_DATA,
                    "Target shipment status is required"
            );
        }

        this.shipment = shipment;
        this.fromStatus = fromStatus;
        this.toStatus = toStatus;
        this.source = source == null ? ShipmentStatusChangeSource.SYSTEM : source;
        this.changedBy = normalize(changedBy);
        this.reason = normalize(reason);
        this.createdAt = Instant.now();
    }

    private String normalize(String value) {
        return value == null || value.isBlank()
                ? null
                : value.trim();
    }
}