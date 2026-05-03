package com.onatsubasi.finalcase.inventory.application.service;

import com.onatsubasi.finalcase.common.core.exception.BaseException;
import com.onatsubasi.finalcase.inventory.application.dto.internal.ConfirmReservationRequest;
import com.onatsubasi.finalcase.inventory.application.dto.internal.ReleaseReservationRequest;
import com.onatsubasi.finalcase.inventory.application.dto.internal.ReserveStockItemRequest;
import com.onatsubasi.finalcase.inventory.application.dto.internal.ReserveStockRequest;
import com.onatsubasi.finalcase.inventory.application.dto.response.ReservationStatusResponse;
import com.onatsubasi.finalcase.inventory.application.dto.response.StockReservationResponse;
import com.onatsubasi.finalcase.inventory.application.port.InventoryEventPublisher;
import com.onatsubasi.finalcase.inventory.domain.enums.ReleaseReason;
import com.onatsubasi.finalcase.inventory.domain.enums.StockReservationStatus;
import com.onatsubasi.finalcase.inventory.domain.exception.InventoryErrorCode;
import com.onatsubasi.finalcase.inventory.domain.entity.InventoryItem;
import com.onatsubasi.finalcase.inventory.domain.entity.StockReservation;
import com.onatsubasi.finalcase.inventory.domain.repository.InventoryItemRepository;
import com.onatsubasi.finalcase.inventory.domain.repository.StockReservationRepository;
import com.onatsubasi.finalcase.inventory.infrastructure.mapper.InventoryMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class InventoryReservationServiceTest {

    @Mock
    private InventoryItemRepository inventoryItemRepository;

    @Mock
    private StockReservationRepository stockReservationRepository;

    @Mock
    private StockMovementService stockMovementService;

    @Mock
    private InventoryEventPublisher eventPublisher;

    @Mock
    private InventoryMapper inventoryMapper;

    @InjectMocks
    private InventoryReservationService inventoryReservationService;

    private UUID userId;
    private UUID checkoutId;
    private UUID productId;

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();
        checkoutId = UUID.randomUUID();
        productId = UUID.randomUUID();
        ReflectionTestUtils.setField(inventoryReservationService, "defaultReservationTimeoutMinutes", 30L);
    }

    @Test
    @DisplayName("Should reserve stock successfully")
    void shouldReserveStock() {
        // Given
        String idempotencyKey = "key-1";
        ReserveStockItemRequest itemRequest = new ReserveStockItemRequest(productId, 2);
        ReserveStockRequest request = new ReserveStockRequest(checkoutId, userId, List.of(itemRequest));

        when(stockReservationRepository.findByIdempotencyKey(idempotencyKey)).thenReturn(Optional.empty());
        
        InventoryItem inventoryItem = InventoryItem.create(productId, 10, 2);
        when(inventoryItemRepository.findAllByProductIdsForUpdate(anyList())).thenReturn(List.of(inventoryItem));
        
        StockReservation reservation = StockReservation.create(idempotencyKey, "hash", checkoutId, userId, Instant.now().plusSeconds(600));
        when(stockReservationRepository.save(any(StockReservation.class))).thenReturn(reservation);
        
        StockReservationResponse response = mock(StockReservationResponse.class);
        when(inventoryMapper.toResponse(any(StockReservation.class))).thenReturn(response);

        // When
        StockReservationResponse result = inventoryReservationService.reserveStock(idempotencyKey, request);

        // Then
        assertThat(result).isNotNull();
        assertThat(inventoryItem.getReservedQuantity()).isEqualTo(2);
        verify(inventoryItemRepository).save(inventoryItem);
        verify(stockReservationRepository).save(any(StockReservation.class));
        verify(eventPublisher).publishStockReserved(any(StockReservation.class));
    }

    @Test
    @DisplayName("Should return existing reservation for same idempotency key")
    void shouldReturnExistingReservation() {
        // Given
        String idempotencyKey = "key-1";
        ReserveStockItemRequest itemRequest = new ReserveStockItemRequest(productId, 2);
        ReserveStockRequest request = new ReserveStockRequest(checkoutId, userId, List.of(itemRequest));

        // Use reflection or a spy to handle hash calculation if needed, but here we can just mock
        StockReservation existing = mock(StockReservation.class);
        when(stockReservationRepository.findByIdempotencyKey(idempotencyKey)).thenReturn(Optional.of(existing));
        
        StockReservationResponse response = mock(StockReservationResponse.class);
        when(inventoryMapper.toResponse(existing)).thenReturn(response);

        // When
        StockReservationResponse result = inventoryReservationService.reserveStock(idempotencyKey, request);

        // Then
        assertThat(result).isNotNull();
        verify(existing).assertSameRequestHash(anyString());
        verify(stockReservationRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should throw exception when stock is insufficient")
    void shouldThrowExceptionWhenInsufficientStock() {
        // Given
        String idempotencyKey = "key-1";
        ReserveStockItemRequest itemRequest = new ReserveStockItemRequest(productId, 20); // More than available
        ReserveStockRequest request = new ReserveStockRequest(checkoutId, userId, List.of(itemRequest));

        when(stockReservationRepository.findByIdempotencyKey(idempotencyKey)).thenReturn(Optional.empty());
        
        InventoryItem inventoryItem = InventoryItem.create(productId, 10, 2);
        when(inventoryItemRepository.findAllByProductIdsForUpdate(anyList())).thenReturn(List.of(inventoryItem));

        // When & Then
        assertThatThrownBy(() -> inventoryReservationService.reserveStock(idempotencyKey, request))
                .isInstanceOf(BaseException.class)
                .hasFieldOrPropertyWithValue("errorCode", InventoryErrorCode.INSUFFICIENT_STOCK);
    }

    @Test
    @DisplayName("Should confirm reservation successfully")
    void shouldConfirmReservation() {
        // Given
        UUID reservationId = UUID.randomUUID();
        UUID orderId = UUID.randomUUID();
        ConfirmReservationRequest request = new ConfirmReservationRequest(orderId);

        StockReservation reservation = StockReservation.create("key", "hash", checkoutId, userId, Instant.now().plusSeconds(600));
        reservation.addItem(productId, 2);
        
        when(stockReservationRepository.findByIdForUpdate(reservationId)).thenReturn(Optional.of(reservation));
        
        InventoryItem inventoryItem = InventoryItem.create(productId, 10, 2);
        inventoryItem.reserve(2);
        when(inventoryItemRepository.findAllByProductIdsForUpdate(anyList())).thenReturn(List.of(inventoryItem));
        
        when(stockReservationRepository.save(any(StockReservation.class))).thenReturn(reservation);
        
        ReservationStatusResponse response = mock(ReservationStatusResponse.class);
        when(inventoryMapper.toStatusResponse(any(StockReservation.class))).thenReturn(response);

        // When
        ReservationStatusResponse result = inventoryReservationService.confirmReservation(reservationId, request);

        // Then
        assertThat(result).isNotNull();
        assertThat(reservation.getStatus()).isEqualTo(StockReservationStatus.CONFIRMED);
        assertThat(inventoryItem.getReservedQuantity()).isEqualTo(0);
        assertThat(inventoryItem.getTotalQuantity()).isEqualTo(8);
        verify(inventoryItemRepository).save(inventoryItem);
        verify(eventPublisher).publishReservationConfirmed(reservation);
    }

    @Test
    @DisplayName("Should release reservation successfully")
    void shouldReleaseReservation() {
        // Given
        UUID reservationId = UUID.randomUUID();
        ReleaseReservationRequest request = new ReleaseReservationRequest(ReleaseReason.PAYMENT_FAILED);

        StockReservation reservation = StockReservation.create("key", "hash", checkoutId, userId, Instant.now().plusSeconds(600));
        reservation.addItem(productId, 2);
        
        when(stockReservationRepository.findByIdForUpdate(reservationId)).thenReturn(Optional.of(reservation));
        
        InventoryItem inventoryItem = InventoryItem.create(productId, 10, 2);
        inventoryItem.reserve(2);
        when(inventoryItemRepository.findAllByProductIdsForUpdate(anyList())).thenReturn(List.of(inventoryItem));
        
        when(stockReservationRepository.save(any(StockReservation.class))).thenReturn(reservation);
        
        ReservationStatusResponse response = mock(ReservationStatusResponse.class);
        when(inventoryMapper.toStatusResponse(any(StockReservation.class))).thenReturn(response);

        // When
        ReservationStatusResponse result = inventoryReservationService.releaseReservation(reservationId, request);

        // Then
        assertThat(result).isNotNull();
        assertThat(reservation.getStatus()).isEqualTo(StockReservationStatus.RELEASED);
        assertThat(inventoryItem.getReservedQuantity()).isEqualTo(0);
        assertThat(inventoryItem.getTotalQuantity()).isEqualTo(10);
        verify(inventoryItemRepository).save(inventoryItem);
        verify(eventPublisher).publishReservationReleased(reservation);
    }
}
