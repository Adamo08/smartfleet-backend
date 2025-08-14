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

            if (StompCommand.SUBSCRIBE.equals(command)) {
                String destination = accessor.getDestination();
                log.debug("WebSocket subscription attempt to: {}", destination);

                // Allow subscription to user-specific notification queue
                if (destination != null && destination.startsWith("/user/") && destination.endsWith("/queue/notifications")) {
                    try {
                        // Verify user is authenticated
                        securityUtilsService.getCurrentAuthenticatedUser();
                        log.debug("User authenticated for notification subscription to: {}", destination);
                        return message;
                    } catch (Exception e) {
                        log.warn("Unauthorized subscription attempt to: {}", destination);
                        return null; // Block the subscription
                    }
                }
            }
        }

        return message;
    }
}
