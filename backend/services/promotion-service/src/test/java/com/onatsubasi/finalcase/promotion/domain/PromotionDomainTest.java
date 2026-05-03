package com.onatsubasi.finalcase.promotion.domain;

import com.onatsubasi.finalcase.common.core.exception.BaseException;
import com.onatsubasi.finalcase.promotion.TestDataFactory;
import com.onatsubasi.finalcase.promotion.domain.enums.PromotionType;
import com.onatsubasi.finalcase.promotion.domain.enums.PromotionUsageCancelReason;
import com.onatsubasi.finalcase.promotion.domain.enums.PromotionUsageReservationStatus;
import com.onatsubasi.finalcase.promotion.domain.exception.PromotionErrorCode;
import com.onatsubasi.finalcase.promotion.domain.entity.Coupon;
import com.onatsubasi.finalcase.promotion.domain.entity.CouponAssignment;
import com.onatsubasi.finalcase.promotion.domain.entity.Promotion;
import com.onatsubasi.finalcase.promotion.domain.entity.PromotionUsageReservation;
import com.onatsubasi.finalcase.promotion.domain.entity.PromotionUsageReservationItem;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class PromotionDomainTest {

    @Test
    void promotionUsageCountersMoveThroughReserveRedeemAndRelease() {
        Promotion promotion = TestDataFactory.activePromotion(
                UUID.randomUUID(),
                PromotionType.PERCENTAGE_DISCOUNT,
                TestDataFactory.percentageConfig("10")
        );

        promotion.reserveUsage();
        assertThat(promotion.getReservedUsageCount()).isEqualTo(1);
        assertThat(promotion.getRedeemedUsageCount()).isZero();

        promotion.redeemReservedUsage();
        assertThat(promotion.getReservedUsageCount()).isZero();
        assertThat(promotion.getRedeemedUsageCount()).isEqualTo(1);

        promotion.reserveUsage();
        promotion.releaseReservedUsage();
        assertThat(promotion.getReservedUsageCount()).isZero();
        assertThat(promotion.getRedeemedUsageCount()).isEqualTo(1);
    }

    @Test
    void promotionRejectsUsageBeyondGlobalLimit() {
        Promotion promotion = Promotion.create(
                "Limited",
                null,
                PromotionType.FIXED_AMOUNT_DISCOUNT,
                false,
                false,
                1,
                TestDataFactory.fixedAmountConfig("50"),
                1,
                null,
                Instant.now().minusSeconds(60),
                Instant.now().plusSeconds(3600)
        );
        promotion.activate();
        promotion.reserveUsage();

        assertThatThrownBy(promotion::reserveUsage)
                .isInstanceOf(BaseException.class)
                .extracting(ex -> ((BaseException) ex).getErrorCode())
                .isEqualTo(PromotionErrorCode.PROMOTION_USAGE_LIMIT_EXCEEDED);
    }

    @Test
    void couponAssignmentCanBeReservedCancelledAndRedeemedOnlyFromReservedState() {
        Promotion promotion = TestDataFactory.activeCouponPromotion(
                UUID.randomUUID(),
                PromotionType.FIXED_AMOUNT_DISCOUNT,
                TestDataFactory.fixedAmountConfig("50")
        );
        Coupon coupon = TestDataFactory.activeCoupon(UUID.randomUUID(), "SAVE50", promotion);
        CouponAssignment assignment = TestDataFactory.assignedCoupon(UUID.randomUUID(), coupon, UUID.randomUUID());

        assertThat(assignment.reserve()).isTrue();
        assertThat(assignment.cancelReservation()).isTrue();
        assertThat(assignment.reserve()).isTrue();
        assertThat(assignment.redeem()).isTrue();

        assertThatThrownBy(assignment::cancelReservation)
                .isInstanceOf(BaseException.class)
                .extracting(ex -> ((BaseException) ex).getErrorCode())
                .isEqualTo(PromotionErrorCode.PROMOTION_USAGE_RESERVATION_CANCEL_BLOCKED);
    }

    @Test
    void usageReservationIsIdempotentForSameRedeemOrderAndRejectsCancellationAfterRedeem() {
        UUID orderId = UUID.randomUUID();
        PromotionUsageReservation reservation = PromotionUsageReservation.create(
                "checkout-123",
                "hash-123",
                UUID.randomUUID(),
                UUID.randomUUID(),
                Instant.now().plusSeconds(1800)
        );
        reservation.addItem(PromotionUsageReservationItem.create(
                UUID.randomUUID(),
                null,
                null,
                null,
                BigDecimal.TEN,
                BigDecimal.ZERO,
                "Test discount"
        ));

        assertThat(reservation.redeem(orderId)).isTrue();
        assertThat(reservation.redeem(orderId)).isFalse();
        assertThat(reservation.getStatus()).isEqualTo(PromotionUsageReservationStatus.REDEEMED);

        assertThatThrownBy(() -> reservation.cancel(PromotionUsageCancelReason.PAYMENT_FAILED))
                .isInstanceOf(BaseException.class)
                .extracting(ex -> ((BaseException) ex).getErrorCode())
                .isEqualTo(PromotionErrorCode.PROMOTION_USAGE_RESERVATION_CANCEL_BLOCKED);
    }
}
