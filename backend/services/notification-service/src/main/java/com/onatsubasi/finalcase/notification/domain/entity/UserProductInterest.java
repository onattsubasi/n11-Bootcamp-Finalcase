package com.onatsubasi.finalcase.notification.domain.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(
        name = "user_product_interests",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_user_product_interest",
                        columnNames = {"user_id", "product_id", "interest_type"}
                )
        },
        indexes = {
                @Index(name = "idx_user_product_interests_user_id", columnList = "user_id"),
                @Index(name = "idx_user_product_interests_product_id", columnList = "product_id"),
                @Index(name = "idx_user_product_interests_active", columnList = "active")
        }
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EqualsAndHashCode(of = "id")
public class UserProductInterest {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Column(name = "product_id", nullable = false, length = 100)
    private String productId;

    @Column(name = "interest_type", nullable = false, length = 80)
    private String interestType;

    @Column(nullable = false)
    private boolean active;

    @Column(name = "last_notified_at")
    private Instant lastNotifiedAt;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    public UserProductInterest(
            UUID userId,
            String productId,
            String interestType
    ) {
        this.userId = userId;
        this.productId = productId;
        this.interestType = interestType;
        this.active = true;
    }

    public void deactivate() {
        this.active = false;
    }

    public void markNotified() {
        this.lastNotifiedAt = Instant.now();
    }
}