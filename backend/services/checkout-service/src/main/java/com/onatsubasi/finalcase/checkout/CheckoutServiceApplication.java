package com.onatsubasi.finalcase.checkout;

import org.springframework.amqp.rabbit.annotation.EnableRabbit;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication(scanBasePackages = "com.onatsubasi.finalcase")
@EnableDiscoveryClient
@EnableFeignClients(basePackages = "com.onatsubasi.finalcase.checkout.application.client")
@EnableRabbit
public class CheckoutServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(CheckoutServiceApplication.class, args);
    }
}