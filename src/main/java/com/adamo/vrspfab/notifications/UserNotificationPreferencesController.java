package com.adamo.vrspfab.notifications;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/user/preferences/notifications")
@RequiredArgsConstructor
@Tag(name = "User Preferences", description = "Endpoints for managing user notification settings")
public class UserNotificationPreferencesController {

    private final UserNotificationPreferencesService preferencesService;

    @Operation(summary = "Get current user's notification preferences",
               description = "Retrieves the notification preferences for the currently authenticated user.",
               responses = {
                       @ApiResponse(responseCode = "200", description = "Successfully retrieved preferences"),
                       @ApiResponse(responseCode = "401", description = "Unauthorized, authentication required"),
                       @ApiResponse(responseCode = "500", description = "Internal server error")
               })
    @GetMapping
    public ResponseEntity<UserNotificationPreferencesDto> getMyNotificationPreferences() {
        return ResponseEntity.ok(preferencesService.getPreferencesForCurrentUser());
    }

    @Operation(summary = "Update current user's notification preferences",
               description = "Updates the notification preferences for the currently authenticated user.",
               responses = {
                       @ApiResponse(responseCode = "200", description = "Preferences updated successfully"),
                       @ApiResponse(responseCode = "400", description = "Invalid preferences data"),
                       @ApiResponse(responseCode = "401", description = "Unauthorized, authentication required"),
                       @ApiResponse(responseCode = "500", description = "Internal server error")
               })
    @PutMapping
    public ResponseEntity<UserNotificationPreferencesDto> updateMyNotificationPreferences(@Valid @RequestBody UserNotificationPreferencesDto dto) {
        return ResponseEntity.ok(preferencesService.updatePreferencesForCurrentUser(dto));
    }
}