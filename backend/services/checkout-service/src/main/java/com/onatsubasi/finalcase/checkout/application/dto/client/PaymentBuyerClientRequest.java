package com.onatsubasi.finalcase.checkout.application.dto.client;

public record PaymentBuyerClientRequest(
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
