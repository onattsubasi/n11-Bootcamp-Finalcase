package com.onatsubasi.finalcase.user.infrastructure.messaging.payload;

import com.onatsubasi.finalcase.user.domain.enums.AddressType;
import com.onatsubasi.finalcase.user.domain.entity.UserAddress;

import java.time.Instant;
import java.util.UUID;

public record UserAddressPayload(
        UUID addressId,
        UUID userId,
        AddressType type,
        String city,
        String country,
        boolean defaultShipping,
        boolean defaultBilling,
        boolean deleted,
        Instant createdAt,
        Instant updatedAt
) {

    public static UserAddressPayload from(UserAddress address) {
        return new UserAddressPayload(
                address.getId(),
                address.getUserId(),
                address.getType(),
                address.getCity(),
                address.getCountry(),
                address.isDefaultShipping(),
                address.isDefaultBilling(),
                address.isDeleted(),
                address.getCreatedAt(),
                address.getUpdatedAt()
        );
    }
}
