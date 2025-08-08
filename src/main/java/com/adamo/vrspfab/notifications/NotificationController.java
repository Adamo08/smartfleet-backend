package com.adamo.vrspfab.notifications;


import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/notifications")
@RequiredArgsConstructor
@Tag(name = "Notifications", description = "Endpoints for managing user notifications")
@SecurityRequirement(name = "bearerAuth")
public class NotificationController {

    private final NotificationService notificationService;

    @Operation(summary = "Get notifications for the current user", description = "Returns a paginated list of notifications for the authenticated user.")
    @GetMapping
    public Page<NotificationDto> getNotificationsForCurrentUser(Pageable pageable) {
        return notificationService.getNotificationsForCurrentUser(pageable);
    }

    @Operation(summary = "Mark a notification as read", description = "Marks a single notification as read by its ID.")
    @ApiResponse(responseCode = "200", description = "Notification marked as read successfully")
    @ApiResponse(responseCode = "404", description = "Notification not found")
    @PatchMapping("/{id}/read")
    public ResponseEntity<NotificationDto> markNotificationAsRead(@PathVariable Long id) {
        return ResponseEntity.ok(notificationService.markNotificationAsRead(id));
    }

    @Operation(summary = "Mark all notifications as read", description = "Marks all unread notifications for the current user as read.")
    @PostMapping("/mark-all-as-read")
    public ResponseEntity<Map<String, String>> markAllNotificationsAsRead() {
        long count = notificationService.markAllNotificationsAsRead();
        return ResponseEntity.ok(Map.of("message", "Successfully marked " + count + " notifications as read."));
    }

    @Operation(summary = "Delete a notification", description = "Deletes a single notification by its ID.")
    @ApiResponse(responseCode = "204", description = "Notification deleted successfully")
    @ApiResponse(responseCode = "404", description = "Notification not found")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteNotification(@PathVariable Long id) {
        notificationService.deleteNotification(id);
        return ResponseEntity.noContent().build();
    }


    @PostMapping("/broadcast")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "[ADMIN] Broadcast a notification", description = "Sends a notification to all users in the system.")
    public ResponseEntity<Map<String, String>> broadcastNotification(@Valid @RequestBody AdminNotificationRequest request) {
        notificationService.broadcastNotification(request);
        return ResponseEntity.ok(Map.of("message", "Broadcast initiated successfully."));
    }
}