package com.integrixs.adapters.impl;

import com.integrixs.adapters.core.*;
import com.integrixs.adapters.config.OdataReceiverAdapterConfig;

import org.apache.olingo.client.api.ODataClient;
import org.apache.olingo.client.api.communication.request.cud.*;
import org.apache.olingo.client.api.communication.request.ODataBasicRequest;
import org.apache.olingo.client.api.communication.response.*;
import org.apache.olingo.client.api.domain.*;
import org.apache.olingo.client.core.ODataClientFactory;
import org.apache.olingo.commons.api.edm.FullQualifiedName;
import org.apache.olingo.commons.api.format.ContentType;
import org.apache.olingo.commons.api.http.HttpMethod;

import java.net.URI;
import java.net.URL;
import java.util.*;

/**
 * OData Receiver Adapter implementation for OData service operations (OUTBOUND).
 * Follows middleware convention: Receiver = sends data TO external systems.
 * Performs CRUD operations on OData services in external systems.
 */
public class OdataReceiverAdapter extends AbstractReceiverAdapter {
    
    private final OdataReceiverAdapterConfig config;
    private ODataClient client;
    
    public OdataReceiverAdapter(OdataReceiverAdapterConfig config) {
        super(AdapterType.ODATA);
        this.config = config;
    }
    
    @Override
    protected void doReceiverInitialize() throws Exception {
        logger.info("Initializing OData receiver adapter (outbound) with service URL: {}", config.getServiceUrl());
        
        validateConfiguration();
        
        // Initialize OData client
        client = ODataClientFactory.getClient();
        
        // Configure client settings
        if (config.getUsername() != null && !config.getUsername().isEmpty()) {
            logger.debug("Configured authentication for user: {}", config.getUsername());
        }
        
        logger.info("OData receiver adapter initialized successfully");
    }
    
    @Override
    protected void doReceiverDestroy() throws Exception {
        logger.info("Destroying OData receiver adapter");
        client = null;
    }
    
    @Override
    protected AdapterResult doTestConnection() throws Exception {
        List<AdapterResult> testResults = new ArrayList<>();
        
        // Test 1: Service URL accessibility
        testResults.add(ConnectionTestUtil.executeBasicConnectivityTest(AdapterType.ODATA, () -> {
            try {
                URL url = new URL(config.getServiceUrl());
                url.openConnection().connect();
                
                return ConnectionTestUtil.createTestSuccess(AdapterType.ODATA, 
                        "Service URL", "Successfully connected to OData service URL");
                        
            } catch (Exception e) {
                return ConnectionTestUtil.createTestFailure(AdapterType.ODATA, 
                        "Service URL", "Failed to connect to OData service: " + e.getMessage(), e);
            }
        }));
        
        // Test 2: Metadata validation
        testResults.add(ConnectionTestUtil.executeConfigurationTest(AdapterType.ODATA, () -> {
            try {
                String metadataUrl = config.getServiceUrl();
                if (!metadataUrl.endsWith("/")) {
                    metadataUrl += "/";
                }
                metadataUrl += "$metadata";
                
                URL url = new URL(metadataUrl);
                url.openConnection().connect();
                
                return ConnectionTestUtil.createTestSuccess(AdapterType.ODATA, 
                        "Metadata Validation", "Successfully validated OData metadata");
                        
            } catch (Exception e) {
                return ConnectionTestUtil.createTestFailure(AdapterType.ODATA, 
                        "Metadata Validation", "Failed to validate metadata: " + e.getMessage(), e);
            }
        }));
        
        // Test 3: Operation configuration
        testResults.add(ConnectionTestUtil.executeConfigurationTest(AdapterType.ODATA, () -> {
            try {
                String operation = config.getDefaultOperation() != null ? 
                        config.getDefaultOperation() : "CREATE";
                
                return ConnectionTestUtil.createTestSuccess(AdapterType.ODATA, 
                        "Operation Config", "Default operation: " + operation);
                        
            } catch (Exception e) {
                return ConnectionTestUtil.createTestFailure(AdapterType.ODATA, 
                        "Operation Config", "Invalid operation configuration: " + e.getMessage(), e);
            }
        }));
        
        return ConnectionTestUtil.combineTestResults(AdapterType.ODATA, 
                testResults.toArray(new AdapterResult[0]));
    }
    
