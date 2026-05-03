package com.onatsubasi.finalcase.catalog.application.support;

import com.onatsubasi.finalcase.catalog.domain.repository.CatalogPage;
import com.onatsubasi.finalcase.common.core.response.ApiPageResponse;
import org.springframework.stereotype.Component;

@Component
public class CatalogPageResponseMapper {

    public <T> ApiPageResponse<T> toApiPageResponse(CatalogPage<T> page) {
        return ApiPageResponse.of(
                page.content(),
                page.page(),
                page.size(),
                page.totalElements(),
                page.totalPages(),
                page.first(),
                page.last()
        );
    }
}