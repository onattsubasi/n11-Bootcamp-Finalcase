package com.onatsubasi.finalcase.inventory.infrastructure.persistence;

import com.onatsubasi.finalcase.inventory.AbstractIntegrationTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class FlywayMigrationIntegrationTest extends AbstractIntegrationTest {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    @DisplayName("Flyway creates the inventory schema expected by JPA validate")
    void flywayCreatesExpectedTables() {
        List<String> tableNames = jdbcTemplate.queryForList(
                """
                select table_name
                  from information_schema.tables
                 where table_schema = 'public'
                   and table_name in (
                       'inventory_items',
                       'stock_reservations',
                       'stock_reservation_items',
                       'stock_movements',
                       'inventory_processed_events'
                   )
                """,
                String.class
        );

        assertThat(tableNames).containsExactlyInAnyOrder(
                "inventory_items",
                "stock_reservations",
                "stock_reservation_items",
                "stock_movements",
                "inventory_processed_events"
        );
    }
}
