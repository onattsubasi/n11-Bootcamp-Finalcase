package com.onatsubasi.finalcase.auth.presentation.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.onatsubasi.finalcase.auth.application.dto.request.ChangePasswordRequest;
import com.onatsubasi.finalcase.auth.application.dto.request.LoginRequest;
import com.onatsubasi.finalcase.auth.application.dto.request.RegisterRequest;
import com.onatsubasi.finalcase.auth.application.dto.response.AuthResponse;
import com.onatsubasi.finalcase.auth.application.dto.response.MeResponse;
import com.onatsubasi.finalcase.auth.application.service.AuthService;
import com.onatsubasi.finalcase.auth.infrastructure.security.CookieFactory;
import com.onatsubasi.finalcase.common.core.http.PlatformHeaders;
import com.onatsubasi.finalcase.common.security.UserContextWebMvcConfig;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseCookie;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.util.Set;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(
        controllers = AuthController.class,
        excludeFilters = @org.springframework.context.annotation.ComponentScan.Filter(
                type = org.springframework.context.annotation.FilterType.ASSIGNABLE_TYPE,
                classes = com.onatsubasi.finalcase.auth.infrastructure.config.SecurityConfig.class
        )
)
@Import(UserContextWebMvcConfig.class)
@AutoConfigureMockMvc(addFilters = false)
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private AuthService authService;

    @MockitoBean
    private CookieFactory cookieFactory;

    @Test
    @DisplayName("POST /api/auth/register - Should return 200 on success")
    void shouldRegisterSuccessfully() throws Exception {
        RegisterRequest request = new RegisterRequest("test@example.com", "password123");
        AuthResponse response = authResponse("test@example.com");
        AuthService.LoginResult result = new AuthService.LoginResult(response, "refresh-token", Instant.now().plusSeconds(3600));

        when(authService.register(any(RegisterRequest.class))).thenReturn(result);
        when(cookieFactory.createRefreshTokenCookie(anyString(), any(Instant.class)))
                .thenReturn(ResponseCookie.from("refresh_token", "refresh-token").build());

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.accessToken").value("access-token"))
                .andExpect(header().exists("Set-Cookie"));
    }

    @Test
    @DisplayName("POST /api/auth/login - Should return 200 on success")
    void shouldLoginSuccessfully() throws Exception {
        LoginRequest request = new LoginRequest("test@example.com", "password123");
        AuthResponse response = authResponse("test@example.com");
        AuthService.LoginResult result = new AuthService.LoginResult(response, "refresh-token", Instant.now().plusSeconds(3600));

        when(authService.login(any(LoginRequest.class))).thenReturn(result);
        when(cookieFactory.createRefreshTokenCookie(anyString(), any(Instant.class)))
                .thenReturn(ResponseCookie.from("refresh_token", "refresh-token").build());

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.accessToken").value("access-token"));
    }

    @Test
    @DisplayName("POST /api/auth/refresh - Should rotate refresh token")
    void shouldRefreshSuccessfully() throws Exception {
        AuthResponse response = authResponse("refresh@example.com");
        AuthService.LoginResult result = new AuthService.LoginResult(response, "new-refresh-token", Instant.now().plusSeconds(3600));

        when(authService.refresh("old-refresh-token")).thenReturn(result);
        when(cookieFactory.createRefreshTokenCookie(anyString(), any(Instant.class)))
                .thenReturn(ResponseCookie.from("refresh_token", "new-refresh-token").build());

        mockMvc.perform(post("/api/auth/refresh")
                        .cookie(new jakarta.servlet.http.Cookie("refresh_token", "old-refresh-token")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.accessToken").value("access-token"))
                .andExpect(header().exists("Set-Cookie"));
    }

    @Test
    @DisplayName("GET /api/auth/me - Should return authenticated account")
    void shouldReturnMe() throws Exception {
        UUID userId = UUID.randomUUID();
        when(authService.me(userId)).thenReturn(new MeResponse(userId, "me@example.com", Set.of("CUSTOMER")));

        mockMvc.perform(get("/api/auth/me")
                        .header(PlatformHeaders.X_USER_ID, userId.toString())
                        .header(PlatformHeaders.X_USER_EMAIL, "me@example.com")
                        .header(PlatformHeaders.X_USER_ROLES, "CUSTOMER"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.userId").value(userId.toString()))
                .andExpect(jsonPath("$.data.email").value("me@example.com"));
    }

    @Test
    @DisplayName("POST /api/auth/change-password - Should return 204")
    void shouldChangePassword() throws Exception {
        UUID userId = UUID.randomUUID();
        ChangePasswordRequest request = new ChangePasswordRequest("OldPassword123", "NewPassword123");

        when(cookieFactory.clearRefreshTokenCookie())
                .thenReturn(ResponseCookie.from("refresh_token", "").maxAge(0).build());

        mockMvc.perform(post("/api/auth/change-password")
                        .header(PlatformHeaders.X_USER_ID, userId.toString())
                        .header(PlatformHeaders.X_USER_EMAIL, "me@example.com")
                        .header(PlatformHeaders.X_USER_ROLES, "CUSTOMER")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNoContent())
                .andExpect(header().exists("Set-Cookie"));

        verify(authService).changePassword(eq(userId), any(ChangePasswordRequest.class));
    }

    @Test
    @DisplayName("POST /api/auth/register - Should return 400 for invalid email")
    void shouldReturn400ForInvalidEmail() throws Exception {
        RegisterRequest request = new RegisterRequest("invalid-email", "password123");

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    private AuthResponse authResponse(String email) {
        return new AuthResponse(
                "access-token",
                "Bearer",
                900L,
                UUID.randomUUID(),
                email,
                Set.of("CUSTOMER"),
                Instant.now()
        );
    }
}
