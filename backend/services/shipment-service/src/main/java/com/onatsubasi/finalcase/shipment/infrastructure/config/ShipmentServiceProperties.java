package com.onatsubasi.finalcase.shipment.infrastructure.config;

import com.onatsubasi.finalcase.shipment.domain.enums.ShipmentCarrier;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter
@Setter
@ConfigurationProperties(prefix = "shipment")
public class ShipmentServiceProperties {

    private ShipmentCarrier defaultCarrier = ShipmentCarrier.MANUAL;

    private boolean autoMarkReadyToShip = true;
}