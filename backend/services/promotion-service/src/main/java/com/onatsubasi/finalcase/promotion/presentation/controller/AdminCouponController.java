package com.onatsubasi.finalcase.promotion.presentation.controller;

import com.onatsubasi.finalcase.common.core.response.ApiResponse;
import com.onatsubasi.finalcase.common.core.response.ErrorResponse;
import com.onatsubasi.finalcase.promotion.application.dto.request.AssignCouponRequest;
import com.onatsubasi.finalcase.promotion.application.dto.request.CreateCouponBatchRequest;
import com.onatsubasi.finalcase.promotion.application.dto.request.CreateCouponRequest;
import com.onatsubasi.finalcase.promotion.application.dto.request.UpdateCouponRequest;
import com.onatsubasi.finalcase.promotion.application.dto.response.CouponAssignmentResponse;
import com.onatsubasi.finalcase.promotion.application.dto.response.CouponResponse;
import com.onatsubasi.finalcase.promotion.application.service.CouponAdminService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/admin/coupons")
@Tag(name = "Coupon Admin", description = "Admin coupon and coupon assignment management")
public class AdminCouponController {

    private final CouponAdminService couponAdminService;

    @Operation(
            summary = "Create coupon",
            description = "Creates a coupon for an existing promotion.",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @io.swagger.v3.oas.annotations.responses.ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "201",
                    description = "Coupon created successfully"
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "400",
                    description = "Invalid coupon request",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "409",
                    description = "Coupon already exists",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            )
    })
    @PostMapping
    public ResponseEntity<ApiResponse<CouponResponse>> createCoupon(
            @Valid @RequestBody CreateCouponRequest request
    ) {
        CouponResponse response = couponAdminService.createCoupon(request);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success("Coupon created successfully", response));
    }


    @Operation(
            summary = "Update coupon",
            description = "Updates coupon limits and date window. Current reserved/redeemed usage cannot exceed the new usage limit.",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @PutMapping("/{couponId}")
    public ResponseEntity<ApiResponse<CouponResponse>> updateCoupon(
            @Parameter(description = "Coupon id")
            @PathVariable UUID couponId,

            @Valid @RequestBody UpdateCouponRequest request
    ) {
        CouponResponse response = couponAdminService.updateCoupon(couponId, request);

        return ResponseEntity.ok(
                ApiResponse.success("Coupon updated successfully", response)
        );
    }

    @Operation(
            summary = "Activate coupon",
            description = "Activates an inactive coupon if it has not expired.",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @PostMapping("/{couponId}/activate")
    public ResponseEntity<ApiResponse<CouponResponse>> activateCoupon(
            @Parameter(description = "Coupon id")
            @PathVariable UUID couponId
    ) {
        CouponResponse response = couponAdminService.activateCoupon(couponId);

        return ResponseEntity.ok(
                ApiResponse.success("Coupon activated successfully", response)
        );
    }

    @Operation(
            summary = "Deactivate coupon",
            description = "Deactivates a coupon so it cannot be used for new quotes or reservations.",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @PostMapping("/{couponId}/deactivate")
    public ResponseEntity<ApiResponse<CouponResponse>> deactivateCoupon(
            @Parameter(description = "Coupon id")
            @PathVariable UUID couponId
    ) {
        CouponResponse response = couponAdminService.deactivateCoupon(couponId);

        return ResponseEntity.ok(
                ApiResponse.success("Coupon deactivated successfully", response)
        );
    }

    @Operation(
            summary = "Expire coupon",
            description = "Manually expires a coupon.",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @PostMapping("/{couponId}/expire")
    public ResponseEntity<ApiResponse<CouponResponse>> expireCoupon(
            @Parameter(description = "Coupon id")
            @PathVariable UUID couponId
    ) {
        CouponResponse response = couponAdminService.expireCoupon(couponId);

        return ResponseEntity.ok(
                ApiResponse.success("Coupon expired successfully", response)
        );
    }

    @Operation(
            summary = "Create coupon batch",
            description = "Generates a batch of coupons for an existing promotion.",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @PostMapping("/batch")
    public ResponseEntity<ApiResponse<List<CouponResponse>>> createCouponBatch(
            @Valid @RequestBody CreateCouponBatchRequest request
    ) {
        List<CouponResponse> response = couponAdminService.createCouponBatch(request);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success("Coupon batch created successfully", response));
    }

    @Operation(
            summary = "Assign coupon to user",
            description = "Assigns a coupon to a specific user. If assignment already exists, returns existing assignment.",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @PostMapping("/assignments")
    public ResponseEntity<ApiResponse<CouponAssignmentResponse>> assignCoupon(
            @Valid @RequestBody AssignCouponRequest request
    ) {
        CouponAssignmentResponse response = couponAdminService.assignCoupon(request);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success("Coupon assigned successfully", response));
    }

    @Operation(
            summary = "List coupons by promotion",
            description = "Lists coupons attached to a promotion.",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @GetMapping("/promotion/{promotionId}")
    public ResponseEntity<ApiResponse<List<CouponResponse>>> listCouponsByPromotion(
            @Parameter(description = "Promotion id")
            @PathVariable UUID promotionId
    ) {
        return ResponseEntity.ok(
                ApiResponse.success(couponAdminService.listCouponsByPromotion(promotionId))
        );
    }
}