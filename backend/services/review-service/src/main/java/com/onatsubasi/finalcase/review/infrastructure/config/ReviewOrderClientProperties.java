package com.onatsubasi.finalcase.review.infrastructure.config;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@Getter
@Setter
@Validated
@ConfigurationProperties(prefix = "review.order-client")
public class ReviewOrderClientProperties {

    @NotBlank
    private String baseUrl = "http://order-service";
}
