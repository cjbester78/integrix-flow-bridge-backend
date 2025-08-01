package com.integrationlab.adapters.impl;

import com.integrationlab.adapters.core.*;
import com.integrationlab.adapters.config.IdocReceiverAdapterConfig;

import java.util.*;
import java.text.SimpleDateFormat;

/**
 * IDoc Receiver Adapter implementation for SAP IDoc sending (OUTBOUND).
 * Follows middleware convention: Receiver = sends data TO external systems.
 * Sends IDocs to SAP systems.
 * 
 * Note: This is a simulation. Real implementation would require SAP JCo IDoc libraries.
 */
public class IdocReceiverAdapter extends AbstractReceiverAdapter {
    
    private final IdocReceiverAdapterConfig config;
    private boolean connectionEstablished = false;
    
    public IdocReceiverAdapter(IdocReceiverAdapterConfig config) {
        super(AdapterType.IDOC);
        this.config = config;
    }
    
    @Override
    protected void doReceiverInitialize() throws Exception {
        logger.info("Initializing IDoc receiver adapter (outbound) for system: {}", config.getSystemId());
        
        validateConfiguration();
        
        // In real implementation, would:
        // 1. Initialize SAP JCo IDoc environment
        // 2. Create destination configuration
        // 3. Establish IDoc connection
        
        connectionEstablished = true;
        logger.info("IDoc receiver adapter initialized successfully");
    }
    
    @Override
    protected void doReceiverDestroy() throws Exception {
        logger.info("Destroying IDoc receiver adapter");
        
        if (connectionEstablished) {
            // In real implementation, would close IDoc connections
            connectionEstablished = false;
        }
    }
    
    @Override
    protected AdapterResult doTestConnection() throws Exception {
        List<AdapterResult> testResults = new ArrayList<>();
        
        // Test 1: SAP system connectivity for IDoc
        testResults.add(ConnectionTestUtil.executeBasicConnectivityTest(AdapterType.IDOC, () -> {
            try {
                String connectionInfo = String.format("System: %s, Host: %s:%s", 
                        config.getSystemId(), config.getApplicationServerHost(), config.getSystemNumber());
                
                if (config.getApplicationServerHost() == null || config.getApplicationServerHost().isEmpty()) {
                    return ConnectionTestUtil.createTestFailure(AdapterType.IDOC, 
                            "SAP Connection", "Application server host not configured", null);
                }
                
                return ConnectionTestUtil.createTestSuccess(AdapterType.IDOC, 
                        "SAP Connection", "IDoc connection configuration valid: " + connectionInfo);
                        
            } catch (Exception e) {
                return ConnectionTestUtil.createTestFailure(AdapterType.IDOC, 
                        "SAP Connection", "Failed to validate connection: " + e.getMessage(), e);
            }
        }));
        
        // Test 2: IDoc port configuration
        testResults.add(ConnectionTestUtil.executeConfigurationTest(AdapterType.IDOC, () -> {
            try {
                if (config.getIdocPort() == null || config.getIdocPort().isEmpty()) {
                    return ConnectionTestUtil.createTestFailure(AdapterType.IDOC, 
                            "IDoc Port", "IDoc port not configured", null);
                }
                
                return ConnectionTestUtil.createTestSuccess(AdapterType.IDOC, 
                        "IDoc Port", "IDoc port configured: " + config.getIdocPort());
                        
            } catch (Exception e) {
                return ConnectionTestUtil.createTestFailure(AdapterType.IDOC, 
                        "IDoc Port", "Invalid port configuration: " + e.getMessage(), e);
            }
        }));
        
        // Test 3: Packet size configuration
        testResults.add(ConnectionTestUtil.executeConfigurationTest(AdapterType.IDOC, () -> {
            try {
                String info = String.format("Packet size: %d, Queue processing: %s", 
                        config.getPacketSize(), 
                        config.isQueueProcessing() ? "Enabled" : "Disabled");
                
                return ConnectionTestUtil.createTestSuccess(AdapterType.IDOC, 
                        "Processing Config", info);
                        
            } catch (Exception e) {
                return ConnectionTestUtil.createTestFailure(AdapterType.IDOC, 
                        "Processing Config", "Invalid configuration: " + e.getMessage(), e);
            }
        }));
        
        return ConnectionTestUtil.combineTestResults(AdapterType.IDOC, 
                testResults.toArray(new AdapterResult[0]));
    }
    
