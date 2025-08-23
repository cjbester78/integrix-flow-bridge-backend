package com.integrixs.adapters.impl;

import com.integrixs.adapters.core.*;
import com.integrixs.adapters.config.SoapReceiverAdapterConfig;

import javax.xml.namespace.QName;
import jakarta.xml.soap.*;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMResult;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamSource;
import jakarta.xml.ws.Dispatch;
import jakarta.xml.ws.Service;
import jakarta.xml.ws.soap.SOAPBinding;
import java.io.StringReader;
import java.net.URL;
import java.util.*;

/**
 * SOAP Receiver Adapter implementation for SOAP service calls (OUTBOUND).
 * Follows middleware convention: Receiver = sends data TO external systems.
 * Makes SOAP requests to external SOAP services.
 */
public class SoapReceiverAdapter extends AbstractReceiverAdapter {
    
    private final SoapReceiverAdapterConfig config;
    private SOAPConnectionFactory soapConnectionFactory;
    private MessageFactory messageFactory;
    
    public SoapReceiverAdapter(SoapReceiverAdapterConfig config) {
        super(AdapterType.SOAP);
        this.config = config;
    }
    
    @Override
    protected void doReceiverInitialize() throws Exception {
        logger.info("Initializing SOAP receiver adapter (outbound) with endpoint: {}", config.getEffectiveEndpoint());
        
        validateConfiguration();
        
        // Initialize SOAP factories
        soapConnectionFactory = SOAPConnectionFactory.newInstance();
        
        // Create message factory based on SOAP version
        if ("1.2".equals(config.getSoapVersion())) {
            messageFactory = MessageFactory.newInstance(SOAPConstants.SOAP_1_2_PROTOCOL);
        } else {
            messageFactory = MessageFactory.newInstance(SOAPConstants.SOAP_1_1_PROTOCOL);
        }
        
        logger.info("SOAP receiver adapter initialized successfully");
    }
    
    @Override
    protected void doReceiverDestroy() throws Exception {
        logger.info("Destroying SOAP receiver adapter");
        // Cleanup resources if needed
    }
    
    @Override
    protected AdapterResult doTestConnection() throws Exception {
        List<AdapterResult> testResults = new ArrayList<>();
        
        // Test 1: Endpoint connectivity
        testResults.add(ConnectionTestUtil.executeBasicConnectivityTest(AdapterType.SOAP, () -> {
            try {
                URL url = new URL(config.getEffectiveEndpoint());
                url.openConnection().connect();
                
                return ConnectionTestUtil.createTestSuccess(AdapterType.SOAP, 
                        "Endpoint Connectivity", "Successfully connected to SOAP endpoint");
                        
            } catch (Exception e) {
                return ConnectionTestUtil.createTestFailure(AdapterType.SOAP, 
                        "Endpoint Connectivity", "Failed to connect to SOAP endpoint: " + e.getMessage(), e);
            }
        }));
        
        // Test 2: WSDL validation
        if (config.getWsdlUrl() != null && !config.getWsdlUrl().isEmpty()) {
            testResults.add(ConnectionTestUtil.executeConfigurationTest(AdapterType.SOAP, () -> {
                try {
                    URL wsdlUrl = new URL(config.getWsdlUrl());
                    
                    // Try to create a service from WSDL
                    QName serviceName = new QName(config.getTargetNamespace(), config.getServiceName());
                    Service service = Service.create(wsdlUrl, serviceName);
                    
                    return ConnectionTestUtil.createTestSuccess(AdapterType.SOAP, 
                            "WSDL Validation", "Successfully validated WSDL and service");
                            
                } catch (Exception e) {
                    return ConnectionTestUtil.createTestFailure(AdapterType.SOAP, 
                            "WSDL Validation", "Failed to validate WSDL: " + e.getMessage(), e);
                }
            }));
        }
        
        // Test 3: Authentication test (if configured)
        if (config.getUsername() != null && !config.getUsername().isEmpty()) {
            testResults.add(ConnectionTestUtil.executeConfigurationTest(AdapterType.SOAP, () -> {
                try {
                    // Create a test SOAP message with authentication
                    SOAPMessage testMessage = createTestMessage();
                    addSecurityHeader(testMessage);
                    
                    return ConnectionTestUtil.createTestSuccess(AdapterType.SOAP, 
                            "Authentication", "Authentication configured for user: " + config.getUsername());
                            
                } catch (Exception e) {
                    return ConnectionTestUtil.createTestFailure(AdapterType.SOAP, 
                            "Authentication", "Failed to configure authentication: " + e.getMessage(), e);
                }
            }));
        }
        
        return ConnectionTestUtil.combineTestResults(AdapterType.SOAP, 
                testResults.toArray(new AdapterResult[0]));
    }
    
    @Override
    protected AdapterResult doReceive(Object criteria) throws Exception {
        // For SOAP Receiver (outbound), this method sends data TO external SOAP service
        return invokeSoapService(criteria);
    }
    
