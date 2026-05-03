package com.onatsubasi.finalcase.review.presentation.controller;

import com.onatsubasi.finalcase.common.core.response.ApiResponse;
import com.onatsubasi.finalcase.common.security.CurrentUser;
import com.onatsubasi.finalcase.common.security.UserContext;
import com.onatsubasi.finalcase.review.application.dto.request.ApproveReviewRequest;
import com.onatsubasi.finalcase.review.application.dto.request.HideReviewRequest;
import com.onatsubasi.finalcase.review.application.dto.request.RejectReviewRequest;
import com.onatsubasi.finalcase.review.application.dto.request.RestoreReviewRequest;
import com.onatsubasi.finalcase.review.application.dto.response.AdminReviewDetailResponse;
import com.onatsubasi.finalcase.review.application.dto.response.PageResponse;
import com.onatsubasi.finalcase.review.application.service.ReviewModerationService;
import com.onatsubasi.finalcase.review.application.service.ReviewQueryService;
import com.onatsubasi.finalcase.review.domain.enums.ReviewStatus;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Size;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/admin/reviews")
@Tag(name = "Admin Reviews", description = "Admin review moderation operations")
public class AdminReviewController {

    private final ReviewQueryService reviewQueryService;
    private final ReviewModerationService reviewModerationService;

    @Operation(
            summary = "List reviews for moderation",
            description = "Lists reviews by optional moderation status.",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @GetMapping
    public ResponseEntity<ApiResponse<PageResponse<AdminReviewDetailResponse>>> listReviews(
            @RequestParam(required = false) ReviewStatus status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        return ResponseEntity.ok(
                ApiResponse.success(reviewQueryService.listAdminReviews(status, page, size))
        );
    }

    @Operation(
            summary = "Get review detail",
            description = "Returns full review moderation detail.",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @GetMapping("/{reviewId}")
    public ResponseEntity<ApiResponse<AdminReviewDetailResponse>> getReview(
            @PathVariable UUID reviewId
    ) {
        return ResponseEntity.ok(
                ApiResponse.success(reviewQueryService.getAdminReview(reviewId))
        );
    }

    @Operation(
            summary = "Approve review",
            description = "Approves a review and makes it public.",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @PostMapping("/{reviewId}/approve")
    public ResponseEntity<ApiResponse<AdminReviewDetailResponse>> approveReview(
            @CurrentUser UserContext admin,
            @PathVariable UUID reviewId,
            @Valid @RequestBody ApproveReviewRequest request
    ) {
        AdminReviewDetailResponse response = reviewModerationService.approveReview(
                admin,
                reviewId,
                request
        );

        return ResponseEntity.ok(
                ApiResponse.success("Review approved successfully", response)
        );
    }

    @Operation(
            summary = "Reject review",
            description = "Rejects a review and keeps it non-public.",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @PostMapping("/{reviewId}/reject")
    public ResponseEntity<ApiResponse<AdminReviewDetailResponse>> rejectReview(
            @CurrentUser UserContext admin,
            @PathVariable UUID reviewId,
            @Valid @RequestBody RejectReviewRequest request
    ) {
        AdminReviewDetailResponse response = reviewModerationService.rejectReview(
                admin,
                reviewId,
                request
        );

        return ResponseEntity.ok(
                ApiResponse.success("Review rejected successfully", response)
        );
    }

    @Operation(
            summary = "Hide review",
            description = "Hides an approved review from public listing.",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @PostMapping("/{reviewId}/hide")
    public ResponseEntity<ApiResponse<AdminReviewDetailResponse>> hideReview(
            @CurrentUser UserContext admin,
            @PathVariable UUID reviewId,
            @Valid @RequestBody HideReviewRequest request
    ) {
        AdminReviewDetailResponse response = reviewModerationService.hideReview(
                admin,
                reviewId,
                request
        );

        return ResponseEntity.ok(
                ApiResponse.success("Review hidden successfully", response)
        );
    }

    @Operation(
            summary = "Restore hidden review",
            description = "Restores a hidden review back to approved public state.",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @PostMapping("/{reviewId}/restore")
    public ResponseEntity<ApiResponse<AdminReviewDetailResponse>> restoreReview(
            @CurrentUser UserContext admin,
            @PathVariable UUID reviewId,
            @Valid @RequestBody RestoreReviewRequest request
    ) {
        AdminReviewDetailResponse response = reviewModerationService.restoreReview(
                admin,
                reviewId,
                request
        );

        return ResponseEntity.ok(
                ApiResponse.success("Review restored successfully", response)
        );
    }

    @Operation(
            summary = "Delete review as admin",
            description = "Soft-deletes a review as admin.",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @DeleteMapping("/{reviewId}")
    public ResponseEntity<ApiResponse<Void>> deleteReviewAsAdmin(
            @CurrentUser UserContext admin,
            @PathVariable UUID reviewId,
            @RequestParam(required = false)
            @Size(max = 500)
            String note
    ) {
        reviewModerationService.deleteReviewAsAdmin(admin, reviewId, note);

        return ResponseEntity.ok(
                ApiResponse.success("Review deleted successfully")
        );
    }
}