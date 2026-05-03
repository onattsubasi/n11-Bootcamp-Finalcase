package com.onatsubasi.finalcase.notification.infrastructure.mapper;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.onatsubasi.finalcase.notification.application.dto.command.CreateNotificationCommand;
import com.onatsubasi.finalcase.notification.application.dto.provider.NotificationChangedEvent;
import com.onatsubasi.finalcase.notification.application.dto.provider.NotificationProviderSendCommand;
import com.onatsubasi.finalcase.notification.application.dto.response.*;
import com.onatsubasi.finalcase.notification.domain.enums.NotificationProvider;
import com.onatsubasi.finalcase.notification.domain.entity.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class NotificationMapper {

    private final ObjectMapper objectMapper;

    public Notification toNotification(
            CreateNotificationCommand command,
            String title,
            String message
    ) {
        return new Notification(
                command.recipientUserId(),
                command.recipientEmail(),
                command.recipientPhone(),
                command.type(),
                command.referenceType(),
                command.referenceId(),
                command.locale(),
                title,
                message,
                command.payloadSnapshot()
        );
    }

    public NotificationSummaryResponse toSummaryResponse(Notification notification) {
        return new NotificationSummaryResponse(
                notification.getId(),
                notification.getRecipientUserId(),
                notification.getType(),
                notification.getStatus(),
                notification.getReferenceType(),
                notification.getReferenceId(),
                notification.getTitle(),
                notification.getMessage(),
                notification.isRead(),
                notification.getReadAt(),
                notification.getCreatedAt()
        );
    }

    public NotificationDetailResponse toDetailResponse(Notification notification) {
        return new NotificationDetailResponse(
                notification.getId(),
                notification.getRecipientUserId(),
                notification.getRecipientEmail(),
                notification.getRecipientPhone(),
                notification.getType(),
                notification.getStatus(),
                notification.getReferenceType(),
                notification.getReferenceId(),
                notification.getLocale(),
                notification.getTitle(),
                notification.getMessage(),
                notification.getPayloadSnapshot(),
                notification.isRead(),
                notification.getReadAt(),
                notification.getDeliveries()
                        .stream()
                        .map(this::toDeliveryResponse)
                        .toList(),
                notification.getCreatedAt(),
                notification.getUpdatedAt()
        );
    }

    public NotificationReadResponse toReadResponse(Notification notification) {
        return new NotificationReadResponse(
                notification.getId(),
                notification.isRead(),
                notification.getReadAt()
        );
    }

    public NotificationDeliveryResponse toDeliveryResponse(NotificationDelivery delivery) {
        return new NotificationDeliveryResponse(
                delivery.getId(),
                delivery.getChannel(),
                delivery.getProvider(),
                delivery.getRecipientAddress(),
                delivery.getStatus(),
                delivery.getAttemptCount(),
                delivery.getMaxAttempts(),
                delivery.getProviderMessageId(),
                delivery.getLastError(),
                delivery.getNextRetryAt(),
                delivery.getSentAt(),
                delivery.getAttempts()
                        .stream()
                        .map(this::toDeliveryAttemptResponse)
                        .toList()
        );
    }

    public NotificationDeliveryAttemptResponse toDeliveryAttemptResponse(
            NotificationDeliveryAttempt attempt
    ) {
        return new NotificationDeliveryAttemptResponse(
                attempt.getId(),
                attempt.getAttemptNumber(),
                attempt.getStatus(),
                attempt.getProviderMessageId(),
                attempt.getErrorMessage(),
                attempt.isRetryable(),
                attempt.getRequestSnapshot(),
                attempt.getResponseSnapshot(),
                attempt.getCreatedAt()
        );
    }

    public NotificationTemplateResponse toTemplateResponse(NotificationTemplate template) {
        return new NotificationTemplateResponse(
                template.getId(),
                template.getType(),
                template.getChannel(),
                template.getLocale(),
                template.getTitleTemplate(),
                template.getMessageTemplate(),
                template.getRequiredVariables(),
                template.isActive(),
                template.getCreatedAt(),
                template.getUpdatedAt()
        );
    }

    public ProcessedNotificationEventResponse toProcessedEventResponse(
            NotificationProcessedEvent event
    ) {
        return new ProcessedNotificationEventResponse(
                event.getId(),
                event.getEventId(),
                event.getEventType(),
                event.getStatus(),
                event.getErrorMessage(),
                event.getProcessedAt()
        );
    }

    public NotificationProviderSendCommand toProviderSendCommand(
            Notification notification,
            NotificationDelivery delivery
    ) {
        return new NotificationProviderSendCommand(
                notification.getId(),
                delivery.getId(),
                delivery.getChannel(),
                delivery.getProvider(),
                delivery.getRecipientAddress(),
                notification.getTitle(),
                notification.getMessage(),
                notification.getPayloadSnapshot()
        );
    }

    public NotificationDelivery toDelivery(
            NotificationProvider provider,
            CreateNotificationCommand command,
            int maxAttempts
    ) {
        return new NotificationDelivery(
                providerToChannel(provider),
                provider,
                resolveDestination(provider, command),
                maxAttempts
        );
    }

    public NotificationChangedEvent toChangedEvent(Notification notification) {
        return NotificationChangedEvent.from(notification);
    }

    public Map<String, Object> toMap(Object value) {
        if (value == null) {
            return new HashMap<>();
        }

        return objectMapper.convertValue(
                value,
                new TypeReference<Map<String, Object>>() {
                }
        );
    }

    private com.onatsubasi.finalcase.notification.domain.enums.NotificationChannel providerToChannel(
            NotificationProvider provider
    ) {
        return switch (provider) {
            case IN_APP -> com.onatsubasi.finalcase.notification.domain.enums.NotificationChannel.IN_APP;
            case MOCK_EMAIL, SMTP, SENDGRID, MAILGUN, AWS_SES ->
                    com.onatsubasi.finalcase.notification.domain.enums.NotificationChannel.EMAIL;
            case TWILIO, NETGSM ->
                    com.onatsubasi.finalcase.notification.domain.enums.NotificationChannel.SMS;
            case FCM ->
                    com.onatsubasi.finalcase.notification.domain.enums.NotificationChannel.PUSH;
            case WEBHOOK ->
                    com.onatsubasi.finalcase.notification.domain.enums.NotificationChannel.WEBHOOK;
        };
    }

    private String resolveDestination(
            NotificationProvider provider,
            CreateNotificationCommand command
    ) {
        return switch (provider) {
            case IN_APP -> command.recipientUserId().toString();
            case MOCK_EMAIL, SMTP, SENDGRID, MAILGUN, AWS_SES -> command.recipientEmail();
            case TWILIO, NETGSM -> command.recipientPhone();
            case FCM, WEBHOOK -> null;
        };
    }
}