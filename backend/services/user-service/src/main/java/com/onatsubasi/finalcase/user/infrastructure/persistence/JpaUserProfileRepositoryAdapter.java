package com.onatsubasi.finalcase.user.infrastructure.persistence;

import com.onatsubasi.finalcase.user.domain.enums.UserProfileStatus;
import com.onatsubasi.finalcase.user.domain.entity.UserProfile;
import com.onatsubasi.finalcase.user.domain.repository.UserProfileRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
@RequiredArgsConstructor
public class JpaUserProfileRepositoryAdapter implements UserProfileRepository {

    private final SpringDataUserProfileJpaRepository springDataRepository;

    @Override
    public UserProfile save(UserProfile userProfile) {
        return springDataRepository.save(userProfile);
    }

    @Override
    public Optional<UserProfile> findByUserId(UUID userId) {
        return springDataRepository.findByUserId(userId);
    }

    @Override
    public Optional<UserProfile> findByUserIdForUpdate(UUID userId) {
        return springDataRepository.findByUserIdForUpdate(userId);
    }

    @Override
    public boolean existsByUserId(UUID userId) {
        return springDataRepository.existsByUserId(userId);
    }

    @Override
    public List<UserProfile> findByStatus(UserProfileStatus status) {
        return springDataRepository.findByStatus(status);
    }
}
