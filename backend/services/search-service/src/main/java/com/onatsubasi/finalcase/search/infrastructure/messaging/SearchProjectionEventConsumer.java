package com.onatsubasi.finalcase.search.infrastructure.messaging;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.onatsubasi.finalcase.common.event.EventEnvelope;
import com.onatsubasi.finalcase.search.application.dto.event.*;
import com.onatsubasi.finalcase.search.application.service.SearchEventProcessingService;
import com.onatsubasi.finalcase.search.application.service.SearchProjectionService;
import com.onatsubasi.finalcase.search.infrastructure.config.RabbitMQConfig;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class SearchProjectionEventConsumer {

    private final ObjectMapper objectMapper;
    private final SearchProjectionService projectionService;
    private final SearchEventProcessingService eventProcessingService;

    @RabbitListener(queues = RabbitMQConfig.SEARCH_PROJECTION_QUEUE)
    public void consume(EventEnvelope<?> envelope) {
        try {
            MDC.put("eventName", "search.event.received");

            if (envelope == null) {
                log.warn("Null search projection event ignored");
                return;
            }

            log.info(
                    "Search projection event received, eventId={}, eventType={}, source={}",
                    envelope.eventId(),
                    envelope.eventType(),
                    envelope.source()
            );

            switch (envelope.eventType()) {
                case SearchConsumedEventTypes.CATALOG_PRODUCT_CREATED,
                     SearchConsumedEventTypes.CATALOG_PRODUCT_UPDATED ->
                        eventProcessingService.processOnce(
                                envelope,
                                () -> projectionService.upsertCatalogProduct(
                                        convertPayload(envelope, CatalogProductProjectionPayload.class)
                                )
                        );

                case SearchConsumedEventTypes.CATALOG_PRODUCT_DELETED ->
                        eventProcessingService.processOnce(
                                envelope,
                                () -> projectionService.markProductDeleted(
                                        convertPayload(envelope, CatalogProductDeletedPayload.class)
                                )
                        );

                case SearchConsumedEventTypes.CATALOG_PRODUCT_ACTIVATED,
                     SearchConsumedEventTypes.CATALOG_PRODUCT_DEACTIVATED,
                     SearchConsumedEventTypes.CATALOG_PRODUCT_STATUS_CHANGED ->
                        eventProcessingService.processOnce(
                                envelope,
                                () -> projectionService.updateProductStatus(
                                        convertPayload(envelope, CatalogProductStatusChangedPayload.class)
                                )
                        );

                case SearchConsumedEventTypes.CATALOG_CATEGORY_UPDATED ->
                        eventProcessingService.processOnce(
                                envelope,
                                () -> projectionService.updateCategoryProjection(
                                        convertPayload(envelope, CatalogCategoryUpdatedPayload.class)
                                )
                        );

                case SearchConsumedEventTypes.CATALOG_BRAND_UPDATED ->
                        eventProcessingService.processOnce(
                                envelope,
                                () -> projectionService.updateBrandProjection(
                                        convertPayload(envelope, CatalogBrandUpdatedPayload.class)
                                )
                        );

                case SearchConsumedEventTypes.INVENTORY_STOCK_UPDATED,
                     SearchConsumedEventTypes.INVENTORY_STOCK_LOW,
                     SearchConsumedEventTypes.INVENTORY_BACK_IN_STOCK,
                     SearchConsumedEventTypes.INVENTORY_OUT_OF_STOCK ->
                        eventProcessingService.processOnce(
                                envelope,
                                () -> projectionService.updateStockProjection(
                                        convertPayload(envelope, InventoryStockProjectionPayload.class)
                                )
                        );

                case SearchConsumedEventTypes.PROMOTION_PRODUCT_PROJECTION_UPDATED ->
                        eventProcessingService.processOnce(
                                envelope,
                                () -> projectionService.updatePromotionProjection(
                                        convertPayload(envelope, PromotionProjectionPayload.class)
                                )
                        );

                case SearchConsumedEventTypes.PROMOTION_PRODUCT_PROJECTION_CLEARED ->
                        eventProcessingService.processOnce(
                                envelope,
                                () -> projectionService.clearPromotionProjection(
                                        convertPayload(envelope, PromotionProjectionPayload.class)
                                )
                        );

                case SearchConsumedEventTypes.REVIEW_RATING_SUMMARY_UPDATED ->
                        eventProcessingService.processOnce(
                                envelope,
                                () -> projectionService.updateRatingProjection(
                                        convertPayload(envelope, RatingSummaryProjectionPayload.class)
                                )
                        );

                default -> log.debug(
                        "Search projection event ignored, eventId={}, eventType={}",
                        envelope.eventId(),
                        envelope.eventType()
                );
            }
        } finally {
            MDC.remove("eventName");
        }
    }

    private <T> T convertPayload(EventEnvelope<?> envelope, Class<T> targetType) {
        return objectMapper.convertValue(envelope.payload(), targetType);
    }
}
