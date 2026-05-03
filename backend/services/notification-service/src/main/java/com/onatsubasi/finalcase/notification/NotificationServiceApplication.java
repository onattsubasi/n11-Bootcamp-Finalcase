package com.onatsubasi.finalcase.notification;

import com.onatsubasi.finalcase.notification.infrastructure.config.NotificationServiceProperties;
import org.springframework.amqp.rabbit.annotation.EnableRabbit;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication(scanBasePackages = "com.onatsubasi.finalcase")
@EnableDiscoveryClient
@EnableRabbit
@EnableScheduling
@EnableConfigurationProperties(NotificationServiceProperties.class)
public class NotificationServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(NotificationServiceApplication.class, args);
    }
}
