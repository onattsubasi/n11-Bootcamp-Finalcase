package com.onatsubasi.finalcase.catalog.application.service;

import com.onatsubasi.finalcase.catalog.application.dto.request.CreateCategoryRequest;
import com.onatsubasi.finalcase.catalog.application.dto.request.UpdateCategoryRequest;
import com.onatsubasi.finalcase.catalog.application.dto.response.CategoryResponse;
import com.onatsubasi.finalcase.catalog.application.port.CatalogEventPublisher;
import com.onatsubasi.finalcase.catalog.application.support.SlugGenerator;
import com.onatsubasi.finalcase.catalog.domain.enums.ProductStatus;
import com.onatsubasi.finalcase.catalog.domain.exception.CatalogErrorCode;
import com.onatsubasi.finalcase.catalog.domain.entity.Category;
import com.onatsubasi.finalcase.catalog.domain.repository.CatalogPage;
import com.onatsubasi.finalcase.catalog.domain.repository.CategoryQuery;
import com.onatsubasi.finalcase.catalog.domain.repository.CategoryRepository;
import com.onatsubasi.finalcase.catalog.domain.repository.ProductRepository;
import com.onatsubasi.finalcase.catalog.domain.valueobject.CategoryAncestor;
import com.onatsubasi.finalcase.catalog.domain.valueobject.CategorySnapshot;
import com.onatsubasi.finalcase.catalog.infrastructure.mapper.CategoryMapper;
import com.onatsubasi.finalcase.common.core.exception.BaseException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class CategoryService {

    private final CategoryRepository categoryRepository;
    private final ProductRepository productRepository;
    private final CategoryMapper categoryMapper;
    private final CatalogEventPublisher eventPublisher;
    private final SlugGenerator slugGenerator;

    @Transactional
    public CategoryResponse create(CreateCategoryRequest request) {
        try {
            MDC.put("eventName", "catalog.category.create.started");
            log.info(
                    "Category creation started, name={}, parentId={}",
                    request.name(),
                    request.parentId()
            );

            String slug = slugGenerator.generate(
                    request.slug(),
                    request.name(),
                    CatalogErrorCode.INVALID_CATEGORY_DATA
            );

            if (categoryRepository.existsBySlug(slug)) {
                throw new BaseException(CatalogErrorCode.CATEGORY_SLUG_ALREADY_EXISTS);
            }

            Category category;

            if (request.parentId() == null) {
                if (categoryRepository.existsByPath(slug)) {
                    throw new BaseException(CatalogErrorCode.CATEGORY_PATH_ALREADY_EXISTS);
                }

                category = Category.createRoot(
                        request.name(),
                        slug,
                        request.description(),
                        normalizeSortOrder(request.sortOrder())
                );
            } else {
                Category parent = getActiveCategoryOrThrow(request.parentId());

                String path = parent.getPath() + "/" + slug;

                if (categoryRepository.existsByPath(path)) {
                    throw new BaseException(CatalogErrorCode.CATEGORY_PATH_ALREADY_EXISTS);
                }

                category = Category.createChild(
                        request.name(),
                        slug,
                        request.description(),
                        parent.getId(),
                        parent.getPath(),
                        parent.getLevel(),
                        normalizeSortOrder(request.sortOrder())
                );
            }

            Category saved = categoryRepository.save(category);
            eventPublisher.publishCategoryCreated(saved);

            MDC.put("eventName", "catalog.category.created");
            log.info(
                    "Category created, categoryId={}, path={}",
                    saved.getId(),
                    saved.getPath()
            );

            return categoryMapper.toResponse(saved);
        } catch (BaseException ex) {
            logBusinessFailure("catalog.category.create.failed", ex);
            throw ex;
        } finally {
            clearMdc();
        }
    }

    @Transactional
    public CategoryResponse update(UUID categoryId, UpdateCategoryRequest request) {
        try {
            MDC.put("eventName", "catalog.category.update.started");
            log.info(
                    "Category update started, categoryId={}, requestedParentId={}",
                    categoryId,
                    request.parentId()
            );

            Category category = getCategoryOrThrow(categoryId);

            String slug = slugGenerator.generate(
                    request.slug(),
                    request.name(),
                    CatalogErrorCode.INVALID_CATEGORY_DATA
            );

            if (categoryRepository.existsBySlugAndIdNot(slug, categoryId)) {
                throw new BaseException(CatalogErrorCode.CATEGORY_SLUG_ALREADY_EXISTS);
            }

            CategoryStructure structure = resolveCategoryStructure(
                    category,
                    request.parentId(),
                    slug
            );

            if (categoryRepository.existsByPathAndIdNot(structure.path(), categoryId)) {
                throw new BaseException(CatalogErrorCode.CATEGORY_PATH_ALREADY_EXISTS);
            }

            category.update(
                    request.name(),
                    slug,
                    request.description(),
                    structure.parentId(),
                    structure.path(),
                    structure.level(),
                    normalizeSortOrder(request.sortOrder())
            );

            Category saved = categoryRepository.save(category);

            propagateCategoryTreeSnapshots(saved);
            eventPublisher.publishCategoryUpdated(saved);

            MDC.put("eventName", "catalog.category.updated");
            log.info(
                    "Category updated, categoryId={}, path={}",
                    saved.getId(),
                    saved.getPath()
            );

            return categoryMapper.toResponse(saved);
        } catch (BaseException ex) {
            logBusinessFailure("catalog.category.update.failed", ex);
            throw ex;
        } finally {
            clearMdc();
        }
    }

    @Transactional
    public CategoryResponse activate(UUID categoryId) {
        try {
            MDC.put("eventName", "catalog.category.activate.started");
            log.info("Category activation started, categoryId={}", categoryId);

            Category category = getCategoryOrThrow(categoryId);
            category.activate();

            Category saved = categoryRepository.save(category);

            propagateCategoryTreeSnapshots(saved);
            eventPublisher.publishCategoryStatusChanged(saved);

            MDC.put("eventName", "catalog.category.activated");
            log.info("Category activated, categoryId={}", saved.getId());

            return categoryMapper.toResponse(saved);
        } catch (BaseException ex) {
            logBusinessFailure("catalog.category.activate.failed", ex);
            throw ex;
        } finally {
            clearMdc();
        }
    }

    @Transactional
    public CategoryResponse suspend(UUID categoryId) {
        return suspendCategory(categoryId, "suspend");
    }

    @Transactional
    public CategoryResponse deactivate(UUID categoryId) {
        /*
         * Backward-compatible alias for older controller naming.
         * Do not call suspend(categoryId) here because that would be
         * a transactional self-invocation.
         */
        return suspendCategory(categoryId, "deactivate");
    }

    @Transactional
    public void delete(UUID categoryId) {
        try {
            MDC.put("eventName", "catalog.category.delete.started");
            log.info("Category delete started, categoryId={}", categoryId);

            Category category = getCategoryOrThrow(categoryId);

            if (!categoryRepository.findChildren(categoryId).isEmpty()) {
                throw new BaseException(
                        CatalogErrorCode.INVALID_CATEGORY_DATA,
                        "Category with children cannot be deleted"
                );
            }

            long productCount = productRepository.countByCategoryIdAndStatusNot(
                    categoryId,
                    ProductStatus.DELETED
            );

            if (productCount > 0) {
                throw new BaseException(CatalogErrorCode.CATEGORY_IN_USE);
            }

            category.softDelete();

            Category saved = categoryRepository.save(category);
            eventPublisher.publishCategoryStatusChanged(saved);

            MDC.put("eventName", "catalog.category.deleted");
            log.info("Category soft-deleted, categoryId={}", saved.getId());
        } catch (BaseException ex) {
            logBusinessFailure("catalog.category.delete.failed", ex);
            throw ex;
        } finally {
            clearMdc();
        }
    }

    @Transactional(readOnly = true)
    public CategoryResponse getById(UUID categoryId) {
        return categoryMapper.toResponse(getCategoryOrThrow(categoryId));
    }

    @Transactional(readOnly = true)
    public CatalogPage<CategoryResponse> list(CategoryQuery query) {
        CatalogPage<Category> page = categoryRepository.findAll(query);

        return new CatalogPage<>(
                page.content().stream().map(categoryMapper::toResponse).toList(),
                page.page(),
                page.size(),
                page.totalElements(),
                page.totalPages()
        );
    }

    /*
     * Package-private helper intentionally has NO @Transactional.
     *
     * It is used by ProductService while ProductService already owns
     * the transaction boundary for product create/update operations.
     */
    Category getActiveCategoryOrThrow(UUID categoryId) {
        Category category = getCategoryOrThrow(categoryId);

        if (!category.isActive()) {
            throw new BaseException(CatalogErrorCode.CATEGORY_NOT_ACTIVE);
        }

        return category;
    }

    /*
     * Package-private helper intentionally has NO @Transactional.
     *
     * It is a pure snapshot builder and should run inside the caller's
     * current transaction when needed.
     */
    CategorySnapshot buildSnapshot(Category category) {
        return CategorySnapshot.from(
                category,
                buildAncestors(category)
        );
    }

    private CategoryResponse suspendCategory(UUID categoryId, String operationName) {
        try {
            MDC.put("eventName", "catalog.category." + operationName + ".started");
            log.info("Category suspension started, categoryId={}", categoryId);

            Category category = getCategoryOrThrow(categoryId);

            long productCount = productRepository.countByCategoryIdAndStatusNot(
                    categoryId,
                    ProductStatus.DELETED
            );

            if (productCount > 0) {
                throw new BaseException(CatalogErrorCode.CATEGORY_IN_USE);
            }

            category.suspend();

            Category saved = categoryRepository.save(category);
            eventPublisher.publishCategoryStatusChanged(saved);

            MDC.put("eventName", "catalog.category.suspended");
            log.info("Category suspended, categoryId={}", saved.getId());

            return categoryMapper.toResponse(saved);
        } catch (BaseException ex) {
            logBusinessFailure("catalog.category." + operationName + ".failed", ex);
            throw ex;
        } finally {
            clearMdc();
        }
    }

    private void propagateCategoryTreeSnapshots(Category category) {
        productRepository.updateCategorySnapshot(
                category.getId(),
                buildSnapshot(category)
        );

        List<Category> children = categoryRepository.findChildren(category.getId());

        for (Category child : children) {
            String newPath = category.getPath() + "/" + child.getSlug();
            int newLevel = category.getLevel() + 1;

            child.update(
                    child.getName(),
                    child.getSlug(),
                    child.getDescription(),
                    category.getId(),
                    newPath,
                    newLevel,
                    child.getSortOrder()
            );

            Category savedChild = categoryRepository.save(child);

            productRepository.updateCategorySnapshot(
                    savedChild.getId(),
                    buildSnapshot(savedChild)
            );

            propagateCategoryTreeSnapshots(savedChild);
        }
    }

    private List<CategoryAncestor> buildAncestors(Category category) {
        List<CategoryAncestor> ancestors = new ArrayList<>();

        UUID parentId = category.getParentId();

        while (parentId != null) {
            Category parent = getCategoryOrThrow(parentId);
            ancestors.add(0, CategoryAncestor.from(parent));
            parentId = parent.getParentId();
        }

        return ancestors;
    }

    private CategoryStructure resolveCategoryStructure(
            Category existingCategory,
            UUID requestedParentId,
            String slug
    ) {
        if (requestedParentId == null) {
            return new CategoryStructure(null, slug, 0);
        }

        if (requestedParentId.equals(existingCategory.getId())) {
            throw new BaseException(
                    CatalogErrorCode.INVALID_CATEGORY_DATA,
                    "Category cannot be parent of itself"
            );
        }

        Category parent = getActiveCategoryOrThrow(requestedParentId);

        if (parent.getPath().startsWith(existingCategory.getPath() + "/")) {
            throw new BaseException(
                    CatalogErrorCode.INVALID_CATEGORY_DATA,
                    "Category cannot be moved under one of its descendants"
            );
        }

        return new CategoryStructure(
                parent.getId(),
                parent.getPath() + "/" + slug,
                parent.getLevel() + 1
        );
    }

    private Category getCategoryOrThrow(UUID categoryId) {
        return categoryRepository.findById(categoryId)
                .orElseThrow(() -> new BaseException(CatalogErrorCode.CATEGORY_NOT_FOUND));
    }

    private int normalizeSortOrder(Integer sortOrder) {
        return sortOrder == null ? 0 : Math.max(sortOrder, 0);
    }

    private void logBusinessFailure(String eventName, BaseException ex) {
        MDC.put("eventName", eventName);
        MDC.put("errorCode", ex.getErrorCode().code());
        log.warn("Category operation failed, errorCode={}", ex.getErrorCode().code());
    }

    private void clearMdc() {
        MDC.remove("eventName");
        MDC.remove("errorCode");
    }

    private record CategoryStructure(
            UUID parentId,
            String path,
            int level
    ) {
    }
}