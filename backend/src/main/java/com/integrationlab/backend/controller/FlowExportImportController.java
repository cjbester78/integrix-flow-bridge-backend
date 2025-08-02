package com.integrationlab.backend.controller;

import com.integrationlab.backend.service.FlowExportService;
import com.integrationlab.backend.service.FlowImportService;
import com.integrationlab.shared.dto.export.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;

/**
 * REST controller for flow export/import operations.
 */
@Slf4j
@RestController
@RequestMapping("/api/flows/export-import")
@CrossOrigin(origins = "*", maxAge = 3600)
@RequiredArgsConstructor
@Tag(name = "Flow Export/Import", description = "Export and import integration flows")
public class FlowExportImportController {

    private final FlowExportService exportService;
    private final FlowImportService importService;
    private final ObjectMapper objectMapper;

    /**
     * Export a flow to JSON file.
     *
     * @param request Export request
     * @return JSON file download
     */
    @PostMapping("/export")
    @PreAuthorize("hasAnyRole('ADMIN', 'INTEGRATOR')")
    @Operation(summary = "Export an integration flow")
    public ResponseEntity<Resource> exportFlow(@Valid @RequestBody FlowExportRequestDTO request) {
        log.info("Exporting flow: {}", request.getFlowId());
        
        try {
            // Export the flow
            FlowExportDTO export = exportService.exportFlow(request);
            
            // Convert to JSON
            String json = objectMapper.writerWithDefaultPrettyPrinter()
                    .writeValueAsString(export);
            
            // Create filename
            String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
            String filename = String.format("flow_%s_%s.json", 
                    sanitizeFilename(export.getFlow().getName()), timestamp);
            
            // Return as downloadable file
            ByteArrayResource resource = new ByteArrayResource(json.getBytes(StandardCharsets.UTF_8));
            
            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                    .contentType(MediaType.APPLICATION_JSON)
                    .contentLength(resource.contentLength())
                    .body(resource);
                    
        } catch (Exception e) {
            log.error("Failed to export flow", e);
            throw new RuntimeException("Export failed: " + e.getMessage(), e);
        }
    }

    /**
     * Export a flow and return as JSON response.
     *
     * @param flowId Flow ID
     * @return Export data
     */
    @GetMapping("/export/{flowId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'INTEGRATOR')")
    @Operation(summary = "Export flow as JSON response")
    public ResponseEntity<FlowExportDTO> exportFlowJson(@PathVariable String flowId) {
        log.info("Exporting flow as JSON: {}", flowId);
        
        FlowExportRequestDTO request = FlowExportRequestDTO.builder()
                .flowId(flowId)
                .build();
        
        FlowExportDTO export = exportService.exportFlow(request);
        return ResponseEntity.ok(export);
    }

    /**
     * Validate if a flow can be exported.
     *
     * @param flowId Flow ID
     * @return Validation result
     */
    @GetMapping("/export/{flowId}/validate")
    @PreAuthorize("hasAnyRole('ADMIN', 'INTEGRATOR')")
    @Operation(summary = "Validate flow export")
    public ResponseEntity<Map<String, Object>> validateExport(@PathVariable String flowId) {
        log.info("Validating export for flow: {}", flowId);
        
        Map<String, Object> validation = exportService.validateExport(flowId);
        return ResponseEntity.ok(validation);
    }