    @Override
    protected AdapterResult doReceive(Object criteria) throws Exception {
        // For IDoc Receiver (outbound), this method sends IDocs TO SAP
        return sendIdoc(criteria);
    }
    
    protected AdapterResult doReceive() throws Exception {
        throw new AdapterException.OperationException(AdapterType.IDOC, 
                "IDoc Receiver requires IDoc payload");
    }
    
    private AdapterResult sendIdoc(Object payload) throws Exception {
        if (payload == null) {
            throw new AdapterException.ValidationException(AdapterType.IDOC, "Payload cannot be null");
        }
        
        try {
            Map<String, Object> responseData = new HashMap<>();
            List<String> sentIdocNumbers = new ArrayList<>();
            
            if (payload instanceof Map) {
                // Single IDoc
                Map<String, Object> idocResult = processSingleIdoc((Map<String, Object>) payload);
                sentIdocNumbers.add((String) idocResult.get("idocNumber"));
                responseData = idocResult;
                
            } else if (payload instanceof Collection) {
                // Multiple IDocs (packet)
                Collection<?> idocCollection = (Collection<?>) payload;
                List<Map<String, Object>> results = new ArrayList<>();
                
                for (Object idoc : idocCollection) {
                    if (idoc instanceof Map) {
                        Map<String, Object> idocResult = processSingleIdoc((Map<String, Object>) idoc);
                        results.add(idocResult);
                        sentIdocNumbers.add((String) idocResult.get("idocNumber"));
                        
                        // Check packet size
                        if (config.getPacketSize() > 0 && results.size() >= config.getPacketSize()) {
                            // Would commit packet in real implementation
                            logger.debug("Reached packet size limit: {}", config.getPacketSize());
                        }
                    }
                }
                
                responseData.put("results", results);
                responseData.put("totalSent", results.size());
                
            } else {
                throw new AdapterException.ValidationException(AdapterType.IDOC, 
                        "Unsupported payload type: " + payload.getClass().getName());
            }
            
            responseData.put("sentIdocNumbers", sentIdocNumbers);
            responseData.put("timestamp", new Date());
            responseData.put("system", config.getSystemId());
            
            logger.info("IDoc receiver adapter sent {} IDoc(s) to SAP", sentIdocNumbers.size());
            
            return AdapterResult.success(responseData, 
                    String.format("Successfully sent %d IDoc(s) to SAP", sentIdocNumbers.size()));
                    
        } catch (Exception e) {
            logger.error("Error sending IDoc", e);
            throw new AdapterException.OperationException(AdapterType.IDOC, 
                    "Failed to send IDoc: " + e.getMessage(), e);
        }
    }
    
    private Map<String, Object> processSingleIdoc(Map<String, Object> idocData) throws Exception {
        Map<String, Object> result = new HashMap<>();
        
        // Extract or create control record
        Map<String, Object> controlRecord = (Map<String, Object>) idocData.get("controlRecord");
        if (controlRecord == null) {
            controlRecord = createControlRecord(idocData);
        }
        
        // Set sender/receiver information
        updateControlRecord(controlRecord);
        
        // Extract data records
        List<Map<String, Object>> dataRecords = (List<Map<String, Object>>) idocData.get("dataRecords");
        if (dataRecords == null || dataRecords.isEmpty()) {
            throw new AdapterException.ValidationException(AdapterType.IDOC, 
                    "IDoc must contain at least one data record");
        }
        
        // In real implementation, would:
        // 1. Create IDoc document
        // 2. Set control record fields
        // 3. Add data records with proper segment structure
        // 4. Send IDoc to SAP
        // 5. Get IDoc number from response
        
        // Simulate sending
        String idocNumber = generateIdocNumber();
        result.put("idocNumber", idocNumber);
        result.put("idocType", controlRecord.get("IDOCTYP"));
        result.put("messageType", controlRecord.get("MESTYP"));
        result.put("status", "03"); // IDoc sent
        result.put("dataRecordCount", dataRecords.size());
        
        // Add status record
        Map<String, Object> statusRecord = new HashMap<>();
        statusRecord.put("STATUS", "03");
        statusRecord.put("STATXT", "IDoc sent to SAP system");
        statusRecord.put("CREDAT", new SimpleDateFormat("yyyyMMdd").format(new Date()));
        statusRecord.put("CRETIM", new SimpleDateFormat("HHmmss").format(new Date()));
        result.put("statusRecord", statusRecord);
        
        return result;
    }
    
