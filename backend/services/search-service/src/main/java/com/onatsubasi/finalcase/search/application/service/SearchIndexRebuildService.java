package com.onatsubasi.finalcase.search.application.service;

import com.onatsubasi.finalcase.common.core.exception.BaseException;
import com.onatsubasi.finalcase.search.application.dto.internal.CatalogSearchSnapshotResponse;
import com.onatsubasi.finalcase.search.application.dto.response.SearchRebuildResponse;
import com.onatsubasi.finalcase.search.application.port.CatalogSearchSnapshotGateway;
import com.onatsubasi.finalcase.search.domain.exception.SearchErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.stereotype.Service;

import java.time.Instant;

@Slf4j
@Service
@RequiredArgsConstructor
public class SearchIndexRebuildService {

    private static final int DEFAULT_BATCH_SIZE = 100;

    private final CatalogSearchSnapshotGateway catalogGateway;
    private final SearchProjectionService projectionService;

    public SearchRebuildResponse rebuildAll() {
        Instant startedAt = Instant.now();

        int page = 0;
        int processedCount = 0;
        int upsertedCount = 0;
        int skippedCount = 0;

        try {
            MDC.put("eventName", "search.index.rebuild_started");

            log.info("Search index rebuild started");

            boolean last;

            do {
                CatalogSearchSnapshotResponse snapshot = catalogGateway.fetchSearchSnapshots(
                        page,
                        DEFAULT_BATCH_SIZE
                );

                if (snapshot == null || snapshot.items() == null) {
                    throw new BaseException(SearchErrorCode.INDEX_REBUILD_FAILED);
                }

                for (var item : snapshot.items()) {
                    processedCount++;

                    boolean changed = projectionService.upsertCatalogProduct(item);

                    if (changed) {
                        upsertedCount++;
                    } else {
                        skippedCount++;
                    }
                }

                last = snapshot.last();
                page++;
            } while (!last);

            Instant finishedAt = Instant.now();

            MDC.put("eventName", "search.index.rebuild_completed");
            log.info(
                    "Search index rebuild completed, processedCount={}, upsertedCount={}, skippedCount={}",
                    processedCount,
                    upsertedCount,
                    skippedCount
            );

            return new SearchRebuildResponse(
                    processedCount,
                    upsertedCount,
                    skippedCount,
                    startedAt,
                    finishedAt
            );
        } catch (BaseException ex) {
            MDC.put("eventName", "search.index.rebuild_failed");
            MDC.put("errorCode", ex.getErrorCode().code());
            log.warn("Search index rebuild failed, errorCode={}", ex.getErrorCode().code());
            throw ex;
        } catch (Exception ex) {
            MDC.put("eventName", "search.index.rebuild_failed");
            log.error("Search index rebuild failed unexpectedly", ex);
            throw new BaseException(SearchErrorCode.INDEX_REBUILD_FAILED);
        } finally {
            MDC.remove("eventName");
            MDC.remove("errorCode");
        }
    }
}
