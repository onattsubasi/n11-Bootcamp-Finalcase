package com.onatsubasi.finalcase.search.infrastructure.query;

import com.onatsubasi.finalcase.search.application.dto.query.ProductSearchCriteria;
import com.onatsubasi.finalcase.search.application.dto.query.SearchFacetCriteria;
import com.onatsubasi.finalcase.search.application.dto.response.AutocompleteSuggestionResponse;
import com.onatsubasi.finalcase.search.application.dto.response.FacetBucketResponse;
import com.onatsubasi.finalcase.search.application.dto.response.SearchFacetResponse;
import com.onatsubasi.finalcase.search.application.port.ProductSearchQueryPort;
import com.onatsubasi.finalcase.search.domain.enums.SearchSort;
import com.onatsubasi.finalcase.search.domain.entity.ProductSearchDocument;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.Query;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.*;

@Slf4j
@Repository
public class PostgresProductSearchQueryAdapter implements ProductSearchQueryPort {

    @PersistenceContext
    private EntityManager entityManager;

    @Override
    public Page<ProductSearchDocument> search(ProductSearchCriteria criteria) {
        Map<String, Object> params = new LinkedHashMap<>();
        String where = buildWhereClause(criteria, params);
        String orderBy = buildOrderBy(criteria);
        int offset = criteria.page() * criteria.size();

        String selectSql = """
                select *
                  from product_search_documents
                """ + where + orderBy + " limit :limit offset :offset";

        String countSql = """
                select count(*)
                  from product_search_documents
                """ + where;

        Query selectQuery = entityManager.createNativeQuery(selectSql, ProductSearchDocument.class);
        Query countQuery = entityManager.createNativeQuery(countSql);

        bindParameters(selectQuery, params);
        bindParameters(countQuery, params);

        selectQuery.setParameter("limit", criteria.size());
        selectQuery.setParameter("offset", offset);

        @SuppressWarnings("unchecked")
        List<ProductSearchDocument> content = selectQuery.getResultList();

        Number total = (Number) countQuery.getSingleResult();

        return new PageImpl<>(
                content,
                PageRequest.of(criteria.page(), criteria.size()),
                total.longValue()
        );
    }

    @Override
    public List<AutocompleteSuggestionResponse> autocomplete(String query, int limit) {
        if (query == null || query.isBlank()) {
            return List.of();
        }

        Query nativeQuery = entityManager.createNativeQuery("""
                select name as text, 'PRODUCT' as type
                  from product_search_documents
                 where status = 'ACTIVE'
                   and visible = true
                   and (
                        lower(name) like lower(:prefix)
                        or similarity(unaccent(lower(name)), unaccent(lower(:rawQuery))) > 0.2
                   )
                 order by
                   case when lower(name) like lower(:prefix) then 0 else 1 end,
                   similarity(unaccent(lower(name)), unaccent(lower(:rawQuery))) desc,
                   name asc
                 limit :limit
                """);

        nativeQuery.setParameter("rawQuery", query.trim());
        nativeQuery.setParameter("prefix", query.trim() + "%");
        nativeQuery.setParameter("limit", limit);

        @SuppressWarnings("unchecked")
        List<Object[]> rows = nativeQuery.getResultList();

        return rows.stream()
                .map(row -> new AutocompleteSuggestionResponse(
                        row[0] == null ? null : row[0].toString(),
                        row[1] == null ? "PRODUCT" : row[1].toString()
                ))
                .toList();
    }

    @Override
    public SearchFacetResponse facets(SearchFacetCriteria criteria) {
        Map<String, Object> params = new LinkedHashMap<>();
        String where = buildFacetWhereClause(criteria, params);

        List<FacetBucketResponse> brands = executeFacetQuery(
                """
                select brand_id::text as value,
                       coalesce(brand_name, 'Unknown') as label,
                       count(*) as count
                  from product_search_documents
                """ + where + """
                 and brand_id is not null
                 group by brand_id, brand_name
                 order by count desc, label asc
                 limit 30
                """,
                params
        );

        List<FacetBucketResponse> categories = executeFacetQuery(
                """
                select category_id::text as value,
                       coalesce(category_name, 'Unknown') as label,
                       count(*) as count
                  from product_search_documents
                """ + where + """
                 and category_id is not null
                 group by category_id, category_name
                 order by count desc, label asc
                 limit 30
                """,
                params
        );

        List<FacetBucketResponse> stockStatuses = executeFacetQuery(
                """
                select stock_status as value,
                       stock_status as label,
                       count(*) as count
                  from product_search_documents
                """ + where + """
                 group by stock_status
                 order by count desc, label asc
                """,
                params
        );

        List<FacetBucketResponse> priceRanges = priceRangeFacets(where, params);

        return new SearchFacetResponse(
                brands,
                categories,
                stockStatuses,
                priceRanges
        );
    }

