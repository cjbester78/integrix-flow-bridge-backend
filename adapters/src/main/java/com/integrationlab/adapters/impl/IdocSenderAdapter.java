package com.integrationlab.adapters.impl;

import com.integrationlab.adapters.core.*;
import com.integrationlab.adapters.config.IdocSenderAdapterConfig;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.text.SimpleDateFormat;

/**
 * IDoc Sender Adapter implementation for SAP IDoc receiving (INBOUND).
 * Follows middleware convention: Sender = receives data FROM external systems.
 * Receives IDocs from SAP systems.
 * 
 * Note: This is a simulation. Real implementation would require SAP JCo IDoc libraries.
 */
public class IdocSenderAdapter extends AbstractSenderAdapter {
    
    private final IdocSenderAdapterConfig config;
    private final Map<String, Object> receivedIdocs = new ConcurrentHashMap<>();
    private boolean serverStarted = false;
    
    public IdocSenderAdapter(IdocSenderAdapterConfig config) {
        super(AdapterType.IDOC);
        this.config = config;
    }
    
    @Override
    protected void doSenderInitialize() throws Exception {
        logger.info("Initializing IDoc sender adapter (inbound) with program ID: {}", config.getProgramId());
        
        validateConfiguration();
        
        // In real implementation, would:
        // 1. Initialize SAP JCo IDoc environment
        // 2. Create IDoc server instance
        // 3. Register IDoc handlers for specific IDoc types
        // 4. Start IDoc server
        
        serverStarted = true;
        logger.info("IDoc sender adapter initialized successfully");
    }
    
    @Override
    protected void doSenderDestroy() throws Exception {
        logger.info("Destroying IDoc sender adapter");
        
        if (serverStarted) {
            // In real implementation, would stop IDoc server
            serverStarted = false;
        }
        
        receivedIdocs.clear();
    }
    
    @Override
    protected AdapterResult doTestConnection() throws Exception {
        List<AdapterResult> testResults = new ArrayList<>();
        
        // Test 1: Gateway connectivity for IDoc
        testResults.add(ConnectionTestUtil.executeBasicConnectivityTest(AdapterType.IDOC, () -> {
            try {
                String gatewayInfo = String.format("Gateway: %s:%s", 
                        config.getGatewayHost(), config.getGatewayService());
                
                if (config.getGatewayHost() == null || config.getGatewayHost().isEmpty()) {
                    return ConnectionTestUtil.createTestFailure(AdapterType.IDOC, 
                            "Gateway Connection", "Gateway host not configured", null);
                }
                
                return ConnectionTestUtil.createTestSuccess(AdapterType.IDOC, 
                        "Gateway Connection", "IDoc gateway configuration valid: " + gatewayInfo);
                        
            } catch (Exception e) {
                return ConnectionTestUtil.createTestFailure(AdapterType.IDOC, 
                        "Gateway Connection", "Failed to validate gateway: " + e.getMessage(), e);
            }
        }));
        
        // Test 2: IDoc type configuration
        testResults.add(ConnectionTestUtil.executeConfigurationTest(AdapterType.IDOC, () -> {
            try {
                String idocTypes = config.getAllowedIdocTypes();
                String info = "IDoc server ready to receive IDocs";
                
                if (idocTypes != null && !idocTypes.isEmpty()) {
                    info += ", Allowed types: " + idocTypes;
                } else {
                    info += ", All IDoc types allowed";
                }
                
                return ConnectionTestUtil.createTestSuccess(AdapterType.IDOC, 
                        "IDoc Configuration", info);
                        
            } catch (Exception e) {
                return ConnectionTestUtil.createTestFailure(AdapterType.IDOC, 
                        "IDoc Configuration", "Invalid configuration: " + e.getMessage(), e);
            }
        }));
        
        // Test 3: TID management configuration
        testResults.add(ConnectionTestUtil.executeConfigurationTest(AdapterType.IDOC, () -> {
            try {
                String tidMode = config.isEnableTidManagement() ? "Enabled" : "Disabled";
                
                return ConnectionTestUtil.createTestSuccess(AdapterType.IDOC, 
                        "TID Management", "Transaction ID management: " + tidMode);
                        
            } catch (Exception e) {
                return ConnectionTestUtil.createTestFailure(AdapterType.IDOC, 
                        "TID Management", "TID configuration error: " + e.getMessage(), e);
            }
        }));
        
        return ConnectionTestUtil.combineTestResults(AdapterType.IDOC, 
                testResults.toArray(new AdapterResult[0]));
    }
    
    @Override
    protected AdapterResult doSend(Object payload, Map<String, Object> headers) throws Exception {
        // For IDoc Sender (inbound), this simulates receiving IDocs from SAP
        return receiveIdoc(payload, headers);
    }
    
