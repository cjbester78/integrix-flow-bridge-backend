package com.integrationlab.adapters.impl;

import com.integrationlab.adapters.core.*;
import com.integrationlab.adapters.config.RfcSenderAdapterConfig;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * RFC Sender Adapter implementation for SAP RFC server functionality (INBOUND).
 * Follows middleware convention: Sender = receives data FROM external systems.
 * Acts as an RFC server to receive function calls from SAP systems.
 * 
 * Note: This is a simulation. Real implementation would require SAP JCo libraries.
 */
public class RfcSenderAdapter extends AbstractSenderAdapter {
    
    private final RfcSenderAdapterConfig config;
    private final Map<String, Object> receivedCalls = new ConcurrentHashMap<>();
    private boolean serverStarted = false;
    
    public RfcSenderAdapter(RfcSenderAdapterConfig config) {
        super(AdapterType.RFC);
        this.config = config;
    }
    
    @Override
    protected void doSenderInitialize() throws Exception {
        logger.info("Initializing RFC sender adapter (inbound) with program ID: {}", config.getProgramId());
        
        validateConfiguration();
        
        // In real implementation, would:
        // 1. Initialize SAP JCo environment
        // 2. Create RFC server instance
        // 3. Register function handlers
        // 4. Start RFC server
        
        serverStarted = true;
        logger.info("RFC sender adapter initialized successfully");
    }
    
    @Override
    protected void doSenderDestroy() throws Exception {
        logger.info("Destroying RFC sender adapter");
        
        if (serverStarted) {
            // In real implementation, would stop RFC server
            serverStarted = false;
        }
        
        receivedCalls.clear();
    }
    
    @Override
    protected AdapterResult doTestConnection() throws Exception {
        List<AdapterResult> testResults = new ArrayList<>();
        
        // Test 1: Gateway connectivity
        testResults.add(ConnectionTestUtil.executeBasicConnectivityTest(AdapterType.RFC, () -> {
            try {
                // Simulate gateway connection test
                String gatewayInfo = String.format("Gateway: %s:%s", 
                        config.getGatewayHost(), config.getGatewayService());
                
                if (config.getGatewayHost() == null || config.getGatewayHost().isEmpty()) {
                    return ConnectionTestUtil.createTestFailure(AdapterType.RFC, 
                            "Gateway Connection", "Gateway host not configured", null);
                }
                
                return ConnectionTestUtil.createTestSuccess(AdapterType.RFC, 
                        "Gateway Connection", "Gateway configuration valid: " + gatewayInfo);
                        
            } catch (Exception e) {
                return ConnectionTestUtil.createTestFailure(AdapterType.RFC, 
                        "Gateway Connection", "Failed to validate gateway: " + e.getMessage(), e);
            }
        }));
        
        // Test 2: Program ID validation
        testResults.add(ConnectionTestUtil.executeConfigurationTest(AdapterType.RFC, () -> {
            try {
                if (config.getProgramId() == null || config.getProgramId().isEmpty()) {
                    return ConnectionTestUtil.createTestFailure(AdapterType.RFC, 
                            "Program ID", "Program ID not configured", null);
                }
                
                return ConnectionTestUtil.createTestSuccess(AdapterType.RFC, 
                        "Program ID", "Program ID configured: " + config.getProgramId());
                        
            } catch (Exception e) {
                return ConnectionTestUtil.createTestFailure(AdapterType.RFC, 
                        "Program ID", "Invalid program ID: " + e.getMessage(), e);
            }
        }));
        
        // Test 3: Function module configuration
        testResults.add(ConnectionTestUtil.executeConfigurationTest(AdapterType.RFC, () -> {
            try {
                String info = "RFC server ready to receive function calls";
                if (config.getAllowedFunctions() != null && !config.getAllowedFunctions().isEmpty()) {
                    info += ", Allowed functions: " + config.getAllowedFunctions();
                }
                
                return ConnectionTestUtil.createTestSuccess(AdapterType.RFC, 
                        "Function Configuration", info);
                        
            } catch (Exception e) {
                return ConnectionTestUtil.createTestFailure(AdapterType.RFC, 
                        "Function Configuration", "Invalid configuration: " + e.getMessage(), e);
            }
        }));
        
        return ConnectionTestUtil.combineTestResults(AdapterType.RFC, 
                testResults.toArray(new AdapterResult[0]));
    }
    
