package com.onatsubasi.finalcase.shipment.presentation;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.onatsubasi.finalcase.shipment.application.dto.response.ShipmentDetailResponse;
import com.onatsubasi.finalcase.shipment.application.service.ShipmentCommandService;
import com.onatsubasi.finalcase.shipment.application.service.ShipmentQueryService;
import com.onatsubasi.finalcase.shipment.domain.enums.ShipmentCarrier;
import com.onatsubasi.finalcase.shipment.domain.enums.ShipmentStatus;
import com.onatsubasi.finalcase.shipment.presentation.controller.InternalShipmentController;
import com.onatsubasi.finalcase.shipment.support.ShipmentTestData;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(InternalShipmentController.class)
@AutoConfigureMockMvc(addFilters = false)
class InternalShipmentControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private ShipmentCommandService shipmentCommandService;

    @MockitoBean
    private ShipmentQueryService shipmentQueryService;

    @Test
    void createShipmentSupportsCanonicalInternalRoute() throws Exception {
        ShipmentDetailResponse response = detailResponse();

        when(shipmentCommandService.createShipmentForOrder(eq("idem-1"), eq(ShipmentTestData.createRequest())))
                .thenReturn(response);

        mockMvc.perform(post("/internal/shipments")
                        .header("Idempotency-Key", "idem-1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(ShipmentTestData.createRequest())))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.orderId").value(ShipmentTestData.ORDER_ID.toString()))
                .andExpect(jsonPath("$.data.status").value("READY_TO_SHIP"));
    }

    @Test
    void createShipmentKeepsBackwardCompatibleOrdersAlias() throws Exception {
        when(shipmentCommandService.createShipmentForOrder(eq("idem-1"), eq(ShipmentTestData.createRequest())))
                .thenReturn(detailResponse());

        mockMvc.perform(post("/internal/shipments/orders")
                        .header("Idempotency-Key", "idem-1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(ShipmentTestData.createRequest())))
                .andExpect(status().isCreated());
    }

    @Test
    void getByOrderIdReturnsShipment() throws Exception {
        when(shipmentQueryService.getByOrderIdForInternal(ShipmentTestData.ORDER_ID))
                .thenReturn(detailResponse());

        mockMvc.perform(get("/internal/shipments/orders/{orderId}", ShipmentTestData.ORDER_ID))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.shipmentNumber").value(ShipmentTestData.SHIPMENT_NUMBER));
    }

    private ShipmentDetailResponse detailResponse() {
        return new ShipmentDetailResponse(
                java.util.UUID.randomUUID(),
                ShipmentTestData.SHIPMENT_NUMBER,
                ShipmentTestData.ORDER_ID,
                "ORD-20260503-000001",
                ShipmentTestData.USER_ID,
                ShipmentCarrier.MANUAL,
                ShipmentStatus.READY_TO_SHIP,
                "carrier-1",
                "TRK-1",
                "https://carrier.example/TRK-1",
                null,
                "READY",
                null,
                null,
                List.of(),
                List.of(),
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null
        );
    }
}
