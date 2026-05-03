package com.onatsubasi.finalcase.auth.application.service;

import com.onatsubasi.finalcase.auth.domain.entity.AuthUser;
import com.onatsubasi.finalcase.auth.domain.entity.RefreshToken;
import com.onatsubasi.finalcase.auth.domain.exception.AuthErrorCode;
import com.onatsubasi.finalcase.auth.domain.repository.RefreshTokenRepository;
import com.onatsubasi.finalcase.common.core.exception.BaseException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RefreshTokenServiceTest {

    @Mock
    private RefreshTokenRepository refreshTokenRepository;

    private RefreshTokenService refreshTokenService;

    private static final String PEPPER = "test-pepper";
    private static final long EXPIRATION = 604800;

    @BeforeEach
    void setUp() {
        refreshTokenService = new RefreshTokenService(refreshTokenRepository, PEPPER, EXPIRATION);
    }

    @Test
    @DisplayName("Should issue a new refresh token")
    void shouldIssueNewRefreshToken() {
        AuthUser user = userWithId();

        when(refreshTokenRepository.save(any(RefreshToken.class))).thenAnswer(invocation -> {
            RefreshToken token = invocation.getArgument(0);
            org.springframework.test.util.ReflectionTestUtils.setField(token, "id", UUID.randomUUID());
            return token;
        });

        RefreshTokenService.IssuedRefreshToken issuedToken = refreshTokenService.issue(user);

        assertThat(issuedToken.rawToken()).isNotBlank();
        assertThat(issuedToken.userId()).isEqualTo(user.getId());
        assertThat(issuedToken.expiresAt()).isAfter(Instant.now());

        verify(refreshTokenRepository).save(any(RefreshToken.class));
    }

    @Test
    @DisplayName("Should rotate a valid refresh token")
    void shouldRotateValidRefreshToken() {
        String oldRawToken = "old-raw-token";
        UUID userId = UUID.randomUUID();
        UUID familyId = UUID.randomUUID();

        RefreshToken existingToken = new RefreshToken("old-hash", userId, familyId, Instant.now().plusSeconds(3600));
        org.springframework.test.util.ReflectionTestUtils.setField(existingToken, "id", UUID.randomUUID());

        when(refreshTokenRepository.findByTokenHash(anyString())).thenReturn(Optional.of(existingToken));
        when(refreshTokenRepository.save(any(RefreshToken.class))).thenAnswer(invocation -> {
            RefreshToken token = invocation.getArgument(0);
            org.springframework.test.util.ReflectionTestUtils.setField(token, "id", UUID.randomUUID());
            return token;
        });

        RefreshTokenService.IssuedRefreshToken issuedToken = refreshTokenService.rotate(oldRawToken);

        assertThat(issuedToken.rawToken()).isNotBlank();
        assertThat(issuedToken.userId()).isEqualTo(userId);
        assertThat(existingToken.isRevoked()).isTrue();
        assertThat(existingToken.getReplacedByTokenId()).isNotNull();

        verify(refreshTokenRepository).save(any(RefreshToken.class));
    }

    @Test
    @DisplayName("Should detect reuse and revoke family")
    void shouldDetectReuseAndRevokeFamily() {
        String reusedRawToken = "reused-raw-token";
        UUID familyId = UUID.randomUUID();

        RefreshToken existingToken = new RefreshToken("hash", UUID.randomUUID(), familyId, Instant.now().plusSeconds(3600));
        existingToken.revoke();

        when(refreshTokenRepository.findByTokenHash(anyString())).thenReturn(Optional.of(existingToken));

        assertThatThrownBy(() -> refreshTokenService.rotate(reusedRawToken))
                .isInstanceOf(BaseException.class)
                .hasFieldOrPropertyWithValue("errorCode", AuthErrorCode.REFRESH_TOKEN_REUSED);

        verify(refreshTokenRepository).revokeTokenFamily(eq(familyId), any(Instant.class));
    }

    @Test
    @DisplayName("Should throw exception for expired refresh token")
    void shouldThrowExceptionForExpiredToken() {
        String expiredRawToken = "expired-raw-token";
        RefreshToken existingToken = new RefreshToken("hash", UUID.randomUUID(), UUID.randomUUID(), Instant.now().minusSeconds(3600));

        when(refreshTokenRepository.findByTokenHash(anyString())).thenReturn(Optional.of(existingToken));

        assertThatThrownBy(() -> refreshTokenService.rotate(expiredRawToken))
                .isInstanceOf(BaseException.class)
                .hasFieldOrPropertyWithValue("errorCode", AuthErrorCode.REFRESH_TOKEN_EXPIRED);

        assertThat(existingToken.isRevoked()).isTrue();
    }

    @Test
    @DisplayName("Should revoke token on logout")
    void shouldRevokeTokenOnLogout() {
        String rawToken = "logout-token";
        RefreshToken token = new RefreshToken("hash", UUID.randomUUID(), UUID.randomUUID(), Instant.now().plusSeconds(3600));

        when(refreshTokenRepository.findByTokenHash(anyString())).thenReturn(Optional.of(token));

        refreshTokenService.revoke(rawToken);

        assertThat(token.isRevoked()).isTrue();
        verify(refreshTokenRepository).findByTokenHash(anyString());
    }

    @Test
    @DisplayName("Should revoke all tokens by user ID")
    void shouldRevokeAllTokensByUserId() {
        UUID userId = UUID.randomUUID();
        when(refreshTokenRepository.revokeAllActiveTokensByUserId(eq(userId), any(Instant.class))).thenReturn(5);

        refreshTokenService.revokeAllByUserId(userId);

        verify(refreshTokenRepository).revokeAllActiveTokensByUserId(eq(userId), any(Instant.class));
    }

    private AuthUser userWithId() {
        AuthUser user = new AuthUser("test@example.com", "hash", Set.of("CUSTOMER"));
        org.springframework.test.util.ReflectionTestUtils.setField(user, "id", UUID.randomUUID());
        return user;
    }
}
