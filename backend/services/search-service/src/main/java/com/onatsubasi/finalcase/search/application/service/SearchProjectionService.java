package com.onatsubasi.finalcase.search.application.service;

import com.onatsubasi.finalcase.common.core.exception.BaseException;
import com.onatsubasi.finalcase.search.application.dto.event.*;
import com.onatsubasi.finalcase.search.domain.enums.ProductSearchStatus;
import com.onatsubasi.finalcase.search.domain.exception.SearchErrorCode;
import com.onatsubasi.finalcase.search.domain.entity.ProductSearchDocument;
import com.onatsubasi.finalcase.search.domain.repository.ProductSearchDocumentRepository;
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
public class SearchProjectionService {

    private final ProductSearchDocumentRepository documentRepository;

    @Transactional
    public boolean upsertCatalogProduct(CatalogProductProjectionPayload payload) {
        validateProductId(payload.productId());

        try {
            MDC.put("eventName", "search.document.catalog_upsert.started");
            MDC.put("productId", payload.productId().toString());

            ProductSearchDocument document = documentRepository.findByProductIdForUpdate(payload.productId())
                    .orElse(null);

            boolean changed;

            if (document == null) {
                document = ProductSearchDocument.createFromCatalogProjection(
                        payload.productId(),
                        payload.sku(),
                        payload.slug(),
                        payload.name(),
                        payload.description(),
                        payload.brandId(),
                        payload.brandName(),
                        payload.categoryId(),
                        payload.categoryName(),
                        payload.categoryPath(),
                        payload.basePrice(),
                        payload.currency(),
                        payload.imageUrl(),
                        payload.attributes(),
                        payload.tags(),
                        payload.status(),
                        payload.visible(),
                        payload.sourceUpdatedAt()
                );
                changed = true;
            } else {
                changed = document.updateCatalogProjection(
                        payload.sku(),
                        payload.slug(),
                        payload.name(),
                        payload.description(),
                        payload.brandId(),
                        payload.brandName(),
                        payload.categoryId(),
                        payload.categoryName(),
                        payload.categoryPath(),
                        payload.basePrice(),
                        payload.currency(),
                        payload.imageUrl(),
                        payload.attributes(),
                        payload.tags(),
                        payload.status(),
                        payload.visible(),
                        payload.sourceUpdatedAt()
                );
            }

            if (!changed) {
                log.info(
                        "Catalog projection ignored because event is older, productId={}, sourceUpdatedAt={}",
                        payload.productId(),
                        payload.sourceUpdatedAt()
                );
                return false;
            }

            ProductSearchDocument saved = documentRepository.save(document);

            MDC.put("eventName", "search.document.catalog_upserted");
            log.info(
                    "Search document catalog projection upserted, productId={}, status={}, visible={}",
                    saved.getProductId(),
                    saved.getStatus(),
                    saved.isVisible()
            );

            return true;
        } catch (BaseException ex) {
            logBusinessFailure("search.document.catalog_upsert.failed", ex);
            throw ex;
        } finally {
            clearMdc();
        }
    }

    @Transactional
    public boolean markProductDeleted(CatalogProductDeletedPayload payload) {
        validateProductId(payload.productId());

        ProductSearchDocument document = documentRepository.findByProductIdForUpdate(payload.productId())
                .orElseThrow(() -> new BaseException(SearchErrorCode.PRODUCT_SEARCH_DOCUMENT_NOT_FOUND));

        boolean changed = document.markDeleted(payload.sourceUpdatedAt());

        if (changed) {
            documentRepository.save(document);
            logProjection("search.document.deleted", payload.productId());
        }

        return changed;
    }

    @Transactional
    public boolean updateProductStatus(CatalogProductStatusChangedPayload payload) {
        validateProductId(payload.productId());

        ProductSearchDocument document = documentRepository.findByProductIdForUpdate(payload.productId())
                .orElseThrow(() -> new BaseException(SearchErrorCode.PRODUCT_SEARCH_DOCUMENT_NOT_FOUND));

        boolean changed;

        if (payload.status() == ProductSearchStatus.DELETED) {
            changed = document.markDeleted(payload.sourceUpdatedAt());
        } else if (payload.status() == ProductSearchStatus.INACTIVE || !payload.visible()) {
            changed = document.markInactive(payload.sourceUpdatedAt());
        } else {
            changed = document.updateCatalogProjection(
                    document.getSku(),
                    document.getSlug(),
                    document.getName(),
                    document.getDescription(),
                    document.getBrandId(),
                    document.getBrandName(),
                    document.getCategoryId(),
                    document.getCategoryName(),
                    document.getCategoryPath(),
                    document.getBasePrice(),
                    document.getCurrency(),
                    document.getImageUrl(),
                    document.getAttributes(),
                    document.getTags(),
                    payload.status(),
                    payload.visible(),
                    payload.sourceUpdatedAt()
            );
        }

        if (changed) {
            documentRepository.save(document);
            logProjection("search.document.status_updated", payload.productId());
        }

        return changed;
    }

