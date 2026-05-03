package com.onatsubasi.finalcase.review.infrastructure.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter
@Setter
@ConfigurationProperties(prefix = "review.moderation")
public class ReviewModerationProperties {

    private boolean autoApprove = true;
}
