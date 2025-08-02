package com.integrationlab.backend.controller.v1;

import com.integrationlab.backend.config.ApiVersioningConfig.ApiV1;
import com.integrationlab.backend.domain.services.FlowDomainService;
import com.integrationlab.backend.service.FlowCompositionService;
import com.integrationlab.backend.service.IntegrationFlowService;
import com.integrationlab.data.model.IntegrationFlow;
import com.integrationlab.shared.dto.flow.FlowCreateRequestDTO;
import com.integrationlab.shared.dto.flow.IntegrationFlowDTO;
import io.micrometer.core.annotation.Timed;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import org.springframework.data.domain.PageImpl;
import java.util.List;
import java.util.stream.Collectors;

/**
 * REST controller for integration flows - API Version 1.
 * 
 * <p>Provides versioned endpoints for flow management.
 * 
 * @author Integration Team
 * @since 1.0.0
 */
@Slf4j
@RestController
@RequestMapping("/flows")
@ApiV1  // This annotation marks it for v1 versioning
@RequiredArgsConstructor
@CrossOrigin(origins = "*", maxAge = 3600)
public class FlowControllerV1 {
    
    private final FlowCompositionService flowCompositionService;
    private final IntegrationFlowService integrationFlowService;
    private final FlowDomainService flowDomainService;
    
