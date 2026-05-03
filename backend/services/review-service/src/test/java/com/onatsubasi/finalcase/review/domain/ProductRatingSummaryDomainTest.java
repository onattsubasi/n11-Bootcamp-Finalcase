package com.onatsubasi.finalcase.review.domain;

import com.onatsubasi.finalcase.review.domain.entity.ProductRatingSummary;
import com.onatsubasi.finalcase.review.testsupport.ReviewTestData;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

class ProductRatingSummaryDomainTest {

    @Test
    void calculatesWeightedAverageAndTotalReviewCount() {
        ProductRatingSummary summary = ProductRatingSummary.empty(ReviewTestData.PRODUCT_ID);

        summary.updateCounts(1, 1, 0, 0, 2);

        assertThat(summary.getReviewCount()).isEqualTo(4);
        assertThat(summary.getAverageRating()).isEqualByComparingTo(new BigDecimal("3.25"));
        assertThat(summary.getRating1Count()).isEqualTo(1);
        assertThat(summary.getRating5Count()).isEqualTo(2);
    }

    @Test
    void neverStoresNegativeCounts() {
        ProductRatingSummary summary = ProductRatingSummary.empty(ReviewTestData.PRODUCT_ID);

        summary.updateCounts(-1, -2, -3, -4, -5);

        assertThat(summary.getReviewCount()).isZero();
        assertThat(summary.getAverageRating()).isEqualByComparingTo(new BigDecimal("0.00"));
    }
}
