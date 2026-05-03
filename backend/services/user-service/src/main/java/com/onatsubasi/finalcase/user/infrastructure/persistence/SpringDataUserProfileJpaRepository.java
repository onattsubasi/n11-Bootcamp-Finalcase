package com.onatsubasi.finalcase.user.infrastructure.persistence;

import com.onatsubasi.finalcase.user.domain.enums.UserProfileStatus;
import com.onatsubasi.finalcase.user.domain.entity.UserProfile;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface SpringDataUserProfileJpaRepository extends JpaRepository<UserProfile, UUID> {

    Optional<UserProfile> findByUserId(UUID userId);

    boolean existsByUserId(UUID userId);

    List<UserProfile> findByStatus(UserProfileStatus status);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
           select p
             from UserProfile p
            where p.userId = :userId
           """)
    Optional<UserProfile> findByUserIdForUpdate(@Param("userId") UUID userId);
}