    private Map<String, Object> createControlRecord(Map<String, Object> idocData) {
        Map<String, Object> control = new HashMap<>();
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMdd");
        SimpleDateFormat timeFormat = new SimpleDateFormat("HHmmss");
        Date now = new Date();
        
        // Extract from payload or use defaults
        control.put("TABNAM", "EDI_DC40");
        control.put("IDOCTYP", idocData.getOrDefault("idocType", config.getDefaultIdocType()));
        control.put("MESTYP", idocData.getOrDefault("messageType", config.getDefaultMessageType()));
        control.put("CREDAT", dateFormat.format(now));
        control.put("CRETIM", timeFormat.format(now));
        control.put("STATUS", "01"); // IDoc created
        
        return control;
    }
    
    private void updateControlRecord(Map<String, Object> controlRecord) {
        // Set sender information (middleware)
        controlRecord.put("SNDPOR", config.getIdocPort());
        controlRecord.put("SNDPRT", "LS");
        controlRecord.put("SNDPRN", config.getSenderPartner() != null ? 
                config.getSenderPartner() : "MIDDLEWARE");
        
        // Set receiver information (SAP)
        controlRecord.put("RCVPOR", "SAP" + config.getSystemId());
        controlRecord.put("RCVPRT", "LS");
        controlRecord.put("RCVPRN", config.getReceiverPartner() != null ? 
                config.getReceiverPartner() : config.getSystemId());
    }
    
    private String generateIdocNumber() {
        // Generate a 16-digit IDoc number
        return String.format("%016d", System.currentTimeMillis() % 10000000000000000L);
    }
    
    private void validateConfiguration() throws AdapterException.ConfigurationException {
        if (config.getSystemId() == null || config.getSystemId().trim().isEmpty()) {
            throw new AdapterException.ConfigurationException(AdapterType.IDOC, "System ID is required");
        }
        
        if (config.getApplicationServerHost() == null || config.getApplicationServerHost().trim().isEmpty()) {
            throw new AdapterException.ConfigurationException(AdapterType.IDOC, "Application server host is required");
        }
        
        if (config.getSystemNumber() == null || config.getSystemNumber().trim().isEmpty()) {
            throw new AdapterException.ConfigurationException(AdapterType.IDOC, "System number is required");
        }
        
        if (config.getClient() == null || config.getClient().trim().isEmpty()) {
            throw new AdapterException.ConfigurationException(AdapterType.IDOC, "Client is required");
        }
        
        if (config.getUser() == null || config.getUser().trim().isEmpty()) {
            throw new AdapterException.ConfigurationException(AdapterType.IDOC, "User is required");
        }
        
        if (config.getIdocPort() == null || config.getIdocPort().trim().isEmpty()) {
            throw new AdapterException.ConfigurationException(AdapterType.IDOC, "IDoc port is required");
        }
        
        // Set defaults
        if (config.getPacketSize() <= 0) {
            config.setPacketSize(1); // Default to single IDoc processing
        }
    }
    
    @Override
    protected long getPollingIntervalMs() {
        // IDoc receivers typically don't poll, they send IDocs
        return 0;
    }
    
    @Override
    public String getConfigurationSummary() {
        return String.format("IDoc Receiver (Outbound): System: %s, Port: %s, Packet Size: %d", 
                config.getSystemId(),
                config.getIdocPort(),
                config.getPacketSize());
    }
}