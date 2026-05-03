package com.onatsubasi.finalcase.search.application.service;

import com.onatsubasi.finalcase.common.core.exception.BaseException;
import com.onatsubasi.finalcase.search.application.dto.query.ProductSearchCriteria;
import com.onatsubasi.finalcase.search.application.dto.query.SearchFacetCriteria;
import com.onatsubasi.finalcase.search.application.dto.response.AutocompleteSuggestionResponse;
import com.onatsubasi.finalcase.search.application.dto.response.ProductSearchResultResponse;
import com.onatsubasi.finalcase.search.application.dto.response.SearchFacetResponse;
import com.onatsubasi.finalcase.search.application.dto.response.SearchPageResponse;
import com.onatsubasi.finalcase.search.application.port.ProductSearchQueryPort;
import com.onatsubasi.finalcase.search.domain.exception.SearchErrorCode;
import com.onatsubasi.finalcase.search.domain.entity.ProductSearchDocument;
import com.onatsubasi.finalcase.search.infrastructure.mapper.SearchMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.Instant;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProductSearchQueryService {

    private final ProductSearchQueryPort queryPort;
    private final SearchMapper searchMapper;

    @Transactional(readOnly = true)
    public SearchPageResponse<ProductSearchResultResponse> search(ProductSearchCriteria criteria) {
        validateCriteria(criteria);

        Instant startedAt = Instant.now();

        try {
            MDC.put("eventName", "search.query.started");

            Page<ProductSearchDocument> page = queryPort.search(criteria);

            List<ProductSearchResultResponse> items = page.getContent()
                    .stream()
                    .map(searchMapper::toSearchResult)
                    .toList();

            long durationMs = Duration.between(startedAt, Instant.now()).toMillis();

            MDC.put("eventName", "search.query.executed");
            log.info(
                    "Search query executed, query={}, page={}, size={}, resultCount={}, durationMs={}",
                    safeQuery(criteria.query()),
                    criteria.page(),
                    criteria.size(),
                    page.getTotalElements(),
                    durationMs
            );

            return new SearchPageResponse<>(
                    items,
                    page.getNumber(),
                    page.getSize(),
                    page.getTotalElements(),
                    page.getTotalPages(),
                    page.isFirst(),
                    page.isLast()
            );
        } catch (BaseException ex) {
            logBusinessFailure("search.query.failed", ex);
            throw ex;
        } catch (Exception ex) {
            MDC.put("eventName", "search.query.failed");
            log.error("Search query failed, query={}", safeQuery(criteria.query()), ex);
            throw new BaseException(SearchErrorCode.SEARCH_QUERY_FAILED);
        } finally {
            clearMdc();
        }
    }

    @Transactional(readOnly = true)
    public List<AutocompleteSuggestionResponse> autocomplete(String query, int limit) {
        if (query == null || query.isBlank()) {
            return List.of();
        }

        int effectiveLimit = Math.min(Math.max(limit, 1), 20);

        return queryPort.autocomplete(query.trim(), effectiveLimit);
    }

    @Transactional(readOnly = true)
    public SearchFacetResponse facets(SearchFacetCriteria criteria) {
        return queryPort.facets(criteria);
    }

    private void validateCriteria(ProductSearchCriteria criteria) {
        if (criteria == null) {
            throw new BaseException(SearchErrorCode.INVALID_SEARCH_REQUEST);
        }

        BigDecimal minPrice = criteria.minPrice();
        BigDecimal maxPrice = criteria.maxPrice();

        if (minPrice != null && minPrice.compareTo(BigDecimal.ZERO) < 0) {
            throw new BaseException(SearchErrorCode.INVALID_PRICE_RANGE);
        }

        if (maxPrice != null && maxPrice.compareTo(BigDecimal.ZERO) < 0) {
            throw new BaseException(SearchErrorCode.INVALID_PRICE_RANGE);
        }

        if (minPrice != null && maxPrice != null && minPrice.compareTo(maxPrice) > 0) {
            throw new BaseException(SearchErrorCode.INVALID_PRICE_RANGE);
        }
    }

    private String safeQuery(String query) {
        if (query == null) {
            return null;
        }

        String normalized = query.trim();

        return normalized.length() > 80
                ? normalized.substring(0, 80)
                : normalized;
    }

    private void logBusinessFailure(String eventName, BaseException ex) {
        MDC.put("eventName", eventName);
        MDC.put("errorCode", ex.getErrorCode().code());
        log.warn("Search query business failure, errorCode={}", ex.getErrorCode().code());
    }

    private void clearMdc() {
        MDC.remove("eventName");
        MDC.remove("errorCode");
    }
}
