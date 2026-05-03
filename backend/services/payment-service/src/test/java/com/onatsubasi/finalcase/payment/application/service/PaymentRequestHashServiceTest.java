package com.onatsubasi.finalcase.payment.application.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class PaymentRequestHashServiceTest {

    private final PaymentRequestHashService hashService = new PaymentRequestHashService(new ObjectMapper());

    @Test
    void hashIsStableForSamePayloadAndDifferentForDifferentPayload() {
        String first = hashService.hash(new HashPayload("1", "100.00"));
        String same = hashService.hash(new HashPayload("1", "100.00"));
        String different = hashService.hash(new HashPayload("1", "101.00"));

        assertThat(first).hasSize(64);
        assertThat(first).isEqualTo(same);
        assertThat(first).isNotEqualTo(different);
    }

    private record HashPayload(String orderId, String amount) {
    }
}
