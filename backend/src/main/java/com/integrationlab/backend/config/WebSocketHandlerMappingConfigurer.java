package com.integrationlab.backend.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.event.EventListener;
import org.springframework.core.Ordered;
import org.springframework.web.servlet.HandlerMapping;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;
import org.springframework.web.socket.server.support.WebSocketHandlerMapping;

import java.util.Map;

@Slf4j
@Configuration
public class WebSocketHandlerMappingConfigurer {

    @Autowired
    private ApplicationContext applicationContext;

    @EventListener(ApplicationReadyEvent.class)
    public void configureWebSocketHandlerOrder() {
        log.info("Configuring WebSocket handler mapping order...");
        
        Map<String, HandlerMapping> handlerMappings = applicationContext.getBeansOfType(HandlerMapping.class);
        
        for (Map.Entry<String, HandlerMapping> entry : handlerMappings.entrySet()) {
            HandlerMapping mapping = entry.getValue();
            log.info("Found handler mapping: {} - {}", entry.getKey(), mapping.getClass().getSimpleName());
            
            if (mapping instanceof WebSocketHandlerMapping) {
                WebSocketHandlerMapping wsMapping = (WebSocketHandlerMapping) mapping;
                log.info("Setting WebSocketHandlerMapping order to HIGHEST_PRECEDENCE");
                wsMapping.setOrder(Ordered.HIGHEST_PRECEDENCE);
            }
            
            if (mapping instanceof Ordered) {
                log.info("  Order: {}", ((Ordered) mapping).getOrder());
            }
        }
    }
}