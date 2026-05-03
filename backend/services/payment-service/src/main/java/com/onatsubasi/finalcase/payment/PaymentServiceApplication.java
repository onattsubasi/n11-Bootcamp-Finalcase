package com.onatsubasi.finalcase.payment;

import com.onatsubasi.finalcase.payment.infrastructure.config.IyzicoProperties;
import com.onatsubasi.finalcase.payment.infrastructure.config.PaymentServiceProperties;
import org.springframework.amqp.rabbit.annotation.EnableRabbit;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

@SpringBootApplication(scanBasePackages = "com.onatsubasi.finalcase")
@EnableDiscoveryClient
@EnableRabbit
@EnableConfigurationProperties({
        PaymentServiceProperties.class,
        IyzicoProperties.class
})
public class PaymentServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(PaymentServiceApplication.class, args);
    }
}