package com.onatsubasi.finalcase.notification.infrastructure.persistence;

import com.onatsubasi.finalcase.notification.domain.entity.UserProductInterest;
import com.onatsubasi.finalcase.notification.domain.repository.UserProductInterestRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
@RequiredArgsConstructor
public class JpaUserProductInterestRepository
        implements UserProductInterestRepository {

    private final SpringDataUserProductInterestJpaRepository springDataRepository;

    @Override
    public UserProductInterest save(UserProductInterest interest) {
        return springDataRepository.save(interest);
    }

    @Override
    public Optional<UserProductInterest> findByUserIdAndProductIdAndInterestType(
            UUID userId,
            String productId,
            String interestType
    ) {
        return springDataRepository.findByUserIdAndProductIdAndInterestType(
                userId,
                productId,
                interestType
        );
    }

    @Override
    public List<UserProductInterest> findActiveByProductId(String productId) {
        return springDataRepository.findByProductIdAndActiveTrue(productId);
    }
}