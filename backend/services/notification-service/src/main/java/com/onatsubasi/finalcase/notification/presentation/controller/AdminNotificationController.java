package com.onatsubasi.finalcase.notification.presentation.controller;

import com.onatsubasi.finalcase.common.core.response.ApiResponse;
import com.onatsubasi.finalcase.notification.application.dto.request.CreateDirectNotificationRequest;
import com.onatsubasi.finalcase.notification.application.dto.response.NotificationDetailResponse;
import com.onatsubasi.finalcase.notification.application.dto.response.NotificationSummaryResponse;
import com.onatsubasi.finalcase.notification.application.service.NotificationCommandService;
import com.onatsubasi.finalcase.notification.application.service.NotificationDeliveryService;
import com.onatsubasi.finalcase.notification.application.service.NotificationQueryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/admin/notifications")
@RequiredArgsConstructor
@Tag(
        name = "Notification Admin",
        description = "Admin notification inspection, direct notification and retry APIs"
)
public class AdminNotificationController {

    private final NotificationQueryService notificationQueryService;
    private final NotificationCommandService notificationCommandService;
    private final NotificationDeliveryService notificationDeliveryService;

    @Operation(
            summary = "List notifications",
            description = "Returns paginated notifications for admin monitoring.",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Notifications listed")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthorized")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Forbidden")
    @GetMapping
    public ResponseEntity<ApiResponse<Page<NotificationSummaryResponse>>> getAllNotifications(
            @Parameter(description = "Page index", example = "0")
            @RequestParam(defaultValue = "0") int page,

            @Parameter(description = "Page size", example = "20")
            @RequestParam(defaultValue = "20") int size
    ) {
        return ResponseEntity.ok(
                ApiResponse.success(
                        notificationQueryService.getAllNotifications(page, size)
                )
        );
    }

    @Operation(
            summary = "Get notification by id",
            description = "Returns notification detail for admin inspection.",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Notification found")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Notification not found")
    @GetMapping("/{notificationId}")
    public ResponseEntity<ApiResponse<NotificationDetailResponse>> getById(
            @Parameter(description = "Notification id", required = true)
            @PathVariable UUID notificationId
    ) {
        return ResponseEntity.ok(
                ApiResponse.success(
                        notificationQueryService.getByIdForAdmin(notificationId)
                )
        );
    }

    @Operation(
            summary = "Create direct notification",
            description = "Creates and sends a direct notification. Mostly for admin/system use.",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Notification created")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Invalid notification request")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Template not found")
    @PostMapping("/direct")
    public ResponseEntity<ApiResponse<NotificationDetailResponse>> createDirectNotification(
            @Valid @RequestBody CreateDirectNotificationRequest request
    ) {
        return ResponseEntity.ok(
                ApiResponse.success(
                        notificationCommandService.createDirectNotification(request)
                )
        );
    }

    @Operation(
            summary = "Retry notification delivery",
            description = "Manually retries a failed or retry-scheduled notification delivery.",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Delivery retry triggered")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Delivery not found")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "409", description = "Delivery is not retryable")
    @PostMapping("/deliveries/{deliveryId}/retry")
    public ResponseEntity<ApiResponse<Void>> retryDelivery(
            @Parameter(description = "Notification delivery id", required = true)
            @PathVariable UUID deliveryId
    ) {
        notificationDeliveryService.retryDelivery(deliveryId);

        return ResponseEntity.ok(ApiResponse.success(null));
    }
}