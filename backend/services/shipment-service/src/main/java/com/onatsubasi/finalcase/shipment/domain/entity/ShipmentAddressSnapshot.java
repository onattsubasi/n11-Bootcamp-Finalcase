package com.onatsubasi.finalcase.shipment.domain.entity;

import com.onatsubasi.finalcase.common.core.exception.BaseException;
import com.onatsubasi.finalcase.shipment.domain.exception.ShipmentErrorCode;
import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Embeddable
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ShipmentAddressSnapshot {

    @Column(name = "recipient_name", nullable = false, length = 150)
    private String recipientName;

    @Column(name = "recipient_phone", length = 30)
    private String recipientPhone;

    @Column(name = "country", nullable = false, length = 100)
    private String country;

    @Column(name = "city", nullable = false, length = 100)
    private String city;

    @Column(name = "district", length = 100)
    private String district;

    @Column(name = "neighborhood", length = 150)
    private String neighborhood;

    @Column(name = "address_line_1", nullable = false, length = 500)
    private String addressLine1;

    @Column(name = "address_line_2", length = 500)
    private String addressLine2;

    @Column(name = "postal_code", length = 20)
    private String postalCode;

    public ShipmentAddressSnapshot(
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
            throw new BaseException(ShipmentErrorCode.INVALID_SHIPMENT_ADDRESS, message);
        }
    }

    private String normalize(String value) {
        return value == null || value.isBlank()
                ? null
                : value.trim();
    }
}