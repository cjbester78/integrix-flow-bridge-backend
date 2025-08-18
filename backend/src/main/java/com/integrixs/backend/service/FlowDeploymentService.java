package com.integrixs.backend.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.integrixs.data.model.CommunicationAdapter;
import com.integrixs.data.model.FlowStatus;
import com.integrixs.data.model.IntegrationFlow;
import com.integrixs.data.repository.CommunicationAdapterRepository;
import com.integrixs.data.repository.IntegrationFlowRepository;
import com.integrixs.shared.dto.DeploymentInfoDTO;
import com.integrixs.shared.dto.IntegrationFlowDTO;
import com.integrixs.shared.enums.AdapterType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Service
@Transactional
public class FlowDeploymentService {
    
    private static final Logger logger = LoggerFactory.getLogger(FlowDeploymentService.class);
    
    @Autowired
    private IntegrationFlowRepository flowRepository;
    
    @Autowired
    private CommunicationAdapterRepository adapterRepository;
    
    @Autowired
    private ObjectMapper objectMapper;
    
    @Value("${server.host:localhost}")
    private String serverHost;
    
    @Value("${server.port:8080}")
    private String serverPort;
    
    @Value("${server.protocol:http}")
    private String serverProtocol;
    
    /**
     * Deploy an integration flow
     */
    public DeploymentInfoDTO deployFlow(String flowId, String userId) throws Exception {
        logger.info("Deploying flow: {} by user: {}", flowId, userId);
        
        IntegrationFlow flow = flowRepository.findById(UUID.fromString(flowId))
            .orElseThrow(() -> new IllegalArgumentException("Flow not found: " + flowId));
        
        // Validate flow is ready for deployment
        validateFlowForDeployment(flow);
        
        // Get source adapter to determine endpoint type
        CommunicationAdapter sourceAdapter = adapterRepository.findById(flow.getSourceAdapterId())
            .orElseThrow(() -> new IllegalArgumentException("Source adapter not found"));
        
        // Generate deployment endpoint based on adapter type
        String endpoint = generateEndpoint(flow, sourceAdapter);
        
        // Create deployment metadata
        Map<String, Object> metadata = createDeploymentMetadata(flow, sourceAdapter, endpoint);
        
        try {
            // Register the flow endpoint
            registerFlowEndpoint(flow, sourceAdapter);
            
            // Initialize adapters
            initializeAdapters(flow, sourceAdapter);
            
            // Only update flow status after successful deployment
            flow.setStatus(FlowStatus.DEPLOYED_ACTIVE);
            flow.setDeployedAt(LocalDateTime.now());
            flow.setDeployedBy(userId);
            flow.setDeploymentEndpoint(endpoint);
            flow.setDeploymentMetadata(objectMapper.writeValueAsString(metadata));
            
            // Activate the flow
            flow.setActive(true);
            
            flowRepository.save(flow);
            
            logger.info("Flow deployed successfully: {} with endpoint: {}", flowId, endpoint);
            
            return DeploymentInfoDTO.builder()
                .flowId(flowId)
                .endpoint(endpoint)
                .deployedAt(flow.getDeployedAt())
                .metadata(metadata)
                .build();
        } catch (Exception e) {
            logger.error("Failed to deploy flow: {}", flowId, e);
            // Make sure we don't leave the flow in an inconsistent state
            flow.setStatus(FlowStatus.DEVELOPED_INACTIVE);
            flow.setActive(false);
            flowRepository.save(flow);
            throw new RuntimeException("Failed to deploy flow: " + e.getMessage(), e);
        }
    }
    
    /**
     * Undeploy an integration flow
     */
    public void undeployFlow(String flowId, String userId) throws Exception {
        logger.info("Undeploying flow: {} by user: {}", flowId, userId);
        
        IntegrationFlow flow = flowRepository.findById(UUID.fromString(flowId))
            .orElseThrow(() -> new IllegalArgumentException("Flow not found: " + flowId));
        
        // Validate flow is deployed
        if (flow.getStatus() != FlowStatus.DEPLOYED_ACTIVE) {
            throw new IllegalStateException("Flow is not deployed: " + flowId);
        }
        
        // Update flow status
        flow.setStatus(FlowStatus.DEVELOPED_INACTIVE);
        flow.setActive(false);
        flow.setDeploymentEndpoint(null);
        flow.setDeploymentMetadata(null);
        
        flowRepository.save(flow);
        
        logger.info("Flow undeployed successfully: {}", flowId);
    }
    
