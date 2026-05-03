package com.onatsubasi.finalcase.payment.infrastructure.config;

import com.iyzipay.Options;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@RequiredArgsConstructor
public class IyzicoConfig {

    private final IyzicoProperties properties;

    @Bean
    public Options iyzicoOptions() {
        Options options = new Options();
        options.setApiKey(properties.getApiKey());
        options.setSecretKey(properties.getSecretKey());
        options.setBaseUrl(properties.getBaseUrl());
        return options;
    }
}