package com.integrationlab.backend.config;

import com.integrationlab.backend.websocket.FlowExecutionWebSocketHandler;
import com.integrationlab.backend.websocket.MessageWebSocketHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;

@Configuration
@EnableWebSocket
@EnableScheduling
public class WebSocketConfig implements WebSocketConfigurer {

    @Autowired
    private FlowExecutionWebSocketHandler flowExecutionWebSocketHandler;
    
    @Autowired
    private MessageWebSocketHandler messageWebSocketHandler;

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        // Register WebSocket endpoint for flow execution monitoring  
        registry.addHandler(flowExecutionWebSocketHandler, "/ws/flow-execution")
                .setAllowedOrigins("*"); // In production, specify actual allowed origins
        
        // Also register without SockJS for native WebSocket clients
        registry.addHandler(flowExecutionWebSocketHandler, "/ws/flow-execution-native")
                .setAllowedOrigins("*");
                
        // Message monitoring WebSocket endpoint
        registry.addHandler(messageWebSocketHandler, "/ws/messages")
                .setAllowedOrigins("*");
    }
}