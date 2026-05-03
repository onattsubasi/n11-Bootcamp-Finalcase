package com.onatsubasi.finalcase.search.application.service;

import com.onatsubasi.finalcase.common.core.exception.BaseException;
import com.onatsubasi.finalcase.search.application.dto.response.ProductSearchDocumentResponse;
import com.onatsubasi.finalcase.search.domain.enums.ProductSearchStatus;
import com.onatsubasi.finalcase.search.domain.exception.SearchErrorCode;
import com.onatsubasi.finalcase.search.domain.entity.ProductSearchDocument;
import com.onatsubasi.finalcase.search.domain.repository.ProductSearchDocumentRepository;
import com.onatsubasi.finalcase.search.infrastructure.mapper.SearchMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class SearchDocumentAdminService {

    private final ProductSearchDocumentRepository documentRepository;
    private final SearchMapper searchMapper;

    @Transactional(readOnly = true)
    public ProductSearchDocumentResponse getByProductId(UUID productId) {
        ProductSearchDocument document = documentRepository.findByProductId(productId)
                .orElseThrow(() -> new BaseException(SearchErrorCode.PRODUCT_SEARCH_DOCUMENT_NOT_FOUND));

        return searchMapper.toDocumentResponse(document);
    }

    @Transactional(readOnly = true)
    public List<ProductSearchDocumentResponse> listByStatus(ProductSearchStatus status) {
        ProductSearchStatus effectiveStatus = status == null
                ? ProductSearchStatus.ACTIVE
                : status;

        return documentRepository.findByStatus(effectiveStatus)
                .stream()
                .map(searchMapper::toDocumentResponse)
                .toList();
    }
}
