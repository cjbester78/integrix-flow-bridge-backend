package com.integrixs.adapters.impl;

import com.integrixs.adapters.core.*;
import com.integrixs.adapters.config.HttpSenderAdapterConfig;
import com.integrixs.adapters.config.AuthenticationType;
import com.integrixs.adapters.config.HttpMethod;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Base64;
import java.util.Map;
import java.util.HashMap;
import java.security.KeyStore;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManagerFactory;
import javax.net.ssl.KeyManagerFactory;

/**
 * HTTP Sender Adapter implementation for sending data via HTTP/HTTPS.
 * Supports various authentication methods, SSL configuration, and error handling.
 */
public class HttpSenderAdapter extends AbstractSenderAdapter {
    
    private final HttpSenderAdapterConfig config;
    private HttpClient httpClient;
    
    public HttpSenderAdapter(HttpSenderAdapterConfig config) {
        super(AdapterType.HTTP);
        this.config = config;
    }
    
    @Override
    protected void doSenderInitialize() throws Exception {
        logger.info("Initializing HTTP sender adapter with endpoint: {}", 
                maskSensitiveUrl(config.getEndpointUrl()));
        
        validateConfiguration();
        httpClient = createHttpClient();
        
        logger.info("HTTP sender adapter initialized successfully");
    }
    
    @Override
    protected void doSenderDestroy() throws Exception {
        logger.info("Destroying HTTP sender adapter");
        // HttpClient doesn't need explicit cleanup
        httpClient = null;
    }
    
