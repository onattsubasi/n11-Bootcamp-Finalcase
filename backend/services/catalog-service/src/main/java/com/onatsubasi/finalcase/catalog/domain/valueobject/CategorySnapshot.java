package com.onatsubasi.finalcase.catalog.domain.valueobject;

import com.onatsubasi.finalcase.catalog.domain.exception.CatalogErrorCode;
import com.onatsubasi.finalcase.catalog.domain.entity.Category;
import com.onatsubasi.finalcase.common.core.exception.BaseException;
import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Getter
@Embeddable
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class CategorySnapshot {

    @Column(name = "id", nullable = false)
    private UUID id;

    @Column(name = "name", nullable = false, length = 120)
    private String name;

    @Column(name = "slug", nullable = false, length = 140)
    private String slug;

    @Column(name = "path", nullable = false, length = 1000)
    private String path;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "ancestors", nullable = false, columnDefinition = "jsonb")
    private List<CategoryAncestor> ancestors = new ArrayList<>();

    public CategorySnapshot(
            UUID id,
            String name,
            String slug,
            String path,
            List<CategoryAncestor> ancestors
    ) {
        if (id == null) {
            throw new BaseException(
                    CatalogErrorCode.INVALID_PRODUCT_DATA,
                    "Category snapshot id is required"
            );
        }

        if (name == null || name.isBlank()) {
            throw new BaseException(
                    CatalogErrorCode.INVALID_PRODUCT_DATA,
                    "Category snapshot name is required"
            );
        }

        if (slug == null || slug.isBlank()) {
            throw new BaseException(
                    CatalogErrorCode.INVALID_PRODUCT_DATA,
                    "Category snapshot slug is required"
            );
        }

        if (path == null || path.isBlank()) {
            throw new BaseException(
                    CatalogErrorCode.INVALID_PRODUCT_DATA,
                    "Category snapshot path is required"
            );
        }

        this.id = id;
        this.name = name.trim();
        this.slug = slug.trim();
        this.path = path.trim();
        this.ancestors = ancestors == null
                ? new ArrayList<>()
                : new ArrayList<>(ancestors);
    }

    public static CategorySnapshot from(
            Category category,
            List<CategoryAncestor> ancestors
    ) {
        return new CategorySnapshot(
                category.getId(),
                category.getName(),
                category.getSlug(),
                category.getPath(),
                ancestors
        );
    }

    public List<CategoryAncestor> getAncestors() {
        return List.copyOf(ancestors);
    }
}