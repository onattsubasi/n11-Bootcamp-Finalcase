package com.onatsubasi.finalcase.user.domain.repository;

import com.onatsubasi.finalcase.user.domain.entity.UserAddress;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface UserAddressRepository {

    UserAddress save(UserAddress address);

    Optional<UserAddress> findById(UUID addressId);

    Optional<UserAddress> findByIdAndUserIdAndDeletedFalse(UUID addressId, UUID userId);

    Optional<UserAddress> findDefaultShippingAddress(UUID userId);

    Optional<UserAddress> findDefaultBillingAddress(UUID userId);

    List<UserAddress> findByUserIdAndDeletedFalse(UUID userId);

    void clearDefaultShipping(UUID userId, java.time.Instant updatedAt);

    void clearDefaultBilling(UUID userId, java.time.Instant updatedAt);
}
