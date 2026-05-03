package com.onatsubasi.finalcase.catalog.infrastructure.persistence;

import com.onatsubasi.finalcase.catalog.domain.enums.ProductStatus;
import com.onatsubasi.finalcase.catalog.domain.entity.Product;
import com.onatsubasi.finalcase.catalog.domain.repository.CatalogPage;
import com.onatsubasi.finalcase.catalog.domain.repository.ProductQuery;
import com.onatsubasi.finalcase.catalog.domain.repository.ProductRepository;
import com.onatsubasi.finalcase.catalog.domain.valueobject.BrandSnapshot;
import com.onatsubasi.finalcase.catalog.domain.valueobject.CategorySnapshot;
import jakarta.persistence.criteria.Predicate;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Repository;

import java.util.*;

@Repository
@RequiredArgsConstructor
public class JpaProductRepositoryAdapter implements ProductRepository {

    private final SpringDataProductJpaRepository springDataRepository;

    @Override
    public Product save(Product product) {
        return springDataRepository.save(product);
    }

    @Override
    public List<Product> saveAll(Collection<Product> products) {
        if (products == null || products.isEmpty()) {
            return List.of();
        }

        return springDataRepository.saveAll(products);
    }

    @Override
    public Optional<Product> findById(UUID id) {
        return springDataRepository.findById(id);
    }

    @Override
    public Optional<Product> findBySku(String sku) {
        return springDataRepository.findBySku(sku);
    }

    @Override
    public Optional<Product> findBySlug(String slug) {
        return springDataRepository.findBySlug(slug);
    }

    @Override
    public List<Product> findByIds(Collection<UUID> ids) {
        if (ids == null || ids.isEmpty()) {
            return List.of();
        }

        return springDataRepository.findByIdIn(ids);
    }

    @Override
    public List<Product> findActiveByIds(Collection<UUID> ids) {
        if (ids == null || ids.isEmpty()) {
            return List.of();
        }

        return springDataRepository.findByIdInAndStatus(ids, ProductStatus.ACTIVE);
    }

    @Override
    public List<Product> findByCategoryIdAndStatusNot(UUID categoryId, ProductStatus status) {
        return springDataRepository.findByCategoryIdAndStatusNot(categoryId, status);
    }

    @Override
    public List<Product> findByBrandIdAndStatusNot(UUID brandId, ProductStatus status) {
        return springDataRepository.findByBrandIdAndStatusNot(brandId, status);
    }

    @Override
    public CatalogPage<Product> findAll(ProductQuery query) {
        PageRequest pageRequest = PageRequest.of(
                query.page(),
                query.size(),
                Sort.by(
                        Sort.Order.desc("updatedAt"),
                        Sort.Order.desc("createdAt")
                )
        );

        Page<Product> page = springDataRepository.findAll(
                specification(query),
                pageRequest
        );

        return new CatalogPage<>(
                page.getContent(),
                page.getNumber(),
                page.getSize(),
                page.getTotalElements(),
                page.getTotalPages()
        );
    }

    @Override
    public boolean existsBySku(String sku) {
        return springDataRepository.existsBySku(sku);
    }

    @Override
    public boolean existsBySlug(String slug) {
        return springDataRepository.existsBySlug(slug);
    }

    @Override
    public boolean existsBySkuAndIdNot(String sku, UUID productId) {
        return springDataRepository.existsBySkuAndIdNot(sku, productId);
    }

    @Override
    public boolean existsBySlugAndIdNot(String slug, UUID productId) {
        return springDataRepository.existsBySlugAndIdNot(slug, productId);
    }

    @Override
    public long countByCategoryIdAndStatusNot(UUID categoryId, ProductStatus status) {
        return springDataRepository.countByCategoryIdAndStatusNot(categoryId, status);
    }

    @Override
    public long countByBrandIdAndStatusNot(UUID brandId, ProductStatus status) {
        return springDataRepository.countByBrandIdAndStatusNot(brandId, status);
    }

    @Override
    public void updateCategorySnapshot(UUID categoryId, CategorySnapshot snapshot) {
        List<Product> affectedProducts = springDataRepository.findByCategoryIdAndStatusNot(
                categoryId,
                ProductStatus.DELETED
        );

        if (affectedProducts.isEmpty()) {
            return;
        }

        affectedProducts.forEach(product -> product.changeCategory(snapshot));
        springDataRepository.saveAll(affectedProducts);
    }

    @Override
    public void updateBrandSnapshot(UUID brandId, BrandSnapshot snapshot) {
        List<Product> affectedProducts = springDataRepository.findByBrandIdAndStatusNot(
                brandId,
                ProductStatus.DELETED
        );

        if (affectedProducts.isEmpty()) {
            return;
        }

        affectedProducts.forEach(product -> product.changeBrand(snapshot));
        springDataRepository.saveAll(affectedProducts);
    }

    private Specification<Product> specification(ProductQuery query) {
        return (root, criteriaQuery, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (query.keyword() != null) {
                String pattern = "%" + query.keyword().toLowerCase() + "%";

                predicates.add(criteriaBuilder.or(
                        criteriaBuilder.like(criteriaBuilder.lower(root.get("name")), pattern),
                        criteriaBuilder.like(criteriaBuilder.lower(root.get("slug")), pattern),
                        criteriaBuilder.like(criteriaBuilder.lower(root.get("sku")), pattern),
                        criteriaBuilder.like(criteriaBuilder.lower(root.get("description")), pattern)
                ));
            }

            if (query.status() != null) {
                predicates.add(criteriaBuilder.equal(root.get("status"), query.status()));
            } else {
                predicates.add(criteriaBuilder.notEqual(root.get("status"), ProductStatus.DELETED));
            }

            if (query.categoryId() != null) {
                predicates.add(criteriaBuilder.equal(
                        root.get("category").get("id"),
                        query.categoryId()
                ));
            }

            if (query.brandId() != null) {
                predicates.add(criteriaBuilder.equal(
                        root.get("brand").get("id"),
                        query.brandId()
                ));
            }

            if (query.storeId() != null) {
                predicates.add(criteriaBuilder.equal(
                        root.get("ownership").get("storeId"),
                        query.storeId()
                ));
            }

            return criteriaBuilder.and(predicates.toArray(Predicate[]::new));
        };
    }
}