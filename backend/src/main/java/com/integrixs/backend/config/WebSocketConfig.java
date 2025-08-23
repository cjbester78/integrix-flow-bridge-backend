package com.integrixs.backend.config;

import com.integrixs.backend.websocket.FlowExecutionWebSocketHandler;
import com.integrixs.backend.websocket.MessageWebSocketHandler;
import com.integrixs.backend.websocket.SimpleWebSocketHandler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;

@Slf4j
@Configuration
@EnableWebSocket
@EnableScheduling
public class WebSocketConfig implements WebSocketConfigurer {

    @Autowired
    private MessageWebSocketHandler messageWebSocketHandler;
    
    @Autowired(required = false)
    private FlowExecutionWebSocketHandler flowExecutionWebSocketHandler;
    
    @Autowired(required = false)
    private SimpleWebSocketHandler simpleWebSocketHandler;

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        log.info("=== Registering WebSocket handlers ===");
        
        // Message monitoring WebSocket endpoint - this is the critical one
        registry.addHandler(messageWebSocketHandler, "/ws/messages")
                .setAllowedOrigins("*");
        log.info("Registered /ws/messages WebSocket endpoint");
        
        // Flow execution monitoring
        if (flowExecutionWebSocketHandler != null) {
            registry.addHandler(flowExecutionWebSocketHandler, "/ws/flow-execution")
                    .setAllowedOrigins("*");
            log.info("Registered /ws/flow-execution WebSocket endpoint");
        }
        
        // Test endpoint
        if (simpleWebSocketHandler != null) {
            registry.addHandler(simpleWebSocketHandler, "/ws/test")
                    .setAllowedOrigins("*");
            log.info("Registered /ws/test WebSocket endpoint");
        }
        
        log.info("=== WebSocket handler registration complete ===");
    }
}