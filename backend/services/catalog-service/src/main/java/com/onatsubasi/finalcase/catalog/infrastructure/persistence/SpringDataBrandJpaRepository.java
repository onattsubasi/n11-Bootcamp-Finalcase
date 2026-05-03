package com.onatsubasi.finalcase.catalog.infrastructure.persistence;

import com.onatsubasi.finalcase.catalog.domain.entity.Brand;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface SpringDataBrandJpaRepository
        extends JpaRepository<Brand, UUID>, JpaSpecificationExecutor<Brand> {

    Optional<Brand> findBySlug(String slug);

    List<Brand> findByIdIn(Collection<UUID> ids);

    boolean existsBySlug(String slug);

    boolean existsBySlugAndIdNot(String slug, UUID id);
}