package com.onatsubasi.finalcase.catalog.infrastructure.persistence;

import com.onatsubasi.finalcase.catalog.domain.entity.Category;
import com.onatsubasi.finalcase.catalog.domain.repository.CatalogPage;
import com.onatsubasi.finalcase.catalog.domain.repository.CategoryQuery;
import com.onatsubasi.finalcase.catalog.domain.repository.CategoryRepository;
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
public class JpaCategoryRepositoryAdapter implements CategoryRepository {

    private final SpringDataCategoryJpaRepository springDataRepository;

    @Override
    public Category save(Category category) {
        return springDataRepository.save(category);
    }

    @Override
    public List<Category> saveAll(Collection<Category> categories) {
        if (categories == null || categories.isEmpty()) {
            return List.of();
        }

        return springDataRepository.saveAll(categories);
    }

    @Override
    public Optional<Category> findById(UUID id) {
        return springDataRepository.findById(id);
    }

    @Override
    public Optional<Category> findBySlug(String slug) {
        return springDataRepository.findBySlug(slug);
    }

    @Override
    public Optional<Category> findByPath(String path) {
        return springDataRepository.findByPath(path);
    }

    @Override
    public List<Category> findByIds(Collection<UUID> ids) {
        if (ids == null || ids.isEmpty()) {
            return List.of();
        }

        return springDataRepository.findByIdIn(ids);
    }

    @Override
    public List<Category> findChildren(UUID parentId) {
        return springDataRepository.findByParentIdOrderBySortOrderAscNameAsc(parentId);
    }

    @Override
    public CatalogPage<Category> findAll(CategoryQuery query) {
        PageRequest pageRequest = PageRequest.of(
                query.page(),
                query.size(),
                Sort.by(
                        Sort.Order.asc("level"),
                        Sort.Order.asc("sortOrder"),
                        Sort.Order.asc("name")
                )
        );

        Page<Category> page = springDataRepository.findAll(
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
    public boolean existsBySlug(String slug) {
        return springDataRepository.existsBySlug(slug);
    }

    @Override
    public boolean existsByPath(String path) {
        return springDataRepository.existsByPath(path);
    }

    @Override
    public boolean existsBySlugAndIdNot(String slug, UUID categoryId) {
        return springDataRepository.existsBySlugAndIdNot(slug, categoryId);
    }

    @Override
    public boolean existsByPathAndIdNot(String path, UUID categoryId) {
        return springDataRepository.existsByPathAndIdNot(path, categoryId);
    }

    private Specification<Category> specification(CategoryQuery query) {
        return (root, criteriaQuery, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (query.keyword() != null) {
                String pattern = "%" + query.keyword().toLowerCase() + "%";

                predicates.add(criteriaBuilder.or(
                        criteriaBuilder.like(criteriaBuilder.lower(root.get("name")), pattern),
                        criteriaBuilder.like(criteriaBuilder.lower(root.get("slug")), pattern),
                        criteriaBuilder.like(criteriaBuilder.lower(root.get("path")), pattern),
                        criteriaBuilder.like(criteriaBuilder.lower(root.get("description")), pattern)
                ));
            }

            if (query.status() != null) {
                predicates.add(criteriaBuilder.equal(root.get("status"), query.status()));
            }

            if (query.parentId() != null) {
                predicates.add(criteriaBuilder.equal(root.get("parentId"), query.parentId()));
            }

            return criteriaBuilder.and(predicates.toArray(Predicate[]::new));
        };
    }
}