package com.integrixs.backend.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.socket.server.support.WebSocketHandlerMapping;

import java.io.IOException;

@Slf4j
//@Component  // Disabled - might be interfering
@Order(Ordered.HIGHEST_PRECEDENCE + 1)
public class WebSocketUpgradeFilter extends OncePerRequestFilter {

    @Autowired
    private ApplicationContext applicationContext;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        
        String upgradeHeader = request.getHeader("Upgrade");
        
        if ("websocket".equalsIgnoreCase(upgradeHeader)) {
            log.info("WebSocket upgrade request detected for path: {}", request.getRequestURI());
            
            // Set attribute to indicate this is a WebSocket request
            request.setAttribute("org.springframework.web.socket.server.HandshakeRequest", Boolean.TRUE);
            
            // Check if we have a WebSocketHandlerMapping
            try {
                WebSocketHandlerMapping wsMapping = applicationContext.getBean(WebSocketHandlerMapping.class);
                if (wsMapping != null) {
                    log.info("WebSocketHandlerMapping found, proceeding with upgrade");
                }
            } catch (Exception e) {
                log.error("No WebSocketHandlerMapping found", e);
            }
        }
        
        filterChain.doFilter(request, response);
    }
}