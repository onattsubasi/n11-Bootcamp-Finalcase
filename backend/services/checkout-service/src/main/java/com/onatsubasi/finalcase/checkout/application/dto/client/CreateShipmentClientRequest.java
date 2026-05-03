package com.onatsubasi.finalcase.checkout.application.dto.client;

import java.util.List;
import java.util.UUID;

public record CreateShipmentClientRequest(
        UUID orderId,
        UUID userId,
        String orderNumber,
        OrderAddressSnapshotClientRequest shippingAddress,
        List<CreateShipmentItemClientRequest> items
) {
}