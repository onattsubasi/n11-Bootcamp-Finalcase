package com.onatsubasi.finalcase.review.presentation.controller;

import com.onatsubasi.finalcase.common.core.response.ApiResponse;
import com.onatsubasi.finalcase.common.security.CurrentUser;
import com.onatsubasi.finalcase.common.security.UserContext;
import com.onatsubasi.finalcase.review.application.dto.request.CreateReviewRequest;
import com.onatsubasi.finalcase.review.application.dto.request.ReportReviewRequest;
import com.onatsubasi.finalcase.review.application.dto.request.UpdateReviewRequest;
import com.onatsubasi.finalcase.review.application.dto.request.VoteReviewRequest;
import com.onatsubasi.finalcase.review.application.dto.response.CustomerReviewResponse;
import com.onatsubasi.finalcase.review.application.dto.response.PageResponse;
import com.onatsubasi.finalcase.review.application.dto.response.ReviewReportResponse;
import com.onatsubasi.finalcase.review.application.dto.response.ReviewVoteResponse;
import com.onatsubasi.finalcase.review.application.service.ReviewCommandService;
import com.onatsubasi.finalcase.review.application.service.ReviewQueryService;
import com.onatsubasi.finalcase.review.application.service.ReviewReportService;
import com.onatsubasi.finalcase.review.application.service.ReviewVoteService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;
import java.util.UUID;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/customer/reviews")
@Tag(name = "Customer Reviews", description = "Customer review, vote, and report operations")
public class CustomerReviewController {

    private final ReviewCommandService reviewCommandService;
    private final ReviewQueryService reviewQueryService;
    private final ReviewVoteService reviewVoteService;
    private final ReviewReportService reviewReportService;

    @Operation(
            summary = "Create review",
            description = "Creates a verified-purchase review for a delivered product.",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @PostMapping
    public ResponseEntity<ApiResponse<CustomerReviewResponse>> createReview(
            @CurrentUser UserContext currentUser,
            @Valid @RequestBody CreateReviewRequest request
    ) {
        CustomerReviewResponse response = reviewCommandService.createReview(
                currentUser,
                request
        );

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success("Review created successfully", response));
    }

    @Operation(
            summary = "Update my review",
            description = "Updates current customer's own review.",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @PutMapping("/{reviewId}")
    public ResponseEntity<ApiResponse<CustomerReviewResponse>> updateReview(
            @CurrentUser UserContext currentUser,
            @PathVariable UUID reviewId,
            @Valid @RequestBody UpdateReviewRequest request
    ) {
        CustomerReviewResponse response = reviewCommandService.updateReview(
                currentUser,
                reviewId,
                request
        );

        return ResponseEntity.ok(
                ApiResponse.success("Review updated successfully", response)
        );
    }

    @Operation(
            summary = "Delete my review",
            description = "Soft-deletes current customer's own review.",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @DeleteMapping("/{reviewId}")
    public ResponseEntity<ApiResponse<Void>> deleteReview(
            @CurrentUser UserContext currentUser,
            @PathVariable UUID reviewId
    ) {
        reviewCommandService.deleteMyReview(currentUser, reviewId);

        return ResponseEntity.ok(
                ApiResponse.success("Review deleted successfully")
        );
    }

    @Operation(
            summary = "List my reviews",
            description = "Lists current customer's reviews.",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @GetMapping("/me")
    public ResponseEntity<ApiResponse<PageResponse<CustomerReviewResponse>>> listMyReviews(
            @CurrentUser UserContext currentUser,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        return ResponseEntity.ok(
                ApiResponse.success(reviewQueryService.listMyReviews(currentUser, page, size))
        );
    }

    @Operation(
            summary = "Get my review for product",
            description = "Returns current customer's active review for a product if it exists.",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @GetMapping("/products/{productId}/me")
    public ResponseEntity<ApiResponse<CustomerReviewResponse>> getMyReviewForProduct(
            @CurrentUser UserContext currentUser,
            @PathVariable UUID productId
    ) {
        Optional<CustomerReviewResponse> response = reviewQueryService.getMyReviewForProduct(
                currentUser,
                productId
        );

        return ResponseEntity.ok(
                ApiResponse.success(response.orElse(null))
        );
    }

    @Operation(
            summary = "Vote review",
            description = "Votes a review as helpful or unhelpful. Self-vote is not allowed.",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @PostMapping("/{reviewId}/votes")
    public ResponseEntity<ApiResponse<ReviewVoteResponse>> voteReview(
            @CurrentUser UserContext currentUser,
            @PathVariable UUID reviewId,
            @Valid @RequestBody VoteReviewRequest request
    ) {
        ReviewVoteResponse response = reviewVoteService.vote(
                currentUser,
                reviewId,
                request
        );

        return ResponseEntity.ok(
                ApiResponse.success("Review vote saved successfully", response)
        );
    }

    @Operation(
            summary = "Remove review vote",
            description = "Removes current customer's vote from a review.",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @DeleteMapping("/{reviewId}/votes")
    public ResponseEntity<ApiResponse<Void>> removeVote(
            @CurrentUser UserContext currentUser,
            @PathVariable UUID reviewId
    ) {
        reviewVoteService.removeVote(currentUser, reviewId);

        return ResponseEntity.ok(
                ApiResponse.success("Review vote removed successfully")
        );
    }

    @Operation(
            summary = "Report review",
            description = "Reports a review. Self-report and duplicate report are not allowed.",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @PostMapping("/{reviewId}/reports")
    public ResponseEntity<ApiResponse<ReviewReportResponse>> reportReview(
            @CurrentUser UserContext currentUser,
            @PathVariable UUID reviewId,
            @Valid @RequestBody ReportReviewRequest request
    ) {
        ReviewReportResponse response = reviewReportService.reportReview(
                currentUser,
                reviewId,
                request
        );

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success("Review reported successfully", response));
    }
}