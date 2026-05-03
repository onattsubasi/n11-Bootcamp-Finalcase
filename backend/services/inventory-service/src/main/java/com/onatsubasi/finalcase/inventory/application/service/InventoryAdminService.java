package com.onatsubasi.finalcase.inventory.application.service;

import com.onatsubasi.finalcase.common.core.exception.BaseException;
import com.onatsubasi.finalcase.common.security.UserContext;
import com.onatsubasi.finalcase.inventory.application.dto.request.*;
import com.onatsubasi.finalcase.inventory.application.dto.response.InventoryItemResponse;
import com.onatsubasi.finalcase.inventory.application.dto.response.StockMovementResponse;
import com.onatsubasi.finalcase.inventory.application.port.InventoryEventPublisher;
import com.onatsubasi.finalcase.inventory.domain.enums.InventoryItemStatus;
import com.onatsubasi.finalcase.inventory.domain.enums.StockMovementType;
import com.onatsubasi.finalcase.inventory.domain.enums.StockStatus;
import com.onatsubasi.finalcase.inventory.domain.exception.InventoryErrorCode;
import com.onatsubasi.finalcase.inventory.domain.entity.InventoryItem;
import com.onatsubasi.finalcase.inventory.domain.repository.InventoryItemRepository;
import com.onatsubasi.finalcase.inventory.domain.repository.StockMovementRepository;
import com.onatsubasi.finalcase.inventory.infrastructure.mapper.InventoryMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class InventoryAdminService {

    private final InventoryItemRepository inventoryItemRepository;
    private final StockMovementRepository stockMovementRepository;
    private final StockMovementService stockMovementService;
    private final InventoryEventPublisher eventPublisher;
    private final InventoryMapper inventoryMapper;

    @Transactional
    public InventoryItemResponse createInventoryItem(
            UserContext admin,
            CreateInventoryItemRequest request
    ) {
        try {
            MDC.put("eventName", "inventory.item.create.started");
            log.info("Inventory item creation started, productId={}", request.productId());

            if (inventoryItemRepository.existsByProductId(request.productId())) {
                throw new BaseException(InventoryErrorCode.INVENTORY_ITEM_ALREADY_EXISTS);
            }

            InventoryItem item = InventoryItem.create(
                    request.productId(),
                    request.initialQuantity(),
                    request.lowStockThreshold()
            );

            InventoryItem saved = inventoryItemRepository.save(item);

            stockMovementService.record(
                    saved,
                    StockMovementType.INITIAL_STOCK,
                    request.initialQuantity(),
                    0,
                    0,
                    null,
                    null,
                    null,
                    "Initial stock",
                    adminReference(admin)
            );

            eventPublisher.publishStockUpdated(saved);
            publishStockStateEventIfNeeded(null, saved.stockStatus(), saved);

            MDC.put("eventName", "inventory.item.created");
            log.info(
                    "Inventory item created, inventoryItemId={}, productId={}, totalQuantity={}",
                    saved.getId(),
                    saved.getProductId(),
                    saved.getTotalQuantity()
            );

            return inventoryMapper.toResponse(saved);
        } catch (BaseException ex) {
            logBusinessFailure("inventory.item.create.failed", ex);
            throw ex;
        } finally {
            clearMdc();
        }
    }

    @Transactional
    public InventoryItemResponse increaseStock(
            UserContext admin,
            UUID productId,
            IncreaseStockRequest request
    ) {
        try {
            MDC.put("eventName", "inventory.stock.increase.started");
            log.info("Stock increase started, productId={}, quantity={}", productId, request.quantity());

            InventoryItem item = getByProductIdForUpdate(productId);

            StockStatus previousStatus = item.stockStatus();
            int totalBefore = item.getTotalQuantity();
            int reservedBefore = item.getReservedQuantity();

            item.increaseStock(request.quantity());

            InventoryItem saved = inventoryItemRepository.save(item);

            stockMovementService.record(
                    saved,
                    StockMovementType.ADMIN_INCREASE,
                    request.quantity(),
                    totalBefore,
                    reservedBefore,
                    null,
                    null,
                    null,
                    request.reason(),
                    adminReference(admin)
            );

            eventPublisher.publishStockUpdated(saved);
            publishStockStateEventIfNeeded(previousStatus, saved.stockStatus(), saved);

            MDC.put("eventName", "inventory.stock.increased");
            log.info(
                    "Stock increased, inventoryItemId={}, productId={}, quantity={}, availableQuantity={}",
                    saved.getId(),
                    saved.getProductId(),
                    request.quantity(),
                    saved.availableQuantity()
            );

            return inventoryMapper.toResponse(saved);
        } catch (BaseException ex) {
            logBusinessFailure("inventory.stock.increase.failed", ex);
            throw ex;
        } finally {
            clearMdc();
        }
    }

    @Transactional
    public InventoryItemResponse decreaseStock(
            UserContext admin,
            UUID productId,
            DecreaseStockRequest request
    ) {
        try {
            MDC.put("eventName", "inventory.stock.decrease.started");
            log.info("Stock decrease started, productId={}, quantity={}", productId, request.quantity());

            InventoryItem item = getByProductIdForUpdate(productId);

            StockStatus previousStatus = item.stockStatus();
            int totalBefore = item.getTotalQuantity();
            int reservedBefore = item.getReservedQuantity();

            item.decreaseStock(request.quantity());

            InventoryItem saved = inventoryItemRepository.save(item);

            stockMovementService.record(
                    saved,
                    StockMovementType.ADMIN_DECREASE,
                    -request.quantity(),
                    totalBefore,
                    reservedBefore,
                    null,
                    null,
                    null,
                    request.reason(),
                    adminReference(admin)
            );

            eventPublisher.publishStockUpdated(saved);
            publishStockStateEventIfNeeded(previousStatus, saved.stockStatus(), saved);

            MDC.put("eventName", "inventory.stock.decreased");
            log.info(
                    "Stock decreased, inventoryItemId={}, productId={}, quantity={}, availableQuantity={}",
                    saved.getId(),
                    saved.getProductId(),
                    request.quantity(),
                    saved.availableQuantity()
            );

            return inventoryMapper.toResponse(saved);
        } catch (BaseException ex) {
            logBusinessFailure("inventory.stock.decrease.failed", ex);
            throw ex;
        } finally {
            clearMdc();
        }
    }

    @Transactional
    public InventoryItemResponse setStock(
            UserContext admin,
            UUID productId,
            SetStockRequest request
    ) {
        try {
            MDC.put("eventName", "inventory.stock.set.started");
            log.info("Stock set started, productId={}, totalQuantity={}", productId, request.totalQuantity());

            InventoryItem item = getByProductIdForUpdate(productId);

            StockStatus previousStatus = item.stockStatus();
            int totalBefore = item.getTotalQuantity();
            int reservedBefore = item.getReservedQuantity();

            item.setTotalQuantity(request.totalQuantity());

            InventoryItem saved = inventoryItemRepository.save(item);

            stockMovementService.record(
                    saved,
                    StockMovementType.ADMIN_SET,
                    request.totalQuantity() - totalBefore,
                    totalBefore,
                    reservedBefore,
                    null,
                    null,
                    null,
                    request.reason(),
                    adminReference(admin)
            );

            eventPublisher.publishStockUpdated(saved);
            publishStockStateEventIfNeeded(previousStatus, saved.stockStatus(), saved);

            MDC.put("eventName", "inventory.stock.set");
            log.info(
                    "Stock set completed, inventoryItemId={}, productId={}, totalQuantity={}, availableQuantity={}",
                    saved.getId(),
                    saved.getProductId(),
                    saved.getTotalQuantity(),
                    saved.availableQuantity()
            );

            return inventoryMapper.toResponse(saved);
        } catch (BaseException ex) {
            logBusinessFailure("inventory.stock.set.failed", ex);
            throw ex;
        } finally {
            clearMdc();
        }
    }

    @Transactional
    public InventoryItemResponse updateLowStockThreshold(
            UUID productId,
            UpdateLowStockThresholdRequest request
    ) {
        try {
            MDC.put("eventName", "inventory.threshold.update.started");
            log.info("Low stock threshold update started, productId={}, threshold={}",
                    productId,
                    request.lowStockThreshold());

            InventoryItem item = getByProductIdForUpdate(productId);

            StockStatus previousStatus = item.stockStatus();

            item.updateLowStockThreshold(request.lowStockThreshold());

            InventoryItem saved = inventoryItemRepository.save(item);

            eventPublisher.publishStockUpdated(saved);
            publishStockStateEventIfNeeded(previousStatus, saved.stockStatus(), saved);

            MDC.put("eventName", "inventory.threshold.updated");
            log.info(
                    "Low stock threshold updated, inventoryItemId={}, productId={}, threshold={}",
                    saved.getId(),
                    saved.getProductId(),
                    saved.getLowStockThreshold()
            );

            return inventoryMapper.toResponse(saved);
        } catch (BaseException ex) {
            logBusinessFailure("inventory.threshold.update.failed", ex);
            throw ex;
        } finally {
            clearMdc();
        }
    }

    @Transactional(readOnly = true)
    public InventoryItemResponse getByProductId(UUID productId) {
        InventoryItem item = inventoryItemRepository.findByProductId(productId)
                .orElseThrow(() -> new BaseException(InventoryErrorCode.INVENTORY_ITEM_NOT_FOUND));

        return inventoryMapper.toResponse(item);
    }

    @Transactional(readOnly = true)
    public List<InventoryItemResponse> listActiveItems() {
        return inventoryItemRepository.findByStatus(InventoryItemStatus.ACTIVE)
                .stream()
                .map(inventoryMapper::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<StockMovementResponse> getMovements(UUID productId) {
        return stockMovementRepository.findByProductId(productId)
                .stream()
                .map(inventoryMapper::toResponse)
                .toList();
    }

    private InventoryItem getByProductIdForUpdate(UUID productId) {
        return inventoryItemRepository.findByProductIdForUpdate(productId)
                .orElseThrow(() -> new BaseException(InventoryErrorCode.INVENTORY_ITEM_NOT_FOUND));
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

    private String adminReference(UserContext admin) {
        return admin == null || admin.userId() == null
                ? null
                : admin.userId().toString();
    }

    private void logBusinessFailure(String eventName, BaseException ex) {
        MDC.put("eventName", eventName);
        MDC.put("errorCode", ex.getErrorCode().code());
        log.warn("Inventory admin operation failed, errorCode={}", ex.getErrorCode().code());
    }

    private void clearMdc() {
        MDC.remove("eventName");
        MDC.remove("errorCode");
    }
}
