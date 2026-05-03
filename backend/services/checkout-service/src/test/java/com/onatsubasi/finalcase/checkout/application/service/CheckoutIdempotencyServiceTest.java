package com.onatsubasi.finalcase.checkout.application.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.onatsubasi.finalcase.checkout.application.dto.response.CheckoutSubmitResponse;
import com.onatsubasi.finalcase.checkout.domain.entity.CheckoutIdempotencyRecord;
import com.onatsubasi.finalcase.checkout.domain.exception.CheckoutErrorCode;
import com.onatsubasi.finalcase.checkout.domain.repository.CheckoutIdempotencyRecordRepository;
import com.onatsubasi.finalcase.checkout.support.CheckoutTestFixtures;
import com.onatsubasi.finalcase.common.core.exception.BaseException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CheckoutIdempotencyServiceTest {

    @Mock
    private CheckoutIdempotencyRecordRepository repository;

    @Spy
    private ObjectMapper objectMapper = new ObjectMapper();

    @InjectMocks
    private CheckoutIdempotencyService idempotencyService;

    @Test
    void getOrCreateForUpdate_rejectsMissingKey() {
        assertThatThrownBy(() -> idempotencyService.getOrCreateForUpdate(UUID.randomUUID(), " ", "hash"))
                .isInstanceOf(BaseException.class)
                .extracting(ex -> ((BaseException) ex).getErrorCode())
                .isEqualTo(CheckoutErrorCode.CHECKOUT_IDEMPOTENCY_KEY_REQUIRED);
    }

    @Test
    void getOrCreateForUpdate_reusesSameKeyOnlyForSameUserAndSameRequestHash() {
        UUID userId = UUID.randomUUID();
        CheckoutIdempotencyRecord existing = new CheckoutIdempotencyRecord(
                "idem-1",
                userId,
                "hash-1",
                Instant.now().plusSeconds(300)
        );
        when(repository.findByIdempotencyKeyForUpdate("idem-1")).thenReturn(Optional.of(existing));

        CheckoutIdempotencyRecord result = idempotencyService.getOrCreateForUpdate(userId, "idem-1", "hash-1");

        assertThat(result).isSameAs(existing);
    }

    @Test
    void getOrCreateForUpdate_rejectsSameKeyWithDifferentPayload() {
        UUID userId = UUID.randomUUID();
        CheckoutIdempotencyRecord existing = new CheckoutIdempotencyRecord(
                "idem-1",
                userId,
                "hash-1",
                Instant.now().plusSeconds(300)
        );
        when(repository.findByIdempotencyKeyForUpdate("idem-1")).thenReturn(Optional.of(existing));

        assertThatThrownBy(() -> idempotencyService.getOrCreateForUpdate(userId, "idem-1", "hash-2"))
                .isInstanceOf(BaseException.class)
                .extracting(ex -> ((BaseException) ex).getErrorCode())
                .isEqualTo(CheckoutErrorCode.CHECKOUT_IDEMPOTENCY_CONFLICT);
    }

    @Test
    void storeSubmitResponse_attachesCheckoutAndCanDeserializeStoredResponse() {
        UUID userId = UUID.randomUUID();
        UUID checkoutId = UUID.randomUUID();
        UUID orderId = UUID.randomUUID();
        UUID paymentId = UUID.randomUUID();
        CheckoutIdempotencyRecord record = new CheckoutIdempotencyRecord(
                "idem-1",
                userId,
                "hash-1",
                Instant.now().plusSeconds(300)
        );
        CheckoutSubmitResponse response = CheckoutTestFixtures.submitResponse(checkoutId, orderId, paymentId);
        when(repository.save(any(CheckoutIdempotencyRecord.class))).thenAnswer(invocation -> invocation.getArgument(0));

        idempotencyService.storeSubmitResponse(record, checkoutId, response);

        ArgumentCaptor<CheckoutIdempotencyRecord> captor = ArgumentCaptor.forClass(CheckoutIdempotencyRecord.class);
        verify(repository).save(captor.capture());
        CheckoutIdempotencyRecord saved = captor.getValue();
        assertThat(saved.getCheckoutSessionId()).isEqualTo(checkoutId);
        assertThat(saved.hasStoredResponse()).isTrue();

        Optional<CheckoutSubmitResponse> stored = idempotencyService.getStoredSubmitResponse(saved);
        assertThat(stored).isPresent();
        assertThat(stored.get().checkoutSessionId()).isEqualTo(checkoutId);
        assertThat(stored.get().orderId()).isEqualTo(orderId);
        assertThat(stored.get().paymentAction().paymentId()).isEqualTo(paymentId);
    }
}
