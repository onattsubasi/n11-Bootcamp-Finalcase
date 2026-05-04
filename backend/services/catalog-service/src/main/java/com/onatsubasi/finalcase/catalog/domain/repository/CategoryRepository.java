package com.onatsubasi.finalcase.catalog.domain.repository;

import com.onatsubasi.finalcase.catalog.domain.entity.Category;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface CategoryRepository {

    Category save(Category category);

    List<Category> saveAll(Collection<Category> categories);

    Optional<Category> findById(UUID id);

    Optional<Category> findBySlug(String slug);

    Optional<Category> findByPath(String path);

    List<Category> findByIds(Collection<UUID> ids);

    List<Category> findChildren(UUID parentId);

    CatalogPage<Category> findAll(CategoryQuery query);

    boolean existsBySlug(String slug);

    boolean existsByPath(String path);

    boolean existsBySlugAndIdNot(String slug, UUID categoryId);

    boolean existsByPathAndIdNot(String path, UUID categoryId);
}