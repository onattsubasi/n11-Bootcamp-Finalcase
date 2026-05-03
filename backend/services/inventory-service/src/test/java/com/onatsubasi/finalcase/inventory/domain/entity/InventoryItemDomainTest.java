package com.onatsubasi.finalcase.inventory.domain.entity;

import com.onatsubasi.finalcase.common.core.exception.BaseException;
import com.onatsubasi.finalcase.inventory.domain.enums.StockStatus;
import com.onatsubasi.finalcase.inventory.domain.exception.InventoryErrorCode;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class InventoryItemDomainTest {

    @Test
    @DisplayName("reserve, confirm and release preserve stock invariants")
    void reservationLifecyclePreservesQuantities() {
        InventoryItem item = InventoryItem.create(UUID.randomUUID(), 10, 2);

        item.reserve(3);
        assertThat(item.getTotalQuantity()).isEqualTo(10);
        assertThat(item.getReservedQuantity()).isEqualTo(3);
        assertThat(item.availableQuantity()).isEqualTo(7);

        item.releaseReserved(1);
        assertThat(item.getReservedQuantity()).isEqualTo(2);
        assertThat(item.availableQuantity()).isEqualTo(8);

        item.confirmSale(2);
        assertThat(item.getTotalQuantity()).isEqualTo(8);
        assertThat(item.getReservedQuantity()).isZero();
        assertThat(item.availableQuantity()).isEqualTo(8);
    }

    @Test
    @DisplayName("cannot decrease total quantity below reserved quantity")
    void cannotDecreaseBelowReservedQuantity() {
        InventoryItem item = InventoryItem.create(UUID.randomUUID(), 5, 1);
        item.reserve(4);

        assertThatThrownBy(() -> item.decreaseStock(2))
                .isInstanceOf(BaseException.class)
                .extracting("errorCode")
                .isEqualTo(InventoryErrorCode.TOTAL_QUANTITY_BELOW_RESERVED);
    }

    @Test
    @DisplayName("stock status is calculated from available quantity and threshold")
    void stockStatusUsesAvailableQuantity() {
        InventoryItem item = InventoryItem.create(UUID.randomUUID(), 3, 2);

        assertThat(item.stockStatus()).isEqualTo(StockStatus.IN_STOCK);

        item.reserve(1);
        assertThat(item.stockStatus()).isEqualTo(StockStatus.LOW_STOCK);

        item.reserve(2);
        assertThat(item.stockStatus()).isEqualTo(StockStatus.OUT_OF_STOCK);
    }
}
