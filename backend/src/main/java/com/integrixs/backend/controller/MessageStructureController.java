package com.integrixs.backend.controller;

import com.integrixs.backend.security.CurrentUser;
import com.integrixs.backend.service.MessageStructureService;
import com.integrixs.data.model.User;
import com.integrixs.shared.dto.structure.MessageStructureCreateRequestDTO;
import com.integrixs.shared.dto.structure.MessageStructureDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Arrays;
import java.util.List;

@RestController
@RequestMapping("/api/message-structures")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Message Structures", description = "Message structure management endpoints")
public class MessageStructureController {
    
    private final MessageStructureService messageStructureService;
    
    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'USER')")
    @Operation(summary = "Create a new message structure")
    public ResponseEntity<MessageStructureDTO> create(@Valid @RequestBody MessageStructureCreateRequestDTO request,
                                                    @CurrentUser User currentUser) {
        log.info("Creating message structure: {}", request.getName());
        MessageStructureDTO created = messageStructureService.create(request, currentUser);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }
    
    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'USER')")
    @Operation(summary = "Update an existing message structure")
    public ResponseEntity<MessageStructureDTO> update(@PathVariable String id,
                                                    @Valid @RequestBody MessageStructureCreateRequestDTO request,
                                                    @CurrentUser User currentUser) {
        log.info("Updating message structure: {}", id);
        MessageStructureDTO updated = messageStructureService.update(id, request, currentUser);
        return ResponseEntity.ok(updated);
    }
    
    @GetMapping("/{id}")
    @Operation(summary = "Get a message structure by ID")
    public ResponseEntity<MessageStructureDTO> findById(@PathVariable String id) {
        log.info("Getting message structure: {}", id);
        MessageStructureDTO structure = messageStructureService.findById(id);
        return ResponseEntity.ok(structure);
    }
    
    @GetMapping
    @Operation(summary = "Get all message structures with filters")
    public ResponseEntity<Page<MessageStructureDTO>> findAll(
            @RequestParam(required = false) String businessComponentId,
            @RequestParam(required = false) String search,
            Pageable pageable) {
        log.info("Getting all message structures with filters");
        Page<MessageStructureDTO> structures = messageStructureService.findAll(businessComponentId, search, pageable);
        return ResponseEntity.ok(structures);
    }
    
    @GetMapping("/by-business-component/{businessComponentId}")
    @Operation(summary = "Get all message structures for a business component")
    public ResponseEntity<List<MessageStructureDTO>> findByBusinessComponent(@PathVariable String businessComponentId) {
        log.info("Getting message structures for business component: {}", businessComponentId);
        List<MessageStructureDTO> structures = messageStructureService.findByBusinessComponent(businessComponentId);
        return ResponseEntity.ok(structures);
    }
    
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMINISTRATOR')")
    @Operation(summary = "Delete a message structure")
    public ResponseEntity<Void> delete(@PathVariable String id) {
        log.info("Deleting message structure: {}", id);
        messageStructureService.delete(id);
        return ResponseEntity.noContent().build();
    }
    
    @PostMapping(value = "/validate-xsd", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasAnyRole('ADMINISTRATOR', 'DEVELOPER', 'INTEGRATOR')")
    @Operation(summary = "Validate XSD files and check dependencies")
    public ResponseEntity<List<?>> validateXsdFiles(@RequestParam("files") MultipartFile[] files,
                                                   @CurrentUser User currentUser) {
        log.info("Validating {} XSD files", files.length);
        return ResponseEntity.ok(messageStructureService.validateXsdFiles(Arrays.asList(files)));
    }
    
    @PostMapping(value = "/import-xsd", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasAnyRole('ADMINISTRATOR', 'DEVELOPER', 'INTEGRATOR')")
    @Operation(summary = "Import XSD files as message structures")
    public ResponseEntity<List<?>> importXsdFiles(@RequestParam("files") MultipartFile[] files,
                                                @CurrentUser User currentUser) {
        log.info("Importing {} XSD files", files.length);
        return ResponseEntity.ok(messageStructureService.importXsdFiles(Arrays.asList(files), currentUser));
    }
}