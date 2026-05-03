package com.onatsubasi.finalcase.payment.infrastructure.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter
@Setter
@ConfigurationProperties(prefix = "payment.iyzico")
public class IyzicoProperties {

    private String apiKey;

    private String secretKey;

    private String baseUrl;

    private String locale = "tr";

    private String defaultIdentityNumber = "11111111111";

    private String defaultCountry = "Turkey";
}