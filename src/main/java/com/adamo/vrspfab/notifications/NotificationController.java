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

import java.time.LocalDateTime;
import java.util.Map;

@RestController
@RequestMapping("/notifications")
@RequiredArgsConstructor
@Tag(name = "Notifications", description = "Endpoints for managing user notifications")
@SecurityRequirement(name = "bearerAuth")
public class NotificationController {

    private final NotificationService notificationService;

    @Operation(summary = "Get notifications for the current user",
               description = "Returns a paginated list of notifications for the authenticated user.",
               responses = {
                       @ApiResponse(responseCode = "200", description = "Successfully retrieved notifications"),
                       @ApiResponse(responseCode = "401", description = "Unauthorized, authentication required"),
                       @ApiResponse(responseCode = "500", description = "Internal server error")
               })
    @GetMapping
    public Page<NotificationDto> getNotificationsForCurrentUser(Pageable pageable) {
        return notificationService.getNotificationsForCurrentUser(pageable);
    }

    @Operation(summary = "Mark a notification as read",
               description = "Marks a single notification as read by its ID.",
               responses = {
                       @ApiResponse(responseCode = "200", description = "Notification marked as read successfully"),
                       @ApiResponse(responseCode = "401", description = "Unauthorized, authentication required"),
                       @ApiResponse(responseCode = "403", description = "Forbidden, user does not own this notification"),
                       @ApiResponse(responseCode = "404", description = "Notification not found"),
                       @ApiResponse(responseCode = "500", description = "Internal server error")
               })
    @PatchMapping("/{id}/read")
    public ResponseEntity<NotificationDto> markNotificationAsRead(@PathVariable Long id) {
        return ResponseEntity.ok(notificationService.markNotificationAsRead(id));
    }

    @Operation(summary = "Mark all notifications as read",
               description = "Marks all unread notifications for the current user as read.",
               responses = {
                       @ApiResponse(responseCode = "200", description = "All notifications marked as read successfully"),
                       @ApiResponse(responseCode = "401", description = "Unauthorized, authentication required"),
                       @ApiResponse(responseCode = "500", description = "Internal server error")
               })
    @PostMapping("/mark-all-as-read")
    public ResponseEntity<Map<String, String>> markAllNotificationsAsRead() {
        long count = notificationService.markAllNotificationsAsRead();
        return ResponseEntity.ok(Map.of("message", "Successfully marked " + count + " notifications as read."));
    }

    @Operation(summary = "Delete a notification",
               description = "Deletes a single notification by its ID.",
               responses = {
                       @ApiResponse(responseCode = "204", description = "Notification deleted successfully"),
                       @ApiResponse(responseCode = "401", description = "Unauthorized, authentication required"),
                       @ApiResponse(responseCode = "403", description = "Forbidden, user does not own this notification"),
                       @ApiResponse(responseCode = "404", description = "Notification not found"),
                       @ApiResponse(responseCode = "500", description = "Internal server error")
               })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteNotification(@PathVariable Long id) {
        notificationService.deleteNotification(id);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "[ADMIN] Broadcast a notification",
               description = "Sends a notification to all users in the system. Requires ADMIN role.",
               responses = {
                       @ApiResponse(responseCode = "200", description = "Broadcast initiated successfully"),
                       @ApiResponse(responseCode = "400", description = "Invalid notification request"),
                       @ApiResponse(responseCode = "401", description = "Unauthorized"),
                       @ApiResponse(responseCode = "403", description = "Forbidden, insufficient privileges (requires ADMIN role)"),
                       @ApiResponse(responseCode = "500", description = "Internal server error")
               })
    @PostMapping("/broadcast")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, String>> broadcastNotification(@Valid @RequestBody AdminNotificationRequest request) {
        notificationService.broadcastNotification(request);
        return ResponseEntity.ok(Map.of("message", "Broadcast initiated successfully."));
    }

    @Operation(summary = "Test real-time notification",
               description = "Sends a test notification to the current user for testing WebSocket functionality.",
               responses = {
                       @ApiResponse(responseCode = "200", description = "Test notification sent successfully"),
                       @ApiResponse(responseCode = "401", description = "Unauthorized, authentication required"),
                       @ApiResponse(responseCode = "500", description = "Internal server error")
               })
    @PostMapping("/test")
    public ResponseEntity<Map<String, String>> testRealTimeNotification() {
        notificationService.createAndDispatchNotification(
            notificationService.getCurrentUser(),
            NotificationType.GENERAL_UPDATE,
            "This is a test real-time notification! 🚀"
        );
        return ResponseEntity.ok(Map.of("message", "Test notification sent successfully."));
    }

    @Operation(summary = "[ADMIN] Get all notifications",
            description = "Retrieves a paginated and filtered list of all notifications in the system. Requires ADMIN role.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Successfully retrieved notifications"),
                    @ApiResponse(responseCode = "401", description = "Unauthorized"),
                    @ApiResponse(responseCode = "403", description = "Forbidden, insufficient privileges (requires ADMIN role)"),
                    @ApiResponse(responseCode = "500", description = "Internal server error")
            })
    @GetMapping("/admin")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Page<NotificationDto>> getAllNotificationsAdmin(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "DESC") String sortDirection,
            @RequestParam(required = false) Long userId,
            @RequestParam(required = false) Boolean read,
            @RequestParam(required = false) NotificationType type,
            @RequestParam(required = false) @org.springframework.format.annotation.DateTimeFormat(iso = org.springframework.format.annotation.DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam(required = false) @org.springframework.format.annotation.DateTimeFormat(iso = org.springframework.format.annotation.DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate
    ) {
        Pageable pageable = org.springframework.data.domain.PageRequest.of(page, size, org.springframework.data.domain.Sort.by(org.springframework.data.domain.Sort.Direction.fromString(sortDirection), sortBy));
        NotificationFilter filter = NotificationFilter.builder()
                .userId(userId)
                .read(read)
                .type(type)
                .startDate(startDate)
                .endDate(endDate)
                .build();
        return ResponseEntity.ok(notificationService.getAllNotificationsAdmin(filter, pageable));
    }
}