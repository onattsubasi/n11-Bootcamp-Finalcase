package com.onatsubasi.finalcase.inventory.domain.enums;

public enum StockMovementType {
    INITIAL_STOCK,

    ADMIN_INCREASE,
    ADMIN_DECREASE,
    ADMIN_SET,

    RESERVED,
    CONFIRMED_SOLD,
    RELEASED,
    EXPIRED_RELEASE,

    RETURNED,
    DAMAGED_REMOVED
}