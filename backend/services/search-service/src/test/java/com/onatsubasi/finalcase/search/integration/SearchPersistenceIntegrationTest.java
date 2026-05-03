package com.onatsubasi.finalcase.search.integration;

import com.onatsubasi.finalcase.search.domain.enums.ProductSearchStatus;
import com.onatsubasi.finalcase.search.domain.entity.ProductSearchDocument;
import com.onatsubasi.finalcase.search.infrastructure.persistence.SpringDataProductSearchDocumentJpaRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@Testcontainers
@SpringBootTest
@ActiveProfiles("test")
class SearchPersistenceIntegrationTest {

    @Container
    static final PostgreSQLContainer<?> POSTGRES = new PostgreSQLContainer<>("postgres:16-alpine")
            .withDatabaseName("search_test_db")
            .withUsername("search")
            .withPassword("search");

    @DynamicPropertySource
    static void registerProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", POSTGRES::getJdbcUrl);
        registry.add("spring.datasource.username", POSTGRES::getUsername);
        registry.add("spring.datasource.password", POSTGRES::getPassword);
    }

    @Autowired
    private SpringDataProductSearchDocumentJpaRepository repository;

    @Test
    void flywayCreatesSchemaAndJsonbDocumentPersists() {
        UUID productId = UUID.randomUUID();
        ProductSearchDocument document = ProductSearchDocument.createFromCatalogProjection(
                productId,
                "SKU-PG-1",
                "postgres-product",
                "Postgres Product",
                "Search integration test",
                UUID.randomUUID(),
                "Brand",
                UUID.randomUUID(),
                "Category",
                List.of("Root", "Category"),
                BigDecimal.valueOf(555),
                "TRY",
                null,
                Map.of("color", "Blue", "storage", "128GB"),
                List.of("postgres", "search"),
                ProductSearchStatus.ACTIVE,
                true,
                Instant.now()
        );

        repository.saveAndFlush(document);

        ProductSearchDocument saved = repository.findByProductId(productId).orElseThrow();
        assertThat(saved.getProductId()).isEqualTo(productId);
        assertThat(saved.getAttributes()).containsEntry("color", "Blue");
        assertThat(saved.getTags()).containsExactly("postgres", "search");
        assertThat(saved.isPubliclyVisible()).isTrue();
    }
}
