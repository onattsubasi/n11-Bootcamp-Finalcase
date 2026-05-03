package com.onatsubasi.finalcase.promotion.presentation.controller;

import com.onatsubasi.finalcase.common.core.response.ApiResponse;
import com.onatsubasi.finalcase.common.security.CurrentUser;
import com.onatsubasi.finalcase.common.security.UserContext;
import com.onatsubasi.finalcase.promotion.application.dto.response.CouponAssignmentResponse;
import com.onatsubasi.finalcase.promotion.application.service.CustomerCouponService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/customer/coupons")
@Tag(name = "Customer Coupons", description = "Customer coupon listing")
public class CustomerCouponController {

    private final CustomerCouponService customerCouponService;

    @Operation(
            summary = "List assigned coupons",
            description = "Lists active assigned coupons for the current authenticated customer.",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @GetMapping
    public ResponseEntity<ApiResponse<List<CouponAssignmentResponse>>> listAssignedCoupons(
            @CurrentUser UserContext userContext
    ) {
        return ResponseEntity.ok(
                ApiResponse.success(customerCouponService.listAssignedCoupons(userContext))
        );
    }
}