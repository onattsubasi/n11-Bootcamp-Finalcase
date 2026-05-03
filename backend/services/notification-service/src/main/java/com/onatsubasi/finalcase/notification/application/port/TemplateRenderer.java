package com.onatsubasi.finalcase.notification.application.port;

import com.onatsubasi.finalcase.notification.application.dto.command.RenderedNotification;
import com.onatsubasi.finalcase.notification.domain.entity.NotificationTemplate;

import java.util.Map;

public interface TemplateRenderer {

    RenderedNotification render(
            NotificationTemplate template,
            Map<String, Object> variables
    );
}