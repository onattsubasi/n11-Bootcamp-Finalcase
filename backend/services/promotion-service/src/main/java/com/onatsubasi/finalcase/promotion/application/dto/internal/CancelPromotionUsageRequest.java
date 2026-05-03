package com.onatsubasi.finalcase.promotion.application.dto.internal;

import com.onatsubasi.finalcase.promotion.domain.enums.PromotionUsageCancelReason;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Internal request to cancel reserved promotion usage after payment/checkout failure")
public record CancelPromotionUsageRequest(
        PromotionUsageCancelReason reason
) {
}
