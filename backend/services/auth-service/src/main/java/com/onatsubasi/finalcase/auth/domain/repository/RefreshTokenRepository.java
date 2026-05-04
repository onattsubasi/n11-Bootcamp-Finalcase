package com.onatsubasi.finalcase.auth.domain.repository;

import com.onatsubasi.finalcase.auth.domain.entity.RefreshToken;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.UUID;

public interface RefreshTokenRepository extends JpaRepository<RefreshToken, UUID> {

  @Lock(LockModeType.PESSIMISTIC_WRITE)
  Optional<RefreshToken> findByTokenHash(String tokenHash);

  @Modifying(clearAutomatically = true, flushAutomatically = true)
  @Query("""
      update RefreshToken rt
         set rt.revoked = true,
             rt.revokedAt = :revokedAt
       where rt.userId = :userId
         and rt.revoked = false
      """)
  int revokeAllActiveTokensByUserId(@Param("userId") UUID userId, @Param("revokedAt") java.time.Instant revokedAt);

  @Modifying(clearAutomatically = true, flushAutomatically = true)
  @Query("""
      update RefreshToken rt
         set rt.revoked = true,
             rt.revokedAt = :revokedAt
       where rt.familyId = :familyId
         and rt.revoked = false
      """)
  int revokeTokenFamily(@Param("familyId") UUID familyId, @Param("revokedAt") java.time.Instant revokedAt);
}