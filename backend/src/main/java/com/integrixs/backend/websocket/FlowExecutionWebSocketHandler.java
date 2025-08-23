package com.integrixs.backend.websocket;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.*;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class FlowExecutionWebSocketHandler implements WebSocketHandler {

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final Map<String, WebSocketSession> sessions = new ConcurrentHashMap<>();
    private final Map<String, Set<String>> flowSubscriptions = new ConcurrentHashMap<>();

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        sessions.put(session.getId(), session);
        
        // Send welcome message
        FlowExecutionMessage welcomeMessage = new FlowExecutionMessage();
        welcomeMessage.setType(MessageType.CONNECTION_ESTABLISHED);
        welcomeMessage.setMessage("WebSocket connection established for flow execution monitoring");
        welcomeMessage.setTimestamp(LocalDateTime.now());
        
        sendMessage(session, welcomeMessage);
    }

    @Override
    public void handleMessage(WebSocketSession session, WebSocketMessage<?> message) throws Exception {
        if (message instanceof TextMessage) {
            TextMessage textMessage = (TextMessage) message;
            try {
                FlowExecutionCommand command = objectMapper.readValue(textMessage.getPayload(), FlowExecutionCommand.class);
                handleCommand(session, command);
            } catch (Exception e) {
                sendErrorMessage(session, "Invalid command format: " + e.getMessage());
            }
        }
    }

    @Override
    public void handleTransportError(WebSocketSession session, Throwable exception) throws Exception {
        sendErrorMessage(session, "Transport error: " + exception.getMessage());
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus closeStatus) throws Exception {
        String sessionId = session.getId();
        sessions.remove(sessionId);
        
        // Remove session from all flow subscriptions
        flowSubscriptions.values().forEach(subscribers -> subscribers.remove(sessionId));
        
        // Clean up empty subscription sets
        flowSubscriptions.entrySet().removeIf(entry -> entry.getValue().isEmpty());
    }

    @Override
    public boolean supportsPartialMessages() {
        return false;
    }

    private void handleCommand(WebSocketSession session, FlowExecutionCommand command) throws IOException {
        switch (command.getAction()) {
            case "subscribe":
                handleSubscribeCommand(session, command);
                break;
            case "unsubscribe":
                handleUnsubscribeCommand(session, command);
                break;
            case "list_flows":
                handleListFlowsCommand(session, command);
                break;
            case "get_status":
                handleGetStatusCommand(session, command);
                break;
            default:
                sendErrorMessage(session, "Unknown command: " + command.getAction());
        }
    }

    private void handleSubscribeCommand(WebSocketSession session, FlowExecutionCommand command) throws IOException {
        String flowId = command.getFlowId();
        if (flowId != null && !flowId.isEmpty()) {
            flowSubscriptions.computeIfAbsent(flowId, k -> ConcurrentHashMap.newKeySet()).add(session.getId());
            
            FlowExecutionMessage response = new FlowExecutionMessage();
            response.setType(MessageType.SUBSCRIPTION_CONFIRMED);
            response.setFlowId(flowId);
            response.setMessage("Subscribed to flow execution updates for: " + flowId);
            response.setTimestamp(LocalDateTime.now());
            
            sendMessage(session, response);
        } else {
            sendErrorMessage(session, "Flow ID is required for subscription");
        }
    }

    private void handleUnsubscribeCommand(WebSocketSession session, FlowExecutionCommand command) throws IOException {
        String flowId = command.getFlowId();
        if (flowId != null && !flowId.isEmpty()) {
            Set<String> subscribers = flowSubscriptions.get(flowId);
            if (subscribers != null) {
                subscribers.remove(session.getId());
                if (subscribers.isEmpty()) {
                    flowSubscriptions.remove(flowId);
                }
            }
            
            FlowExecutionMessage response = new FlowExecutionMessage();
            response.setType(MessageType.SUBSCRIPTION_CANCELLED);
            response.setFlowId(flowId);
            response.setMessage("Unsubscribed from flow execution updates for: " + flowId);
            response.setTimestamp(LocalDateTime.now());
            
            sendMessage(session, response);
        } else {
            sendErrorMessage(session, "Flow ID is required for unsubscription");
        }
    }

    private void handleListFlowsCommand(WebSocketSession session, FlowExecutionCommand command) throws IOException {
        FlowExecutionMessage response = new FlowExecutionMessage();
        response.setType(MessageType.FLOW_LIST);
        response.setMessage("Active flow subscriptions");
        response.setTimestamp(LocalDateTime.now());
        
        Map<String, Object> data = new HashMap<>();
        data.put("subscribedFlows", new ArrayList<>(flowSubscriptions.keySet()));
        data.put("totalSubscriptions", flowSubscriptions.keySet().size());
        response.setData(data);
        
        sendMessage(session, response);
    }

    private void handleGetStatusCommand(WebSocketSession session, FlowExecutionCommand command) throws IOException {
        String flowId = command.getFlowId();
        if (flowId != null && !flowId.isEmpty()) {
            // In production, this would query actual flow execution status
            FlowExecutionMessage response = new FlowExecutionMessage();
            response.setType(MessageType.FLOW_STATUS);
            response.setFlowId(flowId);
            response.setMessage("Flow status retrieved");
            response.setTimestamp(LocalDateTime.now());
            
            Map<String, Object> statusData = new HashMap<>();
            statusData.put("flowId", flowId);
            statusData.put("status", "RUNNING"); // Placeholder
            statusData.put("lastUpdate", LocalDateTime.now());
            statusData.put("subscribers", flowSubscriptions.getOrDefault(flowId, Collections.emptySet()).size());
            response.setData(statusData);
            
            sendMessage(session, response);
        } else {
            sendErrorMessage(session, "Flow ID is required for status check");
        }
    }

    public void broadcastFlowExecutionUpdate(String flowId, FlowExecutionUpdate update) {
        Set<String> subscribers = flowSubscriptions.get(flowId);
        if (subscribers != null && !subscribers.isEmpty()) {
            FlowExecutionMessage message = new FlowExecutionMessage();
            message.setType(MessageType.EXECUTION_UPDATE);
            message.setFlowId(flowId);
            message.setMessage("Flow execution update");
            message.setTimestamp(LocalDateTime.now());
            message.setData(update);
            
            subscribers.forEach(sessionId -> {
                WebSocketSession session = sessions.get(sessionId);
                if (session != null && session.isOpen()) {
                    try {
                        sendMessage(session, message);
                    } catch (IOException e) {
                        // Remove failed session
                        sessions.remove(sessionId);
                        subscribers.remove(sessionId);
                    }
                }
            });
        }
    }

    public void broadcastFlowExecutionStarted(String flowId, String executionId) {
        FlowExecutionUpdate update = new FlowExecutionUpdate();
        update.setExecutionId(executionId);
        update.setStatus("STARTED");
        update.setMessage("Flow execution started");
        update.setTimestamp(LocalDateTime.now());
        
        broadcastFlowExecutionUpdate(flowId, update);
    }

    public void broadcastFlowExecutionCompleted(String flowId, String executionId, boolean success) {
        FlowExecutionUpdate update = new FlowExecutionUpdate();
        update.setExecutionId(executionId);
        update.setStatus(success ? "COMPLETED" : "FAILED");
        update.setMessage("Flow execution " + (success ? "completed successfully" : "failed"));
        update.setTimestamp(LocalDateTime.now());
        
        broadcastFlowExecutionUpdate(flowId, update);
    }

    public void broadcastFlowExecutionProgress(String flowId, String executionId, String currentStep, String message) {
        FlowExecutionUpdate update = new FlowExecutionUpdate();
        update.setExecutionId(executionId);
        update.setStatus("RUNNING");
        update.setCurrentStep(currentStep);
        update.setMessage(message);
        update.setTimestamp(LocalDateTime.now());
        
        broadcastFlowExecutionUpdate(flowId, update);
    }

    public void broadcastFlowExecutionError(String flowId, String executionId, String errorMessage) {
        FlowExecutionUpdate update = new FlowExecutionUpdate();
        update.setExecutionId(executionId);
        update.setStatus("ERROR");
        update.setMessage("Execution error: " + errorMessage);
        update.setTimestamp(LocalDateTime.now());
        
        broadcastFlowExecutionUpdate(flowId, update);
    }

    private void sendMessage(WebSocketSession session, FlowExecutionMessage message) throws IOException {
        if (session.isOpen()) {
            String json = objectMapper.writeValueAsString(message);
            session.sendMessage(new TextMessage(json));
        }
    }

    private void sendErrorMessage(WebSocketSession session, String errorMessage) throws IOException {
        FlowExecutionMessage message = new FlowExecutionMessage();
        message.setType(MessageType.ERROR);
        message.setMessage(errorMessage);
        message.setTimestamp(LocalDateTime.now());
        
        sendMessage(session, message);
    }

    // Message classes
    public enum MessageType {
        CONNECTION_ESTABLISHED,
        SUBSCRIPTION_CONFIRMED,
        SUBSCRIPTION_CANCELLED,
        FLOW_LIST,
        FLOW_STATUS,
        EXECUTION_UPDATE,
        ERROR
    }

    public static class FlowExecutionMessage {
        private MessageType type;
        private String flowId;
        private String message;
        private LocalDateTime timestamp;
        private Object data;

        // Getters and setters
        public MessageType getType() { return type; }
        public void setType(MessageType type) { this.type = type; }
        public String getFlowId() { return flowId; }
        public void setFlowId(String flowId) { this.flowId = flowId; }
        public String getMessage() { return message; }
        public void setMessage(String message) { this.message = message; }
        public LocalDateTime getTimestamp() { return timestamp; }
        public void setTimestamp(LocalDateTime timestamp) { this.timestamp = timestamp; }
        public Object getData() { return data; }
        public void setData(Object data) { this.data = data; }
    }

    public static class FlowExecutionCommand {
        private String action;
        private String flowId;
        private Map<String, Object> parameters = new HashMap<>();

        // Getters and setters
        public String getAction() { return action; }
        public void setAction(String action) { this.action = action; }
        public String getFlowId() { return flowId; }
        public void setFlowId(String flowId) { this.flowId = flowId; }
        public Map<String, Object> getParameters() { return parameters; }
        public void setParameters(Map<String, Object> parameters) { this.parameters = parameters; }
    }

    public static class FlowExecutionUpdate {
        private String executionId;
        private String status;
        private String currentStep;
        private String message;
        private LocalDateTime timestamp;
        private Map<String, Object> metadata = new HashMap<>();

        // Getters and setters
        public String getExecutionId() { return executionId; }
        public void setExecutionId(String executionId) { this.executionId = executionId; }
        public String getStatus() { return status; }
        public void setStatus(String status) { this.status = status; }
        public String getCurrentStep() { return currentStep; }
        public void setCurrentStep(String currentStep) { this.currentStep = currentStep; }
        public String getMessage() { return message; }
        public void setMessage(String message) { this.message = message; }
        public LocalDateTime getTimestamp() { return timestamp; }
        public void setTimestamp(LocalDateTime timestamp) { this.timestamp = timestamp; }
        public Map<String, Object> getMetadata() { return metadata; }
        public void setMetadata(Map<String, Object> metadata) { this.metadata = metadata; }
    }
}