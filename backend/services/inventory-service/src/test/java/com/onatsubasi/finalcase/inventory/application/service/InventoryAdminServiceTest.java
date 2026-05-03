package com.onatsubasi.finalcase.inventory.application.service;

import com.onatsubasi.finalcase.common.core.exception.BaseException;
import com.onatsubasi.finalcase.common.security.UserContext;
import com.onatsubasi.finalcase.inventory.application.dto.request.*;
import com.onatsubasi.finalcase.inventory.application.dto.response.InventoryItemResponse;
import com.onatsubasi.finalcase.inventory.application.port.InventoryEventPublisher;
import com.onatsubasi.finalcase.inventory.domain.exception.InventoryErrorCode;
import com.onatsubasi.finalcase.inventory.domain.entity.InventoryItem;
import com.onatsubasi.finalcase.inventory.domain.repository.InventoryItemRepository;
import com.onatsubasi.finalcase.inventory.domain.repository.StockMovementRepository;
import com.onatsubasi.finalcase.inventory.infrastructure.mapper.InventoryMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class InventoryAdminServiceTest {

    @Mock
    private InventoryItemRepository inventoryItemRepository;

    @Mock
    private StockMovementRepository stockMovementRepository;

    @Mock
    private StockMovementService stockMovementService;

    @Mock
    private InventoryEventPublisher eventPublisher;

    @Mock
    private InventoryMapper inventoryMapper;

    @InjectMocks
    private InventoryAdminService inventoryAdminService;

    private UserContext adminUser;
    private UUID productId;

    @BeforeEach
    void setUp() {
        adminUser = new UserContext(UUID.randomUUID(), "admin@test.com", Set.of("ADMIN"));
        productId = UUID.randomUUID();
    }

    @Test
    @DisplayName("Should create inventory item successfully")
    void shouldCreateInventoryItem() {
        // Given
        CreateInventoryItemRequest request = new CreateInventoryItemRequest(productId, 100, 10);
        when(inventoryItemRepository.existsByProductId(productId)).thenReturn(false);
        
        InventoryItem item = InventoryItem.create(productId, 100, 10);
        when(inventoryItemRepository.save(any(InventoryItem.class))).thenReturn(item);
        
        InventoryItemResponse response = mock(InventoryItemResponse.class);
        when(inventoryMapper.toResponse(any(InventoryItem.class))).thenReturn(response);

        // When
        InventoryItemResponse result = inventoryAdminService.createInventoryItem(adminUser, request);

        // Then
        assertThat(result).isNotNull();
        verify(inventoryItemRepository).save(any(InventoryItem.class));
        verify(stockMovementService).record(any(), any(), eq(100), anyInt(), anyInt(), any(), any(), any(), anyString(), anyString());
        verify(eventPublisher).publishStockUpdated(any(InventoryItem.class));
    }

    @Test
    @DisplayName("Should throw exception when creating existing inventory item")
    void shouldThrowExceptionWhenItemExists() {
        // Given
        CreateInventoryItemRequest request = new CreateInventoryItemRequest(productId, 100, 10);
        when(inventoryItemRepository.existsByProductId(productId)).thenReturn(true);

        // When & Then
        assertThatThrownBy(() -> inventoryAdminService.createInventoryItem(adminUser, request))
                .isInstanceOf(BaseException.class)
                .hasFieldOrPropertyWithValue("errorCode", InventoryErrorCode.INVENTORY_ITEM_ALREADY_EXISTS);
    }

    @Test
    @DisplayName("Should increase stock successfully")
    void shouldIncreaseStock() {
        // Given
        IncreaseStockRequest request = new IncreaseStockRequest(50, "Restock");
        InventoryItem item = InventoryItem.create(productId, 100, 10);
        
        when(inventoryItemRepository.findByProductIdForUpdate(productId)).thenReturn(Optional.of(item));
        when(inventoryItemRepository.save(any(InventoryItem.class))).thenReturn(item);
        
        InventoryItemResponse response = mock(InventoryItemResponse.class);
        when(inventoryMapper.toResponse(any(InventoryItem.class))).thenReturn(response);

        // When
        InventoryItemResponse result = inventoryAdminService.increaseStock(adminUser, productId, request);

        // Then
        assertThat(result).isNotNull();
        assertThat(item.getTotalQuantity()).isEqualTo(150);
        verify(inventoryItemRepository).save(item);
        verify(stockMovementService).record(any(), any(), eq(50), anyInt(), anyInt(), any(), any(), any(), eq("Restock"), anyString());
        verify(eventPublisher).publishStockUpdated(item);
    }

    @Test
    @DisplayName("Should decrease stock successfully")
    void shouldDecreaseStock() {
        // Given
        DecreaseStockRequest request = new DecreaseStockRequest(30, "Sale adjustment");
        InventoryItem item = InventoryItem.create(productId, 100, 10);
        
        when(inventoryItemRepository.findByProductIdForUpdate(productId)).thenReturn(Optional.of(item));
        when(inventoryItemRepository.save(any(InventoryItem.class))).thenReturn(item);
        
        InventoryItemResponse response = mock(InventoryItemResponse.class);
        when(inventoryMapper.toResponse(any(InventoryItem.class))).thenReturn(response);

        // When
        InventoryItemResponse result = inventoryAdminService.decreaseStock(adminUser, productId, request);

        // Then
        assertThat(result).isNotNull();
        assertThat(item.getTotalQuantity()).isEqualTo(70);
        verify(inventoryItemRepository).save(item);
        verify(stockMovementService).record(any(), any(), eq(-30), anyInt(), anyInt(), any(), any(), any(), eq("Sale adjustment"), anyString());
    }

    @Test
    @DisplayName("Should set stock successfully")
    void shouldSetStock() {
        // Given
        SetStockRequest request = new SetStockRequest(200, "Correction");
        InventoryItem item = InventoryItem.create(productId, 100, 10);
        
        when(inventoryItemRepository.findByProductIdForUpdate(productId)).thenReturn(Optional.of(item));
        when(inventoryItemRepository.save(any(InventoryItem.class))).thenReturn(item);
        
        InventoryItemResponse response = mock(InventoryItemResponse.class);
        when(inventoryMapper.toResponse(any(InventoryItem.class))).thenReturn(response);

        // When
        InventoryItemResponse result = inventoryAdminService.setStock(adminUser, productId, request);

        // Then
        assertThat(result).isNotNull();
        assertThat(item.getTotalQuantity()).isEqualTo(200);
        verify(inventoryItemRepository).save(item);
        verify(stockMovementService).record(any(), any(), eq(100), anyInt(), anyInt(), any(), any(), any(), eq("Correction"), anyString());
    }

    @Test
    @DisplayName("Should update low stock threshold successfully")
    void shouldUpdateThreshold() {
        // Given
        UpdateLowStockThresholdRequest request = new UpdateLowStockThresholdRequest(20);
        InventoryItem item = InventoryItem.create(productId, 100, 10);
        
        when(inventoryItemRepository.findByProductIdForUpdate(productId)).thenReturn(Optional.of(item));
        when(inventoryItemRepository.save(any(InventoryItem.class))).thenReturn(item);
        
        InventoryItemResponse response = mock(InventoryItemResponse.class);
        when(inventoryMapper.toResponse(any(InventoryItem.class))).thenReturn(response);

        // When
        InventoryItemResponse result = inventoryAdminService.updateLowStockThreshold(productId, request);

        // Then
        assertThat(result).isNotNull();
        assertThat(item.getLowStockThreshold()).isEqualTo(20);
        verify(inventoryItemRepository).save(item);
    }
}
