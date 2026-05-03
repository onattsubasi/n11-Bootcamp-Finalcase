package com.onatsubasi.finalcase.shipment.infrastructure.persistence;

import com.onatsubasi.finalcase.shipment.domain.entity.Shipment;
import com.onatsubasi.finalcase.shipment.domain.repository.ShipmentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
@RequiredArgsConstructor
public class JpaShipmentRepository implements ShipmentRepository {

    private final SpringDataShipmentJpaRepository springDataRepository;

    @Override
    public Shipment save(Shipment shipment) {
        return springDataRepository.save(shipment);
    }

    @Override
    public Optional<Shipment> findById(UUID id) {
        return springDataRepository.findById(id);
    }

    @Override
    public Optional<Shipment> findByIdForUpdate(UUID id) {
        return springDataRepository.findByIdForUpdate(id);
    }

    @Override
    public Optional<Shipment> findByOrderId(UUID orderId) {
        return springDataRepository.findByOrderId(orderId);
    }

    @Override
    public Optional<Shipment> findByOrderIdForUpdate(UUID orderId) {
        return springDataRepository.findByOrderIdForUpdate(orderId);
    }

    @Override
    public Optional<Shipment> findByShipmentNumber(String shipmentNumber) {
        return springDataRepository.findByShipmentNumber(shipmentNumber);
    }

    @Override
    public Page<Shipment> findByUserId(UUID userId, Pageable pageable) {
        return springDataRepository.findByUserId(userId, pageable);
    }

    @Override
    public Page<Shipment> findAll(Pageable pageable) {
        return springDataRepository.findAll(pageable);
    }

    @Override
    public boolean existsByShipmentNumber(String shipmentNumber) {
        return springDataRepository.existsByShipmentNumber(shipmentNumber);
    }

    @Override
    public boolean existsByOrderId(UUID orderId) {
        return springDataRepository.existsByOrderId(orderId);
    }
}