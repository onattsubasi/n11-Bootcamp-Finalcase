package com.onatsubasi.finalcase.review.infrastructure.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter
@Setter
@ConfigurationProperties(prefix = "review")
public class ReviewServiceProperties {

    private boolean autoApproveReviews = true;

    private int publicPageMaxSize = 100;

    private int adminPageMaxSize = 100;
}