package com.onatsubasi.finalcase.catalog.application.service;

import com.onatsubasi.finalcase.catalog.application.dto.request.CreateBrandRequest;
import com.onatsubasi.finalcase.catalog.application.dto.request.UpdateBrandRequest;
import com.onatsubasi.finalcase.catalog.application.dto.response.BrandResponse;
import com.onatsubasi.finalcase.catalog.application.port.CatalogEventPublisher;
import com.onatsubasi.finalcase.catalog.application.support.SlugGenerator;
import com.onatsubasi.finalcase.catalog.domain.enums.ProductStatus;
import com.onatsubasi.finalcase.catalog.domain.exception.CatalogErrorCode;
import com.onatsubasi.finalcase.catalog.domain.entity.Brand;
import com.onatsubasi.finalcase.catalog.domain.repository.BrandQuery;
import com.onatsubasi.finalcase.catalog.domain.repository.BrandRepository;
import com.onatsubasi.finalcase.catalog.domain.repository.CatalogPage;
import com.onatsubasi.finalcase.catalog.domain.repository.ProductRepository;
import com.onatsubasi.finalcase.catalog.domain.valueobject.BrandSnapshot;
import com.onatsubasi.finalcase.catalog.infrastructure.mapper.BrandMapper;
import com.onatsubasi.finalcase.common.core.exception.BaseException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class BrandService {

    private final BrandRepository brandRepository;
    private final ProductRepository productRepository;
    private final BrandMapper brandMapper;
    private final SlugGenerator slugGenerator;
    private final CatalogEventPublisher eventPublisher;

    @Transactional
    public BrandResponse create(CreateBrandRequest request) {
        try {
            MDC.put("eventName", "catalog.brand.create.started");
            log.info("Brand creation started, name={}", request.name());

            String slug = slugGenerator.generate(
                    request.slug(),
                    request.name(),
                    CatalogErrorCode.INVALID_BRAND_DATA
            );

            if (brandRepository.existsBySlug(slug)) {
                throw new BaseException(CatalogErrorCode.BRAND_SLUG_ALREADY_EXISTS);
            }

            Brand brand = Brand.create(
                    request.name(),
                    slug,
                    request.description(),
                    request.logoUrl()
            );

            Brand saved = brandRepository.save(brand);
            eventPublisher.publishBrandCreated(saved);

            MDC.put("eventName", "catalog.brand.created");
            log.info("Brand created, brandId={}, slug={}", saved.getId(), saved.getSlug());

            return brandMapper.toResponse(saved);
        } catch (BaseException ex) {
            logBusinessFailure("catalog.brand.create.failed", ex);
            throw ex;
        } finally {
            clearMdc();
        }
    }

    @Transactional
    public BrandResponse update(UUID brandId, UpdateBrandRequest request) {
        try {
            MDC.put("eventName", "catalog.brand.update.started");
            log.info("Brand update started, brandId={}", brandId);

            Brand brand = getBrandOrThrow(brandId);

            String slug = slugGenerator.generate(
                    request.slug(),
                    request.name(),
                    CatalogErrorCode.INVALID_BRAND_DATA
            );

            if (brandRepository.existsBySlugAndIdNot(slug, brandId)) {
                throw new BaseException(CatalogErrorCode.BRAND_SLUG_ALREADY_EXISTS);
            }

            brand.update(
                    request.name(),
                    slug,
                    request.description(),
                    request.logoUrl()
            );

            Brand saved = brandRepository.save(brand);

            productRepository.updateBrandSnapshot(
                    saved.getId(),
                    BrandSnapshot.from(saved)
            );

            eventPublisher.publishBrandUpdated(saved);

            MDC.put("eventName", "catalog.brand.updated");
            log.info("Brand updated, brandId={}, slug={}", saved.getId(), saved.getSlug());

            return brandMapper.toResponse(saved);
        } catch (BaseException ex) {
            logBusinessFailure("catalog.brand.update.failed", ex);
            throw ex;
        } finally {
            clearMdc();
        }
    }

    @Transactional
    public BrandResponse activate(UUID brandId) {
        try {
            MDC.put("eventName", "catalog.brand.activate.started");
            log.info("Brand activation started, brandId={}", brandId);

            Brand brand = getBrandOrThrow(brandId);
            brand.activate();

            Brand saved = brandRepository.save(brand);

            productRepository.updateBrandSnapshot(
                    saved.getId(),
                    BrandSnapshot.from(saved)
            );

            eventPublisher.publishBrandStatusChanged(saved);

            MDC.put("eventName", "catalog.brand.activated");
            log.info("Brand activated, brandId={}", saved.getId());

            return brandMapper.toResponse(saved);
        } catch (BaseException ex) {
            logBusinessFailure("catalog.brand.activate.failed", ex);
            throw ex;
        } finally {
            clearMdc();
        }
    }

    @Transactional
    public BrandResponse suspend(UUID brandId) {
        try {
            MDC.put("eventName", "catalog.brand.suspend.started");
            log.info("Brand suspension started, brandId={}", brandId);

            Brand brand = getBrandOrThrow(brandId);

            long productCount = productRepository.countByBrandIdAndStatusNot(
                    brandId,
                    ProductStatus.DELETED
            );

            if (productCount > 0) {
                throw new BaseException(CatalogErrorCode.BRAND_IN_USE);
            }

            brand.suspend();

            Brand saved = brandRepository.save(brand);
            eventPublisher.publishBrandStatusChanged(saved);

            MDC.put("eventName", "catalog.brand.suspended");
            log.info("Brand suspended, brandId={}", saved.getId());

            return brandMapper.toResponse(saved);
        } catch (BaseException ex) {
            logBusinessFailure("catalog.brand.suspend.failed", ex);
            throw ex;
        } finally {
            clearMdc();
        }
    }

    @Transactional
    public BrandResponse deactivate(UUID brandId) {
        return suspend(brandId);
    }

    @Transactional
    public void delete(UUID brandId) {
        try {
            MDC.put("eventName", "catalog.brand.delete.started");
            log.info("Brand delete started, brandId={}", brandId);

            Brand brand = getBrandOrThrow(brandId);

            long productCount = productRepository.countByBrandIdAndStatusNot(
                    brandId,
                    ProductStatus.DELETED
            );

            if (productCount > 0) {
                throw new BaseException(CatalogErrorCode.BRAND_IN_USE);
            }

            brand.softDelete();

            Brand saved = brandRepository.save(brand);
            eventPublisher.publishBrandStatusChanged(saved);

            MDC.put("eventName", "catalog.brand.deleted");
            log.info("Brand soft-deleted, brandId={}", saved.getId());
        } catch (BaseException ex) {
            logBusinessFailure("catalog.brand.delete.failed", ex);
            throw ex;
        } finally {
            clearMdc();
        }
    }

    @Transactional(readOnly = true)
    public BrandResponse getById(UUID brandId) {
        return brandMapper.toResponse(getBrandOrThrow(brandId));
    }

    @Transactional(readOnly = true)
    public CatalogPage<BrandResponse> list(BrandQuery query) {
        CatalogPage<Brand> page = brandRepository.findAll(query);

        return new CatalogPage<>(
                page.content().stream().map(brandMapper::toResponse).toList(),
                page.page(),
                page.size(),
                page.totalElements(),
                page.totalPages()
        );
    }

    @Transactional(readOnly = true)
    Brand getActiveBrandOrThrow(UUID brandId) {
        Brand brand = getBrandOrThrow(brandId);

        if (!brand.isActive()) {
            throw new BaseException(CatalogErrorCode.BRAND_NOT_ACTIVE);
        }

        return brand;
    }

    private Brand getBrandOrThrow(UUID brandId) {
        return brandRepository.findById(brandId)
                .orElseThrow(() -> new BaseException(CatalogErrorCode.BRAND_NOT_FOUND));
    }

    private void logBusinessFailure(String eventName, BaseException ex) {
        MDC.put("eventName", eventName);
        MDC.put("errorCode", ex.getErrorCode().code());
        log.warn("Brand operation failed, errorCode={}", ex.getErrorCode().code());
    }

    private void clearMdc() {
        MDC.remove("eventName");
        MDC.remove("errorCode");
    }
}