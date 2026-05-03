package com.onatsubasi.finalcase.inventory.application.port;

import com.onatsubasi.finalcase.inventory.domain.entity.InventoryItem;
import com.onatsubasi.finalcase.inventory.domain.entity.StockReservation;

public interface InventoryEventPublisher {

    void publishStockUpdated(InventoryItem inventoryItem);

    void publishStockLow(InventoryItem inventoryItem);

    void publishOutOfStock(InventoryItem inventoryItem);

    void publishBackInStock(InventoryItem inventoryItem);

    void publishStockReserved(StockReservation reservation);

    void publishReservationConfirmed(StockReservation reservation);

    void publishReservationReleased(StockReservation reservation);

    void publishReservationExpired(StockReservation reservation);
}