package com.integrationlab.backend.controller;

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
@RequestMapping("/api/debug")
public class WebSocketDebugController {

    @Autowired
    private ApplicationContext applicationContext;

    @GetMapping("/websocket-handlers")
    public Map<String, Object> getWebSocketHandlers() {
        Map<String, Object> result = new HashMap<>();
        
        Map<String, HandlerMapping> handlerMappings = applicationContext.getBeansOfType(HandlerMapping.class);
        
        for (Map.Entry<String, HandlerMapping> entry : handlerMappings.entrySet()) {
            if (entry.getValue() instanceof WebSocketHandlerMapping) {
                WebSocketHandlerMapping wsMapping = (WebSocketHandlerMapping) entry.getValue();
                Map<String, Object> wsInfo = new HashMap<>();
                wsInfo.put("order", wsMapping.getOrder());
                wsInfo.put("urlMap", wsMapping.getUrlMap());
                wsInfo.put("handlerMap", wsMapping.getHandlerMap());
                result.put(entry.getKey(), wsInfo);
            }
        }
        
        return result;
    }
}