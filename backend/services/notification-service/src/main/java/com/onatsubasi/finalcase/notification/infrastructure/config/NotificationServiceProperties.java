package com.onatsubasi.finalcase.notification.infrastructure.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter
@Setter
@ConfigurationProperties(prefix = "notification")
public class NotificationServiceProperties {

    private String defaultLocale = "tr";

    private int maxDeliveryAttempts = 3;

    private int retryBatchSize = 50;

    private int firstRetryDelaySeconds = 60;

    private int secondRetryDelaySeconds = 300;

    private int fallbackRetryDelaySeconds = 900;
}