package com.integrixs.engine.transformation;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;

/**
 * Processor for executing field mapping transformations
 */
@Component
public class FieldMappingProcessor {
    
    private static final Logger log = LoggerFactory.getLogger(FieldMappingProcessor.class);
    
    private final ScriptEngineManager scriptEngineManager = new ScriptEngineManager();
    
    /**
     * Execute a transformation function on input values
     * @param functionCode The Java/JavaScript function code
     * @param inputValues The input values to transform
     * @return The transformed result
     */
    public String executeFunction(String functionCode, String... inputValues) {
        if (functionCode == null || functionCode.trim().isEmpty()) {
            // No transformation - concatenate inputs
            return String.join(" ", inputValues);
        }
        
        try {
            // Handle built-in functions
            if (functionCode.startsWith("builtin:")) {
                return executeBuiltinFunction(functionCode.substring(8), inputValues);
            }
            
            // Execute custom JavaScript/Java code
            ScriptEngine engine = scriptEngineManager.getEngineByName("javascript");
            if (engine == null) {
                log.error("JavaScript engine not available");
                return String.join(" ", inputValues);
            }
            
            // Set input values as variables
            for (int i = 0; i < inputValues.length; i++) {
                engine.put("input" + (i + 1), inputValues[i]);
                engine.put("$" + (i + 1), inputValues[i]); // Alternative syntax
            }
            engine.put("inputs", inputValues);
            
            // Execute the function
            Object result = engine.eval(functionCode);
            return result != null ? result.toString() : "";
            
        } catch (ScriptException e) {
            log.error("Error executing transformation function: {}", e.getMessage());
            // Fallback to concatenation
            return String.join(" ", inputValues);
        }
    }
    
    /**
     * Execute built-in transformation functions
     */
    private String executeBuiltinFunction(String functionName, String[] inputValues) {
        switch (functionName.toLowerCase()) {
            case "concat":
                log.info("Concat function called with {} parameters", inputValues.length);
                for (int i = 0; i < inputValues.length; i++) {
                    log.info("  Parameter [{}]: '{}'", i, inputValues[i]);
                }
                
                // The concat function expects: string1, string2, delimiter (optional)
                // Based on the DB definition: string1 + delimiter + string2
                if (inputValues.length < 2) {
                    log.warn("Concat requires at least 2 arguments");
                    return inputValues.length > 0 ? inputValues[0] : "";
                }
                
                String string1 = inputValues[0];
                String string2 = inputValues[1];
                String delimiter = inputValues.length > 2 ? inputValues[2] : "";
                
                String result = delimiter.isEmpty() ? string1 + string2 : string1 + delimiter + string2;
                log.info("Concat result: '{}' (string1='{}', string2='{}', delimiter='{}')", 
                         result, string1, string2, delimiter);
                return result;
                
            case "concatenate":
                return String.join(" ", inputValues);
                
            case "uppercase":
                return inputValues.length > 0 ? inputValues[0].toUpperCase() : "";
                
            case "lowercase":
                return inputValues.length > 0 ? inputValues[0].toLowerCase() : "";
                
            case "trim":
                return inputValues.length > 0 ? inputValues[0].trim() : "";
                
            case "substring":
                if (inputValues.length >= 2) {
                    try {
                        String text = inputValues[0];
                        int start = Integer.parseInt(inputValues[1]);
                        // If end parameter is provided, use it
                        if (inputValues.length >= 3 && !inputValues[2].isEmpty()) {
                            int end = Integer.parseInt(inputValues[2]);
                            return text.substring(start, end);
                        } else {
                            // No end parameter, substring from start to end of string
                            return text.substring(start);
                        }
                    } catch (Exception e) {
                        log.error("Invalid substring parameters", e);
                        return inputValues.length > 0 ? inputValues[0] : "";
                    }
                }
                return inputValues.length > 0 ? inputValues[0] : "";
                
            case "replace":
                if (inputValues.length >= 3) {
                    return inputValues[0].replace(inputValues[1], inputValues[2]);
                }
                return inputValues.length > 0 ? inputValues[0] : "";
                
            case "dateformat":
                // Simple date formatting - can be enhanced
                return inputValues.length > 0 ? inputValues[0] : "";
                
            default:
                log.warn("Unknown built-in function: {}", functionName);
                return String.join(" ", inputValues);
        }
    }
}