    private AdapterResult receiveIdoc(Object payload, Map<String, Object> headers) throws Exception {
        Map<String, Object> idocData = new HashMap<>();
        
        if (payload instanceof Map) {
            Map<String, Object> incomingIdoc = (Map<String, Object>) payload;
            
            // Extract IDoc control record
            Map<String, Object> controlRecord = (Map<String, Object>) incomingIdoc.get("controlRecord");
            if (controlRecord == null) {
                controlRecord = createDefaultControlRecord();
            }
            
            String idocType = (String) controlRecord.get("IDOCTYP");
            String messageType = (String) controlRecord.get("MESTYP");
            
            // Check if IDoc type is allowed
            if (config.getAllowedIdocTypes() != null && !config.getAllowedIdocTypes().isEmpty()) {
                List<String> allowedTypes = Arrays.asList(config.getAllowedIdocTypes().split(","));
                if (!allowedTypes.contains(idocType)) {
                    throw new AdapterException.ValidationException(AdapterType.IDOC, 
                            "IDoc type not allowed: " + idocType);
                }
            }
            
            // Build IDoc data structure
            idocData.put("idocNumber", generateIdocNumber());
            idocData.put("controlRecord", controlRecord);
            idocData.put("timestamp", new Date());
            
            // Data records
            List<Map<String, Object>> dataRecords = (List<Map<String, Object>>) incomingIdoc.get("dataRecords");
            if (dataRecords == null) {
                dataRecords = new ArrayList<>();
            }
            idocData.put("dataRecords", dataRecords);
            idocData.put("dataRecordCount", dataRecords.size());
            
            // Status records
            List<Map<String, Object>> statusRecords = new ArrayList<>();
            statusRecords.add(createStatusRecord("53", "IDoc received successfully"));
            idocData.put("statusRecords", statusRecords);
            
            // Transaction ID for exactly-once delivery
            if (config.isEnableTidManagement()) {
                String tid = (String) headers.get("transactionId");
                if (tid == null) {
                    tid = UUID.randomUUID().toString();
                }
                idocData.put("transactionId", tid);
            }
            
            // Store received IDoc
            receivedIdocs.put((String) idocData.get("idocNumber"), idocData);
            
            logger.info("IDoc sender adapter received IDoc: {} of type: {}", 
                    idocData.get("idocNumber"), idocType);
            
            return AdapterResult.success(idocData, 
                    String.format("Successfully received IDoc: %s", idocData.get("idocNumber")));
                    
        } else {
            // Simple simulation
            idocData.put("idocNumber", generateIdocNumber());
            idocData.put("timestamp", new Date());
            idocData.put("payload", payload);
            idocData.put("controlRecord", createDefaultControlRecord());
            
            return AdapterResult.success(idocData, "Successfully received IDoc");
        }
    }
    
    private Map<String, Object> createDefaultControlRecord() {
        Map<String, Object> control = new HashMap<>();
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMdd");
        SimpleDateFormat timeFormat = new SimpleDateFormat("HHmmss");
        Date now = new Date();
        
        control.put("TABNAM", "EDI_DC40");
        control.put("IDOCTYP", "ORDERS05");
        control.put("MESTYP", "ORDERS");
        control.put("SNDPRT", "LS");
        control.put("SNDPRN", "SENDER");
        control.put("RCVPRT", "LS");
        control.put("RCVPRN", "RECEIVER");
        control.put("CREDAT", dateFormat.format(now));
        control.put("CRETIM", timeFormat.format(now));
        control.put("STATUS", "53");
        
        return control;
    }
    
    private Map<String, Object> createStatusRecord(String status, String message) {
        Map<String, Object> statusRecord = new HashMap<>();
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMdd");
        SimpleDateFormat timeFormat = new SimpleDateFormat("HHmmss");
        Date now = new Date();
        
        statusRecord.put("STATUS", status);
        statusRecord.put("STATXT", message);
        statusRecord.put("CREDAT", dateFormat.format(now));
        statusRecord.put("CRETIM", timeFormat.format(now));
        
        return statusRecord;
    }
    
    private String generateIdocNumber() {
        // Generate a 16-digit IDoc number
        return String.format("%016d", System.currentTimeMillis() % 10000000000000000L);
    }
    
    private void validateConfiguration() throws AdapterException.ConfigurationException {
        if (config.getProgramId() == null || config.getProgramId().trim().isEmpty()) {
            throw new AdapterException.ConfigurationException(AdapterType.IDOC, "Program ID is required");
        }
        
        if (config.getGatewayHost() == null || config.getGatewayHost().trim().isEmpty()) {
            throw new AdapterException.ConfigurationException(AdapterType.IDOC, "Gateway host is required");
        }
        
        if (config.getGatewayService() == null || config.getGatewayService().trim().isEmpty()) {
            throw new AdapterException.ConfigurationException(AdapterType.IDOC, "Gateway service is required");
        }
    }
    
    protected long getPollingIntervalMs() {
        return config.getPollingInterval();
    }
    
    @Override
    public String getConfigurationSummary() {
        return String.format("IDoc Sender (Inbound): Program ID: %s, Gateway: %s:%s", 
                config.getProgramId(),
                config.getGatewayHost(),
                config.getGatewayService());
    }
}