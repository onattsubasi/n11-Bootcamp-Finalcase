package com.onatsubasi.finalcase.notification.application.service;

import com.onatsubasi.finalcase.common.core.exception.BaseException;
import com.onatsubasi.finalcase.notification.application.dto.request.UpsertNotificationTemplateRequest;
import com.onatsubasi.finalcase.notification.application.dto.response.NotificationTemplateResponse;
import com.onatsubasi.finalcase.notification.domain.exception.NotificationErrorCode;
import com.onatsubasi.finalcase.notification.domain.entity.NotificationTemplate;
import com.onatsubasi.finalcase.notification.domain.repository.NotificationTemplateRepository;
import com.onatsubasi.finalcase.notification.infrastructure.mapper.NotificationMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationTemplateService {

    private final NotificationTemplateRepository templateRepository;
    private final NotificationMapper notificationMapper;

    @Transactional
    public NotificationTemplateResponse upsertTemplate(
            UpsertNotificationTemplateRequest request
    ) {
        NotificationTemplate template = templateRepository
                .findByTypeAndChannelAndLocale(
                        request.type(),
                        request.channel(),
                        request.locale().trim().toLowerCase()
                )
                .map(existing -> {
                    existing.updateContent(
                            request.titleTemplate(),
                            request.messageTemplate(),
                            request.requiredVariables()
                    );

                    if (Boolean.TRUE.equals(request.active())) {
                        existing.activate();
                    } else if (Boolean.FALSE.equals(request.active())) {
                        existing.deactivate();
                    }

                    return existing;
                })
                .orElseGet(() -> new NotificationTemplate(
                        request.type(),
                        request.channel(),
                        request.locale(),
                        request.titleTemplate(),
                        request.messageTemplate(),
                        request.requiredVariables(),
                        request.active() == null || request.active()
                ));

        NotificationTemplate saved = templateRepository.save(template);

        log.info(
                "event=notification.template_upserted templateId={} type={} channel={} locale={} active={}",
                saved.getId(),
                saved.getType(),
                saved.getChannel(),
                saved.getLocale(),
                saved.isActive()
        );

        return notificationMapper.toTemplateResponse(saved);
    }

    @Transactional
    public NotificationTemplateResponse activateTemplate(UUID templateId) {
        NotificationTemplate template = templateRepository.findById(templateId)
                .orElseThrow(() -> new BaseException(
                        NotificationErrorCode.NOTIFICATION_TEMPLATE_NOT_FOUND
                ));

        template.activate();

        NotificationTemplate saved = templateRepository.save(template);

        return notificationMapper.toTemplateResponse(saved);
    }

    @Transactional
    public NotificationTemplateResponse deactivateTemplate(UUID templateId) {
        NotificationTemplate template = templateRepository.findById(templateId)
                .orElseThrow(() -> new BaseException(
                        NotificationErrorCode.NOTIFICATION_TEMPLATE_NOT_FOUND
                ));

        template.deactivate();

        NotificationTemplate saved = templateRepository.save(template);

        return notificationMapper.toTemplateResponse(saved);
    }
}