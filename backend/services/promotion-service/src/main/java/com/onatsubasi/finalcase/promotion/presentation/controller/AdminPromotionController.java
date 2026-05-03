package com.onatsubasi.finalcase.promotion.presentation.controller;

import com.onatsubasi.finalcase.common.core.response.ApiResponse;
import com.onatsubasi.finalcase.common.core.response.ErrorResponse;
import com.onatsubasi.finalcase.promotion.application.dto.request.CreatePromotionRequest;
import com.onatsubasi.finalcase.promotion.application.dto.request.UpdatePromotionRequest;
import com.onatsubasi.finalcase.promotion.application.dto.response.PromotionResponse;
import com.onatsubasi.finalcase.promotion.application.service.PromotionAdminService;
import com.onatsubasi.finalcase.promotion.domain.enums.PromotionStatus;
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
@RequestMapping("/api/admin/promotions")
@Tag(name = "Promotion Admin", description = "Admin promotion lifecycle and configuration management")
public class AdminPromotionController {

    private final PromotionAdminService promotionAdminService;

    @Operation(
            summary = "Create promotion",
            description = "Creates a promotion in DRAFT status. Discount calculation config is validated by Strategy + Factory.",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @io.swagger.v3.oas.annotations.responses.ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "201",
                    description = "Promotion created successfully"
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "400",
                    description = "Invalid promotion request",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "401",
                    description = "Unauthorized"
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "403",
                    description = "Forbidden"
            )
    })
    @PostMapping
    public ResponseEntity<ApiResponse<PromotionResponse>> create(
            @Valid @RequestBody CreatePromotionRequest request
    ) {
        PromotionResponse response = promotionAdminService.create(request);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success("Promotion created successfully", response));
    }

    @Operation(
            summary = "Update promotion",
            description = "Updates editable promotion fields and validates rule config against its strategy.",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @PutMapping("/{promotionId}")
    public ResponseEntity<ApiResponse<PromotionResponse>> update(
            @Parameter(description = "Promotion id")
            @PathVariable UUID promotionId,

            @Valid @RequestBody UpdatePromotionRequest request
    ) {
        PromotionResponse response = promotionAdminService.update(promotionId, request);

        return ResponseEntity.ok(
                ApiResponse.success("Promotion updated successfully", response)
        );
    }

    @Operation(
            summary = "Activate promotion",
            description = "Activates a promotion if its dates and rule config are valid.",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @PostMapping("/{promotionId}/activate")
    public ResponseEntity<ApiResponse<PromotionResponse>> activate(
            @Parameter(description = "Promotion id")
            @PathVariable UUID promotionId
    ) {
        PromotionResponse response = promotionAdminService.activate(promotionId);

        return ResponseEntity.ok(
                ApiResponse.success("Promotion activated successfully", response)
        );
    }

    @Operation(
            summary = "Pause promotion",
            description = "Pauses an active promotion.",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @PostMapping("/{promotionId}/pause")
    public ResponseEntity<ApiResponse<PromotionResponse>> pause(
            @Parameter(description = "Promotion id")
            @PathVariable UUID promotionId
    ) {
        PromotionResponse response = promotionAdminService.pause(promotionId);

        return ResponseEntity.ok(
                ApiResponse.success("Promotion paused successfully", response)
        );
    }

    @Operation(
            summary = "Expire promotion",
            description = "Manually expires a promotion.",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @PostMapping("/{promotionId}/expire")
    public ResponseEntity<ApiResponse<PromotionResponse>> expire(
            @Parameter(description = "Promotion id")
            @PathVariable UUID promotionId
    ) {
        PromotionResponse response = promotionAdminService.expire(promotionId);

        return ResponseEntity.ok(
                ApiResponse.success("Promotion expired successfully", response)
        );
    }

    @Operation(
            summary = "Delete promotion",
            description = "Soft-deletes a promotion.",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @DeleteMapping("/{promotionId}")
    public ResponseEntity<ApiResponse<Void>> delete(
            @Parameter(description = "Promotion id")
            @PathVariable UUID promotionId
    ) {
        promotionAdminService.delete(promotionId);

        return ResponseEntity.ok(
                ApiResponse.success("Promotion deleted successfully")
        );
    }

    @Operation(
            summary = "Get promotion by id",
            description = "Returns promotion details by id.",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @GetMapping("/{promotionId}")
    public ResponseEntity<ApiResponse<PromotionResponse>> getById(
            @Parameter(description = "Promotion id")
            @PathVariable UUID promotionId
    ) {
        return ResponseEntity.ok(
                ApiResponse.success(promotionAdminService.getById(promotionId))
        );
    }

    @Operation(
            summary = "List promotions",
            description = "Lists promotions by optional status. Returns all promotions when status is omitted.",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @GetMapping
    public ResponseEntity<ApiResponse<List<PromotionResponse>>> list(
            @Parameter(description = "Optional promotion status filter")
            @RequestParam(required = false) PromotionStatus status
    ) {
        return ResponseEntity.ok(
                ApiResponse.success(promotionAdminService.list(status))
        );
    }
}