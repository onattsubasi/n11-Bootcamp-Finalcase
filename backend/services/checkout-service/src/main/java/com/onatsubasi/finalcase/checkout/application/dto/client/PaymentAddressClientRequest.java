package com.onatsubasi.finalcase.checkout.application.dto.client;

public record PaymentAddressClientRequest(
        String contactName,
        String city,
        String country,
        String address,
        String zipCode
) {
}
