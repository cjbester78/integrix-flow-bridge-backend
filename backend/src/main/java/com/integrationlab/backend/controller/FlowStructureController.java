package com.integrationlab.backend.controller;

import com.integrationlab.backend.security.CurrentUser;
import com.integrationlab.backend.service.FlowStructureService;
import com.integrationlab.data.model.User;
import com.integrationlab.shared.dto.structure.FlowStructureCreateRequestDTO;
import com.integrationlab.shared.dto.structure.FlowStructureDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/flow-structures")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Flow Structures", description = "Flow structure management endpoints")
public class FlowStructureController {
    
    private final FlowStructureService flowStructureService;
    
    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'USER')")
    @Operation(summary = "Create a new flow structure")
    public ResponseEntity<FlowStructureDTO> create(@Valid @RequestBody FlowStructureCreateRequestDTO request,
                                                 @CurrentUser User currentUser) {
        log.info("Creating flow structure: {}", request.getName());
        FlowStructureDTO created = flowStructureService.create(request, currentUser);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }
    
    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'USER')")
    @Operation(summary = "Update an existing flow structure")
    public ResponseEntity<FlowStructureDTO> update(@PathVariable String id,
                                                 @Valid @RequestBody FlowStructureCreateRequestDTO request,
                                                 @CurrentUser User currentUser) {
        log.info("Updating flow structure: {}", id);
        FlowStructureDTO updated = flowStructureService.update(id, request, currentUser);
        return ResponseEntity.ok(updated);
    }
    
    @GetMapping("/{id}")
    @Operation(summary = "Get a flow structure by ID")
    public ResponseEntity<FlowStructureDTO> findById(@PathVariable String id) {
        log.info("Getting flow structure: {}", id);
        FlowStructureDTO structure = flowStructureService.findById(id);
        return ResponseEntity.ok(structure);
    }
    
    @GetMapping
    @Operation(summary = "Get all flow structures with filters")
    public ResponseEntity<Page<FlowStructureDTO>> findAll(
            @RequestParam(required = false) String businessComponentId,
            @RequestParam(required = false) FlowStructureDTO.ProcessingMode processingMode,
            @RequestParam(required = false) FlowStructureDTO.Direction direction,
            @RequestParam(required = false) String search,
            Pageable pageable) {
        log.info("Getting all flow structures with filters");
        Page<FlowStructureDTO> structures = flowStructureService.findAll(
                businessComponentId, processingMode, direction, search, pageable);
        return ResponseEntity.ok(structures);
    }
    
    @GetMapping("/by-business-component/{businessComponentId}")
    @Operation(summary = "Get all flow structures for a business component")
    public ResponseEntity<List<FlowStructureDTO>> findByBusinessComponent(@PathVariable String businessComponentId) {
        log.info("Getting flow structures for business component: {}", businessComponentId);
        List<FlowStructureDTO> structures = flowStructureService.findByBusinessComponent(businessComponentId);
        return ResponseEntity.ok(structures);
    }
    
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Delete a flow structure")
    public ResponseEntity<Void> delete(@PathVariable String id) {
        log.info("Deleting flow structure: {}", id);
        flowStructureService.delete(id);
        return ResponseEntity.noContent().build();
    }
}