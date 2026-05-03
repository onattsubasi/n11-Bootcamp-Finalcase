package com.onatsubasi.finalcase.order.domain.entity;

import com.onatsubasi.finalcase.common.core.exception.BaseException;
import com.onatsubasi.finalcase.order.domain.enums.OrderStatus;
import com.onatsubasi.finalcase.order.domain.enums.OrderStatusChangeSource;
import com.onatsubasi.finalcase.order.domain.exception.OrderErrorCode;
import jakarta.persistence.*;
import lombok.Getter;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(
        name = "order_status_history",
        indexes = {
                @Index(name = "idx_order_status_history_order_id", columnList = "order_id"),
                @Index(name = "idx_order_status_history_created_at", columnList = "created_at")
        }
)
public class OrderStatusHistory {

    @Getter
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false)
    private Order order;

    @Getter
    @Enumerated(EnumType.STRING)
    @Column(name = "from_status", length = 50)
    private OrderStatus fromStatus;

    @Getter
    @Enumerated(EnumType.STRING)
    @Column(name = "to_status", nullable = false, length = 50)
    private OrderStatus toStatus;

    @Getter
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private OrderStatusChangeSource source;

    @Getter
    @Column(name = "changed_by", length = 100)
    private String changedBy;

    @Getter
    @Column(length = 500)
    private String reason;

    @Getter
    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    protected OrderStatusHistory() {
    }

    public OrderStatusHistory(
            Order order,
            OrderStatus fromStatus,
            OrderStatus toStatus,
            OrderStatusChangeSource source,
            String changedBy,
            String reason
    ) {
        if (order == null) {
            throw new BaseException(
                    OrderErrorCode.INVALID_ORDER_DATA,
                    "Status history must belong to an order"
            );
        }

        if (toStatus == null) {
            throw new BaseException(
                    OrderErrorCode.INVALID_ORDER_DATA,
                    "Target order status is required"
            );
        }

        this.order = order;
        this.fromStatus = fromStatus;
        this.toStatus = toStatus;
        this.source = source == null ? OrderStatusChangeSource.SYSTEM : source;
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