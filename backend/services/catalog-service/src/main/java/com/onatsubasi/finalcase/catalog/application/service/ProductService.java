package com.onatsubasi.finalcase.catalog.application.service;

import com.onatsubasi.finalcase.catalog.application.dto.request.CreateProductRequest;
import com.onatsubasi.finalcase.catalog.application.dto.request.ProductSnapshotRequest;
import com.onatsubasi.finalcase.catalog.application.dto.request.UpdateProductRequest;
import com.onatsubasi.finalcase.catalog.application.dto.response.ProductResponse;
import com.onatsubasi.finalcase.catalog.application.dto.response.ProductSnapshotResponse;
import com.onatsubasi.finalcase.catalog.application.dto.response.ProductSummaryResponse;
import com.onatsubasi.finalcase.catalog.application.port.CatalogEventPublisher;
import com.onatsubasi.finalcase.catalog.application.support.SlugGenerator;
import com.onatsubasi.finalcase.catalog.domain.enums.ProductStatus;
import com.onatsubasi.finalcase.catalog.domain.exception.CatalogErrorCode;
import com.onatsubasi.finalcase.catalog.domain.entity.Brand;
import com.onatsubasi.finalcase.catalog.domain.entity.Category;
import com.onatsubasi.finalcase.catalog.domain.entity.Product;
import com.onatsubasi.finalcase.catalog.domain.repository.CatalogPage;
import com.onatsubasi.finalcase.catalog.domain.repository.ProductQuery;
import com.onatsubasi.finalcase.catalog.domain.repository.ProductRepository;
import com.onatsubasi.finalcase.catalog.domain.valueobject.BrandSnapshot;
import com.onatsubasi.finalcase.catalog.domain.valueobject.Money;
import com.onatsubasi.finalcase.catalog.domain.valueobject.ProductOwnership;
import com.onatsubasi.finalcase.catalog.infrastructure.config.CatalogProperties;
import com.onatsubasi.finalcase.catalog.infrastructure.mapper.ProductMapper;
import com.onatsubasi.finalcase.common.core.exception.BaseException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProductService {

    private final ProductRepository productRepository;
    private final BrandService brandService;
    private final CategoryService categoryService;
    private final ProductMapper productMapper;
    private final CatalogEventPublisher eventPublisher;
    private final SlugGenerator slugGenerator;
    private final CatalogProperties catalogProperties;

    @Transactional
    public ProductResponse create(CreateProductRequest request) {
        try {
            MDC.put("eventName", "catalog.product.create.started");
            log.info("Product creation started, sku={}, brandId={}, categoryId={}",
                    request.sku(),
                    request.brandId(),
                    request.categoryId());

            String sku = normalizeRequired(request.sku(), "Product SKU is required");

            String slug = slugGenerator.generate(
                    request.slug(),
                    request.name(),
                    CatalogErrorCode.INVALID_PRODUCT_DATA
            );

            if (productRepository.existsBySku(sku)) {
                throw new BaseException(CatalogErrorCode.PRODUCT_SKU_ALREADY_EXISTS);
            }

            if (productRepository.existsBySlug(slug)) {
                throw new BaseException(CatalogErrorCode.PRODUCT_SLUG_ALREADY_EXISTS);
            }

            Brand brand = brandService.getActiveBrandOrThrow(request.brandId());
            Category category = categoryService.getActiveCategoryOrThrow(request.categoryId());

            Product product = Product.createDraft(
                    sku,
                    request.name(),
                    slug,
                    request.description(),
                    Money.of(
                            request.priceAmount(),
                            resolveCurrency(request.currency())
                    ),
                    BrandSnapshot.from(brand),
                    categoryService.buildSnapshot(category),
                    ProductOwnership.platform(
                            catalogProperties.platformStore().id(),
                            catalogProperties.platformStore().name()
                    ),
                    productMapper.toImages(request.images()),
                    request.attributes()
            );

            if (Boolean.TRUE.equals(request.publish())) {
                product.activate();
            }

            Product saved = productRepository.save(product);
            eventPublisher.publishProductCreated(saved);

            MDC.put("eventName", "catalog.product.created");
            log.info("Product created, productId={}, sku={}, status={}",
                    saved.getId(),
                    saved.getSku(),
                    saved.getStatus());

            return productMapper.toResponse(saved);
        } catch (BaseException ex) {
            logBusinessFailure("catalog.product.create.failed", ex);
            throw ex;
        } finally {
            clearMdc();
        }
    }

    @Transactional
    public ProductResponse update(UUID productId, UpdateProductRequest request) {
        try {
            MDC.put("eventName", "catalog.product.update.started");
            log.info("Product update started, productId={}, brandId={}, categoryId={}",
                    productId,
                    request.brandId(),
                    request.categoryId());

            Product product = getProductOrThrow(productId);

            ProductStatus previousStatus = product.getStatus();
            Money previousPrice = product.getBasePrice();

            String slug = slugGenerator.generate(
                    request.slug(),
                    request.name(),
                    CatalogErrorCode.INVALID_PRODUCT_DATA
            );

            if (productRepository.existsBySlugAndIdNot(slug, productId)) {
                throw new BaseException(CatalogErrorCode.PRODUCT_SLUG_ALREADY_EXISTS);
            }

            Brand brand = brandService.getActiveBrandOrThrow(request.brandId());
            Category category = categoryService.getActiveCategoryOrThrow(request.categoryId());

            product.updateDetails(
                    request.name(),
                    slug,
                    request.description(),
                    Money.of(
                            request.priceAmount(),
                            resolveCurrency(request.currency())
                    ),
                    BrandSnapshot.from(brand),
                    categoryService.buildSnapshot(category),
                    ProductOwnership.platform(
                            catalogProperties.platformStore().id(),
                            catalogProperties.platformStore().name()
                    ),
                    productMapper.toImages(request.images()),
                    request.attributes()
            );

            if (Boolean.TRUE.equals(request.publish())) {
                product.activate();
            }

            Product saved = productRepository.save(product);

            eventPublisher.publishProductUpdated(saved);

            if (hasPriceChanged(previousPrice, saved.getBasePrice())) {
                eventPublisher.publishProductPriceChanged(saved);
            }

            if (previousStatus != saved.getStatus()) {
                eventPublisher.publishProductStatusChanged(saved);
            }

            MDC.put("eventName", "catalog.product.updated");
            log.info("Product updated, productId={}, sku={}, status={}",
                    saved.getId(),
                    saved.getSku(),
                    saved.getStatus());

            return productMapper.toResponse(saved);
        } catch (BaseException ex) {
            logBusinessFailure("catalog.product.update.failed", ex);
            throw ex;
        } finally {
            clearMdc();
        }
    }

    @Transactional
    public ProductResponse activate(UUID productId) {
        try {
            MDC.put("eventName", "catalog.product.activate.started");
            log.info("Product activation started, productId={}", productId);

            Product product = getProductOrThrow(productId);
            product.activate();

            Product saved = productRepository.save(product);
            eventPublisher.publishProductStatusChanged(saved);

            MDC.put("eventName", "catalog.product.activated");
            log.info("Product activated, productId={}, sku={}", saved.getId(), saved.getSku());

            return productMapper.toResponse(saved);
        } catch (BaseException ex) {
            logBusinessFailure("catalog.product.activate.failed", ex);
            throw ex;
        } finally {
            clearMdc();
        }
    }

    @Transactional
    public ProductResponse suspend(UUID productId) {
        try {
            MDC.put("eventName", "catalog.product.suspend.started");
            log.info("Product suspension started, productId={}", productId);

            Product product = getProductOrThrow(productId);
            product.suspend();

            Product saved = productRepository.save(product);
            eventPublisher.publishProductStatusChanged(saved);

            MDC.put("eventName", "catalog.product.suspended");
            log.info("Product suspended, productId={}, sku={}", saved.getId(), saved.getSku());

            return productMapper.toResponse(saved);
        } catch (BaseException ex) {
            logBusinessFailure("catalog.product.suspend.failed", ex);
            throw ex;
        } finally {
            clearMdc();
        }
    }

    @Transactional
    public void delete(UUID productId) {
        try {
            MDC.put("eventName", "catalog.product.delete.started");
            log.info("Product delete started, productId={}", productId);

            Product product = getProductOrThrow(productId);
            product.softDelete();

            Product saved = productRepository.save(product);
            eventPublisher.publishProductDeleted(saved);

            MDC.put("eventName", "catalog.product.deleted");
            log.info("Product soft-deleted, productId={}, sku={}", saved.getId(), saved.getSku());
        } catch (BaseException ex) {
            logBusinessFailure("catalog.product.delete.failed", ex);
            throw ex;
        } finally {
            clearMdc();
        }
    }

    @Transactional(readOnly = true)
    public ProductResponse getById(UUID productId) {
        return productMapper.toResponse(getProductOrThrow(productId));
    }

    @Transactional(readOnly = true)
    public CatalogPage<ProductSummaryResponse> list(ProductQuery query) {
        CatalogPage<Product> page = productRepository.findAll(query);

        return new CatalogPage<>(
                page.content().stream().map(productMapper::toSummaryResponse).toList(),
                page.page(),
                page.size(),
                page.totalElements(),
                page.totalPages()
        );
    }

    @Transactional(readOnly = true)
    public List<ProductSnapshotResponse> getSnapshots(ProductSnapshotRequest request) {
        try {
            MDC.put("eventName", "catalog.snapshot.requested");
            log.info("Product snapshot request received, requestedCount={}",
                    request.productIds() == null ? 0 : request.productIds().size());

            Set<UUID> productIds = request.productIds()
                    .stream()
                    .filter(Objects::nonNull)
                    .collect(Collectors.toCollection(LinkedHashSet::new));

            if (productIds.isEmpty()) {
                throw new BaseException(
                        CatalogErrorCode.INVALID_PRODUCT_DATA,
                        "Product ids are required"
                );
            }

            List<Product> products = productRepository.findByIds(productIds);

            Map<UUID, Product> productById = products.stream()
                    .collect(Collectors.toMap(Product::getId, Function.identity()));

            List<ProductSnapshotResponse> response = productIds.stream()
                    .map(productId -> {
                        Product product = productById.get(productId);

                        if (product == null) {
                            throw new BaseException(
                                    CatalogErrorCode.PRODUCT_NOT_FOUND,
                                    "Product not found: " + productId
                            );
                        }

                        return productMapper.toSnapshotResponse(product);
                    })
                    .toList();

            MDC.put("eventName", "catalog.snapshot.completed");
            log.info("Product snapshot request completed, returnedCount={}", response.size());

            return response;
        } catch (BaseException ex) {
            logBusinessFailure("catalog.snapshot.failed", ex);
            throw ex;
        } finally {
            clearMdc();
        }
    }

    private Product getProductOrThrow(UUID productId) {
        return productRepository.findById(productId)
                .orElseThrow(() -> new BaseException(CatalogErrorCode.PRODUCT_NOT_FOUND));
    }

    private String resolveCurrency(String currency) {
        return currency == null || currency.isBlank()
                ? "TRY"
                : currency.trim().toUpperCase();
    }

    private String normalizeRequired(String value, String message) {
        if (value == null || value.isBlank()) {
            throw new BaseException(CatalogErrorCode.INVALID_PRODUCT_DATA, message);
        }

        return value.trim();
    }

    private boolean hasPriceChanged(Money previousPrice, Money newPrice) {
        if (previousPrice == null && newPrice == null) {
            return false;
        }

        if (previousPrice == null || newPrice == null) {
            return true;
        }

        return previousPrice.getAmount().compareTo(newPrice.getAmount()) != 0
                || !previousPrice.getCurrency().equals(newPrice.getCurrency());
    }

    private void logBusinessFailure(String eventName, BaseException ex) {
        MDC.put("eventName", eventName);
        MDC.put("errorCode", ex.getErrorCode().code());
        log.warn("Product operation failed, errorCode={}", ex.getErrorCode().code());
    }

    private void clearMdc() {
        MDC.remove("eventName");
        MDC.remove("errorCode");
    }
}