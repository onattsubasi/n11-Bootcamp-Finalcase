package com.onatsubasi.finalcase.order.application.dto.internal;


import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record OrderAddressSnapshotRequest(
        @NotBlank(message = "Recipient name is required")
        @Size(max = 150, message = "Recipient name cannot exceed 150 characters")
        String recipientName,

        @Size(max = 30, message = "Recipient phone cannot exceed 30 characters")
        String recipientPhone,

        @NotBlank(message = "Country is required")
        @Size(max = 100, message = "Country cannot exceed 100 characters")
        String country,

        @NotBlank(message = "City is required")
        @Size(max = 100, message = "City cannot exceed 100 characters")
        String city,

        @Size(max = 100, message = "District cannot exceed 100 characters")
        String district,

        @Size(max = 150, message = "Neighborhood cannot exceed 150 characters")
        String neighborhood,

        @NotBlank(message = "Address line 1 is required")
        @Size(max = 500, message = "Address line 1 cannot exceed 500 characters")
        String addressLine1,

        @Size(max = 500, message = "Address line 2 cannot exceed 500 characters")
        String addressLine2,

        @Size(max = 20, message = "Postal code cannot exceed 20 characters")
        String postalCode
) {
}