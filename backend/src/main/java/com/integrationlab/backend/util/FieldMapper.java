package com.integrationlab.backend.util;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.integrationlab.model.FieldMapping;
import com.integrationlab.model.ReusableFunction;
import com.integrationlab.backend.service.ReusableJavaFunctionService;

import java.util.*;
import java.util.stream.Collectors;

public class FieldMapper {

    private static final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * Applies field mappings to input JSON, supports direct Java function bodies or reusable functions referenced by functionName.
     *
     * @param inputJson JSON string input data
     * @param mappings List of FieldMapping objects
     * @param reusableFunctionService Service to resolve reusable functions by name or id
     * @return JSON string of mapped output fields
     */
    public static String apply(String inputJson, List<FieldMapping> mappings, ReusableJavaFunctionService reusableFunctionService) {
        try {
            // Parse input JSON to a Map
            Map<String, Object> inputMap = objectMapper.readValue(inputJson, new TypeReference<>() {});
            Map<String, Object> outputMap = new LinkedHashMap<>();

            for (FieldMapping mapping : mappings) {
                if (!mapping.isActive()) continue;

                // Parse sourceFields JSON array
                List<String> sourceFields = objectMapper.readValue(
                        mapping.getSourceFields(), new TypeReference<List<String>>() {}
                );

                // Extract values for source fields
                Map<String, Object> sourceData = new HashMap<>();
                for (String field : sourceFields) {
                    sourceData.put(field, inputMap.get(field));
                }

                // Determine Java function to execute
                String javaFunctionBody = mapping.getJavaFunction();

                // If no inline function, try to resolve reusable function by functionName
                if ((javaFunctionBody == null || javaFunctionBody.isBlank()) && reusableFunctionService != null) {
                    String functionName = mapping.getFunctionName();
                    if (functionName != null && !functionName.isBlank()) {
                        Optional<ReusableFunction> reusableFuncOpt = reusableFunctionService.findByName(functionName);
                        if (reusableFuncOpt.isPresent()) {
                            javaFunctionBody = reusableFuncOpt.get().getFunctionBody();
                        }
                    }
                }

                Object value;
                if (javaFunctionBody != null && !javaFunctionBody.isBlank()) {
                    value = JavaFunctionRunner.run(javaFunctionBody, sourceFields, sourceData);
                } else if (mapping.getMappingRule() != null && !mapping.getMappingRule().isBlank()) {
                    // Simple mapping rule fallback: concatenate all non-null values with a space
                    value = sourceFields.stream()
                            .map(sourceData::get)
                            .filter(Objects::nonNull)
                            .map(Object::toString)
                            .collect(Collectors.joining(" "));
                } else {
                    // No rule, just take first available field or null if none
                    value = sourceFields.isEmpty() ? null : sourceData.get(sourceFields.get(0));
                }

                outputMap.put(mapping.getTargetField(), value);
            }

            return objectMapper.writeValueAsString(outputMap);

        } catch (Exception e) {
            throw new RuntimeException("Failed to apply field mappings", e);
        }
    }
}
