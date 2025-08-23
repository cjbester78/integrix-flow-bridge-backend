package com.integrixs.adapters.impl;

import com.integrixs.adapters.core.*;
import com.integrixs.adapters.config.HttpReceiverAdapterConfig;
import com.integrixs.adapters.config.AuthenticationType;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Base64;
import java.net.InetSocketAddress;
import java.net.ProxySelector;
import java.net.Proxy;

/**
 * HTTP Receiver Adapter implementation for polling/receiving data via HTTP/HTTPS.
 * Supports proxy configuration, authentication, and connection management.
 */
public class HttpReceiverAdapter extends AbstractReceiverAdapter {
    
    private final HttpReceiverAdapterConfig config;
    private HttpClient httpClient;
    
    public HttpReceiverAdapter(HttpReceiverAdapterConfig config) {
        super(AdapterType.HTTP);
        this.config = config;
    }
    
    @Override
    protected void doReceiverInitialize() throws Exception {
        logger.info("Initializing HTTP receiver adapter with target endpoint: {}", 
                maskSensitiveUrl(config.getTargetEndpointUrl()));
        
        validateConfiguration();
        httpClient = createHttpClient();
        
        logger.info("HTTP receiver adapter initialized successfully");
    }
    
    @Override
    protected void doReceiverDestroy() throws Exception {
        logger.info("Destroying HTTP receiver adapter");
        // HttpClient doesn't need explicit cleanup
        httpClient = null;
    }
    
    @Override
    protected AdapterResult doTestConnection() throws Exception {
        String endpoint = getEffectiveEndpoint();
        if (endpoint == null || endpoint.trim().isEmpty()) {
            return AdapterResult.failure("Target endpoint URL not configured");
        }
        
        try {
            // Use HEAD request for connection testing
            HttpRequest.Builder requestBuilder = HttpRequest.newBuilder()
                    .uri(new URI(endpoint))
                    .method("HEAD", HttpRequest.BodyPublishers.noBody())
                    .timeout(Duration.ofSeconds(Math.min(config.getConnectionTimeout(), 10))); // Max 10s for test
            
            addAuthenticationHeaders(requestBuilder);
            addCommonHeaders(requestBuilder);
            
            HttpRequest request = requestBuilder.build();
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            
            int statusCode = response.statusCode();
            if (statusCode >= 200 && statusCode < 400) {
                return AdapterResult.success(null, "Connection test successful, status: " + statusCode);
            } else {
                return AdapterResult.failure("Connection test failed with status: " + statusCode);
            }
            
        } catch (Exception e) {
            logger.debug("Connection test failed", e);
            return AdapterResult.connectionError("Connection test failed: " + e.getMessage(), e);
        }
    }
    
    @Override
    protected AdapterResult doReceive(Object criteria) throws Exception {
        String endpoint = getEffectiveEndpoint();
        
        try {
            HttpRequest.Builder requestBuilder = HttpRequest.newBuilder()
                    .uri(new URI(endpoint))
                    .GET() // Receivers typically use GET
                    .timeout(Duration.ofSeconds(config.getReadTimeout()));
            
            // Add query parameters if criteria provided
            if (criteria != null) {
                String queryString = buildQueryString(criteria);
                if (!queryString.isEmpty()) {
                    String separator = endpoint.contains("?") ? "&" : "?";
                    requestBuilder.uri(new URI(endpoint + separator + queryString));
                }
            }
            
            addAuthenticationHeaders(requestBuilder);
            addCommonHeaders(requestBuilder);
            addRequestHeaders(requestBuilder);
            
            HttpRequest request = requestBuilder.build();
            
            logger.debug("Receiving data from HTTP endpoint: {}", maskSensitiveUrl(endpoint));
            
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            
            return processReceiveResponse(response);
            
        } catch (Exception e) {
            logger.error("HTTP receive operation failed", e);
            
            if (e instanceof java.net.ConnectException) {
                throw new AdapterException.ConnectionException(AdapterType.HTTP, "Connection failed: " + e.getMessage(), e);
            } else if (e instanceof java.net.SocketTimeoutException) {
                throw new AdapterException.TimeoutException(AdapterType.HTTP, "Request timeout: " + e.getMessage(), e);
            } else if (e instanceof javax.net.ssl.SSLException) {
                throw new AdapterException.ConnectionException(AdapterType.HTTP, "SSL connection failed: " + e.getMessage(), e);
            } else {
                throw new AdapterException(AdapterType.HTTP, AdapterMode.RECEIVER, "Receive operation failed", e);
            }
        }
    }
    
