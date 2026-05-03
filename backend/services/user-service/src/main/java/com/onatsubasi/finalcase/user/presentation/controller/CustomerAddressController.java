package com.onatsubasi.finalcase.user.presentation.controller;

import com.onatsubasi.finalcase.common.core.response.ApiResponse;
import com.onatsubasi.finalcase.common.security.CurrentUser;
import com.onatsubasi.finalcase.common.security.UserContext;
import com.onatsubasi.finalcase.user.application.dto.request.CreateAddressRequest;
import com.onatsubasi.finalcase.user.application.dto.request.UpdateAddressRequest;
import com.onatsubasi.finalcase.user.application.dto.response.UserAddressResponse;
import com.onatsubasi.finalcase.user.application.service.UserAddressService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/customer/addresses")
@Tag(name = "Customer Addresses", description = "Current customer address operations")
public class CustomerAddressController {

    private final UserAddressService userAddressService;

    @Operation(
            summary = "List my addresses",
            description = "Lists current customer's non-deleted addresses.",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @GetMapping
    public ResponseEntity<ApiResponse<List<UserAddressResponse>>> listMyAddresses(
            @CurrentUser UserContext userContext
    ) {
        return ResponseEntity.ok(
                ApiResponse.success(userAddressService.listMyAddresses(userContext))
        );
    }

    @Operation(
            summary = "Create address",
            description = "Creates a new address for current customer.",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @PostMapping
    public ResponseEntity<ApiResponse<UserAddressResponse>> createAddress(
            @CurrentUser UserContext userContext,
            @Valid @RequestBody CreateAddressRequest request
    ) {
        UserAddressResponse response = userAddressService.createAddress(userContext, request);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success("Address created successfully", response));
    }

    @Operation(
            summary = "Update address",
            description = "Updates an owned address.",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @PutMapping("/{addressId}")
    public ResponseEntity<ApiResponse<UserAddressResponse>> updateAddress(
            @CurrentUser UserContext userContext,
            @PathVariable UUID addressId,
            @Valid @RequestBody UpdateAddressRequest request
    ) {
        UserAddressResponse response = userAddressService.updateAddress(
                userContext,
                addressId,
                request
        );

        return ResponseEntity.ok(
                ApiResponse.success("Address updated successfully", response)
        );
    }

    @Operation(
            summary = "Delete address",
            description = "Soft-deletes an owned address.",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @DeleteMapping("/{addressId}")
    public ResponseEntity<ApiResponse<Void>> deleteAddress(
            @CurrentUser UserContext userContext,
            @PathVariable UUID addressId
    ) {
        userAddressService.deleteAddress(userContext, addressId);

        return ResponseEntity.ok(
                ApiResponse.success("Address deleted successfully")
        );
    }

    @Operation(
            summary = "Set default shipping address",
            description = "Marks an owned address as default shipping address.",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @PostMapping("/{addressId}/default-shipping")
    public ResponseEntity<ApiResponse<UserAddressResponse>> markDefaultShipping(
            @CurrentUser UserContext userContext,
            @PathVariable UUID addressId
    ) {
        UserAddressResponse response = userAddressService.markDefaultShipping(
                userContext,
                addressId
        );

        return ResponseEntity.ok(
                ApiResponse.success("Default shipping address updated successfully", response)
        );
    }

    @Operation(
            summary = "Set default billing address",
            description = "Marks an owned address as default billing address.",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @PostMapping("/{addressId}/default-billing")
    public ResponseEntity<ApiResponse<UserAddressResponse>> markDefaultBilling(
            @CurrentUser UserContext userContext,
            @PathVariable UUID addressId
    ) {
        UserAddressResponse response = userAddressService.markDefaultBilling(
                userContext,
                addressId
        );

        return ResponseEntity.ok(
                ApiResponse.success("Default billing address updated successfully", response)
        );
    }
}
