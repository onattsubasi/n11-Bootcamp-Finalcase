package com.onatsubasi.finalcase.checkout.domain.repository;

import com.onatsubasi.finalcase.checkout.domain.entity.CheckoutSagaStep;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface CheckoutSagaRepository extends JpaRepository<CheckoutSagaStep, UUID> {
}
