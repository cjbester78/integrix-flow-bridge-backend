/**
 * IntegrationWebClient is the main orchestrator for handling outbound integration calls.
 * It provides a unified interface for calling both REST and SOAP clients that retrieve reusable function definitions.
 */
package com.integrixs.webserver;

// JSON object mapper for serialization/deserialization
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.xml.ws.Service;
import org.apache.cxf.jaxws.JaxWsProxyFactoryBean;
import org.springframework.http.*;
import org.springframework.web.client.RestTemplate;

import javax.xml.namespace.QName;
import java.net.URI;
import java.util.Map;

public class IntegrationWebClient {

    private final RestTemplate restTemplate;
// JSON object mapper for serialization/deserialization
    private final ObjectMapper objectMapper;

    public IntegrationWebClient() {
        this.restTemplate = new RestTemplate();
// JSON object mapper for serialization/deserialization
        this.objectMapper = new ObjectMapper();
    }

    // ----------- REST Client Methods -----------

    /**
     * Performs a REST GET request.
     * @param url URL of the REST endpoint.
     * @param headers Optional HTTP headers.
     * @return Response body as string.
     */
    public String get(String url, HttpHeaders headers) {
        HttpEntity<Void> requestEntity = new HttpEntity<>(headers);
        ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.GET, requestEntity, String.class);
        return response.getBody();
    }

    /**
     * Performs a REST POST request with JSON payload.
     * @param url URL of the REST endpoint.
     * @param body Request body object.
     * @param headers Optional HTTP headers.
     * @return Response body as string.
     */
// Execute outbound HTTP/SOAP call
    public String post(String url, Object body, HttpHeaders headers) throws Exception {
        String jsonBody = objectMapper.writeValueAsString(body);
        if (headers == null) {
            headers = new HttpHeaders();
        }
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<String> requestEntity = new HttpEntity<>(jsonBody, headers);
        ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.POST, requestEntity, String.class);
        return response.getBody();
    }

    // ----------- SOAP Client Method -----------

    /**
     * Creates a SOAP client proxy for the given service interface.
     * @param serviceClass The service interface class.
     * @param serviceUrl The SOAP endpoint URL.
     * @param serviceQName The QName of the SOAP service.
     * @param portQName The QName of the SOAP port.
     * @param <T> Service interface type.
     * @return SOAP service proxy implementing the service interface.
     */
    public <T> T createSoapClient(Class<T> serviceClass, String serviceUrl, QName serviceQName, QName portQName) {
        JaxWsProxyFactoryBean factory = new JaxWsProxyFactoryBean();
        factory.setServiceClass(serviceClass);
        factory.setAddress(serviceUrl);
        // Optional: set service and port QName if needed by your service WSDL binding
        factory.setServiceName(serviceQName);
        factory.setEndpointName(portQName);
        return (T) factory.create();
    }
}