package com.integrixs.backend.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.integrixs.backend.exception.ResourceNotFoundException;
import com.integrixs.backend.security.SecurityUtils;
import com.integrixs.data.model.SystemLog;
import com.integrixs.data.repository.SystemLogRepository;
import com.integrixs.data.model.*;
import com.integrixs.data.repository.*;
import com.integrixs.shared.dto.*;
import com.integrixs.shared.dto.business.BusinessComponentDTO;
import com.integrixs.shared.dto.certificate.CertificateDTO;
import com.integrixs.shared.dto.export.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Service for exporting integration flows with all dependencies.
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class FlowExportService {

    private final IntegrationFlowRepository integrationFlowRepository;
    private final CommunicationAdapterRepository communicationAdapterRepository;
    private final BusinessComponentRepository businessComponentRepository;
    private final FlowTransformationRepository flowTransformationRepository;
    private final FieldMappingRepository fieldMappingRepository;
    private final CertificateRepository certificateRepository;
    private final SystemLogRepository systemLogRepository;
    private final ObjectMapper objectMapper;

    @Value("${app.version:1.0.0}")
    private String applicationVersion;

    /**
     * Export a flow with all its dependencies.
     *
     * @param request Export request with options
     * @return Exported flow data
     */
    public FlowExportDTO exportFlow(FlowExportRequestDTO request) {
        log.info("Exporting flow: {}", request.getFlowId());
        
        // Load the flow
        IntegrationFlow flow = integrationFlowRepository.findById(UUID.fromString(request.getFlowId()))
                .orElseThrow(() -> new ResourceNotFoundException("Flow not found: " + request.getFlowId()));
        
        // Build export
        FlowExportDTO export = FlowExportDTO.builder()
                .metadata(buildExportMetadata(request, flow))
                .flow(convertToFlowDTO(flow))
                .build();
        
        // Include dependencies based on options
        FlowExportRequestDTO.ExportOptions options = request.getOptions();
        
        // Load and include adapters
        if (options.isIncludeAdapterConfigs()) {
            export.setSourceAdapter(loadAdapter(flow.getSourceAdapterId() != null ? flow.getSourceAdapterId().toString() : null));
            export.setTargetAdapter(loadAdapter(flow.getTargetAdapterId() != null ? flow.getTargetAdapterId().toString() : null));
            
            // Include certificate references
            if (options.isIncludeCertificateReferences()) {
                Set<String> certificateIds = new HashSet<>();
                certificateIds.addAll(extractCertificateIds(export.getSourceAdapter()));
                certificateIds.addAll(extractCertificateIds(export.getTargetAdapter()));
                
                List<FlowExportDTO.CertificateReferenceDTO> certRefs = certificateIds.stream()
                        .map(this::createCertificateReference)
                        .filter(Objects::nonNull)
                        .collect(Collectors.toList());
                
                export.setCertificateReferences(certRefs);
            }
        }
        
        // Include business component
        if (options.isIncludeBusinessComponent() && flow.getBusinessComponent() != null) {
            export.setBusinessComponent(convertToBusinessComponentDTO(flow.getBusinessComponent()));
        }
        
        // Load transformations and field mappings
        List<FlowTransformation> transformations = flowTransformationRepository.findByFlowId(flow.getId());
        export.setTransformations(transformations.stream()
                .map(this::convertToFlowTransformationDTO)
                .collect(Collectors.toList()));
        
        // Flatten field mappings for easy access
        List<FieldMappingDTO> allMappings = new ArrayList<>();
        for (FlowTransformation transformation : transformations) {
            List<FieldMapping> mappings = fieldMappingRepository.findByTransformationId(transformation.getId());
            allMappings.addAll(mappings.stream()
                    .map(this::convertToFieldMappingDTO)
                    .collect(Collectors.toList()));
        }
        export.setFieldMappings(allMappings);
        
        // Log the export
        logExportActivity(flow, request);
        
        return export;
    }

    /**
     * Validate if a flow can be exported.
     *
     * @param flowId Flow ID to validate
     * @return Validation result
     */
    public Map<String, Object> validateExport(String flowId) {
        Map<String, Object> validation = new HashMap<>();
        
        try {
            IntegrationFlow flow = integrationFlowRepository.findById(UUID.fromString(flowId))
                    .orElseThrow(() -> new ResourceNotFoundException("Flow not found"));
            
            validation.put("canExport", true);
            validation.put("flowName", flow.getName());
            validation.put("status", flow.getStatus().toString());
            
            // Check dependencies
            boolean hasSourceAdapter = communicationAdapterRepository.existsById(flow.getSourceAdapterId());
            boolean hasTargetAdapter = communicationAdapterRepository.existsById(flow.getTargetAdapterId());
            
            validation.put("hasSourceAdapter", hasSourceAdapter);
            validation.put("hasTargetAdapter", hasTargetAdapter);
            
            if (!hasSourceAdapter || !hasTargetAdapter) {
                validation.put("canExport", false);
                validation.put("reason", "Missing adapter dependencies");
            }
            
        } catch (Exception e) {
            validation.put("canExport", false);
            validation.put("error", e.getMessage());
        }
        
        return validation;
    }

    private FlowExportDTO.ExportMetadata buildExportMetadata(FlowExportRequestDTO request, IntegrationFlow flow) {
        FlowExportRequestDTO.ExportOptions options = request.getOptions();
        
        Map<String, String> tags = new HashMap<>();
        if (options.getTags() != null) {
            options.getTags().forEach(tag -> tags.put(tag, "true"));
        }
        tags.put("flowName", flow.getName());
        tags.put("flowStatus", flow.getStatus().toString());
        
        return FlowExportDTO.ExportMetadata.builder()
                .exportId(UUID.randomUUID().toString())
                .exportVersion("1.0")
                .applicationVersion(applicationVersion)
                .exportDate(LocalDateTime.now())
                .exportedBy(SecurityUtils.getCurrentUserId())
                .exportedByUsername(SecurityUtils.getCurrentUsernameStatic())
                .environment(options.getEnvironment())
                .description(options.getDescription())
                .tags(tags)
                .build();
    }

    private CommunicationAdapterDTO loadAdapter(String adapterId) {
        return communicationAdapterRepository.findById(UUID.fromString(adapterId))
                .map(this::convertToAdapterDTO)
                .orElse(null);
    }

    private Set<String> extractCertificateIds(CommunicationAdapterDTO adapter) {
        Set<String> certificateIds = new HashSet<>();
        
        if (adapter == null || adapter.getConfiguration() == null) {
            return certificateIds;
        }
        
        try {
            // Configuration is already a Map
            Map<String, Object> config = adapter.getConfiguration();
            
            // Common certificate field names
            String[] certFields = {"certificateId", "clientCertificateId", "serverCertificateId", 
                                 "truststoreId", "keystoreId", "sslCertificateId"};
            
            for (String field : certFields) {
                if (config.containsKey(field) && config.get(field) != null) {
                    certificateIds.add(config.get(field).toString());
                }
            }
            
        } catch (Exception e) {
            log.warn("Failed to extract certificate IDs from adapter config: {}", e.getMessage());
        }
        
        return certificateIds;
    }

    private FlowExportDTO.CertificateReferenceDTO createCertificateReference(String certificateId) {
        return certificateRepository.findById(UUID.fromString(certificateId))
                .map(cert -> FlowExportDTO.CertificateReferenceDTO.builder()
                        .id(cert.getId())
                        .name(cert.getName())
                        .type(cert.getType())
                        .format(cert.getFormat())
                        .fileName(cert.getFileName())
                        .passwordProtected(cert.getPassword() != null)
                        .checksum(calculateChecksum(cert.getContent()))
                        .build())
                .orElse(null);
    }

    private String calculateChecksum(byte[] content) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(content);
            return Base64.getEncoder().encodeToString(hash);
        } catch (Exception e) {
            log.error("Failed to calculate checksum", e);
            return null;
        }
    }

    private void logExportActivity(IntegrationFlow flow, FlowExportRequestDTO request) {
        try {
            Map<String, Object> details = new HashMap<>();
            details.put("flowId", flow.getId());
            details.put("flowName", flow.getName());
            details.put("exportOptions", request.getOptions());
            
            SystemLog log = SystemLog.builder()
                    .timestamp(LocalDateTime.now())
                    .level(SystemLog.LogLevel.INFO)
                    .message("Integration flow exported")
                    .details(objectMapper.writeValueAsString(details))
                    .source("EXPORT_SERVICE")
                    .category("FLOW_EXPORT")
                    .userId(SecurityUtils.getCurrentUserId())
                    .username(SecurityUtils.getCurrentUsernameStatic())
                    .domainType("INTEGRATION_FLOW")
                    .domainReferenceId(flow.getId().toString())
                    .build();
            
            systemLogRepository.save(log);
            
        } catch (Exception e) {
            log.error("Failed to log export activity", e);
        }
    }

    // Conversion methods

    private FlowDTO convertToFlowDTO(IntegrationFlow flow) {
        return FlowDTO.builder()
                .id(flow.getId().toString())
                .name(flow.getName())
                .description(flow.getDescription())
                .sourceAdapterId(flow.getSourceAdapterId() != null ? flow.getSourceAdapterId().toString() : null)
                .targetAdapterId(flow.getTargetAdapterId() != null ? flow.getTargetAdapterId().toString() : null)
                .sourceStructureId(flow.getSourceStructureId())
                .targetStructureId(flow.getTargetStructureId())
                .status(flow.getStatus().toString())
                .configuration(flow.getConfiguration())
                // isActive not available in FlowDTO
                .mappingMode(flow.getMappingMode() != null ? flow.getMappingMode().toString() : null)
                .createdAt(flow.getCreatedAt())
                .updatedAt(flow.getUpdatedAt())
                .createdBy(flow.getCreatedBy() != null ? flow.getCreatedBy().getUsername() : null)
                .build();
    }

    private CommunicationAdapterDTO convertToAdapterDTO(CommunicationAdapter adapter) {
        return CommunicationAdapterDTO.builder()
                .id(adapter.getId().toString())
                .name(adapter.getName())
                .type(adapter.getType().toString())
                .mode(adapter.getMode().toString())
                .configuration(parseConfiguration(adapter.getConfiguration()))
                // isActive not in CommunicationAdapterDTO
                .description(adapter.getDescription())
                .businessComponentId(adapter.getBusinessComponentId())
                .build();
    }

    private BusinessComponentDTO convertToBusinessComponentDTO(BusinessComponent component) {
        return BusinessComponentDTO.builder()
                .id(component.getId().toString())
                .name(component.getName())
                .description(component.getDescription())
                .contactEmail(component.getContactEmail())
                .contactPhone(component.getContactPhone())
                .createdAt(component.getCreatedAt())
                .updatedAt(component.getUpdatedAt())
                .build();
    }

    private FlowTransformationDTO convertToFlowTransformationDTO(FlowTransformation transformation) {
        return FlowTransformationDTO.builder()
                .id(transformation.getId().toString())
                .flowId(transformation.getFlow().getId().toString())
                .name(transformation.getName())
                .description(transformation.getDescription())
                .transformationType(com.integrixs.shared.enums.TransformationType.valueOf(transformation.getType().name()))
                .configuration(transformation.getConfiguration())
                .sequence(transformation.getExecutionOrder())
                .isActive(transformation.isActive())
                .createdAt(transformation.getCreatedAt())
                .updatedAt(transformation.getUpdatedAt())
                .build();
    }

    private FieldMappingDTO convertToFieldMappingDTO(FieldMapping mapping) {
        return FieldMappingDTO.builder()
                .id(mapping.getId())
                .transformationId(mapping.getTransformation().getId().toString())
                .sourceFields(mapping.getSourceFieldsList())
                .targetField(mapping.getTargetField())
                .javaFunction(mapping.getJavaFunction())
                .mappingRule(mapping.getMappingRule())
                .sourceXPath(mapping.getSourceXPath())
                .targetXPath(mapping.getTargetXPath())
                .isArrayMapping(mapping.isArrayMapping())
                .arrayContextPath(mapping.getArrayContextPath())
                .namespaceAware(mapping.isNamespaceAware())
                .inputTypes(mapping.getInputTypes())
                .outputType(mapping.getOutputType())
                .description(mapping.getDescription())
                .version(mapping.getVersion())
                .functionName(mapping.getFunctionName())
                .isActive(mapping.isActive())
                .createdAt(mapping.getCreatedAt())
                .updatedAt(mapping.getUpdatedAt())
                .build();
    }
    
    private Map<String, Object> parseConfiguration(String configJson) {
        if (configJson == null || configJson.isEmpty()) {
            return new HashMap<>();
        }
        try {
            return objectMapper.readValue(configJson, 
                objectMapper.getTypeFactory().constructMapType(HashMap.class, String.class, Object.class));
        } catch (Exception e) {
            log.warn("Failed to parse configuration JSON: {}", e.getMessage());
            return new HashMap<>();
        }
    }
}