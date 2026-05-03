package com.onatsubasi.finalcase.shipment.domain.exception;

import com.onatsubasi.finalcase.common.core.exception.ErrorCode;

public enum ShipmentErrorCode implements ErrorCode {

    SHIPMENT_NOT_FOUND("SHIPMENT-001", "Shipment not found", 404),
    SHIPMENT_ACCESS_DENIED("SHIPMENT-002", "Shipment access denied", 403),
    SHIPMENT_ALREADY_EXISTS("SHIPMENT-003", "Shipment already exists for order", 409),

    SHIPMENT_IDEMPOTENCY_KEY_REQUIRED("SHIPMENT-004", "Idempotency-Key header is required", 400),
    SHIPMENT_IDEMPOTENCY_CONFLICT("SHIPMENT-005", "Idempotency key reused with different request payload", 409),

    SHIPMENT_INVALID_STATUS_TRANSITION("SHIPMENT-006", "Invalid shipment status transition", 409),
    SHIPMENT_ALREADY_DELIVERED("SHIPMENT-007", "Shipment is already delivered", 409),
    SHIPMENT_ALREADY_CANCELLED("SHIPMENT-008", "Shipment is already cancelled", 409),
    SHIPMENT_CANCEL_NOT_ALLOWED("SHIPMENT-009", "Shipment cannot be cancelled in current status", 409),

    SHIPMENT_NUMBER_GENERATION_FAILED("SHIPMENT-010", "Shipment number generation failed", 409),
    INVALID_SHIPMENT_DATA("SHIPMENT-011", "Invalid shipment data", 400),
    INVALID_SHIPMENT_ADDRESS("SHIPMENT-012", "Invalid shipment address snapshot", 400),
    INVALID_SHIPMENT_ITEM("SHIPMENT-013", "Invalid shipment item", 400),

    SHIPMENT_CARRIER_NOT_SUPPORTED("SHIPMENT-014", "Shipment carrier is not supported", 400),
    SHIPMENT_CARRIER_OPERATION_FAILED("SHIPMENT-015", "Shipment carrier operation failed", 502),

    ORDER_SERVICE_UNAVAILABLE("SHIPMENT-016", "Order service is temporarily unavailable", 503),
    ORDER_SYNC_FAILED("SHIPMENT-017", "Order shipment summary synchronization failed", 502),
    ORDER_NOT_READY_FOR_SHIPMENT("SHIPMENT-018", "Order is not ready for shipment creation", 409);

    private final String code;
    private final String defaultMessage;
    private final int httpStatus;

    ShipmentErrorCode(String code, String defaultMessage, int httpStatus) {
        this.code = code;
        this.defaultMessage = defaultMessage;
        this.httpStatus = httpStatus;
    }

    @Override
    public String code() {
        return code;
    }

    @Override
    public String defaultMessage() {
        return defaultMessage;
    }

    @Override
    public int httpStatus() {
        return httpStatus;
    }
}