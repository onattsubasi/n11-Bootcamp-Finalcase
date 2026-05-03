package com.onatsubasi.finalcase.user.application.dto.request;

import com.onatsubasi.finalcase.user.domain.enums.AddressType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

@Schema(description = "Request to update a user address")
public record UpdateAddressRequest(

        @NotBlank(message = "title is required")
        @Size(max = 100)
        String title,

        @NotNull(message = "type is required")
        AddressType type,

        @NotBlank(message = "recipientName is required")
        @Size(max = 150)
        String recipientName,

        @Size(max = 30)
        String phoneNumber,

        @NotBlank(message = "line1 is required")
        @Size(max = 500)
        String line1,

        @Size(max = 500)
        String line2,

        @NotBlank(message = "district is required")
        @Size(max = 100)
        String district,

        @NotBlank(message = "city is required")
        @Size(max = 100)
        String city,

        @NotBlank(message = "country is required")
        @Size(max = 100)
        String country,

        @Size(max = 20)
        String postalCode,

        boolean defaultShipping,

        boolean defaultBilling
) {
}