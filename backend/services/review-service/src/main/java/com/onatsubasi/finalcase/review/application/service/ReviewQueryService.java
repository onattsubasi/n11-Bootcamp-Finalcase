package com.onatsubasi.finalcase.review.application.service;

import com.onatsubasi.finalcase.common.core.exception.BaseException;
import com.onatsubasi.finalcase.common.security.UserContext;
import com.onatsubasi.finalcase.review.application.dto.response.*;
import com.onatsubasi.finalcase.review.domain.enums.ReviewSort;
import com.onatsubasi.finalcase.review.domain.enums.ReviewStatus;
import com.onatsubasi.finalcase.review.domain.exception.ReviewErrorCode;
import com.onatsubasi.finalcase.review.domain.entity.Review;
import com.onatsubasi.finalcase.review.domain.repository.ReviewRepository;
import com.onatsubasi.finalcase.review.infrastructure.mapper.ReviewMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class ReviewQueryService {

    private final ReviewRepository reviewRepository;
    private final RatingSummaryService ratingSummaryService;
    private final ReviewMapper reviewMapper;

    @Transactional(readOnly = true)
    public ProductRatingSummaryResponse getProductRatingSummary(UUID productId) {
        return ratingSummaryService.getSummary(productId);
    }

    @Transactional(readOnly = true)
    public PublicReviewListResponse listProductReviews(
            UUID productId,
            Integer rating,
            boolean withImagesOnly,
            ReviewSort sort,
            int page,
            int size
    ) {
        Pageable pageable = PageRequest.of(
                Math.max(page, 0),
                Math.min(Math.max(size, 1), 100),
                toSort(sort)
        );

        Page<Review> reviews = reviewRepository.findPublicReviews(
                productId,
                rating,
                withImagesOnly,
                pageable
        );

        ProductRatingSummaryResponse summary = ratingSummaryService.getSummary(productId);

        return new PublicReviewListResponse(
                summary,
                reviews.getContent()
                        .stream()
                        .map(reviewMapper::toPublicResponse)
                        .toList(),
                reviews.getNumber(),
                reviews.getSize(),
                reviews.getTotalElements(),
                reviews.getTotalPages(),
                reviews.isFirst(),
                reviews.isLast()
        );
    }

    @Transactional(readOnly = true)
    public PageResponse<CustomerReviewResponse> listMyReviews(
            UserContext currentUser,
            int page,
            int size
    ) {
        UUID userId = requireUserId(currentUser);

        Pageable pageable = PageRequest.of(
                Math.max(page, 0),
                Math.min(Math.max(size, 1), 100),
                Sort.by(Sort.Direction.DESC, "createdAt")
        );

        Page<Review> reviews = reviewRepository.findByUserId(userId, pageable);

        return new PageResponse<>(
                reviews.getContent()
                        .stream()
                        .map(reviewMapper::toCustomerResponse)
                        .toList(),
                reviews.getNumber(),
                reviews.getSize(),
                reviews.getTotalElements(),
                reviews.getTotalPages(),
                reviews.isFirst(),
                reviews.isLast()
        );
    }

    @Transactional(readOnly = true)
    public Optional<CustomerReviewResponse> getMyReviewForProduct(
            UserContext currentUser,
            UUID productId
    ) {
        UUID userId = requireUserId(currentUser);

        return reviewRepository.findActiveByUserIdAndProductId(userId, productId)
                .map(reviewMapper::toCustomerResponse);
    }

    @Transactional(readOnly = true)
    public PageResponse<AdminReviewDetailResponse> listAdminReviews(
            ReviewStatus status,
            int page,
            int size
    ) {
        Pageable pageable = PageRequest.of(
                Math.max(page, 0),
                Math.min(Math.max(size, 1), 100),
                Sort.by(Sort.Direction.DESC, "createdAt")
        );

        Page<Review> reviews = status == null
                ? reviewRepository.findAll(pageable)
                : reviewRepository.findByStatus(status, pageable);

        return new PageResponse<>(
                reviews.getContent()
                        .stream()
                        .map(reviewMapper::toAdminResponse)
                        .toList(),
                reviews.getNumber(),
                reviews.getSize(),
                reviews.getTotalElements(),
                reviews.getTotalPages(),
                reviews.isFirst(),
                reviews.isLast()
        );
    }

    @Transactional(readOnly = true)
    public AdminReviewDetailResponse getAdminReview(UUID reviewId) {
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new BaseException(ReviewErrorCode.REVIEW_NOT_FOUND));

        return reviewMapper.toAdminResponse(review);
    }

    private Sort toSort(ReviewSort sort) {
        ReviewSort effectiveSort = sort == null ? ReviewSort.NEWEST : sort;

        return switch (effectiveSort) {
            case NEWEST -> Sort.by(Sort.Direction.DESC, "createdAt");
            case OLDEST -> Sort.by(Sort.Direction.ASC, "createdAt");
            case RATING_DESC -> Sort.by(Sort.Direction.DESC, "rating");
            case RATING_ASC -> Sort.by(Sort.Direction.ASC, "rating");
            case MOST_HELPFUL -> Sort.by(Sort.Direction.DESC, "helpfulCount");
        };
    }

    private UUID requireUserId(UserContext currentUser) {
        if (currentUser == null || !currentUser.isAuthenticated() || currentUser.userId() == null) {
            throw new BaseException(ReviewErrorCode.INVALID_USER_ID);
        }

        return currentUser.userId();
    }
}