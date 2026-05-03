package com.onatsubasi.finalcase.review.application.service;

import com.onatsubasi.finalcase.review.application.dto.response.ProductRatingSummaryResponse;
import com.onatsubasi.finalcase.review.application.port.ReviewEventPublisher;
import com.onatsubasi.finalcase.review.domain.entity.ProductRatingSummary;
import com.onatsubasi.finalcase.review.domain.repository.ProductRatingSummaryRepository;
import com.onatsubasi.finalcase.review.domain.repository.RatingSummaryStats;
import com.onatsubasi.finalcase.review.domain.repository.ReviewRepository;
import com.onatsubasi.finalcase.review.infrastructure.mapper.ReviewMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class RatingSummaryService {

    private final ReviewRepository reviewRepository;
    private final ProductRatingSummaryRepository summaryRepository;
    private final ReviewEventPublisher eventPublisher;
    private final ReviewMapper reviewMapper;

    @Transactional
    public ProductRatingSummary recalculate(UUID productId) {
        ProductRatingSummary summary = summaryRepository.findByProductIdForUpdate(productId)
                .orElseGet(() -> ProductRatingSummary.empty(productId));

        RatingSummaryStats stats = reviewRepository.calculateSummaryStats(productId);

        summary.updateCounts(
                stats.rating1Count(),
                stats.rating2Count(),
                stats.rating3Count(),
                stats.rating4Count(),
                stats.rating5Count()
        );

        return summaryRepository.save(summary);
    }

    @Transactional
    public ProductRatingSummaryResponse recalculateAndPublish(UUID productId) {
        try {
            MDC.put("eventName", "review.rating_summary.recalculate.started");

            ProductRatingSummary summary = recalculate(productId);
            eventPublisher.publishRatingSummaryUpdated(summary);

            MDC.put("eventName", "review.rating_summary.updated");
            log.info(
                    "Product rating summary recalculated, productId={}, averageRating={}, reviewCount={}",
                    summary.getProductId(),
                    summary.getAverageRating(),
                    summary.getReviewCount()
            );

            return reviewMapper.toResponse(summary);
        } finally {
            MDC.remove("eventName");
        }
    }

    @Transactional(readOnly = true)
    public ProductRatingSummaryResponse getSummary(UUID productId) {
        ProductRatingSummary summary = summaryRepository.findByProductId(productId)
                .orElseGet(() -> ProductRatingSummary.empty(productId));

        return reviewMapper.toResponse(summary);
    }
}