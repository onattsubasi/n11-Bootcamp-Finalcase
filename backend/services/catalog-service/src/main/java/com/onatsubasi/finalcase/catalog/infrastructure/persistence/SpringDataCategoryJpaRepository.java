package com.onatsubasi.finalcase.catalog.infrastructure.persistence;

import com.onatsubasi.finalcase.catalog.domain.entity.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface SpringDataCategoryJpaRepository
        extends JpaRepository<Category, UUID>, JpaSpecificationExecutor<Category> {

    Optional<Category> findBySlug(String slug);

    Optional<Category> findByPath(String path);

    List<Category> findByIdIn(Collection<UUID> ids);

    List<Category> findByParentIdOrderBySortOrderAscNameAsc(UUID parentId);

    boolean existsBySlug(String slug);

    boolean existsByPath(String path);

    boolean existsBySlugAndIdNot(String slug, UUID id);

    boolean existsByPathAndIdNot(String path, UUID id);
}