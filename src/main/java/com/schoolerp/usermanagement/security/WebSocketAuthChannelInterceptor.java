package com.schoolerp.usermanagement.security;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class WebSocketAuthChannelInterceptor implements ChannelInterceptor {

    private final JwtTokenProvider jwtTokenProvider;

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);

        if (accessor != null && StompCommand.CONNECT.equals(accessor.getCommand())) {
            String authHeader = accessor.getFirstNativeHeader("Authorization");

            log.debug("WebSocket STOMP CONNECT frame received. Auth header present: {}", (authHeader != null));

            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                log.warn("WebSocket STOMP CONNECT rejected: Missing or invalid Authorization header");
                throw new IllegalArgumentException("Unauthorized: Missing or invalid Authorization header for WebSocket");
            }

            String token = authHeader.substring(7).trim();

            if (!jwtTokenProvider.validateAccessToken(token)) {
                log.warn("WebSocket STOMP CONNECT rejected: Invalid access token");
                throw new IllegalArgumentException("Unauthorized: Invalid access token for WebSocket");
            }

            String userId = jwtTokenProvider.getUserIdFromJWT(token);
            if (userId == null || userId.isBlank()) {
                log.warn("WebSocket STOMP CONNECT rejected: Token does not contain userId");
                throw new IllegalArgumentException("Unauthorized: Token missing userId");
            }

            List<String> roles = jwtTokenProvider.getRolesFromJWT(token);
            List<SimpleGrantedAuthority> authorities = roles == null ? Collections.emptyList() :
                    roles.stream()
                            .map(r -> r.startsWith("ROLE_") ? r : "ROLE_" + r)
                            .map(SimpleGrantedAuthority::new)
                            .toList();

            UsernamePasswordAuthenticationToken authentication =
                    new UsernamePasswordAuthenticationToken(new StompPrincipal(userId), null, authorities);

            accessor.setUser(authentication);
            log.info("WebSocket STOMP user authenticated successfully: userId={}", userId);
        }

        return message;
    }
}