    protected AdapterResult doReceive() throws Exception {
        throw new AdapterException.OperationException(AdapterType.SOAP, 
                "SOAP Receiver requires payload for service invocation");
    }
    
    private AdapterResult invokeSoapService(Object payload) throws Exception {
        SOAPConnection soapConnection = null;
        
        try {
            // Create SOAP message
            SOAPMessage soapRequest = createSoapMessage(payload);
            
            // Add authentication if configured
            if (config.getUsername() != null && !config.getUsername().isEmpty()) {
                addSecurityHeader(soapRequest);
            }
            
            // Add custom headers if configured
            if (config.getCustomHeaders() != null && !config.getCustomHeaders().isEmpty()) {
                addCustomHeaders(soapRequest, config.getCustomHeaders());
            }
            
            // Set SOAP action if configured
            if (config.getSoapAction() != null && !config.getSoapAction().isEmpty()) {
                soapRequest.getMimeHeaders().addHeader("SOAPAction", config.getSoapAction());
            }
            
            // Log outgoing message if configured
            if (config.isLogMessages()) {
                logger.debug("Sending SOAP request: {}", soapMessageToString(soapRequest));
            }
            
            // Send SOAP message
            soapConnection = soapConnectionFactory.createConnection();
            SOAPMessage soapResponse = soapConnection.call(soapRequest, config.getEffectiveEndpoint());
            
            // Process response
            Map<String, Object> responseData = processSoapResponse(soapResponse);
            
            // Log response if configured
            if (config.isLogMessages()) {
                logger.debug("Received SOAP response: {}", responseData);
            }
            
            logger.info("SOAP receiver adapter successfully invoked service");
            
            return AdapterResult.success(responseData, 
                    "Successfully invoked SOAP service and received response");
                    
        } catch (SOAPException soapEx) {
            if (soapEx.getCause() instanceof SOAPFault) {
                SOAPFault fault = (SOAPFault) soapEx.getCause();
                logger.error("SOAP fault received: {}", fault.getFaultString());
                throw new AdapterException.OperationException(AdapterType.SOAP, 
                        "SOAP fault: " + fault.getFaultString(), soapEx);
            } else {
                logger.error("SOAP exception: {}", soapEx.getMessage());
                throw new AdapterException.OperationException(AdapterType.SOAP, 
                        "SOAP exception: " + soapEx.getMessage(), soapEx);
            }
        } finally {
            if (soapConnection != null) {
                try {
                    soapConnection.close();
                } catch (Exception e) {
                    logger.warn("Error closing SOAP connection", e);
                }
            }
        }
    }
    
    private SOAPMessage createSoapMessage(Object payload) throws Exception {
        SOAPMessage soapMessage = messageFactory.createMessage();
        SOAPBody soapBody = soapMessage.getSOAPBody();
        
        if (payload instanceof String) {
            // Parse XML string to SOAP body
            String xmlPayload = (String) payload;
            Source xmlSource = new StreamSource(new StringReader(xmlPayload));
            
            TransformerFactory transformerFactory = TransformerFactory.newInstance();
            Transformer transformer = transformerFactory.newTransformer();
            DOMResult result = new DOMResult(soapBody);
            transformer.transform(xmlSource, result);
            
        } else if (payload instanceof Map) {
            // Convert Map to SOAP body elements
            Map<String, Object> dataMap = (Map<String, Object>) payload;
            createSoapBodyFromMap(soapBody, dataMap);
            
        } else {
            throw new AdapterException.ValidationException(AdapterType.SOAP, 
                    "Unsupported payload type: " + payload.getClass().getName());
        }
        
        soapMessage.saveChanges();
        return soapMessage;
    }
    
    private void createSoapBodyFromMap(SOAPBody soapBody, Map<String, Object> dataMap) throws Exception {
        String namespace = config.getTargetNamespace();
        String operation = config.getOperationName();
        
        if (operation == null || operation.isEmpty()) {
            operation = "Request"; // Default operation name
        }
        
        SOAPElement operationElement = soapBody.addChildElement(operation, "ns", namespace);
        
        for (Map.Entry<String, Object> entry : dataMap.entrySet()) {
            SOAPElement element = operationElement.addChildElement(entry.getKey());
            element.addTextNode(String.valueOf(entry.getValue()));
        }
    }
    