    private String buildWhereClause(
            ProductSearchCriteria criteria,
            Map<String, Object> params
    ) {
        StringBuilder where = new StringBuilder("""
                 where status = 'ACTIVE'
                   and visible = true
                """);

        addKeywordFilter(where, params, criteria.query());
        addCommonFilters(
                where,
                params,
                criteria.categoryId(),
                criteria.brandId(),
                criteria.minPrice(),
                criteria.maxPrice(),
                criteria.stockStatus() == null ? null : criteria.stockStatus().name(),
                criteria.hasDiscount(),
                criteria.attributes()
        );

        return where.toString();
    }

    private String buildFacetWhereClause(
            SearchFacetCriteria criteria,
            Map<String, Object> params
    ) {
        StringBuilder where = new StringBuilder("""
                 where status = 'ACTIVE'
                   and visible = true
                """);

        addKeywordFilter(where, params, criteria.query());
        addCommonFilters(
                where,
                params,
                criteria.categoryId(),
                criteria.brandId(),
                criteria.minPrice(),
                criteria.maxPrice(),
                criteria.stockStatus() == null ? null : criteria.stockStatus().name(),
                criteria.hasDiscount(),
                criteria.attributes()
        );

        return where.toString();
    }

    private void addKeywordFilter(
            StringBuilder where,
            Map<String, Object> params,
            String query
    ) {
        if (query == null || query.isBlank()) {
            return;
        }

        where.append("""
                and (
                    search_vector @@ websearch_to_tsquery('simple', unaccent(:query))
                    or unaccent(lower(name)) like unaccent(lower(:likeQuery))
                    or unaccent(lower(coalesce(brand_name, ''))) like unaccent(lower(:likeQuery))
                    or unaccent(lower(coalesce(category_name, ''))) like unaccent(lower(:likeQuery))
                    or similarity(unaccent(lower(name)), unaccent(lower(:query))) > 0.15
                )
                """);

        params.put("query", query.trim());
        params.put("likeQuery", "%" + query.trim() + "%");
    }

    private void addCommonFilters(
            StringBuilder where,
            Map<String, Object> params,
            UUID categoryId,
            UUID brandId,
            BigDecimal minPrice,
            BigDecimal maxPrice,
            String stockStatus,
            Boolean hasDiscount,
            Map<String, List<String>> attributes
    ) {
        if (categoryId != null) {
            where.append(" and category_id = :categoryId ");
            params.put("categoryId", categoryId);
        }

        if (brandId != null) {
            where.append(" and brand_id = :brandId ");
            params.put("brandId", brandId);
        }

        if (minPrice != null) {
            where.append(" and coalesce(discounted_price, base_price) >= :minPrice ");
            params.put("minPrice", minPrice);
        }

        if (maxPrice != null) {
            where.append(" and coalesce(discounted_price, base_price) <= :maxPrice ");
            params.put("maxPrice", maxPrice);
        }

        if (stockStatus != null && !stockStatus.isBlank()) {
            where.append(" and stock_status = :stockStatus ");
            params.put("stockStatus", stockStatus);
        }

        if (hasDiscount != null) {
            where.append(" and has_discount = :hasDiscount ");
            params.put("hasDiscount", hasDiscount);
        }

        addAttributeFilters(where, params, attributes);
    }