    @Override
    protected long getPollingIntervalMs() {
        // Default polling interval if not specified in config
        // In a real implementation, this would come from configuration
        return 30000; // 30 seconds default
    }
    
    private void validateConfiguration() throws AdapterException {
        String endpoint = getEffectiveEndpoint();
        if (endpoint == null || endpoint.trim().isEmpty()) {
            throw new AdapterException.ConfigurationException(AdapterType.HTTP, 
                    "Target endpoint URL is required for receiver");
        }
        
        if (config.getConnectionTimeout() <= 0) {
            throw new AdapterException.ConfigurationException(AdapterType.HTTP, 
                    "Connection timeout must be positive");
        }
        
        if (config.getReadTimeout() <= 0) {
            throw new AdapterException.ConfigurationException(AdapterType.HTTP, 
                    "Read timeout must be positive");
        }
        
        // Validate proxy configuration if enabled
        if (config.isUseProxy()) {
            validateProxyConfiguration();
        }
        
        // Validate authentication configuration
        if (config.getAuthenticationType() != null && config.getAuthenticationType() != AuthenticationType.NONE) {
            validateAuthenticationConfig();
        }
    }
    
    private void validateProxyConfiguration() throws AdapterException {
        if (config.getProxyServer() == null || config.getProxyServer().trim().isEmpty()) {
            throw new AdapterException.ConfigurationException(AdapterType.HTTP, 
                    "Proxy server is required when proxy is enabled");
        }
        
        if (config.getProxyPort() <= 0 || config.getProxyPort() > 65535) {
            throw new AdapterException.ConfigurationException(AdapterType.HTTP, 
                    "Invalid proxy port: " + config.getProxyPort());
        }
    }
    
    private void validateAuthenticationConfig() throws AdapterException {
        AuthenticationType authType = config.getAuthenticationType();
        
        switch (authType) {
            case BASIC:
                if (config.getBasicUsername() == null || config.getBasicPassword() == null) {
                    throw new AdapterException.ConfigurationException(AdapterType.HTTP, 
                            "Basic authentication requires username and password");
                }
                break;
            case OAUTH2:
                if ((config.getClientId() == null || config.getClientSecret() == null) && 
                    config.getOauthAccessToken() == null) {
                    throw new AdapterException.ConfigurationException(AdapterType.HTTP, 
                            "OAuth2 authentication requires either client credentials or access token");
                }
                break;
        }
    }
    
    private HttpClient createHttpClient() throws Exception {
        HttpClient.Builder clientBuilder = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(config.getConnectionTimeout()));
        
        // Configure proxy if enabled
        if (config.isUseProxy()) {
            ProxySelector proxySelector = ProxySelector.of(
                    new InetSocketAddress(config.getProxyServer(), config.getProxyPort()));
            clientBuilder.proxy(proxySelector);
        }
        
