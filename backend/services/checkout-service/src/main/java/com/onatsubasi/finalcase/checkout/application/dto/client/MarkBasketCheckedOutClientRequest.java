package com.onatsubasi.finalcase.checkout.application.dto.client;

import java.util.UUID;

public record MarkBasketCheckedOutClientRequest(
        UUID orderId
) {
}
