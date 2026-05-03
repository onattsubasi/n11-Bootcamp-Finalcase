package com.onatsubasi.finalcase.search.application.event;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record CatalogProductDeletedMessage(
        String productId
) {
}