    /**
     * Gets all integration flows with pagination.
     * 
     * @param pageable pagination parameters
     * @return page of flows
     */
    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'INTEGRATOR', 'VIEWER')")
    @Timed(value = "api.flows.list", description = "Time taken to list flows")
    public ResponseEntity<Page<IntegrationFlowDTO>> getAllFlows(Pageable pageable) {
        log.debug("GET /api/v1/flows - page: {}, size: {}", 
                 pageable.getPageNumber(), pageable.getPageSize());
        
        // Convert list to page manually
        List<IntegrationFlow> allFlows = integrationFlowService.getAllFlows();
        List<IntegrationFlowDTO> flowDTOs = allFlows.stream()
            .map(this::mapToDTO)
            .collect(Collectors.toList());
        
        // Simple pagination
        int start = (int) pageable.getOffset();
        int end = Math.min((start + pageable.getPageSize()), flowDTOs.size());
        
        Page<IntegrationFlowDTO> flows = new PageImpl<>(
            flowDTOs.subList(start, end),
            pageable,
            flowDTOs.size()
        );
        return ResponseEntity.ok(flows);
    }
    
    /**
     * Gets a specific integration flow by ID.
     * 
     * @param id the flow ID
     * @return the flow
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'INTEGRATOR', 'VIEWER')")
    @Timed(value = "api.flows.get", description = "Time taken to get flow")
    public ResponseEntity<IntegrationFlowDTO> getFlow(@PathVariable String id) {
        log.debug("GET /api/v1/flows/{}", id);
        
        return integrationFlowService.getFlowById(id)
            .map(this::mapToDTO)
            .map(ResponseEntity::ok)
            .orElse(ResponseEntity.notFound().build());
    }
    
    /**
     * Creates a new integration flow.
     * 
     * @param request the flow creation request
     * @param userDetails the authenticated user
     * @return created flow
     */
    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'INTEGRATOR')")
    @Timed(value = "api.flows.create", description = "Time taken to create flow")
    public ResponseEntity<IntegrationFlowDTO> createFlow(
            @Valid @RequestBody FlowCreateRequestDTO request,
            @AuthenticationPrincipal UserDetails userDetails) {
        
        log.info("POST /api/v1/flows - Creating flow: {} by user: {}", 
                 request.getName(), userDetails.getUsername());
        
        IntegrationFlow flow = mapToEntity(request);
        IntegrationFlow created = flowDomainService.createFlow(flow, userDetails.getUsername());
        IntegrationFlowDTO dto = mapToDTO(created);
        
        return ResponseEntity.status(HttpStatus.CREATED).body(dto);
    }
    
    /**
     * Activates an integration flow.
     * 
     * @param id the flow ID
     * @param userDetails the authenticated user
     * @return activated flow
     */
    @PutMapping("/{id}/activate")
    @PreAuthorize("hasAnyRole('ADMIN', 'INTEGRATOR')")
    @Timed(value = "api.flows.activate", description = "Time taken to activate flow")
    public ResponseEntity<IntegrationFlowDTO> activateFlow(
            @PathVariable String id,
            @AuthenticationPrincipal UserDetails userDetails) {
        
        log.info("PUT /api/v1/flows/{}/activate - User: {}", id, userDetails.getUsername());
        
        IntegrationFlow activated = flowDomainService.activateFlow(id, userDetails.getUsername());
        IntegrationFlowDTO dto = mapToDTO(activated);
        
        return ResponseEntity.ok(dto);
    }
    
    /**
     * Deactivates an integration flow.
     * 
     * @param id the flow ID
     * @param reason the deactivation reason
     * @param userDetails the authenticated user
     * @return deactivated flow
     */
    @PutMapping("/{id}/deactivate")
    @PreAuthorize("hasAnyRole('ADMIN', 'INTEGRATOR')")
    @Timed(value = "api.flows.deactivate", description = "Time taken to deactivate flow")
    public ResponseEntity<IntegrationFlowDTO> deactivateFlow(
            @PathVariable String id,
            @RequestParam(required = false, defaultValue = "Manual deactivation") String reason,
            @AuthenticationPrincipal UserDetails userDetails) {
        
        log.info("PUT /api/v1/flows/{}/deactivate - User: {}, Reason: {}", 
                 id, userDetails.getUsername(), reason);
        
        IntegrationFlow deactivated = flowDomainService.deactivateFlow(
            id, userDetails.getUsername(), reason);
        IntegrationFlowDTO dto = mapToDTO(deactivated);
        
        return ResponseEntity.ok(dto);
    }
    
    /**
     * Deletes an integration flow.
     * 
     * @param id the flow ID
     * @param userDetails the authenticated user
     * @return no content
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Timed(value = "api.flows.delete", description = "Time taken to delete flow")
    public ResponseEntity<Void> deleteFlow(
            @PathVariable String id,
            @AuthenticationPrincipal UserDetails userDetails) {
        
        log.warn("DELETE /api/v1/flows/{} - User: {}", id, userDetails.getUsername());
        
        integrationFlowService.deleteFlow(id);
        return ResponseEntity.noContent().build();
    }
    
    /**
     * Maps DTO to entity.
     * 
     * @param dto the DTO
     * @return the entity
     */
    private IntegrationFlow mapToEntity(FlowCreateRequestDTO dto) {
        IntegrationFlow flow = new IntegrationFlow();
        flow.setName(dto.getName());
        flow.setDescription(dto.getDescription());
        flow.setSourceAdapterId(dto.getSourceAdapterId());
        flow.setTargetAdapterId(dto.getTargetAdapterId());
        flow.setConfiguration(dto.getConfiguration() != null ? 
            dto.getConfiguration().toString() : "{}");
        return flow;
    }
    
    /**
     * Maps entity to DTO.
     * 
     * @param flow the entity
     * @return the DTO
     */
    private IntegrationFlowDTO mapToDTO(IntegrationFlow flow) {
        return IntegrationFlowDTO.builder()
            .id(flow.getId())
            .name(flow.getName())
            .description(flow.getDescription())
            .sourceAdapterId(flow.getSourceAdapterId())
            .targetAdapterId(flow.getTargetAdapterId())
            .sourceStructureId(flow.getSourceStructureId())
            .targetStructureId(flow.getTargetStructureId())
            .status(flow.getStatus().toString())
            .mappingMode(flow.getMappingMode() != null ? flow.getMappingMode().toString() : null)
            .isActive(flow.isActive())
            .createdAt(flow.getCreatedAt())
            .updatedAt(flow.getUpdatedAt())
            .createdBy(flow.getCreatedBy())
            .build();
    }
}