package com.onatsubasi.finalcase.notification.presentation.controller;

import com.onatsubasi.finalcase.common.core.response.ApiResponse;
import com.onatsubasi.finalcase.notification.application.dto.request.UpsertNotificationTemplateRequest;
import com.onatsubasi.finalcase.notification.application.dto.response.NotificationTemplateResponse;
import com.onatsubasi.finalcase.notification.application.service.NotificationTemplateService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/admin/notification-templates")
@RequiredArgsConstructor
@Tag(
        name = "Notification Template Admin",
        description = "Admin APIs for notification template management"
)
public class AdminNotificationTemplateController {

    private final NotificationTemplateService notificationTemplateService;

    @Operation(
            summary = "Create or update notification template",
            description = "Upserts a notification template by type, channel, and locale.",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Template upserted")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Invalid template request")
    @PostMapping
    public ResponseEntity<ApiResponse<NotificationTemplateResponse>> upsertTemplate(
            @Valid @RequestBody UpsertNotificationTemplateRequest request
    ) {
        return ResponseEntity.ok(
                ApiResponse.success(
                        notificationTemplateService.upsertTemplate(request)
                )
        );
    }

    @Operation(
            summary = "Activate notification template",
            description = "Activates a notification template.",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Template activated")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Template not found")
    @PatchMapping("/{templateId}/activate")
    public ResponseEntity<ApiResponse<NotificationTemplateResponse>> activateTemplate(
            @Parameter(description = "Template id", required = true)
            @PathVariable UUID templateId
    ) {
        return ResponseEntity.ok(
                ApiResponse.success(
                        notificationTemplateService.activateTemplate(templateId)
                )
        );
    }

    @Operation(
            summary = "Deactivate notification template",
            description = "Deactivates a notification template.",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Template deactivated")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Template not found")
    @PatchMapping("/{templateId}/deactivate")
    public ResponseEntity<ApiResponse<NotificationTemplateResponse>> deactivateTemplate(
            @Parameter(description = "Template id", required = true)
            @PathVariable UUID templateId
    ) {
        return ResponseEntity.ok(
                ApiResponse.success(
                        notificationTemplateService.deactivateTemplate(templateId)
                )
        );
    }
}