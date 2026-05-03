package com.onatsubasi.finalcase.search.domain.entity;

import com.onatsubasi.finalcase.common.core.exception.BaseException;
import com.onatsubasi.finalcase.search.domain.enums.ProductSearchStatus;
import com.onatsubasi.finalcase.search.domain.enums.StockStatus;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ProductSearchDocumentTest {

    @Test
    void createFromCatalogProjectionCreatesVisibleActiveDocument() {
        UUID productId = UUID.randomUUID();

        ProductSearchDocument document = activeDocument(productId, Instant.parse("2026-05-01T10:00:00Z"));

        assertThat(document.getProductId()).isEqualTo(productId);
        assertThat(document.getStatus()).isEqualTo(ProductSearchStatus.ACTIVE);
        assertThat(document.isVisible()).isTrue();
        assertThat(document.getBasePrice()).isEqualByComparingTo("1200.00");
        assertThat(document.effectivePrice()).isEqualByComparingTo("1200.00");
        assertThat(document.getAttributes()).containsEntry("color", "Black");
        assertThat(document.getTags()).containsExactly("phone", "android");
    }

    @Test
    void updateStockProjectionRejectsNegativeAvailableQuantity() {
        ProductSearchDocument document = activeDocument(UUID.randomUUID(), Instant.now());

        assertThatThrownBy(() -> document.updateStockProjection(-1, StockStatus.IN_STOCK, Instant.now()))
                .isInstanceOf(BaseException.class);
    }

    @Test
    void olderCatalogProjectionIsIgnored() {
        Instant newerSourceTime = Instant.parse("2026-05-01T12:00:00Z");
        Instant olderSourceTime = Instant.parse("2026-05-01T11:00:00Z");
        ProductSearchDocument document = activeDocument(UUID.randomUUID(), newerSourceTime);

        boolean changed = document.updateCatalogProjection(
                "SKU-OLD",
                "old-slug",
                "Old Name",
                "Old Description",
                UUID.randomUUID(),
                "Old Brand",
                UUID.randomUUID(),
                "Old Category",
                List.of("Old"),
                BigDecimal.valueOf(10),
                "TRY",
                null,
                Map.of(),
                List.of(),
                ProductSearchStatus.ACTIVE,
                true,
                olderSourceTime
        );

        assertThat(changed).isFalse();
        assertThat(document.getSku()).isEqualTo("SKU-1");
        assertThat(document.getName()).isEqualTo("Demo Phone");
    }

    @Test
    void promotionAndRatingProjectionUpdateEffectiveListingFields() {
        ProductSearchDocument document = activeDocument(UUID.randomUUID(), Instant.now());

        boolean promotionChanged = document.updatePromotionProjection(
                true,
                true,
                BigDecimal.valueOf(999),
                "Spring deal",
                Instant.now()
        );
        boolean ratingChanged = document.updateRatingProjection(
                BigDecimal.valueOf(4.567),
                42,
                Instant.now()
        );

        assertThat(promotionChanged).isTrue();
        assertThat(ratingChanged).isTrue();
        assertThat(document.effectivePrice()).isEqualByComparingTo("999.00");
        assertThat(document.getPromotionBadge()).isEqualTo("Spring deal");
        assertThat(document.getAverageRating()).isEqualByComparingTo("4.57");
        assertThat(document.getReviewCount()).isEqualTo(42);
    }

    private ProductSearchDocument activeDocument(UUID productId, Instant sourceUpdatedAt) {
        return ProductSearchDocument.createFromCatalogProjection(
                productId,
                "SKU-1",
                "demo-phone",
                "Demo Phone",
                "A phone for testing",
                UUID.randomUUID(),
                "Demo Brand",
                UUID.randomUUID(),
                "Phones",
                List.of("Electronics", "Phones"),
                BigDecimal.valueOf(1200),
                "TRY",
                "https://cdn.example.com/demo.jpg",
                Map.of("color", "Black"),
                List.of("phone", "android"),
                ProductSearchStatus.ACTIVE,
                true,
                sourceUpdatedAt
        );
    }
}
