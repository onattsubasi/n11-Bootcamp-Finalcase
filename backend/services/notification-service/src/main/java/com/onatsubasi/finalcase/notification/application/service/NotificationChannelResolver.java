package com.onatsubasi.finalcase.notification.application.service;

import com.onatsubasi.finalcase.notification.domain.enums.NotificationChannel;
import com.onatsubasi.finalcase.notification.domain.enums.NotificationType;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class NotificationChannelResolver {

    public List<NotificationChannel> resolveDefaultChannels(NotificationType type) {
        return switch (type) {
            case ORDER_CREATED,
                 ORDER_PAID,
                 ORDER_PAYMENT_FAILED,
                 ORDER_CANCELLED,
                 SHIPMENT_CREATED,
                 SHIPMENT_SHIPPED,
                 SHIPMENT_DELIVERED,
                 SHIPMENT_DELIVERY_FAILED,
                 SHIPMENT_CANCELLED,
                 SHIPMENT_TRACKING_UPDATED,
                 PAYMENT_FAILED,
                 PAYMENT_REFUNDED -> List.of(
                    NotificationChannel.IN_APP,
                    NotificationChannel.EMAIL
            );

            case COUPON_ASSIGNED,
                 PROMOTION_AVAILABLE,
                 PROMOTION_EXPIRING_SOON,
                 BACK_IN_STOCK,
                 REVIEW_REQUEST -> List.of(NotificationChannel.IN_APP);

            case LOW_STOCK_ADMIN_ALERT,
                 OUT_OF_STOCK_ADMIN_ALERT,
                 CHECKOUT_FINALIZATION_FAILED_ADMIN_ALERT,
                 SYSTEM_ALERT -> List.of(
                    NotificationChannel.IN_APP,
                    NotificationChannel.EMAIL
            );

            case PAYMENT_SUCCEEDED,
                 CHECKOUT_COMPLETED,
                 CHECKOUT_FAILED -> List.of(NotificationChannel.IN_APP);
        };
    }
}