package com.onatsubasi.finalcase.review.infrastructure.mapper;

import com.onatsubasi.finalcase.review.application.dto.request.ReviewImageRequest;
import com.onatsubasi.finalcase.review.application.dto.response.*;
import com.onatsubasi.finalcase.review.domain.entity.ProductRatingSummary;
import com.onatsubasi.finalcase.review.domain.entity.Review;
import com.onatsubasi.finalcase.review.domain.entity.ReviewReport;
import com.onatsubasi.finalcase.review.domain.entity.ReviewVote;
import org.springframework.stereotype.Component;

import java.util.*;

@Component
public class ReviewMapper {

    public ProductRatingSummaryResponse toResponse(ProductRatingSummary summary) {
        return new ProductRatingSummaryResponse(
                summary.getProductId(),
                summary.getAverageRating(),
                summary.getReviewCount(),
                summary.getRating1Count(),
                summary.getRating2Count(),
                summary.getRating3Count(),
                summary.getRating4Count(),
                summary.getRating5Count(),
                summary.getUpdatedAt()
        );
    }

    public PublicReviewResponse toPublicResponse(Review review) {
        return new PublicReviewResponse(
                review.getId(),
                review.getProductId(),
                review.getAuthorDisplayName(),
                review.getRating(),
                review.getTitle(),
                review.getComment(),
                toImageResponses(review.getImages()),
                review.isVerifiedPurchase(),
                review.getHelpfulCount(),
                review.getUnhelpfulCount(),
                review.getCreatedAt()
        );
    }

    public CustomerReviewResponse toCustomerResponse(Review review) {
        return new CustomerReviewResponse(
                review.getId(),
                review.getProductId(),
                review.getOrderId(),
                review.getOrderItemId(),
                review.getOrderNumber(),
                review.getRating(),
                review.getTitle(),
                review.getComment(),
                toImageResponses(review.getImages()),
                review.getStatus(),
                review.isVisible(),
                review.isVerifiedPurchase(),
                review.getHelpfulCount(),
                review.getUnhelpfulCount(),
                review.getReportCount(),
                review.getDeliveredAt(),
                review.getCreatedAt(),
                review.getUpdatedAt()
        );
    }

    public AdminReviewDetailResponse toAdminResponse(Review review) {
        return new AdminReviewDetailResponse(
                review.getId(),
                review.getProductId(),
                review.getUserId(),
                review.getOrderId(),
                review.getOrderItemId(),
                review.getOrderNumber(),
                review.getAuthorDisplayName(),
                review.getRating(),
                review.getTitle(),
                review.getComment(),
                toImageResponses(review.getImages()),
                review.getStatus(),
                review.isVisible(),
                review.isVerifiedPurchase(),
                review.getHelpfulCount(),
                review.getUnhelpfulCount(),
                review.getReportCount(),
                review.getModerationMetadata(),
                review.getLastModeratedBy(),
                review.getLastModeratedAt(),
                review.getApprovedAt(),
                review.getRejectedAt(),
                review.getHiddenAt(),
                review.getDeletedAt(),
                review.getDeliveredAt(),
                review.getCreatedAt(),
                review.getUpdatedAt()
        );
    }

    public ReviewVoteResponse toResponse(ReviewVote vote) {
        Review review = vote.getReview();

        return new ReviewVoteResponse(
                vote.getId(),
                review.getId(),
                vote.getUserId(),
                vote.getVoteType(),
                review.getHelpfulCount(),
                review.getUnhelpfulCount(),
                vote.getCreatedAt(),
                vote.getUpdatedAt()
        );
    }

    public ReviewReportResponse toResponse(ReviewReport report) {
        return new ReviewReportResponse(
                report.getId(),
                report.getReview().getId(),
                report.getReporterUserId(),
                report.getReason(),
                report.getDescription(),
                report.getStatus(),
                report.getResolvedBy(),
                report.getResolvedAt(),
                report.getResolutionNote(),
                report.getCreatedAt(),
                report.getUpdatedAt()
        );
    }

    public List<Map<String, Object>> toImageMaps(List<ReviewImageRequest> images) {
        if (images == null || images.isEmpty()) {
            return List.of();
        }

        return images.stream()
                .sorted(Comparator.comparing(
                        image -> image.sortOrder() == null ? Integer.MAX_VALUE : image.sortOrder()
                ))
                .map(image -> {
                    Map<String, Object> map = new LinkedHashMap<>();
                    map.put("url", image.url());
                    map.put("sortOrder", image.sortOrder());
                    return map;
                })
                .toList();
    }

    private List<ReviewImageResponse> toImageResponses(List<Map<String, Object>> images) {
        if (images == null || images.isEmpty()) {
            return List.of();
        }

        return images.stream()
                .map(this::toImageResponse)
                .toList();
    }

    private ReviewImageResponse toImageResponse(Map<String, Object> image) {
        Object url = image.get("url");
        Object sortOrder = image.get("sortOrder");

        return new ReviewImageResponse(
                url == null ? null : url.toString(),
                parseInteger(sortOrder)
        );
    }

    private Integer parseInteger(Object value) {
        if (value == null) {
            return null;
        }

        if (value instanceof Number number) {
            return number.intValue();
        }

        try {
            return Integer.parseInt(value.toString());
        } catch (NumberFormatException ex) {
            return null;
        }
    }

    public String maskedDisplayName(String firstName, String lastName, UUID userId) {
        String first = firstName == null || firstName.isBlank()
                ? "Customer"
                : firstName.trim();

        String last = lastName == null || lastName.isBlank()
                ? ""
                : lastName.trim();

        if (last.isBlank()) {
            return first.charAt(0) + "***";
        }

        return first.charAt(0) + "*** " + last.charAt(0) + "***";
    }
}