        return clientBuilder.build();
    }
    
    private String getEffectiveEndpoint() {
        // Prefer targetEndpointUrl (receiver-specific), fallback to endpointUrl (general)
        return config.getTargetEndpointUrl() != null ? 
                config.getTargetEndpointUrl() : config.getEndpointUrl();
    }
    
    private void addAuthenticationHeaders(HttpRequest.Builder requestBuilder) throws AdapterException {
        if (config.getAuthenticationType() == null || config.getAuthenticationType() == AuthenticationType.NONE) {
            return;
        }
        
        try {
            switch (config.getAuthenticationType()) {
                case BASIC:
                    String basicAuth = Base64.getEncoder().encodeToString(
                            (config.getBasicUsername() + ":" + config.getBasicPassword())
                                    .getBytes(StandardCharsets.UTF_8));
                    requestBuilder.header("Authorization", "Basic " + basicAuth);
                    break;
                    
                case OAUTH2:
                    if (config.getOauthAccessToken() != null) {
                        requestBuilder.header("Authorization", "Bearer " + config.getOauthAccessToken());
                    } else {
                        // In a real implementation, you'd obtain access token using client credentials
                        logger.warn("OAuth2 access token not available, client credentials flow not implemented");
                    }
                    break;
                    
                default:
                    logger.warn("Unsupported authentication type for receiver: {}", config.getAuthenticationType());
            }
        } catch (Exception e) {
            throw new AdapterException.AuthenticationException(AdapterType.HTTP, 
                    "Failed to add authentication headers", e);
        }
    }
    
    private void addCommonHeaders(HttpRequest.Builder requestBuilder) {
        requestBuilder.header("Accept", "application/json, text/plain, */*");
        requestBuilder.header("User-Agent", "Integrix-Flow-Bridge-HTTP-Receiver/1.0");
        
        if (config.getApiVersion() != null) {
            requestBuilder.header("API-Version", config.getApiVersion());
        }
    }
    
    private void addRequestHeaders(HttpRequest.Builder requestBuilder) {
        if (config.getRequestHeaders() != null && !config.getRequestHeaders().trim().isEmpty()) {
            // Parse request headers string (format: "key1:value1,key2:value2")
            String[] headerPairs = config.getRequestHeaders().split(",");
            for (String pair : headerPairs) {
                String[] keyValue = pair.split(":", 2);
                if (keyValue.length == 2) {
                    requestBuilder.header(keyValue[0].trim(), keyValue[1].trim());
                }
            }
        }
    }
    
    private String buildQueryString(Object criteria) {
        if (criteria == null) {
            return "";
        }
        
        // Simple implementation - in practice, you'd have more sophisticated criteria handling
        if (criteria instanceof String) {
            return criteria.toString();
        } else if (criteria instanceof java.util.Map) {
            @SuppressWarnings("unchecked")
            java.util.Map<String, Object> params = (java.util.Map<String, Object>) criteria;
            
            return params.entrySet().stream()
                    .map(entry -> entry.getKey() + "=" + java.net.URLEncoder.encode(
                            entry.getValue().toString(), StandardCharsets.UTF_8))
                    .collect(java.util.stream.Collectors.joining("&"));
        }
        
        return criteria.toString();
    }
    
    private AdapterResult processReceiveResponse(HttpResponse<String> response) {
        int statusCode = response.statusCode();
        String responseBody = response.body();
        
        AdapterResult result;
        
        if (statusCode >= 200 && statusCode < 300) {
            // Check if we actually received data
            if (responseBody != null && !responseBody.trim().isEmpty()) {
                result = AdapterResult.success(responseBody, "Data received successfully");
            } else {
                // Success but no data - common in polling scenarios
                result = AdapterResult.success(null, "No new data available");
            }
        } else if (statusCode >= 300 && statusCode < 400) {
            result = AdapterResult.failure("HTTP redirect not handled, status: " + statusCode);
        } else if (statusCode >= 400 && statusCode < 500) {
            result = AdapterResult.failure("HTTP client error, status: " + statusCode + ", body: " + responseBody);
        } else {
            result = AdapterResult.failure("HTTP server error, status: " + statusCode + ", body: " + responseBody);
        }
        
        // Add response metadata
        result.addMetadata("statusCode", statusCode);
        result.addMetadata("responseHeaders", response.headers().map());
        result.addMetadata("endpoint", maskSensitiveUrl(getEffectiveEndpoint()));
        result.addMetadata("contentLength", responseBody != null ? responseBody.length() : 0);
        
        return result;
    }
    
    private String maskSensitiveUrl(String url) {
        if (url == null) return null;
        
        try {
            URI uri = new URI(url);
            if (uri.getUserInfo() != null) {
                return url.replace(uri.getUserInfo(), "***:***");
            }
            return url;
        } catch (Exception e) {
            return url;
        }
    }
    
    @Override
    public String getConfigurationSummary() {
        return String.format("HttpReceiverAdapter{endpoint=%s, auth=%s, proxy=%s, polling=%s, active=%s}",
                maskSensitiveUrl(getEffectiveEndpoint()),
                config.getAuthenticationType(),
                config.isUseProxy() ? config.getProxyServer() + ":" + config.getProxyPort() : "none",
                isPolling(),
                isActive());
    }
}