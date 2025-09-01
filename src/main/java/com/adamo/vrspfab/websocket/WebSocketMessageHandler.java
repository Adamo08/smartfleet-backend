package com.adamo.vrspfab.websocket;

import com.adamo.vrspfab.common.SecurityUtilsService;
import com.adamo.vrspfab.users.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.annotation.SendToUser;
import org.springframework.messaging.simp.annotation.SubscribeMapping;
import org.springframework.stereotype.Controller;

@Controller
@RequiredArgsConstructor
@Slf4j
public class WebSocketMessageHandler {

    private final SecurityUtilsService securityUtilsService;

    /**
     * Handle user subscription to notifications
     */
    @SubscribeMapping("/user/queue/notifications")
    public void handleUserSubscription() {
        log.info("=== WebSocket Message Handler: User Subscription Attempt ===");
        try {
            User currentUser = securityUtilsService.getCurrentAuthenticatedUser();
            log.info("✅ User subscription successful: {} ({})", currentUser.getEmail(), currentUser.getId());
            log.info("✅ User role: {}", currentUser.getRole());
            log.info("✅ User notification preferences: realTime={}, email={}", 
                    currentUser.getNotificationPreferences() != null ? currentUser.getNotificationPreferences().isRealTimeEnabled() : "null",
                    currentUser.getNotificationPreferences() != null ? currentUser.getNotificationPreferences().isEmailEnabled() : "null");
        } catch (Exception e) {
            log.error("❌ User subscription failed: {}", e.getMessage(), e);
            log.error("❌ Stack trace: ", e);
        }
    }

    /**
     * Handle ping messages for connection health check
     */
    @MessageMapping("/ping")
    @SendToUser("/queue/pong")
    public String handlePing() {
        log.info("=== WebSocket Message Handler: Ping Received ===");
        try {
            User currentUser = securityUtilsService.getCurrentAuthenticatedUser();
            log.info("✅ Ping from authenticated user: {} ({})", currentUser.getEmail(), currentUser.getId());
        } catch (Exception e) {
            log.error("❌ Ping from unauthenticated user: {}", e.getMessage());
        }
        return "pong";
    }

    /**
     * Handle general messages (if needed)
     */
    @MessageMapping("/message")
    @SendTo("/topic/messages")
    public String handleMessage(String message) {
        log.info("=== WebSocket Message Handler: General Message ===");
        log.info("Received message: {}", message);
        try {
            User currentUser = securityUtilsService.getCurrentAuthenticatedUser();
            log.info("✅ Message from authenticated user: {} ({})", currentUser.getEmail(), currentUser.getId());
        } catch (Exception e) {
            log.error("❌ Message from unauthenticated user: {}", e.getMessage());
        }
        return "Message received: " + message;
    }
}
