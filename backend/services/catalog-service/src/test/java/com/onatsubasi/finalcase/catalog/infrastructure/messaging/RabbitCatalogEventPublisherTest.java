package com.onatsubasi.finalcase.catalog.infrastructure.messaging;

import com.onatsubasi.finalcase.catalog.domain.entity.Product;
import com.onatsubasi.finalcase.catalog.domain.valueobject.BrandSnapshot;
import com.onatsubasi.finalcase.catalog.domain.valueobject.CategorySnapshot;
import com.onatsubasi.finalcase.catalog.domain.valueobject.Money;
import com.onatsubasi.finalcase.catalog.domain.valueobject.ProductOwnership;
import com.onatsubasi.finalcase.common.event.EventBrokerConstants;
import com.onatsubasi.finalcase.common.event.EventEnvelope;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.amqp.core.MessagePostProcessor;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class RabbitCatalogEventPublisherTest {

    @Mock
    private RabbitTemplate rabbitTemplate;

    private RabbitCatalogEventPublisher publisher;

    @BeforeEach
    void setUp() {
        publisher = new RabbitCatalogEventPublisher(rabbitTemplate);
    }

    @Test
    @DisplayName("Should publish product event to marketplace exchange with routing key")
    void shouldPublishProductEvent() {
        Product product = samplePersistedProduct();

        publisher.publishProductCreated(product);

        @SuppressWarnings({"unchecked", "rawtypes"})
        ArgumentCaptor<EventEnvelope<?>> envelopeCaptor = ArgumentCaptor.forClass((Class) EventEnvelope.class);

        verify(rabbitTemplate).convertAndSend(
                eq(EventBrokerConstants.MAIN_EXCHANGE),
                eq(CatalogEventTypes.PRODUCT_CREATED),
                envelopeCaptor.capture(),
                org.mockito.ArgumentMatchers.any(MessagePostProcessor.class)
        );

        EventEnvelope<?> envelope = envelopeCaptor.getValue();

        assertThat(envelope.eventType()).isEqualTo(CatalogEventTypes.PRODUCT_CREATED);
        assertThat(envelope.source()).isEqualTo("catalog-service");
        assertThat(envelope.payload()).isNotNull();
    }

    private Product samplePersistedProduct() {
        Product product = Product.createDraft(
                "SKU-EVT-1",
                "Event Product",
                "event-product",
                "Event description",
                Money.of(BigDecimal.valueOf(100), "TRY"),
                new BrandSnapshot(UUID.randomUUID(), "Brand", "brand"),
                new CategorySnapshot(UUID.randomUUID(), "Category", "category", "category", List.of()),
                ProductOwnership.platform("platform-store", "Platform Store"),
                List.of(),
                Map.of()
        );
        ReflectionTestUtils.setField(product, "id", UUID.randomUUID());
        return product;
    }
}
