package com.onatsubasi.finalcase.review.application.port;

import com.onatsubasi.finalcase.review.application.dto.internal.VerifiedPurchaseResult;

import java.util.UUID;

public interface ReviewOrderGateway {

    VerifiedPurchaseResult verifyDeliveredPurchase(UUID userId, UUID productId);
}
