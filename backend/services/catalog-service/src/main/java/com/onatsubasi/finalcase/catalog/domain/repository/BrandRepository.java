package com.onatsubasi.finalcase.catalog.domain.repository;

import com.onatsubasi.finalcase.catalog.domain.entity.Brand;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface BrandRepository {

    Brand save(Brand brand);

    Optional<Brand> findById(UUID id);

    Optional<Brand> findBySlug(String slug);

    List<Brand> findByIds(Collection<UUID> ids);

    CatalogPage<Brand> findAll(BrandQuery query);

    boolean existsBySlug(String slug);

    boolean existsBySlugAndIdNot(String slug, UUID brandId);
}