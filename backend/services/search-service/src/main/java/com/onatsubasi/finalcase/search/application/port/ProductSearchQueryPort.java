package com.onatsubasi.finalcase.search.application.port;

import com.onatsubasi.finalcase.search.application.dto.query.ProductSearchCriteria;
import com.onatsubasi.finalcase.search.application.dto.query.SearchFacetCriteria;
import com.onatsubasi.finalcase.search.application.dto.response.AutocompleteSuggestionResponse;
import com.onatsubasi.finalcase.search.application.dto.response.SearchFacetResponse;
import com.onatsubasi.finalcase.search.domain.entity.ProductSearchDocument;
import org.springframework.data.domain.Page;

import java.util.List;

public interface ProductSearchQueryPort {

    Page<ProductSearchDocument> search(ProductSearchCriteria criteria);

    List<AutocompleteSuggestionResponse> autocomplete(String query, int limit);

    SearchFacetResponse facets(SearchFacetCriteria criteria);
}
