package com.onatsubasi.finalcase.auth.presentation.controller;

import com.onatsubasi.finalcase.auth.application.dto.request.ChangePasswordRequest;
import com.onatsubasi.finalcase.auth.application.dto.request.LoginRequest;
import com.onatsubasi.finalcase.auth.application.dto.request.RegisterRequest;
import com.onatsubasi.finalcase.auth.application.dto.response.AuthResponse;
import com.onatsubasi.finalcase.auth.application.dto.response.MeResponse;
import com.onatsubasi.finalcase.auth.application.service.AuthService;
import com.onatsubasi.finalcase.auth.infrastructure.security.CookieFactory;
import com.onatsubasi.finalcase.common.core.response.ApiResponse;
import com.onatsubasi.finalcase.common.security.CurrentUser;
import com.onatsubasi.finalcase.common.security.UserContext;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Tag(name = "Authentication", description = "Account authentication and token management")
public class AuthController {

    private final AuthService authService;
    private final CookieFactory cookieFactory;

    @PostMapping("/register")
    @Operation(
            summary = "Register new account",
            description = "Creates a CUSTOMER auth account, publishes auth.user.registered event, issues access token, and sets refresh token cookie. Profile data belongs to User Service."
    )
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Registration successful")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Invalid request")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "409", description = "User already exists")
    public ResponseEntity<ApiResponse<AuthResponse>> register(
            @Valid @RequestBody RegisterRequest request
    ) {
        AuthService.LoginResult result = authService.register(request);

        ResponseCookie refreshCookie = cookieFactory.createRefreshTokenCookie(
                result.refreshToken(),
                result.refreshTokenExpiresAt()
        );

        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, refreshCookie.toString())
                .body(ApiResponse.success("Registration successful", result.response()));
    }

    @PostMapping("/login")
    @Operation(
            summary = "Login account",
            description = "Authenticates credentials, issues access token, and sets rotated refresh token cookie."
    )
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Login successful")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Invalid request")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Invalid credentials")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "User disabled")
    public ResponseEntity<ApiResponse<AuthResponse>> login(
            @Valid @RequestBody LoginRequest request
    ) {
        AuthService.LoginResult result = authService.login(request);

        ResponseCookie refreshCookie = cookieFactory.createRefreshTokenCookie(
                result.refreshToken(),
                result.refreshTokenExpiresAt()
        );

        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, refreshCookie.toString())
                .body(ApiResponse.success("Login successful", result.response()));
    }

    @PostMapping("/refresh")
    @Operation(
            summary = "Refresh access token",
            description = "Rotates refresh token from HttpOnly cookie and returns a new access token."
    )
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Token refreshed successfully")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Invalid, expired, or reused refresh token")
    public ResponseEntity<ApiResponse<AuthResponse>> refresh(
            @CookieValue(name = "${auth.refresh-token.cookie-name:refresh_token}", required = false) String refreshToken
    ) {
        AuthService.LoginResult result = authService.refresh(refreshToken);

        ResponseCookie refreshCookie = cookieFactory.createRefreshTokenCookie(
                result.refreshToken(),
                result.refreshTokenExpiresAt()
        );

        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, refreshCookie.toString())
                .body(ApiResponse.success("Token refreshed successfully", result.response()));
    }

    @PostMapping("/logout")
    @Operation(
            summary = "Logout session",
            description = "Revokes current refresh token if present and clears refresh token cookie."
    )
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "204", description = "Logout successful")
    public ResponseEntity<Void> logout(
            @CookieValue(name = "${auth.refresh-token.cookie-name:refresh_token}", required = false) String refreshToken
    ) {
        authService.logout(refreshToken);

        return ResponseEntity.noContent()
                .header(HttpHeaders.SET_COOKIE, cookieFactory.clearRefreshTokenCookie().toString())
                .build();
    }

    @PostMapping("/logout-all")
    @Operation(
            summary = "Logout all sessions",
            description = "Revokes all active refresh tokens for the authenticated user.",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "204", description = "All sessions logged out")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthorized")
    public ResponseEntity<Void> logoutAll(
            @CurrentUser UserContext currentUser
    ) {
        authService.logoutAll(currentUser.userId());

        return ResponseEntity.noContent()
                .header(HttpHeaders.SET_COOKIE, cookieFactory.clearRefreshTokenCookie().toString())
                .build();
    }

    @GetMapping("/me")
    @Operation(
            summary = "Get authenticated account summary",
            description = "Returns the account identity represented by the Gateway-injected user context.",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Authenticated account returned")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthorized")
    public ResponseEntity<ApiResponse<MeResponse>> me(
            @CurrentUser UserContext currentUser
    ) {
        return ResponseEntity.ok(ApiResponse.success(authService.me(currentUser.userId())));
    }

    @PostMapping("/change-password")
    @Operation(
            summary = "Change password",
            description = "Validates current password, updates password hash, and revokes all active refresh tokens.",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "204", description = "Password changed")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthorized or invalid current password")
    public ResponseEntity<Void> changePassword(
            @CurrentUser UserContext currentUser,
            @Valid @RequestBody ChangePasswordRequest request
    ) {
        authService.changePassword(currentUser.userId(), request);

        return ResponseEntity.noContent()
                .header(HttpHeaders.SET_COOKIE, cookieFactory.clearRefreshTokenCookie().toString())
                .build();
    }
}
