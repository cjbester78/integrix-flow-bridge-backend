package com.integrationlab.backend.websocket;

import com.integrationlab.backend.security.JwtUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.HandshakeInterceptor;

import java.util.Map;

@Slf4j
@Component
public class AuthHandshakeInterceptor implements HandshakeInterceptor {

    private final JwtUtil jwtUtil;
    
    @Autowired
    public AuthHandshakeInterceptor(JwtUtil jwtUtil) {
        this.jwtUtil = jwtUtil;
    }

    @Override
    public boolean beforeHandshake(ServerHttpRequest request, ServerHttpResponse response,
                                   WebSocketHandler wsHandler, Map<String, Object> attributes) throws Exception {
        try {
            log.debug("WebSocket handshake request: {}", request.getURI());
            
            if (jwtUtil == null) {
                log.error("JwtUtil is not initialized!");
                response.setStatusCode(HttpStatus.INTERNAL_SERVER_ERROR);
                return false;
            }
            
            // Extract token from query parameter or Authorization header
            String token = extractToken(request);
            
            if (token == null) {
                log.warn("WebSocket authentication failed - no token provided");
                response.setStatusCode(HttpStatus.UNAUTHORIZED);
                return false;
            }
            
            if (jwtUtil.validateToken(token)) {
                String username = jwtUtil.extractUsername(token);
                attributes.put("username", username);
                log.info("WebSocket authenticated for user: {}", username);
                return true;
            } else {
                log.warn("WebSocket authentication failed - invalid token");
                response.setStatusCode(HttpStatus.UNAUTHORIZED);
                return false;
            }
        } catch (Exception e) {
            log.error("Error during WebSocket handshake authentication", e);
            response.setStatusCode(HttpStatus.INTERNAL_SERVER_ERROR);
            return false;
        }
    }

    @Override
    public void afterHandshake(ServerHttpRequest request, ServerHttpResponse response,
                               WebSocketHandler wsHandler, Exception exception) {
        // No action needed after handshake
    }

    private String extractToken(ServerHttpRequest request) {
        // Try to get token from Authorization header
        String authHeader = request.getHeaders().getFirst("Authorization");
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            return authHeader.substring(7);
        }
        
        // Try to get token from query parameter (for WebSocket connections)
        String query = request.getURI().getQuery();
        if (query != null) {
            String[] params = query.split("&");
            for (String param : params) {
                String[] keyValue = param.split("=");
                if (keyValue.length == 2 && "token".equals(keyValue[0])) {
                    return keyValue[1];
                }
            }
        }
        
        return null;
    }
}