    private void addAttributeFilters(
            StringBuilder where,
            Map<String, Object> params,
            Map<String, List<String>> attributes
    ) {
        if (attributes == null || attributes.isEmpty()) {
            return;
        }

        int index = 0;

        for (Map.Entry<String, List<String>> entry : attributes.entrySet()) {
            String key = entry.getKey();
            List<String> values = entry.getValue();

            if (key == null || key.isBlank() || values == null || values.isEmpty()) {
                continue;
            }

            List<String> normalizedValues = values.stream()
                    .filter(Objects::nonNull)
                    .map(String::trim)
                    .filter(value -> !value.isBlank())
                    .toList();

            if (normalizedValues.isEmpty()) {
                continue;
            }

            String keyParam = "attrKey" + index;
            String valueParam = "attrValues" + index;

            where.append(" and attributes ->> :")
                    .append(keyParam)
                    .append(" in (:")
                    .append(valueParam)
                    .append(") ");

            params.put(keyParam, key.trim());
            params.put(valueParam, normalizedValues);
            index++;
        }
    }

    private String buildOrderBy(ProductSearchCriteria criteria) {
        SearchSort sort = criteria.sort() == null ? SearchSort.RELEVANCE : criteria.sort();

        if (sort == SearchSort.RELEVANCE && criteria.query() != null && !criteria.query().isBlank()) {
            return """
                    order by
                      ts_rank_cd(search_vector, websearch_to_tsquery('simple', unaccent(:query))) desc,
                      average_rating desc,
                      review_count desc,
                      indexed_at desc
                    """;
        }

        return switch (sort) {
            case PRICE_ASC -> " order by coalesce(discounted_price, base_price) asc, product_id asc ";
            case PRICE_DESC -> " order by coalesce(discounted_price, base_price) desc, product_id asc ";
            case NEWEST -> " order by source_updated_at desc nulls last, indexed_at desc, product_id asc ";
            case RATING_DESC -> " order by average_rating desc, review_count desc, product_id asc ";
            case POPULARITY_DESC -> " order by review_count desc, average_rating desc, product_id asc ";
            case RELEVANCE -> " order by average_rating desc, review_count desc, indexed_at desc, product_id asc ";
        };
    }

    private List<FacetBucketResponse> executeFacetQuery(
            String sql,
            Map<String, Object> params
    ) {
        Query query = entityManager.createNativeQuery(sql);
        bindParameters(query, params);

        @SuppressWarnings("unchecked")
        List<Object[]> rows = query.getResultList();

        return rows.stream()
                .map(row -> new FacetBucketResponse(
                        row[0] == null ? null : row[0].toString(),
                        row[1] == null ? null : row[1].toString(),
                        ((Number) row[2]).longValue()
                ))
                .toList();
    }

    private List<FacetBucketResponse> priceRangeFacets(
            String where,
            Map<String, Object> params
    ) {
        Query query = entityManager.createNativeQuery("""
                select bucket.value, bucket.label, count(*) as count
                  from product_search_documents d
                  join lateral (
                       select case
                           when coalesce(d.discounted_price, d.base_price) < 500 then '0-500'
                           when coalesce(d.discounted_price, d.base_price) < 1000 then '500-1000'
                           when coalesce(d.discounted_price, d.base_price) < 5000 then '1000-5000'
                           when coalesce(d.discounted_price, d.base_price) < 10000 then '5000-10000'
                           else '10000+'
                       end as value,
                       case
                           when coalesce(d.discounted_price, d.base_price) < 500 then '0 - 500'
                           when coalesce(d.discounted_price, d.base_price) < 1000 then '500 - 1.000'
                           when coalesce(d.discounted_price, d.base_price) < 5000 then '1.000 - 5.000'
                           when coalesce(d.discounted_price, d.base_price) < 10000 then '5.000 - 10.000'
                           else '10.000+'
                       end as label
                  ) bucket on true
                """ + where.replace("from product_search_documents", "from product_search_documents d") + """
                 group by bucket.value, bucket.label
                 order by
                   case bucket.value
                     when '0-500' then 1
                     when '500-1000' then 2
                     when '1000-5000' then 3
                     when '5000-10000' then 4
                     else 5
                   end
                """);

        bindParameters(query, params);

        @SuppressWarnings("unchecked")
        List<Object[]> rows = query.getResultList();

        return rows.stream()
                .map(row -> new FacetBucketResponse(
                        row[0].toString(),
                        row[1].toString(),
                        ((Number) row[2]).longValue()
                ))
                .toList();
    }

    private void bindParameters(Query query, Map<String, Object> params) {
        params.forEach(query::setParameter);
    }
}
