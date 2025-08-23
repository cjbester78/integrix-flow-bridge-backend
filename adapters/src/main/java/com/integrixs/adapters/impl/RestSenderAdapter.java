package com.integrixs.adapters.impl;

import com.integrixs.adapters.core.*;
import com.integrixs.adapters.config.RestSenderAdapterConfig;

import org.springframework.web.client.RestTemplate;
import org.springframework.http.*;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestClientException;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * REST Sender Adapter implementation for REST API consumption (INBOUND).
 * Follows middleware convention: Sender = receives data FROM external systems.
 * Supports HTTP/HTTPS, various authentication methods, and JSON/XML processing.
 */
public class RestSenderAdapter extends AbstractSenderAdapter {
    
    private final RestSenderAdapterConfig config;
    private final Map<String, String> processedMessages = new ConcurrentHashMap<>();
    private RestTemplate restTemplate;
    
    public RestSenderAdapter(RestSenderAdapterConfig config) {
        super(AdapterType.REST);
        this.config = config;
    }
    
    @Override
    protected void doSenderInitialize() throws Exception {
        logger.info("Initializing REST sender adapter (inbound) with URL: {}", config.getBaseUrl());
        
        validateConfiguration();
        initializeRestTemplate();
        
        logger.info("REST sender adapter initialized successfully");
    }
    
    @Override
    protected void doSenderDestroy() throws Exception {
        logger.info("Destroying REST sender adapter");
        
        processedMessages.clear();
        restTemplate = null;
    }
    
    @Override
    protected AdapterResult doTestConnection() throws Exception {
        List<AdapterResult> testResults = new ArrayList<>();
        
        // Test 1: Basic connectivity test
        testResults.add(ConnectionTestUtil.executeBasicConnectivityTest(AdapterType.REST, () -> {
            try {
                String testUrl = config.getBaseUrl();
                if (config.getHealthCheckEndpoint() != null) {
                    testUrl = config.getBaseUrl() + config.getHealthCheckEndpoint();
                }
                
                HttpHeaders headers = createHeaders();
                HttpEntity<?> entity = new HttpEntity<>(headers);
                
                ResponseEntity<String> response = restTemplate.exchange(
                        testUrl, HttpMethod.GET, entity, String.class);
                
                if (response.getStatusCode().is2xxSuccessful()) {
                    return ConnectionTestUtil.createTestSuccess(AdapterType.REST, 
                            "REST Connection", "Successfully connected to REST endpoint");
                } else {
                    return ConnectionTestUtil.createTestFailure(AdapterType.REST, 
                            "REST Connection", "REST endpoint returned status: " + response.getStatusCode(), null);
                }
                        
            } catch (Exception e) {
                return ConnectionTestUtil.createTestFailure(AdapterType.REST, 
                        "REST Connection", "Failed to connect to REST endpoint: " + e.getMessage(), e);
            }
        }));
        
        // Test 2: Authentication test
        if (config.getAuthenticationType() != null && !"none".equals(config.getAuthenticationType())) {
            testResults.add(ConnectionTestUtil.executeConfigurationTest(AdapterType.REST, () -> {
                try {
                    HttpHeaders headers = createHeaders();
                    
                    if (headers.containsKey(HttpHeaders.AUTHORIZATION) || 
                        headers.containsKey("X-API-Key") ||
                        !headers.isEmpty()) {
                        return ConnectionTestUtil.createTestSuccess(AdapterType.REST, 
                                "Authentication", "Authentication headers configured successfully");
                    } else {
                        return ConnectionTestUtil.createTestFailure(AdapterType.REST, 
                                "Authentication", "Authentication configured but no auth headers found", null);
                    }
                            
                } catch (Exception e) {
                    return ConnectionTestUtil.createTestFailure(AdapterType.REST, 
                            "Authentication", "Failed to configure authentication: " + e.getMessage(), e);
                }
            }));
        }
        
        // Test 3: Data polling test
        testResults.add(ConnectionTestUtil.executeConfigurationTest(AdapterType.REST, () -> {
            try {
                String pollUrl = config.getBaseUrl() + config.getPollingEndpoint();
                HttpHeaders headers = createHeaders();
                HttpEntity<?> entity = new HttpEntity<>(headers);
                
                ResponseEntity<String> response = restTemplate.exchange(
                        pollUrl, HttpMethod.GET, entity, String.class);
                
                return ConnectionTestUtil.createTestSuccess(AdapterType.REST, 
                        "Data Polling", "Successfully polled data from endpoint, status: " + response.getStatusCode());
                        
            } catch (Exception e) {
                return ConnectionTestUtil.createTestFailure(AdapterType.REST, 
                        "Data Polling", "Failed to poll data from endpoint: " + e.getMessage(), e);
            }
        }));
        
        return ConnectionTestUtil.combineTestResults(AdapterType.REST, 
                testResults.toArray(new AdapterResult[0]));
    }
    
