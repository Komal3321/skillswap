package com.skillswap.security.websocket;

import com.skillswap.security.user.CustomUserDetails;
import com.skillswap.service.chat.ChatService;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.SessionConnectEvent;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;

/**
 * Publishes online/offline status events for WebSocket connections.
 */
@Component
public class WebSocketPresenceEventListener {

    private final ChatService chatService;

    public WebSocketPresenceEventListener(ChatService chatService) {
        this.chatService = chatService;
    }

    /**
     * Marks a user online after STOMP connect.
     *
     * @param event connect event
     */
    @EventListener
    public void handleConnect(SessionConnectEvent event) {
        StompHeaderAccessor accessor = StompHeaderAccessor.wrap(event.getMessage());
        if (accessor.getUser() instanceof org.springframework.security.core.Authentication authentication
                && authentication.getPrincipal() instanceof CustomUserDetails principal) {
            chatService.markOnline(principal.getId());
        }
    }

    /**
     * Marks a user offline after STOMP disconnect.
     *
     * @param event disconnect event
     */
    @EventListener
    public void handleDisconnect(SessionDisconnectEvent event) {
        StompHeaderAccessor accessor = StompHeaderAccessor.wrap(event.getMessage());
        if (accessor.getUser() instanceof org.springframework.security.core.Authentication authentication
                && authentication.getPrincipal() instanceof CustomUserDetails principal) {
            chatService.markOffline(principal.getId());
        }
    }
}
