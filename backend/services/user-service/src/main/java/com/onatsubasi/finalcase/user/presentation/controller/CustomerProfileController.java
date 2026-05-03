package com.onatsubasi.finalcase.user.presentation.controller;

import com.onatsubasi.finalcase.common.core.response.ApiResponse;
import com.onatsubasi.finalcase.common.security.CurrentUser;
import com.onatsubasi.finalcase.common.security.UserContext;
import com.onatsubasi.finalcase.user.application.dto.request.UpdateUserProfileRequest;
import com.onatsubasi.finalcase.user.application.dto.response.UserProfileResponse;
import com.onatsubasi.finalcase.user.application.service.UserProfileService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/customer/profile")
@Tag(name = "Customer Profile", description = "Current customer profile operations")
public class CustomerProfileController {

    private final UserProfileService userProfileService;

    @Operation(
            summary = "Get my profile",
            description = "Returns current customer's profile. Creates a lazy profile if it does not exist.",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @GetMapping("/me")
    public ResponseEntity<ApiResponse<UserProfileResponse>> getMyProfile(
            @CurrentUser UserContext userContext
    ) {
        return ResponseEntity.ok(
                ApiResponse.success(userProfileService.getOrCreateMyProfile(userContext))
        );
    }

    @Operation(
            summary = "Update my profile",
            description = "Updates current customer's profile fields. Email ownership remains in Auth Service.",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @PutMapping("/me")
    public ResponseEntity<ApiResponse<UserProfileResponse>> updateMyProfile(
            @CurrentUser UserContext userContext,
            @Valid @RequestBody UpdateUserProfileRequest request
    ) {
        UserProfileResponse response = userProfileService.updateMyProfile(userContext, request);

        return ResponseEntity.ok(
                ApiResponse.success("Profile updated successfully", response)
        );
    }
}