    /**
     * Import a flow from uploaded file.
     *
     * @param file JSON file containing exported flow
     * @param options Import options as query parameters
     * @return Import result
     */
    @PostMapping("/import/file")
    @PreAuthorize("hasAnyRole('ADMIN', 'INTEGRATOR')")
    @Operation(summary = "Import flow from file upload")
    public ResponseEntity<FlowImportResultDTO> importFlowFromFile(
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "conflictStrategy", defaultValue = "FAIL") String conflictStrategy,
            @RequestParam(value = "activateAfterImport", defaultValue = "false") boolean activateAfterImport,
            @RequestParam(value = "namePrefix", required = false) String namePrefix,
            @RequestParam(value = "nameSuffix", required = false) String nameSuffix) {
        
        log.info("Importing flow from file: {}", file.getOriginalFilename());
        
        try {
            // Read file content
            String content = new String(file.getBytes(), StandardCharsets.UTF_8);
            FlowExportDTO exportData = objectMapper.readValue(content, FlowExportDTO.class);
            
            // Build import request
            FlowImportRequestDTO.ImportOptions options = FlowImportRequestDTO.ImportOptions.builder()
                    .conflictStrategy(FlowImportRequestDTO.ConflictStrategy.valueOf(conflictStrategy))
                    .activateAfterImport(activateAfterImport)
                    .namePrefix(namePrefix)
                    .nameSuffix(nameSuffix)
                    .build();
            
            FlowImportRequestDTO request = FlowImportRequestDTO.builder()
                    .flowExport(exportData)
                    .options(options)
                    .build();
            
            // Import the flow
            FlowImportResultDTO result = importService.importFlow(request);
            
            return ResponseEntity.ok(result);
            
        } catch (IOException e) {
            log.error("Failed to read import file", e);
            throw new RuntimeException("Failed to read file: " + e.getMessage(), e);
        }
    }

    /**
     * Import a flow from JSON request body.
     *
     * @param request Import request
     * @return Import result
     */
    @PostMapping("/import")
    @PreAuthorize("hasAnyRole('ADMIN', 'INTEGRATOR')")
    @Operation(summary = "Import flow from JSON")
    public ResponseEntity<FlowImportResultDTO> importFlow(@Valid @RequestBody FlowImportRequestDTO request) {
        log.info("Importing flow: {}", request.getFlowExport().getFlow().getName());
        
        FlowImportResultDTO result = importService.importFlow(request);
        
        if (result.isSuccess()) {
            return ResponseEntity.ok(result);
        } else {
            return ResponseEntity.badRequest().body(result);
        }
    }

    /**
     * Validate an import before actually importing.
     *
     * @param request Import request
     * @return Validation result
     */
    @PostMapping("/import/validate")
    @PreAuthorize("hasAnyRole('ADMIN', 'INTEGRATOR')")
    @Operation(summary = "Validate flow import")
    public ResponseEntity<FlowImportValidationDTO> validateImport(@Valid @RequestBody FlowImportRequestDTO request) {
        log.info("Validating import for flow: {}", request.getFlowExport().getFlow().getName());
        
        FlowImportValidationDTO validation = importService.validateImport(request);
        return ResponseEntity.ok(validation);
    }

    /**
     * Validate import from uploaded file.
     *
     * @param file JSON file containing exported flow
     * @return Validation result
     */
    @PostMapping("/import/validate/file")
    @PreAuthorize("hasAnyRole('ADMIN', 'INTEGRATOR')")
    @Operation(summary = "Validate import from file")
    public ResponseEntity<FlowImportValidationDTO> validateImportFromFile(
            @RequestParam("file") MultipartFile file) {
        
        log.info("Validating import from file: {}", file.getOriginalFilename());
        
        try {
            // Read file content
            String content = new String(file.getBytes(), StandardCharsets.UTF_8);
            FlowExportDTO exportData = objectMapper.readValue(content, FlowExportDTO.class);
            
            // Build import request with default options
            FlowImportRequestDTO request = FlowImportRequestDTO.builder()
                    .flowExport(exportData)
                    .options(FlowImportRequestDTO.ImportOptions.builder().build())
                    .build();
            
            // Validate
            FlowImportValidationDTO validation = importService.validateImport(request);
            
            return ResponseEntity.ok(validation);
            
        } catch (IOException e) {
            log.error("Failed to read import file", e);
            throw new RuntimeException("Failed to read file: " + e.getMessage(), e);
        }
    }

    /**
     * Sanitize filename for safe download.
     *
     * @param filename Original filename
     * @return Sanitized filename
     */
    private String sanitizeFilename(String filename) {
        return filename.replaceAll("[^a-zA-Z0-9._-]", "_");
    }
}