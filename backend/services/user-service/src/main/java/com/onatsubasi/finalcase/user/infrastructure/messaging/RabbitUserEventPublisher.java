package com.onatsubasi.finalcase.user.infrastructure.messaging;

import com.onatsubasi.finalcase.common.core.http.PlatformHeaders;
import com.onatsubasi.finalcase.common.event.EventBrokerConstants;
import com.onatsubasi.finalcase.common.event.EventEnvelope;
import com.onatsubasi.finalcase.user.application.port.UserEventPublisher;
import com.onatsubasi.finalcase.user.domain.entity.FavoriteProduct;
import com.onatsubasi.finalcase.user.domain.entity.ProductList;
import com.onatsubasi.finalcase.user.domain.entity.UserAddress;
import com.onatsubasi.finalcase.user.domain.entity.UserProfile;
import com.onatsubasi.finalcase.user.infrastructure.messaging.payload.FavoriteProductPayload;
import com.onatsubasi.finalcase.user.infrastructure.messaging.payload.ProductListPayload;
import com.onatsubasi.finalcase.user.infrastructure.messaging.payload.UserAddressPayload;
import com.onatsubasi.finalcase.user.infrastructure.messaging.payload.UserProfilePayload;
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

import java.util.UUID;

@Component
@RequiredArgsConstructor
@Slf4j
public class RabbitUserEventPublisher implements UserEventPublisher {

    private static final String SOURCE = "user-service";
    private static final String SOURCE_SERVICE_HEADER = "X-Source-Service";

    private final RabbitTemplate rabbitTemplate;

    @Override
    public void publishProfileCreated(UserProfile profile) {
        publishAfterCommit(
                UserEventTypes.PROFILE_CREATED,
                UserProfilePayload.from(profile),
                profile.getUserId().toString()
        );
    }

    @Override
    public void publishProfileUpdated(UserProfile profile) {
        publishAfterCommit(
                UserEventTypes.PROFILE_UPDATED,
                UserProfilePayload.from(profile),
                profile.getUserId().toString()
        );
    }

    @Override
    public void publishAddressCreated(UserAddress address) {
        publishAfterCommit(
                UserEventTypes.ADDRESS_CREATED,
                UserAddressPayload.from(address),
                address.getId().toString()
        );
    }

    @Override
    public void publishAddressUpdated(UserAddress address) {
        publishAfterCommit(
                UserEventTypes.ADDRESS_UPDATED,
                UserAddressPayload.from(address),
                address.getId().toString()
        );
    }

    @Override
    public void publishAddressDeleted(UserAddress address) {
        publishAfterCommit(
                UserEventTypes.ADDRESS_DELETED,
                UserAddressPayload.from(address),
                address.getId().toString()
        );
    }

    @Override
    public void publishFavoriteAdded(FavoriteProduct favoriteProduct) {
        publishAfterCommit(
                UserEventTypes.FAVORITE_ADDED,
                FavoriteProductPayload.from(favoriteProduct),
                favoriteProduct.getUserId().toString()
        );
    }

    @Override
    public void publishFavoriteRemoved(UUID userId, UUID productId) {
        publishAfterCommit(
                UserEventTypes.FAVORITE_REMOVED,
                FavoriteProductPayload.removed(userId, productId),
                userId.toString()
        );
    }

    @Override
    public void publishProductListCreated(ProductList productList) {
        publishAfterCommit(
                UserEventTypes.PRODUCT_LIST_CREATED,
                ProductListPayload.from(productList),
                productList.getId().toString()
        );
    }

    @Override
    public void publishProductListUpdated(ProductList productList) {
        publishAfterCommit(
                UserEventTypes.PRODUCT_LIST_UPDATED,
                ProductListPayload.from(productList),
                productList.getId().toString()
        );
    }

    @Override
    public void publishProductListDeleted(ProductList productList) {
        publishAfterCommit(
                UserEventTypes.PRODUCT_LIST_DELETED,
                ProductListPayload.from(productList),
                productList.getId().toString()
        );
    }

    @Override
    public void publishProductListItemAdded(ProductList productList, UUID productId) {
        publishAfterCommit(
                UserEventTypes.PRODUCT_LIST_ITEM_ADDED,
                ProductListPayload.from(productList, productId),
                productList.getId().toString()
        );
    }

    @Override
    public void publishProductListItemRemoved(ProductList productList, UUID productId) {
        publishAfterCommit(
                UserEventTypes.PRODUCT_LIST_ITEM_REMOVED,
                ProductListPayload.from(productList, productId),
                productList.getId().toString()
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
                    "User event registered for after-commit publishing, eventType={}, aggregateId={}, correlationId={}",
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
                    "User event published, eventId={}, eventType={}, aggregateId={}, correlationId={}",
                    envelope.eventId(),
                    envelope.eventType(),
                    aggregateId,
                    envelope.correlationId()
            );
        } catch (Exception ex) {
            log.error(
                    "Failed to publish user event, eventId={}, eventType={}, aggregateId={}, correlationId={}",
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
