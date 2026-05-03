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
import com.onatsubasi.finalcase.inventory.domain.enums.StockMovementType;
import com.onatsubasi.finalcase.inventory.domain.enums.StockReservationStatus;
import com.onatsubasi.finalcase.inventory.domain.enums.StockStatus;
import com.onatsubasi.finalcase.inventory.domain.exception.InventoryErrorCode;
import com.onatsubasi.finalcase.inventory.domain.entity.InventoryItem;
import com.onatsubasi.finalcase.inventory.domain.entity.StockReservation;
import com.onatsubasi.finalcase.inventory.domain.entity.StockReservationItem;
import com.onatsubasi.finalcase.inventory.domain.repository.InventoryItemRepository;
import com.onatsubasi.finalcase.inventory.domain.repository.StockReservationRepository;
import com.onatsubasi.finalcase.inventory.infrastructure.mapper.InventoryMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Instant;
import java.util.Collection;
import java.util.Comparator;
import java.util.HexFormat;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class InventoryReservationService {

    private final InventoryItemRepository inventoryItemRepository;
    private final StockReservationRepository stockReservationRepository;
    private final StockMovementService stockMovementService;
    private final InventoryEventPublisher eventPublisher;
    private final InventoryMapper inventoryMapper;

    @Value("${inventory.reservation.default-timeout-minutes:30}")
    private long defaultReservationTimeoutMinutes;

    @Transactional
    public StockReservationResponse reserveStock(String idempotencyKey, ReserveStockRequest request) {
        try {
            MDC.put("eventName", "inventory.reservation.reserve.started");
            log.info(
                    "Stock reservation started, checkoutId={}, userId={}, itemCount={}",
                    request.checkoutId(),
                    request.userId(),
                    request.items() == null ? 0 : request.items().size()
            );

            String normalizedIdempotencyKey = requireIdempotencyKey(idempotencyKey);
            List<ReserveStockItemRequest> normalizedItems = normalizeItems(request.items());
            String requestHash = requestHash(request.checkoutId(), request.userId(), normalizedItems);

            StockReservation existingReservation = stockReservationRepository
                    .findByIdempotencyKey(normalizedIdempotencyKey)
                    .orElse(null);

            if (existingReservation != null) {
                existingReservation.assertSameRequestHash(requestHash);

                MDC.put("eventName", "inventory.reservation.idempotent_return");
                log.info(
                        "Existing reservation returned for idempotent request, reservationId={}, checkoutId={}",
                        existingReservation.getId(),
                        existingReservation.getCheckoutId()
                );

                return inventoryMapper.toResponse(existingReservation);
            }

            Map<UUID, InventoryItem> inventoryByProductId = lockInventoryItems(
                    normalizedItems.stream()
                            .map(ReserveStockItemRequest::productId)
                            .toList()
            );

            StockReservation reservation = StockReservation.create(
                    normalizedIdempotencyKey,
                    requestHash,
                    request.checkoutId(),
                    request.userId(),
                    Instant.now().plusSeconds(defaultReservationTimeoutMinutes * 60)
            );

            for (ReserveStockItemRequest itemRequest : normalizedItems) {
                InventoryItem inventoryItem = inventoryByProductId.get(itemRequest.productId());

                if (inventoryItem == null) {
                    throw new BaseException(
                            InventoryErrorCode.INVENTORY_ITEM_NOT_FOUND,
                            "Inventory item not found for productId: " + itemRequest.productId()
                    );
                }

                StockStatus previousStatus = inventoryItem.stockStatus();
                int totalBefore = inventoryItem.getTotalQuantity();
                int reservedBefore = inventoryItem.getReservedQuantity();

                inventoryItem.reserve(itemRequest.quantity());
                reservation.addItem(itemRequest.productId(), itemRequest.quantity());

                inventoryItemRepository.save(inventoryItem);

                stockMovementService.record(
                        inventoryItem,
                        StockMovementType.RESERVED,
                        itemRequest.quantity(),
                        totalBefore,
                        reservedBefore,
                        null,
                        request.checkoutId(),
                        null,
                        "Checkout stock reservation",
                        normalizedIdempotencyKey
                );

                eventPublisher.publishStockUpdated(inventoryItem);
                publishStockStateEventIfNeeded(previousStatus, inventoryItem.stockStatus(), inventoryItem);
            }

            reservation.assertNotEmpty();

            StockReservation savedReservation = stockReservationRepository.save(reservation);
            eventPublisher.publishStockReserved(savedReservation);

            MDC.put("eventName", "inventory.reservation.reserved");
            log.info(
                    "Stock reservation completed, reservationId={}, checkoutId={}, userId={}, reservedUntil={}",
                    savedReservation.getId(),
                    savedReservation.getCheckoutId(),
                    savedReservation.getUserId(),
                    savedReservation.getReservedUntil()
            );

            return inventoryMapper.toResponse(savedReservation);
        } catch (BaseException ex) {
            logBusinessFailure("inventory.reservation.reserve.failed", ex);
            throw ex;
        } finally {
            clearMdc();
        }
    }

    @Transactional
    public ReservationStatusResponse confirmReservation(UUID reservationId, ConfirmReservationRequest request) {
        try {
            MDC.put("eventName", "inventory.reservation.confirm.started");
            log.info("Reservation confirmation started, reservationId={}, orderId={}", reservationId, request.orderId());

            StockReservation reservation = stockReservationRepository.findByIdForUpdate(reservationId)
                    .orElseThrow(() -> new BaseException(InventoryErrorCode.RESERVATION_NOT_FOUND));

            if (reservation.getStatus() == StockReservationStatus.CONFIRMED
                    && request.orderId().equals(reservation.getOrderId())) {
                return inventoryMapper.toStatusResponse(reservation);
            }

            reservation.confirm(request.orderId());

            Map<UUID, InventoryItem> inventoryByProductId = lockInventoryItems(
                    reservation.getItems().stream()
                            .map(StockReservationItem::getProductId)
                            .toList()
            );

            for (StockReservationItem reservationItem : reservation.getItems()) {
                InventoryItem inventoryItem = inventoryByProductId.get(reservationItem.getProductId());

                if (inventoryItem == null) {
                    throw new BaseException(InventoryErrorCode.INVENTORY_ITEM_NOT_FOUND);
                }

                StockStatus previousStatus = inventoryItem.stockStatus();
                int totalBefore = inventoryItem.getTotalQuantity();
                int reservedBefore = inventoryItem.getReservedQuantity();

                inventoryItem.confirmSale(reservationItem.getQuantity());
                inventoryItemRepository.save(inventoryItem);

                stockMovementService.record(
                        inventoryItem,
                        StockMovementType.CONFIRMED_SOLD,
                        -reservationItem.getQuantity(),
                        totalBefore,
                        reservedBefore,
                        reservation.getId(),
                        reservation.getCheckoutId(),
                        request.orderId(),
                        "Stock reservation confirmed after payment success",
                        reservation.getIdempotencyKey()
                );

                eventPublisher.publishStockUpdated(inventoryItem);
                publishStockStateEventIfNeeded(previousStatus, inventoryItem.stockStatus(), inventoryItem);
            }

            StockReservation savedReservation = stockReservationRepository.save(reservation);
            eventPublisher.publishReservationConfirmed(savedReservation);

            MDC.put("eventName", "inventory.reservation.confirmed");
            log.info(
                    "Reservation confirmed, reservationId={}, checkoutId={}, orderId={}",
                    savedReservation.getId(),
                    savedReservation.getCheckoutId(),
                    savedReservation.getOrderId()
            );

            return inventoryMapper.toStatusResponse(savedReservation);
        } catch (BaseException ex) {
            logBusinessFailure("inventory.reservation.confirm.failed", ex);
            throw ex;
        } finally {
            clearMdc();
        }
    }

    @Transactional
    public ReservationStatusResponse releaseReservation(UUID reservationId, ReleaseReservationRequest request) {
        try {
            ReleaseReason reason = request == null || request.reason() == null
                    ? ReleaseReason.UNKNOWN
                    : request.reason();

            MDC.put("eventName", "inventory.reservation.release.started");
            log.info("Reservation release started, reservationId={}, reason={}", reservationId, reason);

            StockReservation reservation = stockReservationRepository.findByIdForUpdate(reservationId)
                    .orElseThrow(() -> new BaseException(InventoryErrorCode.RESERVATION_NOT_FOUND));

            boolean changed = reservation.release(reason);

            if (!changed) {
                return inventoryMapper.toStatusResponse(reservation);
            }

            releaseInventoryQuantities(
                    reservation,
                    StockMovementType.RELEASED,
                    "Stock reservation released"
            );

            StockReservation savedReservation = stockReservationRepository.save(reservation);
            eventPublisher.publishReservationReleased(savedReservation);

            MDC.put("eventName", "inventory.reservation.released");
            log.info(
                    "Reservation released, reservationId={}, checkoutId={}, reason={}",
                    savedReservation.getId(),
                    savedReservation.getCheckoutId(),
                    savedReservation.getReleaseReason()
            );

            return inventoryMapper.toStatusResponse(savedReservation);
        } catch (BaseException ex) {
            logBusinessFailure("inventory.reservation.release.failed", ex);
            throw ex;
        } finally {
            clearMdc();
        }
    }

    @Transactional
    public int expireReservations(Instant now, int batchSize) {
        Instant referenceTime = now == null ? Instant.now() : now;

        List<StockReservation> expiredReservations = stockReservationRepository.findExpiredReservationsForUpdate(
                StockReservationStatus.RESERVED,
                referenceTime,
                Math.max(batchSize, 1)
        );

        int expiredCount = 0;

        for (StockReservation reservation : expiredReservations) {
            boolean changed = reservation.expire();

            if (!changed) {
                continue;
            }

            releaseInventoryQuantities(
                    reservation,
                    StockMovementType.EXPIRED_RELEASE,
                    "Stock reservation expired"
            );

            StockReservation savedReservation = stockReservationRepository.save(reservation);
            eventPublisher.publishReservationExpired(savedReservation);
            expiredCount++;
        }

        if (expiredCount > 0) {
            MDC.put("eventName", "inventory.reservation.expired.cleanup");
            log.info("Expired stock reservations processed, count={}, referenceTime={}", expiredCount, referenceTime);
            MDC.remove("eventName");
        }

        return expiredCount;
    }

    @Transactional(readOnly = true)
    public StockReservationResponse getReservation(UUID reservationId) {
        StockReservation reservation = stockReservationRepository.findById(reservationId)
                .orElseThrow(() -> new BaseException(InventoryErrorCode.RESERVATION_NOT_FOUND));

        return inventoryMapper.toResponse(reservation);
    }

    private void releaseInventoryQuantities(
            StockReservation reservation,
            StockMovementType movementType,
            String reason
    ) {
        Map<UUID, InventoryItem> inventoryByProductId = lockInventoryItems(
                reservation.getItems().stream()
                        .map(StockReservationItem::getProductId)
                        .toList()
        );

        for (StockReservationItem reservationItem : reservation.getItems()) {
            InventoryItem inventoryItem = inventoryByProductId.get(reservationItem.getProductId());

            if (inventoryItem == null) {
                throw new BaseException(InventoryErrorCode.INVENTORY_ITEM_NOT_FOUND);
            }

            StockStatus previousStatus = inventoryItem.stockStatus();
            int totalBefore = inventoryItem.getTotalQuantity();
            int reservedBefore = inventoryItem.getReservedQuantity();

            inventoryItem.releaseReserved(reservationItem.getQuantity());
            inventoryItemRepository.save(inventoryItem);

            stockMovementService.record(
                    inventoryItem,
                    movementType,
                    -reservationItem.getQuantity(),
                    totalBefore,
                    reservedBefore,
                    reservation.getId(),
                    reservation.getCheckoutId(),
                    reservation.getOrderId(),
                    reason,
                    reservation.getIdempotencyKey()
            );

            eventPublisher.publishStockUpdated(inventoryItem);
            publishStockStateEventIfNeeded(previousStatus, inventoryItem.stockStatus(), inventoryItem);
        }
    }

    private Map<UUID, InventoryItem> lockInventoryItems(Collection<UUID> productIds) {
        List<UUID> orderedProductIds = productIds.stream()
                .distinct()
                .sorted()
                .toList();

        List<InventoryItem> lockedItems = inventoryItemRepository.findAllByProductIdsForUpdate(orderedProductIds);

        return lockedItems.stream()
                .collect(Collectors.toMap(
                        InventoryItem::getProductId,
                        Function.identity(),
                        (left, right) -> left,
                        LinkedHashMap::new
                ));
    }

    private List<ReserveStockItemRequest> normalizeItems(List<ReserveStockItemRequest> items) {
        if (items == null || items.isEmpty()) {
            throw new BaseException(InventoryErrorCode.RESERVATION_EMPTY);
        }

        Map<UUID, Integer> quantityByProductId = new LinkedHashMap<>();

        items.stream()
                .sorted(Comparator.comparing(ReserveStockItemRequest::productId, Comparator.nullsFirst(Comparator.naturalOrder())))
                .forEach(item -> {
                    if (item == null || item.productId() == null) {
                        throw new BaseException(InventoryErrorCode.INVALID_PRODUCT_ID);
                    }

                    if (item.quantity() <= 0) {
                        throw new BaseException(InventoryErrorCode.INVALID_POSITIVE_QUANTITY);
                    }

                    quantityByProductId.merge(item.productId(), item.quantity(), Integer::sum);
                });

        return quantityByProductId.entrySet()
                .stream()
                .map(entry -> new ReserveStockItemRequest(entry.getKey(), entry.getValue()))
                .toList();
    }

    private String requireIdempotencyKey(String idempotencyKey) {
        if (idempotencyKey == null || idempotencyKey.isBlank()) {
            throw new BaseException(InventoryErrorCode.INVALID_IDEMPOTENCY_KEY);
        }

        return idempotencyKey.trim();
    }

    private String requestHash(UUID checkoutId, UUID userId, List<ReserveStockItemRequest> normalizedItems) {
        String canonical = "checkoutId=" + checkoutId
                + "|userId=" + userId
                + "|items=" + normalizedItems.stream()
                .map(item -> item.productId() + ":" + item.quantity())
                .collect(Collectors.joining(","));

        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(canonical.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hash);
        } catch (Exception ex) {
            throw new BaseException(
                    InventoryErrorCode.INVALID_REQUEST_HASH,
                    "Could not calculate reservation request hash"
            );
        }
    }

    private void publishStockStateEventIfNeeded(
            StockStatus previousStatus,
            StockStatus currentStatus,
            InventoryItem item
    ) {
        if (currentStatus == StockStatus.OUT_OF_STOCK && previousStatus != StockStatus.OUT_OF_STOCK) {
            eventPublisher.publishOutOfStock(item);
            return;
        }

        if (currentStatus == StockStatus.LOW_STOCK && previousStatus != StockStatus.LOW_STOCK) {
            eventPublisher.publishStockLow(item);
            return;
        }

        if (previousStatus != null
                && previousStatus != StockStatus.IN_STOCK
                && currentStatus == StockStatus.IN_STOCK) {
            eventPublisher.publishBackInStock(item);
        }
    }

    private void logBusinessFailure(String eventName, BaseException ex) {
        MDC.put("eventName", eventName);
        MDC.put("errorCode", ex.getErrorCode().code());
        log.warn("Inventory reservation operation failed, errorCode={}", ex.getErrorCode().code());
    }

    private void clearMdc() {
        MDC.remove("eventName");
        MDC.remove("errorCode");
    }
}
