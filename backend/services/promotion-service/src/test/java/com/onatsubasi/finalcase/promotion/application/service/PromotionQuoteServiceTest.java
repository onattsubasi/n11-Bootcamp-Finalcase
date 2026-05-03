package com.onatsubasi.finalcase.promotion.application.service;

import com.onatsubasi.finalcase.common.core.exception.BaseException;
import com.onatsubasi.finalcase.promotion.TestDataFactory;
import com.onatsubasi.finalcase.promotion.application.dto.internal.PromotionQuoteRequest;
import com.onatsubasi.finalcase.promotion.application.dto.response.PromotionQuoteResponse;
import com.onatsubasi.finalcase.promotion.application.strategy.DiscountStrategyFactory;
import com.onatsubasi.finalcase.promotion.application.strategy.FixedAmountDiscountStrategy;
import com.onatsubasi.finalcase.promotion.application.strategy.PercentageDiscountStrategy;
import com.onatsubasi.finalcase.promotion.domain.enums.PromotionType;
import com.onatsubasi.finalcase.promotion.domain.exception.PromotionErrorCode;
import com.onatsubasi.finalcase.promotion.domain.entity.Coupon;
import com.onatsubasi.finalcase.promotion.domain.entity.Promotion;
import com.onatsubasi.finalcase.promotion.domain.repository.CouponAssignmentRepository;
import com.onatsubasi.finalcase.promotion.domain.repository.CouponRepository;
import com.onatsubasi.finalcase.promotion.domain.repository.PromotionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PromotionQuoteServiceTest {

    @Mock
    private PromotionRepository promotionRepository;

    @Mock
    private CouponRepository couponRepository;

    @Mock
    private CouponAssignmentRepository assignmentRepository;

    private PromotionQuoteService quoteService;

    @BeforeEach
    void setUp() {
        DiscountStrategyFactory strategyFactory = new DiscountStrategyFactory(List.of(
                new PercentageDiscountStrategy(),
                new FixedAmountDiscountStrategy()
        ));
        quoteService = new PromotionQuoteService(
                promotionRepository,
                couponRepository,
                assignmentRepository,
                strategyFactory
        );
    }

    @Test
    void quoteSelectsBestDiscountAndDoesNotMutateRepositories() {
        UUID userId = UUID.randomUUID();
        Promotion tenPercent = TestDataFactory.activePromotion(
                UUID.randomUUID(),
                PromotionType.PERCENTAGE_DISCOUNT,
                TestDataFactory.percentageConfig("10")
        );
        Promotion fixedTwoHundred = TestDataFactory.activePromotion(
                UUID.randomUUID(),
                PromotionType.FIXED_AMOUNT_DISCOUNT,
                TestDataFactory.fixedAmountConfig("200")
        );
        when(promotionRepository.findActivePromotionsAt(any(Instant.class)))
                .thenReturn(List.of(tenPercent, fixedTwoHundred));

        PromotionQuoteRequest request = new PromotionQuoteRequest(
                userId,
                null,
                List.of(TestDataFactory.line(UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID(), "1000", 1)),
                new BigDecimal("79.99"),
                "try"
        );

        PromotionQuoteResponse response = quoteService.quote(request);

        assertThat(response.userId()).isEqualTo(userId);
        assertThat(response.currency()).isEqualTo("TRY");
        assertThat(response.subtotal()).isEqualByComparingTo("1000.00");
        assertThat(response.shippingFee()).isEqualByComparingTo("79.99");
        assertThat(response.selectedDiscounts()).hasSize(1);
        assertThat(response.selectedDiscounts().get(0).promotionId()).isEqualTo(fixedTwoHundred.getId());
        assertThat(response.totalDiscountAmount()).isEqualByComparingTo("200.00");
        assertThat(response.payableAmount()).isEqualByComparingTo("879.99");
        verify(promotionRepository, never()).save(any());
    }

    @Test
    void quoteUsesCouponPromotionWhenCouponCodeIsProvided() {
        UUID userId = UUID.randomUUID();
        Promotion couponPromotion = TestDataFactory.activeCouponPromotion(
                UUID.randomUUID(),
                PromotionType.FIXED_AMOUNT_DISCOUNT,
                TestDataFactory.fixedAmountConfig("150")
        );
        Coupon coupon = TestDataFactory.activeCoupon(UUID.randomUUID(), "save150", couponPromotion);
        when(promotionRepository.findActivePromotionsAt(any(Instant.class))).thenReturn(List.of());
        when(couponRepository.findByCode("SAVE150")).thenReturn(Optional.of(coupon));
        when(assignmentRepository.findByCouponIdAndUserId(coupon.getId(), userId)).thenReturn(Optional.empty());

        PromotionQuoteResponse response = quoteService.quote(new PromotionQuoteRequest(
                userId,
                " save150 ",
                List.of(TestDataFactory.line(UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID(), "1000", 1)),
                BigDecimal.ZERO,
                "TRY"
        ));

        assertThat(response.couponCode()).isEqualTo("SAVE150");
        assertThat(response.selectedDiscounts()).hasSize(1);
        assertThat(response.selectedDiscounts().get(0).couponId()).isEqualTo(coupon.getId());
        assertThat(response.totalDiscountAmount()).isEqualByComparingTo("150.00");
    }

    @Test
    void quoteRejectsMissingItemsBeforeRepositoryCalls() {
        PromotionQuoteRequest request = new PromotionQuoteRequest(
                UUID.randomUUID(),
                null,
                List.of(),
                BigDecimal.ZERO,
                "TRY"
        );

        assertThatThrownBy(() -> quoteService.quote(request))
                .isInstanceOf(BaseException.class)
                .extracting(ex -> ((BaseException) ex).getErrorCode())
                .isEqualTo(PromotionErrorCode.PROMOTION_QUOTE_FAILED);
        verify(promotionRepository, never()).findActivePromotionsAt(any());
    }
}
