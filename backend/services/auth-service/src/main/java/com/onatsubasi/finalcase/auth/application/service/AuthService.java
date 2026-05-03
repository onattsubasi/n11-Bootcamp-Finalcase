package com.onatsubasi.finalcase.auth.application.service;

import com.onatsubasi.finalcase.auth.application.dto.request.ChangePasswordRequest;
import com.onatsubasi.finalcase.auth.application.dto.request.LoginRequest;
import com.onatsubasi.finalcase.auth.application.dto.request.RegisterRequest;
import com.onatsubasi.finalcase.auth.application.dto.response.AuthResponse;
import com.onatsubasi.finalcase.auth.application.dto.response.MeResponse;
import com.onatsubasi.finalcase.auth.domain.entity.AuthUser;
import com.onatsubasi.finalcase.auth.domain.exception.AuthErrorCode;
import com.onatsubasi.finalcase.auth.domain.repository.AuthUserRepository;
import com.onatsubasi.finalcase.auth.infrastructure.messaging.AuthEventPublisher;
import com.onatsubasi.finalcase.common.core.exception.BaseException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService {

    private static final Set<String> DEFAULT_CUSTOMER_ROLE = Set.of("CUSTOMER");

    private final AuthUserRepository authUserRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final RefreshTokenService refreshTokenService;
    private final AuthenticationManager authenticationManager;
    private final AuthEventPublisher authEventPublisher;

    @Transactional
    public LoginResult register(RegisterRequest request) {
        try {
            MDC.put("eventName", "auth.register.started");
            log.info("Registration attempt received");

            String email = normalizeEmail(request.email());

            if (authUserRepository.existsByEmail(email)) {
                throw userAlreadyExists();
            }

            AuthUser user = new AuthUser(
                    email,
                    passwordEncoder.encode(request.password()),
                    DEFAULT_CUSTOMER_ROLE
            );

            AuthUser savedUser = authUserRepository.saveAndFlush(user);

            authEventPublisher.publishUserRegisteredAfterCommit(
                    savedUser.getId(),
                    savedUser.getEmail(),
                    savedUser.getRoles()
            );

            LoginResult result = issueLoginResult(savedUser);

            MDC.put("eventName", "auth.register.succeeded");
            log.info("Registration succeeded for accountId={}", savedUser.getId());

            return result;
        } catch (DataIntegrityViolationException ex) {
            throw userAlreadyExists();
        } catch (BaseException ex) {
            MDC.put("eventName", "auth.register.failed");
            MDC.put("errorCode", ex.getErrorCode().code());
            log.warn("Registration failed with errorCode={}", ex.getErrorCode().code());
            throw ex;
        } finally {
            clearAuthMdc();
        }
    }

    @Transactional
    public LoginResult login(LoginRequest request) {
        try {
            MDC.put("eventName", "auth.login.started");
            log.info("Login attempt received");

            String email = normalizeEmail(request.email());

            try {
                authenticationManager.authenticate(
                        new UsernamePasswordAuthenticationToken(email, request.password())
                );
            } catch (DisabledException ex) {
                throw new BaseException(AuthErrorCode.USER_DISABLED);
            } catch (BadCredentialsException ex) {
                throw new BaseException(AuthErrorCode.INVALID_CREDENTIALS);
            } catch (AuthenticationException ex) {
                throw new BaseException(AuthErrorCode.INVALID_CREDENTIALS);
            }

            AuthUser user = authUserRepository.findByEmail(email)
                    .orElseThrow(() -> new BaseException(AuthErrorCode.INVALID_CREDENTIALS));

            ensureUserCanAuthenticate(user);

            LoginResult result = issueLoginResult(user);

            MDC.put("eventName", "auth.login.succeeded");
            log.info("Login succeeded for accountId={}", user.getId());

            return result;
        } catch (BaseException ex) {
            MDC.put("eventName", "auth.login.failed");
            MDC.put("errorCode", ex.getErrorCode().code());
            log.warn("Login failed with errorCode={}", ex.getErrorCode().code());
            throw ex;
        } finally {
            clearAuthMdc();
        }
    }

    @Transactional
    public LoginResult refresh(String rawRefreshToken) {
        try {
            MDC.put("eventName", "auth.refresh.started");
            log.info("Refresh attempt received");

            RefreshTokenService.IssuedRefreshToken rotated = refreshTokenService.rotate(rawRefreshToken);

            AuthUser user = authUserRepository.findById(rotated.userId())
                    .orElseThrow(() -> new BaseException(AuthErrorCode.USER_NOT_FOUND));

            if (!user.isActive()) {
                refreshTokenService.revokeAllByUserId(rotated.userId());
                throw new BaseException(AuthErrorCode.USER_DISABLED);
            }

            AuthResponse response = createAuthResponse(user);

            MDC.put("eventName", "auth.refresh.succeeded");
            log.info("Refresh succeeded for accountId={}", user.getId());

            return new LoginResult(response, rotated.rawToken(), rotated.expiresAt());
        } catch (BaseException ex) {
            MDC.put("eventName", "auth.refresh.failed");
            MDC.put("errorCode", ex.getErrorCode().code());
            log.warn("Refresh failed with errorCode={}", ex.getErrorCode().code());
            throw ex;
        } finally {
            clearAuthMdc();
        }
    }

    @Transactional
    public void logout(String rawRefreshToken) {
        try {
            MDC.put("eventName", "auth.logout.started");
            log.info("Logout attempt received");

            refreshTokenService.revoke(rawRefreshToken);

            MDC.put("eventName", "auth.logout.succeeded");
            log.info("Logout completed");
        } finally {
            clearAuthMdc();
        }
    }

    @Transactional
    public void logoutAll(UUID userId) {
        try {
            MDC.put("eventName", "auth.logout_all.started");
            log.info("Logout-all attempt received for userId={}", userId);

            if (userId == null) {
                throw new BaseException(AuthErrorCode.AUTHENTICATION_REQUIRED);
            }

            refreshTokenService.revokeAllByUserId(userId);

            MDC.put("eventName", "auth.logout_all.succeeded");
            log.info("Logout-all completed for userId={}", userId);
        } finally {
            clearAuthMdc();
        }
    }

    @Transactional(readOnly = true)
    public MeResponse me(UUID userId) {
        if (userId == null) {
            throw new BaseException(AuthErrorCode.AUTHENTICATION_REQUIRED);
        }

        AuthUser user = authUserRepository.findById(userId)
                .orElseThrow(() -> new BaseException(AuthErrorCode.USER_NOT_FOUND));

        return new MeResponse(user.getId(), user.getEmail(), user.getRoles());
    }

    @Transactional
    public void changePassword(UUID userId, ChangePasswordRequest request) {
        try {
            MDC.put("eventName", "auth.password_change.started");
            log.info("Password change attempt received for userId={}", userId);

            if (userId == null) {
                throw new BaseException(AuthErrorCode.AUTHENTICATION_REQUIRED);
            }

            AuthUser user = authUserRepository.findById(userId)
                    .orElseThrow(() -> new BaseException(AuthErrorCode.USER_NOT_FOUND));

            ensureUserCanAuthenticate(user);

            if (!passwordEncoder.matches(request.currentPassword(), user.getPasswordHash())) {
                throw new BaseException(AuthErrorCode.CURRENT_PASSWORD_INVALID);
            }

            user.changePasswordHash(passwordEncoder.encode(request.newPassword()));
            refreshTokenService.revokeAllByUserId(userId);

            MDC.put("eventName", "auth.password_change.succeeded");
            log.info("Password changed and active refresh tokens revoked for userId={}", userId);
        } catch (BaseException ex) {
            MDC.put("eventName", "auth.password_change.failed");
            MDC.put("errorCode", ex.getErrorCode().code());
            log.warn("Password change failed with errorCode={}", ex.getErrorCode().code());
            throw ex;
        } finally {
            clearAuthMdc();
        }
    }

    private LoginResult issueLoginResult(AuthUser user) {
        AuthResponse response = createAuthResponse(user);
        RefreshTokenService.IssuedRefreshToken issuedRefreshToken = refreshTokenService.issue(user);

        return new LoginResult(
                response,
                issuedRefreshToken.rawToken(),
                issuedRefreshToken.expiresAt()
        );
    }

    private AuthResponse createAuthResponse(AuthUser user) {
        String accessToken = jwtService.generateAccessToken(
                user.getId(),
                user.getEmail(),
                user.getRoles()
        );

        return new AuthResponse(
                accessToken,
                "Bearer",
                jwtService.accessTokenExpiresInSeconds(),
                user.getId(),
                user.getEmail(),
                user.getRoles(),
                Instant.now()
        );
    }

    private void ensureUserCanAuthenticate(AuthUser user) {
        if (!user.isActive()) {
            throw new BaseException(AuthErrorCode.USER_DISABLED);
        }
    }

    private BaseException userAlreadyExists() {
        MDC.put("eventName", "auth.register.failed");
        MDC.put("errorCode", AuthErrorCode.USER_ALREADY_EXISTS.code());
        log.warn("Registration failed because account already exists");
        return new BaseException(AuthErrorCode.USER_ALREADY_EXISTS);
    }

    private String normalizeEmail(String email) {
        return email.trim().toLowerCase(Locale.ROOT);
    }

    private void clearAuthMdc() {
        MDC.remove("eventName");
        MDC.remove("errorCode");
    }

    public record LoginResult(
            AuthResponse response,
            String refreshToken,
            Instant refreshTokenExpiresAt
    ) {
    }
}
