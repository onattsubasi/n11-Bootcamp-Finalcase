package com.onatsubasi.finalcase.catalog.infrastructure.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "catalog")
public record CatalogProperties(
        PlatformStore platformStore
) {

    public CatalogProperties {
        if (platformStore == null) {
            platformStore = new PlatformStore("platform-store", "Platform Store");
        }
    }

    public record PlatformStore(
            String id,
            String name
    ) {

        public PlatformStore {
            if (id == null || id.isBlank()) {
                id = "platform-store";
            }

            if (name == null || name.isBlank()) {
                name = "Platform Store";
            }
        }
    }
}