    private void addSecurityHeader(SOAPMessage soapMessage) throws Exception {
        SOAPHeader soapHeader = soapMessage.getSOAPHeader();
        if (soapHeader == null) {
            soapHeader = soapMessage.getSOAPPart().getEnvelope().addHeader();
        }
        
        // Add WS-Security header
        String wsseNamespace = "http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-wssecurity-secext-1.0.xsd";
        String wsuNamespace = "http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-wssecurity-utility-1.0.xsd";
        
        SOAPElement securityElement = soapHeader.addChildElement("Security", "wsse", wsseNamespace);
        securityElement.addAttribute(new QName("mustUnderstand"), "1");
        
        SOAPElement usernameTokenElement = securityElement.addChildElement("UsernameToken", "wsse");
        usernameTokenElement.addAttribute(new QName(wsuNamespace, "Id", "wsu"), "UsernameToken-1");
        
        SOAPElement usernameElement = usernameTokenElement.addChildElement("Username", "wsse");
        usernameElement.addTextNode(config.getUsername());
        
        SOAPElement passwordElement = usernameTokenElement.addChildElement("Password", "wsse");
        passwordElement.setAttribute("Type", "http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-username-token-profile-1.0#PasswordText");
        passwordElement.addTextNode(config.getPassword());
    }
    
    private void addCustomHeaders(SOAPMessage soapMessage, String customHeaders) throws Exception {
        SOAPHeader soapHeader = soapMessage.getSOAPHeader();
        if (soapHeader == null) {
            soapHeader = soapMessage.getSOAPPart().getEnvelope().addHeader();
        }
        
        // Parse custom headers (format: key1=value1,key2=value2)
        String[] headerPairs = customHeaders.split(",");
        for (String headerPair : headerPairs) {
            String[] keyValue = headerPair.split("=");
            if (keyValue.length == 2) {
                SOAPElement headerElement = soapHeader.addChildElement(keyValue[0].trim());
                headerElement.addTextNode(keyValue[1].trim());
            }
        }
    }
    
    private Map<String, Object> processSoapResponse(SOAPMessage soapResponse) throws Exception {
        Map<String, Object> responseData = new HashMap<>();
        
        // Check for SOAP fault
        SOAPBody soapBody = soapResponse.getSOAPBody();
        if (soapBody.hasFault()) {
            SOAPFault fault = soapBody.getFault();
            responseData.put("fault", true);
            responseData.put("faultCode", fault.getFaultCode());
            responseData.put("faultString", fault.getFaultString());
            responseData.put("faultActor", fault.getFaultActor());
            if (fault.getDetail() != null) {
                responseData.put("faultDetail", fault.getDetail().getTextContent());
            }
        } else {
            // Extract response body
            responseData.put("fault", false);
            responseData.put("body", soapMessageToString(soapResponse));
            
            // Extract specific elements if needed
            Iterator<?> bodyElements = soapBody.getChildElements();
            Map<String, Object> bodyData = new HashMap<>();
            while (bodyElements.hasNext()) {
                Object element = bodyElements.next();
                if (element instanceof SOAPElement) {
                    SOAPElement soapElement = (SOAPElement) element;
                    extractElementData(soapElement, bodyData);
                }
            }
            responseData.put("parsedBody", bodyData);
        }
        
        responseData.put("timestamp", new Date());
        
        return responseData;
    }
    
    private void extractElementData(SOAPElement element, Map<String, Object> data) {
        String elementName = element.getLocalName();
        
        // Check if element has child elements
        Iterator<?> children = element.getChildElements();
        if (children.hasNext()) {
            Map<String, Object> childData = new HashMap<>();
            while (children.hasNext()) {
                Object child = children.next();
                if (child instanceof SOAPElement) {
                    extractElementData((SOAPElement) child, childData);
                }
            }
            data.put(elementName, childData);
        } else {
            // Leaf element - get text content
            data.put(elementName, element.getTextContent());
        }
    }
    
    private String soapMessageToString(SOAPMessage soapMessage) throws Exception {
        java.io.ByteArrayOutputStream baos = new java.io.ByteArrayOutputStream();
        soapMessage.writeTo(baos);
        return baos.toString();
    }
    
    private SOAPMessage createTestMessage() throws Exception {
        SOAPMessage message = messageFactory.createMessage();
        SOAPBody body = message.getSOAPBody();
        body.addChildElement("TestElement").addTextNode("Test");
        message.saveChanges();
        return message;
    }
    
    private void validateConfiguration() throws AdapterException.ConfigurationException {
        if (config.getEffectiveEndpoint() == null || config.getEffectiveEndpoint().trim().isEmpty()) {
            throw new AdapterException.ConfigurationException(AdapterType.SOAP, "Endpoint URL is required");
        }
        
        if (config.getSoapVersion() == null || config.getSoapVersion().trim().isEmpty()) {
            config.setSoapVersion("1.1"); // Default to SOAP 1.1
        }
    }
    
    @Override
    protected long getPollingIntervalMs() {
        // SOAP receivers typically don't poll, they push data
        return 0;
    }
    
    @Override
    public String getConfigurationSummary() {
        return String.format("SOAP Receiver (Outbound): %s, Action: %s, SOAP Version: %s", 
                config.getEffectiveEndpoint(),
                config.getSoapAction() != null ? config.getSoapAction() : "Not specified",
                config.getSoapVersion());
    }
}