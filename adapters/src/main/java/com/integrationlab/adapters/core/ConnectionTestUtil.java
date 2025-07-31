package com.integrationlab.adapters.core;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.function.Supplier;

/**
 * Utility class for standardized connection testing across all adapters.
 * Provides common patterns for connection validation, timeout handling, and result reporting.
 */
public class ConnectionTestUtil {
    
    private static final Logger logger = LoggerFactory.getLogger(ConnectionTestUtil.class);
    
    /**
     * Execute a connection test with timeout and error handling.
     * 
     * @param adapterType the adapter type
     * @param testName descriptive name for the test
     * @param testOperation the connection test operation
     * @param timeoutSeconds timeout in seconds
     * @return AdapterResult with test results
     */
    public static AdapterResult executeConnectionTest(AdapterType adapterType, 
                                                    String testName,
                                                    Supplier<AdapterResult> testOperation,
                                                    int timeoutSeconds) {
        
        logger.debug("Starting connection test '{}' for {} adapter", testName, adapterType);
        
        long startTime = System.currentTimeMillis();
        
        try {
            // Execute test with timeout
            CompletableFuture<AdapterResult> future = CompletableFuture.supplyAsync(testOperation);
            AdapterResult result = future.get(timeoutSeconds, TimeUnit.SECONDS);
            
            long duration = System.currentTimeMillis() - startTime;
            
            if (result.isSuccess()) {
                logger.debug("Connection test '{}' passed in {}ms", testName, duration);
                result.addMetadata("testDurationMs", duration);
                result.addMetadata("testName", testName);
            } else {
                logger.warn("Connection test '{}' failed in {}ms: {}", testName, duration, result.getMessage());
            }
            
            return result;
            
        } catch (TimeoutException e) {
            long duration = System.currentTimeMillis() - startTime;
            logger.error("Connection test '{}' timed out after {}ms", testName, duration);
            
            AdapterResult result = AdapterResult.timeout("Connection test timed out after " + timeoutSeconds + " seconds");
            result.addMetadata("testDurationMs", duration);
            result.addMetadata("testName", testName);
            return result;
            
        } catch (Exception e) {
            long duration = System.currentTimeMillis() - startTime;
            logger.error("Connection test '{}' failed with exception after {}ms", testName, duration, e);
            
            AdapterResult result = AdapterResult.connectionError("Connection test failed: " + e.getMessage(), e);
            result.addMetadata("testDurationMs", duration);
            result.addMetadata("testName", testName);
            return result;
        }
    }
    
    /**
     * Execute a basic connectivity check.
     * 
     * @param adapterType the adapter type
     * @param testOperation the connection test operation
     * @return AdapterResult with test results
     */
    public static AdapterResult executeBasicConnectivityTest(AdapterType adapterType,
                                                           Supplier<AdapterResult> testOperation) {
        return executeConnectionTest(adapterType, "Basic Connectivity", testOperation, 10);
    }
    
    /**
     * Execute an authentication test.
     * 
     * @param adapterType the adapter type
     * @param testOperation the authentication test operation
     * @return AdapterResult with test results
     */
    public static AdapterResult executeAuthenticationTest(AdapterType adapterType,
                                                        Supplier<AdapterResult> testOperation) {
        return executeConnectionTest(adapterType, "Authentication", testOperation, 15);
    }
    
    /**
     * Execute a configuration validation test.
     * 
     * @param adapterType the adapter type
     * @param testOperation the configuration test operation
     * @return AdapterResult with test results
     */
    public static AdapterResult executeConfigurationTest(AdapterType adapterType,
                                                       Supplier<AdapterResult> testOperation) {
        return executeConnectionTest(adapterType, "Configuration Validation", testOperation, 5);
    }
    
