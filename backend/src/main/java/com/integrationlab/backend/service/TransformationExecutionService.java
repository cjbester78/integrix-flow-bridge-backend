package com.integrationlab.backend.service;

import com.integrationlab.data.model.FieldMapping;
import com.integrationlab.data.repository.FieldMappingRepository;
import com.integrationlab.backend.service.transformation.EnrichmentTransformationService;
import com.integrationlab.backend.service.transformation.FilterTransformationService;
import com.integrationlab.backend.service.transformation.ValidationTransformationService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import java.util.*;
import java.util.regex.Pattern;

@Service
public class TransformationExecutionService {

    @Autowired
    private FieldMappingRepository fieldMappingRepository;
    
    @Autowired
    private EnrichmentTransformationService enrichmentService;
    
    @Autowired
    private FilterTransformationService filterService;
    
    @Autowired
    private ValidationTransformationService validationService;
    
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final ScriptEngine scriptEngine;

    public TransformationExecutionService() {
        ScriptEngineManager manager = new ScriptEngineManager();
        this.scriptEngine = manager.getEngineByName("javascript");
    }

    /**
     * Execute field transformation for a specific transformation ID
     */
    public TransformationResult executeTransformation(String transformationId, Object inputData) {
        try {
            List<FieldMapping> mappings = fieldMappingRepository.findByTransformationId(transformationId);
            
            if (mappings.isEmpty()) {
                return TransformationResult.success(inputData, "No field mappings found");
            }

            JsonNode inputJson = objectMapper.valueToTree(inputData);
            ObjectNode outputJson = objectMapper.createObjectNode();
            List<String> executionLogs = new ArrayList<>();

            for (FieldMapping mapping : mappings) {
                try {
                    Object transformedValue = executeFieldMapping(mapping, inputJson, executionLogs);
                    setNestedValue(outputJson, mapping.getTargetField(), transformedValue);
                    executionLogs.add("Successfully mapped: " + mapping.getSourceFields() + " -> " + mapping.getTargetField());
                } catch (Exception e) {
                    executionLogs.add("Failed to map " + mapping.getTargetField() + ": " + e.getMessage());
                    return TransformationResult.error("Field mapping failed: " + e.getMessage(), executionLogs);
                }
            }

            return TransformationResult.success(outputJson, executionLogs);
        } catch (Exception e) {
            return TransformationResult.error("Transformation execution failed: " + e.getMessage());
        }
    }

    /**
     * Execute a single field mapping
     */
    private Object executeFieldMapping(FieldMapping mapping, JsonNode inputData, List<String> logs) throws Exception {
        // Parse source fields (could be comma-separated)
        String[] sourceFields = mapping.getSourceFields().split(",");
        Map<String, Object> sourceValues = new HashMap<>();
        
        for (String field : sourceFields) {
            field = field.trim();
            Object value = getNestedValue(inputData, field);
            sourceValues.put(field, value);
            logs.add("Source field " + field + " = " + value);
        }

        // Execute transformation based on type
        if (mapping.getJavaFunction() != null && !mapping.getJavaFunction().isEmpty()) {
            return executeJavaScriptFunction(mapping.getJavaFunction(), sourceValues, logs);
        } else if (mapping.getMappingRule() != null && !mapping.getMappingRule().isEmpty()) {
            return executeMappingRule(mapping.getMappingRule(), sourceValues, logs);
        } else if (sourceFields.length == 1) {
            // Direct mapping - just copy the value
            return sourceValues.get(sourceFields[0].trim());
        } else {
            throw new IllegalArgumentException("No transformation method specified for mapping: " + mapping.getTargetField());
        }
    }

    /**
     * Execute JavaScript function with source values
     */
    private Object executeJavaScriptFunction(String javaFunction, Map<String, Object> sourceValues, List<String> logs) throws ScriptException {
        // Set source values as variables in the script engine
        for (Map.Entry<String, Object> entry : sourceValues.entrySet()) {
            scriptEngine.put(entry.getKey().replaceAll("[^a-zA-Z0-9_]", "_"), entry.getValue());
        }
        
        // Add utility functions
        scriptEngine.put("StringUtils", new StringTransformationUtils());
        scriptEngine.put("DateUtils", new DateTransformationUtils());
        scriptEngine.put("NumberUtils", new NumberTransformationUtils());
        
        logs.add("Executing JavaScript: " + javaFunction);
        Object result = scriptEngine.eval(javaFunction);
        logs.add("JavaScript result: " + result);
        
        return result;
    }

