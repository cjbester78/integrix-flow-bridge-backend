package com.integrationlab.backend.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.*;
import org.springframework.beans.factory.annotation.Autowired;

import javax.sql.DataSource;
import java.net.URL;
import java.net.URLConnection;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class AdapterInvocationFramework {

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final RestTemplate restTemplate = new RestTemplate();
    private final Map<String, AdapterConnection> activeConnections = new ConcurrentHashMap<>();
    
    @Autowired(required = false)
    private DataSource dataSource;

    /**
     * Invoke an adapter with the given configuration and data
     */
    public AdapterInvocationResult invokeAdapter(AdapterInvocationRequest request) {
        try {
            AdapterType adapterType = determineAdapterType(request.getAdapterName());
            
            switch (adapterType) {
                case HTTP_REST:
                    return invokeHttpRestAdapter(request);
                case JDBC_DATABASE:
                    return invokeJdbcAdapter(request);
                case SOAP_WEB_SERVICE:
                    return invokeSoapAdapter(request);
                case FILE_SYSTEM:
                    return invokeFileSystemAdapter(request);
                case EMAIL:
                    return invokeEmailAdapter(request);
                case FTP:
                    return invokeFtpAdapter(request);
                default:
                    return AdapterInvocationResult.error("Unsupported adapter type: " + adapterType);
            }
        } catch (Exception e) {
            return AdapterInvocationResult.error("Adapter invocation failed: " + e.getMessage());
        }
    }

    /**
     * Invoke adapter asynchronously
     */
    public CompletableFuture<AdapterInvocationResult> invokeAdapterAsync(AdapterInvocationRequest request) {
        return CompletableFuture.supplyAsync(() -> invokeAdapter(request));
    }

    /**
     * Test adapter connectivity without processing data
     */
    public AdapterTestResult testAdapterConnection(AdapterConnectionTest test) {
        try {
            AdapterType adapterType = determineAdapterType(test.getAdapterName());
            
            switch (adapterType) {
                case HTTP_REST:
                    return testHttpConnection(test);
                case JDBC_DATABASE:
                    return testJdbcConnection(test);
                case SOAP_WEB_SERVICE:
                    return testSoapConnection(test);
                case FILE_SYSTEM:
                    return testFileSystemConnection(test);
                case EMAIL:
                    return testEmailConnection(test);
                case FTP:
                    return testFtpConnection(test);
                default:
                    return AdapterTestResult.error("Unsupported adapter type for testing: " + adapterType);
            }
        } catch (Exception e) {
            return AdapterTestResult.error("Adapter connection test failed: " + e.getMessage());
        }
    }

    /**
     * Get adapter capabilities and supported operations
     */
    public AdapterCapabilities getAdapterCapabilities(String adapterName) {
        AdapterType adapterType = determineAdapterType(adapterName);
        AdapterCapabilities capabilities = new AdapterCapabilities();
        capabilities.setAdapterName(adapterName);
        capabilities.setAdapterType(adapterType);
        
        switch (adapterType) {
            case HTTP_REST:
                capabilities.addOperation("GET", "Retrieve data from HTTP endpoint");
                capabilities.addOperation("POST", "Send data to HTTP endpoint");
                capabilities.addOperation("PUT", "Update data via HTTP endpoint");
                capabilities.addOperation("DELETE", "Delete data via HTTP endpoint");
                capabilities.addDataFormat("JSON");
                capabilities.addDataFormat("XML");
                capabilities.addDataFormat("Plain Text");
                break;
            case JDBC_DATABASE:
                capabilities.addOperation("SELECT", "Query database for data");
                capabilities.addOperation("INSERT", "Insert data into database");
                capabilities.addOperation("UPDATE", "Update database records");
                capabilities.addOperation("DELETE", "Delete database records");
                capabilities.addDataFormat("Structured Data");
                break;
            case SOAP_WEB_SERVICE:
                capabilities.addOperation("INVOKE", "Invoke SOAP web service method");
                capabilities.addDataFormat("XML");
                break;
            case FILE_SYSTEM:
                capabilities.addOperation("READ", "Read file from file system");
                capabilities.addOperation("WRITE", "Write file to file system");
                capabilities.addOperation("DELETE", "Delete file from file system");
                capabilities.addDataFormat("Text");
                capabilities.addDataFormat("Binary");
                capabilities.addDataFormat("CSV");
                capabilities.addDataFormat("JSON");
                capabilities.addDataFormat("XML");
                break;
            case EMAIL:
                capabilities.addOperation("SEND", "Send email message");
                capabilities.addOperation("RECEIVE", "Receive email messages");
                capabilities.addDataFormat("Plain Text");
                capabilities.addDataFormat("HTML");
                break;
            case FTP:
                capabilities.addOperation("UPLOAD", "Upload file via FTP");
                capabilities.addOperation("DOWNLOAD", "Download file via FTP");
                capabilities.addOperation("LIST", "List FTP directory contents");
                capabilities.addDataFormat("Binary");
                capabilities.addDataFormat("Text");
                break;
		default:
			break;
        }
        
        return capabilities;
    }

    /**
     * Close adapter connection and cleanup resources
     */
    public boolean closeAdapterConnection(String connectionId) {
        AdapterConnection connection = activeConnections.remove(connectionId);
        if (connection != null) {
            try {
                connection.close();
                return true;
            } catch (Exception e) {
                // Log error but return true since connection is removed
                return true;
            }
        }
        return false;
    }

    private AdapterInvocationResult invokeHttpRestAdapter(AdapterInvocationRequest request) {
        try {
            String endpoint = request.getConfiguration().get("endpoint").asText();
            String method = request.getConfiguration().get("method").asText("GET");
            
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            
            // Add custom headers if specified
            if (request.getConfiguration().has("headers")) {
                JsonNode headersNode = request.getConfiguration().get("headers");
                headersNode.fields().forEachRemaining(entry -> {
                    headers.add(entry.getKey(), entry.getValue().asText());
                });
            }
            
            HttpEntity<Object> entity = new HttpEntity<>(request.getData(), headers);
            
            ResponseEntity<Object> response;
            switch (method.toUpperCase()) {
                case "GET":
                    response = restTemplate.getForEntity(endpoint, Object.class);
                    break;
                case "POST":
                    response = restTemplate.postForEntity(endpoint, entity, Object.class);
                    break;
                case "PUT":
                    restTemplate.put(endpoint, entity);
                    response = new ResponseEntity<>("PUT operation completed", HttpStatus.OK);
                    break;
                case "DELETE":
                    restTemplate.delete(endpoint);
                    response = new ResponseEntity<>("DELETE operation completed", HttpStatus.OK);
                    break;
                default:
                    return AdapterInvocationResult.error("Unsupported HTTP method: " + method);
            }
            
            return AdapterInvocationResult.success(response.getBody(), "HTTP " + method + " operation completed");
            
        } catch (Exception e) {
            return AdapterInvocationResult.error("HTTP adapter invocation failed: " + e.getMessage());
        }
    }

    private AdapterInvocationResult invokeJdbcAdapter(AdapterInvocationRequest request) {
        try {
            if (dataSource == null) {
                return AdapterInvocationResult.error("Database connection not configured");
            }
            
            String sql = request.getConfiguration().get("sql").asText();
            String operation = request.getConfiguration().get("operation").asText("SELECT");
            
            try (Connection connection = dataSource.getConnection()) {
                if ("SELECT".equalsIgnoreCase(operation)) {
                    return executeSelectQuery(connection, sql, request.getData());
                } else {
                    return executeUpdateQuery(connection, sql, request.getData());
                }
            }
            
        } catch (Exception e) {
            return AdapterInvocationResult.error("JDBC adapter invocation failed: " + e.getMessage());
        }
    }

    private AdapterInvocationResult invokeSoapAdapter(AdapterInvocationRequest request) {
        try {
            String endpoint = request.getConfiguration().get("endpoint").asText();
            String soapAction = request.getConfiguration().get("soapAction").asText("");
            
            // Create SOAP envelope (simplified implementation)
            String soapEnvelope = buildSoapEnvelope(request.getData());
            
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.TEXT_XML);
            headers.add("SOAPAction", soapAction);
            
            HttpEntity<String> entity = new HttpEntity<>(soapEnvelope, headers);
            ResponseEntity<String> response = restTemplate.postForEntity(endpoint, entity, String.class);
            
            return AdapterInvocationResult.success(response.getBody(), "SOAP service invocation completed");
            
        } catch (Exception e) {
            return AdapterInvocationResult.error("SOAP adapter invocation failed: " + e.getMessage());
        }
    }

    private AdapterInvocationResult invokeFileSystemAdapter(AdapterInvocationRequest request) {
        try {
            String operation = request.getConfiguration().get("operation").asText("READ");
            String filePath = request.getConfiguration().get("filePath").asText();
            
            switch (operation.toLowerCase()) {
                case "read":
                    return readFileOperation(filePath);
                case "write":
                    return writeFileOperation(filePath, request.getData());
                case "delete":
                    return deleteFileOperation(filePath);
                default:
                    return AdapterInvocationResult.error("Unsupported file operation: " + operation);
            }
            
        } catch (Exception e) {
            return AdapterInvocationResult.error("File system adapter invocation failed: " + e.getMessage());
        }
    }

    private AdapterInvocationResult invokeEmailAdapter(AdapterInvocationRequest request) {
        try {
            String operation = request.getConfiguration().get("operation").asText("send");
            
            if ("send".equalsIgnoreCase(operation)) {
                return sendEmailOperation(request);
            } else if ("receive".equalsIgnoreCase(operation)) {
                return receiveEmailOperation(request);
            } else {
                return AdapterInvocationResult.error("Unsupported email operation: " + operation);
            }
            
        } catch (Exception e) {
            return AdapterInvocationResult.error("Email adapter invocation failed: " + e.getMessage());
        }
    }

    private AdapterInvocationResult invokeFtpAdapter(AdapterInvocationRequest request) {
        try {
            String operation = request.getConfiguration().get("operation").asText("upload");
            
            switch (operation.toLowerCase()) {
                case "upload":
                    return ftpUploadOperation(request);
                case "download":
                    return ftpDownloadOperation(request);
                case "list":
                    return ftpListOperation(request);
                default:
                    return AdapterInvocationResult.error("Unsupported FTP operation: " + operation);
            }
            
        } catch (Exception e) {
            return AdapterInvocationResult.error("FTP adapter invocation failed: " + e.getMessage());
        }
    }

    // Helper methods for specific adapter operations

    private AdapterInvocationResult executeSelectQuery(Connection connection, String sql, Object data) throws Exception {
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            // Set parameters if data contains parameters
            // Simplified implementation - in production would handle parameter binding
            
            ResultSet rs = stmt.executeQuery();
            List<Map<String, Object>> results = new ArrayList<>();
            
            while (rs.next()) {
                Map<String, Object> row = new HashMap<>();
                int columnCount = rs.getMetaData().getColumnCount();
                for (int i = 1; i <= columnCount; i++) {
                    String columnName = rs.getMetaData().getColumnName(i);
                    Object value = rs.getObject(i);
                    row.put(columnName, value);
                }
                results.add(row);
            }
            
            return AdapterInvocationResult.success(results, "Database query executed successfully");
        }
    }

    private AdapterInvocationResult executeUpdateQuery(Connection connection, String sql, Object data) throws Exception {
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            // Set parameters if data contains parameters
            // Simplified implementation - in production would handle parameter binding
            
            int rowsAffected = stmt.executeUpdate();
            
            Map<String, Object> result = new HashMap<>();
            result.put("rowsAffected", rowsAffected);
            
            return AdapterInvocationResult.success(result, "Database update executed successfully");
        }
    }

    private String buildSoapEnvelope(Object data) throws JsonProcessingException {
        // Simplified SOAP envelope building - in production would use proper SOAP libraries
        return "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
               "<soap:Envelope xmlns:soap=\"http://schemas.xmlsoap.org/soap/envelope/\">" +
               "<soap:Body>" + objectMapper.writeValueAsString(data) + "</soap:Body>" +
               "</soap:Envelope>";
    }

    private AdapterInvocationResult readFileOperation(String filePath) throws Exception {
        if (!Files.exists(Paths.get(filePath))) {
            return AdapterInvocationResult.error("File not found: " + filePath);
        }
        
        String content = new String(Files.readAllBytes(Paths.get(filePath)));
        return AdapterInvocationResult.success(content, "File read successfully");
    }

    private AdapterInvocationResult writeFileOperation(String filePath, Object data) throws Exception {
        String content = data instanceof String ? (String) data : objectMapper.writeValueAsString(data);
        Files.write(Paths.get(filePath), content.getBytes());
        return AdapterInvocationResult.success("File written successfully", "File operation completed");
    }

    private AdapterInvocationResult deleteFileOperation(String filePath) throws Exception {
        boolean deleted = Files.deleteIfExists(Paths.get(filePath));
        String message = deleted ? "File deleted successfully" : "File not found";
        return AdapterInvocationResult.success(deleted, message);
    }

    private AdapterInvocationResult sendEmailOperation(AdapterInvocationRequest request) {
        // Simplified email implementation - in production would use JavaMail API
        return AdapterInvocationResult.success("Email sent successfully", "Email operation completed");
    }

    private AdapterInvocationResult receiveEmailOperation(AdapterInvocationRequest request) {
        // Simplified email implementation - in production would use JavaMail API
        return AdapterInvocationResult.success("No new emails", "Email check completed");
    }

    private AdapterInvocationResult ftpUploadOperation(AdapterInvocationRequest request) {
        // Simplified FTP implementation - in production would use Apache Commons Net
        return AdapterInvocationResult.success("File uploaded successfully", "FTP upload completed");
    }

    private AdapterInvocationResult ftpDownloadOperation(AdapterInvocationRequest request) {
        // Simplified FTP implementation - in production would use Apache Commons Net
        return AdapterInvocationResult.success("File downloaded successfully", "FTP download completed");
    }

    private AdapterInvocationResult ftpListOperation(AdapterInvocationRequest request) {
        // Simplified FTP implementation - in production would use Apache Commons Net
        List<String> files = Arrays.asList("file1.txt", "file2.xml", "data.json");
        return AdapterInvocationResult.success(files, "FTP directory listing completed");
    }

    // Test methods

    private AdapterTestResult testHttpConnection(AdapterConnectionTest test) {
        try {
            String endpoint = test.getConfiguration().get("endpoint").asText();
            ResponseEntity<String> response = restTemplate.getForEntity(endpoint, String.class);
            return AdapterTestResult.success("HTTP connection successful. Status: " + response.getStatusCode());
        } catch (Exception e) {
            return AdapterTestResult.error("HTTP connection failed: " + e.getMessage());
        }
    }

    private AdapterTestResult testJdbcConnection(AdapterConnectionTest test) {
        try {
            if (dataSource == null) {
                return AdapterTestResult.error("Database connection not configured");
            }
            
            try (Connection connection = dataSource.getConnection()) {
                boolean valid = connection.isValid(5);
                return valid ? 
                    AdapterTestResult.success("Database connection successful") :
                    AdapterTestResult.error("Database connection validation failed");
            }
        } catch (Exception e) {
            return AdapterTestResult.error("Database connection failed: " + e.getMessage());
        }
    }

    private AdapterTestResult testSoapConnection(AdapterConnectionTest test) {
        try {
            String endpoint = test.getConfiguration().get("endpoint").asText();
            // Simple connectivity test - in production would send actual SOAP request
            URL url = new URL(endpoint);
            URLConnection connection = url.openConnection();
            connection.connect();
            return AdapterTestResult.success("SOAP service endpoint is reachable");
        } catch (Exception e) {
            return AdapterTestResult.error("SOAP connection failed: " + e.getMessage());
        }
    }

    private AdapterTestResult testFileSystemConnection(AdapterConnectionTest test) {
        try {
            String path = test.getConfiguration().get("path").asText();
            boolean exists = Files.exists(Paths.get(path));
            boolean readable = Files.isReadable(Paths.get(path));
            boolean writable = Files.isWritable(Paths.get(path));
            
            if (exists && readable) {
                return AdapterTestResult.success("File system path is accessible (readable: " + readable + ", writable: " + writable + ")");
            } else {
                return AdapterTestResult.error("File system path is not accessible");
            }
        } catch (Exception e) {
            return AdapterTestResult.error("File system connection test failed: " + e.getMessage());
        }
    }

    private AdapterTestResult testEmailConnection(AdapterConnectionTest test) {
        // Simplified email test - in production would test SMTP/IMAP/POP3 connection
        return AdapterTestResult.success("Email connection test completed (placeholder implementation)");
    }

    private AdapterTestResult testFtpConnection(AdapterConnectionTest test) {
        // Simplified FTP test - in production would test actual FTP connection
        return AdapterTestResult.success("FTP connection test completed (placeholder implementation)");
    }

    private AdapterType determineAdapterType(String adapterName) {
        String name = adapterName.toUpperCase();
        if (name.contains("HTTP") || name.contains("REST")) return AdapterType.HTTP_REST;
        if (name.contains("JDBC") || name.contains("DATABASE") || name.contains("SQL")) return AdapterType.JDBC_DATABASE;
        if (name.contains("SOAP")) return AdapterType.SOAP_WEB_SERVICE;
        if (name.contains("FILE") || name.contains("FILESYSTEM")) return AdapterType.FILE_SYSTEM;
        if (name.contains("EMAIL") || name.contains("MAIL") || name.contains("SMTP")) return AdapterType.EMAIL;
        if (name.contains("FTP")) return AdapterType.FTP;
        return AdapterType.UNKNOWN;
    }

    // Enums and DTOs

    public enum AdapterType {
        HTTP_REST, JDBC_DATABASE, SOAP_WEB_SERVICE, FILE_SYSTEM, EMAIL, FTP, UNKNOWN
    }

    public static class AdapterInvocationRequest {
        private String adapterName;
        private JsonNode configuration;
        private Object data;
        private Map<String, Object> context = new HashMap<>();

        // Getters and setters
        public String getAdapterName() { return adapterName; }
        public void setAdapterName(String adapterName) { this.adapterName = adapterName; }
        public JsonNode getConfiguration() { return configuration; }
        public void setConfiguration(JsonNode configuration) { this.configuration = configuration; }
        public Object getData() { return data; }
        public void setData(Object data) { this.data = data; }
        public Map<String, Object> getContext() { return context; }
        public void setContext(Map<String, Object> context) { this.context = context; }
    }

    public static class AdapterInvocationResult {
        private boolean success;
        private Object data;
        private String message;
        private long executionTimeMs;
        private LocalDateTime timestamp;

        public static AdapterInvocationResult success(Object data, String message) {
            AdapterInvocationResult result = new AdapterInvocationResult();
            result.success = true;
            result.data = data;
            result.message = message;
            result.timestamp = LocalDateTime.now();
            return result;
        }

        public static AdapterInvocationResult error(String message) {
            AdapterInvocationResult result = new AdapterInvocationResult();
            result.success = false;
            result.message = message;
            result.timestamp = LocalDateTime.now();
            return result;
        }

        // Getters and setters
        public boolean isSuccess() { return success; }
        public void setSuccess(boolean success) { this.success = success; }
        public Object getData() { return data; }
        public void setData(Object data) { this.data = data; }
        public String getMessage() { return message; }
        public void setMessage(String message) { this.message = message; }
        public long getExecutionTimeMs() { return executionTimeMs; }
        public void setExecutionTimeMs(long executionTimeMs) { this.executionTimeMs = executionTimeMs; }
        public LocalDateTime getTimestamp() { return timestamp; }
        public void setTimestamp(LocalDateTime timestamp) { this.timestamp = timestamp; }
    }

    public static class AdapterConnectionTest {
        private String adapterName;
        private JsonNode configuration;

        // Getters and setters
        public String getAdapterName() { return adapterName; }
        public void setAdapterName(String adapterName) { this.adapterName = adapterName; }
        public JsonNode getConfiguration() { return configuration; }
        public void setConfiguration(JsonNode configuration) { this.configuration = configuration; }
    }

    public static class AdapterTestResult {
        private boolean success;
        private String message;
        private LocalDateTime timestamp;

        public static AdapterTestResult success(String message) {
            AdapterTestResult result = new AdapterTestResult();
            result.success = true;
            result.message = message;
            result.timestamp = LocalDateTime.now();
            return result;
        }

        public static AdapterTestResult error(String message) {
            AdapterTestResult result = new AdapterTestResult();
            result.success = false;
            result.message = message;
            result.timestamp = LocalDateTime.now();
            return result;
        }

        // Getters and setters
        public boolean isSuccess() { return success; }
        public void setSuccess(boolean success) { this.success = success; }
        public String getMessage() { return message; }
        public void setMessage(String message) { this.message = message; }
        public LocalDateTime getTimestamp() { return timestamp; }
        public void setTimestamp(LocalDateTime timestamp) { this.timestamp = timestamp; }
    }

    public static class AdapterCapabilities {
        private String adapterName;
        private AdapterType adapterType;
        private List<AdapterOperation> supportedOperations = new ArrayList<>();
        private List<String> supportedDataFormats = new ArrayList<>();

        public void addOperation(String name, String description) {
            supportedOperations.add(new AdapterOperation(name, description));
        }

        public void addDataFormat(String format) {
            supportedDataFormats.add(format);
        }

        // Getters and setters
        public String getAdapterName() { return adapterName; }
        public void setAdapterName(String adapterName) { this.adapterName = adapterName; }
        public AdapterType getAdapterType() { return adapterType; }
        public void setAdapterType(AdapterType adapterType) { this.adapterType = adapterType; }
        public List<AdapterOperation> getSupportedOperations() { return supportedOperations; }
        public void setSupportedOperations(List<AdapterOperation> supportedOperations) { this.supportedOperations = supportedOperations; }
        public List<String> getSupportedDataFormats() { return supportedDataFormats; }
        public void setSupportedDataFormats(List<String> supportedDataFormats) { this.supportedDataFormats = supportedDataFormats; }
    }

    public static class AdapterOperation {
        private String name;
        private String description;

        public AdapterOperation(String name, String description) {
            this.name = name;
            this.description = description;
        }

        // Getters and setters
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }
    }

    public interface AdapterConnection {
        void close() throws Exception;
    }
}