    @Transactional
    public int updateCategoryProjection(CatalogCategoryUpdatedPayload payload) {
        if (payload.categoryId() == null) {
            throw new BaseException(SearchErrorCode.INVALID_CATEGORY_ID);
        }

        List<ProductSearchDocument> documents = documentRepository.findByCategoryIdForUpdate(payload.categoryId());

        int updatedCount = 0;

        for (ProductSearchDocument document : documents) {
            boolean changed = document.updateCategoryProjection(
                    payload.categoryId(),
                    payload.categoryName(),
                    payload.categoryPath(),
                    payload.sourceUpdatedAt()
            );

            if (changed) {
                documentRepository.save(document);
                updatedCount++;
            }
        }

        log.info(
                "Search category projection updated, categoryId={}, updatedCount={}",
                payload.categoryId(),
                updatedCount
        );

        return updatedCount;
    }

    @Transactional
    public int updateBrandProjection(CatalogBrandUpdatedPayload payload) {
        if (payload.brandId() == null) {
            throw new BaseException(SearchErrorCode.INVALID_BRAND_ID);
        }

        List<ProductSearchDocument> documents = documentRepository.findByBrandIdForUpdate(payload.brandId());

        int updatedCount = 0;

        for (ProductSearchDocument document : documents) {
            boolean changed = document.updateBrandProjection(
                    payload.brandId(),
                    payload.brandName(),
                    payload.sourceUpdatedAt()
            );

            if (changed) {
                documentRepository.save(document);
                updatedCount++;
            }
        }

        log.info(
                "Search brand projection updated, brandId={}, updatedCount={}",
                payload.brandId(),
                updatedCount
        );

        return updatedCount;
    }

    @Transactional
    public boolean updateStockProjection(InventoryStockProjectionPayload payload) {
        validateProductId(payload.productId());

        ProductSearchDocument document = documentRepository.findByProductIdForUpdate(payload.productId())
                .orElseThrow(() -> new BaseException(SearchErrorCode.PRODUCT_SEARCH_DOCUMENT_NOT_FOUND));

        boolean changed = document.updateStockProjection(
                payload.availableQuantity(),
                payload.stockStatus(),
                payload.stockUpdatedAt()
        );

        if (changed) {
            documentRepository.save(document);
            logProjection("search.document.stock_updated", payload.productId());
        }

        return changed;
    }

    @Transactional
    public boolean updatePromotionProjection(PromotionProjectionPayload payload) {
        validateProductId(payload.productId());

        ProductSearchDocument document = documentRepository.findByProductIdForUpdate(payload.productId())
                .orElseThrow(() -> new BaseException(SearchErrorCode.PRODUCT_SEARCH_DOCUMENT_NOT_FOUND));

        boolean changed = document.updatePromotionProjection(
                payload.hasActivePromotion(),
                payload.hasDiscount(),
                payload.discountedPrice(),
                payload.promotionBadge(),
                payload.promotionUpdatedAt()
        );

        if (changed) {
            documentRepository.save(document);
            logProjection("search.document.promotion_updated", payload.productId());
        }

        return changed;
    }


    @Transactional
    public boolean clearPromotionProjection(PromotionProjectionPayload payload) {
        validateProductId(payload.productId());

        ProductSearchDocument document = documentRepository.findByProductIdForUpdate(payload.productId())
                .orElseThrow(() -> new BaseException(SearchErrorCode.PRODUCT_SEARCH_DOCUMENT_NOT_FOUND));

        boolean changed = document.clearPromotionProjection(payload.promotionUpdatedAt());

        if (changed) {
            documentRepository.save(document);
            logProjection("search.document.promotion_cleared", payload.productId());
        }

        return changed;
    }

    @Transactional
    public boolean updateRatingProjection(RatingSummaryProjectionPayload payload) {
        validateProductId(payload.productId());

        ProductSearchDocument document = documentRepository.findByProductIdForUpdate(payload.productId())
                .orElseThrow(() -> new BaseException(SearchErrorCode.PRODUCT_SEARCH_DOCUMENT_NOT_FOUND));

        boolean changed = document.updateRatingProjection(
                payload.averageRating(),
                payload.reviewCount(),
                payload.effectiveUpdatedAt()
        );

        if (changed) {
            documentRepository.save(document);
            logProjection("search.document.rating_updated", payload.productId());
        }

        return changed;
    }

    private void validateProductId(UUID productId) {
        if (productId == null) {
            throw new BaseException(SearchErrorCode.INVALID_PRODUCT_ID);
        }
    }

    private void logProjection(String eventName, UUID productId) {
        try {
            MDC.put("eventName", eventName);
            MDC.put("productId", productId.toString());
            log.info("Search projection updated, eventName={}, productId={}", eventName, productId);
        } finally {
            MDC.remove("eventName");
            MDC.remove("productId");
        }
    }

    private void logBusinessFailure(String eventName, BaseException ex) {
        MDC.put("eventName", eventName);
        MDC.put("errorCode", ex.getErrorCode().code());
        log.warn("Search projection failed, errorCode={}", ex.getErrorCode().code());
    }

    private void clearMdc() {
        MDC.remove("eventName");
        MDC.remove("errorCode");
        MDC.remove("productId");
    }
}