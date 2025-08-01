package com.integrationlab.adapters.impl;

import com.integrationlab.adapters.core.*;
import com.integrationlab.adapters.config.OdataSenderAdapterConfig;

import org.apache.olingo.client.api.ODataClient;
import org.apache.olingo.client.api.communication.request.retrieve.ODataEntitySetRequest;
import org.apache.olingo.client.api.communication.response.ODataRetrieveResponse;
import org.apache.olingo.client.api.domain.ClientEntity;
import org.apache.olingo.client.api.domain.ClientEntitySet;
import org.apache.olingo.client.api.domain.ClientProperty;
import org.apache.olingo.client.core.ODataClientFactory;
import org.apache.olingo.commons.api.format.ContentType;

import java.net.URI;
import java.net.URL;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * OData Sender Adapter implementation for OData service consumption (INBOUND).
 * Follows middleware convention: Sender = receives data FROM external systems.
 * Polls OData services and retrieves entities from external systems.
 */
public class OdataSenderAdapter extends AbstractSenderAdapter {
    
    private final OdataSenderAdapterConfig config;
    private ODataClient client;
    private final Map<String, String> processedEntities = new ConcurrentHashMap<>();
    private String lastDeltaToken;
    
    public OdataSenderAdapter(OdataSenderAdapterConfig config) {
        super(AdapterType.ODATA);
        this.config = config;
    }
    
    @Override
    protected void doSenderInitialize() throws Exception {
        logger.info("Initializing OData sender adapter (inbound) with service URL: {}", config.getServiceUrl());
        
        validateConfiguration();
        
        // Initialize OData client
        client = ODataClientFactory.getClient();
        
        // Configure client settings
        if (config.getUsername() != null && !config.getUsername().isEmpty()) {
            // Basic authentication would be configured here
            logger.debug("Configured authentication for user: {}", config.getUsername());
        }
        
        logger.info("OData sender adapter initialized successfully");
    }
    
