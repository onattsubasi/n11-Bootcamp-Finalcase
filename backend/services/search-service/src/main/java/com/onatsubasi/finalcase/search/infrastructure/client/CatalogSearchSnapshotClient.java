package com.onatsubasi.finalcase.search.infrastructure.client;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.onatsubasi.finalcase.common.core.exception.BaseException;
import com.onatsubasi.finalcase.search.application.dto.event.CatalogProductProjectionPayload;
import com.onatsubasi.finalcase.search.application.dto.internal.CatalogSearchSnapshotResponse;
import com.onatsubasi.finalcase.search.application.port.CatalogSearchSnapshotGateway;
import com.onatsubasi.finalcase.search.domain.exception.SearchErrorCode;
import com.onatsubasi.finalcase.search.infrastructure.config.SearchCatalogClientProperties;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class CatalogSearchSnapshotClient implements CatalogSearchSnapshotGateway {

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;
    private final SearchCatalogClientProperties properties;

    @Override
    @CircuitBreaker(name = "catalog-service", fallbackMethod = "fetchSearchSnapshotsFallback")
    public CatalogSearchSnapshotResponse fetchSearchSnapshots(int page, int size) {
        try {
            String url = UriComponentsBuilder
                    .fromHttpUrl(properties.getBaseUrl())
                    .path("/internal/catalog/search-snapshots")
                    .queryParam("page", page)
                    .queryParam("size", size)
                    .toUriString();

            String responseBody = restTemplate.getForObject(url, String.class);

            if (responseBody == null || responseBody.isBlank()) {
                throw new BaseException(SearchErrorCode.INDEX_REBUILD_FAILED);
            }

            JsonNode root = objectMapper.readTree(responseBody);
            JsonNode data = root.path("data");

            List<CatalogProductProjectionPayload> items = new ArrayList<>();

            JsonNode itemNodes = data.path("items");
            if (itemNodes.isArray()) {
                for (JsonNode itemNode : itemNodes) {
                    items.add(objectMapper.convertValue(
                            itemNode,
                            CatalogProductProjectionPayload.class
                    ));
                }
            }

            return new CatalogSearchSnapshotResponse(
                    items,
                    data.path("page").asInt(page),
                    data.path("size").asInt(size),
                    data.path("totalElements").asLong(items.size()),
                    data.path("totalPages").asInt(1),
                    data.path("last").asBoolean(true)
            );
        } catch (BaseException ex) {
            throw ex;
        } catch (RestClientException ex) {
            throw new BaseException(SearchErrorCode.CATALOG_REBUILD_SOURCE_UNAVAILABLE);
        } catch (Exception ex) {
            throw new BaseException(SearchErrorCode.INDEX_REBUILD_FAILED);
        }
    }

    public CatalogSearchSnapshotResponse fetchSearchSnapshotsFallback(
            int page,
            int size,
            Throwable throwable
    ) {
        log.warn(
                "Catalog search snapshot fallback triggered, page={}, size={}, reason={}",
                page,
                size,
                throwable == null ? null : throwable.getClass().getSimpleName()
        );

        throw new BaseException(SearchErrorCode.CATALOG_REBUILD_SOURCE_UNAVAILABLE);
    }
}
