package com.onatsubasi.finalcase.catalog.infrastructure.migration;

import com.onatsubasi.finalcase.catalog.support.AbstractCatalogIntegrationTest;
import org.flywaydb.core.Flyway;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;

import static org.assertj.core.api.Assertions.assertThat;

class FlywayMigrationIntegrationTest extends AbstractCatalogIntegrationTest {

    @Autowired
    private Flyway flyway;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    @DisplayName("Should apply catalog schema migration and create core tables")
    void shouldApplyCatalogSchemaMigration() {
        assertThat(flyway.info().current()).isNotNull();

        Integer tableCount = jdbcTemplate.queryForObject(
                """
                select count(*)
                  from information_schema.tables
                 where table_schema = 'public'
                   and table_name in ('brands', 'categories', 'products')
                """,
                Integer.class
        );

        assertThat(tableCount).isEqualTo(3);
    }
}
