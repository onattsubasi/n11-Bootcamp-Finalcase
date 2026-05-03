package com.onatsubasi.finalcase.inventory.application.service;

import com.onatsubasi.finalcase.inventory.domain.enums.StockMovementType;
import com.onatsubasi.finalcase.inventory.domain.entity.InventoryItem;
import com.onatsubasi.finalcase.inventory.domain.entity.StockMovement;
import com.onatsubasi.finalcase.inventory.domain.repository.StockMovementRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class StockMovementService {

    private final StockMovementRepository stockMovementRepository;

    public void record(
            InventoryItem item,
            StockMovementType type,
            int quantityChange,
            int totalBefore,
            int reservedBefore,
            UUID reservationId,
            UUID checkoutId,
            UUID orderId,
            String reason,
            String referenceId
    ) {
        StockMovement movement = StockMovement.create(
                item.getId(),
                item.getProductId(),
                type,
                quantityChange,
                totalBefore,
                reservedBefore,
                item.getTotalQuantity(),
                item.getReservedQuantity(),
                reservationId,
                checkoutId,
                orderId,
                reason,
                referenceId
        );

        stockMovementRepository.save(movement);
    }
}
