package com.onatsubasi.finalcase.shipment.domain.repository;

import com.onatsubasi.finalcase.shipment.domain.entity.Shipment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Optional;
import java.util.UUID;

public interface ShipmentRepository {

    Shipment save(Shipment shipment);

    Optional<Shipment> findById(UUID id);

    Optional<Shipment> findByIdForUpdate(UUID id);

    Optional<Shipment> findByOrderId(UUID orderId);

    Optional<Shipment> findByOrderIdForUpdate(UUID orderId);

    Optional<Shipment> findByShipmentNumber(String shipmentNumber);

    Page<Shipment> findByUserId(UUID userId, Pageable pageable);

    Page<Shipment> findAll(Pageable pageable);

    boolean existsByShipmentNumber(String shipmentNumber);

    boolean existsByOrderId(UUID orderId);
}