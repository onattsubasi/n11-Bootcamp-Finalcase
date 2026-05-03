package com.onatsubasi.finalcase.notification.infrastructure.persistence;

import com.onatsubasi.finalcase.notification.domain.entity.UserProductInterest;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface SpringDataUserProductInterestJpaRepository
        extends JpaRepository<UserProductInterest, UUID> {

    Optional<UserProductInterest> findByUserIdAndProductIdAndInterestType(
            UUID userId,
            String productId,
            String interestType
    );

    List<UserProductInterest> findByProductIdAndActiveTrue(String productId);
}