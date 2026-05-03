package com.onatsubasi.finalcase.payment.application.dto.provider;

import lombok.Builder;

@Builder
public record ProviderBuyerInfo(
        String id,
        String name,
        String surname,
        String email,
        String phone,
        String identityNumber,
        String registrationAddress,
        String city,
        String country,
        String zipCode,
        String ip
) {
}