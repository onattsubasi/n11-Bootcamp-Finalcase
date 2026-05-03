package com.onatsubasi.finalcase.order.infrastructure.persistence;

import com.onatsubasi.finalcase.order.domain.entity.Order;
import jakarta.persistence.LockModeType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;
import java.util.UUID;

public interface SpringDataOrderJpaRepository extends JpaRepository<Order, UUID> {

    Optional<Order> findByCheckoutId(UUID checkoutId);

    Optional<Order> findByOrderNumber(String orderNumber);

    Page<Order> findByUserId(UUID userId, Pageable pageable);

    boolean existsByOrderNumber(String orderNumber);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select o from Order o where o.id = :id")
    Optional<Order> findByIdForUpdate(UUID id);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select o from Order o where o.checkoutId = :checkoutId")
    Optional<Order> findByCheckoutIdForUpdate(UUID checkoutId);
}