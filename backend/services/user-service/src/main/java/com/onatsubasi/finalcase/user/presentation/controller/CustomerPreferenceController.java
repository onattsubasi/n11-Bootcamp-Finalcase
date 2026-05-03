package com.onatsubasi.finalcase.user.presentation.controller;

import com.onatsubasi.finalcase.common.core.response.ApiResponse;
import com.onatsubasi.finalcase.common.security.CurrentUser;
import com.onatsubasi.finalcase.common.security.UserContext;
import com.onatsubasi.finalcase.user.application.dto.request.UpdateUserPreferenceRequest;
import com.onatsubasi.finalcase.user.application.dto.response.UserPreferenceResponse;
import com.onatsubasi.finalcase.user.application.service.UserPreferenceService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/customer/preferences")
@Tag(name = "Customer Preferences", description = "Current customer preference operations")
public class CustomerPreferenceController {

    private final UserPreferenceService userPreferenceService;

    @Operation(
            summary = "Get my preferences",
            description = "Returns current customer's preferences. Creates defaults if missing.",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @GetMapping("/me")
    public ResponseEntity<ApiResponse<UserPreferenceResponse>> getMyPreferences(
            @CurrentUser UserContext userContext
    ) {
        return ResponseEntity.ok(
                ApiResponse.success(userPreferenceService.getMyPreferences(userContext))
        );
    }

    @Operation(
            summary = "Update my preferences",
            description = "Updates current customer's language, currency and notification preferences.",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @PutMapping("/me")
    public ResponseEntity<ApiResponse<UserPreferenceResponse>> updateMyPreferences(
            @CurrentUser UserContext userContext,
            @Valid @RequestBody UpdateUserPreferenceRequest request
    ) {
        UserPreferenceResponse response = userPreferenceService.updateMyPreferences(
                userContext,
                request
        );

        return ResponseEntity.ok(
                ApiResponse.success("Preferences updated successfully", response)
        );
    }
}
