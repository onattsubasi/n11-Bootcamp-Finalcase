package com.onatsubasi.finalcase.shipment;

import com.onatsubasi.finalcase.shipment.infrastructure.config.ShipmentServiceProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.amqp.rabbit.annotation.EnableRabbit;

@SpringBootApplication(scanBasePackages = "com.onatsubasi.finalcase")
@EnableDiscoveryClient
@EnableFeignClients(basePackages = "com.onatsubasi.finalcase.shipment.application.client")
@EnableRabbit
@EnableConfigurationProperties(ShipmentServiceProperties.class)
public class ShipmentServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(ShipmentServiceApplication.class, args);
    }
}