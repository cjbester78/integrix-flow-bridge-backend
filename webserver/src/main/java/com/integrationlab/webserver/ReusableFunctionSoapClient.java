/**
 * ReusableFunctionSoapClient handles SOAP-based calls to retrieve reusable function definitions.
 * It builds SOAP requests and interprets JAXB responses mapped to Java objects.
 */
package com.integrationlab.webserver;

import com.integrationlab.shared.dto.transformation.ReusableJavaFunctionDTO;
import com.integrationlab.soapbindings.GetReusableFunctionRequest;
import com.integrationlab.soapbindings.GetReusableFunctionResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.oxm.jaxb.Jaxb2Marshaller;
import org.springframework.stereotype.Component;
import org.springframework.ws.client.core.WebServiceTemplate;
import org.springframework.ws.soap.client.core.SoapActionCallback;

@Component
public class ReusableFunctionSoapClient {

    private static final Logger logger = LoggerFactory.getLogger(ReusableFunctionSoapClient.class);

    private final Jaxb2Marshaller marshaller;
    private final String defaultSoapEndpoint;

    public ReusableFunctionSoapClient(@Value("${soap.reusable.function.endpoint}") String defaultSoapEndpoint) {
        this.defaultSoapEndpoint = defaultSoapEndpoint;

        this.marshaller = new Jaxb2Marshaller();
        this.marshaller.setContextPath("com.integrationlab.soapbindings"); // package with JAXB-generated classes
        try {
            this.marshaller.afterPropertiesSet();
        } catch (Exception e) {
            throw new RuntimeException("Failed to initialize JAXB marshaller", e);
        }
    }

    /**
     * Call SOAP service with given parameters.
     * @param id Function ID to query
     * @param soapEndpoint optional SOAP endpoint URL; if null or blank, uses default from config
     * @param soapAction SOAP action URI
     * @return DTO with reusable function data
     */
    public ReusableJavaFunctionDTO getFunctionById(String id, String soapEndpoint, String soapAction) {
        String endpointToUse = (soapEndpoint == null || soapEndpoint.isBlank()) ? defaultSoapEndpoint : soapEndpoint;

        GetReusableFunctionRequest request = new GetReusableFunctionRequest();
        request.setId(id);

        WebServiceTemplate webServiceTemplate = new WebServiceTemplate(marshaller);
        webServiceTemplate.setUnmarshaller(marshaller);
        webServiceTemplate.setDefaultUri(endpointToUse);

        try {
            GetReusableFunctionResponse response = (GetReusableFunctionResponse) webServiceTemplate
                .marshalSendAndReceive(request, new SoapActionCallback(soapAction));

            return convertResponseToDTO(response);
        } catch (Exception e) {
            logger.error("SOAP request failed to endpoint {} with action {}", endpointToUse, soapAction, e);
            throw new RuntimeException("Failed to get reusable function by id", e);
        }
    }

    private ReusableJavaFunctionDTO convertResponseToDTO(GetReusableFunctionResponse response) {
        var func = response.getFunction();
        ReusableJavaFunctionDTO dto = new ReusableJavaFunctionDTO();
        dto.setId(func.getId());
        dto.setName(func.getName());
        dto.setVersion(func.getVersion());
        dto.setFunctionBody(func.getFunctionBody());
        dto.setInputTypes(func.getInputTypes());
        dto.setOutputType(func.getOutputType());
        dto.setDescription(func.getDescription());
        dto.setCreatedAt(func.getCreatedAt());
        dto.setUpdatedAt(func.getUpdatedAt());
        return dto;
    }
}