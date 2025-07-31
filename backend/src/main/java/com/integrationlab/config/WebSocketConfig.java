package com.integrationlab.config;

import com.integrationlab.websocket.FlowExecutionWebSocketHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;

@Configuration
@EnableWebSocket
public class WebSocketConfig implements WebSocketConfigurer {

    @Autowired
    private FlowExecutionWebSocketHandler flowExecutionWebSocketHandler;

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        // Register WebSocket endpoint for flow execution monitoring  
        registry.addHandler(flowExecutionWebSocketHandler, "/ws/flow-execution")
                .setAllowedOrigins("*"); // In production, specify actual allowed origins
        
        // Also register without SockJS for native WebSocket clients
        registry.addHandler(flowExecutionWebSocketHandler, "/ws/flow-execution-native")
                .setAllowedOrigins("*");
    }
}