package com.onatsubasi.finalcase.review.infrastructure.messaging;

import com.onatsubasi.finalcase.common.core.http.PlatformHeaders;
import com.onatsubasi.finalcase.common.event.EventBrokerConstants;
import com.onatsubasi.finalcase.common.event.EventEnvelope;
import com.onatsubasi.finalcase.review.application.port.ReviewEventPublisher;
import com.onatsubasi.finalcase.review.domain.entity.ProductRatingSummary;
import com.onatsubasi.finalcase.review.domain.entity.Review;
import com.onatsubasi.finalcase.review.domain.entity.ReviewReport;
import com.onatsubasi.finalcase.review.domain.entity.ReviewVote;
import com.onatsubasi.finalcase.review.infrastructure.messaging.payload.RatingSummaryPayload;
import com.onatsubasi.finalcase.review.infrastructure.messaging.payload.ReviewPayload;
import com.onatsubasi.finalcase.review.infrastructure.messaging.payload.ReviewReportPayload;
import com.onatsubasi.finalcase.review.infrastructure.messaging.payload.ReviewVotePayload;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

@Component
@RequiredArgsConstructor
@Slf4j
public class RabbitReviewEventPublisher implements ReviewEventPublisher {

    private static final String SOURCE = "review-service";
    private static final String SOURCE_SERVICE_HEADER = "X-Source-Service";

    private final RabbitTemplate rabbitTemplate;

    @Override
    public void publishReviewSubmitted(Review review) {
        publishAfterCommit(
                ReviewEventTypes.REVIEW_SUBMITTED,
                ReviewPayload.from(review),
                review.getId().toString()
        );
    }

    @Override
    public void publishReviewApproved(Review review) {
        publishAfterCommit(
                ReviewEventTypes.REVIEW_APPROVED,
                ReviewPayload.from(review),
                review.getId().toString()
        );
    }

    @Override
    public void publishReviewRejected(Review review) {
        publishAfterCommit(
                ReviewEventTypes.REVIEW_REJECTED,
                ReviewPayload.from(review),
                review.getId().toString()
        );
    }

    @Override
    public void publishReviewHidden(Review review) {
        publishAfterCommit(
                ReviewEventTypes.REVIEW_HIDDEN,
                ReviewPayload.from(review),
                review.getId().toString()
        );
    }

    @Override
    public void publishReviewRestored(Review review) {
        publishAfterCommit(
                ReviewEventTypes.REVIEW_RESTORED,
                ReviewPayload.from(review),
                review.getId().toString()
        );
    }

    @Override
    public void publishReviewDeleted(Review review) {
        publishAfterCommit(
                ReviewEventTypes.REVIEW_DELETED,
                ReviewPayload.from(review),
                review.getId().toString()
        );
    }

    @Override
    public void publishReviewUpdated(Review review) {
        publishAfterCommit(
                ReviewEventTypes.REVIEW_UPDATED,
                ReviewPayload.from(review),
                review.getId().toString()
        );
    }

    @Override
    public void publishRatingSummaryUpdated(ProductRatingSummary summary) {
        publishAfterCommit(
                ReviewEventTypes.RATING_SUMMARY_UPDATED,
                RatingSummaryPayload.from(summary),
                summary.getProductId().toString()
        );
    }

    @Override
    public void publishReviewVoted(ReviewVote vote) {
        publishAfterCommit(
                ReviewEventTypes.REVIEW_VOTED,
                ReviewVotePayload.from(vote),
                vote.getReview().getId().toString()
        );
    }

    @Override
    public void publishReviewVoteRemoved(Review review) {
        publishAfterCommit(
                ReviewEventTypes.REVIEW_VOTE_REMOVED,
                ReviewVotePayload.removed(review),
                review.getId().toString()
        );
    }

    @Override
    public void publishReviewReported(ReviewReport report) {
        publishAfterCommit(
                ReviewEventTypes.REVIEW_REPORTED,
                ReviewReportPayload.from(report),
                report.getId().toString()
        );
    }

    @Override
    public void publishReviewReportResolved(ReviewReport report) {
        publishAfterCommit(
                ReviewEventTypes.REVIEW_REPORT_RESOLVED,
                ReviewReportPayload.from(report),
                report.getId().toString()
        );
    }

    private <T> void publishAfterCommit(
            String eventType,
            T payload,
            String aggregateId
    ) {
        String correlationId = currentCorrelationId();

        EventEnvelope<T> envelope = EventEnvelope.of(
                eventType,
                SOURCE,
                correlationId,
                payload
        );

        if (TransactionSynchronizationManager.isSynchronizationActive()) {
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override
                public void afterCommit() {
                    publishImmediately(envelope, aggregateId);
                }
            });

            log.debug(
                    "Review event registered for after-commit publishing, eventType={}, aggregateId={}, correlationId={}",
                    eventType,
                    aggregateId,
                    correlationId
            );

            return;
        }

        publishImmediately(envelope, aggregateId);
    }

    private <T> void publishImmediately(
            EventEnvelope<T> envelope,
            String aggregateId
    ) {
        String previousEventName = MDC.get("eventName");

        try {
            MDC.put("eventName", envelope.eventType());

            rabbitTemplate.convertAndSend(
                    EventBrokerConstants.MAIN_EXCHANGE,
                    envelope.eventType(),
                    envelope,
                    message -> {
                        message.getMessageProperties()
                                .setHeader(EventBrokerConstants.EVENT_ID_HEADER, envelope.eventId());
                        message.getMessageProperties()
                                .setHeader(EventBrokerConstants.EVENT_TYPE_HEADER, envelope.eventType());
                        message.getMessageProperties()
                                .setHeader(SOURCE_SERVICE_HEADER, SOURCE);

                        if (envelope.correlationId() != null && !envelope.correlationId().isBlank()) {
                            message.getMessageProperties()
                                    .setHeader(
                                            EventBrokerConstants.CORRELATION_ID_HEADER,
                                            envelope.correlationId()
                                    );
                        }

                        return message;
                    }
            );

            log.info(
                    "Review event published, eventId={}, eventType={}, aggregateId={}, correlationId={}",
                    envelope.eventId(),
                    envelope.eventType(),
                    aggregateId,
                    envelope.correlationId()
            );
        } catch (Exception ex) {
            log.error(
                    "Failed to publish review event, eventId={}, eventType={}, aggregateId={}, correlationId={}",
                    envelope.eventId(),
                    envelope.eventType(),
                    aggregateId,
                    envelope.correlationId(),
                    ex
            );
        } finally {
            if (previousEventName == null) {
                MDC.remove("eventName");
            } else {
                MDC.put("eventName", previousEventName);
            }
        }
    }

    private String currentCorrelationId() {
        String mdcCorrelationId = MDC.get("correlationId");

        if (mdcCorrelationId != null && !mdcCorrelationId.isBlank()) {
            return mdcCorrelationId.trim();
        }

        RequestAttributes attributes = RequestContextHolder.getRequestAttributes();

        if (!(attributes instanceof ServletRequestAttributes servletRequestAttributes)) {
            return null;
        }

        HttpServletRequest request = servletRequestAttributes.getRequest();
        String correlationId = request.getHeader(PlatformHeaders.X_CORRELATION_ID);

        return correlationId == null || correlationId.isBlank()
                ? null
                : correlationId.trim();
    }
}