    /**
     * Execute mapping rule (e.g., lookup tables, conditions)
     */
    private Object executeMappingRule(String mappingRule, Map<String, Object> sourceValues, List<String> logs) throws Exception {
        logs.add("Executing mapping rule: " + mappingRule);
        
        // Parse mapping rule as JSON
        JsonNode ruleNode = objectMapper.readTree(mappingRule);
        String ruleType = ruleNode.get("type").asText();
        
        switch (ruleType) {
            case "lookup":
                return executeLookupRule(ruleNode, sourceValues, logs);
            case "condition":
                return executeConditionRule(ruleNode, sourceValues, logs);
            case "concatenation":
                return executeConcatenationRule(ruleNode, sourceValues, logs);
            case "format":
                return executeFormatRule(ruleNode, sourceValues, logs);
            default:
                throw new IllegalArgumentException("Unknown mapping rule type: " + ruleType);
        }
    }

    private Object executeLookupRule(JsonNode rule, Map<String, Object> sourceValues, List<String> logs) {
        String sourceField = rule.get("sourceField").asText();
        Object sourceValue = sourceValues.get(sourceField);
        JsonNode lookupTable = rule.get("lookupTable");
        String defaultValue = rule.has("defaultValue") ? rule.get("defaultValue").asText() : null;
        
        if (lookupTable.has(sourceValue.toString())) {
            Object result = lookupTable.get(sourceValue.toString()).asText();
            logs.add("Lookup result for " + sourceValue + ": " + result);
            return result;
        } else {
            logs.add("No lookup match for " + sourceValue + ", using default: " + defaultValue);
            return defaultValue;
        }
    }

    private Object executeConditionRule(JsonNode rule, Map<String, Object> sourceValues, List<String> logs) {
        String condition = rule.get("condition").asText();
        String trueValue = rule.get("trueValue").asText();
        String falseValue = rule.get("falseValue").asText();
        
        try {
            // Simple condition evaluation (can be extended)
            boolean conditionResult = evaluateCondition(condition, sourceValues);
            Object result = conditionResult ? trueValue : falseValue;
            logs.add("Condition '" + condition + "' evaluated to " + conditionResult + ", result: " + result);
            return result;
        } catch (Exception e) {
            logs.add("Failed to evaluate condition: " + e.getMessage());
            return falseValue;
        }
    }

    private Object executeConcatenationRule(JsonNode rule, Map<String, Object> sourceValues, List<String> logs) {
        JsonNode fields = rule.get("fields");
        String separator = rule.has("separator") ? rule.get("separator").asText() : "";
        
        StringBuilder result = new StringBuilder();
        for (JsonNode field : fields) {
            String fieldName = field.asText();
            Object value = sourceValues.get(fieldName);
            if (value != null) {
                if (result.length() > 0 && !separator.isEmpty()) {
                    result.append(separator);
                }
                result.append(value.toString());
            }
        }
        
        logs.add("Concatenation result: " + result.toString());
        return result.toString();
    }

    private Object executeFormatRule(JsonNode rule, Map<String, Object> sourceValues, List<String> logs) {
        String pattern = rule.get("pattern").asText();
        String sourceField = rule.get("sourceField").asText();
        Object sourceValue = sourceValues.get(sourceField);
        
        if (sourceValue != null) {
            String result = String.format(pattern, sourceValue);
            logs.add("Format result: " + result);
            return result;
        }
        
        return null;
    }

    /**
     * Validate transformation configuration
     */
    public ValidationResult validateTransformation(String transformationId) {
        ValidationResult result = new ValidationResult();
        
        try {
            List<FieldMapping> mappings = fieldMappingRepository.findByTransformationId(transformationId);
            
            if (mappings.isEmpty()) {
                result.addWarning("No field mappings defined for transformation");
                return result;
            }

            for (FieldMapping mapping : mappings) {
                validateFieldMapping(mapping, result);
            }
            
        } catch (Exception e) {
            result.addError("Validation failed: " + e.getMessage());
        }
        
        return result;
    }

    private void validateFieldMapping(FieldMapping mapping, ValidationResult result) {
        if (mapping.getSourceFields() == null || mapping.getSourceFields().trim().isEmpty()) {
            result.addError("Source fields cannot be empty for target: " + mapping.getTargetField());
        }
        
        if (mapping.getTargetField() == null || mapping.getTargetField().trim().isEmpty()) {
            result.addError("Target field cannot be empty");
        }
        
        // Validate JavaScript function if present
        if (mapping.getJavaFunction() != null && !mapping.getJavaFunction().isEmpty()) {
            try {
                scriptEngine.eval("(function() { " + mapping.getJavaFunction() + " })");
            } catch (ScriptException e) {
                result.addError("Invalid JavaScript function for " + mapping.getTargetField() + ": " + e.getMessage());
            }
        }
        
        // Validate mapping rule if present
        if (mapping.getMappingRule() != null && !mapping.getMappingRule().isEmpty()) {
            try {
                objectMapper.readTree(mapping.getMappingRule());
            } catch (Exception e) {
                result.addError("Invalid mapping rule JSON for " + mapping.getTargetField() + ": " + e.getMessage());
            }
        }
    }

