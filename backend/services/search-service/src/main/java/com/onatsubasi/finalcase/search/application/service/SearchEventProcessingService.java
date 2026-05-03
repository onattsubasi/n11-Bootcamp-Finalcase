package com.onatsubasi.finalcase.search.application.service;

import com.onatsubasi.finalcase.common.core.exception.BaseException;
import com.onatsubasi.finalcase.common.event.EventEnvelope;
import com.onatsubasi.finalcase.search.domain.exception.SearchErrorCode;
import com.onatsubasi.finalcase.search.domain.entity.ProcessedSearchEvent;
import com.onatsubasi.finalcase.search.domain.repository.ProcessedSearchEventRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class SearchEventProcessingService {

    private final ProcessedSearchEventRepository processedEventRepository;

    @Transactional
    public boolean processOnce(EventEnvelope<?> envelope, Runnable projectionAction) {
        validateEnvelope(envelope);

        try {
            MDC.put("eventName", "search.event.received");
            MDC.put("eventId", envelope.eventId());
            MDC.put("eventType", envelope.eventType());

            if (processedEventRepository.existsByEventId(envelope.eventId())) {
                MDC.put("eventName", "search.event.duplicate_skipped");
                log.info(
                        "Duplicate search event skipped, eventId={}, eventType={}",
                        envelope.eventId(),
                        envelope.eventType());
                return false;
            }

            projectionAction.run();

            ProcessedSearchEvent processedEvent = ProcessedSearchEvent.create(
                    envelope.eventId(),
                    envelope.eventType(),
                    envelope.source(),
                    aggregateIdOrNull(envelope.payload()),
                    envelope.correlationId(),
                    envelope.occurredAt());

            processedEventRepository.save(processedEvent);

            MDC.put("eventName", "search.event.processed");
            log.info(
                    "Search event processed, eventId={}, eventType={}, source={}",
                    envelope.eventId(),
                    envelope.eventType(),
                    envelope.source());

            return true;
        } catch (DataIntegrityViolationException ex) {
            MDC.put("eventName", "search.event.duplicate_skipped");
            log.info(
                    "Duplicate search event skipped by unique constraint, eventId={}, eventType={}",
                    envelope.eventId(),
                    envelope.eventType());
            return false;
        } catch (BaseException ex) {
            MDC.put("eventName", "search.event.failed");
            MDC.put("errorCode", ex.getErrorCode().code());
            log.warn(
                    "Search event processing failed, eventId={}, eventType={}, errorCode={}",
                    envelope.eventId(),
                    envelope.eventType(),
                    ex.getErrorCode().code());
            throw ex;
        } finally {
            clearMdc();
        }
    }

    private void validateEnvelope(EventEnvelope<?> envelope) {
        if (envelope == null || envelope.eventId() == null || envelope.eventId().isBlank()) {
            throw new BaseException(SearchErrorCode.INVALID_EVENT_DATA, "Event id is required");
        }

        if (envelope.eventType() == null || envelope.eventType().isBlank()) {
            throw new BaseException(SearchErrorCode.INVALID_EVENT_DATA, "Event type is required");
        }
    }

    private String aggregateIdOrNull(Object payload) {
        if (payload == null) {
            return null;
        }

        try {
            Object aggregateId = payload.getClass().getMethod("productId").invoke(payload);
            return aggregateId == null ? null : aggregateId.toString();
        } catch (ReflectiveOperationException ignored) {
            // Some projection events are category/brand scoped. Keep aggregate id optional.
        }

        try {
            Object aggregateId = payload.getClass().getMethod("categoryId").invoke(payload);
            return aggregateId == null ? null : aggregateId.toString();
        } catch (ReflectiveOperationException ignored) {
            // Some projection events are brand scoped. Keep trying known aggregate accessors.
        }

        try {
            Object aggregateId = payload.getClass().getMethod("brandId").invoke(payload);
            return aggregateId == null ? null : aggregateId.toString();
        } catch (ReflectiveOperationException ignored) {
            return null;
        }
    }

    private void clearMdc() {
        MDC.remove("eventName");
        MDC.remove("eventId");
        MDC.remove("eventType");
        MDC.remove("errorCode");
    }
}