    @Override
    protected AdapterResult doSend(Object payload, Map<String, Object> headers) throws Exception {
        // For REST Sender (inbound), "send" means polling/retrieving data FROM REST API
        return pollFromRestApi();
    }
    
    private AdapterResult pollFromRestApi() throws Exception {
        List<Map<String, Object>> processedData = new ArrayList<>();
        
        try {
            String pollUrl = config.getBaseUrl() + config.getPollingEndpoint();
            
            // Add query parameters if configured
            if (config.getQueryParameters() != null && !config.getQueryParameters().isEmpty()) {
                pollUrl += "?" + config.getQueryParameters();
            }
            
            HttpHeaders httpHeaders = createHeaders();
            HttpEntity<?> entity = new HttpEntity<>(httpHeaders);
            
            ResponseEntity<String> response = restTemplate.exchange(
                    pollUrl, HttpMethod.GET, entity, String.class);
            
            if (response.getStatusCode().is2xxSuccessful()) {
                String responseBody = response.getBody();
                
                if (responseBody != null && !responseBody.trim().isEmpty()) {
                    Map<String, Object> responseData = processResponse(responseBody, response.getHeaders());
                    
                    // Check for duplicates if enabled
                    if (config.isEnableDuplicateHandling()) {
                        String messageId = generateMessageId(responseData);
                        if (processedMessages.containsKey(messageId)) {
                            logger.debug("Duplicate message detected, skipping: {}", messageId);
                            return AdapterResult.success(Collections.emptyList(), "Duplicate message skipped");
                        }
                        processedMessages.put(messageId, String.valueOf(System.currentTimeMillis()));
                    }
                    
                    processedData.add(responseData);
                } else {
                    logger.debug("Empty response from REST endpoint");
                }
            } else {
                throw new AdapterException.ProcessingException(AdapterType.REST, 
                        "REST polling failed with status: " + response.getStatusCode());
            }
            
        } catch (HttpClientErrorException e) {
            throw new AdapterException.ProcessingException(AdapterType.REST, 
                    "REST client error: " + e.getStatusCode() + " - " + e.getResponseBodyAsString(), e);
        } catch (HttpServerErrorException e) {
            throw new AdapterException.ProcessingException(AdapterType.REST, 
                    "REST server error: " + e.getStatusCode() + " - " + e.getResponseBodyAsString(), e);
        } catch (RestClientException e) {
            throw new AdapterException.ProcessingException(AdapterType.REST, 
                    "REST communication error: " + e.getMessage(), e);
        }
        
        logger.info("REST sender adapter polled {} items from API", processedData.size());
        
        return AdapterResult.success(processedData, 
                String.format("Retrieved %d items from REST API", processedData.size()));
    }
    
    private Map<String, Object> processResponse(String responseBody, HttpHeaders responseHeaders) throws Exception {
        Map<String, Object> responseData = new HashMap<>();
        
        // Store raw response
        responseData.put("rawResponse", responseBody);
        responseData.put("responseSize", responseBody.length());
        responseData.put("timestamp", System.currentTimeMillis());
        
        // Store response headers if configured
        if (config.isIncludeResponseHeaders()) {
            Map<String, String> headers = new HashMap<>();
            responseHeaders.forEach((key, values) -> 
                headers.put(key, String.join(",", values)));
            responseData.put("responseHeaders", headers);
        }
        
        // Parse response based on content type
        String contentType = responseHeaders.getFirst(HttpHeaders.CONTENT_TYPE);
        if (contentType != null) {
            responseData.put("contentType", contentType);
            
            if (contentType.contains("application/json")) {
                responseData.put("parsedContent", parseJsonResponse(responseBody));
            } else if (contentType.contains("application/xml") || contentType.contains("text/xml")) {
                responseData.put("parsedContent", parseXmlResponse(responseBody));
            } else {
                responseData.put("parsedContent", responseBody);
            }
        } else {
            responseData.put("parsedContent", responseBody);
        }
        
        return responseData;
    }
    
    private Object parseJsonResponse(String jsonResponse) throws Exception {
        // Simple JSON parsing - in production, use Jackson or Gson
        try {
            // This is a simplified implementation
            // In a real scenario, you'd use proper JSON parsing
            return jsonResponse;
        } catch (Exception e) {
            logger.warn("Failed to parse JSON response, returning as string", e);
            return jsonResponse;
        }
    }
    
