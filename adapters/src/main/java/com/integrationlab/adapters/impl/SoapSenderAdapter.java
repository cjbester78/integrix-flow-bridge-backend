package com.integrationlab.adapters.impl;

import com.integrationlab.adapters.core.*;
import com.integrationlab.adapters.config.SoapSenderAdapterConfig;

import javax.xml.namespace.QName;
import jakarta.xml.soap.*;
import jakarta.xml.ws.Endpoint;
import jakarta.xml.ws.handler.MessageContext;
import jakarta.xml.ws.handler.soap.SOAPHandler;
import jakarta.xml.ws.handler.soap.SOAPMessageContext;
import java.io.ByteArrayOutputStream;
import java.net.URL;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * SOAP Sender Adapter implementation for SOAP service endpoints (INBOUND).
 * Follows middleware convention: Sender = receives data FROM external systems.
 * Creates SOAP endpoints to receive SOAP requests from external systems.
 */
public class SoapSenderAdapter extends AbstractSenderAdapter {
    
    private final SoapSenderAdapterConfig config;
    private Endpoint endpoint;
    private final Map<String, Object> receivedMessages = new ConcurrentHashMap<>();
    private SOAPConnectionFactory soapConnectionFactory;
    
    public SoapSenderAdapter(SoapSenderAdapterConfig config) {
        super(AdapterType.SOAP);
        this.config = config;
    }
    
    @Override
    protected void doSenderInitialize() throws Exception {
        logger.info("Initializing SOAP sender adapter (inbound) with endpoint: {}", config.getEndpointUrl());
        
        validateConfiguration();
        
        // Initialize SOAP connection factory for testing
        soapConnectionFactory = SOAPConnectionFactory.newInstance();
        
        // In a real implementation, you would create a SOAP endpoint here
        // For now, we're simulating the endpoint creation
        logger.info("SOAP sender adapter initialized successfully");
    }
    
    @Override
    protected void doSenderDestroy() throws Exception {
        logger.info("Destroying SOAP sender adapter");
        
        if (endpoint != null) {
            endpoint.stop();
            endpoint = null;
        }
        
        receivedMessages.clear();
    }
    
    @Override
    protected AdapterResult doTestConnection() throws Exception {
        List<AdapterResult> testResults = new ArrayList<>();
        
        // Test 1: SOAP endpoint validation
        testResults.add(ConnectionTestUtil.executeBasicConnectivityTest(AdapterType.SOAP, () -> {
            try {
                URL url = new URL(config.getEndpointUrl());
                
                return ConnectionTestUtil.createTestSuccess(AdapterType.SOAP, 
                        "Endpoint Validation", "SOAP endpoint URL is valid: " + url.toString());
                        
            } catch (Exception e) {
                return ConnectionTestUtil.createTestFailure(AdapterType.SOAP, 
                        "Endpoint Validation", "Invalid SOAP endpoint URL: " + e.getMessage(), e);
            }
        }));
        
        // Test 2: WSDL accessibility test
        if (config.getWsdlUrl() != null && !config.getWsdlUrl().isEmpty()) {
            testResults.add(ConnectionTestUtil.executeConfigurationTest(AdapterType.SOAP, () -> {
                try {
                    URL wsdlUrl = new URL(config.getWsdlUrl());
                    wsdlUrl.openConnection().connect();
                    
                    return ConnectionTestUtil.createTestSuccess(AdapterType.SOAP, 
                            "WSDL Access", "WSDL is accessible at: " + wsdlUrl);
                            
                } catch (Exception e) {
                    return ConnectionTestUtil.createTestFailure(AdapterType.SOAP, 
                            "WSDL Access", "Failed to access WSDL: " + e.getMessage(), e);
                }
            }));
        }
        
        // Test 3: SOAP version compatibility
        testResults.add(ConnectionTestUtil.executeConfigurationTest(AdapterType.SOAP, () -> {
            try {
                String soapVersion = config.getSoapVersion();
                if ("1.1".equals(soapVersion) || "1.2".equals(soapVersion)) {
                    return ConnectionTestUtil.createTestSuccess(AdapterType.SOAP, 
                            "SOAP Version", "Using SOAP version: " + soapVersion);
                } else {
                    return ConnectionTestUtil.createTestFailure(AdapterType.SOAP, 
                            "SOAP Version", "Invalid SOAP version: " + soapVersion, null);
                }
            } catch (Exception e) {
                return ConnectionTestUtil.createTestFailure(AdapterType.SOAP, 
                        "SOAP Version", "Failed to determine SOAP version: " + e.getMessage(), e);
            }
        }));
        
        return ConnectionTestUtil.combineTestResults(AdapterType.SOAP, 
                testResults.toArray(new AdapterResult[0]));
    }
    
