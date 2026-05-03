package com.onatsubasi.finalcase.notification.infrastructure.template;

import com.onatsubasi.finalcase.common.core.exception.BaseException;
import com.onatsubasi.finalcase.notification.application.dto.command.RenderedNotification;
import com.onatsubasi.finalcase.notification.application.port.TemplateRenderer;
import com.onatsubasi.finalcase.notification.domain.exception.NotificationErrorCode;
import com.onatsubasi.finalcase.notification.domain.entity.NotificationTemplate;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Map;

@Slf4j
@Component
public class SimpleTemplateRenderer implements TemplateRenderer {

    @Override
    public RenderedNotification render(
            NotificationTemplate template,
            Map<String, Object> variables
    ) {
        if (template == null) {
            throw new BaseException(NotificationErrorCode.NOTIFICATION_TEMPLATE_NOT_FOUND);
        }

        if (!template.isActive()) {
            throw new BaseException(NotificationErrorCode.NOTIFICATION_TEMPLATE_INACTIVE);
        }

        Map<String, Object> safeVariables = variables == null
                ? Map.of()
                : variables;

        validateRequiredVariables(template, safeVariables);

        String title = renderText(template.getTitleTemplate(), safeVariables);
        String message = renderText(template.getMessageTemplate(), safeVariables);

        return new RenderedNotification(title, message);
    }

    private void validateRequiredVariables(
            NotificationTemplate template,
            Map<String, Object> variables
    ) {
        for (String variable : template.getRequiredVariables()) {
            if (!variables.containsKey(variable) || variables.get(variable) == null) {
                log.warn(
                        "event=notification.template_variable_missing templateId={} variable={}",
                        template.getId(),
                        variable
                );

                throw new BaseException(
                        NotificationErrorCode.NOTIFICATION_TEMPLATE_VARIABLE_MISSING,
                        "Missing template variable: " + variable
                );
            }
        }
    }

    private String renderText(
            String template,
            Map<String, Object> variables
    ) {
        String rendered = template;

        for (Map.Entry<String, Object> entry : variables.entrySet()) {
            String placeholder = "{{" + entry.getKey() + "}}";
            String value = String.valueOf(entry.getValue());

            rendered = rendered.replace(placeholder, value);
        }

        return rendered;
    }
}