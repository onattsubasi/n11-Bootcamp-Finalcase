package com.onatsubasi.finalcase.user.presentation.controller;

import com.onatsubasi.finalcase.common.core.response.ApiResponse;
import com.onatsubasi.finalcase.user.application.dto.internal.AddressSnapshotRequest;
import com.onatsubasi.finalcase.user.application.dto.internal.AddressSnapshotResponse;
import com.onatsubasi.finalcase.user.application.dto.response.UserProfileResponse;
import com.onatsubasi.finalcase.user.application.service.UserAddressService;
import com.onatsubasi.finalcase.user.application.service.UserProfileService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RequiredArgsConstructor
@RestController
@RequestMapping("/internal/users")
@Tag(name = "Internal Users", description = "Internal user endpoints for service-to-service calls")
public class InternalUserController {

    private final UserAddressService userAddressService;
    private final UserProfileService userProfileService;

    @Operation(
            summary = "Create address snapshots",
            description = "Returns immutable shipping and billing address snapshots for Checkout Service."
    )
    @PostMapping("/address-snapshots")
    public ResponseEntity<ApiResponse<AddressSnapshotResponse>> getAddressSnapshots(
            @Valid @RequestBody AddressSnapshotRequest request
    ) {
        AddressSnapshotResponse response = userAddressService.getAddressSnapshots(request);

        return ResponseEntity.ok(
                ApiResponse.success("Address snapshots created successfully", response)
        );
    }

    @Operation(
            summary = "Get profile by user id",
            description = "Internal profile lookup by user id."
    )
    @GetMapping("/{userId}/profile")
    public ResponseEntity<ApiResponse<UserProfileResponse>> getProfileByUserId(
            @PathVariable UUID userId
    ) {
        return ResponseEntity.ok(
                ApiResponse.success(userProfileService.getByUserId(userId))
        );
    }
}
