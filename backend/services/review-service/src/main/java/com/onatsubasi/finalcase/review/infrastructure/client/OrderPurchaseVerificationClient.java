package com.onatsubasi.finalcase.review.infrastructure.client;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.onatsubasi.finalcase.common.core.exception.BaseException;
import com.onatsubasi.finalcase.review.application.dto.internal.VerifiedPurchaseResult;
import com.onatsubasi.finalcase.review.application.port.ReviewOrderGateway;
import com.onatsubasi.finalcase.review.domain.exception.ReviewErrorCode;
import com.onatsubasi.finalcase.review.infrastructure.config.ReviewOrderClientProperties;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.time.Instant;
import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class OrderPurchaseVerificationClient implements ReviewOrderGateway {

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;
    private final ReviewOrderClientProperties properties;

    @Override
    @CircuitBreaker(name = "order-service", fallbackMethod = "verifyDeliveredPurchaseFallback")
    public VerifiedPurchaseResult verifyDeliveredPurchase(UUID userId, UUID productId) {
        try {
            MDC.put("eventName", "review.order.verify_purchase.started");

            String url = UriComponentsBuilder
                    .fromHttpUrl(properties.getBaseUrl())
                    .path("/internal/orders/verify-purchase")
                    .queryParam("userId", userId)
                    .queryParam("productId", productId)
                    .toUriString();

            String responseBody = restTemplate.getForObject(url, String.class);

            if (responseBody == null || responseBody.isBlank()) {
                throw new BaseException(ReviewErrorCode.PURCHASE_VERIFICATION_FAILED);
            }

            JsonNode root = objectMapper.readTree(responseBody);
            JsonNode data = root.has("data") ? root.path("data") : root;

            boolean verified = data.path("verified").asBoolean(false);

            VerifiedPurchaseResult result = new VerifiedPurchaseResult(
                    verified,
                    uuidOrNull(data.path("orderId").asText(null)),
                    uuidOrNull(data.path("orderItemId").asText(null)),
                    textOrNull(data.path("orderNumber").asText(null)),
                    instantOrNull(data.path("deliveredAt").asText(null)));

            MDC.put("eventName", "review.order.verify_purchase.completed");
            log.info(
                    "Order purchase verification completed, userId={}, productId={}, verified={}",
                    userId,
                    productId,
                    result.verified());

            return result;
        } catch (BaseException ex) {
            throw ex;
        } catch (RestClientException ex) {
            throw new BaseException(ReviewErrorCode.ORDER_SERVICE_UNAVAILABLE);
        } catch (Exception ex) {
            throw new BaseException(ReviewErrorCode.PURCHASE_VERIFICATION_FAILED);
        } finally {
            MDC.remove("eventName");
        }
    }

    public VerifiedPurchaseResult verifyDeliveredPurchaseFallback(
            UUID userId,
            UUID productId,
            Throwable throwable) {
        MDC.put("eventName", "review.order.verify_purchase.fallback");

        log.warn(
                "Order purchase verification fallback triggered, userId={}, productId={}, reason={}",
                userId,
                productId,
                throwable == null ? null : throwable.getClass().getSimpleName());

        MDC.remove("eventName");

        throw new BaseException(ReviewErrorCode.ORDER_SERVICE_UNAVAILABLE);
    }

    private UUID uuidOrNull(String value) {
        if (value == null || value.isBlank() || "null".equalsIgnoreCase(value)) {
            return null;
        }

        return UUID.fromString(value);
    }

    private Instant instantOrNull(String value) {
        if (value == null || value.isBlank() || "null".equalsIgnoreCase(value)) {
            return null;
        }

        return Instant.parse(value);
    }

    private String textOrNull(String value) {
        if (value == null || value.isBlank() || "null".equalsIgnoreCase(value)) {
            return null;
        }

        return value;
    }
}
