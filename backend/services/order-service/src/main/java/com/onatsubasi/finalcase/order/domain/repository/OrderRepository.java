package com.onatsubasi.finalcase.order.domain.repository;

import com.onatsubasi.finalcase.order.domain.entity.Order;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Optional;
import java.util.UUID;

public interface OrderRepository {

    Order save(Order order);

    Optional<Order> findById(UUID id);

    Optional<Order> findByIdForUpdate(UUID id);

    Optional<Order> findByCheckoutId(UUID checkoutId);

    Optional<Order> findByCheckoutIdForUpdate(UUID checkoutId);

    Optional<Order> findByOrderNumber(String orderNumber);

    Page<Order> findByUserId(UUID userId, Pageable pageable);

    Page<Order> findAll(Pageable pageable);

    boolean existsByOrderNumber(String orderNumber);
}
