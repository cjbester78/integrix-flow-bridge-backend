package com.integrixs.adapters.impl;

import com.integrixs.adapters.core.*;
import com.integrixs.adapters.config.RfcReceiverAdapterConfig;

import java.util.*;

/**
 * RFC Receiver Adapter implementation for SAP RFC client functionality (OUTBOUND).
 * Follows middleware convention: Receiver = sends data TO external systems.
 * Makes RFC calls to SAP systems.
 * 
 * Note: This is a simulation. Real implementation would require SAP JCo libraries.
 */
public class RfcReceiverAdapter extends AbstractReceiverAdapter {
    
    private final RfcReceiverAdapterConfig config;
    private boolean connectionEstablished = false;
    
    public RfcReceiverAdapter(RfcReceiverAdapterConfig config) {
        super(AdapterType.RFC);
        this.config = config;
    }
    
    @Override
    protected void doReceiverInitialize() throws Exception {
        logger.info("Initializing RFC receiver adapter (outbound) for system: {}", config.getSystemId());
        
        validateConfiguration();
        
        // In real implementation, would:
        // 1. Initialize SAP JCo environment
        // 2. Create destination configuration
        // 3. Establish connection pool
        
        connectionEstablished = true;
        logger.info("RFC receiver adapter initialized successfully");
    }
    
    @Override
    protected void doReceiverDestroy() throws Exception {
        logger.info("Destroying RFC receiver adapter");
        
        if (connectionEstablished) {
            // In real implementation, would close connections and cleanup
            connectionEstablished = false;
        }
    }
    
    @Override
    protected AdapterResult doTestConnection() throws Exception {
        List<AdapterResult> testResults = new ArrayList<>();
        
        // Test 1: SAP system connectivity
        testResults.add(ConnectionTestUtil.executeBasicConnectivityTest(AdapterType.RFC, () -> {
            try {
                // Simulate connection test
                String connectionInfo = String.format("System: %s, Host: %s:%s", 
                        config.getSystemId(), config.getApplicationServerHost(), config.getSystemNumber());
                
                if (config.getApplicationServerHost() == null || config.getApplicationServerHost().isEmpty()) {
                    return ConnectionTestUtil.createTestFailure(AdapterType.RFC, 
                            "SAP Connection", "Application server host not configured", null);
                }
                
                return ConnectionTestUtil.createTestSuccess(AdapterType.RFC, 
                        "SAP Connection", "SAP system configuration valid: " + connectionInfo);
                        
            } catch (Exception e) {
                return ConnectionTestUtil.createTestFailure(AdapterType.RFC, 
                        "SAP Connection", "Failed to validate SAP connection: " + e.getMessage(), e);
            }
        }));
        
        // Test 2: Authentication validation
        testResults.add(ConnectionTestUtil.executeConfigurationTest(AdapterType.RFC, () -> {
            try {
                if (config.getUser() == null || config.getUser().isEmpty()) {
                    return ConnectionTestUtil.createTestFailure(AdapterType.RFC, 
                            "Authentication", "SAP user not configured", null);
                }
                
                return ConnectionTestUtil.createTestSuccess(AdapterType.RFC, 
                        "Authentication", "Authentication configured for user: " + config.getUser());
                        
            } catch (Exception e) {
                return ConnectionTestUtil.createTestFailure(AdapterType.RFC, 
                        "Authentication", "Invalid authentication: " + e.getMessage(), e);
            }
        }));
        
        // Test 3: Connection pool configuration
        testResults.add(ConnectionTestUtil.executeConfigurationTest(AdapterType.RFC, () -> {
            try {
                String poolInfo = String.format("Pool size: %d, Peak limit: %d", 
                        config.getPoolCapacity(), config.getPeakLimit());
                
                return ConnectionTestUtil.createTestSuccess(AdapterType.RFC, 
                        "Connection Pool", poolInfo);
                        
            } catch (Exception e) {
                return ConnectionTestUtil.createTestFailure(AdapterType.RFC, 
                        "Connection Pool", "Invalid pool configuration: " + e.getMessage(), e);
            }
        }));
        
        return ConnectionTestUtil.combineTestResults(AdapterType.RFC, 
                testResults.toArray(new AdapterResult[0]));
    }
    
    @Override
    protected AdapterResult doReceive(Object criteria) throws Exception {
        // For RFC Receiver (outbound), this method sends RFC calls TO SAP
        return executeRfcCall(criteria);
    }
    
    protected AdapterResult doReceive() throws Exception {
        throw new AdapterException.OperationException(AdapterType.RFC, 
                "RFC Receiver requires function call payload");
    }
    
