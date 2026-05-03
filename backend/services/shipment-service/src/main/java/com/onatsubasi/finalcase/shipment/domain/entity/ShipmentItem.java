package com.onatsubasi.finalcase.shipment.domain.entity;

import com.onatsubasi.finalcase.common.core.exception.BaseException;
import com.onatsubasi.finalcase.shipment.domain.exception.ShipmentErrorCode;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Entity
@Table(
        name = "shipment_items",
        indexes = {
                @Index(name = "idx_shipment_items_shipment_id", columnList = "shipment_id"),
                @Index(name = "idx_shipment_items_product_id", columnList = "product_id")
        }
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EqualsAndHashCode(of = "id")
public class ShipmentItem {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "shipment_id", nullable = false)
    private Shipment shipment;

    @Column(name = "product_id", nullable = false, length = 100)
    private String productId;

    @Column(nullable = false, length = 120)
    private String sku;

    @Column(name = "product_name", nullable = false, length = 250)
    private String productName;

    @Column(nullable = false)
    private int quantity;

    public ShipmentItem(
            String productId,
            String sku,
            String productName,
            int quantity
    ) {
        validateRequired(productId, "Product id is required");
        validateRequired(sku, "SKU is required");
        validateRequired(productName, "Product name is required");

        if (quantity <= 0) {
            throw new BaseException(
                    ShipmentErrorCode.INVALID_SHIPMENT_ITEM,
                    "Shipment item quantity must be greater than zero"
            );
        }

        this.productId = productId.trim();
        this.sku = sku.trim();
        this.productName = productName.trim();
        this.quantity = quantity;
    }

    void assignTo(Shipment shipment) {
        if (shipment == null) {
            throw new BaseException(
                    ShipmentErrorCode.INVALID_SHIPMENT_ITEM,
                    "Shipment item must belong to shipment"
            );
        }

        this.shipment = shipment;
    }

    private void validateRequired(String value, String message) {
        if (value == null || value.isBlank()) {
            throw new BaseException(ShipmentErrorCode.INVALID_SHIPMENT_ITEM, message);
        }
    }
}