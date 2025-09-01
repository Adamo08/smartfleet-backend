package com.adamo.vrspfab.users.events;

import com.adamo.vrspfab.notifications.NotificationService;
import com.adamo.vrspfab.notifications.NotificationType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class UserEventListener {
    
    private final NotificationService notificationService;

    @EventListener
    public void handleUserRegistered(UserRegisteredEvent event) {
        try {
            notificationService.createAndDispatchNotification(
                    event.getUser(),
                    NotificationType.ACCOUNT_VERIFICATION,
                    "Welcome to SmartFleet! Your account has been successfully created.",
                    java.util.Map.of(
                            "username", event.getUser().getFirstName(),
                            "email", event.getUser().getEmail(),
                            "role", event.getUser().getRole().getName()
                    )
            );
        } catch (Exception e) {
            log.warn("Failed to send welcome notification for user: {}", event.getUser().getEmail(), e);
        }
    }

    @EventListener
    public void handleUserRoleChanged(UserRoleChangedEvent event) {
        try {
            notificationService.createAndDispatchNotification(
                    event.getUser(),
                    NotificationType.SYSTEM_ALERT,
                    "Your account role has been updated from " + event.getOldRole().getName() + " to " + event.getNewRole().getName() + ".",
                    java.util.Map.of(
                            "oldRole", event.getOldRole().getName(),
                            "newRole", event.getNewRole().getName(),
                            "username", event.getUser().getFirstName()
                    )
            );
        } catch (Exception e) {
            log.warn("Failed to send role change notification for user: {}", event.getUser().getEmail(), e);
        }
    }
}
