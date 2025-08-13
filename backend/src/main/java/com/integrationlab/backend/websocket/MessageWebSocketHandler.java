package com.integrationlab.backend.websocket;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.integrationlab.backend.security.JwtUtil;
import com.integrationlab.backend.service.MessageService;
import com.integrationlab.shared.dto.MessageDTO;
import com.integrationlab.shared.dto.MessageStatsDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class MessageWebSocketHandler extends TextWebSocketHandler {
    
    private static final Logger logger = LoggerFactory.getLogger(MessageWebSocketHandler.class);
    
    private final Map<String, WebSocketSession> sessions = new ConcurrentHashMap<>();
    private final Map<String, String> sessionToUser = new ConcurrentHashMap<>();
    private final ObjectMapper objectMapper = new ObjectMapper();
    
    @Autowired
    private MessageService messageService;
    
    @Autowired
    private JwtUtil jwtUtil;
    
    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        logger.info("WebSocket connection established: {}", session.getId());
        sessions.put(session.getId(), session);
        
        // Don't send initial stats - let the client request with filters
        // sendMessageStats(session);
    }
    
    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        logger.info("WebSocket connection closed: {}", session.getId());
        sessions.remove(session.getId());
    }
    
    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        logger.debug("Received WebSocket message: {}", message.getPayload());
        
        try {
            Map<String, Object> payload = objectMapper.readValue(message.getPayload(), Map.class);
            String command = (String) payload.get("command");
            
            if ("get_stats".equals(command)) {
                sendMessageStats(session);
            }
        } catch (Exception e) {
            logger.error("Error handling WebSocket message", e);
        }
    }
    
    @Override
    public void handleTransportError(WebSocketSession session, Throwable exception) throws Exception {
        logger.error("WebSocket transport error for session: " + session.getId(), exception);
        sessions.remove(session.getId());
    }
    
    /**
     * Broadcast updates to all connected sessions
     */
    // Disabled scheduled updates as they override filtered stats in the UI
    // TODO: Implement filtered stats based on client's current filters
    // @Scheduled(fixedDelay = 5000) // Every 5 seconds
    public void broadcastUpdates() {
        if (sessions.isEmpty()) {
            return;
        }
        
        // Get latest stats
        MessageStatsDTO stats = messageService.getMessageStats(new HashMap<>());
        
        Map<String, Object> update = new HashMap<>();
        update.put("type", "stats_update");
        update.put("stats", stats);
        
        String jsonMessage;
        try {
            jsonMessage = objectMapper.writeValueAsString(update);
        } catch (Exception e) {
            logger.error("Error serializing stats update", e);
            return;
        }
        
        // Broadcast to all connected sessions
        sessions.values().forEach(session -> {
            if (session.isOpen()) {
                try {
                    session.sendMessage(new TextMessage(jsonMessage));
                } catch (IOException e) {
                    logger.error("Error sending WebSocket message to session: " + session.getId(), e);
                }
            }
        });
    }
    
    /**
     * Send message stats to a specific session
     */
    private void sendMessageStats(WebSocketSession session) {
        try {
            MessageStatsDTO stats = messageService.getMessageStats(new HashMap<>());
            
            Map<String, Object> response = new HashMap<>();
            response.put("type", "stats_update");
            response.put("stats", stats);
            
            String jsonMessage = objectMapper.writeValueAsString(response);
            session.sendMessage(new TextMessage(jsonMessage));
        } catch (Exception e) {
            logger.error("Error sending stats to session: " + session.getId(), e);
        }
    }
    
    /**
     * Send message update notification
     */
    public void sendMessageUpdate(MessageDTO message) {
        Map<String, Object> update = new HashMap<>();
        update.put("type", "message_update");
        update.put("message", message);
        
        String jsonMessage;
        try {
            jsonMessage = objectMapper.writeValueAsString(update);
        } catch (Exception e) {
            logger.error("Error serializing message update", e);
            return;
        }
        
        sessions.values().forEach(session -> {
            if (session.isOpen()) {
                try {
                    session.sendMessage(new TextMessage(jsonMessage));
                } catch (IOException e) {
                    logger.error("Error sending message update to session: " + session.getId(), e);
                }
            }
        });
    }
}