    @Override
    protected void doSenderDestroy() throws Exception {
        logger.info("Destroying OData sender adapter");
        processedEntities.clear();
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
        
        // Test 2: Metadata document accessibility
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
                        "Metadata Access", "Successfully accessed OData metadata document");
                        
            } catch (Exception e) {
                return ConnectionTestUtil.createTestFailure(AdapterType.ODATA, 
                        "Metadata Access", "Failed to access metadata: " + e.getMessage(), e);
            }
        }));
        
        // Test 3: Entity set validation
        if (config.getEntitySetName() != null && !config.getEntitySetName().isEmpty()) {
            testResults.add(ConnectionTestUtil.executeConfigurationTest(AdapterType.ODATA, () -> {
                try {
                    String info = String.format("Entity Set: %s", config.getEntitySetName());
                    if (config.getFilter() != null) {
                        info += String.format(", Filter: %s", config.getFilter());
                    }
                    
                    return ConnectionTestUtil.createTestSuccess(AdapterType.ODATA, 
                            "Entity Configuration", info);
                            
                } catch (Exception e) {
                    return ConnectionTestUtil.createTestFailure(AdapterType.ODATA, 
                            "Entity Configuration", "Invalid entity configuration: " + e.getMessage(), e);
                }
            }));
        }
        
        return ConnectionTestUtil.combineTestResults(AdapterType.ODATA, 
                testResults.toArray(new AdapterResult[0]));
    }
    
    @Override
    protected AdapterResult doSend(Object payload, Map<String, Object> headers) throws Exception {
        // For OData Sender (inbound), this method polls/retrieves data FROM OData service
        return pollODataService();
    }
    
    private AdapterResult pollODataService() throws Exception {
        List<Map<String, Object>> entities = new ArrayList<>();
        
        try {
            // Build OData URI
            String serviceUrl = config.getServiceUrl();
            
            // Create entity set request
            ODataEntitySetRequest<ClientEntitySet> request = client.getRetrieveRequestFactory()
                    .getEntitySetRequest(client.newURIBuilder(serviceUrl)
                            .appendEntitySetSegment(config.getEntitySetName())
                            .build());
            
            // Set format
            request.setFormat(ContentType.APPLICATION_JSON);
            
            // Add query options
            URI requestUri = addQueryOptions(request.getURI());
            request = client.getRetrieveRequestFactory().getEntitySetRequest(requestUri);
            
            // Add authentication headers if configured
            if (config.getUsername() != null && !config.getUsername().isEmpty()) {
                String credentials = config.getUsername() + ":" + config.getPassword();
                String encodedCredentials = Base64.getEncoder().encodeToString(credentials.getBytes());
                request.addCustomHeader("Authorization", "Basic " + encodedCredentials);
            }
            
            // Add custom headers if configured
            if (config.getCustomHeaders() != null && !config.getCustomHeaders().isEmpty()) {
                for (Map.Entry<String, String> header : config.getCustomHeaders().entrySet()) {
                    request.addCustomHeader(header.getKey(), header.getValue());
                }
            }
            
            // Execute request
            ODataRetrieveResponse<ClientEntitySet> response = request.execute();
            ClientEntitySet entitySet = response.getBody();
            
            // Process entities
            for (ClientEntity entity : entitySet.getEntities()) {
                Map<String, Object> entityData = processEntity(entity);
                if (entityData != null) {
                    entities.add(entityData);
                }
            }
            
            // Handle delta token for change tracking
            if (config.isEnableChangeTracking() && response.getHeader("DataServiceVersion") != null) {
                String deltaLink = entitySet.getDeltaLink() != null ? entitySet.getDeltaLink().toString() : null;
                if (deltaLink != null) {
                    lastDeltaToken = extractDeltaToken(deltaLink);
                    logger.debug("Updated delta token: {}", lastDeltaToken);
                }
            }
            
            logger.info("OData sender adapter retrieved {} entities", entities.size());
            
            return AdapterResult.success(entities, 
                    String.format("Successfully retrieved %d entities from OData service", entities.size()));
                    
        } catch (Exception e) {
            logger.error("Error polling OData service", e);
            throw new AdapterException.OperationException(AdapterType.ODATA, 
                    "Failed to poll OData service: " + e.getMessage(), e);
        }
    }
    
    private Map<String, Object> processEntity(ClientEntity entity) {
        Map<String, Object> entityData = new HashMap<>();
        
        // Extract entity ID
        if (entity.getId() != null) {
            entityData.put("@odata.id", entity.getId().toString());
        }
        
        // Extract entity type
        if (entity.getTypeName() != null) {
            entityData.put("@odata.type", entity.getTypeName().toString());
        }
        
        // Extract properties
        for (ClientProperty property : entity.getProperties()) {
            String propertyName = property.getName();
            Object propertyValue = property.getValue().asPrimitive();
            entityData.put(propertyName, propertyValue);
        }
        
        // Extract navigation properties if configured
        if (config.isExpandNavigationProperties() && !entity.getNavigationLinks().isEmpty()) {
            Map<String, Object> navigationProps = new HashMap<>();
            entity.getNavigationLinks().forEach(link -> {
                navigationProps.put(link.getName(), link.getLink().toString());
            });
            entityData.put("@odata.navigationProperties", navigationProps);
        }
        
        // Check for duplicate processing
        String entityId = entityData.get("@odata.id") != null ? 
                entityData.get("@odata.id").toString() : UUID.randomUUID().toString();
        
        if (config.isEnableDuplicateHandling() && processedEntities.containsKey(entityId)) {
            logger.debug("Skipping already processed entity: {}", entityId);
            return null;
        }
        
        processedEntities.put(entityId, String.valueOf(System.currentTimeMillis()));
        
        return entityData;
    }
    
    private URI addQueryOptions(URI baseUri) throws Exception {
        String uriString = baseUri.toString();
        List<String> queryOptions = new ArrayList<>();
        
        // Add filter
        if (config.getFilter() != null && !config.getFilter().isEmpty()) {
            queryOptions.add("$filter=" + encodeQueryParam(config.getFilter()));
        }
        
        // Add select
        if (config.getSelect() != null && !config.getSelect().isEmpty()) {
            queryOptions.add("$select=" + encodeQueryParam(config.getSelect()));
        }
        
        // Add expand
        if (config.getExpand() != null && !config.getExpand().isEmpty()) {
            queryOptions.add("$expand=" + encodeQueryParam(config.getExpand()));
        }
        
        // Add orderby
        if (config.getOrderBy() != null && !config.getOrderBy().isEmpty()) {
            queryOptions.add("$orderby=" + encodeQueryParam(config.getOrderBy()));
        }
        
        // Add top
        if (config.getTop() > 0) {
            queryOptions.add("$top=" + config.getTop());
        }
        
        // Add skip for pagination
        if (config.getSkip() > 0) {
            queryOptions.add("$skip=" + config.getSkip());
        }
        
        // Add count
        if (config.isIncludeCount()) {
            queryOptions.add("$count=true");
        }
        
        // Add delta token for change tracking
        if (config.isEnableChangeTracking() && lastDeltaToken != null) {
            queryOptions.add("$deltatoken=" + lastDeltaToken);
        }
        
        // Append query options
        if (!queryOptions.isEmpty()) {
            String queryString = String.join("&", queryOptions);
            uriString += (uriString.contains("?") ? "&" : "?") + queryString;
        }
        
        return URI.create(uriString);
    }
    
    private String encodeQueryParam(String param) throws Exception {
        return java.net.URLEncoder.encode(param, "UTF-8");
    }
    
    private String extractDeltaToken(String deltaLink) {
        // Extract delta token from delta link
        int tokenIndex = deltaLink.indexOf("$deltatoken=");
        if (tokenIndex >= 0) {
            return deltaLink.substring(tokenIndex + 12);
        }
        return null;
    }
    
    private void validateConfiguration() throws AdapterException.ConfigurationException {
        if (config.getServiceUrl() == null || config.getServiceUrl().trim().isEmpty()) {
            throw new AdapterException.ConfigurationException(AdapterType.ODATA, "Service URL is required");
        }
        
        if (config.getEntitySetName() == null || config.getEntitySetName().trim().isEmpty()) {
            throw new AdapterException.ConfigurationException(AdapterType.ODATA, "Entity set name is required");
        }
        
        // Set defaults
        if (config.getPollingInterval() <= 0) {
            config.setPollingInterval(30000L); // Default 30 seconds
        }
    }
    
    protected long getPollingIntervalMs() {
        return config.getPollingInterval();
    }
    
    @Override
    public String getConfigurationSummary() {
        return String.format("OData Sender (Inbound): %s/%s, Polling: %dms", 
                config.getServiceUrl(),
                config.getEntitySetName(),
                config.getPollingInterval());
    }
}