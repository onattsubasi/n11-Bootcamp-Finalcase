package com.onatsubasi.finalcase.catalog.infrastructure.mapper;

import com.onatsubasi.finalcase.catalog.application.dto.response.CategoryResponse;
import com.onatsubasi.finalcase.catalog.domain.entity.Category;
import org.springframework.stereotype.Component;

@Component
public class CategoryMapper {

    public CategoryResponse toResponse(Category category) {
        return new CategoryResponse(
                category.getId(),
                category.getName(),
                category.getSlug(),
                category.getDescription(),
                category.getParentId(),
                category.getPath(),
                category.getLevel(),
                category.getStatus(),
                category.isActive(),
                category.getSortOrder(),
                category.getCreatedAt(),
                category.getUpdatedAt()
        );
    }
}