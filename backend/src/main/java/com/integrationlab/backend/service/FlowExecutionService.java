package com.integrationlab.backend.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.integrationlab.engine.AdapterExecutor;
import com.integrationlab.data.model.FieldMapping;
import com.integrationlab.data.model.FlowTransformation;
import com.integrationlab.data.model.IntegrationFlow;
import com.integrationlab.data.model.MappingMode;
import com.integrationlab.data.model.TransformationCustomFunction;
import com.integrationlab.data.repository.FieldMappingRepository;
import com.integrationlab.data.repository.FlowTransformationRepository;
import com.integrationlab.data.repository.IntegrationFlowRepository;
import com.integrationlab.backend.service.transformation.FilterTransformationService;
import com.integrationlab.backend.service.transformation.EnrichmentTransformationService;
import com.integrationlab.backend.service.transformation.ValidationTransformationService;
import com.integrationlab.shared.dto.transformation.CustomFunctionConfigDTO;
import com.integrationlab.shared.dto.transformation.EnrichmentTransformationConfigDTO;
import com.integrationlab.shared.dto.transformation.FilterTransformationConfigDTO;
import com.integrationlab.shared.dto.transformation.ValidationTransformationConfigDTO;
import com.integrationlab.backend.util.FieldMapper;
import com.integrationlab.backend.util.JavaFunctionRunner;
import com.integrationlab.data.model.CommunicationAdapter;
import com.integrationlab.data.repository.CommunicationAdapterRepository;
import com.integrationlab.engine.service.FormatConversionService;
import com.integrationlab.engine.xml.XmlConversionException;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class FlowExecutionService {
    
    private static final Logger logger = LoggerFactory.getLogger(FlowExecutionService.class);

    private final IntegrationFlowRepository flowRepository;
    private final FlowTransformationRepository transformationRepository;
    private final FieldMappingRepository fieldMappingRepository;
    private final CommunicationAdapterRepository adapterRepository;
    private final AdapterExecutor adapterExecutor;
    private final LogService logService;
    private final FilterTransformationService filterTransformationService;
    private final EnrichmentTransformationService enrichmentTransformationService;
    private final ValidationTransformationService validationTransformationService;
    private final DevelopmentFunctionService developmentFunctionService;
    private final FormatConversionService formatConversionService;
    private final DirectFileTransferService directFileTransferService;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public FlowExecutionService(
            IntegrationFlowRepository flowRepository,
            FlowTransformationRepository transformationRepository,
            FieldMappingRepository fieldMappingRepository,
            CommunicationAdapterRepository adapterRepository,
            AdapterExecutor adapterExecutor,
            LogService logService,
            FilterTransformationService filterTransformationService,
            EnrichmentTransformationService enrichmentTransformationService,
            ValidationTransformationService validationTransformationService,
            DevelopmentFunctionService developmentFunctionService,
            FormatConversionService formatConversionService,
            DirectFileTransferService directFileTransferService
    ) {
        this.flowRepository = flowRepository;
        this.transformationRepository = transformationRepository;
        this.fieldMappingRepository = fieldMappingRepository;
        this.adapterRepository = adapterRepository;
        this.adapterExecutor = adapterExecutor;
        this.logService = logService;
        this.filterTransformationService = filterTransformationService;
        this.enrichmentTransformationService = enrichmentTransformationService;
        this.validationTransformationService = validationTransformationService;
        this.developmentFunctionService = developmentFunctionService;
        this.formatConversionService = formatConversionService;
        this.directFileTransferService = directFileTransferService;
    }

    public void executeFlow(String flowId) {
        IntegrationFlow flow = flowRepository.findById(flowId)
                .orElseThrow(() -> new RuntimeException("Flow not found"));

        try {
            // Get adapters
            CommunicationAdapter sourceAdapter = adapterRepository.findById(flow.getSourceAdapterId())
                    .orElseThrow(() -> new RuntimeException("Source adapter not found"));
            CommunicationAdapter targetAdapter = adapterRepository.findById(flow.getTargetAdapterId())
                    .orElseThrow(() -> new RuntimeException("Target adapter not found"));

            // Validate adapters are active
            if (!sourceAdapter.isActive()) {
                throw new RuntimeException("Cannot execute flow: Source adapter '" + sourceAdapter.getName() + 
                    "' is in a stopped status. Please activate the adapter before using it in a flow.");
            }
            if (!targetAdapter.isActive()) {
                throw new RuntimeException("Cannot execute flow: Target adapter '" + targetAdapter.getName() + 
                    "' is in a stopped status. Please activate the adapter before using it in a flow.");
            }

            // Check if we should skip XML conversion (direct passthrough)
            if (flow.isSkipXmlConversion()) {
                logger.info("Executing direct transfer (skip XML conversion) for flow: {}", flow.getName());
                try {
                    directFileTransferService.executeDirectTransfer(flow, sourceAdapter, targetAdapter);
                    return;
                } catch (Exception e) {
                    throw new RuntimeException("Direct transfer failed: " + e.getMessage(), e);
                }
            }

            // Step 1: Fetch source data
            Object rawData = adapterExecutor.fetchDataAsObject(flow.getSourceAdapterId());
            logger.info("Fetched data from source adapter: {}", sourceAdapter.getName());

            // Check if the data is binary and should skip XML conversion
            if (directFileTransferService.isBinaryFile(rawData)) {
                logger.info("Binary file detected, using direct transfer for flow: {}", flow.getName());
                try {
                    directFileTransferService.executeDirectTransfer(flow, sourceAdapter, targetAdapter);
                    return;
                } catch (Exception e) {
                    throw new RuntimeException("Direct transfer failed for binary file: " + e.getMessage(), e);
                }
            }

            String processedData;
            
            // Check if mapping is required
            boolean mappingRequired = flow.getMappingMode() == MappingMode.WITH_MAPPING;
            
            if (mappingRequired) {
                logger.info("Mapping required for flow: {}", flow.getName());
                
                // Step 2a: Convert source data to XML
                String xmlData = formatConversionService.convertToXml(rawData, sourceAdapter);
                logger.debug("Converted source data to XML");
                
                // Step 2b: Apply transformations (including field mappings)
                String transformedXml = applyTransformations(flow, xmlData);
                logger.debug("Applied transformations to XML data");
                
                // Step 2c: Convert XML back to target format
                Object targetData = formatConversionService.convertFromXml(
                    transformedXml, 
                    targetAdapter, 
                    getConversionConfig(flow, targetAdapter)
                );
                processedData = targetData.toString();
                logger.info("Converted XML to target format: {}", targetAdapter.getType());
                
            } else {
                logger.info("Pass-through mode for flow: {}", flow.getName());
                // Pass-through mode - no conversion needed
                processedData = rawData.toString();
            }

            // Step 3: Send to target adapter
            adapterExecutor.sendData(flow.getTargetAdapterId(), processedData);
            logger.info("Sent data to target adapter: {}", targetAdapter.getName());

            // Step 4: Log success
            String rawDataStr = rawData instanceof byte[] ? 
                new String((byte[]) rawData) : rawData.toString();
            logService.logFlowExecutionSuccess(flow, rawDataStr, processedData);

        } catch (XmlConversionException e) {
            logger.error("XML conversion error executing flow: {}", flow.getName(), e);
            logService.logFlowExecutionError(flow, e);
            throw new RuntimeException("XML conversion failed: " + e.getMessage(), e);
        } catch (Exception e) {
            logger.error("Error executing flow: {}", flow.getName(), e);
            logService.logFlowExecutionError(flow, e);
            throw e;
        }
    }

    private Map<String, Object> getConversionConfig(IntegrationFlow flow, CommunicationAdapter targetAdapter) {
        Map<String, Object> config = new HashMap<>();
        
        // Get adapter configuration
        Map<String, Object> adapterConfig = parseAdapterConfiguration(targetAdapter.getConfiguration());
        if (adapterConfig == null) {
            adapterConfig = new HashMap<>();
        }
        
        String adapterType = targetAdapter.getType().name();
        
        // Configure based on adapter type
        switch (adapterType) {
            case "FILE":
            case "FTP":
            case "SFTP":
                String fileFormat = (String) adapterConfig.getOrDefault("fileFormat", "CSV");
                
                if ("CSV".equalsIgnoreCase(fileFormat)) {
                    // CSV configuration with file separators
                    config.put("delimiter", adapterConfig.getOrDefault("delimiter", ","));
                    config.put("includeHeaders", adapterConfig.getOrDefault("includeHeaders", true));
                    config.put("quoteAllFields", adapterConfig.getOrDefault("quoteAllFields", false));
                    config.put("lineTerminator", adapterConfig.getOrDefault("lineTerminator", "\n"));
                    config.put("quoteCharacter", adapterConfig.getOrDefault("quoteCharacter", "\""));
                } else if ("FIXED".equalsIgnoreCase(fileFormat)) {
                    // Fixed-length configuration
                    config.put("fixedLength", true);
                    config.put("fieldLengths", adapterConfig.get("fieldLengths")); // Map of field->length
                    config.put("padCharacter", adapterConfig.getOrDefault("padCharacter", " "));
                    config.put("lineTerminator", adapterConfig.getOrDefault("lineTerminator", "\n"));
                }
                break;
                
            case "JDBC":
                // SQL configuration
                config.put("tableName", adapterConfig.getOrDefault("tableName", "data"));
                config.put("operation", adapterConfig.getOrDefault("operation", "INSERT"));
                config.put("whereClause", adapterConfig.get("whereClause"));
                config.put("generateBatch", adapterConfig.getOrDefault("generateBatch", false));
                break;
                
            default:
                // Default configuration for other adapter types
                break;
        }
        
        // Add field mappings if available from transformations
        List<FlowTransformation> transformations = transformationRepository.findByFlowId(flow.getId());
        Map<String, String> fieldMappings = new HashMap<>();
        
        for (FlowTransformation transformation : transformations) {
            if (transformation.getType() == FlowTransformation.TransformationType.FIELD_MAPPING) {
                List<FieldMapping> mappings = fieldMappingRepository.findByTransformationId(transformation.getId());
                for (FieldMapping mapping : mappings) {
                    // Parse source fields JSON to get first field
                    try {
                        List<String> sourceFields = objectMapper.readValue(
                            mapping.getSourceFields(), 
                            new TypeReference<List<String>>() {}
                        );
                        if (!sourceFields.isEmpty()) {
                            fieldMappings.put(sourceFields.get(0), mapping.getTargetField());
                        }
                    } catch (Exception e) {
                        logger.warn("Failed to parse source fields for mapping: {}", mapping.getId());
                    }
                }
            }
        }
        
        if (!fieldMappings.isEmpty()) {
            config.put("fieldMappings", fieldMappings);
        }
        
        return config;
    }
    
    private Map<String, Object> parseAdapterConfiguration(String configJson) {
        if (configJson == null || configJson.isEmpty()) {
            return new HashMap<>();
        }
        try {
            return objectMapper.readValue(configJson, new TypeReference<Map<String, Object>>() {});
        } catch (Exception e) {
            logger.warn("Failed to parse adapter configuration: {}", e.getMessage());
            return new HashMap<>();
        }
    }

    private String applyTransformations(IntegrationFlow flow, String inputJson) {
        List<FlowTransformation> transformations = transformationRepository.findByFlowId(flow.getId());

        String currentData = inputJson;
        for (FlowTransformation t : transformations) {
            if (!t.isActive()) continue;

            switch (t.getType()) {
                case FIELD_MAPPING -> {
                    List<FieldMapping> mappings = fieldMappingRepository.findByTransformationId(t.getId());
                    currentData = FieldMapper.apply(currentData, mappings, developmentFunctionService);
                }
                case CUSTOM_FUNCTION -> {
                    currentData = applyCustomFunctionTransformation(t, currentData);
                }
                case FILTER -> {
                    currentData = applyFilterTransformation(t, currentData);
                }
                case ENRICHMENT -> {
                    currentData = applyEnrichmentTransformation(t, currentData);
                }
                case VALIDATION -> {
                    currentData = applyValidationTransformation(t, currentData);
                }
                default -> throw new UnsupportedOperationException("Transformation type not supported: " + t.getType());
            }
        }
        return currentData;
    }

    private String applyCustomFunctionTransformation(FlowTransformation t, String currentData) {
        try {
            if (t.getConfiguration() == null || t.getConfiguration().isBlank()) {
                throw new RuntimeException("Custom function configuration is missing");
            }
            CustomFunctionConfigDTO config = objectMapper.readValue(t.getConfiguration(), CustomFunctionConfigDTO.class);
            Map<String, Object> inputMap = objectMapper.readValue(currentData, new TypeReference<>() {});

            String functionBody = config.getJavaFunction();
            if (functionBody == null || functionBody.isBlank()) {
                throw new RuntimeException("Custom function name/body is missing");
            }

            // Try to find the function in transformation_custom_functions table
            try {
                TransformationCustomFunction customFunction = developmentFunctionService.getBuiltInFunctionByName(functionBody);
                functionBody = customFunction.getFunctionBody();
            } catch (Exception e) {
                // Function not found in database, assume functionBody contains the actual code
                logger.debug("Function '{}' not found in transformation_custom_functions, using as direct code", functionBody);
            }

            Object result = JavaFunctionRunner.run(functionBody, config.getSourceFields(), inputMap);
            return result != null ? result.toString() : null;
        } catch (Exception e) {
            throw new RuntimeException("Failed to execute custom function", e);
        }
    }

    private String applyFilterTransformation(FlowTransformation t, String currentData) {
        try {
            if (t.getConfiguration() == null || t.getConfiguration().isBlank()) {
                throw new RuntimeException("Filter transformation configuration is missing");
            }
            FilterTransformationConfigDTO filterConfig = objectMapper.readValue(t.getConfiguration(), FilterTransformationConfigDTO.class);
            return filterTransformationService.applyFilter(currentData, filterConfig);
        } catch (Exception e) {
            throw new RuntimeException("Failed to apply filter transformation", e);
        }
    }

    private String applyEnrichmentTransformation(FlowTransformation t, String currentData) {
        try {
            if (t.getConfiguration() == null || t.getConfiguration().isBlank()) {
                throw new RuntimeException("Enrichment transformation configuration is missing");
            }
            EnrichmentTransformationConfigDTO enrichConfig = objectMapper.readValue(t.getConfiguration(), EnrichmentTransformationConfigDTO.class);
            return enrichmentTransformationService.applyEnrichment(currentData, enrichConfig);
        } catch (Exception e) {
            throw new RuntimeException("Failed to apply enrichment transformation", e);
        }
    }

    private String applyValidationTransformation(FlowTransformation t, String currentData) {
        try {
            if (t.getConfiguration() == null || t.getConfiguration().isBlank()) {
                throw new RuntimeException("Validation transformation configuration is missing");
            }
            ValidationTransformationConfigDTO validationConfig = objectMapper.readValue(t.getConfiguration(), ValidationTransformationConfigDTO.class);
            return validationTransformationService.applyValidation(currentData, validationConfig);
        } catch (Exception e) {
            throw new RuntimeException("Failed to apply validation transformation", e);
        }
    }
}
