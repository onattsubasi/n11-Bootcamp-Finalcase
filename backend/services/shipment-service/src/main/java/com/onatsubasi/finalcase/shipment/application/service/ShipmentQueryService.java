package com.onatsubasi.finalcase.shipment.application.service;

import com.onatsubasi.finalcase.common.core.exception.BaseException;
import com.onatsubasi.finalcase.shipment.application.dto.response.ShipmentDetailResponse;
import com.onatsubasi.finalcase.shipment.application.dto.response.ShipmentSummaryResponse;
import com.onatsubasi.finalcase.shipment.domain.entity.Shipment;
import com.onatsubasi.finalcase.shipment.domain.exception.ShipmentErrorCode;
import com.onatsubasi.finalcase.shipment.domain.repository.ShipmentRepository;
import com.onatsubasi.finalcase.shipment.infrastructure.mapper.ShipmentMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class ShipmentQueryService {

    private final ShipmentRepository shipmentRepository;
    private final ShipmentMapper shipmentMapper;

    @Transactional(readOnly = true)
    public ShipmentDetailResponse getByIdForAdmin(UUID shipmentId) {
        Shipment shipment = shipmentRepository.findById(shipmentId)
                .orElseThrow(() -> new BaseException(ShipmentErrorCode.SHIPMENT_NOT_FOUND));

        return shipmentMapper.toDetailResponse(shipment);
    }

    @Transactional(readOnly = true)
    public ShipmentDetailResponse getByIdForCustomer(
            UUID shipmentId,
            UUID userId
    ) {
        Shipment shipment = shipmentRepository.findById(shipmentId)
                .orElseThrow(() -> new BaseException(ShipmentErrorCode.SHIPMENT_NOT_FOUND));

        if (!shipment.getUserId().equals(userId)) {
            throw new BaseException(ShipmentErrorCode.SHIPMENT_ACCESS_DENIED);
        }

        return shipmentMapper.toDetailResponse(shipment);
    }

    @Transactional(readOnly = true)
    public ShipmentDetailResponse getByOrderIdForInternal(UUID orderId) {
        Shipment shipment = shipmentRepository.findByOrderId(orderId)
                .orElseThrow(() -> new BaseException(ShipmentErrorCode.SHIPMENT_NOT_FOUND));

        return shipmentMapper.toDetailResponse(shipment);
    }

    @Transactional(readOnly = true)
    public Page<ShipmentSummaryResponse> getMyShipments(
            UUID userId,
            int page,
            int size
    ) {
        Pageable pageable = PageRequest.of(
                Math.max(page, 0),
                Math.min(Math.max(size, 1), 100),
                Sort.by(Sort.Direction.DESC, "createdAt")
        );

        return shipmentRepository.findByUserId(userId, pageable)
                .map(shipmentMapper::toSummaryResponse);
    }

    @Transactional(readOnly = true)
    public Page<ShipmentSummaryResponse> getAllShipments(
            int page,
            int size
    ) {
        Pageable pageable = PageRequest.of(
                Math.max(page, 0),
                Math.min(Math.max(size, 1), 100),
                Sort.by(Sort.Direction.DESC, "createdAt")
        );

        return shipmentRepository.findAll(pageable)
                .map(shipmentMapper::toSummaryResponse);
    }
}