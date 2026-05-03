package com.onatsubasi.finalcase.search.application.port;

import com.onatsubasi.finalcase.search.application.dto.internal.CatalogSearchSnapshotResponse;

public interface CatalogSearchSnapshotGateway {

    CatalogSearchSnapshotResponse fetchSearchSnapshots(int page, int size);
}
