package com.onatsubasi.finalcase.auth.application.service;

import com.onatsubasi.finalcase.auth.application.dto.request.ChangePasswordRequest;
import com.onatsubasi.finalcase.auth.application.dto.request.LoginRequest;
import com.onatsubasi.finalcase.auth.application.dto.request.RegisterRequest;
import com.onatsubasi.finalcase.auth.domain.entity.AuthUser;
import com.onatsubasi.finalcase.auth.domain.exception.AuthErrorCode;
import com.onatsubasi.finalcase.auth.domain.repository.AuthUserRepository;
import com.onatsubasi.finalcase.auth.infrastructure.messaging.AuthEventPublisher;
import com.onatsubasi.finalcase.common.core.exception.BaseException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;

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
class AuthServiceTest {

    @Mock
    private AuthUserRepository authUserRepository;
    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private JwtService jwtService;
    @Mock
    private RefreshTokenService refreshTokenService;
    @Mock
    private AuthenticationManager authenticationManager;
    @Mock
    private AuthEventPublisher authEventPublisher;

    @InjectMocks
    private AuthService authService;

    @Test
    @DisplayName("Should register a new user successfully")
    void shouldRegisterUserSuccessfully() {
        RegisterRequest request = new RegisterRequest("test@example.com", "password123");
        AuthUser savedUser = userWithId("test@example.com");

        when(authUserRepository.existsByEmail(anyString())).thenReturn(false);
        when(passwordEncoder.encode(anyString())).thenReturn("encodedPassword");
        when(authUserRepository.saveAndFlush(any(AuthUser.class))).thenReturn(savedUser);
        when(jwtService.generateAccessToken(any(), any(), any())).thenReturn("access-token");
        when(jwtService.accessTokenExpiresInSeconds()).thenReturn(900L);
        when(refreshTokenService.issue(any())).thenReturn(
                new RefreshTokenService.IssuedRefreshToken("refresh-token", Instant.now().plusSeconds(3600),
                        savedUser.getId()));

        AuthService.LoginResult result = authService.register(request);

        assertThat(result.response().accessToken()).isEqualTo("access-token");
        assertThat(result.refreshToken()).isEqualTo("refresh-token");
        verify(authUserRepository).saveAndFlush(any(AuthUser.class));
        verify(authEventPublisher).publishUserRegisteredAfterCommit(
                eq(savedUser.getId()),
                eq("test@example.com"),
                eq(savedUser.getRoles()));
    }

    @Test
    @DisplayName("Should throw exception when registering with existing email")
    void shouldThrowExceptionWhenEmailExists() {
        RegisterRequest request = new RegisterRequest("test@example.com", "password123");
        when(authUserRepository.existsByEmail(anyString())).thenReturn(true);

        assertThatThrownBy(() -> authService.register(request))
                .isInstanceOf(BaseException.class)
                .hasFieldOrPropertyWithValue("errorCode", AuthErrorCode.USER_ALREADY_EXISTS);
    }

    @Test
    @DisplayName("Should login successfully")
    void shouldLoginSuccessfully() {
        LoginRequest request = new LoginRequest("test@example.com", "password123");
        AuthUser user = userWithId("test@example.com");

        when(authUserRepository.findByEmail(anyString())).thenReturn(Optional.of(user));
        when(jwtService.generateAccessToken(any(), any(), any())).thenReturn("access-token");
        when(jwtService.accessTokenExpiresInSeconds()).thenReturn(900L);
        when(refreshTokenService.issue(any())).thenReturn(
                new RefreshTokenService.IssuedRefreshToken("refresh-token", Instant.now().plusSeconds(3600),
                        user.getId()));

        AuthService.LoginResult result = authService.login(request);

        assertThat(result.response().accessToken()).isEqualTo("access-token");
        verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
    }

    @Test
    @DisplayName("Should throw exception for invalid credentials")
    void shouldThrowExceptionForInvalidCredentials() {
        LoginRequest request = new LoginRequest("test@example.com", "wrong-password");
        when(authenticationManager.authenticate(any())).thenThrow(new BadCredentialsException("Bad credentials"));

        assertThatThrownBy(() -> authService.login(request))
                .isInstanceOf(BaseException.class)
                .hasFieldOrPropertyWithValue("errorCode", AuthErrorCode.INVALID_CREDENTIALS);
    }

    @Test
    @DisplayName("Should refresh token successfully")
    void shouldRefreshTokenSuccessfully() {
        String rawRefreshToken = "old-refresh-token";
        UUID userId = UUID.randomUUID();
        AuthUser user = userWithId("test@example.com", userId);

        when(refreshTokenService.rotate(anyString())).thenReturn(
                new RefreshTokenService.IssuedRefreshToken("new-refresh-token", Instant.now().plusSeconds(3600),
                        userId));
        when(authUserRepository.findById(userId)).thenReturn(Optional.of(user));
        when(jwtService.generateAccessToken(any(), any(), any())).thenReturn("new-access-token");
        when(jwtService.accessTokenExpiresInSeconds()).thenReturn(900L);

        AuthService.LoginResult result = authService.refresh(rawRefreshToken);

        assertThat(result.response().accessToken()).isEqualTo("new-access-token");
        assertThat(result.refreshToken()).isEqualTo("new-refresh-token");
    }

    @Test
    @DisplayName("Should logout successfully")
    void shouldLogoutSuccessfully() {
        String rawRefreshToken = "token-to-revoke";

        authService.logout(rawRefreshToken);

        verify(refreshTokenService).revoke(rawRefreshToken);
    }

    @Test
    @DisplayName("Should change password and revoke all sessions")
    void shouldChangePasswordAndRevokeAllSessions() {
        UUID userId = UUID.randomUUID();
        AuthUser user = userWithId("test@example.com", userId);
        ChangePasswordRequest request = new ChangePasswordRequest("OldPassword123", "NewPassword123");

        when(authUserRepository.findById(userId)).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("OldPassword123", user.getPasswordHash())).thenReturn(true);
        when(passwordEncoder.encode("NewPassword123")).thenReturn("new-encoded-hash");

        authService.changePassword(userId, request);

        assertThat(user.getPasswordHash()).isEqualTo("new-encoded-hash");
        verify(refreshTokenService).revokeAllByUserId(userId);
    }

    private AuthUser userWithId(String email) {
        return userWithId(email, UUID.randomUUID());
    }

    private AuthUser userWithId(String email, UUID id) {
        AuthUser user = new AuthUser(email, "encodedPassword", Set.of("CUSTOMER"));
        org.springframework.test.util.ReflectionTestUtils.setField(user, "id", id);
        return user;
    }
}
