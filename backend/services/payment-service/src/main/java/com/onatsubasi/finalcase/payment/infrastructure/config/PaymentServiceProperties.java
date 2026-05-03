package com.onatsubasi.finalcase.payment.infrastructure.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter
@Setter
@ConfigurationProperties(prefix = "payment")
public class PaymentServiceProperties {

    private String callbackUrl;

    private String defaultSuccessUrl;

    private String defaultFailureUrl;
}