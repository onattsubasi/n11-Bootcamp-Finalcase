package com.onatsubasi.finalcase.checkout.domain.repository;

import com.onatsubasi.finalcase.checkout.domain.entity.CheckoutSagaStep;
import com.onatsubasi.finalcase.checkout.domain.enums.CheckoutSagaStepName;
import com.onatsubasi.finalcase.checkout.domain.enums.CheckoutSagaStepStatus;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface CheckoutSagaStepRepository {

    CheckoutSagaStep save(CheckoutSagaStep step);

    Optional<CheckoutSagaStep> findById(UUID id);

    List<CheckoutSagaStep> findByCheckoutSessionId(UUID checkoutSessionId);

    Optional<CheckoutSagaStep> findLatestByCheckoutSessionIdAndStepName(
            UUID checkoutSessionId,
            CheckoutSagaStepName stepName
    );

    boolean existsByCheckoutSessionIdAndStepNameAndStatus(
            UUID checkoutSessionId,
            CheckoutSagaStepName stepName,
            CheckoutSagaStepStatus status
    );
}
