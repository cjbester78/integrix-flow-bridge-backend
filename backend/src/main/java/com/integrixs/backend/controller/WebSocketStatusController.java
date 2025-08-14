package com.integrixs.backend.controller;

import jakarta.servlet.ServletContext;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.HandlerMapping;
import org.springframework.web.socket.server.support.WebSocketHandlerMapping;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/websocket-test")
public class WebSocketStatusController {

    @Autowired
    private ApplicationContext applicationContext;
    
    @Autowired(required = false)
    private ServletContext servletContext;

    @GetMapping("/status")
    public Map<String, Object> getWebSocketStatus() {
        Map<String, Object> status = new HashMap<>();
        
        // Check if WebSocket support is available
        try {
            // Check for WebSocket handler mappings
            Map<String, HandlerMapping> handlerMappings = applicationContext.getBeansOfType(HandlerMapping.class);
            int webSocketHandlerCount = 0;
            
            for (Map.Entry<String, HandlerMapping> entry : handlerMappings.entrySet()) {
                if (entry.getValue() instanceof WebSocketHandlerMapping) {
                    WebSocketHandlerMapping wsMapping = (WebSocketHandlerMapping) entry.getValue();
                    status.put("webSocketHandlerMapping", true);
                    status.put("webSocketHandlerMappingOrder", wsMapping.getOrder());
                    status.put("webSocketHandlers", wsMapping.getUrlMap().keySet());
                    webSocketHandlerCount++;
                }
            }
            
            status.put("webSocketHandlerMappingCount", webSocketHandlerCount);
            
            // Check servlet context
            if (servletContext != null) {
                status.put("servletContextAvailable", true);
                status.put("servletContextClass", servletContext.getClass().getName());
                
                // Check for WebSocket container
                try {
                    Object wsContainer = servletContext.getAttribute("jakarta.websocket.server.ServerContainer");
                    status.put("webSocketContainerAvailable", wsContainer != null);
                    if (wsContainer != null) {
                        status.put("webSocketContainerClass", wsContainer.getClass().getName());
                    }
                } catch (Exception e) {
                    status.put("webSocketContainerError", e.getMessage());
                }
            } else {
                status.put("servletContextAvailable", false);
            }
            
            // Check for WebSocket beans
            status.put("webSocketConfigurers", applicationContext.getBeanNamesForType(
                org.springframework.web.socket.config.annotation.WebSocketConfigurer.class));
            
        } catch (Exception e) {
            status.put("error", e.getMessage());
            log.error("Error checking WebSocket status", e);
        }
        
        return status;
    }
}