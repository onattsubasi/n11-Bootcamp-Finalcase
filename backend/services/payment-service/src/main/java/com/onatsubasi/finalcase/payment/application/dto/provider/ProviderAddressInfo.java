package com.onatsubasi.finalcase.payment.application.dto.provider;

import lombok.Builder;

@Builder
public record ProviderAddressInfo(
        String contactName,
        String city,
        String country,
        String address,
        String zipCode
) {
}