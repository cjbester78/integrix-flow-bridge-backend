package com.integrationlab.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.integrationlab.engine.AdapterExecutor;
import com.integrationlab.model.FieldMapping;
import com.integrationlab.model.FlowTransformation;
import com.integrationlab.model.IntegrationFlow;
import com.integrationlab.model.ReusableFunction;
import com.integrationlab.repository.FieldMappingRepository;
import com.integrationlab.repository.FlowTransformationRepository;
import com.integrationlab.repository.IntegrationFlowRepository;
import com.integrationlab.service.transformation.FilterTransformationService;
import com.integrationlab.service.transformation.EnrichmentTransformationService;
import com.integrationlab.service.transformation.ValidationTransformationService;
import com.integrationlab.shared.dto.transformation.CustomFunctionConfigDTO;
import com.integrationlab.shared.dto.transformation.EnrichmentTransformationConfigDTO;
import com.integrationlab.shared.dto.transformation.FilterTransformationConfigDTO;
import com.integrationlab.shared.dto.transformation.ValidationTransformationConfigDTO;
import com.integrationlab.util.FieldMapper;
import com.integrationlab.util.JavaFunctionRunner;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class FlowExecutionService {

    private final IntegrationFlowRepository flowRepository;
    private final FlowTransformationRepository transformationRepository;
    private final FieldMappingRepository fieldMappingRepository;
    private final AdapterExecutor adapterExecutor;
    private final LogService logService;
    private final FilterTransformationService filterTransformationService;
    private final EnrichmentTransformationService enrichmentTransformationService;
    private final ValidationTransformationService validationTransformationService;
    private final ReusableJavaFunctionService reusableFunctionService;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public FlowExecutionService(
            IntegrationFlowRepository flowRepository,
            FlowTransformationRepository transformationRepository,
            FieldMappingRepository fieldMappingRepository,
            AdapterExecutor adapterExecutor,
            LogService logService,
            FilterTransformationService filterTransformationService,
            EnrichmentTransformationService enrichmentTransformationService,
            ValidationTransformationService validationTransformationService,
            ReusableJavaFunctionService reusableFunctionService
    ) {
        this.flowRepository = flowRepository;
        this.transformationRepository = transformationRepository;
        this.fieldMappingRepository = fieldMappingRepository;
        this.adapterExecutor = adapterExecutor;
        this.logService = logService;
        this.filterTransformationService = filterTransformationService;
        this.enrichmentTransformationService = enrichmentTransformationService;
        this.validationTransformationService = validationTransformationService;
        this.reusableFunctionService = reusableFunctionService;
    }

    public void executeFlow(String flowId) {
        IntegrationFlow flow = flowRepository.findById(flowId)
                .orElseThrow(() -> new RuntimeException("Flow not found"));

        try {
            // Step 1: Fetch source data
            String rawData = adapterExecutor.fetchData(flow.getSourceAdapterId());

            // Step 2: Apply transformations
            String transformedData = applyTransformations(flow, rawData);

            // Step 3: Send to target adapter
            adapterExecutor.sendData(flow.getTargetAdapterId(), transformedData);

            // Step 4: Log success
            logService.logFlowExecutionSuccess(flow, rawData, transformedData);

        } catch (Exception e) {
            logService.logFlowExecutionError(flow, e);
            throw e;
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
                    currentData = FieldMapper.apply(currentData, mappings, reusableFunctionService);
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

            Optional<ReusableFunction> reusableFuncOpt = reusableFunctionService.findByName(functionBody);
            if (reusableFuncOpt.isEmpty()) {
                reusableFuncOpt = reusableFunctionService.findById(functionBody);
            }
            if (reusableFuncOpt.isPresent()) {
                functionBody = reusableFuncOpt.get().getFunctionBody();
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
