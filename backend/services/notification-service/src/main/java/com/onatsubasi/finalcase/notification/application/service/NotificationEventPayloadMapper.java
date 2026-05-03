package com.onatsubasi.finalcase.notification.application.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.onatsubasi.finalcase.notification.application.dto.command.CreateNotificationCommand;
import com.onatsubasi.finalcase.notification.domain.enums.NotificationChannel;
import com.onatsubasi.finalcase.notification.domain.enums.NotificationReferenceType;
import com.onatsubasi.finalcase.notification.domain.enums.NotificationType;
import com.onatsubasi.finalcase.notification.infrastructure.messaging.NotificationEventTypes;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class NotificationEventPayloadMapper {

    private final ObjectMapper objectMapper;
    private final NotificationChannelResolver channelResolver;

    public CreateNotificationCommand toCommand(
            String eventType,
            Object payload
    ) {
        Map<String, Object> data = objectMapper.convertValue(payload, Map.class);

        NotificationType notificationType = toNotificationType(eventType);
        NotificationReferenceType referenceType = toReferenceType(eventType);
        String referenceId = resolveReferenceId(referenceType, data);
        UUID userId = resolveUserId(data);

        if (notificationType == null || referenceType == null || userId == null) {
            return null;
        }

        List<NotificationChannel> channels =
                channelResolver.resolveDefaultChannels(notificationType);

        return new CreateNotificationCommand(
                notificationType,
                userId,
                stringValue(data, "userEmail"),
                stringValue(data, "email"),
                channels,
                stringValueOrDefault(data, "locale", "tr"),
                referenceType,
                referenceId,
                data,
                data
        );
    }

    private NotificationType toNotificationType(String eventType) {
        return switch (eventType) {
            case NotificationEventTypes.ORDER_CREATED -> NotificationType.ORDER_CREATED;
            case NotificationEventTypes.ORDER_PAID -> NotificationType.ORDER_PAID;
            case NotificationEventTypes.ORDER_PAYMENT_FAILED -> NotificationType.ORDER_PAYMENT_FAILED;
            case NotificationEventTypes.ORDER_CANCELLED -> NotificationType.ORDER_CANCELLED;

            case NotificationEventTypes.PAYMENT_SUCCEEDED -> NotificationType.PAYMENT_SUCCEEDED;
            case NotificationEventTypes.PAYMENT_FAILED -> NotificationType.PAYMENT_FAILED;
            case NotificationEventTypes.PAYMENT_REFUNDED -> NotificationType.PAYMENT_REFUNDED;

            case NotificationEventTypes.SHIPMENT_CREATED -> NotificationType.SHIPMENT_CREATED;
            case NotificationEventTypes.SHIPMENT_SHIPPED -> NotificationType.SHIPMENT_SHIPPED;
            case NotificationEventTypes.SHIPMENT_DELIVERED -> NotificationType.SHIPMENT_DELIVERED;
            case NotificationEventTypes.SHIPMENT_DELIVERY_FAILED -> NotificationType.SHIPMENT_DELIVERY_FAILED;
            case NotificationEventTypes.SHIPMENT_CANCELLED -> NotificationType.SHIPMENT_CANCELLED;

            case NotificationEventTypes.CHECKOUT_COMPLETED -> NotificationType.CHECKOUT_COMPLETED;
            case NotificationEventTypes.CHECKOUT_FAILED -> NotificationType.CHECKOUT_FAILED;
            case NotificationEventTypes.CHECKOUT_FINALIZATION_FAILED ->
                    NotificationType.CHECKOUT_FINALIZATION_FAILED_ADMIN_ALERT;

            case NotificationEventTypes.INVENTORY_LOW_STOCK -> NotificationType.LOW_STOCK_ADMIN_ALERT;
            case NotificationEventTypes.INVENTORY_OUT_OF_STOCK -> NotificationType.OUT_OF_STOCK_ADMIN_ALERT;
            case NotificationEventTypes.INVENTORY_BACK_IN_STOCK -> NotificationType.BACK_IN_STOCK;

            case NotificationEventTypes.PROMOTION_AVAILABLE -> NotificationType.PROMOTION_AVAILABLE;
            case NotificationEventTypes.PROMOTION_EXPIRING_SOON -> NotificationType.PROMOTION_EXPIRING_SOON;
            case NotificationEventTypes.COUPON_ASSIGNED -> NotificationType.COUPON_ASSIGNED;

            default -> null;
        };
    }

    private NotificationReferenceType toReferenceType(String eventType) {
        if (eventType.startsWith("order.")) {
            return NotificationReferenceType.ORDER;
        }

        if (eventType.startsWith("payment.")) {
            return NotificationReferenceType.PAYMENT;
        }

        if (eventType.startsWith("shipment.")) {
            return NotificationReferenceType.SHIPMENT;
        }

        if (eventType.startsWith("checkout.")) {
            return NotificationReferenceType.CHECKOUT;
        }

        if (eventType.startsWith("inventory.")) {
            return NotificationReferenceType.INVENTORY;
        }

        if (eventType.startsWith("promotion.")) {
            return NotificationReferenceType.PROMOTION;
        }

        if (eventType.startsWith("coupon.")) {
            return NotificationReferenceType.COUPON;
        }

        return NotificationReferenceType.SYSTEM;
    }

    private String resolveReferenceId(
            NotificationReferenceType referenceType,
            Map<String, Object> data
    ) {
        return switch (referenceType) {
            case ORDER -> firstString(data, "orderId", "id");
            case PAYMENT -> firstString(data, "paymentId", "id");
            case SHIPMENT -> firstString(data, "shipmentId", "id");
            case CHECKOUT -> firstString(data, "checkoutId", "id");
            case INVENTORY -> firstString(data, "inventoryId", "productId", "id");
            case PROMOTION -> firstString(data, "promotionId", "id");
            case COUPON -> firstString(data, "couponId", "id");
            case PRODUCT -> firstString(data, "productId", "id");
            case REVIEW -> firstString(data, "reviewId", "id");
            case SYSTEM -> firstString(data, "id");
        };
    }

    private UUID resolveUserId(Map<String, Object> data) {
        String value = firstString(data, "userId", "recipientUserId", "customerId");

        if (value == null) {
            return null;
        }

        try {
            return UUID.fromString(value);
        } catch (Exception ignored) {
            return null;
        }
    }

    private String firstString(
            Map<String, Object> data,
            String... keys
    ) {
        for (String key : keys) {
            String value = stringValue(data, key);

            if (value != null) {
                return value;
            }
        }

        return null;
    }

    private String stringValue(
            Map<String, Object> data,
            String key
    ) {
        Object value = data.get(key);

        if (value == null) {
            return null;
        }

        String stringValue = String.valueOf(value);

        return stringValue.isBlank() ? null : stringValue;
    }

    private String stringValueOrDefault(
            Map<String, Object> data,
            String key,
            String fallback
    ) {
        String value = stringValue(data, key);

        return value == null ? fallback : value;
    }
}