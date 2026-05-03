package com.onatsubasi.finalcase.auth.application.service;

import com.onatsubasi.finalcase.auth.domain.entity.AuthUser;
import com.onatsubasi.finalcase.auth.domain.entity.RefreshToken;
import com.onatsubasi.finalcase.auth.domain.exception.AuthErrorCode;
import com.onatsubasi.finalcase.auth.domain.repository.RefreshTokenRepository;
import com.onatsubasi.finalcase.common.core.exception.BaseException;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.time.Instant;
import java.util.Base64;
import java.util.HexFormat;
import java.util.UUID;

@Service
@Slf4j
public class RefreshTokenService {

    private final RefreshTokenRepository refreshTokenRepository;
    private final SecureRandom secureRandom = new SecureRandom();
    private final String refreshTokenPepper;
    private final long refreshTokenExpirationSeconds;

    public RefreshTokenService(
            RefreshTokenRepository refreshTokenRepository,
            @Value("${auth.refresh-token.pepper}") String refreshTokenPepper,
            @Value("${auth.refresh-token.expiration-seconds}") long refreshTokenExpirationSeconds
    ) {
        if (refreshTokenPepper == null || refreshTokenPepper.isBlank()) {
            throw new IllegalStateException("REFRESH_TOKEN_PEPPER must be configured");
        }

        if (refreshTokenExpirationSeconds <= 0) {
            throw new IllegalStateException("Refresh token expiration must be greater than zero");
        }

        this.refreshTokenRepository = refreshTokenRepository;
        this.refreshTokenPepper = refreshTokenPepper;
        this.refreshTokenExpirationSeconds = refreshTokenExpirationSeconds;
    }

    @Transactional
    public IssuedRefreshToken issue(AuthUser user) {
        return issue(user, UUID.randomUUID());
    }

    @Transactional
    public IssuedRefreshToken rotate(String rawRefreshToken) {
        if (rawRefreshToken == null || rawRefreshToken.isBlank()) {
            MDC.put("eventName", "auth.refresh.failed");
            MDC.put("errorCode", AuthErrorCode.INVALID_REFRESH_TOKEN.code());
            log.warn("Refresh token rotation failed because cookie is missing or blank");
            throw new BaseException(AuthErrorCode.INVALID_REFRESH_TOKEN);
        }

        RefreshToken existingToken = refreshTokenRepository.findByTokenHash(hash(rawRefreshToken))
                .orElseThrow(() -> {
                    MDC.put("eventName", "auth.refresh.failed");
                    MDC.put("errorCode", AuthErrorCode.INVALID_REFRESH_TOKEN.code());
                    log.warn("Refresh token rotation failed because token hash was not found");
                    return new BaseException(AuthErrorCode.INVALID_REFRESH_TOKEN);
                });

        if (existingToken.isRevoked()) {
            refreshTokenRepository.revokeTokenFamily(existingToken.getFamilyId(), Instant.now());

            MDC.put("eventName", "auth.refresh.reuse_detected");
            MDC.put("errorCode", AuthErrorCode.REFRESH_TOKEN_REUSED.code());
            log.warn(
                    "Refresh token reuse detected for userId={}, familyId={}",
                    existingToken.getUserId(),
                    existingToken.getFamilyId()
            );

            throw new BaseException(AuthErrorCode.REFRESH_TOKEN_REUSED);
        }

        if (existingToken.isExpired()) {
            existingToken.revoke();

            MDC.put("eventName", "auth.refresh.failed");
            MDC.put("errorCode", AuthErrorCode.REFRESH_TOKEN_EXPIRED.code());
            log.warn("Refresh token rotation failed because token expired for userId={}", existingToken.getUserId());

            throw new BaseException(AuthErrorCode.REFRESH_TOKEN_EXPIRED);
        }

        String newRawToken = generateRawToken();

        RefreshToken newToken = new RefreshToken(
                hash(newRawToken),
                existingToken.getUserId(),
                existingToken.getFamilyId(),
                Instant.now().plusSeconds(refreshTokenExpirationSeconds)
        );

        RefreshToken savedNewToken = refreshTokenRepository.save(newToken);
        existingToken.replaceWith(savedNewToken.getId());

        MDC.put("eventName", "auth.refresh.rotated");
        log.info("Refresh token rotated for userId={}", savedNewToken.getUserId());

        return new IssuedRefreshToken(
                newRawToken,
                savedNewToken.getExpiresAt(),
                savedNewToken.getUserId()
        );
    }

    @Transactional
    public void revoke(String rawRefreshToken) {
        if (rawRefreshToken == null || rawRefreshToken.isBlank()) {
            MDC.put("eventName", "auth.logout.no_refresh_cookie");
            log.info("Logout requested without refresh token cookie");
            return;
        }

        refreshTokenRepository.findByTokenHash(hash(rawRefreshToken))
                .ifPresent(token -> {
                    token.revoke();
                    MDC.put("eventName", "auth.logout.refresh_revoked");
                    log.info("Refresh token revoked for userId={}", token.getUserId());
                });
    }

    @Transactional
    public void revokeAllByUserId(UUID userId) {
        if (userId == null) {
            return;
        }

        int revokedCount = refreshTokenRepository.revokeAllActiveTokensByUserId(userId, Instant.now());

        MDC.put("eventName", "auth.logout_all.succeeded");
        log.info("All active refresh tokens revoked for userId={}, revokedCount={}", userId, revokedCount);
    }

    private IssuedRefreshToken issue(AuthUser user, UUID familyId) {
        String rawToken = generateRawToken();

        RefreshToken refreshToken = new RefreshToken(
                hash(rawToken),
                user.getId(),
                familyId,
                Instant.now().plusSeconds(refreshTokenExpirationSeconds)
        );

        RefreshToken saved = refreshTokenRepository.save(refreshToken);

        MDC.put("eventName", "auth.refresh.issued");
        log.info("Refresh token issued for userId={}", saved.getUserId());

        return new IssuedRefreshToken(
                rawToken,
                saved.getExpiresAt(),
                user.getId()
        );
    }

    private String generateRawToken() {
        byte[] bytes = new byte[64];
        secureRandom.nextBytes(bytes);

        return Base64.getUrlEncoder()
                .withoutPadding()
                .encodeToString(bytes);
    }

    private String hash(String rawToken) {
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(
                    refreshTokenPepper.getBytes(StandardCharsets.UTF_8),
                    "HmacSHA256"
            ));

            byte[] digest = mac.doFinal(rawToken.getBytes(StandardCharsets.UTF_8));

            return HexFormat.of().formatHex(digest);
        } catch (Exception ex) {
            throw new IllegalStateException("Could not hash refresh token", ex);
        }
    }

    public record IssuedRefreshToken(
            String rawToken,
            Instant expiresAt,
            UUID userId
    ) {
    }
}
