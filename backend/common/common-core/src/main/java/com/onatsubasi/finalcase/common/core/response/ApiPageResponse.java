package com.onatsubasi.finalcase.common.core.response;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.time.Instant;
import java.util.Collections;
import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record ApiPageResponse<T>(
        boolean success,
        List<T> data,
        PageMetadata meta,
        Instant timestamp
) {

    public ApiPageResponse {
        data = data == null ? Collections.emptyList() : List.copyOf(data);
        timestamp = timestamp == null ? Instant.now() : timestamp;
    }

    public static <T> ApiPageResponse<T> of(
            List<T> data,
            int page,
            int size,
            long totalElements,
            int totalPages,
            boolean first,
            boolean last
    ) {
        return new ApiPageResponse<>(
                true,
                data,
                new PageMetadata(
                        page,
                        size,
                        totalElements,
                        totalPages,
                        first,
                        last,
                        data == null || data.isEmpty()
                ),
                Instant.now()
        );
    }

    public record PageMetadata(
            int page,
            int size,
            long totalElements,
            int totalPages,
            boolean first,
            boolean last,
            boolean empty
    ) {
    }
}