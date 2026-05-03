package com.onatsubasi.finalcase.promotion.application.service;

import com.onatsubasi.finalcase.common.core.exception.BaseException;
import com.onatsubasi.finalcase.promotion.TestDataFactory;
import com.onatsubasi.finalcase.promotion.application.dto.internal.CancelPromotionUsageRequest;
import com.onatsubasi.finalcase.promotion.application.dto.internal.PromotionQuoteRequest;
import com.onatsubasi.finalcase.promotion.application.dto.internal.RedeemPromotionUsageRequest;
import com.onatsubasi.finalcase.promotion.application.dto.internal.ReservePromotionUsageRequest;
import com.onatsubasi.finalcase.promotion.application.dto.response.AppliedDiscountResponse;
import com.onatsubasi.finalcase.promotion.application.dto.response.PromotionQuoteResponse;
import com.onatsubasi.finalcase.promotion.application.dto.response.PromotionUsageReservationResponse;
import com.onatsubasi.finalcase.promotion.application.port.PromotionEventPublisher;
import com.onatsubasi.finalcase.promotion.domain.enums.PromotionType;
import com.onatsubasi.finalcase.promotion.domain.enums.PromotionUsageCancelReason;
import com.onatsubasi.finalcase.promotion.domain.enums.PromotionUsageReservationStatus;
import com.onatsubasi.finalcase.promotion.domain.exception.PromotionErrorCode;
import com.onatsubasi.finalcase.promotion.domain.entity.Promotion;
import com.onatsubasi.finalcase.promotion.domain.entity.PromotionUsageReservation;
import com.onatsubasi.finalcase.promotion.domain.repository.CouponAssignmentRepository;
import com.onatsubasi.finalcase.promotion.domain.repository.CouponRepository;
import com.onatsubasi.finalcase.promotion.domain.repository.PromotionRepository;
import com.onatsubasi.finalcase.promotion.domain.repository.PromotionUsageReservationRepository;
import com.onatsubasi.finalcase.promotion.infrastructure.config.PromotionUsageReservationProperties;
import com.onatsubasi.finalcase.promotion.infrastructure.mapper.PromotionMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PromotionUsageReservationServiceTest {

    @Mock
    private PromotionQuoteService quoteService;

    @Mock
    private PromotionRepository promotionRepository;

    @Mock
    private CouponRepository couponRepository;

    @Mock
    private CouponAssignmentRepository assignmentRepository;

    @Mock
    private PromotionUsageReservationRepository reservationRepository;

    @Mock
    private PromotionEventPublisher eventPublisher;

    private PromotionUsageReservationService reservationService;

    @BeforeEach
    void setUp() {
        PromotionUsageReservationProperties properties = new PromotionUsageReservationProperties();
        properties.setDefaultTimeoutMinutes(30);
        properties.setExpirationBatchSize(100);
        properties.setExpirationFixedDelayMs(60_000L);
        properties.setExpirationSchedulerEnabled(false);

        reservationService = new PromotionUsageReservationService(
                properties,
                quoteService,
                promotionRepository,
                couponRepository,
                assignmentRepository,
                reservationRepository,
                new PromotionMapper(),
                eventPublisher
        );
    }

    @Test
    void reserveCreatesReservationAndIncrementsPromotionCounter() {
        UUID promotionId = UUID.randomUUID();
        Promotion promotion = TestDataFactory.activePromotion(
                promotionId,
                PromotionType.FIXED_AMOUNT_DISCOUNT,
                TestDataFactory.fixedAmountConfig("100")
        );
        ReservePromotionUsageRequest request = reserveRequest(null);
        AppliedDiscountResponse discount = discount(promotionId, null);

        when(reservationRepository.findByIdempotencyKey("idem-1")).thenReturn(Optional.empty());
        when(quoteService.quote(any(PromotionQuoteRequest.class))).thenReturn(quoteResponse(request, discount));
        when(promotionRepository.findAllByIdsForUpdate(List.of(promotionId))).thenReturn(List.of(promotion));
        when(reservationRepository.countByUserIdAndPromotionIdAndStatuses(eq(request.userId()), eq(promotionId), any()))
                .thenReturn(0L);
        when(promotionRepository.save(any(Promotion.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(reservationRepository.save(any(PromotionUsageReservation.class))).thenAnswer(invocation -> {
            PromotionUsageReservation reservation = invocation.getArgument(0);
            ReflectionTestUtils.setField(reservation, "id", UUID.randomUUID());
            return reservation;
        });

        PromotionUsageReservationResponse response = reservationService.reserve("idem-1", request);

        assertThat(response.status()).isEqualTo(PromotionUsageReservationStatus.RESERVED);
        assertThat(response.items()).hasSize(1);
        assertThat(promotion.getReservedUsageCount()).isEqualTo(1);
        verify(eventPublisher).publishUsageReserved(any(PromotionUsageReservation.class));
    }

    @Test
    void reserveReturnsExistingReservationForSameIdempotencyKeyAndRequestHash() {
        ReservePromotionUsageRequest request = reserveRequest(null);
        String requestHash = ReflectionTestUtils.invokeMethod(reservationService, "requestHash", request);
        PromotionUsageReservation existing = PromotionUsageReservation.create(
                "idem-1",
                requestHash,
                request.checkoutId(),
                request.userId(),
                Instant.now().plusSeconds(1800)
        );
        ReflectionTestUtils.setField(existing, "id", UUID.randomUUID());
        when(reservationRepository.findByIdempotencyKey("idem-1")).thenReturn(Optional.of(existing));

        PromotionUsageReservationResponse response = reservationService.reserve("idem-1", request);

        assertThat(response.id()).isEqualTo(existing.getId());
        verify(quoteService, never()).quote(any());
        verify(eventPublisher, never()).publishUsageReserved(any());
    }

    @Test
    void reserveRejectsSameIdempotencyKeyWithDifferentPayload() {
        ReservePromotionUsageRequest request = reserveRequest(null);
        PromotionUsageReservation existing = PromotionUsageReservation.create(
                "idem-1",
                "different-hash",
                request.checkoutId(),
                request.userId(),
                Instant.now().plusSeconds(1800)
        );
        when(reservationRepository.findByIdempotencyKey("idem-1")).thenReturn(Optional.of(existing));

        assertThatThrownBy(() -> reservationService.reserve("idem-1", request))
                .isInstanceOf(BaseException.class)
                .extracting(ex -> ((BaseException) ex).getErrorCode())
                .isEqualTo(PromotionErrorCode.IDEMPOTENCY_KEY_REUSED_WITH_DIFFERENT_PAYLOAD);
    }

    @Test
    void cancelReleasesPromotionCounterAndPublishesCancellationEvent() {
        UUID promotionId = UUID.randomUUID();
        Promotion promotion = TestDataFactory.activePromotion(
                promotionId,
                PromotionType.FIXED_AMOUNT_DISCOUNT,
                TestDataFactory.fixedAmountConfig("100")
        );
        promotion.reserveUsage();
        PromotionUsageReservation reservation = reservedReservation(promotionId);
        when(reservationRepository.findByIdForUpdate(reservation.getId())).thenReturn(Optional.of(reservation));
        when(promotionRepository.findByIdForUpdate(promotionId)).thenReturn(Optional.of(promotion));
        when(promotionRepository.save(any(Promotion.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(reservationRepository.save(any(PromotionUsageReservation.class))).thenAnswer(invocation -> invocation.getArgument(0));

        PromotionUsageReservationResponse response = reservationService.cancel(
                reservation.getId(),
                new CancelPromotionUsageRequest(PromotionUsageCancelReason.PAYMENT_FAILED)
        );

        assertThat(response.status()).isEqualTo(PromotionUsageReservationStatus.CANCELLED);
        assertThat(promotion.getReservedUsageCount()).isZero();
        verify(eventPublisher).publishUsageCancelled(reservation);
    }

    @Test
    void redeemMovesPromotionCounterFromReservedToRedeemed() {
        UUID promotionId = UUID.randomUUID();
        UUID orderId = UUID.randomUUID();
        Promotion promotion = TestDataFactory.activePromotion(
                promotionId,
                PromotionType.FIXED_AMOUNT_DISCOUNT,
                TestDataFactory.fixedAmountConfig("100")
        );
        promotion.reserveUsage();
        PromotionUsageReservation reservation = reservedReservation(promotionId);
        when(reservationRepository.findByIdForUpdate(reservation.getId())).thenReturn(Optional.of(reservation));
        when(promotionRepository.findByIdForUpdate(promotionId)).thenReturn(Optional.of(promotion));
        when(promotionRepository.save(any(Promotion.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(reservationRepository.save(any(PromotionUsageReservation.class))).thenAnswer(invocation -> invocation.getArgument(0));

        PromotionUsageReservationResponse response = reservationService.redeem(
                reservation.getId(),
                new RedeemPromotionUsageRequest(orderId)
        );

        assertThat(response.status()).isEqualTo(PromotionUsageReservationStatus.REDEEMED);
        assertThat(response.orderId()).isEqualTo(orderId);
        assertThat(promotion.getReservedUsageCount()).isZero();
        assertThat(promotion.getRedeemedUsageCount()).isEqualTo(1);
        verify(eventPublisher).publishUsageRedeemed(reservation);
    }

    private ReservePromotionUsageRequest reserveRequest(List<UUID> selectedPromotionIds) {
        return new ReservePromotionUsageRequest(
                UUID.randomUUID(),
                UUID.randomUUID(),
                null,
                selectedPromotionIds,
                List.of(TestDataFactory.line(UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID(), "1000", 1)),
                BigDecimal.ZERO,
                "TRY"
        );
    }

    private PromotionQuoteResponse quoteResponse(ReservePromotionUsageRequest request, AppliedDiscountResponse discount) {
        return new PromotionQuoteResponse(
                request.userId(),
                request.couponCode(),
                new BigDecimal("1000.00"),
                BigDecimal.ZERO.setScale(2),
                discount.totalDiscountAmount(),
                BigDecimal.ZERO.setScale(2),
                new BigDecimal("900.00"),
                "TRY",
                List.of(discount),
                List.of(discount),
                List.of(),
                Instant.now()
        );
    }

    private AppliedDiscountResponse discount(UUID promotionId, UUID couponId) {
        return new AppliedDiscountResponse(
                promotionId,
                couponId,
                null,
                null,
                PromotionType.FIXED_AMOUNT_DISCOUNT,
                "100 TRY off",
                new BigDecimal("100.00"),
                BigDecimal.ZERO.setScale(2),
                new BigDecimal("100.00")
        );
    }

    private PromotionUsageReservation reservedReservation(UUID promotionId) {
        PromotionUsageReservation reservation = PromotionUsageReservation.create(
                "idem-1",
                "hash-1",
                UUID.randomUUID(),
                UUID.randomUUID(),
                Instant.now().plusSeconds(1800)
        );
        ReflectionTestUtils.setField(reservation, "id", UUID.randomUUID());
        reservation.addItem(com.onatsubasi.finalcase.promotion.domain.entity.PromotionUsageReservationItem.create(
                promotionId,
                null,
                null,
                null,
                new BigDecimal("100.00"),
                BigDecimal.ZERO,
                "100 TRY off"
        ));
        return reservation;
    }
}
