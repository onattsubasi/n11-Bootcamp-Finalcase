package com.onatsubasi.finalcase.auth;

import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.Base64;

@Testcontainers
public abstract class AbstractIntegrationTest {

    private static final String TEST_JWT_SECRET = Base64.getEncoder()
            .encodeToString("integration-test-secret-that-is-at-least-32-bytes".getBytes());

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine")
            .withDatabaseName("auth_db")
            .withUsername("test")
            .withPassword("test");

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);

        registry.add("jwt.secret", () -> TEST_JWT_SECRET);
        registry.add("jwt.access-token-expiration-ms", () -> "900000");
        registry.add("jwt.issuer", () -> "finalcase-auth-service-test");
        registry.add("auth.refresh-token.pepper", () -> "integration-test-refresh-token-pepper");
        registry.add("auth.refresh-token.expiration-seconds", () -> "604800");

        registry.add("eureka.client.enabled", () -> "false");
        registry.add("spring.cloud.discovery.enabled", () -> "false");
    }
}