    // Utility methods
    private Object getNestedValue(JsonNode node, String path) {
        String[] parts = path.split("\\.");
        JsonNode current = node;
        
        for (String part : parts) {
            if (current == null || !current.has(part)) {
                return null;
            }
            current = current.get(part);
        }
        
        return current != null ? current.asText() : null;
    }
    
    private void setNestedValue(ObjectNode node, String path, Object value) {
        String[] parts = path.split("\\.");
        ObjectNode current = node;
        
        for (int i = 0; i < parts.length - 1; i++) {
            if (!current.has(parts[i])) {
                current.set(parts[i], objectMapper.createObjectNode());
            }
            current = (ObjectNode) current.get(parts[i]);
        }
        
        if (value != null) {
            current.set(parts[parts.length - 1], objectMapper.valueToTree(value));
        }
    }

    private boolean evaluateCondition(String condition, Map<String, Object> sourceValues) {
        // Simple condition evaluator - can be extended
        for (Map.Entry<String, Object> entry : sourceValues.entrySet()) {
            condition = condition.replace("${" + entry.getKey() + "}", entry.getValue().toString());
        }
        
        // Basic comparisons
        if (condition.contains("==")) {
            String[] parts = condition.split("==");
            return parts[0].trim().equals(parts[1].trim());
        } else if (condition.contains("!=")) {
            String[] parts = condition.split("!=");
            return !parts[0].trim().equals(parts[1].trim());
        }
        
        return Boolean.parseBoolean(condition);
    }

    // Result classes
    public static class TransformationResult {
        private boolean success;
        private Object data;
        private String message;
        private List<String> logs;

        public static TransformationResult success(Object data, List<String> logs) {
            TransformationResult result = new TransformationResult();
            result.success = true;
            result.data = data;
            result.logs = logs != null ? logs : new ArrayList<>();
            return result;
        }

        public static TransformationResult success(Object data, String message) {
            TransformationResult result = new TransformationResult();
            result.success = true;
            result.data = data;
            result.message = message;
            result.logs = new ArrayList<>();
            return result;
        }

        public static TransformationResult error(String message) {
            TransformationResult result = new TransformationResult();
            result.success = false;
            result.message = message;
            result.logs = new ArrayList<>();
            return result;
        }

        public static TransformationResult error(String message, List<String> logs) {
            TransformationResult result = new TransformationResult();
            result.success = false;
            result.message = message;
            result.logs = logs != null ? logs : new ArrayList<>();
            return result;
        }

        // Getters and setters
        public boolean isSuccess() { return success; }
        public void setSuccess(boolean success) { this.success = success; }
        public Object getData() { return data; }
        public void setData(Object data) { this.data = data; }
        public String getMessage() { return message; }
        public void setMessage(String message) { this.message = message; }
        public List<String> getLogs() { return logs; }
        public void setLogs(List<String> logs) { this.logs = logs; }
    }

    public static class ValidationResult {
        private boolean valid = true;
        private List<String> errors = new ArrayList<>();
        private List<String> warnings = new ArrayList<>();

        public boolean isValid() { return valid; }
        public void setValid(boolean valid) { this.valid = valid; }
        public List<String> getErrors() { return errors; }
        public void setErrors(List<String> errors) { this.errors = errors; }
        public List<String> getWarnings() { return warnings; }
        public void setWarnings(List<String> warnings) { this.warnings = warnings; }
        
        public void addError(String error) { 
            this.errors.add(error); 
            this.valid = false;
        }
        
        public void addWarning(String warning) { 
            this.warnings.add(warning); 
        }
    }

    // Utility classes for transformations
    public static class StringTransformationUtils {
        public String toUpperCase(String str) { return str != null ? str.toUpperCase() : null; }
        public String toLowerCase(String str) { return str != null ? str.toLowerCase() : null; }
        public String trim(String str) { return str != null ? str.trim() : null; }
        public String substring(String str, int start, int end) { return str != null ? str.substring(start, end) : null; }
        public String replace(String str, String target, String replacement) { return str != null ? str.replace(target, replacement) : null; }
        public boolean matches(String str, String regex) { return str != null && Pattern.matches(regex, str); }
    }

    public static class DateTransformationUtils {
        public String formatDate(String date, String format) {
            // Simplified date formatting - can be extended with proper date parsing
            return date;
        }
        
        public String getCurrentDate() {
            return new java.util.Date().toString();
        }
    }

    public static class NumberTransformationUtils {
        public double parseDouble(String str) {
            try {
                return Double.parseDouble(str);
            } catch (NumberFormatException e) {
                return 0.0;
            }
        }
        
        public int parseInt(String str) {
            try {
                return Integer.parseInt(str);
            } catch (NumberFormatException e) {
                return 0;
            }
        }
        
        public String formatNumber(double number, String format) {
            return String.format(format, number);
        }
    }
}