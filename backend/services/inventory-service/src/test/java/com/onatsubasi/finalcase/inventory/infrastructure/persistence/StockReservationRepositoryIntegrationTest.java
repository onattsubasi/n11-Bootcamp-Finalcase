package com.onatsubasi.finalcase.inventory.infrastructure.persistence;

import com.onatsubasi.finalcase.inventory.AbstractIntegrationTest;
import com.onatsubasi.finalcase.inventory.domain.enums.StockReservationStatus;
import com.onatsubasi.finalcase.inventory.domain.entity.StockReservation;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@Transactional
class StockReservationRepositoryIntegrationTest extends AbstractIntegrationTest {

    @Autowired
    private JpaStockReservationRepositoryAdapter stockReservationRepository;

    @Test
    @DisplayName("reservation can be found by idempotency key with items loaded")
    void shouldSaveAndFindByIdempotencyKey() {
        UUID checkoutId = UUID.randomUUID();
        String idempotencyKey = "key-" + checkoutId;
        StockReservation reservation = StockReservation.create(
                idempotencyKey,
                "hash123",
                checkoutId,
                UUID.randomUUID(),
                Instant.now().plusSeconds(300)
        );
        reservation.addItem(UUID.randomUUID(), 2);

        stockReservationRepository.save(reservation);
        Optional<StockReservation> found = stockReservationRepository.findByIdempotencyKey(idempotencyKey);

        assertThat(found).isPresent();
        assertThat(found.get().getCheckoutId()).isEqualTo(checkoutId);
        assertThat(found.get().getStatus()).isEqualTo(StockReservationStatus.RESERVED);
        assertThat(found.get().getItems()).hasSize(1);
    }
}
