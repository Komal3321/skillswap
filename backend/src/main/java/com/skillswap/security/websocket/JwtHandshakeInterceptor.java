package com.skillswap.security.websocket;

import java.net.URI;
import java.util.Map;

import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.HandshakeInterceptor;

/**
 * Captures WebSocket handshake token metadata for SockJS clients.
 */
@Component
public class JwtHandshakeInterceptor implements HandshakeInterceptor {

    static final String ACCESS_TOKEN_ATTRIBUTE = "access_token";

    @Override
    public boolean beforeHandshake(
            ServerHttpRequest request,
            ServerHttpResponse response,
            WebSocketHandler wsHandler,
            Map<String, Object> attributes) {
        String token = extractQueryToken(request.getURI());
        if (StringUtils.hasText(token)) {
            attributes.put(ACCESS_TOKEN_ATTRIBUTE, token);
        }
        return true;
    }

    @Override
    public void afterHandshake(
            ServerHttpRequest request,
            ServerHttpResponse response,
            WebSocketHandler wsHandler,
            Exception exception) {
        // STOMP CONNECT authentication is handled by JwtStompChannelInterceptor.
    }

    private String extractQueryToken(URI uri) {
        String query = uri.getQuery();
        if (!StringUtils.hasText(query)) {
            return null;
        }
        for (String part : query.split("&")) {
            String[] pair = part.split("=", 2);
            if (pair.length == 2 && ACCESS_TOKEN_ATTRIBUTE.equals(pair[0])) {
                return pair[1];
            }
        }
        return null;
    }
}
