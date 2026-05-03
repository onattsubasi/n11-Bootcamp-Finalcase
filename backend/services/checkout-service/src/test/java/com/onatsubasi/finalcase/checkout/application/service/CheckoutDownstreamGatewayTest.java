package com.onatsubasi.finalcase.checkout.application.service;

import com.onatsubasi.finalcase.checkout.application.client.*;
import com.onatsubasi.finalcase.checkout.application.dto.client.*;
import com.onatsubasi.finalcase.checkout.domain.exception.CheckoutErrorCode;
import com.onatsubasi.finalcase.checkout.support.CheckoutTestFixtures;
import com.onatsubasi.finalcase.common.core.response.ApiResponse;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CheckoutDownstreamGatewayTest {

    @Mock
    private BasketClient basketClient;
    @Mock
    private CatalogClient catalogClient;
    @Mock
    private UserClient userClient;
    @Mock
    private InventoryClient inventoryClient;
    @Mock
    private PromotionClient promotionClient;
    @Mock
    private OrderClient orderClient;
    @Mock
    private PaymentClient paymentClient;
    @Mock
    private ShipmentClient shipmentClient;
    @Mock
    private CheckoutDownstreamResponseExtractor extractor;

    @InjectMocks
    private CheckoutDownstreamGateway gateway;

    @Test
    void reserveStock_sendsStableIdempotencyKeyDerivedFromCheckoutId() {
        UUID checkoutId = UUID.randomUUID();
        UUID reservationId = UUID.randomUUID();
        InventoryReserveClientRequest request = new InventoryReserveClientRequest(
                checkoutId,
                UUID.randomUUID(),
                UUID.randomUUID(),
                List.of(new InventoryReserveItemClientRequest(UUID.randomUUID().toString(), 2))
        );
        InventoryReservationClientResponse response = new InventoryReservationClientResponse(
                reservationId,
                "RESERVED",
                Instant.now().plusSeconds(1800)
        );
        ApiResponse<InventoryReservationClientResponse> apiResponse = ApiResponse.success(response);
        when(inventoryClient.reserveStock(eq("checkout:inventory-reserve:" + checkoutId), eq(request)))
                .thenReturn(apiResponse);
        when(extractor.extract(apiResponse, CheckoutErrorCode.DOWNSTREAM_INVENTORY_FAILED)).thenReturn(response);

        InventoryReservationClientResponse result = gateway.reserveStock(request);

        assertThat(result.reservationId()).isEqualTo(reservationId);
        verify(inventoryClient).reserveStock("checkout:inventory-reserve:" + checkoutId, request);
    }

    @Test
    void initializePayment_sendsStableIdempotencyKeyDerivedFromCheckoutId() {
        UUID checkoutId = UUID.randomUUID();
        UUID paymentId = UUID.randomUUID();
        PaymentInitializeClientRequest request = new PaymentInitializeClientRequest(
                checkoutId,
                UUID.randomUUID(),
                UUID.randomUUID(),
                CheckoutTestFixtures.money("1900.00"),
                "TRY",
                "IYZICO",
                "CHECKOUT_FORM",
                null,
                false
        );
        PaymentInitializeClientResponse response = new PaymentInitializeClientResponse(
                paymentId,
                "session-token",
                "https://pay.example.com",
                "IYZICO",
                "WAITING_PROVIDER_ACTION"
        );
        ApiResponse<PaymentInitializeClientResponse> apiResponse = ApiResponse.success(response);
        ArgumentCaptor<String> keyCaptor = ArgumentCaptor.forClass(String.class);
        when(paymentClient.initializePayment(eq("checkout:payment-initialize:" + checkoutId), eq(request)))
                .thenReturn(apiResponse);
        when(extractor.extract(apiResponse, CheckoutErrorCode.DOWNSTREAM_PAYMENT_FAILED)).thenReturn(response);

        PaymentInitializeClientResponse result = gateway.initializePayment(request);

        assertThat(result.paymentId()).isEqualTo(paymentId);
        verify(paymentClient).initializePayment(keyCaptor.capture(), eq(request));
        assertThat(keyCaptor.getValue()).isEqualTo("checkout:payment-initialize:" + checkoutId);
    }
}