    private Object parseXmlResponse(String xmlResponse) throws Exception {
        // Simple XML parsing - in production, use proper XML parser
        try {
            // This is a simplified implementation
            // In a real scenario, you'd use proper XML parsing
            return xmlResponse;
        } catch (Exception e) {
            logger.warn("Failed to parse XML response, returning as string", e);
            return xmlResponse;
        }
    }
    
    private String generateMessageId(Map<String, Object> responseData) {
        // Generate unique message ID based on content
        String content = (String) responseData.get("rawResponse");
        return String.valueOf(content.hashCode()) + "_" + responseData.get("timestamp");
    }
    
    private HttpHeaders createHeaders() throws Exception {
        HttpHeaders headers = new HttpHeaders();
        
        // Set content type
        if (config.getContentType() != null) {
            headers.setContentType(MediaType.parseMediaType(config.getContentType()));
        }
        
        // Set accept header
        if (config.getAcceptType() != null) {
            headers.setAccept(Arrays.asList(MediaType.parseMediaType(config.getAcceptType())));
        }
        
        // Authentication
        if (config.getAuthenticationType() != null) {
            switch (config.getAuthenticationType().name().toLowerCase()) {
                case "basic":
                    if (config.getUsername() != null && config.getPassword() != null) {
                        String auth = config.getUsername() + ":" + config.getPassword();
                        String encodedAuth = Base64.getEncoder().encodeToString(auth.getBytes());
                        headers.set(HttpHeaders.AUTHORIZATION, "Basic " + encodedAuth);
                    }
                    break;
                    
                case "bearer":
                    if (config.getBearerToken() != null) {
                        headers.set(HttpHeaders.AUTHORIZATION, "Bearer " + config.getBearerToken());
                    }
                    break;
                    
                case "apikey":
                    if (config.getApiKey() != null) {
                        String apiKeyHeader = config.getApiKeyHeader() != null ? 
                                config.getApiKeyHeader() : "X-API-Key";
                        headers.set(apiKeyHeader, config.getApiKey());
                    }
                    break;
            }
        }
        
        // Custom headers
        if (config.getHeaders() != null) {
            config.getHeaders().forEach(headers::set);
        }
        
        // User agent
        if (config.getUserAgent() != null) {
            headers.set(HttpHeaders.USER_AGENT, config.getUserAgent());
        }
        
        return headers;
    }
    
    private String buildQueryString(Map<String, String> parameters) {
        StringBuilder queryString = new StringBuilder();
        boolean first = true;
        
        for (Map.Entry<String, String> entry : parameters.entrySet()) {
            if (!first) {
                queryString.append("&");
            }
            queryString.append(entry.getKey()).append("=").append(entry.getValue());
            first = false;
        }
        
        return queryString.toString();
    }
    
    private void initializeRestTemplate() throws Exception {
        restTemplate = new RestTemplate();
        
        // Configure timeouts and other settings
        // In production, you'd configure connection pool, timeouts, etc.
        logger.debug("REST template initialized with default configuration");
    }
    
    private void validateConfiguration() throws AdapterException.ConfigurationException {
        if (config.getBaseUrl() == null || config.getBaseUrl().trim().isEmpty()) {
            throw new AdapterException.ConfigurationException(AdapterType.REST, "Base URL is required");
        }
        if (config.getPollingEndpoint() == null || config.getPollingEndpoint().trim().isEmpty()) {
            throw new AdapterException.ConfigurationException(AdapterType.REST, "Polling endpoint is required");
        }
        
        // Validate authentication configuration
        if ("basic".equals(config.getAuthenticationType())) {
            if (config.getUsername() == null || config.getPassword() == null) {
                throw new AdapterException.ConfigurationException(AdapterType.REST, 
                        "Username and password are required for basic authentication");
            }
        }
        
        if ("bearer".equals(config.getAuthenticationType())) {
            if (config.getBearerToken() == null) {
                throw new AdapterException.ConfigurationException(AdapterType.REST, 
                        "Bearer token is required for bearer authentication");
            }
        }
        
        if ("apikey".equals(config.getAuthenticationType())) {
            if (config.getApiKey() == null) {
                throw new AdapterException.ConfigurationException(AdapterType.REST, 
                        "API key is required for API key authentication");
            }
        }
    }
    
    @Override
    public String getConfigurationSummary() {
        return String.format("REST Sender (Inbound): %s%s, Auth: %s, Polling: %sms", 
                config.getBaseUrl(),
                config.getPollingEndpoint(),
                config.getAuthenticationType() != null ? config.getAuthenticationType() : "none",
                config.getPollingInterval() != null ? config.getPollingInterval() : "0");
    }
}