    /**
     * Get deployment information for a flow
     */
    public DeploymentInfoDTO getDeploymentInfo(String flowId) throws Exception {
        IntegrationFlow flow = flowRepository.findById(UUID.fromString(flowId))
            .orElseThrow(() -> new IllegalArgumentException("Flow not found: " + flowId));
        
        if (flow.getStatus() != FlowStatus.DEPLOYED_ACTIVE) {
            return null;
        }
        
        Map<String, Object> metadata = flow.getDeploymentMetadata() != null 
            ? objectMapper.readValue(flow.getDeploymentMetadata(), Map.class)
            : new HashMap<>();
        
        return DeploymentInfoDTO.builder()
            .flowId(flowId)
            .endpoint(flow.getDeploymentEndpoint())
            .deployedAt(flow.getDeployedAt())
            .deployedBy(flow.getDeployedBy())
            .metadata(metadata)
            .build();
    }
    
    private void validateFlowForDeployment(IntegrationFlow flow) {
        if (flow.getStatus() == FlowStatus.DEPLOYED_ACTIVE) {
            throw new IllegalStateException("Flow is already deployed");
        }
        
        // Don't check isActive for deployment - the deployment process will activate it
        
        if (flow.getSourceAdapterId() == null || flow.getTargetAdapterId() == null) {
            throw new IllegalStateException("Flow must have source and target adapters configured");
        }

        // Validate adapters are active
        CommunicationAdapter sourceAdapter = adapterRepository.findById(flow.getSourceAdapterId())
            .orElseThrow(() -> new IllegalArgumentException("Source adapter not found"));
        CommunicationAdapter targetAdapter = adapterRepository.findById(flow.getTargetAdapterId())
            .orElseThrow(() -> new IllegalArgumentException("Target adapter not found"));
            
        if (!sourceAdapter.isActive()) {
            throw new IllegalStateException("Cannot deploy flow: Source adapter '" + sourceAdapter.getName() + 
                "' is in a stopped status. Please activate the adapter before deploying the flow.");
        }
        if (!targetAdapter.isActive()) {
            throw new IllegalStateException("Cannot deploy flow: Target adapter '" + targetAdapter.getName() + 
                "' is in a stopped status. Please activate the adapter before deploying the flow.");
        }
    }
    
    private String generateEndpoint(IntegrationFlow flow, CommunicationAdapter sourceAdapter) {
        String baseUrl = String.format("%s://%s:%s", serverProtocol, serverHost, serverPort);
        
        // Check if adapter has configuration
        String configJson = sourceAdapter.getConfiguration();
        if (configJson != null) {
            try {
                Map<String, Object> config = objectMapper.readValue(configJson, Map.class);
                String connectionMode = (String) config.get("connectionMode");
                
                // If PUSH mode and endpoint is configured, use it
                if ("PUSH".equals(connectionMode) && config.containsKey("serviceEndpointUrl")) {
                    String configuredEndpoint = (String) config.get("serviceEndpointUrl");
                    if (configuredEndpoint != null && !configuredEndpoint.isEmpty()) {
                        // Ensure it starts with /
                        if (!configuredEndpoint.startsWith("/")) {
                            configuredEndpoint = "/" + configuredEndpoint;
                        }
                        // Remove any /soap prefix if already included to avoid duplication
                        if (configuredEndpoint.startsWith("/soap/")) {
                            return baseUrl + configuredEndpoint;
                        }
                        // Add /soap prefix for SOAP adapters
                        if (sourceAdapter.getType() == AdapterType.SOAP) {
                            return baseUrl + "/soap" + configuredEndpoint;
                        }
                        return baseUrl + configuredEndpoint;
                    }
                }
            } catch (Exception e) {
                logger.warn("Error parsing adapter configuration: {}", e.getMessage());
            }
        }
        
        // Fall back to auto-generated endpoint
        String flowPath = flow.getName().toLowerCase().replaceAll("[^a-zA-Z0-9-]", "-");
        
        switch (sourceAdapter.getType()) {
            case HTTP:
            case REST:
                return String.format("%s/api/integration/%s", baseUrl, flowPath);
                
            case SOAP:
                return String.format("%s/soap/%s", baseUrl, flowPath);
                
            case FILE:
            case FTP:
            case SFTP:
                // File-based adapters don't have HTTP endpoints
                return String.format("file://%s/%s", getFileBasePath(), flowPath);
                
            default:
                return String.format("%s/integration/%s", baseUrl, flowPath);
        }
    }
    