    @Override
    protected AdapterResult doSend(Object payload, Map<String, Object> headers) throws Exception {
        // For SOAP Sender (inbound), this method would handle incoming SOAP requests
        // In a real implementation, this would be triggered by the SOAP endpoint receiving a request
        
        if (payload instanceof SOAPMessage) {
            SOAPMessage soapMessage = (SOAPMessage) payload;
            
            // Extract message data
            Map<String, Object> messageData = extractSoapMessageData(soapMessage);
            
            // Store received message
            String messageId = UUID.randomUUID().toString();
            receivedMessages.put(messageId, messageData);
            
            logger.info("SOAP sender adapter received message with ID: {}", messageId);
            
            return AdapterResult.success(messageData, 
                    String.format("Successfully received SOAP message: %s", messageId));
        } else {
            // For testing/simulation, accept other payload types
            Map<String, Object> messageData = new HashMap<>();
            messageData.put("payload", payload);
            messageData.put("headers", headers);
            messageData.put("timestamp", new Date());
            
            String messageId = UUID.randomUUID().toString();
            receivedMessages.put(messageId, messageData);
            
            return AdapterResult.success(messageData, 
                    String.format("Successfully processed message: %s", messageId));
        }
    }
    
    private Map<String, Object> extractSoapMessageData(SOAPMessage soapMessage) throws Exception {
        Map<String, Object> data = new HashMap<>();
        
        // Extract SOAP headers
        SOAPHeader soapHeader = soapMessage.getSOAPHeader();
        if (soapHeader != null) {
            Map<String, String> headers = new HashMap<>();
            Iterator<?> headerElements = soapHeader.examineAllHeaderElements();
            while (headerElements.hasNext()) {
                SOAPHeaderElement element = (SOAPHeaderElement) headerElements.next();
                headers.put(element.getLocalName(), element.getTextContent());
            }
            data.put("headers", headers);
        }
        
        // Extract SOAP body
        SOAPBody soapBody = soapMessage.getSOAPBody();
        if (soapBody != null) {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            soapMessage.writeTo(baos);
            data.put("body", baos.toString());
        }
        
        // Extract SOAP action
        String[] soapActionHeader = soapMessage.getMimeHeaders().getHeader("SOAPAction");
        if (soapActionHeader != null && soapActionHeader.length > 0) {
            data.put("soapAction", soapActionHeader[0]);
        }
        
        data.put("timestamp", new Date());
        
        return data;
    }
    
    private void validateConfiguration() throws AdapterException.ConfigurationException {
        if (config.getEndpointUrl() == null || config.getEndpointUrl().trim().isEmpty()) {
            throw new AdapterException.ConfigurationException(AdapterType.SOAP, "Endpoint URL is required");
        }
        
        if (config.getServiceName() == null || config.getServiceName().trim().isEmpty()) {
            throw new AdapterException.ConfigurationException(AdapterType.SOAP, "Service name is required");
        }
        
        if (config.getSoapVersion() == null || config.getSoapVersion().trim().isEmpty()) {
            config.setSoapVersion("1.1"); // Default to SOAP 1.1
        }
    }
    
    @Override
    public String getConfigurationSummary() {
        return String.format("SOAP Sender (Inbound): %s, Service: %s, SOAP Version: %s", 
                config.getEndpointUrl(),
                config.getServiceName(),
                config.getSoapVersion());
    }
}