    @Override
    protected AdapterResult doTestConnection() throws Exception {
        if (config.getEndpointUrl() == null || config.getEndpointUrl().trim().isEmpty()) {
            return AdapterResult.failure("Endpoint URL not configured");
        }
        
        try {
            // Use HEAD request for connection testing to minimize impact
            HttpRequest.Builder requestBuilder = HttpRequest.newBuilder()
                    .uri(new URI(config.getEndpointUrl()))
                    .method("HEAD", HttpRequest.BodyPublishers.noBody())
                    .timeout(Duration.ofMillis(Math.min(config.getTimeoutMillis(), 10000))); // Max 10s for test
            
            addAuthenticationHeaders(requestBuilder, new HashMap<>());
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
    protected AdapterResult doSend(Object payload, Map<String, Object> headers) throws Exception {
        if (payload == null) {
            throw new AdapterException.ValidationException(AdapterType.HTTP, "Payload cannot be null");
        }
        
        String payloadString = convertPayloadToString(payload);
        
        try {
            HttpRequest.Builder requestBuilder = HttpRequest.newBuilder()
                    .uri(new URI(config.getEndpointUrl()))
                    .timeout(Duration.ofMillis(config.getTimeoutMillis()));
            
            // Set HTTP method and body
            setHttpMethodAndBody(requestBuilder, payloadString);
            
            // Add authentication headers
            addAuthenticationHeaders(requestBuilder, headers);
            
            // Add common headers
            addCommonHeaders(requestBuilder);
            
            // Add custom headers from config
            addCustomHeaders(requestBuilder);
            
            // Add runtime headers
            addRuntimeHeaders(requestBuilder, headers);
            
            HttpRequest request = requestBuilder.build();
            
            logger.debug("Sending HTTP request to: {}", maskSensitiveUrl(config.getEndpointUrl()));
            
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            
            return processResponse(response);
            
        } catch (Exception e) {
            logger.error("HTTP send operation failed", e);
            
            if (e instanceof java.net.ConnectException) {
                throw new AdapterException.ConnectionException(AdapterType.HTTP, "Connection failed: " + e.getMessage(), e);
            } else if (e instanceof java.net.SocketTimeoutException) {
                throw new AdapterException.TimeoutException(AdapterType.HTTP, "Request timeout: " + e.getMessage(), e);
            } else if (e instanceof javax.net.ssl.SSLException) {
                throw new AdapterException.ConnectionException(AdapterType.HTTP, "SSL connection failed: " + e.getMessage(), e);
            } else {
                throw new AdapterException(AdapterType.HTTP, AdapterMode.SENDER, "Send operation failed", e);
            }
        }
    }
    
    private void validateConfiguration() throws AdapterException {
        if (config.getEndpointUrl() == null || config.getEndpointUrl().trim().isEmpty()) {
            throw new AdapterException.ConfigurationException(AdapterType.HTTP, "Endpoint URL is required");
        }
        
        if (config.getHttpMethod() == null) {
            throw new AdapterException.ConfigurationException(AdapterType.HTTP, "HTTP method is required");
        }
        
        if (config.getReadTimeout() <= 0) {
            throw new AdapterException.ConfigurationException(AdapterType.HTTP, "Read timeout must be positive");
        }
        
        // Validate authentication configuration
        if (config.getAuthenticationType() != null) {
            validateAuthenticationConfig();
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
            case BEARER_TOKEN:
                if (config.getBearerToken() == null || config.getBearerToken().trim().isEmpty()) {
                    throw new AdapterException.ConfigurationException(AdapterType.HTTP, 
                            "Bearer token authentication requires token");
                }
                break;
            case API_KEY:
                if (config.getApiKey() == null || config.getApiKeyHeaderName() == null) {
                    throw new AdapterException.ConfigurationException(AdapterType.HTTP, 
                            "API key authentication requires key and header name");
                }
                break;
            case OAUTH2:
                if (config.getOauthAccessToken() == null || config.getOauthAccessToken().trim().isEmpty()) {
                    throw new AdapterException.ConfigurationException(AdapterType.HTTP, 
                            "OAuth2 authentication requires access token");
                }
                break;
        }
    }
    
    private HttpClient createHttpClient() throws Exception {
        HttpClient.Builder clientBuilder = HttpClient.newBuilder()
                .connectTimeout(Duration.ofMillis(config.getTimeoutMillis()));
        
        // Configure SSL if needed
        if (config.getEndpointUrl().toLowerCase().startsWith("https")) {
            if (config.getSslKeyStorePath() != null || config.getSslTrustStorePath() != null) {
                SSLContext sslContext = createSSLContext();
                clientBuilder.sslContext(sslContext);
            }
        }
        
        return clientBuilder.build();
    }
    
    private SSLContext createSSLContext() throws Exception {
        SSLContext sslContext = SSLContext.getInstance("TLS");
        
        KeyManagerFactory keyManagerFactory = null;
        TrustManagerFactory trustManagerFactory = null;
        
        // Configure key store (client certificate)
        if (config.getSslKeyStorePath() != null) {
            KeyStore keyStore = KeyStore.getInstance("JKS");
            // In a real implementation, you'd load from file system
            // keyStore.load(new FileInputStream(config.getSslKeyStorePath()), 
            //               config.getSslKeyStorePassword().toCharArray());
            
            keyManagerFactory = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
            keyManagerFactory.init(keyStore, config.getSslKeyStorePassword().toCharArray());
        }
        
        // Configure trust store (trusted certificates)
        if (config.getSslTrustStorePath() != null) {
            KeyStore trustStore = KeyStore.getInstance("JKS");
            // In a real implementation, you'd load from file system
            // trustStore.load(new FileInputStream(config.getSslTrustStorePath()), 
            //                config.getSslTrustStorePassword().toCharArray());
            
            trustManagerFactory = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
            trustManagerFactory.init(trustStore);
        }
        
        sslContext.init(
                keyManagerFactory != null ? keyManagerFactory.getKeyManagers() : null,
                trustManagerFactory != null ? trustManagerFactory.getTrustManagers() : null,
                null
        );
        
        return sslContext;
    }
    
    private void setHttpMethodAndBody(HttpRequest.Builder requestBuilder, String payload) {
        HttpMethod method = config.getHttpMethod();
        
        switch (method) {
            case GET:
                requestBuilder.GET();
                break;
            case POST:
                requestBuilder.POST(HttpRequest.BodyPublishers.ofString(payload, StandardCharsets.UTF_8));
                break;
            case PUT:
                requestBuilder.PUT(HttpRequest.BodyPublishers.ofString(payload, StandardCharsets.UTF_8));
                break;
            case PATCH:
                requestBuilder.method("PATCH", HttpRequest.BodyPublishers.ofString(payload, StandardCharsets.UTF_8));
                break;
            case DELETE:
                requestBuilder.DELETE();
                break;
            default:
                throw new IllegalArgumentException("Unsupported HTTP method: " + method);
        }
    }
    
    private void addAuthenticationHeaders(HttpRequest.Builder requestBuilder, Map<String, Object> headers) 
            throws AdapterException {
        
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
                    
                case BEARER_TOKEN:
                    requestBuilder.header("Authorization", "Bearer " + config.getBearerToken());
                    break;
                    
                case API_KEY:
                    requestBuilder.header(config.getApiKeyHeaderName(), config.getApiKey());
                    break;
                    
                case OAUTH2:
                    requestBuilder.header("Authorization", "Bearer " + config.getOauthAccessToken());
                    break;
                    
                case JWT:
                    if (config.getJwtToken() != null) {
                        requestBuilder.header("Authorization", "Bearer " + config.getJwtToken());
                    }
                    break;
                    
                default:
                    logger.warn("Unsupported authentication type: {}", config.getAuthenticationType());
            }
        } catch (Exception e) {
            throw new AdapterException.AuthenticationException(AdapterType.HTTP, 
                    "Failed to add authentication headers", e);
        }
    }
    
