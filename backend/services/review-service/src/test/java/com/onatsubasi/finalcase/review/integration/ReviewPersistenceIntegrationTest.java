package com.onatsubasi.finalcase.review.integration;

import com.onatsubasi.finalcase.review.domain.entity.ProductRatingSummary;
import com.onatsubasi.finalcase.review.domain.entity.Review;
import com.onatsubasi.finalcase.review.infrastructure.persistence.SpringDataProductRatingSummaryJpaRepository;
import com.onatsubasi.finalcase.review.infrastructure.persistence.SpringDataReviewJpaRepository;
import com.onatsubasi.finalcase.review.testsupport.ReviewTestData;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.time.Instant;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@Testcontainers
@SpringBootTest
@ActiveProfiles("test")
class ReviewPersistenceIntegrationTest {

    @Container
    static final PostgreSQLContainer<?> POSTGRES = new PostgreSQLContainer<>("postgres:16-alpine")
            .withDatabaseName("review_test_db")
            .withUsername("review")
            .withPassword("review");

    @DynamicPropertySource
    static void postgresProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", POSTGRES::getJdbcUrl);
        registry.add("spring.datasource.username", POSTGRES::getUsername);
        registry.add("spring.datasource.password", POSTGRES::getPassword);
        registry.add("spring.datasource.driver-class-name", POSTGRES::getDriverClassName);
    }

    @Autowired
    SpringDataReviewJpaRepository reviewRepository;

    @Autowired
    SpringDataProductRatingSummaryJpaRepository summaryRepository;

    @BeforeEach
    void cleanDatabase() {
        summaryRepository.deleteAll();
        reviewRepository.deleteAll();
    }

    @Test
    void flywaySchemaSupportsReviewAndRatingSummaryPersistence() {
        Review review = Review.createVerifiedPurchaseReview(
                ReviewTestData.PRODUCT_ID,
                ReviewTestData.USER_ID,
                ReviewTestData.ORDER_ID,
                ReviewTestData.ORDER_ITEM_ID,
                "ORD-20260503-000001",
                Instant.parse("2026-05-03T10:00:00Z"),
                "C***",
                5,
                "Great",
                "Good product",
                List.of(),
                true
        );

        Review saved = reviewRepository.saveAndFlush(review);

        ProductRatingSummary summary = ProductRatingSummary.empty(ReviewTestData.PRODUCT_ID);
        summary.updateCounts(0, 0, 0, 0, 1);
        summaryRepository.saveAndFlush(summary);

        assertThat(saved.getId()).isNotNull();
        assertThat(reviewRepository.findActiveByUserIdAndProductId(
                ReviewTestData.USER_ID,
                ReviewTestData.PRODUCT_ID
        )).isPresent();
        assertThat(summaryRepository.findByProductId(ReviewTestData.PRODUCT_ID)).isPresent();
    }

    @Test
    void databaseRejectsTwoActiveReviewsForSameUserAndProduct() {
        Review first = Review.createVerifiedPurchaseReview(
                ReviewTestData.PRODUCT_ID,
                ReviewTestData.USER_ID,
                ReviewTestData.ORDER_ID,
                ReviewTestData.ORDER_ITEM_ID,
                "ORD-20260503-000001",
                Instant.parse("2026-05-03T10:00:00Z"),
                "C***",
                5,
                "Great",
                "Good product",
                List.of(),
                true
        );
        reviewRepository.saveAndFlush(first);

        Review second = Review.createVerifiedPurchaseReview(
                ReviewTestData.PRODUCT_ID,
                ReviewTestData.USER_ID,
                ReviewTestData.ORDER_ID,
                ReviewTestData.ORDER_ITEM_ID,
                "ORD-20260503-000002",
                Instant.parse("2026-05-03T11:00:00Z"),
                "C***",
                4,
                "Duplicate",
                "Should fail",
                List.of(),
                true
        );

        assertThatThrownBy(() -> reviewRepository.saveAndFlush(second))
                .isInstanceOf(DataIntegrityViolationException.class);
    }
}
