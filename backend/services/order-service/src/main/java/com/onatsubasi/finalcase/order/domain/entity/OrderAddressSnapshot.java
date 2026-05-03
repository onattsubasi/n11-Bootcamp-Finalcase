package com.onatsubasi.finalcase.order.domain.entity;

import com.onatsubasi.finalcase.common.core.exception.BaseException;
import com.onatsubasi.finalcase.order.domain.exception.OrderErrorCode;
import jakarta.persistence.*;
import lombok.Getter;


@Embeddable
public class OrderAddressSnapshot {

    @Getter
    @Column(name = "recipient_name", length = 150)
    private String recipientName;

    @Getter
    @Column(name = "recipient_phone", length = 30)
    private String recipientPhone;

    @Getter
    @Column(name = "country", length = 100)
    private String country;

    @Getter
    @Column(name = "city", length = 100)
    private String city;

    @Getter
    @Column(name = "district", length = 100)
    private String district;

    @Getter
    @Column(name = "neighborhood", length = 150)
    private String neighborhood;

    @Getter
    @Column(name = "address_line_1", length = 500)
    private String addressLine1;

    @Getter
    @Column(name = "address_line_2", length = 500)
    private String addressLine2;

    @Getter
    @Column(name = "postal_code", length = 20)
    private String postalCode;

    protected OrderAddressSnapshot() {
    }

    public OrderAddressSnapshot(
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
        validateRequired(recipientName, "Recipient name is required");
        validateRequired(country, "Country is required");
        validateRequired(city, "City is required");
        validateRequired(addressLine1, "Address line 1 is required");

        this.recipientName = recipientName.trim();
        this.recipientPhone = normalize(recipientPhone);
        this.country = country.trim();
        this.city = city.trim();
        this.district = normalize(district);
        this.neighborhood = normalize(neighborhood);
        this.addressLine1 = addressLine1.trim();
        this.addressLine2 = normalize(addressLine2);
        this.postalCode = normalize(postalCode);
    }

    private void validateRequired(String value, String message) {
        if (value == null || value.isBlank()) {
            throw new BaseException(OrderErrorCode.INVALID_ORDER_ADDRESS, message);
        }
    }

    private String normalize(String value) {
        return value == null || value.isBlank()
                ? null
                : value.trim();
    }
}
