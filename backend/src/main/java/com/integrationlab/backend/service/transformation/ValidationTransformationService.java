package com.integrationlab.backend.service.transformation;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.integrationlab.shared.dto.transformation.ValidationTransformationConfigDTO;
import com.integrationlab.backend.util.JavaFunctionRunner;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
public class ValidationTransformationService {

    private final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * Applies validation rules on the input JSON string.
     * Returns input JSON unchanged if all validations pass.
     * Throws RuntimeException with combined error messages if validation fails.
     *
     * @param inputJson input JSON string
     * @param config validation configuration DTO
     * @return input JSON string if validation succeeds
     */
    public String applyValidation(String inputJson, ValidationTransformationConfigDTO config) {
        try {
            JsonNode inputNode = objectMapper.readTree(inputJson);

            // Convert to map for JavaFunctionRunner
            Map<String, Object> record = objectMapper.convertValue(inputNode, Map.class);

            List<String> errors = new ArrayList<>();

            List<String> rules = config.getValidationRules();
            List<String> messages = config.getErrorMessages();

            if (rules == null || rules.isEmpty()) {
                return inputJson; // No rules to validate
            }

            for (int i = 0; i < rules.size(); i++) {
                String rule = rules.get(i);
                String errorMessage = (messages != null && i < messages.size()) ? messages.get(i) : "Validation failed";

                // Run the JS validation rule; it should return boolean
                Object result = JavaFunctionRunner.run(rule, List.of("record"), Map.of("record", record));
                boolean valid;
                if (result instanceof Boolean) {
                    valid = (Boolean) result;
                } else if (result instanceof String) {
                    valid = Boolean.parseBoolean((String) result);
                } else {
                    valid = false; // Unexpected type means invalid
                }

                if (!valid) {
                    errors.add(errorMessage);
                    if (config.isFailFast()) {
                        break;
                    }
                }
            }

            if (!errors.isEmpty()) {
                throw new RuntimeException("Validation failed: " + String.join("; ", errors));
            }

            return inputJson;

        } catch (Exception e) {
            throw new RuntimeException("Failed to apply validation transformation", e);
        }
    }
}
