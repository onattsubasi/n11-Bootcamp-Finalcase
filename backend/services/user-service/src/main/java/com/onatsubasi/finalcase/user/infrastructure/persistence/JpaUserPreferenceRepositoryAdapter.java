package com.onatsubasi.finalcase.user.infrastructure.persistence;

import com.onatsubasi.finalcase.user.domain.entity.UserPreference;
import com.onatsubasi.finalcase.user.domain.repository.UserPreferenceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
@RequiredArgsConstructor
public class JpaUserPreferenceRepositoryAdapter implements UserPreferenceRepository {

    private final SpringDataUserPreferenceJpaRepository springDataRepository;

    @Override
    public UserPreference save(UserPreference preference) {
        return springDataRepository.save(preference);
    }

    @Override
    public Optional<UserPreference> findByUserId(UUID userId) {
        return springDataRepository.findByUserId(userId);
    }
}
