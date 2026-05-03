package com.onatsubasi.finalcase.notification.infrastructure.template;

import com.onatsubasi.finalcase.common.core.exception.BaseException;
import com.onatsubasi.finalcase.notification.application.dto.command.RenderedNotification;
import com.onatsubasi.finalcase.notification.domain.enums.NotificationChannel;
import com.onatsubasi.finalcase.notification.domain.enums.NotificationType;
import com.onatsubasi.finalcase.notification.domain.exception.NotificationErrorCode;
import com.onatsubasi.finalcase.notification.domain.entity.NotificationTemplate;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class SimpleTemplateRendererTest {

    private final SimpleTemplateRenderer renderer = new SimpleTemplateRenderer();

    @Test
    void rendersRequiredVariables() {
        NotificationTemplate template = new NotificationTemplate(
                NotificationType.ORDER_PAID,
                NotificationChannel.IN_APP,
                "tr",
                "Sipariş {{orderNumber}} ödendi",
                "Merhaba {{name}}, siparişiniz hazır.",
                List.of("orderNumber", "name"),
                true
        );

        RenderedNotification rendered = renderer.render(template, Map.of(
                "orderNumber", "ORD-1",
                "name", "Oytun"
        ));

        assertThat(rendered.title()).isEqualTo("Sipariş ORD-1 ödendi");
        assertThat(rendered.message()).isEqualTo("Merhaba Oytun, siparişiniz hazır.");
    }

    @Test
    void throwsWhenRequiredVariableIsMissing() {
        NotificationTemplate template = new NotificationTemplate(
                NotificationType.ORDER_PAID,
                NotificationChannel.IN_APP,
                "tr",
                "Sipariş {{orderNumber}}",
                "Mesaj",
                List.of("orderNumber"),
                true
        );

        assertThatThrownBy(() -> renderer.render(template, Map.of()))
                .isInstanceOf(BaseException.class)
                .extracting(ex -> ((BaseException) ex).getErrorCode())
                .isEqualTo(NotificationErrorCode.NOTIFICATION_TEMPLATE_VARIABLE_MISSING);
    }
}
