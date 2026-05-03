package com.onatsubasi.finalcase.basket;

import org.springframework.amqp.rabbit.annotation.EnableRabbit;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication(scanBasePackages = "com.onatsubasi.finalcase")
@ConfigurationPropertiesScan(basePackages = "com.onatsubasi.finalcase.basket")
@EnableRabbit
@EnableDiscoveryClient
@EnableScheduling
public class BasketServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(BasketServiceApplication.class, args);
    }
}