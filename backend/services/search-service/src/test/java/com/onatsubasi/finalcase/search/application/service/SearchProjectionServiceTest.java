package com.onatsubasi.finalcase.search.application.service;

import com.onatsubasi.finalcase.search.application.dto.event.CatalogProductProjectionPayload;
import com.onatsubasi.finalcase.search.application.dto.event.PromotionProjectionPayload;
import com.onatsubasi.finalcase.search.domain.enums.ProductSearchStatus;
import com.onatsubasi.finalcase.search.domain.entity.ProductSearchDocument;
import com.onatsubasi.finalcase.search.domain.repository.ProductSearchDocumentRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SearchProjectionServiceTest {

    @Mock
    private ProductSearchDocumentRepository documentRepository;

    @InjectMocks
    private SearchProjectionService projectionService;

    @Test
    void upsertCatalogProductCreatesDocumentWhenMissing() {
        CatalogProductProjectionPayload payload = productPayload(UUID.randomUUID(), Instant.now());
        when(documentRepository.findByProductIdForUpdate(payload.productId())).thenReturn(Optional.empty());
        when(documentRepository.save(any(ProductSearchDocument.class))).thenAnswer(invocation -> invocation.getArgument(0));

        boolean changed = projectionService.upsertCatalogProduct(payload);

        ArgumentCaptor<ProductSearchDocument> captor = ArgumentCaptor.forClass(ProductSearchDocument.class);
        verify(documentRepository).save(captor.capture());
        assertThat(changed).isTrue();
        assertThat(captor.getValue().getProductId()).isEqualTo(payload.productId());
        assertThat(captor.getValue().isVisible()).isTrue();
    }

    @Test
    void olderCatalogProductEventIsIgnoredAndNotSaved() {
        UUID productId = UUID.randomUUID();
        ProductSearchDocument document = ProductSearchDocument.createFromCatalogProjection(
                productId,
                "SKU-NEW",
                "new-slug",
                "New Name",
                null,
                null,
                null,
                null,
                null,
                List.of(),
                BigDecimal.valueOf(100),
                "TRY",
                null,
                Map.of(),
                List.of(),
                ProductSearchStatus.ACTIVE,
                true,
                Instant.parse("2026-05-01T12:00:00Z")
        );
        when(documentRepository.findByProductIdForUpdate(productId)).thenReturn(Optional.of(document));

        boolean changed = projectionService.upsertCatalogProduct(
                productPayload(productId, Instant.parse("2026-05-01T11:00:00Z"))
        );

        assertThat(changed).isFalse();
        verify(documentRepository, never()).save(any());
    }

    @Test
    void clearPromotionProjectionRemovesDiscountFields() {
        UUID productId = UUID.randomUUID();
        ProductSearchDocument document = ProductSearchDocument.createFromCatalogProjection(
                productId,
                "SKU-1",
                "demo",
                "Demo",
                null,
                null,
                null,
                null,
                null,
                List.of(),
                BigDecimal.valueOf(100),
                "TRY",
                null,
                Map.of(),
                List.of(),
                ProductSearchStatus.ACTIVE,
                true,
                Instant.now()
        );
        document.updatePromotionProjection(true, true, BigDecimal.valueOf(80), "Deal", Instant.parse("2026-05-01T12:00:00Z"));
        when(documentRepository.findByProductIdForUpdate(productId)).thenReturn(Optional.of(document));
        when(documentRepository.save(document)).thenReturn(document);

        boolean changed = projectionService.clearPromotionProjection(
                new PromotionProjectionPayload(productId, false, false, null, null, Instant.parse("2026-05-01T13:00:00Z"))
        );

        assertThat(changed).isTrue();
        assertThat(document.isHasDiscount()).isFalse();
        assertThat(document.isHasActivePromotion()).isFalse();
        assertThat(document.getDiscountedPrice()).isNull();
        verify(documentRepository).save(document);
    }

    private CatalogProductProjectionPayload productPayload(UUID productId, Instant sourceUpdatedAt) {
        return new CatalogProductProjectionPayload(
                productId,
                "SKU-1",
                "demo-product",
                "Demo Product",
                "Description",
                UUID.randomUUID(),
                "Brand",
                UUID.randomUUID(),
                "Category",
                List.of("Root", "Category"),
                BigDecimal.valueOf(250),
                "TRY",
                null,
                Map.of("color", "Black"),
                List.of("demo"),
                ProductSearchStatus.ACTIVE,
                true,
                sourceUpdatedAt
        );
    }
}
