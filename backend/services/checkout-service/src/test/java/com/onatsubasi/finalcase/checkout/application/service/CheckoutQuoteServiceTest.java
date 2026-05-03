package com.onatsubasi.finalcase.checkout.application.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.onatsubasi.finalcase.checkout.application.dto.client.CatalogProductSnapshotsClientRequest;
import com.onatsubasi.finalcase.checkout.application.dto.client.PromotionQuoteClientRequest;
import com.onatsubasi.finalcase.checkout.application.dto.response.CheckoutQuoteResponse;
import com.onatsubasi.finalcase.checkout.domain.exception.CheckoutErrorCode;
import com.onatsubasi.finalcase.checkout.infrastructure.mapper.CheckoutMapper;
import com.onatsubasi.finalcase.checkout.support.CheckoutTestFixtures;
import com.onatsubasi.finalcase.common.core.exception.BaseException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CheckoutQuoteServiceTest {

    @Mock
    private CheckoutDownstreamGateway downstreamGateway;

    @Spy
    private CheckoutMapper checkoutMapper = new CheckoutMapper(new ObjectMapper());

    @InjectMocks
    private CheckoutQuoteService quoteService;

    @Test
    void quote_usesBasketCatalogAndPromotionThenReturnsReadOnlyTotals() {
        UUID userId = UUID.randomUUID();
        UUID basketId = UUID.randomUUID();
        UUID addressId = UUID.randomUUID();
        String productId = UUID.randomUUID().toString();

        when(downstreamGateway.getBasketSnapshot(basketId, userId))
                .thenReturn(CheckoutTestFixtures.basket(userId, basketId, productId, "500.00", 2));
        when(downstreamGateway.getProductSnapshots(any(CatalogProductSnapshotsClientRequest.class)))
                .thenReturn(List.of(CheckoutTestFixtures.product(productId, "500.00", true)));
        when(downstreamGateway.quotePromotion(any(PromotionQuoteClientRequest.class)))
                .thenReturn(CheckoutTestFixtures.noDiscountQuote("1000.00", "0.00"));

        CheckoutQuoteResponse response = quoteService.quote(
                CheckoutTestFixtures.customer(userId),
                CheckoutTestFixtures.quoteRequest(basketId, addressId)
        );

        assertThat(response.basketId()).isEqualTo(basketId);
        assertThat(response.items()).hasSize(1);
        assertThat(response.money().subtotalAmount()).isEqualByComparingTo("1000.00");
        assertThat(response.money().grandTotalAmount()).isEqualByComparingTo("1000.00");

        ArgumentCaptor<PromotionQuoteClientRequest> promotionCaptor = ArgumentCaptor.forClass(PromotionQuoteClientRequest.class);
        verify(downstreamGateway).quotePromotion(promotionCaptor.capture());
        assertThat(promotionCaptor.getValue().items()).hasSize(1);
        assertThat(promotionCaptor.getValue().subtotal()).isEqualByComparingTo("1000.00");
    }

    @Test
    void quote_rejectsEmptyBasketBeforeCallingCatalogOrPromotion() {
        UUID userId = UUID.randomUUID();
        UUID basketId = UUID.randomUUID();

        when(downstreamGateway.getBasketSnapshot(basketId, userId))
                .thenReturn(CheckoutTestFixtures.emptyBasket(userId, basketId));

        assertThatThrownBy(() -> quoteService.quote(
                CheckoutTestFixtures.customer(userId),
                CheckoutTestFixtures.quoteRequest(basketId, UUID.randomUUID())
        ))
                .isInstanceOf(BaseException.class)
                .extracting(ex -> ((BaseException) ex).getErrorCode())
                .isEqualTo(CheckoutErrorCode.CHECKOUT_BASKET_EMPTY);

        verify(downstreamGateway, never()).getProductSnapshots(any());
        verify(downstreamGateway, never()).quotePromotion(any());
    }

    @Test
    void quote_rejectsInactiveCatalogProductBeforePromotionQuote() {
        UUID userId = UUID.randomUUID();
        UUID basketId = UUID.randomUUID();
        String productId = UUID.randomUUID().toString();

        when(downstreamGateway.getBasketSnapshot(basketId, userId))
                .thenReturn(CheckoutTestFixtures.basket(userId, basketId, productId, "500.00", 1));
        when(downstreamGateway.getProductSnapshots(any(CatalogProductSnapshotsClientRequest.class)))
                .thenReturn(List.of(CheckoutTestFixtures.product(productId, "500.00", false)));

        assertThatThrownBy(() -> quoteService.quote(
                CheckoutTestFixtures.customer(userId),
                CheckoutTestFixtures.quoteRequest(basketId, UUID.randomUUID())
        ))
                .isInstanceOf(BaseException.class)
                .extracting(ex -> ((BaseException) ex).getErrorCode())
                .isEqualTo(CheckoutErrorCode.CHECKOUT_PRODUCT_NOT_SELLABLE);

        verify(downstreamGateway, never()).quotePromotion(any());
    }
}