    private Map<String, Object> createDeploymentMetadata(IntegrationFlow flow, 
                                                        CommunicationAdapter sourceAdapter,
                                                        String endpoint) {
        Map<String, Object> metadata = new HashMap<>();
        metadata.put("flowName", flow.getName());
        metadata.put("adapterType", sourceAdapter.getType().toString());
        metadata.put("adapterMode", sourceAdapter.getMode().toString());
        
        // Add adapter-specific metadata
        switch (sourceAdapter.getType()) {
            case SOAP:
                metadata.put("wsdlUrl", endpoint + "?wsdl");
                metadata.put("soapVersion", "1.1/1.2");
                break;
                
            case REST:
                metadata.put("apiDocsUrl", endpoint + "/docs");
                metadata.put("openApiUrl", endpoint + "/openapi.json");
                break;
                
            case HTTP:
                metadata.put("httpMethods", "POST, GET");
                metadata.put("contentType", "application/json, application/xml");
                break;
                
            case FILE:
            case FTP:
            case SFTP:
                metadata.put("pollingEnabled", true);
                metadata.put("filePattern", "*.*");
                break;
        }
        
        return metadata;
    }
    
    private String getFileBasePath() {
        // This should come from configuration
        return "/opt/integrixflowbridge/flows";
    }
    
    private void registerFlowEndpoint(IntegrationFlow flow, CommunicationAdapter sourceAdapter) {
        logger.info("Registering endpoint for flow: {} with adapter type: {}", 
                   flow.getName(), sourceAdapter.getType());
        
        // The endpoints are already registered via the IntegrationEndpointController
        // This method is for any additional registration logic needed
        
        switch (sourceAdapter.getType()) {
            case FILE:
            case FTP:
            case SFTP:
                // File-based adapters may need directory setup
                setupFileBasedEndpoint(flow, sourceAdapter);
                break;
            default:
                // HTTP-based endpoints are handled by the controller
                logger.info("HTTP endpoint registered: {}", flow.getDeploymentEndpoint());
        }
    }
    
    private void initializeAdapters(IntegrationFlow flow, CommunicationAdapter sourceAdapter) {
        logger.info("Initializing adapters for flow: {}", flow.getName());
        
        // Initialize source adapter if needed
        if (sourceAdapter.getMode() == com.integrixs.adapters.core.AdapterMode.RECEIVER) {
            // Receiver adapters may need polling setup
            setupPollingAdapter(flow, sourceAdapter);
        }
        
        // TODO: Initialize target adapter if needed
    }
    
    private void setupFileBasedEndpoint(IntegrationFlow flow, CommunicationAdapter adapter) {
        String configJson = adapter.getConfiguration();
        if (configJson == null || configJson.isEmpty()) {
            return;
        }
        
        try {
            Map<String, Object> config = objectMapper.readValue(configJson, Map.class);
            String directory = (String) config.get("directory");
            
            if (directory != null) {
                try {
                    java.nio.file.Files.createDirectories(java.nio.file.Paths.get(directory));
                    logger.info("Created directory for file adapter: {}", directory);
                } catch (Exception e) {
                    logger.error("Failed to create directory: {}", directory, e);
                }
            }
        } catch (Exception e) {
            logger.error("Failed to parse adapter configuration", e);
        }
    }
    
    private void setupPollingAdapter(IntegrationFlow flow, CommunicationAdapter adapter) {
        // TODO: Implement polling mechanism for receiver adapters
        logger.info("Polling setup would be configured here for adapter: {}", adapter.getName());
    }
}