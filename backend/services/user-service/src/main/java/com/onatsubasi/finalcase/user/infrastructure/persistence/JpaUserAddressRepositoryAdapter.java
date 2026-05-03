package com.onatsubasi.finalcase.user.infrastructure.persistence;

import com.onatsubasi.finalcase.user.domain.entity.UserAddress;
import com.onatsubasi.finalcase.user.domain.repository.UserAddressRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
@RequiredArgsConstructor
public class JpaUserAddressRepositoryAdapter implements UserAddressRepository {

    private final SpringDataUserAddressJpaRepository springDataRepository;

    @Override
    public UserAddress save(UserAddress address) {
        return springDataRepository.save(address);
    }

    @Override
    public Optional<UserAddress> findById(UUID addressId) {
        return springDataRepository.findById(addressId);
    }

    @Override
    public Optional<UserAddress> findByIdAndUserIdAndDeletedFalse(UUID addressId, UUID userId) {
        return springDataRepository.findByIdAndUserIdAndDeletedFalse(addressId, userId);
    }

    @Override
    public Optional<UserAddress> findDefaultShippingAddress(UUID userId) {
        return springDataRepository.findDefaultShippingAddress(userId);
    }

    @Override
    public Optional<UserAddress> findDefaultBillingAddress(UUID userId) {
        return springDataRepository.findDefaultBillingAddress(userId);
    }

    @Override
    public List<UserAddress> findByUserIdAndDeletedFalse(UUID userId) {
        return springDataRepository.findByUserIdAndDeletedFalseOrderByCreatedAtDesc(userId);
    }

    @Override
    public void clearDefaultShipping(UUID userId, java.time.Instant updatedAt) {
        springDataRepository.clearDefaultShipping(userId, updatedAt);
    }

    @Override
    public void clearDefaultBilling(UUID userId, java.time.Instant updatedAt) {
        springDataRepository.clearDefaultBilling(userId, updatedAt);
    }
}