    private AdapterResult executeRfcCall(Object payload) throws Exception {
        if (payload == null) {
            throw new AdapterException.ValidationException(AdapterType.RFC, "Payload cannot be null");
        }
        
        try {
            Map<String, Object> responseData = new HashMap<>();
            
            if (payload instanceof Map) {
                Map<String, Object> callData = (Map<String, Object>) payload;
                
                // Extract function name
                String functionName = (String) callData.get("functionName");
                if (functionName == null || functionName.isEmpty()) {
                    functionName = config.getDefaultFunction();
                }
                if (functionName == null || functionName.isEmpty()) {
                    throw new AdapterException.ValidationException(AdapterType.RFC, 
                            "Function name is required");
                }
                
                // In real implementation, would:
                // 1. Get destination from JCo destination manager
                // 2. Create function object from repository
                // 3. Set import parameters
                // 4. Set table parameters
                // 5. Execute function
                // 6. Get export parameters and tables
                
                // Simulate RFC call
                responseData.put("functionName", functionName);
                responseData.put("executionId", UUID.randomUUID().toString());
                responseData.put("timestamp", new Date());
                responseData.put("system", config.getSystemId());
                
                // Import parameters
                Map<String, Object> importParams = (Map<String, Object>) callData.get("importParameters");
                if (importParams != null) {
                    responseData.put("importParameters", importParams);
                }
                
                // Simulate export parameters
                Map<String, Object> exportParams = new HashMap<>();
                exportParams.put("EV_RESULT", "SUCCESS");
                exportParams.put("EV_MESSAGE", "RFC executed successfully");
                exportParams.put("EV_TIMESTAMP", new Date().toString());
                responseData.put("exportParameters", exportParams);
                
                // Table parameters
                Map<String, List<Map<String, Object>>> tableParams = 
                        (Map<String, List<Map<String, Object>>>) callData.get("tableParameters");
                if (tableParams != null) {
                    // Process tables
                    Map<String, Object> outputTables = new HashMap<>();
                    for (Map.Entry<String, List<Map<String, Object>>> entry : tableParams.entrySet()) {
                        outputTables.put(entry.getKey(), "Processed " + entry.getValue().size() + " rows");
                    }
                    responseData.put("tableResults", outputTables);
                }
                
                // Handle specific function types
                if (functionName.startsWith("BAPI_")) {
                    // BAPI call - add return structure
                    Map<String, Object> bapiReturn = new HashMap<>();
                    bapiReturn.put("TYPE", "S");
                    bapiReturn.put("MESSAGE", "BAPI executed successfully");
                    bapiReturn.put("NUMBER", "000");
                    responseData.put("RETURN", bapiReturn);
                    
                    // Check for commit
                    Boolean commit = (Boolean) callData.get("commit");
                    if (Boolean.TRUE.equals(commit)) {
                        responseData.put("BAPI_TRANSACTION_COMMIT", "Executed");
                    }
                }
                
            } else {
                // Simple payload
                responseData.put("functionName", config.getDefaultFunction());
                responseData.put("executionId", UUID.randomUUID().toString());
                responseData.put("timestamp", new Date());
                responseData.put("payload", payload);
                responseData.put("result", "SUCCESS");
            }
            
            logger.info("RFC receiver adapter executed function: {}", responseData.get("functionName"));
            
            return AdapterResult.success(responseData, 
                    String.format("Successfully executed RFC: %s", responseData.get("functionName")));
                    
        } catch (Exception e) {
            logger.error("Error executing RFC call", e);
            throw new AdapterException.OperationException(AdapterType.RFC, 
                    "Failed to execute RFC: " + e.getMessage(), e);
        }
    }
    
    private void validateConfiguration() throws AdapterException.ConfigurationException {
        if (config.getSystemId() == null || config.getSystemId().trim().isEmpty()) {
            throw new AdapterException.ConfigurationException(AdapterType.RFC, "System ID is required");
        }
        
        if (config.getApplicationServerHost() == null || config.getApplicationServerHost().trim().isEmpty()) {
            throw new AdapterException.ConfigurationException(AdapterType.RFC, "Application server host is required");
        }
        
        if (config.getSystemNumber() == null || config.getSystemNumber().trim().isEmpty()) {
            throw new AdapterException.ConfigurationException(AdapterType.RFC, "System number is required");
        }
        
        if (config.getClient() == null || config.getClient().trim().isEmpty()) {
            throw new AdapterException.ConfigurationException(AdapterType.RFC, "Client is required");
        }
        
        if (config.getUser() == null || config.getUser().trim().isEmpty()) {
            throw new AdapterException.ConfigurationException(AdapterType.RFC, "User is required");
        }
        
        // Set defaults
        if (config.getPoolCapacity() <= 0) {
            config.setPoolCapacity(5);
        }
        
        if (config.getPeakLimit() <= 0) {
            config.setPeakLimit(10);
        }
    }
    
    @Override
    protected long getPollingIntervalMs() {
        // RFC receivers typically don't poll, they execute functions
        return 0;
    }
    
    @Override
    public String getConfigurationSummary() {
        return String.format("RFC Receiver (Outbound): System: %s, Host: %s:%s, Client: %s", 
                config.getSystemId(),
                config.getApplicationServerHost(),
                config.getSystemNumber(),
                config.getClient());
    }
}