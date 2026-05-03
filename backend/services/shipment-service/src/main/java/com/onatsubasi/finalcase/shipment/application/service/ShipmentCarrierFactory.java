package com.onatsubasi.finalcase.shipment.application.service;

import com.onatsubasi.finalcase.common.core.exception.BaseException;
import com.onatsubasi.finalcase.shipment.application.port.ShipmentCarrierPort;
import com.onatsubasi.finalcase.shipment.domain.enums.ShipmentCarrier;
import com.onatsubasi.finalcase.shipment.domain.exception.ShipmentErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class ShipmentCarrierFactory {

    private final List<ShipmentCarrierPort> carriers;

    public ShipmentCarrierPort getCarrier(ShipmentCarrier carrier) {
        return carriers.stream()
                .filter(port -> port.carrier() == carrier)
                .findFirst()
                .orElseThrow(() -> {
                    log.warn(
                            "event=shipment.carrier_not_supported carrier={}",
                            carrier
                    );

                    return new BaseException(
                            ShipmentErrorCode.SHIPMENT_CARRIER_NOT_SUPPORTED
                    );
                });
    }
}