    private void addCommonHeaders(HttpRequest.Builder requestBuilder) {
        if (config.getContentType() != null) {
            requestBuilder.header("Content-Type", config.getContentType());
        } else if (needsContentType()) {
            requestBuilder.header("Content-Type", "application/json");
        }
        
        requestBuilder.header("User-Agent", "Integrix-Flow-Bridge-HTTP-Adapter/1.0");
        
        if (config.getApiVersion() != null) {
            requestBuilder.header("API-Version", config.getApiVersion());
        }
    }
    
    private void addCustomHeaders(HttpRequest.Builder requestBuilder) {
        if (config.getHeaders() != null) {
            config.getHeaders().forEach(requestBuilder::header);
        }
        
        if (config.getCustomHeaders() != null && !config.getCustomHeaders().trim().isEmpty()) {
            // Parse custom headers string (format: "key1:value1,key2:value2")
            String[] headerPairs = config.getCustomHeaders().split(",");
            for (String pair : headerPairs) {
                String[] keyValue = pair.split(":", 2);
                if (keyValue.length == 2) {
                    requestBuilder.header(keyValue[0].trim(), keyValue[1].trim());
                }
            }
        }
    }
    
    private void addRuntimeHeaders(HttpRequest.Builder requestBuilder, Map<String, Object> headers) {
        if (headers != null) {
            headers.forEach((key, value) -> {
                if (value != null) {
                    requestBuilder.header(key, value.toString());
                }
            });
        }
    }
    
    private boolean needsContentType() {
        HttpMethod method = config.getHttpMethod();
        return method == HttpMethod.POST || method == HttpMethod.PUT || method == HttpMethod.PATCH;
    }
    
    private AdapterResult processResponse(HttpResponse<String> response) {
        int statusCode = response.statusCode();
        String responseBody = response.body();
        
        AdapterResult result;
        
        if (statusCode >= 200 && statusCode < 300) {
            result = AdapterResult.success(responseBody, "HTTP request successful");
        } else if (statusCode >= 300 && statusCode < 400) {
            result = AdapterResult.success(responseBody, "HTTP request redirected, status: " + statusCode);
        } else if (statusCode >= 400 && statusCode < 500) {
            result = AdapterResult.failure("HTTP client error, status: " + statusCode + ", body: " + responseBody);
        } else {
            result = AdapterResult.failure("HTTP server error, status: " + statusCode + ", body: " + responseBody);
        }
        
        // Add response metadata
        result.addMetadata("statusCode", statusCode);
        result.addMetadata("responseHeaders", response.headers().map());
        result.addMetadata("endpoint", maskSensitiveUrl(config.getEndpointUrl()));
        result.addMetadata("httpMethod", config.getHttpMethod().toString());
        
        return result;
    }
    
    private String convertPayloadToString(Object payload) {
        if (payload instanceof String) {
            return (String) payload;
        } else if (payload instanceof byte[]) {
            return new String((byte[]) payload, StandardCharsets.UTF_8);
        } else {
            // For other objects, convert to JSON string representation
            // In a real implementation, you might use Jackson or similar
            return payload.toString();
        }
    }
    
    private String maskSensitiveUrl(String url) {
        if (url == null) return null;
        
        // Mask credentials in URL if present
        try {
            URI uri = new URI(url);
            if (uri.getUserInfo() != null) {
                return url.replace(uri.getUserInfo(), "***:***");
            }
            return url;
        } catch (Exception e) {
            return url; // Return original if parsing fails
        }
    }
    
    @Override
    public String getConfigurationSummary() {
        return String.format("HttpSenderAdapter{endpoint=%s, method=%s, auth=%s, timeout=%dms, active=%s}",
                maskSensitiveUrl(config.getEndpointUrl()),
                config.getHttpMethod(),
                config.getAuthenticationType(),
                config.getTimeoutMillis(),
                isActive());
    }
}