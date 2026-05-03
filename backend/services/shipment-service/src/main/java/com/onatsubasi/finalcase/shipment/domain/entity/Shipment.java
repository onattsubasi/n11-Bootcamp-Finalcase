package com.onatsubasi.finalcase.shipment.domain.entity;

import com.onatsubasi.finalcase.shipment.domain.enums.ShipmentCarrier;
import com.onatsubasi.finalcase.shipment.domain.enums.ShipmentStatus;
import com.onatsubasi.finalcase.shipment.domain.enums.ShipmentStatusChangeSource;
import jakarta.persistence.*;
import com.onatsubasi.finalcase.common.core.exception.BaseException;
import com.onatsubasi.finalcase.shipment.domain.exception.ShipmentErrorCode;
import jakarta.persistence.Column;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(
        name = "shipments",
        indexes = {
                @Index(name = "idx_shipments_shipment_number", columnList = "shipment_number", unique = true),
                @Index(name = "idx_shipments_order_id", columnList = "order_id", unique = true),
                @Index(name = "idx_shipments_user_id", columnList = "user_id"),
                @Index(name = "idx_shipments_status", columnList = "status"),
                @Index(name = "idx_shipments_carrier", columnList = "carrier"),
                @Index(name = "idx_shipments_tracking_number", columnList = "tracking_number")
        }
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EqualsAndHashCode(of = "id")
public class Shipment {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "shipment_number", nullable = false, unique = true, length = 80)
    private String shipmentNumber;

    @Column(name = "order_id", nullable = false, unique = true)
    private UUID orderId;

    @Column(name = "order_number", nullable = false, length = 80)
    private String orderNumber;

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private ShipmentCarrier carrier;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private ShipmentStatus status;

    @Column(name = "carrier_shipment_id", length = 150)
    private String carrierShipmentId;

    @Column(name = "tracking_number", length = 150)
    private String trackingNumber;

    @Column(name = "tracking_url", length = 1000)
    private String trackingUrl;

    @Column(name = "label_url", length = 1000)
    private String labelUrl;

    @Column(name = "carrier_status", length = 100)
    private String carrierStatus;

    @Column(name = "failure_reason", length = 1000)
    private String failureReason;

    @Embedded
    private ShipmentAddressSnapshot shippingAddress;

    @OneToMany(mappedBy = "shipment", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ShipmentItem> items = new ArrayList<>();

    @OneToMany(mappedBy = "shipment", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ShipmentStatusHistory> statusHistory = new ArrayList<>();

    @Version
    private Long version;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @Column(name = "ready_to_ship_at")
    private Instant readyToShipAt;

    @Column(name = "shipped_at")
    private Instant shippedAt;

    @Column(name = "in_transit_at")
    private Instant inTransitAt;

    @Column(name = "out_for_delivery_at")
    private Instant outForDeliveryAt;

    @Column(name = "delivered_at")
    private Instant deliveredAt;

    @Column(name = "delivery_failed_at")
    private Instant deliveryFailedAt;

    @Column(name = "cancelled_at")
    private Instant cancelledAt;

    public Shipment(
            String shipmentNumber,
            UUID orderId,
            String orderNumber,
            UUID userId,
            ShipmentCarrier carrier,
            ShipmentAddressSnapshot shippingAddress
    ) {
        validateRequired(shipmentNumber, "Shipment number is required");
        validateUuid(orderId, "Order id is required");
        validateRequired(orderNumber, "Order number is required");
        validateUuid(userId, "User id is required");

        if (carrier == null) {
            throw new BaseException(
                    ShipmentErrorCode.INVALID_SHIPMENT_DATA,
                    "Shipment carrier is required"
            );
        }

        if (shippingAddress == null) {
            throw new BaseException(ShipmentErrorCode.INVALID_SHIPMENT_ADDRESS);
        }

        this.shipmentNumber = shipmentNumber.trim();
        this.orderId = orderId;
        this.orderNumber = orderNumber.trim();
        this.userId = userId;
        this.carrier = carrier;
        this.shippingAddress = shippingAddress;
        this.status = ShipmentStatus.CREATED;

        addStatusHistory(
                null,
                ShipmentStatus.CREATED,
                ShipmentStatusChangeSource.CHECKOUT_SERVICE,
                null,
                "Shipment created"
        );
    }

    public void addItem(ShipmentItem item) {
        if (item == null) {
            throw new BaseException(ShipmentErrorCode.INVALID_SHIPMENT_ITEM);
        }

        items.add(item);
        item.assignTo(this);
    }

    public void attachCarrierResult(
            String carrierShipmentId,
            String trackingNumber,
            String trackingUrl,
            String labelUrl,
            String carrierStatus
    ) {
        this.carrierShipmentId = normalize(carrierShipmentId);
        this.trackingNumber = normalize(trackingNumber);
        this.trackingUrl = normalize(trackingUrl);
        this.labelUrl = normalize(labelUrl);
        this.carrierStatus = normalize(carrierStatus);
    }

    public void updateTracking(
            String trackingNumber,
            String trackingUrl,
            ShipmentStatusChangeSource source,
            String changedBy,
            String reason
    ) {
        this.trackingNumber = normalize(trackingNumber);
        this.trackingUrl = normalize(trackingUrl);

        addStatusHistory(
                status,
                status,
                source,
                changedBy,
                reason == null ? "Tracking updated" : reason
        );
    }

    public void markReadyToShip(
            ShipmentStatusChangeSource source,
            String changedBy,
            String reason
    ) {
        if (status == ShipmentStatus.READY_TO_SHIP) {
            return;
        }

        transitionTo(
                ShipmentStatus.READY_TO_SHIP,
                source,
                changedBy,
                reason == null ? "Shipment ready to ship" : reason
        );

        this.readyToShipAt = Instant.now();
    }

    public void markShipped(
            String trackingNumber,
            String trackingUrl,
            ShipmentStatusChangeSource source,
            String changedBy,
            String reason
    ) {
        if (status == ShipmentStatus.SHIPPED) {
            return;
        }

        if (trackingNumber != null && !trackingNumber.isBlank()) {
            this.trackingNumber = trackingNumber.trim();
        }

        if (trackingUrl != null && !trackingUrl.isBlank()) {
            this.trackingUrl = trackingUrl.trim();
        }

        transitionTo(
                ShipmentStatus.SHIPPED,
                source,
                changedBy,
                reason == null ? "Shipment shipped" : reason
        );

        this.shippedAt = Instant.now();
    }

    public void markInTransit(
            ShipmentStatusChangeSource source,
            String changedBy,
            String reason
    ) {
        if (status == ShipmentStatus.IN_TRANSIT) {
            return;
        }

        transitionTo(
                ShipmentStatus.IN_TRANSIT,
                source,
                changedBy,
                reason == null ? "Shipment in transit" : reason
        );

        this.inTransitAt = Instant.now();
    }

    public void markOutForDelivery(
            ShipmentStatusChangeSource source,
            String changedBy,
            String reason
    ) {
        if (status == ShipmentStatus.OUT_FOR_DELIVERY) {
            return;
        }

        transitionTo(
                ShipmentStatus.OUT_FOR_DELIVERY,
                source,
                changedBy,
                reason == null ? "Shipment out for delivery" : reason
        );

        this.outForDeliveryAt = Instant.now();
    }

    public void markDelivered(
            ShipmentStatusChangeSource source,
            String changedBy,
            String reason
    ) {
        if (status == ShipmentStatus.DELIVERED) {
            return;
        }

        transitionTo(
                ShipmentStatus.DELIVERED,
                source,
                changedBy,
                reason == null ? "Shipment delivered" : reason
        );

        this.deliveredAt = Instant.now();
    }

    public void markDeliveryFailed(
            String failureReason,
            ShipmentStatusChangeSource source,
            String changedBy
    ) {
        if (status == ShipmentStatus.DELIVERY_FAILED) {
            return;
        }

        this.failureReason = normalize(failureReason);

        transitionTo(
                ShipmentStatus.DELIVERY_FAILED,
                source,
                changedBy,
                failureReason == null ? "Shipment delivery failed" : failureReason
        );

        this.deliveryFailedAt = Instant.now();
    }

    public void cancel(
            ShipmentStatusChangeSource source,
            String changedBy,
            String reason
    ) {
        if (status == ShipmentStatus.CANCELLED) {
            return;
        }

        if (!canCancel()) {
            throw new BaseException(ShipmentErrorCode.SHIPMENT_CANCEL_NOT_ALLOWED);
        }

        transitionTo(
                ShipmentStatus.CANCELLED,
                source,
                changedBy,
                reason == null ? "Shipment cancelled" : reason
        );

        this.cancelledAt = Instant.now();
    }

    public List<ShipmentItem> getItems() {
        return List.copyOf(items);
    }

    public List<ShipmentStatusHistory> getStatusHistory() {
        return List.copyOf(statusHistory);
    }

    private void transitionTo(
            ShipmentStatus targetStatus,
            ShipmentStatusChangeSource source,
            String changedBy,
            String reason
    ) {
        if (!canTransition(status, targetStatus)) {
            throw new BaseException(
                    ShipmentErrorCode.SHIPMENT_INVALID_STATUS_TRANSITION,
                    "Cannot transition shipment from " + status + " to " + targetStatus
            );
        }

        ShipmentStatus previous = this.status;
        this.status = targetStatus;

        addStatusHistory(previous, targetStatus, source, changedBy, reason);
    }

    private boolean canTransition(
            ShipmentStatus from,
            ShipmentStatus to
    ) {
        if (from == to) {
            return true;
        }

        return switch (from) {
            case CREATED -> to == ShipmentStatus.READY_TO_SHIP
                    || to == ShipmentStatus.SHIPPED
                    || to == ShipmentStatus.CANCELLED
                    || to == ShipmentStatus.DELIVERY_FAILED;

            case READY_TO_SHIP -> to == ShipmentStatus.SHIPPED
                    || to == ShipmentStatus.CANCELLED
                    || to == ShipmentStatus.DELIVERY_FAILED;

            case SHIPPED -> to == ShipmentStatus.IN_TRANSIT
                    || to == ShipmentStatus.OUT_FOR_DELIVERY
                    || to == ShipmentStatus.DELIVERED
                    || to == ShipmentStatus.DELIVERY_FAILED;

            case IN_TRANSIT -> to == ShipmentStatus.OUT_FOR_DELIVERY
                    || to == ShipmentStatus.DELIVERED
                    || to == ShipmentStatus.DELIVERY_FAILED;

            case OUT_FOR_DELIVERY -> to == ShipmentStatus.DELIVERED
                    || to == ShipmentStatus.DELIVERY_FAILED;

            case DELIVERY_FAILED -> to == ShipmentStatus.READY_TO_SHIP
                    || to == ShipmentStatus.CANCELLED;

            case DELIVERED, CANCELLED -> false;
        };
    }

    private boolean canCancel() {
        return status == ShipmentStatus.CREATED
                || status == ShipmentStatus.READY_TO_SHIP
                || status == ShipmentStatus.DELIVERY_FAILED;
    }

    private void addStatusHistory(
            ShipmentStatus from,
            ShipmentStatus to,
            ShipmentStatusChangeSource source,
            String changedBy,
            String reason
    ) {
        statusHistory.add(new ShipmentStatusHistory(
                this,
                from,
                to,
                source,
                changedBy,
                reason
        ));
    }

    private void validateUuid(UUID value, String message) {
        if (value == null) {
            throw new BaseException(ShipmentErrorCode.INVALID_SHIPMENT_DATA, message);
        }
    }

    private void validateRequired(String value, String message) {
        if (value == null || value.isBlank()) {
            throw new BaseException(ShipmentErrorCode.INVALID_SHIPMENT_DATA, message);
        }
    }

    private String normalize(String value) {
        return value == null || value.isBlank()
                ? null
                : value.trim();
    }
}
