package com.onatsubasi.finalcase.search.presentation.controller;

import com.onatsubasi.finalcase.common.core.response.ApiResponse;
import com.onatsubasi.finalcase.search.application.dto.query.ProductSearchCriteria;
import com.onatsubasi.finalcase.search.application.dto.query.SearchFacetCriteria;
import com.onatsubasi.finalcase.search.application.dto.response.AutocompleteSuggestionResponse;
import com.onatsubasi.finalcase.search.application.dto.response.ProductSearchResultResponse;
import com.onatsubasi.finalcase.search.application.dto.response.SearchFacetResponse;
import com.onatsubasi.finalcase.search.application.dto.response.SearchPageResponse;
import com.onatsubasi.finalcase.search.application.service.ProductSearchQueryService;
import com.onatsubasi.finalcase.search.domain.enums.SearchSort;
import com.onatsubasi.finalcase.search.domain.enums.StockStatus;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RequiredArgsConstructor
@RestController

@Tag(name = "Public Product Search", description = "Public product search, autocomplete, and facets")
public class PublicProductSearchController {

    private static final String ATTRIBUTE_PREFIX = "attr_";

    private final ProductSearchQueryService productSearchQueryService;

    @Operation(
            summary = "Search products",
            description = """
                    Searches active and visible product search documents.
                    Supports keyword, category, brand, price, stock, discount, and JSONB attribute filters.
                    Attribute filters can be sent as query params with attr_ prefix, for example:
                    attr_color=Black&attr_storage=256GB
                    """
    )
    @GetMapping({"/api/products/search", "/api/search", "/api/products"})
    public ResponseEntity<ApiResponse<SearchPageResponse<ProductSearchResultResponse>>> search(
            @RequestParam(required = false, name = "q") String query,
            @RequestParam(required = false) UUID categoryId,
            @RequestParam(required = false) UUID brandId,
            @RequestParam(required = false) BigDecimal minPrice,
            @RequestParam(required = false) BigDecimal maxPrice,
            @RequestParam(required = false) StockStatus stockStatus,
            @RequestParam(required = false) Boolean hasDiscount,
            @RequestParam(required = false) SearchSort sort,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam MultiValueMap<String, String> queryParams
    ) {
        ProductSearchCriteria criteria = new ProductSearchCriteria(
                query,
                categoryId,
                brandId,
                minPrice,
                maxPrice,
                stockStatus,
                hasDiscount,
                extractAttributeFilters(queryParams),
                sort,
                page,
                size
        );

        return ResponseEntity.ok(
                ApiResponse.success(productSearchQueryService.search(criteria))
        );
    }

    @Operation(
            summary = "Autocomplete products",
            description = "Returns product name suggestions for search input."
    )
    @GetMapping({"/api/products/autocomplete", "/api/search/autocomplete"})
    public ResponseEntity<ApiResponse<List<AutocompleteSuggestionResponse>>> autocomplete(
            @RequestParam(name = "q") String query,
            @RequestParam(defaultValue = "10") int limit
    ) {
        return ResponseEntity.ok(
                ApiResponse.success(productSearchQueryService.autocomplete(query, limit))
        );
    }

    @Operation(
            summary = "Get product search facets",
            description = """
                    Returns brand, category, stock, and price-range facets for current search filters.
                    Attribute filters can be sent with attr_ prefix.
                    """
    )
    @GetMapping({"/api/products/facets", "/api/search/facets"})
    public ResponseEntity<ApiResponse<SearchFacetResponse>> facets(
            @RequestParam(required = false, name = "q") String query,
            @RequestParam(required = false) UUID categoryId,
            @RequestParam(required = false) UUID brandId,
            @RequestParam(required = false) BigDecimal minPrice,
            @RequestParam(required = false) BigDecimal maxPrice,
            @RequestParam(required = false) StockStatus stockStatus,
            @RequestParam(required = false) Boolean hasDiscount,
            @RequestParam MultiValueMap<String, String> queryParams
    ) {
        SearchFacetCriteria criteria = new SearchFacetCriteria(
                query,
                categoryId,
                brandId,
                minPrice,
                maxPrice,
                stockStatus,
                hasDiscount,
                extractAttributeFilters(queryParams)
        );

        return ResponseEntity.ok(
                ApiResponse.success(productSearchQueryService.facets(criteria))
        );
    }

    private Map<String, List<String>> extractAttributeFilters(MultiValueMap<String, String> queryParams) {
        if (queryParams == null || queryParams.isEmpty()) {
            return Map.of();
        }

        Map<String, List<String>> attributes = new HashMap<>();

        queryParams.forEach((key, values) -> {
            if (key != null && key.startsWith(ATTRIBUTE_PREFIX)) {
                String attributeKey = key.substring(ATTRIBUTE_PREFIX.length());

                if (!attributeKey.isBlank() && values != null && !values.isEmpty()) {
                    attributes.put(attributeKey, values);
                }
            }
        });

        return attributes;
    }
}
