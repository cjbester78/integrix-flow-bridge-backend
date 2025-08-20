package com.integrixs.backend.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.integrixs.backend.exception.BusinessException;
import com.integrixs.backend.exception.ConflictException;
import com.integrixs.backend.security.SecurityUtils;
import com.integrixs.data.model.SystemLog;
import com.integrixs.data.repository.SystemLogRepository;
import com.integrixs.data.model.*;
import com.integrixs.data.repository.*;
import com.integrixs.shared.dto.*;
import com.integrixs.shared.dto.business.BusinessComponentDTO;
import com.integrixs.shared.dto.export.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Service for importing integration flows with validation and conflict resolution.
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class FlowImportService {

    private final IntegrationFlowRepository integrationFlowRepository;
    private final CommunicationAdapterRepository communicationAdapterRepository;
    private final BusinessComponentRepository businessComponentRepository;
    private final FlowTransformationRepository flowTransformationRepository;
    private final FieldMappingRepository fieldMappingRepository;
    private final CertificateRepository certificateRepository;
    private final SystemLogRepository systemLogRepository;
    private final ObjectMapper objectMapper;
    private final UserRepository userRepository;

    @Value("${app.version:1.0.0}")
    private String applicationVersion;

    /**
     * Validate an import before actually importing.
     *
     * @param request Import request
     * @return Validation result
     */
    @Transactional(readOnly = true)
    public FlowImportValidationDTO validateImport(FlowImportRequestDTO request) {
        log.info("Validating flow import: {}", request.getFlowExport().getFlow().getName());
        
        FlowImportValidationDTO validation = FlowImportValidationDTO.builder()
                .canImport(true)
                .isValid(true)
                .build();
        
        FlowExportDTO export = request.getFlowExport();
        FlowImportRequestDTO.ImportOptions options = request.getOptions();
        
        // Build preview
        validation.setPreview(buildImportPreview(export));
        
        // Check version compatibility
        validation.setVersionCompatibility(checkVersionCompatibility(export));
        if (!validation.getVersionCompatibility().isCompatible()) {
            validation.setCanImport(false);
            validation.setValid(false);
        }
        
        // Check for conflicts
        List<FlowImportValidationDTO.Conflict> conflicts = new ArrayList<>();
        
        // Check flow name conflict
        if (integrationFlowRepository.existsByName(export.getFlow().getName())) {
            conflicts.add(createNameConflict("IntegrationFlow", export.getFlow().getId(), 
                    export.getFlow().getName()));
        }
        
        // Check business component conflicts
        if (options.isImportBusinessComponent() && export.getBusinessComponent() != null) {
            if (businessComponentRepository.existsByName(export.getBusinessComponent().getName())) {
                conflicts.add(createNameConflict("BusinessComponent", 
                        export.getBusinessComponent().getId(), 
                        export.getBusinessComponent().getName()));
            }
        }
        
        // Check adapter conflicts
        if (options.isImportAdapters()) {
            if (export.getSourceAdapter() != null && 
                communicationAdapterRepository.existsByName(export.getSourceAdapter().getName())) {
                conflicts.add(createNameConflict("CommunicationAdapter", 
                        export.getSourceAdapter().getId(), 
                        export.getSourceAdapter().getName()));
            }
            
            if (export.getTargetAdapter() != null && 
                communicationAdapterRepository.existsByName(export.getTargetAdapter().getName())) {
                conflicts.add(createNameConflict("CommunicationAdapter", 
                        export.getTargetAdapter().getId(), 
                        export.getTargetAdapter().getName()));
            }
        }
        
        // Check certificate references
        if (options.isImportCertificateReferences() && export.getCertificateReferences() != null) {
            for (FlowExportDTO.CertificateReferenceDTO certRef : export.getCertificateReferences()) {
                if (!certificateRepository.existsById(UUID.fromString(certRef.getId()))) {
                    conflicts.add(createMissingReferenceConflict("Certificate", 
                            certRef.getId(), certRef.getName()));
                }
            }
        }
        
        validation.setConflicts(conflicts);
        
        // Handle conflicts based on strategy
        if (!conflicts.isEmpty()) {
            if (options.getConflictStrategy() == FlowImportRequestDTO.ConflictStrategy.FAIL) {
                validation.setCanImport(false);
                validation.getErrors().add(createValidationError("CONFLICTS_DETECTED", 
                        "Import conflicts detected and strategy is FAIL"));
            }
        }
        
        // Add required permissions
        validation.getRequiredPermissions().add("FLOW_IMPORT");
        if (options.isImportBusinessComponent()) {
            validation.getRequiredPermissions().add("BUSINESS_COMPONENT_CREATE");
        }
        if (options.isImportAdapters()) {
            validation.getRequiredPermissions().add("ADAPTER_CREATE");
        }
        
        return validation;
    }

    /**
     * Import a flow with all its dependencies.
     *
     * @param request Import request
     * @return Import result
     */
    public FlowImportResultDTO importFlow(FlowImportRequestDTO request) {
        long startTime = System.currentTimeMillis();
        
        log.info("Importing flow: {}", request.getFlowExport().getFlow().getName());
        
        FlowImportResultDTO result = FlowImportResultDTO.builder()
                .success(false)
                .build();
        
        try {
            // Validate first
            FlowImportValidationDTO validation = validateImport(request);
            if (!validation.isCanImport()) {
                result.getErrors().add(FlowImportResultDTO.ImportMessage.builder()
                        .code("VALIDATION_FAILED")
                        .message("Import validation failed")
                        .build());
                return result;
            }
            
            FlowExportDTO export = request.getFlowExport();
            FlowImportRequestDTO.ImportOptions options = request.getOptions();
            Map<String, String> idMappings = new HashMap<>();
            
            // Import business component if needed
            String businessComponentId = null;
            if (options.isImportBusinessComponent() && export.getBusinessComponent() != null) {
                BusinessComponent importedComponent = importBusinessComponent(
                        export.getBusinessComponent(), options, idMappings, result);
                if (importedComponent != null) {
                    businessComponentId = importedComponent.getId().toString();
                    result.getSummary().setBusinessComponentImported(true);
                }
            } else if (options.getTargetBusinessComponentId() != null) {
                businessComponentId = options.getTargetBusinessComponentId();
            }
            
            // Import adapters
            String sourceAdapterId = null;
            String targetAdapterId = null;
            
            if (options.isImportAdapters()) {
                if (export.getSourceAdapter() != null) {
                    CommunicationAdapter sourceAdapter = importAdapter(
                            export.getSourceAdapter(), businessComponentId, options, idMappings, result);
                    if (sourceAdapter != null) {
                        sourceAdapterId = sourceAdapter.getId().toString();
                        result.getSummary().setAdaptersImported(result.getSummary().getAdaptersImported() + 1);
                    }
                }
                
                if (export.getTargetAdapter() != null) {
                    CommunicationAdapter targetAdapter = importAdapter(
                            export.getTargetAdapter(), businessComponentId, options, idMappings, result);
                    if (targetAdapter != null) {
                        targetAdapterId = targetAdapter.getId().toString();
                        result.getSummary().setAdaptersImported(result.getSummary().getAdaptersImported() + 1);
                    }
                }
            } else {
                // Use existing adapters if not importing
                sourceAdapterId = export.getFlow().getSourceAdapterId();
                targetAdapterId = export.getFlow().getTargetAdapterId();
            }
            
            // Import the flow
            IntegrationFlow importedFlow = importIntegrationFlow(
                    export.getFlow(), businessComponentId, sourceAdapterId, targetAdapterId, 
                    options, idMappings, result);
            
            if (importedFlow != null) {
                result.setImportedFlowId(importedFlow.getId().toString());
                result.setImportedFlowName(importedFlow.getName());
                result.getSummary().setFlowImported(true);
                
                // Import transformations and field mappings
                importTransformationsAndMappings(export, importedFlow, idMappings, result);
                
                // Activate flow if requested
                if (options.isActivateAfterImport()) {
                    importedFlow.setActive(true);
                    integrationFlowRepository.save(importedFlow);
                }
                
                result.setSuccess(true);
            }
            
            // Set final statistics
            result.getSummary().setImportDurationMs(System.currentTimeMillis() - startTime);
            result.getSummary().setTotalObjectsImported(
                    (result.getSummary().isFlowImported() ? 1 : 0) +
                    (result.getSummary().isBusinessComponentImported() ? 1 : 0) +
                    result.getSummary().getAdaptersImported() +
                    result.getSummary().getTransformationsImported() +
                    result.getSummary().getFieldMappingsImported()
            );
            
            result.setIdMappings(idMappings);
            
            // Log the import
            logImportActivity(importedFlow, request, result);
            
        } catch (Exception e) {
            log.error("Failed to import flow", e);
            result.getErrors().add(FlowImportResultDTO.ImportMessage.builder()
                    .code("IMPORT_ERROR")
                    .message(e.getMessage())
                    .build());
        }
        
        return result;
    }

    private BusinessComponent importBusinessComponent(BusinessComponentDTO dto, 
                                                    FlowImportRequestDTO.ImportOptions options,
                                                    Map<String, String> idMappings,
                                                    FlowImportResultDTO result) {
        try {
            // Check for existing
            if (businessComponentRepository.existsByName(dto.getName())) {
                if (options.getConflictStrategy() == FlowImportRequestDTO.ConflictStrategy.SKIP) {
                    result.getConflictResolutions().add(createSkippedResolution(
                            "BusinessComponent", dto.getId(), dto.getName()));
                    return null;
                } else if (options.getConflictStrategy() == FlowImportRequestDTO.ConflictStrategy.CREATE_NEW) {
                    dto.setName(generateUniqueName(dto.getName(), options));
                }
            }
            
            BusinessComponent component = new BusinessComponent();
            component.setName(dto.getName());
            component.setDescription(dto.getDescription());
            component.setContactEmail(dto.getContactEmail());
            component.setContactPhone(dto.getContactPhone());
            // Status, department not available in DTO - using defaults
            component.setStatus("ACTIVE");
            component.setDepartment(null);
            
            component = businessComponentRepository.save(component);
            idMappings.put(dto.getId(), component.getId().toString());
            
            return component;
            
        } catch (Exception e) {
            log.error("Failed to import business component", e);
            result.getErrors().add(createImportError("BusinessComponent", dto.getId(), 
                    dto.getName(), e.getMessage()));
            return null;
        }
    }

    private CommunicationAdapter importAdapter(CommunicationAdapterDTO dto,
                                             String businessComponentId,
                                             FlowImportRequestDTO.ImportOptions options,
                                             Map<String, String> idMappings,
                                             FlowImportResultDTO result) {
        try {
            // Check for existing
            if (communicationAdapterRepository.existsByName(dto.getName())) {
                if (options.getConflictStrategy() == FlowImportRequestDTO.ConflictStrategy.SKIP) {
                    result.getConflictResolutions().add(createSkippedResolution(
                            "CommunicationAdapter", dto.getId(), dto.getName()));
                    return null;
                } else if (options.getConflictStrategy() == FlowImportRequestDTO.ConflictStrategy.CREATE_NEW) {
                    dto.setName(generateUniqueName(dto.getName(), options));
                }
            }
            
            CommunicationAdapter adapter = new CommunicationAdapter();
            adapter.setName(dto.getName());
            adapter.setType(com.integrixs.shared.enums.AdapterType.valueOf(dto.getType()));
            adapter.setMode(com.integrixs.adapters.core.AdapterMode.valueOf(dto.getMode()));
            adapter.setConfiguration(objectMapper.writeValueAsString(dto.getConfiguration()));
            adapter.setActive(dto.isActive());
            adapter.setDescription(dto.getDescription());
            if (businessComponentId != null) {
                adapter.setBusinessComponent(businessComponentRepository.findById(UUID.fromString(businessComponentId))
                    .orElseThrow(() -> new RuntimeException("Business component not found: " + businessComponentId)));
            }
            
            adapter = communicationAdapterRepository.save(adapter);
            idMappings.put(dto.getId(), adapter.getId().toString());
            
            return adapter;
            
        } catch (Exception e) {
            log.error("Failed to import adapter", e);
            result.getErrors().add(createImportError("CommunicationAdapter", dto.getId(), 
                    dto.getName(), e.getMessage()));
            return null;
        }
    }

    private IntegrationFlow importIntegrationFlow(FlowDTO dto,
                                                String businessComponentId,
                                                String sourceAdapterId,
                                                String targetAdapterId,
                                                FlowImportRequestDTO.ImportOptions options,
                                                Map<String, String> idMappings,
                                                FlowImportResultDTO result) {
        try {
            // Check for existing
            if (integrationFlowRepository.existsByName(dto.getName())) {
                if (options.getConflictStrategy() == FlowImportRequestDTO.ConflictStrategy.SKIP) {
                    result.getConflictResolutions().add(createSkippedResolution(
                            "IntegrationFlow", dto.getId(), dto.getName()));
                    return null;
                } else if (options.getConflictStrategy() == FlowImportRequestDTO.ConflictStrategy.CREATE_NEW) {
                    dto.setName(generateUniqueName(dto.getName(), options));
                }
            }
            
            IntegrationFlow flow = IntegrationFlow.builder()
                    .name(dto.getName())
                    .description(dto.getDescription())
                    .sourceAdapterId(sourceAdapterId != null ? UUID.fromString(sourceAdapterId) : null)
                    .targetAdapterId(targetAdapterId != null ? UUID.fromString(targetAdapterId) : null)
                    .sourceFlowStructureId(dto.getSourceFlowStructureId() != null ? UUID.fromString(dto.getSourceFlowStructureId()) : null)
                    .targetFlowStructureId(dto.getTargetFlowStructureId() != null ? UUID.fromString(dto.getTargetFlowStructureId()) : null)
                    .status(FlowStatus.DEVELOPED_INACTIVE) // Always import as undeployed
                    // Configuration field removed - using native columns instead
                    .isActive(false) // Start inactive
                    .mappingMode(com.integrixs.data.model.MappingMode.valueOf(dto.getMappingMode()))
                    .createdBy(userRepository.findByUsername(SecurityUtils.getCurrentUsernameStatic()))
                    .build();
            
            if (businessComponentId != null) {
                BusinessComponent bc = businessComponentRepository.findById(UUID.fromString(businessComponentId)).orElse(null);
                flow.setBusinessComponent(bc);
            }
            
            flow = integrationFlowRepository.save(flow);
            idMappings.put(dto.getId(), flow.getId().toString());
            
            return flow;
            
        } catch (Exception e) {
            log.error("Failed to import flow", e);
            result.getErrors().add(createImportError("IntegrationFlow", dto.getId(), 
                    dto.getName(), e.getMessage()));
            return null;
        }
    }

    private void importTransformationsAndMappings(FlowExportDTO export, 
                                                IntegrationFlow importedFlow,
                                                Map<String, String> idMappings,
                                                FlowImportResultDTO result) {
        if (export.getTransformations() == null) {
            return;
        }
        
        Map<String, FlowTransformation> transformationMap = new HashMap<>();
        
        // Import transformations
        for (FlowTransformationDTO transDto : export.getTransformations()) {
            try {
                FlowTransformation transformation = FlowTransformation.builder()
                        .flow(importedFlow)
                        .name(transDto.getName())
                        .description(transDto.getDescription())
                        .type(com.integrixs.data.model.FlowTransformation.TransformationType.valueOf(transDto.getTransformationType().name()))
                        .configuration(transDto.getConfiguration())
                        .executionOrder(transDto.getSequence())
                        .isActive(transDto.isActive())
                        .build();
                
                transformation = flowTransformationRepository.save(transformation);
                transformationMap.put(transDto.getId(), transformation);
                idMappings.put(transDto.getId(), transformation.getId().toString());
                result.getSummary().setTransformationsImported(
                        result.getSummary().getTransformationsImported() + 1);
                
            } catch (Exception e) {
                log.error("Failed to import transformation", e);
                result.getWarnings().add(createImportWarning("FlowTransformation", 
                        transDto.getId(), transDto.getName(), e.getMessage()));
            }
        }
        
        // Import field mappings
        if (export.getFieldMappings() != null) {
            for (FieldMappingDTO mappingDto : export.getFieldMappings()) {
                try {
                    FlowTransformation transformation = transformationMap.get(mappingDto.getTransformationId());
                    if (transformation == null) {
                        continue;
                    }
                    
                    FieldMapping mapping = FieldMapping.builder()
                            .transformation(transformation)
                            .targetField(mappingDto.getTargetField())
                            .javaFunction(mappingDto.getJavaFunction())
                            .mappingRule(mappingDto.getMappingRule())
                            .sourceXPath(mappingDto.getSourceXPath())
                            .targetXPath(mappingDto.getTargetXPath())
                            .isArrayMapping(mappingDto.isArrayMapping())
                            .arrayContextPath(mappingDto.getArrayContextPath())
                            .namespaceAware(mappingDto.isNamespaceAware())
                            .inputTypes(mappingDto.getInputTypes())
                            .outputType(mappingDto.getOutputType())
                            .description(mappingDto.getDescription())
                            .version(mappingDto.getVersion())
                            .functionName(mappingDto.getFunctionName())
                            .isActive(mappingDto.isActive())
                            .build();
                    
                    // Set sourceFields from List<String>
                    mapping.setSourceFieldsList(mappingDto.getSourceFields());
                    
                    mapping = fieldMappingRepository.save(mapping);
                    idMappings.put(mappingDto.getId(), mapping.getId().toString());
                    result.getSummary().setFieldMappingsImported(
                            result.getSummary().getFieldMappingsImported() + 1);
                    
                } catch (Exception e) {
                    log.error("Failed to import field mapping", e);
                    result.getWarnings().add(createImportWarning("FieldMapping", 
                            mappingDto.getId(), mappingDto.getTargetField(), e.getMessage()));
                }
            }
        }
    }

    private String generateUniqueName(String originalName, FlowImportRequestDTO.ImportOptions options) {
        String baseName = originalName;
        
        if (options.getNamePrefix() != null) {
            baseName = options.getNamePrefix() + baseName;
        }
        if (options.getNameSuffix() != null) {
            baseName = baseName + options.getNameSuffix();
        }
        
        // Add timestamp to ensure uniqueness
        return baseName + "_" + System.currentTimeMillis();
    }

    private FlowImportValidationDTO.ImportPreview buildImportPreview(FlowExportDTO export) {
        Map<String, Integer> objectCounts = new HashMap<>();
        objectCounts.put("flows", 1);
        objectCounts.put("businessComponents", export.getBusinessComponent() != null ? 1 : 0);
        objectCounts.put("adapters", 
                (export.getSourceAdapter() != null ? 1 : 0) + 
                (export.getTargetAdapter() != null ? 1 : 0));
        objectCounts.put("transformations", 
                export.getTransformations() != null ? export.getTransformations().size() : 0);
        objectCounts.put("fieldMappings", 
                export.getFieldMappings() != null ? export.getFieldMappings().size() : 0);
        objectCounts.put("certificateReferences", 
                export.getCertificateReferences() != null ? export.getCertificateReferences().size() : 0);
        
        return FlowImportValidationDTO.ImportPreview.builder()
                .flowName(export.getFlow().getName())
                .flowDescription(export.getFlow().getDescription())
                .businessComponentName(export.getBusinessComponent() != null ? 
                        export.getBusinessComponent().getName() : null)
                .sourceAdapterName(export.getSourceAdapter() != null ? 
                        export.getSourceAdapter().getName() : null)
                .targetAdapterName(export.getTargetAdapter() != null ? 
                        export.getTargetAdapter().getName() : null)
                .transformationCount(objectCounts.get("transformations"))
                .fieldMappingCount(objectCounts.get("fieldMappings"))
                .certificateReferenceCount(objectCounts.get("certificateReferences"))
                .objectCounts(objectCounts)
                .build();
    }

    private FlowImportValidationDTO.VersionCompatibility checkVersionCompatibility(FlowExportDTO export) {
        String exportVersion = export.getMetadata().getApplicationVersion();
        boolean isCompatible = true;
        List<String> breakingChanges = new ArrayList<>();
        
        // Simple version check - could be enhanced
        if (!exportVersion.equals(applicationVersion)) {
            // Check major version
            String[] exportParts = exportVersion.split("\\.");
            String[] currentParts = applicationVersion.split("\\.");
            
            if (!exportParts[0].equals(currentParts[0])) {
                isCompatible = false;
                breakingChanges.add("Major version mismatch");
            }
        }
        
        return FlowImportValidationDTO.VersionCompatibility.builder()
                .exportVersion(exportVersion)
                .currentVersion(applicationVersion)
                .isCompatible(isCompatible)
                .requiresMigration(!isCompatible)
                .breakingChanges(breakingChanges)
                .build();
    }

    private void logImportActivity(IntegrationFlow flow, FlowImportRequestDTO request, 
                                 FlowImportResultDTO result) {
        try {
            Map<String, Object> details = new HashMap<>();
            details.put("flowId", flow != null ? flow.getId() : null);
            details.put("flowName", flow != null ? flow.getName() : null);
            details.put("importOptions", request.getOptions());
            details.put("result", result);
            
            SystemLog log = SystemLog.builder()
                    .timestamp(LocalDateTime.now())
                    .level(result.isSuccess() ? SystemLog.LogLevel.INFO : SystemLog.LogLevel.ERROR)
                    .message("Integration flow import " + (result.isSuccess() ? "successful" : "failed"))
                    .details(objectMapper.writeValueAsString(details))
                    .source("IMPORT_SERVICE")
                    .category("FLOW_IMPORT")
                    .userId(SecurityUtils.getCurrentUserId() != null ? UUID.fromString(SecurityUtils.getCurrentUserId()) : null)
                    .username(SecurityUtils.getCurrentUsernameStatic())
                    .domainType("INTEGRATION_FLOW")
                    .domainReferenceId(flow != null ? flow.getId().toString() : null)
                    .build();
            
            systemLogRepository.save(log);
            
        } catch (Exception e) {
            log.error("Failed to log import activity", e);
        }
    }

    // Helper methods for creating DTOs

    private FlowImportValidationDTO.Conflict createNameConflict(String objectType, 
                                                               String importId, 
                                                               String importName) {
        return FlowImportValidationDTO.Conflict.builder()
                .objectType(objectType)
                .importId(importId)
                .importName(importName)
                .type(FlowImportValidationDTO.Conflict.ConflictType.NAME_EXISTS)
                .resolutionOptions(Arrays.asList("SKIP", "CREATE_NEW", "UPDATE_EXISTING"))
                .build();
    }

    private FlowImportValidationDTO.Conflict createMissingReferenceConflict(String objectType,
                                                                          String importId,
                                                                          String importName) {
        return FlowImportValidationDTO.Conflict.builder()
                .objectType(objectType)
                .importId(importId)
                .importName(importName)
                .type(FlowImportValidationDTO.Conflict.ConflictType.REFERENCE_MISSING)
                .resolutionOptions(Arrays.asList("SKIP", "CREATE_PLACEHOLDER"))
                .build();
    }

    private FlowImportValidationDTO.ValidationIssue createValidationError(String code, 
                                                                        String message) {
        return FlowImportValidationDTO.ValidationIssue.builder()
                .code(code)
                .message(message)
                .severity(FlowImportValidationDTO.ValidationIssue.Severity.ERROR)
                .build();
    }

    private FlowImportResultDTO.ImportMessage createImportError(String objectType,
                                                              String objectId,
                                                              String objectName,
                                                              String message) {
        return FlowImportResultDTO.ImportMessage.builder()
                .code("IMPORT_FAILED")
                .message(message)
                .objectType(objectType)
                .objectId(objectId)
                .objectName(objectName)
                .build();
    }

    private FlowImportResultDTO.ImportMessage createImportWarning(String objectType,
                                                                String objectId,
                                                                String objectName,
                                                                String message) {
        return FlowImportResultDTO.ImportMessage.builder()
                .code("IMPORT_WARNING")
                .message(message)
                .objectType(objectType)
                .objectId(objectId)
                .objectName(objectName)
                .build();
    }

    private FlowImportResultDTO.ConflictResolution createSkippedResolution(String objectType,
                                                                         String originalId,
                                                                         String originalName) {
        return FlowImportResultDTO.ConflictResolution.builder()
                .objectType(objectType)
                .originalId(originalId)
                .originalName(originalName)
                .resolution("SKIPPED")
                .reason("Object with same name already exists")
                .build();
    }
}