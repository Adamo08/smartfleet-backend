package com.adamo.vrspfab.notifications;

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

    @GetMapping
    public ResponseEntity<UserNotificationPreferencesDto> getMyNotificationPreferences() {
        return ResponseEntity.ok(preferencesService.getPreferencesForCurrentUser());
    }

    @PutMapping
    public ResponseEntity<UserNotificationPreferencesDto> updateMyNotificationPreferences(@Valid @RequestBody UserNotificationPreferencesDto dto) {
        return ResponseEntity.ok(preferencesService.updatePreferencesForCurrentUser(dto));
    }
}