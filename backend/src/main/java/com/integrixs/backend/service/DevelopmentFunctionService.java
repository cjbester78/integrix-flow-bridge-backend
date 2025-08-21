package com.integrixs.backend.service;

import com.integrixs.backend.exception.BusinessException;
import com.integrixs.data.model.TransformationCustomFunction;
import com.integrixs.data.repository.TransformationCustomFunctionRepository;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class DevelopmentFunctionService {
    
    private final TransformationCustomFunctionRepository functionRepository;
    private final JavaCompilationService compilationService;
    private final org.springframework.core.env.Environment environment;
    
    /**
     * Check if development mode is enabled
     */
    public boolean isDevelopmentMode() {
        String[] activeProfiles = environment.getActiveProfiles();
        String profilesStr = String.join(",", activeProfiles);
        log.debug("Checking development mode, activeProfiles: {}", profilesStr);
        
        for (String profile : activeProfiles) {
            if ("dev".equalsIgnoreCase(profile) || "development".equalsIgnoreCase(profile)) {
                log.debug("isDevelopmentMode: true (found profile: {})", profile);
                return true;
            }
        }
        
        log.debug("isDevelopmentMode: false");
        return false;
    }
    
    /**
     * Compile Java function code
     */
    public JavaCompilationService.CompilationResult compileJavaFunction(String functionName, String code) {
        if (!isDevelopmentMode()) {
            throw new BusinessException("Java compilation is only allowed in development mode");
        }
        
        return compilationService.compileFunction(functionName, code);
    }
    
    /**
     * Get a specific function by ID
     */
    @Transactional(readOnly = true)
    public TransformationCustomFunction getFunction(String functionId) {
        return functionRepository.findById(UUID.fromString(functionId))
                .orElseThrow(() -> new BusinessException("Function not found: " + functionId));
    }
    
    /**
     * Get a built-in function by name
     */
    @Transactional(readOnly = true)
    public TransformationCustomFunction getBuiltInFunctionByName(String functionName) {
        return functionRepository.findByName(functionName)
                .orElseThrow(() -> new BusinessException("Built-in function not found: " + functionName));
    }
    
    /**
     * Get all functions (both built-in and custom)
     */
    @Transactional(readOnly = true)
    public DevelopmentFunctionsResponse getAllFunctions(Pageable pageable) {
        try {
            log.debug("Getting all development functions, developmentMode: {}", isDevelopmentMode());
            
            DevelopmentFunctionsResponse response = new DevelopmentFunctionsResponse();
            response.setDevelopmentMode(isDevelopmentMode());
            
            // Get built-in functions from database
            List<TransformationCustomFunction> builtInFunctions = functionRepository.findByBuiltInTrue();
            log.debug("Found {} built-in functions", builtInFunctions.size());
            response.setBuiltInFunctions(convertToBuiltInFunctions(builtInFunctions));
            
            // Get custom functions from database (non-built-in)
            Specification<TransformationCustomFunction> spec = (root, query, criteriaBuilder) -> 
                criteriaBuilder.equal(root.get("builtIn"), false);
            
            Page<TransformationCustomFunction> customFunctions = functionRepository.findAll(spec, pageable);
            log.debug("Found {} custom functions", customFunctions.getTotalElements());
            response.setCustomFunctions(CustomFunctionsPage.fromPage(customFunctions));
            
            return response;
        } catch (Exception e) {
            log.error("Error getting development functions", e);
            throw new BusinessException("Failed to retrieve development functions: " + e.getMessage());
        }
    }
    
    /**
     * Convert TransformationCustomFunction entities to BuiltInFunction DTOs
     */
    private List<BuiltInFunction> convertToBuiltInFunctions(List<TransformationCustomFunction> functions) {
        return functions.stream()
            .map(func -> new BuiltInFunction(
                func.getName(),
                func.getCategory() != null ? func.getCategory() : "general",
                func.getDescription(),
                func.getFunctionSignature(),
                func.getParameters()
            ))
            .collect(java.util.stream.Collectors.toList());
    }
    
    /**
     * Create a new custom function (development mode only)
     */
    @Transactional
    public TransformationCustomFunction createFunction(FunctionCreateRequest request) {
        if (!isDevelopmentMode()) {
            throw new BusinessException("Function creation is only allowed in development mode");
        }
        
        // Check if function name already exists
        if (functionRepository.findByName(request.getName()).isPresent()) {
            throw new BusinessException("Function with name '" + request.getName() + "' already exists");
        }
        
        // Validate function syntax
        validateFunctionSyntax(request.getLanguage(), request.getFunctionBody());
        
        TransformationCustomFunction function = TransformationCustomFunction.builder()
                .functionId(UUID.randomUUID())
                .name(request.getName())
                .description(request.getDescription())
                .category(request.getCategory())
                .language(request.getLanguage())
                .functionSignature(request.getFunctionSignature())
                .functionBody(request.getFunctionBody())
                .dependencies(request.getDependencies())
                .testCases(request.getTestCases())
                .isSafe(request.getIsSafe() != null ? request.getIsSafe() : false)
                .isPublic(request.getIsPublic() != null ? request.getIsPublic() : true)
                .builtIn(false) // Custom functions are never built-in
                .performanceClass(request.getPerformanceClass() != null ? 
                        request.getPerformanceClass() : TransformationCustomFunction.PerformanceClass.NORMAL)
                .version(1)
                .createdBy(request.getCreatedBy())
                .build();
        
        return functionRepository.save(function);
    }
    
    /**
     * Update an existing custom function (development mode only)
     */
    @Transactional
    public TransformationCustomFunction updateFunction(String functionId, FunctionUpdateRequest request) {
        if (!isDevelopmentMode()) {
            throw new BusinessException("Function update is only allowed in development mode");
        }
        
        TransformationCustomFunction function = functionRepository.findById(UUID.fromString(functionId))
                .orElseThrow(() -> new BusinessException("Function not found: " + functionId));
        
        // Allow editing built-in functions in development mode
        if (function.isBuiltIn() && !isDevelopmentMode()) {
            throw new BusinessException("Built-in functions can only be modified in development mode");
        }
        
        // Check if new name conflicts with existing function
        if (request.getName() != null && !request.getName().equals(function.getName())) {
            if (functionRepository.existsByNameAndFunctionIdNot(request.getName(), UUID.fromString(functionId))) {
                throw new BusinessException("Function with name '" + request.getName() + "' already exists");
            }
            function.setName(request.getName());
        }
        
        if (request.getDescription() != null) {
            function.setDescription(request.getDescription());
        }
        
        if (request.getCategory() != null) {
            function.setCategory(request.getCategory());
        }
        
        if (request.getFunctionSignature() != null) {
            function.setFunctionSignature(request.getFunctionSignature());
        }
        
        if (request.getParameters() != null) {
            // Convert parameters array to JSON string
            try {
                com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
                String parametersJson = mapper.writeValueAsString(request.getParameters());
                function.setParameters(parametersJson);
            } catch (Exception e) {
                log.error("Error serializing parameters", e);
                throw new BusinessException("Invalid parameters format");
            }
        }
        
        if (request.getFunctionBody() != null) {
            // Skip validation for built-in functions as they have pre-compiled code
            if (!function.isBuiltIn()) {
                validateFunctionSyntax(function.getLanguage(), request.getFunctionBody());
            }
            function.setFunctionBody(request.getFunctionBody());
        }
        
        if (request.getDependencies() != null) {
            function.setDependencies(request.getDependencies());
        }
        
        if (request.getTestCases() != null) {
            function.setTestCases(request.getTestCases());
        }
        
        if (request.getIsSafe() != null) {
            function.setSafe(request.getIsSafe());
        }
        
        if (request.getIsPublic() != null) {
            function.setPublic(request.getIsPublic());
        }
        
        if (request.getPerformanceClass() != null) {
            function.setPerformanceClass(request.getPerformanceClass());
        }
        
        function.setVersion(function.getVersion() + 1);
        
        return functionRepository.save(function);
    }
    
    /**
     * Delete a custom function (development mode only)
     */
    @Transactional
    public void deleteFunction(String functionId) {
        if (!isDevelopmentMode()) {
            throw new BusinessException("Function deletion is only allowed in development mode");
        }
        
        if (!functionRepository.existsById(UUID.fromString(functionId))) {
            throw new BusinessException("Function not found: " + functionId);
        }
        
        functionRepository.deleteById(UUID.fromString(functionId));
    }
    
    /**
     * Test a function with provided inputs
     */
    public FunctionTestResult testFunction(String functionId, Map<String, Object> inputs) {
        TransformationCustomFunction function = functionRepository.findById(UUID.fromString(functionId))
                .orElseThrow(() -> new BusinessException("Function not found: " + functionId));
        
        FunctionTestResult result = new FunctionTestResult();
        result.setFunctionId(functionId);
        result.setFunctionName(function.getName());
        
        try {
            Object output = executeFunction(function, inputs);
            result.setSuccess(true);
            result.setOutput(output);
        } catch (Exception e) {
            result.setSuccess(false);
            result.setError(e.getMessage());
        }
        
        return result;
    }
    
    /**
     * Execute a function (simplified implementation)
     */
    private Object executeFunction(TransformationCustomFunction function, Map<String, Object> inputs) throws Exception {
        if (function.isBuiltIn() && function.getLanguage() == TransformationCustomFunction.FunctionLanguage.JAVA) {
            // Handle built-in Java functions
            return executeBuiltInFunction(function.getName(), inputs);
        } else if (function.getLanguage() == TransformationCustomFunction.FunctionLanguage.JAVASCRIPT) {
            ScriptEngineManager manager = new ScriptEngineManager();
            ScriptEngine engine = manager.getEngineByName("nashorn");
            
            if (engine == null) {
                engine = manager.getEngineByName("graal.js");
            }
            
            if (engine == null) {
                throw new BusinessException("JavaScript engine not available");
            }
            
            // Add inputs to engine context
            for (Map.Entry<String, Object> entry : inputs.entrySet()) {
                engine.put(entry.getKey(), entry.getValue());
            }
            
            // Execute function
            return engine.eval(function.getFunctionBody());
        } else if (function.getLanguage() == TransformationCustomFunction.FunctionLanguage.JAVA) {
            // For custom Java functions, compilation would be needed
            throw new BusinessException("Custom Java function execution not yet implemented");
        } else {
            throw new BusinessException("Unsupported function language: " + function.getLanguage());
        }
    }
    
    /**
     * Execute built-in functions
     */
    private Object executeBuiltInFunction(String functionName, Map<String, Object> inputs) {
        switch (functionName) {
            // Math functions
            case "add":
                return toDouble(inputs.get("a")) + toDouble(inputs.get("b"));
            case "subtract":
                return toDouble(inputs.get("a")) - toDouble(inputs.get("b"));
            case "multiply":
                return toDouble(inputs.get("a")) * toDouble(inputs.get("b"));
            case "divide": {
                double b = toDouble(inputs.get("b"));
                if (b == 0) throw new IllegalArgumentException("Division by zero");
                return toDouble(inputs.get("a")) / b;
            }
            case "power":
                return Math.pow(toDouble(inputs.get("base")), toDouble(inputs.get("exponent")));
            case "sqrt":
                return Math.sqrt(toDouble(inputs.get("value")));
            case "absolute":
                return Math.abs(toDouble(inputs.get("value")));
            case "max":
                return Math.max(toDouble(inputs.get("a")), toDouble(inputs.get("b")));
            case "min":
                return Math.min(toDouble(inputs.get("a")), toDouble(inputs.get("b")));
            case "round":
                return Math.round(toDouble(inputs.get("value")));
            case "ceil":
                return Math.ceil(toDouble(inputs.get("value")));
            case "floor":
                return Math.floor(toDouble(inputs.get("value")));
            
            // String functions
            case "concat": {
                String str1 = toString(inputs.get("string1"));
                String str2 = toString(inputs.get("string2"));
                String delimiter = inputs.containsKey("delimiter") ? toString(inputs.get("delimiter")) : "";
                return delimiter.isEmpty() ? str1 + str2 : str1 + delimiter + str2;
            }
            case "substring": {
                String text = toString(inputs.get("text"));
                int start = toInt(inputs.get("start"));
                if (inputs.containsKey("end")) {
                    return text.substring(start, toInt(inputs.get("end")));
                }
                return text.substring(start);
            }
            case "length":
                return toString(inputs.get("text")).length();
            case "toUpperCase":
                return toString(inputs.get("text")).toUpperCase();
            case "toLowerCase":
                return toString(inputs.get("text")).toLowerCase();
            case "trim":
                return toString(inputs.get("text")).trim();
            case "indexOf":
                return toString(inputs.get("text")).indexOf(toString(inputs.get("searchValue")));
            case "replaceString":
                return toString(inputs.get("text")).replace(
                    toString(inputs.get("searchValue")), 
                    toString(inputs.get("replaceValue"))
                );
            
            // Boolean functions
            case "and":
                return toBoolean(inputs.get("a")) && toBoolean(inputs.get("b"));
            case "or":
                return toBoolean(inputs.get("a")) || toBoolean(inputs.get("b"));
            case "not":
                return !toBoolean(inputs.get("value"));
            case "equals":
                return Objects.equals(inputs.get("a"), inputs.get("b"));
            case "notEquals":
                return !Objects.equals(inputs.get("a"), inputs.get("b"));
            
            // List functions
            case "sum": {
                List<?> values = toList(inputs.get("values"));
                return values.stream()
                    .mapToDouble(v -> toDouble(v))
                    .sum();
            }
            case "average": {
                List<?> values = toList(inputs.get("values"));
                return values.stream()
                    .mapToDouble(v -> toDouble(v))
                    .average()
                    .orElse(0.0);
            }
            case "count":
                return toList(inputs.get("values")).size();
                
            default:
                throw new BusinessException("Built-in function not implemented for testing: " + functionName);
        }
    }
    
    // Helper methods for type conversion
    private double toDouble(Object value) {
        if (value == null) return 0.0;
        if (value instanceof Number) return ((Number) value).doubleValue();
        try {
            return Double.parseDouble(value.toString());
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Cannot convert to double: " + value);
        }
    }
    
    private int toInt(Object value) {
        if (value == null) return 0;
        if (value instanceof Number) return ((Number) value).intValue();
        try {
            return Integer.parseInt(value.toString());
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Cannot convert to int: " + value);
        }
    }
    
    private String toString(Object value) {
        return value == null ? "" : value.toString();
    }
    
    private boolean toBoolean(Object value) {
        if (value == null) return false;
        if (value instanceof Boolean) return (Boolean) value;
        return "true".equalsIgnoreCase(value.toString());
    }
    
    private List<?> toList(Object value) {
        if (value == null) return new ArrayList<>();
        if (value instanceof List) return (List<?>) value;
        if (value instanceof Collection) return new ArrayList<>((Collection<?>) value);
        if (value.getClass().isArray()) return Arrays.asList((Object[]) value);
        // Try to parse as JSON array
        try {
            com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
            return mapper.readValue(value.toString(), List.class);
        } catch (Exception e) {
            // If not JSON, return single-element list
            return Collections.singletonList(value);
        }
    }
    
    /**
     * Validate function syntax
     */
    private void validateFunctionSyntax(TransformationCustomFunction.FunctionLanguage language, String functionBody) {
        if (language == TransformationCustomFunction.FunctionLanguage.JAVASCRIPT) {
            try {
                ScriptEngineManager manager = new ScriptEngineManager();
                ScriptEngine engine = manager.getEngineByName("nashorn");
                if (engine == null) {
                    engine = manager.getEngineByName("graal.js");
                }
                if (engine != null) {
                    engine.eval(functionBody);
                }
            } catch (ScriptException e) {
                throw new BusinessException("Invalid JavaScript syntax: " + e.getMessage());
            }
        } else if (language == TransformationCustomFunction.FunctionLanguage.JAVA) {
            // Skip compilation for built-in functions since they are pre-compiled
            if (functionBody != null && functionBody.contains("// Built-in function")) {
                log.debug("Skipping compilation for built-in function");
                return;
            }
            
            // Compile Java code to validate syntax
            JavaCompilationService.CompilationResult result = 
                compilationService.compileFunction("TestFunction", functionBody);
            
            if (!result.isSuccess()) {
                String errors = String.join("\n", result.getErrors());
                throw new BusinessException("Java compilation failed:\n" + errors);
            }
            
            if (result.hasWarnings()) {
                log.warn("Java compilation warnings:\n{}", String.join("\n", result.getWarnings()));
            }
        }
    }
    
    /**
     * Get list of built-in functions (metadata only)
     * @deprecated Now reading from database instead
     */
    @Deprecated
    private List<BuiltInFunction> getBuiltInFunctionsOld() {
        List<BuiltInFunction> builtInFunctions = new ArrayList<>();
        
        // Math functions
        builtInFunctions.add(new BuiltInFunction("add", "math", "Add two numbers", "add(a, b)"));
        builtInFunctions.add(new BuiltInFunction("subtract", "math", "Subtract two numbers", "subtract(a, b)"));
        builtInFunctions.add(new BuiltInFunction("multiply", "math", "Multiply two numbers", "multiply(a, b)"));
        builtInFunctions.add(new BuiltInFunction("divide", "math", "Divide two numbers", "divide(a, b)"));
        builtInFunctions.add(new BuiltInFunction("equals", "math", "Check if two numbers are equal", "equals(a, b)"));
        builtInFunctions.add(new BuiltInFunction("absolute", "math", "Get absolute value of a number", "absolute(value)"));
        builtInFunctions.add(new BuiltInFunction("sqrt", "math", "Get square root of a number", "sqrt(value)"));
        builtInFunctions.add(new BuiltInFunction("square", "math", "Square a number", "square(value)"));
        builtInFunctions.add(new BuiltInFunction("sign", "math", "Get sign of a number (-1, 0, or 1)", "sign(value)"));
        builtInFunctions.add(new BuiltInFunction("neg", "math", "Negate a number", "neg(value)"));
        builtInFunctions.add(new BuiltInFunction("inv", "math", "Get inverse of a number (1/x)", "inv(value)"));
        builtInFunctions.add(new BuiltInFunction("power", "math", "Raise number to power", "power(base, exponent)"));
        builtInFunctions.add(new BuiltInFunction("lesser", "math", "Check if first number is less than second", "lesser(a, b)"));
        builtInFunctions.add(new BuiltInFunction("greater", "math", "Check if first number is greater than second", "greater(a, b)"));
        builtInFunctions.add(new BuiltInFunction("max", "math", "Get maximum of two numbers", "max(a, b)"));
        builtInFunctions.add(new BuiltInFunction("min", "math", "Get minimum of two numbers", "min(a, b)"));
        builtInFunctions.add(new BuiltInFunction("ceil", "math", "Round number up to nearest integer", "ceil(value)"));
        builtInFunctions.add(new BuiltInFunction("floor", "math", "Round number down to nearest integer", "floor(value)"));
        builtInFunctions.add(new BuiltInFunction("round", "math", "Round number to nearest integer", "round(value)"));
        builtInFunctions.add(new BuiltInFunction("counter", "math", "Generate incremental counter", "counter(start?, step?)"));
        builtInFunctions.add(new BuiltInFunction("formatNumber", "math", "Format number with specified total digits and decimal places", "formatNumber(value, totalDigits, decimals?)"));
        builtInFunctions.add(new BuiltInFunction("sum", "math", "Sum multiple numbers", "sum(values)"));
        builtInFunctions.add(new BuiltInFunction("average", "math", "Calculate average of numbers", "average(values)"));
        builtInFunctions.add(new BuiltInFunction("count", "math", "Count elements in array", "count(values)"));
        builtInFunctions.add(new BuiltInFunction("index", "math", "Get current index in iteration", "index(position)"));
        
        // Text functions
        builtInFunctions.add(new BuiltInFunction("concat", "text", "Concatenate two strings with optional delimiter", "concat(string1, string2, delimiter?)"));
        builtInFunctions.add(new BuiltInFunction("substring", "text", "Extract substring from text", "substring(text, start, end?)"));
        builtInFunctions.add(new BuiltInFunction("equals", "text", "Check if two strings are equal", "equals(string1, string2)"));
        builtInFunctions.add(new BuiltInFunction("indexOf", "text", "Find index of substring", "indexOf(text, searchValue)"));
        builtInFunctions.add(new BuiltInFunction("lastIndexOf", "text", "Find last index of substring", "lastIndexOf(text, searchValue)"));
        builtInFunctions.add(new BuiltInFunction("compare", "text", "Compare two strings lexicographically", "compare(string1, string2)"));
        builtInFunctions.add(new BuiltInFunction("replaceString", "text", "Replace substring in text", "replaceString(text, searchValue, replaceValue)"));
        builtInFunctions.add(new BuiltInFunction("length", "text", "Get length of string", "length(text)"));
        builtInFunctions.add(new BuiltInFunction("endsWith", "text", "Check if string ends with specified suffix", "endsWith(text, suffix)"));
        builtInFunctions.add(new BuiltInFunction("startsWith", "text", "Check if string starts with specified prefix", "startsWith(text, prefix)"));
        builtInFunctions.add(new BuiltInFunction("toUpperCase", "text", "Convert text to uppercase", "toUpperCase(text)"));
        builtInFunctions.add(new BuiltInFunction("toLowerCase", "text", "Convert text to lowercase", "toLowerCase(text)"));
        builtInFunctions.add(new BuiltInFunction("trim", "text", "Remove whitespace from both ends", "trim(text)"));
        
        // Boolean functions
        builtInFunctions.add(new BuiltInFunction("and", "boolean", "Logical AND operation", "and(a, b)"));
        builtInFunctions.add(new BuiltInFunction("or", "boolean", "Logical OR operation", "or(a, b)"));
        builtInFunctions.add(new BuiltInFunction("not", "boolean", "Logical NOT operation", "not(value)"));
        builtInFunctions.add(new BuiltInFunction("equals", "boolean", "Check if two boolean values are equal", "equals(a, b)"));
        builtInFunctions.add(new BuiltInFunction("notEquals", "boolean", "Check if two values are not equal", "notEquals(a, b)"));
        builtInFunctions.add(new BuiltInFunction("if", "boolean", "Conditional logic with true/false branches", "if(condition, trueValue, falseValue)"));
        builtInFunctions.add(new BuiltInFunction("ifWithoutElse", "boolean", "Conditional logic without else branch", "ifWithoutElse(condition, trueValue)"));
        builtInFunctions.add(new BuiltInFunction("isNil", "boolean", "Check if value is null or undefined", "isNil(value)"));
        
        // Conversion functions
        builtInFunctions.add(new BuiltInFunction("fixValues", "conversion", "Fix and validate values according to specified format", "fixValues(value, format)"));
        
        // Date functions
        builtInFunctions.add(new BuiltInFunction("currentDate", "date", "Get current date", "currentDate(format?)"));
        builtInFunctions.add(new BuiltInFunction("dateTrans", "date", "Transform date format", "dateTrans(date, fromFormat, toFormat, firstWeekday?, minDays?, lenient?)"));
        builtInFunctions.add(new BuiltInFunction("dateBefore", "date", "Check if first date is before second date", "dateBefore(date1, date2)"));
        builtInFunctions.add(new BuiltInFunction("dateAfter", "date", "Check if first date is after second date", "dateAfter(date1, date2)"));
        builtInFunctions.add(new BuiltInFunction("compareDates", "date", "Compare two dates (-1, 0, 1)", "compareDates(date1, date2)"));
        
        // Node functions
        builtInFunctions.add(new BuiltInFunction("createIf", "node", "Create conditional node structure", "createIf(condition, value)"));
        builtInFunctions.add(new BuiltInFunction("removeContexts", "node", "Remove context from node structure", "removeContexts(node)"));
        builtInFunctions.add(new BuiltInFunction("replaceValue", "node", "Replace value in node structure", "replaceValue(node, oldValue, newValue)"));
        builtInFunctions.add(new BuiltInFunction("exists", "node", "Check if node exists", "exists(node)"));
        builtInFunctions.add(new BuiltInFunction("getHeader", "node", "Get header value from message", "getHeader(headerName)"));
        builtInFunctions.add(new BuiltInFunction("getProperty", "node", "Get property value", "getProperty(propertyName)"));
        builtInFunctions.add(new BuiltInFunction("splitByValue", "node", "Split node by delimiter", "splitByValue(value, delimiter)"));
        builtInFunctions.add(new BuiltInFunction("collapseContexts", "node", "Collapse multiple contexts into one", "collapseContexts(contexts)"));
        builtInFunctions.add(new BuiltInFunction("useOneAsMany", "node", "Replicate a field that occurs once to pair with fields that occur multiple times", "useOneAsMany(singleField, countField, multipleField)"));
        builtInFunctions.add(new BuiltInFunction("sort", "node", "Sort array of values", "sort(values)"));
        builtInFunctions.add(new BuiltInFunction("sortByKey", "node", "Sort array by specific key", "sortByKey(values, key)"));
        builtInFunctions.add(new BuiltInFunction("mapWithDefault", "node", "Map value with default fallback", "mapWithDefault(value, defaultValue)"));
        builtInFunctions.add(new BuiltInFunction("formatByExample", "node", "Format value using example pattern", "formatByExample(value, example)"));
        
        // Constants functions
        builtInFunctions.add(new BuiltInFunction("constant", "constants", "Set a fixed value", "constant(value)"));
        
        return builtInFunctions;
    }
    
    // DTOs
    @Data
    public static class DevelopmentFunctionsResponse {
        private boolean developmentMode;
        private List<BuiltInFunction> builtInFunctions;
        private CustomFunctionsPage customFunctions;
    }
    
    @Data
    public static class CustomFunctionsPage {
        private List<TransformationCustomFunction> content;
        private long totalElements;
        private int totalPages;
        private int number;
        
        public static CustomFunctionsPage fromPage(Page<TransformationCustomFunction> page) {
            CustomFunctionsPage result = new CustomFunctionsPage();
            result.content = page.getContent();
            result.totalElements = page.getTotalElements();
            result.totalPages = page.getTotalPages();
            result.number = page.getNumber();
            return result;
        }
    }
    
    @Data
    public static class BuiltInFunction {
        private String name;
        private String category;
        private String description;
        private String signature;
        private String parameters; // JSON string of parameters
        
        public BuiltInFunction(String name, String category, String description, String signature) {
            this.name = name;
            this.category = category;
            this.description = description;
            this.signature = signature;
        }
        
        public BuiltInFunction(String name, String category, String description, String signature, String parameters) {
            this.name = name;
            this.category = category;
            this.description = description;
            this.signature = signature;
            this.parameters = parameters;
        }
    }
    
    @Data
    public static class FunctionCreateRequest {
        private String name;
        private String description;
        private String category;
        private TransformationCustomFunction.FunctionLanguage language;
        private String functionSignature;
        private String functionBody;
        private List<String> dependencies;
        private List<TransformationCustomFunction.TestCase> testCases;
        private Boolean isSafe;
        private Boolean isPublic;
        private TransformationCustomFunction.PerformanceClass performanceClass;
        private String createdBy;
    }
    
    @Data
    public static class FunctionUpdateRequest {
        private String name;
        private String description;
        private String category;
        private String functionSignature;
        private String functionBody;
        private List<Map<String, Object>> parameters; // Function parameters metadata
        private List<String> dependencies;
        private List<TransformationCustomFunction.TestCase> testCases;
        private Boolean isSafe;
        private Boolean isPublic;
        private TransformationCustomFunction.PerformanceClass performanceClass;
    }
    
    @Data
    public static class FunctionTestResult {
        private String functionId;
        private String functionName;
        private boolean success;
        private Object output;
        private String error;
    }
}