package com.onatsubasi.finalcase.shipment.domain;

import com.onatsubasi.finalcase.common.core.exception.BaseException;
import com.onatsubasi.finalcase.shipment.domain.enums.ShipmentStatus;
import com.onatsubasi.finalcase.shipment.domain.enums.ShipmentStatusChangeSource;
import com.onatsubasi.finalcase.shipment.domain.entity.Shipment;
import com.onatsubasi.finalcase.shipment.support.ShipmentTestData;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ShipmentDomainTest {

    @Test
    void constructorCreatesShipmentWithCreatedStatusAndInitialHistory() {
        Shipment shipment = ShipmentTestData.shipment();

        assertThat(shipment.getStatus()).isEqualTo(ShipmentStatus.CREATED);
        assertThat(shipment.getItems()).hasSize(1);
        assertThat(shipment.getStatusHistory()).hasSize(1);
        assertThat(shipment.getStatusHistory().getFirst().getToStatus()).isEqualTo(ShipmentStatus.CREATED);
    }

    @Test
    void validStatusLifecycleCanReachDelivered() {
        Shipment shipment = ShipmentTestData.shipment();

        shipment.markReadyToShip(ShipmentStatusChangeSource.ADMIN, "admin", "ready");
        shipment.markShipped("TRK-1", "https://carrier.example/TRK-1", ShipmentStatusChangeSource.ADMIN, "admin", "shipped");
        shipment.markDelivered(ShipmentStatusChangeSource.ADMIN, "admin", "delivered");

        assertThat(shipment.getStatus()).isEqualTo(ShipmentStatus.DELIVERED);
        assertThat(shipment.getTrackingNumber()).isEqualTo("TRK-1");
        assertThat(shipment.getDeliveredAt()).isNotNull();
        assertThat(shipment.getStatusHistory()).extracting("toStatus")
                .containsExactly(
                        ShipmentStatus.CREATED,
                        ShipmentStatus.READY_TO_SHIP,
                        ShipmentStatus.SHIPPED,
                        ShipmentStatus.DELIVERED
                );
    }

    @Test
    void invalidTransitionFromCreatedToDeliveredIsRejected() {
        Shipment shipment = ShipmentTestData.shipment();

        assertThatThrownBy(() -> shipment.markDelivered(ShipmentStatusChangeSource.ADMIN, "admin", "skip"))
                .isInstanceOf(BaseException.class)
                .hasMessageContaining("Cannot transition shipment from CREATED to DELIVERED");
    }

    @Test
    void shippedShipmentCannotBeCancelledInInitialPolicy() {
        Shipment shipment = ShipmentTestData.shipment();
        shipment.markReadyToShip(ShipmentStatusChangeSource.ADMIN, "admin", "ready");
        shipment.markShipped("TRK-1", null, ShipmentStatusChangeSource.ADMIN, "admin", "shipped");

        assertThatThrownBy(() -> shipment.cancel(ShipmentStatusChangeSource.ADMIN, "admin", "cancel"))
                .isInstanceOf(BaseException.class);
    }
}
