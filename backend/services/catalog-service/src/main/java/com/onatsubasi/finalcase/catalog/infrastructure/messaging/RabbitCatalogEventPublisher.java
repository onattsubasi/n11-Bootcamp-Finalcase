package com.onatsubasi.finalcase.catalog.infrastructure.messaging;

import com.onatsubasi.finalcase.catalog.application.port.CatalogEventPublisher;
import com.onatsubasi.finalcase.catalog.domain.entity.Brand;
import com.onatsubasi.finalcase.catalog.domain.entity.Category;
import com.onatsubasi.finalcase.catalog.domain.entity.Product;
import com.onatsubasi.finalcase.catalog.infrastructure.messaging.payload.BrandCatalogPayload;
import com.onatsubasi.finalcase.catalog.infrastructure.messaging.payload.CategoryCatalogPayload;
import com.onatsubasi.finalcase.catalog.infrastructure.messaging.payload.ProductCatalogPayload;
import com.onatsubasi.finalcase.common.core.http.PlatformHeaders;
import com.onatsubasi.finalcase.common.event.EventBrokerConstants;
import com.onatsubasi.finalcase.common.event.EventEnvelope;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

@Component
@RequiredArgsConstructor
@Slf4j
public class RabbitCatalogEventPublisher implements CatalogEventPublisher {

    private static final String SOURCE = "catalog-service";
    private static final String SOURCE_SERVICE_HEADER = "X-Source-Service";

    private final RabbitTemplate rabbitTemplate;

    @Override
    public void publishProductCreated(Product product) {
        publishAfterCommit(
                CatalogEventTypes.PRODUCT_CREATED,
                ProductCatalogPayload.from(product),
                product.getId().toString()
        );
    }

    @Override
    public void publishProductUpdated(Product product) {
        publishAfterCommit(
                CatalogEventTypes.PRODUCT_UPDATED,
                ProductCatalogPayload.from(product),
                product.getId().toString()
        );
    }

    @Override
    public void publishProductPriceChanged(Product product) {
        publishAfterCommit(
                CatalogEventTypes.PRODUCT_PRICE_CHANGED,
                ProductCatalogPayload.from(product),
                product.getId().toString()
        );
    }

    @Override
    public void publishProductStatusChanged(Product product) {
        publishAfterCommit(
                CatalogEventTypes.PRODUCT_STATUS_CHANGED,
                ProductCatalogPayload.from(product),
                product.getId().toString()
        );
    }

    @Override
    public void publishProductDeleted(Product product) {
        publishAfterCommit(
                CatalogEventTypes.PRODUCT_DELETED,
                ProductCatalogPayload.from(product),
                product.getId().toString()
        );
    }

    @Override
    public void publishCategoryCreated(Category category) {
        publishAfterCommit(
                CatalogEventTypes.CATEGORY_CREATED,
                CategoryCatalogPayload.from(category),
                category.getId().toString()
        );
    }

    @Override
    public void publishCategoryUpdated(Category category) {
        publishAfterCommit(
                CatalogEventTypes.CATEGORY_UPDATED,
                CategoryCatalogPayload.from(category),
                category.getId().toString()
        );
    }

    @Override
    public void publishCategoryStatusChanged(Category category) {
        publishAfterCommit(
                CatalogEventTypes.CATEGORY_STATUS_CHANGED,
                CategoryCatalogPayload.from(category),
                category.getId().toString()
        );
    }

    @Override
    public void publishBrandCreated(Brand brand) {
        publishAfterCommit(
                CatalogEventTypes.BRAND_CREATED,
                BrandCatalogPayload.from(brand),
                brand.getId().toString()
        );
    }

    @Override
    public void publishBrandUpdated(Brand brand) {
        publishAfterCommit(
                CatalogEventTypes.BRAND_UPDATED,
                BrandCatalogPayload.from(brand),
                brand.getId().toString()
        );
    }

    @Override
    public void publishBrandStatusChanged(Brand brand) {
        publishAfterCommit(
                CatalogEventTypes.BRAND_STATUS_CHANGED,
                BrandCatalogPayload.from(brand),
                brand.getId().toString()
        );
    }

    private <T> void publishAfterCommit(
            String eventType,
            T payload,
            String aggregateId
    ) {
        String correlationId = currentCorrelationId();

        EventEnvelope<T> envelope = EventEnvelope.of(
                eventType,
                SOURCE,
                correlationId,
                payload
        );

        if (TransactionSynchronizationManager.isSynchronizationActive()) {
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override
                public void afterCommit() {
                    publishImmediately(envelope, aggregateId);
                }
            });

            log.debug(
                    "Catalog event registered for after-commit publishing, eventType={}, aggregateId={}, correlationId={}",
                    eventType,
                    aggregateId,
                    correlationId
            );

            return;
        }

        publishImmediately(envelope, aggregateId);
    }

    private <T> void publishImmediately(
            EventEnvelope<T> envelope,
            String aggregateId
    ) {
        String previousEventName = MDC.get("eventName");

        try {
            MDC.put("eventName", envelope.eventType());

            rabbitTemplate.convertAndSend(
                    EventBrokerConstants.MAIN_EXCHANGE,
                    envelope.eventType(),
                    envelope,
                    message -> {
                        message.getMessageProperties()
                                .setHeader(EventBrokerConstants.EVENT_ID_HEADER, envelope.eventId());
                        message.getMessageProperties()
                                .setHeader(EventBrokerConstants.EVENT_TYPE_HEADER, envelope.eventType());
                        message.getMessageProperties()
                                .setHeader(SOURCE_SERVICE_HEADER, SOURCE);

                        if (envelope.correlationId() != null && !envelope.correlationId().isBlank()) {
                            message.getMessageProperties()
                                    .setHeader(
                                            EventBrokerConstants.CORRELATION_ID_HEADER,
                                            envelope.correlationId()
                                    );
                        }

                        return message;
                    }
            );

            log.info(
                    "Catalog event published, eventId={}, eventType={}, aggregateId={}, correlationId={}",
                    envelope.eventId(),
                    envelope.eventType(),
                    aggregateId,
                    envelope.correlationId()
            );
        } catch (Exception ex) {
            log.error(
                    "Failed to publish catalog event, eventId={}, eventType={}, aggregateId={}, correlationId={}",
                    envelope.eventId(),
                    envelope.eventType(),
                    aggregateId,
                    envelope.correlationId(),
                    ex
            );
        } finally {
            if (previousEventName == null) {
                MDC.remove("eventName");
            } else {
                MDC.put("eventName", previousEventName);
            }
        }
    }

    private String currentCorrelationId() {
        String mdcCorrelationId = MDC.get("correlationId");

        if (mdcCorrelationId != null && !mdcCorrelationId.isBlank()) {
            return mdcCorrelationId.trim();
        }

        RequestAttributes attributes = RequestContextHolder.getRequestAttributes();

        if (!(attributes instanceof ServletRequestAttributes servletRequestAttributes)) {
            return null;
        }

        HttpServletRequest request = servletRequestAttributes.getRequest();

        String correlationId = request.getHeader(PlatformHeaders.X_CORRELATION_ID);

        return correlationId == null || correlationId.isBlank()
                ? null
                : correlationId.trim();
    }
}