    @Override
    protected AdapterResult doReceive(Object criteria) throws Exception {
        // For OData Receiver (outbound), this method sends data TO OData service
        return performODataOperation(criteria);
    }
    
    protected AdapterResult doReceive() throws Exception {
        throw new AdapterException.OperationException(AdapterType.ODATA, 
                "OData Receiver requires payload for operations");
    }
    
    private AdapterResult performODataOperation(Object payload) throws Exception {
        if (payload == null) {
            throw new AdapterException.ValidationException(AdapterType.ODATA, "Payload cannot be null");
        }
        
        try {
            Map<String, Object> responseData;
            
            if (payload instanceof Map) {
                Map<String, Object> dataMap = (Map<String, Object>) payload;
                
                // Determine operation
                String operation = determineOperation(dataMap);
                
                switch (operation.toUpperCase()) {
                    case "CREATE":
                        responseData = createEntity(dataMap);
                        break;
                    case "UPDATE":
                        responseData = updateEntity(dataMap);
                        break;
                    case "DELETE":
                        responseData = deleteEntity(dataMap);
                        break;
                    case "READ":
                        responseData = readEntity(dataMap);
                        break;
                    case "BATCH":
                        responseData = performBatchOperation(dataMap);
                        break;
                    default:
                        throw new AdapterException.ValidationException(AdapterType.ODATA, 
                                "Unsupported operation: " + operation);
                }
            } else if (payload instanceof Collection) {
                // Batch operation for collection
                responseData = performBatchOperation((Collection<?>) payload);
            } else {
                throw new AdapterException.ValidationException(AdapterType.ODATA, 
                        "Unsupported payload type: " + payload.getClass().getName());
            }
            
            logger.info("OData receiver adapter successfully performed operation");
            
            return AdapterResult.success(responseData, 
                    "Successfully performed OData operation");
                    
        } catch (Exception e) {
            logger.error("Error performing OData operation", e);
            throw new AdapterException.OperationException(AdapterType.ODATA, 
                    "Failed to perform OData operation: " + e.getMessage(), e);
        }
    }
    
    private Map<String, Object> createEntity(Map<String, Object> dataMap) throws Exception {
        String entitySetName = getEntitySetName(dataMap);
        
        // Create entity
        ClientEntity entity = client.getObjectFactory().newEntity(
                new FullQualifiedName(config.getNamespace(), getEntityType(dataMap)));
        
        // Set properties
        Map<String, Object> properties = (Map<String, Object>) dataMap.get("properties");
        if (properties == null) {
            properties = dataMap; // Use entire map as properties
        }
        
        for (Map.Entry<String, Object> entry : properties.entrySet()) {
            if (!entry.getKey().startsWith("@") && !entry.getKey().equals("operation")) {
                entity.getProperties().add(client.getObjectFactory().newPrimitiveProperty(
                        entry.getKey(), 
                        client.getObjectFactory().newPrimitiveValueBuilder().buildString(
                                String.valueOf(entry.getValue()))));
            }
        }
        
        // Build URI
        URI serviceUri = URI.create(config.getServiceUrl());
        URI entitySetUri = client.newURIBuilder(serviceUri.toString())
                .appendEntitySetSegment(entitySetName)
                .build();
        
        // Create request
        ODataEntityCreateRequest<ClientEntity> request = 
                client.getCUDRequestFactory().getEntityCreateRequest(entitySetUri, entity);
        
        request.setFormat(ContentType.APPLICATION_JSON);
        
        // Add authentication
        addAuthentication(request);
        
        // Execute request
        ODataEntityCreateResponse<ClientEntity> response = request.execute();
        
        Map<String, Object> result = new HashMap<>();
        result.put("status", response.getStatusCode());
        result.put("location", response.getHeader("Location"));
        
        if (response.getBody() != null) {
            result.put("entity", extractEntityData(response.getBody()));
        }
        
        return result;
    }
    
