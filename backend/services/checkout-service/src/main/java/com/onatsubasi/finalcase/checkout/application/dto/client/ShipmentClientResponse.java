package com.onatsubasi.finalcase.checkout.application.dto.client;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.UUID;

@JsonIgnoreProperties(ignoreUnknown = true)
public record ShipmentClientResponse(
        @JsonAlias("id")
        UUID shipmentId,
        String shipmentNumber,
        String carrier,
        String trackingNumber,
        String status
) {
}
