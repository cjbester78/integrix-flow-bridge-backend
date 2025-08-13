package com.integrationlab.backend.websocket;

import jakarta.websocket.*;
import jakarta.websocket.server.ServerEndpoint;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@ServerEndpoint("/direct-ws")
public class DirectWebSocketEndpoint {

    @OnOpen
    public void onOpen(Session session) {
        log.info("Direct WebSocket opened: {}", session.getId());
        try {
            session.getBasicRemote().sendText("Connected to Direct WebSocket!");
        } catch (Exception e) {
            log.error("Error sending welcome message", e);
        }
    }

    @OnMessage
    public String onMessage(String message, Session session) {
        log.info("Direct WebSocket received: {}", message);
        return "Echo: " + message;
    }

    @OnClose
    public void onClose(Session session) {
        log.info("Direct WebSocket closed: {}", session.getId());
    }

    @OnError
    public void onError(Session session, Throwable throwable) {
        log.error("Direct WebSocket error", throwable);
    }
}