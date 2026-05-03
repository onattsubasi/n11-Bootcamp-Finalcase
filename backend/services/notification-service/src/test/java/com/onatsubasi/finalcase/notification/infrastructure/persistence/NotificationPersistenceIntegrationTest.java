package com.onatsubasi.finalcase.notification.infrastructure.persistence;

import com.onatsubasi.finalcase.notification.domain.enums.NotificationReferenceType;
import com.onatsubasi.finalcase.notification.domain.enums.NotificationType;
import com.onatsubasi.finalcase.notification.domain.entity.Notification;
import com.onatsubasi.finalcase.notification.support.PostgresDataJpaTestBase;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;

import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest(properties = {
        "spring.flyway.enabled=true",
        "spring.jpa.hibernate.ddl-auto=validate"
})
@Import({JpaNotificationRepository.class})
class NotificationPersistenceIntegrationTest extends PostgresDataJpaTestBase {

    @Autowired
    private SpringDataNotificationJpaRepository springDataRepository;

    @Autowired
    private JpaNotificationRepository notificationRepository;

    @Test
    void flywaySchemaMatchesJpaMappingsAndUnreadCountWorks() {
        UUID userId = UUID.randomUUID();
        Notification notification = new Notification(
                userId,
                "customer@example.com",
                null,
                NotificationType.ORDER_PAID,
                NotificationReferenceType.ORDER,
                "order-1",
                "tr",
                "Order paid",
                "Your order is paid",
                Map.of("orderNumber", "ORD-1")
        );

        Notification saved = springDataRepository.saveAndFlush(notification);

        assertThat(saved.getId()).isNotNull();
        assertThat(notificationRepository.countUnreadByRecipientUserId(userId)).isEqualTo(1);

        int updated = notificationRepository.markAllReadByRecipientUserId(userId);

        assertThat(updated).isEqualTo(1);
        assertThat(notificationRepository.countUnreadByRecipientUserId(userId)).isZero();
    }
}
