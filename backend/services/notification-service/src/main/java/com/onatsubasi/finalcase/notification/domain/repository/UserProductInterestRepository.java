package com.onatsubasi.finalcase.notification.domain.repository;

import com.onatsubasi.finalcase.notification.domain.entity.UserProductInterest;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface UserProductInterestRepository {

    UserProductInterest save(UserProductInterest interest);

    Optional<UserProductInterest> findByUserIdAndProductIdAndInterestType(
            UUID userId,
            String productId,
            String interestType
    );

    List<UserProductInterest> findActiveByProductId(String productId);
}