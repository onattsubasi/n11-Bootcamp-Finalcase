package com.onatsubasi.finalcase.search.application.dto.response;

import java.util.List;

public record ProductSearchResponse(
        List<ProductSearchDocumentResponse> items,
        int page,
        int size,
        long totalElements,
        int totalPages
) {
}