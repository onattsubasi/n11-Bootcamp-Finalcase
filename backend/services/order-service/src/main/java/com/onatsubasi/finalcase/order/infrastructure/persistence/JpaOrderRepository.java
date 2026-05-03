package com.onatsubasi.finalcase.order.infrastructure.persistence;
import com.onatsubasi.finalcase.order.domain.entity.Order;
import com.onatsubasi.finalcase.order.domain.repository.OrderRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;


@Repository
public class JpaOrderRepository implements OrderRepository {

    private final SpringDataOrderJpaRepository springDataRepository;

    public JpaOrderRepository(SpringDataOrderJpaRepository springDataRepository) {
        this.springDataRepository = springDataRepository;
    }

    @Override
    public Order save(Order order) {
        return springDataRepository.save(order);
    }

    @Override
    public Optional<Order> findById(UUID id) {
        return springDataRepository.findById(id);
    }

    @Override
    public Optional<Order> findByIdForUpdate(UUID id) {
        return springDataRepository.findByIdForUpdate(id);
    }

    @Override
    public Optional<Order> findByCheckoutId(UUID checkoutId) {
        return springDataRepository.findByCheckoutId(checkoutId);
    }

    @Override
    public Optional<Order> findByCheckoutIdForUpdate(UUID checkoutId) {
        return springDataRepository.findByCheckoutIdForUpdate(checkoutId);
    }

    @Override
    public Optional<Order> findByOrderNumber(String orderNumber) {
        return springDataRepository.findByOrderNumber(orderNumber);
    }

    @Override
    public Page<Order> findByUserId(UUID userId, Pageable pageable) {
        return springDataRepository.findByUserId(userId, pageable);
    }

    @Override
    public Page<Order> findAll(Pageable pageable) {
        return springDataRepository.findAll(pageable);
    }

    @Override
    public boolean existsByOrderNumber(String orderNumber) {
        return springDataRepository.existsByOrderNumber(orderNumber);
    }
}