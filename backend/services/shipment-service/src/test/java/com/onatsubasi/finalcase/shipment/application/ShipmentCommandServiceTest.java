package com.onatsubasi.finalcase.shipment.application;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.onatsubasi.finalcase.common.core.exception.BaseException;
import com.onatsubasi.finalcase.shipment.application.dto.request.CreateShipmentForOrderRequest;
import com.onatsubasi.finalcase.shipment.application.dto.response.ShipmentDetailResponse;
import com.onatsubasi.finalcase.shipment.application.port.ShipmentCarrierPort;
import com.onatsubasi.finalcase.shipment.application.port.ShipmentEventPublisher;
import com.onatsubasi.finalcase.shipment.application.service.*;
import com.onatsubasi.finalcase.shipment.domain.enums.ShipmentCarrier;
import com.onatsubasi.finalcase.shipment.domain.enums.ShipmentStatus;
import com.onatsubasi.finalcase.shipment.domain.exception.ShipmentErrorCode;
import com.onatsubasi.finalcase.shipment.domain.entity.Shipment;
import com.onatsubasi.finalcase.shipment.domain.entity.ShipmentIdempotencyRecord;
import com.onatsubasi.finalcase.shipment.domain.repository.ShipmentRepository;
import com.onatsubasi.finalcase.shipment.infrastructure.config.ShipmentServiceProperties;
import com.onatsubasi.finalcase.shipment.infrastructure.mapper.ShipmentMapper;
import com.onatsubasi.finalcase.shipment.support.ShipmentTestData;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ShipmentCommandServiceTest {

    @Mock
    private ShipmentRepository shipmentRepository;
    @Mock
    private ShipmentIdempotencyService idempotencyService;
    @Mock
    private ShipmentNumberGenerator shipmentNumberGenerator;
    @Mock
    private ShipmentOrderGateway orderGateway;
    @Mock
    private ShipmentCarrierFactory carrierFactory;
    @Mock
    private ShipmentCarrierPort carrierPort;
    @Mock
    private ShipmentEventPublisher eventPublisher;

    private ShipmentCommandService service;
    private ShipmentMapper mapper;

    @BeforeEach
    void setUp() {
        mapper = new ShipmentMapper(new ObjectMapper());
        ShipmentServiceProperties properties = new ShipmentServiceProperties();
        properties.setDefaultCarrier(ShipmentCarrier.MANUAL);
        properties.setAutoMarkReadyToShip(true);

        service = new ShipmentCommandService(
                shipmentRepository,
                new ShipmentRequestHashService(new ObjectMapper()),
                idempotencyService,
                shipmentNumberGenerator,
                orderGateway,
                carrierFactory,
                properties,
                mapper,
                eventPublisher
        );
    }

    @Test
    void createShipmentRejectsOrderThatIsNotPaidYet() {
        String requestHash = new ShipmentRequestHashService(new ObjectMapper()).hash(ShipmentTestData.createRequest());
        ShipmentIdempotencyRecord record = ShipmentTestData.idempotencyRecord(requestHash);

        when(idempotencyService.getOrCreateForUpdate("idem-1", requestHash)).thenReturn(record);
        when(idempotencyService.getStoredShipmentResponse(record)).thenReturn(Optional.empty());
        when(shipmentRepository.findByOrderIdForUpdate(ShipmentTestData.ORDER_ID)).thenReturn(Optional.empty());
        when(orderGateway.getOrder(ShipmentTestData.ORDER_ID)).thenReturn(ShipmentTestData.orderWithStatus("PENDING_PAYMENT"));

        assertThatThrownBy(() -> service.createShipmentForOrder("idem-1", ShipmentTestData.createRequest()))
                .isInstanceOf(BaseException.class)
                .extracting("errorCode")
                .isEqualTo(ShipmentErrorCode.ORDER_NOT_READY_FOR_SHIPMENT);

        verifyNoInteractions(carrierFactory, carrierPort, eventPublisher);
    }

    @Test
    void createShipmentPersistsSnapshotCallsCarrierAndSyncsOrder() {
        CreateShipmentForOrderRequest request = ShipmentTestData.createRequest();
        String requestHash = new ShipmentRequestHashService(new ObjectMapper()).hash(request);
        ShipmentIdempotencyRecord record = ShipmentTestData.idempotencyRecord(requestHash);

        when(idempotencyService.getOrCreateForUpdate("idem-1", requestHash)).thenReturn(record);
        when(idempotencyService.getStoredShipmentResponse(record)).thenReturn(Optional.empty());
        when(shipmentRepository.findByOrderIdForUpdate(ShipmentTestData.ORDER_ID)).thenReturn(Optional.empty());
        when(orderGateway.getOrder(ShipmentTestData.ORDER_ID)).thenReturn(ShipmentTestData.paidOrder());
        when(shipmentNumberGenerator.generate()).thenReturn(ShipmentTestData.SHIPMENT_NUMBER);
        when(shipmentRepository.save(any(Shipment.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(carrierFactory.getCarrier(ShipmentCarrier.MANUAL)).thenReturn(carrierPort);
        when(carrierPort.createShipment(any())).thenReturn(ShipmentTestData.successfulCarrierResult());

        ShipmentDetailResponse response = service.createShipmentForOrder("idem-1", request);

        assertThat(response.orderId()).isEqualTo(ShipmentTestData.ORDER_ID);
        assertThat(response.status()).isEqualTo(ShipmentStatus.READY_TO_SHIP);
        assertThat(response.trackingNumber()).isEqualTo("TRK-1");

        verify(orderGateway).updateShipmentCreated(eq(ShipmentTestData.ORDER_ID), any());
        verify(eventPublisher).publishShipmentCreated(any(Shipment.class));
        verify(eventPublisher).publishShipmentReadyToShip(any(Shipment.class));
        verify(idempotencyService).storeShipmentResponse(eq(record), any(Shipment.class), any(ShipmentDetailResponse.class));
    }

    @Test
    void existingShipmentWithDifferentCarrierIsAConflict() {
        CreateShipmentForOrderRequest request = new CreateShipmentForOrderRequest(ShipmentTestData.ORDER_ID, ShipmentCarrier.MOCK);
        String requestHash = new ShipmentRequestHashService(new ObjectMapper()).hash(request);
        ShipmentIdempotencyRecord record = ShipmentTestData.idempotencyRecord(requestHash);

        when(idempotencyService.getOrCreateForUpdate("idem-1", requestHash)).thenReturn(record);
        when(idempotencyService.getStoredShipmentResponse(record)).thenReturn(Optional.empty());
        when(shipmentRepository.findByOrderIdForUpdate(ShipmentTestData.ORDER_ID)).thenReturn(Optional.of(ShipmentTestData.shipment()));

        assertThatThrownBy(() -> service.createShipmentForOrder("idem-1", request))
                .isInstanceOf(BaseException.class)
                .extracting("errorCode")
                .isEqualTo(ShipmentErrorCode.SHIPMENT_ALREADY_EXISTS);
    }

    @Test
    void statusChangeToShippedPublishesEventAndSyncsOrderOnce() {
        Shipment shipment = ShipmentTestData.shipment();
        shipment.markReadyToShip(com.onatsubasi.finalcase.shipment.domain.enums.ShipmentStatusChangeSource.SYSTEM, null, "ready");

        when(shipmentRepository.findByIdForUpdate(any())).thenReturn(Optional.of(shipment));
        when(shipmentRepository.save(any(Shipment.class))).thenAnswer(invocation -> invocation.getArgument(0));

        ShipmentDetailResponse response = service.changeStatus(
                java.util.UUID.randomUUID(),
                "admin",
                ShipmentTestData.statusRequest(ShipmentStatus.SHIPPED)
        );

        assertThat(response.status()).isEqualTo(ShipmentStatus.SHIPPED);
        verify(eventPublisher).publishShipmentShipped(shipment);
        verify(orderGateway).markOrderShipped(eq(ShipmentTestData.ORDER_ID), any());
    }
}
