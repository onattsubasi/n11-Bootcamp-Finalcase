package com.onatsubasi.finalcase.inventory.application.service;

import com.onatsubasi.finalcase.inventory.domain.enums.StockMovementType;
import com.onatsubasi.finalcase.inventory.domain.entity.InventoryItem;
import com.onatsubasi.finalcase.inventory.domain.entity.StockMovement;
import com.onatsubasi.finalcase.inventory.domain.repository.StockMovementRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class StockMovementServiceTest {

    @Mock
    private StockMovementRepository stockMovementRepository;

    @InjectMocks
    private StockMovementService stockMovementService;

    @Test
    @DisplayName("Should record stock movement")
    void shouldRecordMovement() {
        // Given
        InventoryItem item = mock(InventoryItem.class);
        when(item.getId()).thenReturn(UUID.randomUUID());
        when(item.getProductId()).thenReturn(UUID.randomUUID());
        when(item.getTotalQuantity()).thenReturn(100);
        when(item.getReservedQuantity()).thenReturn(10);
        
        // When
        stockMovementService.record(
                item, 
                StockMovementType.ADMIN_INCREASE, 
                50, 
                50, 
                0, 
                null, 
                null, 
                null, 
                "Initial", 
                "REF-1"
        );

        // Then
        verify(stockMovementRepository).save(any(StockMovement.class));
    }
}
