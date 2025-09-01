package com.adamo.vrspfab.websocket;

import com.adamo.vrspfab.common.SecurityUtilsService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class WebSocketChannelInterceptor implements ChannelInterceptor {

    private final SecurityUtilsService securityUtilsService;

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);
        
        if (accessor != null) {
            StompCommand command = accessor.getCommand();
            String destination = accessor.getDestination();
            String sessionId = accessor.getSessionId();
            
            log.info("=== WebSocket Channel Interceptor Debug ===");
            log.info("Command: {}", command);
            log.info("Destination: {}", destination);
            log.info("Session ID: {}", sessionId);
            log.info("Headers: {}", accessor.toNativeHeaderMap());
            
            if (StompCommand.SUBSCRIBE.equals(command)) {
                log.info("Processing SUBSCRIBE command to destination: {}", destination);
                
                // Check SecurityContext
                try {
                    var auth = SecurityContextHolder.getContext().getAuthentication();
                    log.info("SecurityContext Authentication: {}", auth);
                    if (auth != null) {
                        log.info("Authentication Principal: {} (Type: {})", 
                                auth.getPrincipal(), 
                                auth.getPrincipal() != null ? auth.getPrincipal().getClass().getName() : "null");
                        log.info("Authentication Authorities: {}", auth.getAuthorities());
                        log.info("Authentication isAuthenticated: {}", auth.isAuthenticated());
                    } else {
                        log.warn("SecurityContext Authentication is NULL");
                    }
                } catch (Exception e) {
                    log.error("Error accessing SecurityContext: {}", e.getMessage(), e);
                }

                // Allow subscription to user-specific notification queue
                if (destination != null && destination.startsWith("/user/") && destination.endsWith("/queue/notifications")) {
                    log.info("Processing notification subscription to: {}", destination);
                    
                    try {
                        // Verify user is authenticated
                        var currentUser = securityUtilsService.getCurrentAuthenticatedUser();
                        log.info("✅ User authenticated successfully: {} ({})", currentUser.getEmail(), currentUser.getId());
                        log.info("✅ Allowing subscription to: {}", destination);
                        return message;
                    } catch (Exception e) {
                        log.error("❌ Authentication failed for subscription to: {}", destination);
                        log.error("❌ Error details: {}", e.getMessage(), e);
                        log.warn("❌ BLOCKING subscription attempt to: {}", destination);
                        return null; // Block the subscription
                    }
                } else {
                    log.info("Destination {} doesn't match notification pattern, allowing", destination);
                }
            } else if (StompCommand.CONNECT.equals(command)) {
                log.info("Processing CONNECT command");
                log.info("Connect headers: {}", accessor.toNativeHeaderMap());
            } else if (StompCommand.SEND.equals(command)) {
                log.info("Processing SEND command to: {}", destination);
            }
        } else {
            log.warn("No StompHeaderAccessor found in message");
        }
        
        return message;
    }
}
