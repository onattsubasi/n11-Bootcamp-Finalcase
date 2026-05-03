package com.onatsubasi.finalcase.notification.application.service;

import com.onatsubasi.finalcase.common.core.exception.BaseException;
import com.onatsubasi.finalcase.notification.application.dto.command.CreateNotificationCommand;
import com.onatsubasi.finalcase.notification.application.dto.command.RenderedNotification;
import com.onatsubasi.finalcase.notification.application.dto.request.CreateDirectNotificationRequest;
import com.onatsubasi.finalcase.notification.application.dto.response.NotificationDetailResponse;
import com.onatsubasi.finalcase.notification.application.port.NotificationEventPublisher;
import com.onatsubasi.finalcase.notification.application.port.TemplateRenderer;
import com.onatsubasi.finalcase.notification.domain.enums.NotificationChannel;
import com.onatsubasi.finalcase.notification.domain.enums.NotificationProvider;
import com.onatsubasi.finalcase.notification.domain.exception.NotificationErrorCode;
import com.onatsubasi.finalcase.notification.domain.entity.Notification;
import com.onatsubasi.finalcase.notification.domain.entity.NotificationDelivery;
import com.onatsubasi.finalcase.notification.domain.entity.NotificationTemplate;
import com.onatsubasi.finalcase.notification.domain.repository.NotificationRepository;
import com.onatsubasi.finalcase.notification.domain.repository.NotificationTemplateRepository;
import com.onatsubasi.finalcase.notification.infrastructure.config.NotificationServiceProperties;
import com.onatsubasi.finalcase.notification.infrastructure.mapper.NotificationMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationCommandService {

    private final NotificationRepository notificationRepository;
    private final NotificationTemplateRepository templateRepository;
    private final TemplateRenderer templateRenderer;
    private final NotificationProviderResolver providerResolver;
    private final NotificationChannelResolver channelResolver;
    private final NotificationDeliveryService deliveryService;
    private final NotificationMapper notificationMapper;
    private final NotificationEventPublisher eventPublisher;
    private final NotificationServiceProperties properties;

    @Transactional
    public NotificationDetailResponse createDirectNotification(
            CreateDirectNotificationRequest request
    ) {
        List<NotificationChannel> channels = request.channels() == null
                || request.channels().isEmpty()
                ? channelResolver.resolveDefaultChannels(request.type())
                : request.channels();

        CreateNotificationCommand command = new CreateNotificationCommand(
                request.type(),
                request.recipientUserId(),
                request.recipientEmail(),
                request.recipientPhone(),
                channels,
                normalizeLocale(request.locale()),
                request.referenceType(),
                request.referenceId(),
                request.templateVariables(),
                request.templateVariables()
        );

        return createNotification(command);
    }

    @Transactional
    public NotificationDetailResponse createNotification(
            CreateNotificationCommand command
    ) {
        List<NotificationChannel> channels = command.channels() == null
                || command.channels().isEmpty()
                ? channelResolver.resolveDefaultChannels(command.type())
                : command.channels();

        RenderedNotification rendered = renderNotification(command, channels);

        Notification notification = notificationMapper.toNotification(
                command,
                rendered.title(),
                rendered.message()
        );

        for (NotificationChannel channel : channels) {
            NotificationProvider provider = providerResolver.resolve(channel);

            NotificationDelivery delivery = notificationMapper.toDelivery(
                    provider,
                    command,
                    properties.getMaxDeliveryAttempts()
            );

            notification.addDelivery(delivery);
        }

        Notification saved = notificationRepository.save(notification);

        eventPublisher.publishNotificationCreated(saved);

        log.info(
                "event=notification.created notificationId={} recipientUserId={} type={} deliveryCount={}",
                saved.getId(),
                saved.getRecipientUserId(),
                saved.getType(),
                saved.getDeliveries().size()
        );

        saved.getDeliveries()
                .forEach(delivery -> deliveryService.sendDelivery(delivery.getId()));

        Notification refreshed = notificationRepository.findById(saved.getId())
                .orElse(saved);

        return notificationMapper.toDetailResponse(refreshed);
    }

    private RenderedNotification renderNotification(
            CreateNotificationCommand command,
            List<NotificationChannel> channels
    ) {
        NotificationChannel templateChannel = channels.contains(NotificationChannel.EMAIL)
                ? NotificationChannel.EMAIL
                : channels.get(0);

        NotificationTemplate template = templateRepository
                .findActiveTemplate(
                        command.type(),
                        templateChannel,
                        normalizeLocale(command.locale())
                )
                .orElseThrow(() -> new BaseException(
                        NotificationErrorCode.NOTIFICATION_TEMPLATE_NOT_FOUND,
                        "Template not found for type=" + command.type()
                                + ", channel=" + templateChannel
                                + ", locale=" + normalizeLocale(command.locale())
                ));

        try {
            return templateRenderer.render(
                    template,
                    command.templateVariables()
            );
        } catch (Exception ex) {
            log.warn(
                    "event=notification.template_render_failed type={} channel={} locale={} reason={}",
                    command.type(),
                    templateChannel,
                    normalizeLocale(command.locale()),
                    ex.getMessage()
            );

            throw ex;
        }
    }

    private String normalizeLocale(String locale) {
        return locale == null || locale.isBlank()
                ? properties.getDefaultLocale()
                : locale.trim().toLowerCase();
    }
}