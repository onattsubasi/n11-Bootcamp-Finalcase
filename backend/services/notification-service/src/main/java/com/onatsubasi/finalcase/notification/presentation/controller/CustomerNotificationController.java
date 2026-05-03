package com.onatsubasi.finalcase.notification.presentation.controller;

import com.onatsubasi.finalcase.common.core.response.ApiResponse;
import com.onatsubasi.finalcase.common.security.CurrentUser;
import com.onatsubasi.finalcase.common.security.UserContext;
import com.onatsubasi.finalcase.notification.application.dto.response.MarkAllNotificationsReadResponse;
import com.onatsubasi.finalcase.notification.application.dto.response.NotificationDetailResponse;
import com.onatsubasi.finalcase.notification.application.dto.response.NotificationReadResponse;
import com.onatsubasi.finalcase.notification.application.dto.response.NotificationSummaryResponse;
import com.onatsubasi.finalcase.notification.application.dto.response.UnreadNotificationCountResponse;
import com.onatsubasi.finalcase.notification.application.service.NotificationQueryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/customer/notifications")
@RequiredArgsConstructor
@Tag(
        name = "Notification",
        description = "Customer notification inbox APIs"
)
public class CustomerNotificationController {

    private final NotificationQueryService notificationQueryService;

    @Operation(
            summary = "List my notifications",
            description = "Returns paginated customer notifications.",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Notifications listed")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthorized")
    @GetMapping
    public ResponseEntity<ApiResponse<Page<NotificationSummaryResponse>>> getMyNotifications(
            @CurrentUser UserContext user,

            @Parameter(description = "Only unread notifications", example = "false")
            @RequestParam(defaultValue = "false") boolean unreadOnly,

            @Parameter(description = "Page index", example = "0")
            @RequestParam(defaultValue = "0") int page,

            @Parameter(description = "Page size", example = "20")
            @RequestParam(defaultValue = "20") int size
    ) {
        Page<NotificationSummaryResponse> response = notificationQueryService.getMyNotifications(
                user.userId(),
                unreadOnly,
                page,
                size
        );

        return ResponseEntity.ok(
                ApiResponse.success("Notifications listed", response)
        );
    }

    @Operation(
            summary = "Get unread notification count",
            description = "Returns unread notification count for the authenticated customer.",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Unread count returned")
    @GetMapping("/unread-count")
    public ResponseEntity<ApiResponse<UnreadNotificationCountResponse>> getUnreadCount(
            @CurrentUser UserContext user
    ) {
        UnreadNotificationCountResponse response = notificationQueryService.getUnreadCount(user.userId());

        return ResponseEntity.ok(
                ApiResponse.success("Unread count returned", response)
        );
    }

    @Operation(
            summary = "Get my notification by id",
            description = "Returns notification detail if it belongs to the authenticated customer.",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Notification found")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Notification not found")
    @GetMapping("/{notificationId}")
    public ResponseEntity<ApiResponse<NotificationDetailResponse>> getById(
            @CurrentUser UserContext user,

            @Parameter(description = "Notification id", required = true)
            @PathVariable UUID notificationId
    ) {
        NotificationDetailResponse response = notificationQueryService.getMyNotification(
                notificationId,
                user.userId()
        );

        return ResponseEntity.ok(
                ApiResponse.success("Notification found", response)
        );
    }

    @Operation(
            summary = "Mark notification as read",
            description = "Marks a customer notification as read.",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Notification marked as read")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Notification access denied")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Notification not found")
    @PatchMapping("/{notificationId}/read")
    public ResponseEntity<ApiResponse<NotificationReadResponse>> markRead(
            @CurrentUser UserContext user,

            @Parameter(description = "Notification id", required = true)
            @PathVariable UUID notificationId
    ) {
        NotificationReadResponse response = notificationQueryService.markRead(
                notificationId,
                user.userId()
        );

        return ResponseEntity.ok(
                ApiResponse.success("Notification marked as read", response)
        );
    }

    @Operation(
            summary = "Mark all my notifications as read",
            description = "Marks all unread notifications owned by the authenticated customer as read.",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Notifications marked as read")
    @PatchMapping("/read-all")
    public ResponseEntity<ApiResponse<MarkAllNotificationsReadResponse>> markAllRead(
            @CurrentUser UserContext user
    ) {
        MarkAllNotificationsReadResponse response = notificationQueryService.markAllRead(user.userId());

        return ResponseEntity.ok(
                ApiResponse.success("Notifications marked as read", response)
        );
    }
}
