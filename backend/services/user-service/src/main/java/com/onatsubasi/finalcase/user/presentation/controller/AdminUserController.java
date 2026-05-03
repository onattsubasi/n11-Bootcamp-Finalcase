package com.onatsubasi.finalcase.user.presentation.controller;

import com.onatsubasi.finalcase.common.core.response.ApiResponse;
import com.onatsubasi.finalcase.user.application.dto.response.UserProfileResponse;
import com.onatsubasi.finalcase.user.application.service.UserProfileService;
import com.onatsubasi.finalcase.user.domain.enums.UserProfileStatus;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/admin/users")
@Tag(name = "Admin Users", description = "Admin user profile operations")
public class AdminUserController {

    private final UserProfileService userProfileService;

    @Operation(
            summary = "Get user profile",
            description = "Returns a user profile by user id.",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @GetMapping("/{userId}")
    public ResponseEntity<ApiResponse<UserProfileResponse>> getByUserId(
            @PathVariable UUID userId
    ) {
        return ResponseEntity.ok(
                ApiResponse.success(userProfileService.getByUserId(userId))
        );
    }

    @Operation(
            summary = "List user profiles",
            description = "Lists user profiles by optional status. Defaults to ACTIVE.",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @GetMapping
    public ResponseEntity<ApiResponse<List<UserProfileResponse>>> listByStatus(
            @RequestParam(required = false) UserProfileStatus status
    ) {
        return ResponseEntity.ok(
                ApiResponse.success(userProfileService.listByStatus(status))
        );
    }

    @Operation(
            summary = "Disable user profile",
            description = "Disables a user profile. Authentication/credentials remain owned by Auth Service.",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @PostMapping("/{userId}/disable")
    public ResponseEntity<ApiResponse<UserProfileResponse>> disableProfile(
            @PathVariable UUID userId
    ) {
        UserProfileResponse response = userProfileService.disableProfile(userId);

        return ResponseEntity.ok(
                ApiResponse.success("User profile disabled successfully", response)
        );
    }

    @Operation(
            summary = "Activate user profile",
            description = "Activates a disabled user profile.",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @PostMapping("/{userId}/activate")
    public ResponseEntity<ApiResponse<UserProfileResponse>> activateProfile(
            @PathVariable UUID userId
    ) {
        UserProfileResponse response = userProfileService.activateProfile(userId);

        return ResponseEntity.ok(
                ApiResponse.success("User profile activated successfully", response)
        );
    }

    @Operation(
            summary = "Delete user profile",
            description = "Soft-deletes a user profile. Authentication/credentials remain owned by Auth Service.",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @DeleteMapping("/{userId}")
    public ResponseEntity<ApiResponse<Void>> deleteProfile(
            @PathVariable UUID userId
    ) {
        userProfileService.deleteProfile(userId);

        return ResponseEntity.ok(
                ApiResponse.success("User profile deleted successfully")
        );
    }
}
