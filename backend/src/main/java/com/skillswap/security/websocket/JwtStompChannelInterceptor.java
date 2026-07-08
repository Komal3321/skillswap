package com.skillswap.security.websocket;

import com.skillswap.security.jwt.JwtTokenProvider;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

/**
 * Authenticates STOMP connections using the same JWTs as the REST API.
 */
@Component
public class JwtStompChannelInterceptor implements ChannelInterceptor {

    private static final String AUTHORIZATION_HEADER = "Authorization";
    private static final String TOKEN_HEADER = "access_token";
    private static final String BEARER_PREFIX = "Bearer ";

    private final JwtTokenProvider jwtTokenProvider;
    private final UserDetailsService userDetailsService;

    public JwtStompChannelInterceptor(JwtTokenProvider jwtTokenProvider, UserDetailsService userDetailsService) {
        this.jwtTokenProvider = jwtTokenProvider;
        this.userDetailsService = userDetailsService;
    }

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);
        if (accessor == null) {
            return message;
        }

        if (StompCommand.CONNECT.equals(accessor.getCommand())) {
            UsernamePasswordAuthenticationToken authentication = authenticate(accessor);
            accessor.setUser(authentication);
            SecurityContextHolder.getContext().setAuthentication(authentication);
            return message;
        }

        if (accessor.getUser() instanceof UsernamePasswordAuthenticationToken authentication) {
            SecurityContextHolder.getContext().setAuthentication(authentication);
        }
        return message;
    }

    private UsernamePasswordAuthenticationToken authenticate(StompHeaderAccessor accessor) {
        String token = resolveToken(accessor);
        if (!StringUtils.hasText(token) || !jwtTokenProvider.isTokenValid(token)) {
            throw new org.springframework.security.access.AccessDeniedException("Invalid WebSocket token");
        }
        UserDetails userDetails = userDetailsService.loadUserByUsername(jwtTokenProvider.extractSubject(token));
        return new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
    }

    private String resolveToken(StompHeaderAccessor accessor) {
        String authorization = accessor.getFirstNativeHeader(AUTHORIZATION_HEADER);
        if (StringUtils.hasText(authorization) && authorization.startsWith(BEARER_PREFIX)) {
            return authorization.substring(BEARER_PREFIX.length());
        }
        String nativeToken = accessor.getFirstNativeHeader(TOKEN_HEADER);
        if (StringUtils.hasText(nativeToken)) {
            return nativeToken;
        }
        Object handshakeToken = accessor.getSessionAttributes() == null
                ? null
                : accessor.getSessionAttributes().get(JwtHandshakeInterceptor.ACCESS_TOKEN_ATTRIBUTE);
        return handshakeToken instanceof String token ? token : null;
    }
}
