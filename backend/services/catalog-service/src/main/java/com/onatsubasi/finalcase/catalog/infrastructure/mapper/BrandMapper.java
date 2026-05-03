package com.onatsubasi.finalcase.catalog.infrastructure.mapper;

import com.onatsubasi.finalcase.catalog.application.dto.response.BrandResponse;
import com.onatsubasi.finalcase.catalog.domain.entity.Brand;
import org.springframework.stereotype.Component;

@Component
public class BrandMapper {

    public BrandResponse toResponse(Brand brand) {
        return new BrandResponse(
                brand.getId(),
                brand.getName(),
                brand.getSlug(),
                brand.getDescription(),
                brand.getLogoUrl(),
                brand.getStatus(),
                brand.isActive(),
                brand.getCreatedAt(),
                brand.getUpdatedAt()
        );
    }
}