    @Override
    protected AdapterResult doSend(Object payload, Map<String, Object> headers) throws Exception {
        // For RFC Sender (inbound), this simulates receiving RFC calls from SAP
        return receiveRfcCall(payload, headers);
    }
    
    private AdapterResult receiveRfcCall(Object payload, Map<String, Object> headers) throws Exception {
        // Simulate receiving an RFC call
        Map<String, Object> rfcData = new HashMap<>();
        
        if (payload instanceof Map) {
            Map<String, Object> callData = (Map<String, Object>) payload;
            
            // Extract function name
            String functionName = (String) callData.get("functionName");
            if (functionName == null) {
                functionName = "SIMULATED_RFC_FUNCTION";
            }
            
            // Check if function is allowed
            if (config.getAllowedFunctions() != null && !config.getAllowedFunctions().isEmpty()) {
                List<String> allowedFunctions = Arrays.asList(config.getAllowedFunctions().split(","));
                if (!allowedFunctions.contains(functionName)) {
                    throw new AdapterException.ValidationException(AdapterType.RFC, 
                            "Function not allowed: " + functionName);
                }
            }
            
            rfcData.put("functionName", functionName);
            rfcData.put("callId", UUID.randomUUID().toString());
            rfcData.put("timestamp", new Date());
            rfcData.put("sourceSystem", headers != null ? headers.get("sourceSystem") : "SAP");
            
            // Import parameters
            Map<String, Object> importParams = (Map<String, Object>) callData.get("importParameters");
            if (importParams == null) {
                importParams = new HashMap<>();
            }
            rfcData.put("importParameters", importParams);
            
            // Table parameters
            Map<String, List<Map<String, Object>>> tableParams = 
                    (Map<String, List<Map<String, Object>>>) callData.get("tableParameters");
            if (tableParams == null) {
                tableParams = new HashMap<>();
            }
            rfcData.put("tableParameters", tableParams);
            
            // Store received call
            receivedCalls.put((String) rfcData.get("callId"), rfcData);
            
            // Prepare response (export parameters)
            Map<String, Object> exportParams = new HashMap<>();
            exportParams.put("EV_RESULT", "SUCCESS");
            exportParams.put("EV_MESSAGE", "RFC call processed successfully");
            rfcData.put("exportParameters", exportParams);
            
            logger.info("RFC sender adapter received function call: {}", functionName);
            
            return AdapterResult.success(rfcData, 
                    String.format("Successfully received RFC call: %s", functionName));
                    
        } else {
            // Simple simulation
            rfcData.put("functionName", "SIMULATED_RFC");
            rfcData.put("callId", UUID.randomUUID().toString());
            rfcData.put("timestamp", new Date());
            rfcData.put("payload", payload);
            
            return AdapterResult.success(rfcData, "Successfully received RFC call");
        }
    }
    
    private void validateConfiguration() throws AdapterException.ConfigurationException {
        if (config.getProgramId() == null || config.getProgramId().trim().isEmpty()) {
            throw new AdapterException.ConfigurationException(AdapterType.RFC, "Program ID is required");
        }
        
        if (config.getGatewayHost() == null || config.getGatewayHost().trim().isEmpty()) {
            throw new AdapterException.ConfigurationException(AdapterType.RFC, "Gateway host is required");
        }
        
        if (config.getGatewayService() == null || config.getGatewayService().trim().isEmpty()) {
            throw new AdapterException.ConfigurationException(AdapterType.RFC, "Gateway service is required");
        }
        
        // Set defaults
        if (config.getConnectionCount() <= 0) {
            config.setConnectionCount(1);
        }
    }
    
    @Override
    public String getConfigurationSummary() {
        return String.format("RFC Sender (Inbound): Program ID: %s, Gateway: %s:%s", 
                config.getProgramId(),
                config.getGatewayHost(),
                config.getGatewayService());
    }
}