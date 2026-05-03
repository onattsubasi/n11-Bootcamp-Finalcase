package com.onatsubasi.finalcase.shipment.application;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.onatsubasi.finalcase.shipment.application.dto.request.CreateShipmentForOrderRequest;
import com.onatsubasi.finalcase.shipment.application.service.ShipmentRequestHashService;
import com.onatsubasi.finalcase.shipment.domain.enums.ShipmentCarrier;
import com.onatsubasi.finalcase.shipment.support.ShipmentTestData;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ShipmentRequestHashServiceTest {

    private final ShipmentRequestHashService service = new ShipmentRequestHashService(new ObjectMapper());

    @Test
    void samePayloadProducesSameHash() {
        String first = service.hash(ShipmentTestData.createRequest());
        String second = service.hash(ShipmentTestData.createRequest());

        assertThat(first).isEqualTo(second);
        assertThat(first).hasSize(64);
    }

    @Test
    void differentCarrierProducesDifferentHash() {
        String manual = service.hash(new CreateShipmentForOrderRequest(ShipmentTestData.ORDER_ID, ShipmentCarrier.MANUAL));
        String mock = service.hash(new CreateShipmentForOrderRequest(ShipmentTestData.ORDER_ID, ShipmentCarrier.MOCK));

        assertThat(manual).isNotEqualTo(mock);
    }
}
