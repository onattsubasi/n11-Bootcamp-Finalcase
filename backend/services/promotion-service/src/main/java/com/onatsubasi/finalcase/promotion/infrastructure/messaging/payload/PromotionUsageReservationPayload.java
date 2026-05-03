package com.onatsubasi.finalcase.promotion.infrastructure.messaging.payload;

import com.onatsubasi.finalcase.promotion.domain.enums.PromotionUsageCancelReason;
import com.onatsubasi.finalcase.promotion.domain.enums.PromotionUsageReservationStatus;
import com.onatsubasi.finalcase.promotion.domain.entity.PromotionUsageReservation;
import com.onatsubasi.finalcase.promotion.domain.entity.PromotionUsageReservationItem;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;

public record PromotionUsageReservationPayload(
        UUID reservationId,
        String idempotencyKey,
        UUID checkoutId,
        UUID userId,
        UUID orderId,
        PromotionUsageReservationStatus status,
        Instant reservedUntil,
        Instant redeemedAt,
        Instant cancelledAt,
        Instant expiredAt,
        PromotionUsageCancelReason cancelReason,
        List<ItemPayload> items,
        Instant createdAt,
        Instant updatedAt
) {

    public record ItemPayload(
            UUID itemId,
            UUID promotionId,
            UUID couponId,
            UUID couponAssignmentId,
            String couponCode,
            BigDecimal discountAmount,
            BigDecimal shippingDiscountAmount,
            BigDecimal totalDiscountAmount,
            String description,
            Instant createdAt
    ) {

        public static ItemPayload from(PromotionUsageReservationItem item) {
            return new ItemPayload(
                    item.getId(),
                    item.getPromotionId(),
                    item.getCouponId(),
                    item.getCouponAssignmentId(),
                    item.getCouponCode(),
                    item.getDiscountAmount(),
                    item.getShippingDiscountAmount(),
                    item.totalDiscountAmount(),
                    item.getDescription(),
                    item.getCreatedAt()
            );
        }
    }

    public static PromotionUsageReservationPayload from(PromotionUsageReservation reservation) {
        List<ItemPayload> items = reservation.getItems()
                .stream()
                .sorted(Comparator.comparing(PromotionUsageReservationItem::getCreatedAt))
                .map(ItemPayload::from)
                .toList();

        return new PromotionUsageReservationPayload(
                reservation.getId(),
                reservation.getIdempotencyKey(),
                reservation.getCheckoutId(),
                reservation.getUserId(),
                reservation.getOrderId(),
                reservation.getStatus(),
                reservation.getReservedUntil(),
                reservation.getRedeemedAt(),
                reservation.getCancelledAt(),
                reservation.getExpiredAt(),
                reservation.getCancelReason(),
                items,
                reservation.getCreatedAt(),
                reservation.getUpdatedAt()
        );
    }
}
