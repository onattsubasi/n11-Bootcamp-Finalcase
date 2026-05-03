package com.onatsubasi.finalcase.order.application.dto.response;

import com.onatsubasi.finalcase.order.domain.enums.OrderAddressType;

public record OrderAddressSnapshotResponse(
        OrderAddressType type,
        String recipientName,
        String recipientPhone,
        String country,
        String city,
        String district,
        String neighborhood,
        String addressLine1,
        String addressLine2,
        String postalCode
) {
}