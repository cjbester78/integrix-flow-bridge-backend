/**
 * ReusableFunctionRestClient handles calling RESTful APIs to fetch reusable function definitions.
 * It uses a configurable endpoint and provides deserialization of JSON responses.
 */
package com.integrationlab.webserver;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.*;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.support.BasicAuthenticationInterceptor;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestTemplate;

import com.integrationlab.shared.dto.transformation.ReusableJavaFunctionDTO;

import java.util.Arrays;
import java.util.List;

public class ReusableFunctionRestClient {

    private static final Logger logger = LoggerFactory.getLogger(ReusableFunctionRestClient.class);

    private final RestTemplate restTemplate;
    private final String baseUrl;

    public ReusableFunctionRestClient(String baseUrl, String username, String password) {
        this.restTemplate = new RestTemplate();

        // Add Basic Auth interceptor
        ClientHttpRequestInterceptor authInterceptor = new BasicAuthenticationInterceptor(username, password);
        restTemplate.getInterceptors().add(authInterceptor);

        this.baseUrl = baseUrl.endsWith("/") ? baseUrl : baseUrl + "/";
    }

    public List<ReusableJavaFunctionDTO> getAllFunctions() {
        String url = baseUrl + "api/reusable-functions";
        try {
            ResponseEntity<ReusableJavaFunctionDTO[]> response = restTemplate.getForEntity(url, ReusableJavaFunctionDTO[].class);
            return Arrays.asList(response.getBody());
        } catch (HttpStatusCodeException e) {
            logger.error("Failed to get all reusable functions: {} - {}", e.getStatusCode(), e.getResponseBodyAsString());
            throw e;
        }
    }

    public ReusableJavaFunctionDTO getFunctionById(String id) {
        String url = baseUrl + "api/reusable-functions/" + id;
        try {
            return restTemplate.getForObject(url, ReusableJavaFunctionDTO.class);
        } catch (HttpStatusCodeException e) {
            logger.error("Failed to get reusable function by id {}: {} - {}", id, e.getStatusCode(), e.getResponseBodyAsString());
            throw e;
        }
    }

    public ReusableJavaFunctionDTO createFunction(ReusableJavaFunctionDTO dto) {
        String url = baseUrl + "api/reusable-functions";
        try {
// Execute outbound HTTP/SOAP call
            return restTemplate.postForObject(url, dto, ReusableJavaFunctionDTO.class);
        } catch (HttpStatusCodeException e) {
            logger.error("Failed to create reusable function: {} - {}", e.getStatusCode(), e.getResponseBodyAsString());
            throw e;
        }
    }

    public void updateFunction(String id, ReusableJavaFunctionDTO dto) {
        String url = baseUrl + "api/reusable-functions/" + id;
        try {
            restTemplate.put(url, dto);
        } catch (HttpStatusCodeException e) {
            logger.error("Failed to update reusable function {}: {} - {}", id, e.getStatusCode(), e.getResponseBodyAsString());
            throw e;
        }
    }

    public void deleteFunction(String id) {
        String url = baseUrl + "api/reusable-functions/" + id;
        try {
            restTemplate.delete(url);
        } catch (HttpStatusCodeException e) {
            logger.error("Failed to delete reusable function {}: {} - {}", id, e.getStatusCode(), e.getResponseBodyAsString());
            throw e;
        }
    }
}