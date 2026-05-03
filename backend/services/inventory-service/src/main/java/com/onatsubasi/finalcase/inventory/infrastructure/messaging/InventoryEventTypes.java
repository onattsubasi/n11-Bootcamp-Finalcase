package com.onatsubasi.finalcase.inventory.infrastructure.messaging;

public final class InventoryEventTypes {

    private InventoryEventTypes() {
    }

    public static final String STOCK_UPDATED = "inventory.stock.updated";
    public static final String STOCK_LOW = "inventory.stock.low";
    public static final String OUT_OF_STOCK = "inventory.out_of_stock";
    public static final String STOCK_BACK_IN_STOCK = "inventory.stock.back_in_stock";

    public static final String STOCK_RESERVED = "inventory.stock.reserved";
    public static final String RESERVATION_CONFIRMED = "inventory.reservation.confirmed";
    public static final String RESERVATION_RELEASED = "inventory.reservation.released";
    public static final String RESERVATION_EXPIRED = "inventory.reservation.expired";
}