package com.onatsubasi.finalcase.common.core.response;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record FieldError(
        String field,
        String message
) {
}