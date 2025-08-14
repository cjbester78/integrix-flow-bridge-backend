package com.integrixs.backend.controller;

import com.integrixs.backend.service.DevelopmentFunctionService;
import com.integrixs.backend.service.JavaCompilationService;
import com.integrixs.data.model.TransformationCustomFunction;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/development/functions")
@RequiredArgsConstructor
@Tag(name = "Development Functions", description = "Development function management endpoints")
@PreAuthorize("hasAnyRole('ADMINISTRATOR', 'DEVELOPER')")
public class DevelopmentFunctionController {
    
    private final DevelopmentFunctionService functionService;
    
    @GetMapping
    @Operation(summary = "Get all functions", description = "Get all built-in and custom functions")
    public ResponseEntity<DevelopmentFunctionService.DevelopmentFunctionsResponse> getAllFunctions(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "name") String sortBy,
            @RequestParam(defaultValue = "ASC") String sortDirection) {
        
        log.info("Fetching all development functions");
        
        Sort.Direction direction = sortDirection.equalsIgnoreCase("DESC") ? 
                Sort.Direction.DESC : Sort.Direction.ASC;
        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortBy));
        
        DevelopmentFunctionService.DevelopmentFunctionsResponse response = 
                functionService.getAllFunctions(pageable);
        
        return ResponseEntity.ok(response);
    }
    
    @GetMapping("/{functionId}")
    @Operation(summary = "Get function by ID", description = "Get a specific custom function")
    public ResponseEntity<TransformationCustomFunction> getFunction(
            @Parameter(description = "Function ID") @PathVariable String functionId) {
        
        log.info("Fetching function: {}", functionId);
        
        TransformationCustomFunction function = functionService.getFunction(functionId);
        return ResponseEntity.ok(function);
    }
    
    @GetMapping("/built-in/{functionName}")
    @Operation(summary = "Get built-in function by name", description = "Get a specific built-in function by its name")
    public ResponseEntity<TransformationCustomFunction> getBuiltInFunction(
            @Parameter(description = "Function name") @PathVariable String functionName) {
        
        log.info("Fetching built-in function: {}", functionName);
        
        TransformationCustomFunction function = functionService.getBuiltInFunctionByName(functionName);
        return ResponseEntity.ok(function);
    }
    
    @PostMapping
    @Operation(summary = "Create function", description = "Create a new custom function (development mode only)")
    public ResponseEntity<TransformationCustomFunction> createFunction(
            @Valid @RequestBody DevelopmentFunctionService.FunctionCreateRequest request) {
        
        log.info("Creating new function: {}", request.getName());
        
        TransformationCustomFunction function = functionService.createFunction(request);
        
        return ResponseEntity.status(HttpStatus.CREATED).body(function);
    }
    
    @PutMapping("/{functionId}")
    @Operation(summary = "Update function", description = "Update an existing custom function (development mode only)")
    public ResponseEntity<TransformationCustomFunction> updateFunction(
            @Parameter(description = "Function ID") @PathVariable String functionId,
            @Valid @RequestBody DevelopmentFunctionService.FunctionUpdateRequest request) {
        
        log.info("Updating function: {}", functionId);
        
        TransformationCustomFunction function = functionService.updateFunction(functionId, request);
        
        return ResponseEntity.ok(function);
    }
    
    @DeleteMapping("/{functionId}")
    @Operation(summary = "Delete function", description = "Delete a custom function (development mode only)")
    public ResponseEntity<Void> deleteFunction(
            @Parameter(description = "Function ID") @PathVariable String functionId) {
        
        log.info("Deleting function: {}", functionId);
        
        functionService.deleteFunction(functionId);
        
        return ResponseEntity.noContent().build();
    }
    
    @PostMapping("/{functionId}/test")
    @Operation(summary = "Test function", description = "Test a function with provided inputs")
    public ResponseEntity<DevelopmentFunctionService.FunctionTestResult> testFunction(
            @Parameter(description = "Function ID") @PathVariable String functionId,
            @RequestBody Map<String, Object> inputs) {
        
        log.info("Testing function: {} with inputs: {}", functionId, inputs);
        
        DevelopmentFunctionService.FunctionTestResult result = 
                functionService.testFunction(functionId, inputs);
        
        return ResponseEntity.ok(result);
    }
    
    @GetMapping("/development-mode")
    @Operation(summary = "Check development mode", description = "Check if development mode is enabled")
    public ResponseEntity<Map<String, Boolean>> isDevelopmentMode() {
        boolean devMode = functionService.isDevelopmentMode();
        return ResponseEntity.ok(Map.of("developmentMode", devMode));
    }
    
    @PostMapping("/compile")
    @Operation(summary = "Compile Java code", description = "Compile and validate Java function code")
    public ResponseEntity<JavaCompilationService.CompilationResult> compileJavaCode(
            @RequestBody Map<String, String> request) {
        
        String functionName = request.get("functionName");
        String code = request.get("code");
        
        if (functionName == null || code == null) {
            throw new IllegalArgumentException("functionName and code are required");
        }
        
        log.info("Compiling Java function: {}", functionName);
        
        JavaCompilationService.CompilationResult result = 
                functionService.compileJavaFunction(functionName, code);
        
        return ResponseEntity.ok(result);
    }
}