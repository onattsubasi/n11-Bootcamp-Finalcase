package com.onatsubasi.finalcase.inventory.domain.entity;

import com.onatsubasi.finalcase.common.core.exception.BaseException;
import com.onatsubasi.finalcase.inventory.domain.enums.ReleaseReason;
import com.onatsubasi.finalcase.inventory.domain.enums.StockReservationStatus;
import com.onatsubasi.finalcase.inventory.domain.exception.InventoryErrorCode;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class StockReservationDomainTest {

    @Test
    @DisplayName("same confirm request is idempotent but different order id is rejected")
    void confirmIsIdempotentForSameOrderOnly() {
        StockReservation reservation = newReservation();
        UUID orderId = UUID.randomUUID();

        reservation.confirm(orderId);
        reservation.confirm(orderId);

        assertThat(reservation.getStatus()).isEqualTo(StockReservationStatus.CONFIRMED);
        assertThat(reservation.getOrderId()).isEqualTo(orderId);
        assertThatThrownBy(() -> reservation.confirm(UUID.randomUUID()))
                .isInstanceOf(BaseException.class)
                .extracting("errorCode")
                .isEqualTo(InventoryErrorCode.RESERVATION_ALREADY_CONFIRMED);
    }

    @Test
    @DisplayName("release is idempotent after first state change")
    void releaseIsIdempotent() {
        StockReservation reservation = newReservation();

        boolean first = reservation.release(ReleaseReason.PAYMENT_FAILED);
        boolean second = reservation.release(ReleaseReason.PAYMENT_FAILED);

        assertThat(first).isTrue();
        assertThat(second).isFalse();
        assertThat(reservation.getStatus()).isEqualTo(StockReservationStatus.RELEASED);
    }

    @Test
    @DisplayName("request hash mismatch blocks idempotency key reuse")
    void requestHashMismatchIsRejected() {
        StockReservation reservation = newReservation();

        assertThatThrownBy(() -> reservation.assertSameRequestHash("different-hash"))
                .isInstanceOf(BaseException.class)
                .extracting("errorCode")
                .isEqualTo(InventoryErrorCode.IDEMPOTENCY_KEY_REUSED_WITH_DIFFERENT_PAYLOAD);
    }

    private StockReservation newReservation() {
        StockReservation reservation = StockReservation.create(
                "idem-key",
                "request-hash",
                UUID.randomUUID(),
                UUID.randomUUID(),
                Instant.now().plusSeconds(600)
        );
        reservation.addItem(UUID.randomUUID(), 1);
        return reservation;
    }
}
