package com.integrationlab.backend.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.integrationlab.data.model.CommunicationAdapter;
import com.integrationlab.data.model.FlowStatus;
import com.integrationlab.data.model.IntegrationFlow;
import com.integrationlab.data.repository.CommunicationAdapterRepository;
import com.integrationlab.data.repository.IntegrationFlowRepository;
import com.integrationlab.shared.dto.DeploymentInfoDTO;
import com.integrationlab.shared.dto.IntegrationFlowDTO;
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
        
        IntegrationFlow flow = flowRepository.findById(flowId)
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
        
        // Update flow with deployment info
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
    }
    
    /**
     * Undeploy an integration flow
     */
    public void undeployFlow(String flowId, String userId) throws Exception {
        logger.info("Undeploying flow: {} by user: {}", flowId, userId);
        
        IntegrationFlow flow = flowRepository.findById(flowId)
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
        IntegrationFlow flow = flowRepository.findById(flowId)
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
        
        if (!flow.isActive()) {
            throw new IllegalStateException("Flow must be active to deploy");
        }
        
        if (flow.getSourceAdapterId() == null || flow.getTargetAdapterId() == null) {
            throw new IllegalStateException("Flow must have source and target adapters configured");
        }
    }
    
    private String generateEndpoint(IntegrationFlow flow, CommunicationAdapter sourceAdapter) {
        String baseUrl = String.format("%s://%s:%s", serverProtocol, serverHost, serverPort);
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
        metadata.put("adapterType", sourceAdapter.getType());
        metadata.put("adapterMode", sourceAdapter.getMode());
        
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
}