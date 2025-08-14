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
        try {
            User currentUser = securityUtilsService.getCurrentAuthenticatedUser();
            log.info("User {} subscribed to notifications", currentUser.getEmail());
        } catch (Exception e) {
            log.warn("Unauthorized user attempted to subscribe to notifications");
        }
    }

    /**
     * Handle ping messages for connection health check
     */
    @MessageMapping("/ping")
    @SendToUser("/queue/pong")
    public String handlePing() {
        return "pong";
    }

    /**
     * Handle general messages (if needed)
     */
    @MessageMapping("/message")
    @SendTo("/topic/messages")
    public String handleMessage(String message) {
        log.info("Received message: {}", message);
        return "Message received: " + message;
    }
}