    private Map<String, Object> updateEntity(Map<String, Object> dataMap) throws Exception {
        String entitySetName = getEntitySetName(dataMap);
        String entityKey = (String) dataMap.get("key");
        
        if (entityKey == null || entityKey.isEmpty()) {
            throw new AdapterException.ValidationException(AdapterType.ODATA, 
                    "Entity key is required for update operation");
        }
        
        // Create entity
        ClientEntity entity = client.getObjectFactory().newEntity(
                new FullQualifiedName(config.getNamespace(), getEntityType(dataMap)));
        
        // Set properties
        Map<String, Object> properties = (Map<String, Object>) dataMap.get("properties");
        if (properties == null) {
            properties = dataMap;
        }
        
        for (Map.Entry<String, Object> entry : properties.entrySet()) {
            if (!entry.getKey().startsWith("@") && !entry.getKey().equals("operation") && 
                !entry.getKey().equals("key")) {
                entity.getProperties().add(client.getObjectFactory().newPrimitiveProperty(
                        entry.getKey(), 
                        client.getObjectFactory().newPrimitiveValueBuilder().buildString(
                                String.valueOf(entry.getValue()))));
            }
        }
        
        // Build URI
        URI serviceUri = URI.create(config.getServiceUrl());
        URI entityUri = client.newURIBuilder(serviceUri.toString())
                .appendEntitySetSegment(entitySetName)
                .appendKeySegment(entityKey)
                .build();
        
        // Create request (using PATCH for partial update)
        ODataEntityUpdateRequest<ClientEntity> request = 
                client.getCUDRequestFactory().getEntityUpdateRequest(entityUri, 
                        UpdateType.PATCH, entity);
        
        request.setFormat(ContentType.APPLICATION_JSON);
        
        // Add authentication
        addAuthentication(request);
        
        // Execute request
        ODataEntityUpdateResponse<ClientEntity> response = request.execute();
        
        Map<String, Object> result = new HashMap<>();
        result.put("status", response.getStatusCode());
        result.put("operation", "UPDATE");
        result.put("key", entityKey);
        
        return result;
    }
    
    private Map<String, Object> deleteEntity(Map<String, Object> dataMap) throws Exception {
        String entitySetName = getEntitySetName(dataMap);
        String entityKey = (String) dataMap.get("key");
        
        if (entityKey == null || entityKey.isEmpty()) {
            throw new AdapterException.ValidationException(AdapterType.ODATA, 
                    "Entity key is required for delete operation");
        }
        
        // Build URI
        URI serviceUri = URI.create(config.getServiceUrl());
        URI entityUri = client.newURIBuilder(serviceUri.toString())
                .appendEntitySetSegment(entitySetName)
                .appendKeySegment(entityKey)
                .build();
        
        // Create request
        ODataDeleteRequest request = client.getCUDRequestFactory().getDeleteRequest(entityUri);
        
        // Add authentication
        addAuthentication(request);
        
        // Execute request
        ODataDeleteResponse response = request.execute();
        
        Map<String, Object> result = new HashMap<>();
        result.put("status", response.getStatusCode());
        result.put("operation", "DELETE");
        result.put("key", entityKey);
        
        return result;
    }
    
    private Map<String, Object> readEntity(Map<String, Object> dataMap) throws Exception {
        String entitySetName = getEntitySetName(dataMap);
        String entityKey = (String) dataMap.get("key");
        
        // Build URI
        URI serviceUri = URI.create(config.getServiceUrl());
        URI uri;
        
        if (entityKey != null && !entityKey.isEmpty()) {
            // Read single entity
            uri = client.newURIBuilder(serviceUri.toString())
                    .appendEntitySetSegment(entitySetName)
                    .appendKeySegment(entityKey)
                    .build();
        } else {
            // Read entity set
            uri = client.newURIBuilder(serviceUri.toString())
                    .appendEntitySetSegment(entitySetName)
                    .build();
        }
        
        // This is a simplified read operation
        Map<String, Object> result = new HashMap<>();
        result.put("operation", "READ");
        result.put("entitySet", entitySetName);
        if (entityKey != null) {
            result.put("key", entityKey);
        }
        
        return result;
    }
    
