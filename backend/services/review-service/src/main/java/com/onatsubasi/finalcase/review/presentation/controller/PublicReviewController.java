package com.onatsubasi.finalcase.review.presentation.controller;

import com.onatsubasi.finalcase.common.core.response.ApiResponse;
import com.onatsubasi.finalcase.review.application.dto.response.ProductRatingSummaryResponse;
import com.onatsubasi.finalcase.review.application.dto.response.PublicReviewListResponse;
import com.onatsubasi.finalcase.review.application.service.ReviewQueryService;
import com.onatsubasi.finalcase.review.domain.enums.ReviewSort;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/public/reviews")
@Tag(name = "Public Reviews", description = "Public product reviews and rating summaries")
public class PublicReviewController {

    private final ReviewQueryService reviewQueryService;

    @Operation(
            summary = "Get product rating summary",
            description = "Returns public rating summary for a product."
    )
    @GetMapping("/products/{productId}/summary")
    public ResponseEntity<ApiResponse<ProductRatingSummaryResponse>> getProductRatingSummary(
            @PathVariable UUID productId
    ) {
        return ResponseEntity.ok(
                ApiResponse.success(reviewQueryService.getProductRatingSummary(productId))
        );
    }

    @Operation(
            summary = "List public product reviews",
            description = "Lists approved, visible, non-deleted reviews for a product."
    )
    @GetMapping("/products/{productId}")
    public ResponseEntity<ApiResponse<PublicReviewListResponse>> listProductReviews(
            @PathVariable UUID productId,
            @RequestParam(required = false) Integer rating,
            @RequestParam(defaultValue = "false") boolean withImagesOnly,
            @RequestParam(required = false) ReviewSort sort,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        PublicReviewListResponse response = reviewQueryService.listProductReviews(
                productId,
                rating,
                withImagesOnly,
                sort,
                page,
                size
        );

        return ResponseEntity.ok(
                ApiResponse.success(response)
        );
    }
}