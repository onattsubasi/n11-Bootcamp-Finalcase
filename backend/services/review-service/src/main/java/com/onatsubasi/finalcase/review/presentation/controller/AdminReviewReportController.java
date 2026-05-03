package com.onatsubasi.finalcase.review.presentation.controller;

import com.onatsubasi.finalcase.common.core.response.ApiResponse;
import com.onatsubasi.finalcase.common.security.CurrentUser;
import com.onatsubasi.finalcase.common.security.UserContext;
import com.onatsubasi.finalcase.review.application.dto.request.ResolveReviewReportRequest;
import com.onatsubasi.finalcase.review.application.dto.response.PageResponse;
import com.onatsubasi.finalcase.review.application.dto.response.ReviewReportResponse;
import com.onatsubasi.finalcase.review.application.service.ReviewReportService;
import com.onatsubasi.finalcase.review.domain.enums.ReviewReportStatus;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/admin/review-reports")
@Tag(name = "Admin Review Reports", description = "Admin review report management")
public class AdminReviewReportController {

    private final ReviewReportService reviewReportService;

    @Operation(
            summary = "List review reports",
            description = "Lists review reports by optional status.",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @GetMapping
    public ResponseEntity<ApiResponse<PageResponse<ReviewReportResponse>>> listReports(
            @RequestParam(required = false) ReviewReportStatus status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        PageRequest pageable = PageRequest.of(
                Math.max(page, 0),
                Math.min(Math.max(size, 1), 100),
                Sort.by(Sort.Direction.DESC, "createdAt")
        );

        return ResponseEntity.ok(
                ApiResponse.success(reviewReportService.listReports(status, pageable))
        );
    }

    @Operation(
            summary = "Resolve review report",
            description = "Marks a review report as resolved.",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @PostMapping("/{reportId}/resolve")
    public ResponseEntity<ApiResponse<ReviewReportResponse>> resolveReport(
            @CurrentUser UserContext admin,
            @PathVariable UUID reportId,
            @Valid @RequestBody ResolveReviewReportRequest request
    ) {
        ReviewReportResponse response = reviewReportService.resolveReport(
                admin,
                reportId,
                request
        );

        return ResponseEntity.ok(
                ApiResponse.success("Review report resolved successfully", response)
        );
    }

    @Operation(
            summary = "Dismiss review report",
            description = "Marks a review report as dismissed.",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @PostMapping("/{reportId}/dismiss")
    public ResponseEntity<ApiResponse<ReviewReportResponse>> dismissReport(
            @CurrentUser UserContext admin,
            @PathVariable UUID reportId,
            @Valid @RequestBody ResolveReviewReportRequest request
    ) {
        ReviewReportResponse response = reviewReportService.dismissReport(
                admin,
                reportId,
                request
        );

        return ResponseEntity.ok(
                ApiResponse.success("Review report dismissed successfully", response)
        );
    }
}