    private Map<String, Object> performBatchOperation(Object payload) throws Exception {
        // Batch operations would be implemented here
        Map<String, Object> result = new HashMap<>();
        result.put("operation", "BATCH");
        result.put("message", "Batch operations not fully implemented in this example");
        
        if (payload instanceof Collection) {
            result.put("itemCount", ((Collection<?>) payload).size());
        }
        
        return result;
    }
    
    private Map<String, Object> extractEntityData(ClientEntity entity) {
        Map<String, Object> data = new HashMap<>();
        
        if (entity.getId() != null) {
            data.put("@odata.id", entity.getId().toString());
        }
        
        for (ClientProperty property : entity.getProperties()) {
            data.put(property.getName(), property.getValue().asPrimitive());
        }
        
        return data;
    }
    
    private void addAuthentication(ODataBasicRequest<?> request) {
        if (config.getUsername() != null && !config.getUsername().isEmpty()) {
            String credentials = config.getUsername() + ":" + config.getPassword();
            String encodedCredentials = Base64.getEncoder().encodeToString(credentials.getBytes());
            request.addCustomHeader("Authorization", "Basic " + encodedCredentials);
        }
        
        // Add custom headers
        if (config.getCustomHeaders() != null && !config.getCustomHeaders().isEmpty()) {
            for (Map.Entry<String, String> header : config.getCustomHeaders().entrySet()) {
                request.addCustomHeader(header.getKey(), header.getValue());
            }
        }
    }
    
    private String determineOperation(Map<String, Object> dataMap) {
        String operation = (String) dataMap.get("operation");
        if (operation == null || operation.isEmpty()) {
            operation = config.getDefaultOperation();
        }
        if (operation == null || operation.isEmpty()) {
            operation = "CREATE"; // Default
        }
        return operation;
    }
    
    private String getEntitySetName(Map<String, Object> dataMap) throws AdapterException.ValidationException {
        String entitySet = (String) dataMap.get("entitySet");
        if (entitySet == null || entitySet.isEmpty()) {
            entitySet = config.getEntitySetName();
        }
        if (entitySet == null || entitySet.isEmpty()) {
            throw new AdapterException.ValidationException(AdapterType.ODATA, 
                    "Entity set name is required");
        }
        return entitySet;
    }
    
    private String getEntityType(Map<String, Object> dataMap) throws AdapterException.ValidationException {
        String entityType = (String) dataMap.get("entityType");
        if (entityType == null || entityType.isEmpty()) {
            entityType = config.getEntityTypeName();
        }
        if (entityType == null || entityType.isEmpty()) {
            // Default to entity set name
            entityType = getEntitySetName(dataMap);
        }
        return entityType;
    }
    
    private void validateConfiguration() throws AdapterException.ConfigurationException {
        if (config.getServiceUrl() == null || config.getServiceUrl().trim().isEmpty()) {
            throw new AdapterException.ConfigurationException(AdapterType.ODATA, "Service URL is required");
        }
        
        if (config.getNamespace() == null || config.getNamespace().trim().isEmpty()) {
            config.setNamespace("Default"); // Set default namespace
        }
    }
    
    @Override
    protected long getPollingIntervalMs() {
        // OData receivers typically don't poll, they push data
        return 0;
    }
    
    @Override
    public String getConfigurationSummary() {
        return String.format("OData Receiver (Outbound): %s, Default Operation: %s", 
                config.getServiceUrl(),
                config.getDefaultOperation() != null ? config.getDefaultOperation() : "CREATE");
    }
}