    /**
     * Create a comprehensive connection test result by combining multiple test results.
     * 
     * @param adapterType the adapter type
     * @param testResults array of individual test results
     * @return combined AdapterResult
     */
    public static AdapterResult combineTestResults(AdapterType adapterType, AdapterResult... testResults) {
        if (testResults == null || testResults.length == 0) {
            return AdapterResult.failure("No test results provided");
        }
        
        int passedTests = 0;
        int totalTests = testResults.length;
        StringBuilder messageBuilder = new StringBuilder();
        long totalDuration = 0;
        
        for (int i = 0; i < testResults.length; i++) {
            AdapterResult result = testResults[i];
            
            if (result.isSuccess()) {
                passedTests++;
            }
            
            String testName = (String) result.getMetadata().get("testName");
            Long duration = (Long) result.getMetadata().get("testDurationMs");
            
            if (testName != null) {
                messageBuilder.append(testName).append(": ");
                messageBuilder.append(result.isSuccess() ? "PASS" : "FAIL");
                
                if (duration != null) {
                    messageBuilder.append(" (").append(duration).append("ms)");
                    totalDuration += duration;
                }
                
                if (!result.isSuccess()) {
                    messageBuilder.append(" - ").append(result.getMessage());
                }
                
                if (i < testResults.length - 1) {
                    messageBuilder.append("; ");
                }
            }
        }
        
        boolean allPassed = passedTests == totalTests;
        String finalMessage = String.format("Connection tests: %d/%d passed. %s", 
                                           passedTests, totalTests, messageBuilder.toString());
        
        AdapterResult combinedResult = allPassed ? 
                AdapterResult.success(null, finalMessage) : 
                AdapterResult.failure(finalMessage);
        
        combinedResult.addMetadata("testsPassed", passedTests);
        combinedResult.addMetadata("testsTotal", totalTests);
        combinedResult.addMetadata("totalDurationMs", totalDuration);
        
        return combinedResult;
    }
    
    /**
     * Create a standardized connection test failure result.
     * 
     * @param adapterType the adapter type
     * @param testName the test name
     * @param reason the failure reason
     * @param exception optional exception
     * @return failure AdapterResult
     */
    public static AdapterResult createTestFailure(AdapterType adapterType, String testName, 
                                                String reason, Exception exception) {
        
        AdapterResult result = exception != null ? 
                AdapterResult.connectionError(reason, exception) : 
                AdapterResult.failure(reason);
        
        result.addMetadata("testName", testName);
        result.addMetadata("adapterType", adapterType.toString());
        
        return result;
    }
    
    /**
     * Create a standardized connection test success result.
     * 
     * @param adapterType the adapter type
     * @param testName the test name
     * @param message success message
     * @return success AdapterResult
     */
    public static AdapterResult createTestSuccess(AdapterType adapterType, String testName, String message) {
        AdapterResult result = AdapterResult.success(null, message);
        result.addMetadata("testName", testName);
        result.addMetadata("adapterType", adapterType.toString());
        return result;
    }
    
    /**
     * Validate that required configuration fields are not null or empty.
     * 
     * @param adapterType the adapter type
     * @param fieldName the field name
     * @param fieldValue the field value
     * @throws AdapterException.ConfigurationException if validation fails
     */
    public static void validateRequiredField(AdapterType adapterType, String fieldName, String fieldValue) 
            throws AdapterException.ConfigurationException {
        
        if (fieldValue == null || fieldValue.trim().isEmpty()) {
            throw new AdapterException.ConfigurationException(adapterType, 
                    fieldName + " is required and cannot be null or empty");
        }
    }
    
    /**
     * Validate that a numeric field is within valid range.
     * 
     * @param adapterType the adapter type
     * @param fieldName the field name
     * @param value the numeric value
     * @param minValue minimum allowed value (inclusive)
     * @param maxValue maximum allowed value (inclusive)
     * @throws AdapterException.ConfigurationException if validation fails
     */
    public static void validateNumericRange(AdapterType adapterType, String fieldName, 
                                          int value, int minValue, int maxValue) 
            throws AdapterException.ConfigurationException {
        
        if (value < minValue || value > maxValue) {
            throw new AdapterException.ConfigurationException(adapterType, 
                    String.format("%s must be between %d and %d, got %d", 
                                fieldName, minValue, maxValue, value));
        }
    }
}