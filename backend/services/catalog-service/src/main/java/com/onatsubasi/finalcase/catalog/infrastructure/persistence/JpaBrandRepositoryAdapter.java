package com.onatsubasi.finalcase.catalog.infrastructure.persistence;

import com.onatsubasi.finalcase.catalog.domain.entity.Brand;
import com.onatsubasi.finalcase.catalog.domain.repository.BrandQuery;
import com.onatsubasi.finalcase.catalog.domain.repository.BrandRepository;
import com.onatsubasi.finalcase.catalog.domain.repository.CatalogPage;
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
public class JpaBrandRepositoryAdapter implements BrandRepository {

    private final SpringDataBrandJpaRepository springDataRepository;

    @Override
    public Brand save(Brand brand) {
        return springDataRepository.save(brand);
    }

    @Override
    public Optional<Brand> findById(UUID id) {
        return springDataRepository.findById(id);
    }

    @Override
    public Optional<Brand> findBySlug(String slug) {
        return springDataRepository.findBySlug(slug);
    }

    @Override
    public List<Brand> findByIds(Collection<UUID> ids) {
        if (ids == null || ids.isEmpty()) {
            return List.of();
        }

        return springDataRepository.findByIdIn(ids);
    }

    @Override
    public CatalogPage<Brand> findAll(BrandQuery query) {
        PageRequest pageRequest = PageRequest.of(
                query.page(),
                query.size(),
                Sort.by(Sort.Direction.ASC, "name")
        );

        Page<Brand> page = springDataRepository.findAll(
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
    public boolean existsBySlugAndIdNot(String slug, UUID brandId) {
        return springDataRepository.existsBySlugAndIdNot(slug, brandId);
    }

    private Specification<Brand> specification(BrandQuery query) {
        return (root, criteriaQuery, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (query.keyword() != null) {
                String pattern = "%" + query.keyword().toLowerCase() + "%";

                predicates.add(criteriaBuilder.or(
                        criteriaBuilder.like(criteriaBuilder.lower(root.get("name")), pattern),
                        criteriaBuilder.like(criteriaBuilder.lower(root.get("slug")), pattern),
                        criteriaBuilder.like(criteriaBuilder.lower(root.get("description")), pattern)
                ));
            }

            if (query.status() != null) {
                predicates.add(criteriaBuilder.equal(root.get("status"), query.status()));
            }

            return criteriaBuilder.and(predicates.toArray(Predicate[]::new));
        };
    }
}