package com.onatsubasi.finalcase.shipment.infrastructure.persistence;

import com.onatsubasi.finalcase.shipment.domain.entity.Shipment;
import jakarta.persistence.LockModeType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;
import java.util.UUID;

public interface SpringDataShipmentJpaRepository extends JpaRepository<Shipment, UUID> {

    Optional<Shipment> findByOrderId(UUID orderId);

    Optional<Shipment> findByShipmentNumber(String shipmentNumber);

    Page<Shipment> findByUserId(UUID userId, Pageable pageable);

    boolean existsByShipmentNumber(String shipmentNumber);

    boolean existsByOrderId(UUID orderId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select s from Shipment s where s.id = :id")
    Optional<Shipment> findByIdForUpdate(UUID id);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select s from Shipment s where s.orderId = :orderId")
    Optional<Shipment> findByOrderIdForUpdate(UUID orderId);
}