package com.onatsubasi.finalcase.shipment.application;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.onatsubasi.finalcase.common.core.exception.BaseException;
import com.onatsubasi.finalcase.shipment.application.service.ShipmentQueryService;
import com.onatsubasi.finalcase.shipment.domain.exception.ShipmentErrorCode;
import com.onatsubasi.finalcase.shipment.domain.repository.ShipmentRepository;
import com.onatsubasi.finalcase.shipment.infrastructure.mapper.ShipmentMapper;
import com.onatsubasi.finalcase.shipment.support.ShipmentTestData;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ShipmentQueryServiceTest {

    @Mock
    private ShipmentRepository shipmentRepository;

    @Test
    void customerCanReadOnlyOwnShipment() {
        ShipmentQueryService service = new ShipmentQueryService(
                shipmentRepository,
                new ShipmentMapper(new ObjectMapper())
        );
        UUID shipmentId = UUID.randomUUID();
        when(shipmentRepository.findById(shipmentId)).thenReturn(Optional.of(ShipmentTestData.shipment()));

        assertThat(service.getByIdForCustomer(shipmentId, ShipmentTestData.USER_ID).orderId())
                .isEqualTo(ShipmentTestData.ORDER_ID);
    }

    @Test
    void customerCannotReadAnotherUsersShipment() {
        ShipmentQueryService service = new ShipmentQueryService(
                shipmentRepository,
                new ShipmentMapper(new ObjectMapper())
        );
        UUID shipmentId = UUID.randomUUID();
        when(shipmentRepository.findById(shipmentId)).thenReturn(Optional.of(ShipmentTestData.shipment()));

        assertThatThrownBy(() -> service.getByIdForCustomer(shipmentId, UUID.randomUUID()))
                .isInstanceOf(BaseException.class)
                .extracting("errorCode")
                .isEqualTo(ShipmentErrorCode.SHIPMENT_ACCESS_DENIED);
    }
}
