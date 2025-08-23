package com.integrixs.webclient;

import com.integrixs.adapters.core.AdapterResult;
import com.integrixs.adapters.core.ReceiverAdapter;
import com.integrixs.adapters.factory.AdapterFactoryManager;
import com.integrixs.adapters.core.AdapterType;
import com.integrixs.adapters.config.SoapReceiverAdapterConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ws.server.endpoint.annotation.Endpoint;
import org.springframework.ws.server.endpoint.annotation.PayloadRoot;
import org.springframework.ws.server.endpoint.annotation.RequestPayload;
import org.springframework.ws.server.endpoint.annotation.ResponsePayload;

import javax.xml.transform.Source;
import javax.xml.transform.dom.DOMSource;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * Inbound SOAP Endpoint for receiving SOAP requests from external systems.
 * Routes incoming SOAP messages through the adapter framework for processing.
 */
@Endpoint
public class InboundSoapEndpoint {

    private static final Logger logger = LoggerFactory.getLogger(InboundSoapEndpoint.class);
    private static final String NAMESPACE_URI = "http://integrationlab.com/inbound";
    
    private final AdapterFactoryManager adapterFactory;

    public InboundSoapEndpoint() {
        this.adapterFactory = AdapterFactoryManager.getInstance();
    }

    /**
     * Handle generic inbound SOAP requests
     */
    @PayloadRoot(namespace = NAMESPACE_URI, localPart = "InboundSoapRequest")
    @ResponsePayload
    public Source handleInboundSoapRequest(@RequestPayload Source request) {
        
        logger.info("Received inbound SOAP request");
        
        try {
            // Create SOAP receiver adapter configuration
            SoapReceiverAdapterConfig config = createSoapConfig();
            
            // Create and initialize adapter
            ReceiverAdapter adapter = adapterFactory.createReceiver(AdapterType.SOAP, config);
            adapter.initialize();
            
            try {
                // Process the inbound SOAP message
                AdapterResult result = adapter.receive(request);
                
                if (result.isSuccess()) {
                    logger.info("Successfully processed inbound SOAP message");
                    return createSuccessResponse("Message processed successfully");
                } else {
                    logger.error("Failed to process inbound SOAP message: {}", result.getMessage());
                    return createErrorResponse("Failed to process message: " + result.getMessage());
                }
            } finally {
                adapter.destroy();
            }
            
        } catch (Exception e) {
            logger.error("Error processing inbound SOAP request", e);
            return createErrorResponse("Internal server error: " + e.getMessage());
        }
    }


    /**
     * Create SOAP receiver adapter configuration
     */
    private SoapReceiverAdapterConfig createSoapConfig() {
        SoapReceiverAdapterConfig config = new SoapReceiverAdapterConfig();
        
        // Set basic SOAP configuration
        config.setServiceEndpointUrl("http://localhost:8080/ws/inbound");
        config.setSoapAction("process");
        
        return config;
    }

    /**
     * Create success response
     */
    private Source createSuccessResponse(String message) {
        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            factory.setNamespaceAware(true);
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document document = builder.newDocument();
            
            Element responseElement = document.createElementNS(NAMESPACE_URI, "InboundSoapResponse");
            Element statusElement = document.createElement("status");
            statusElement.setTextContent("SUCCESS");
            Element messageElement = document.createElement("message");
            messageElement.setTextContent(message);
            
            responseElement.appendChild(statusElement);
            responseElement.appendChild(messageElement);
            document.appendChild(responseElement);
            
            return new DOMSource(document);
        } catch (Exception e) {
            logger.error("Error creating success response", e);
            return createErrorResponse("Error creating response");
        }
    }

    /**
     * Create error response
     */
    private Source createErrorResponse(String errorMessage) {
        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            factory.setNamespaceAware(true);
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document document = builder.newDocument();
            
            Element responseElement = document.createElementNS(NAMESPACE_URI, "InboundSoapResponse");
            Element statusElement = document.createElement("status");
            statusElement.setTextContent("ERROR");
            Element messageElement = document.createElement("message");
            messageElement.setTextContent(errorMessage);
            
            responseElement.appendChild(statusElement);
            responseElement.appendChild(messageElement);
            document.appendChild(responseElement);
            
            return new DOMSource(document);
        } catch (Exception e) {
            logger.error("Error creating error response", e);
            // Return a simple fallback response
            return null;
        }
    }

}