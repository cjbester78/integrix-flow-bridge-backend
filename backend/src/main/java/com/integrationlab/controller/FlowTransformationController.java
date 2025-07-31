package com.integrationlab.controller;

import com.integrationlab.service.FlowTransformationService;
import com.integrationlab.shared.dto.flow.FlowTransformationDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/flows/{flowId}/transformations")
public class FlowTransformationController {

    @Autowired
    private FlowTransformationService transformationService;

    @GetMapping
    public ResponseEntity<List<FlowTransformationDTO>> getAllByFlow(@PathVariable String flowId) {
        return ResponseEntity.ok(transformationService.getByFlowId(flowId));
    }

    @GetMapping("/{id}")
    public ResponseEntity<FlowTransformationDTO> getById(@PathVariable String flowId, @PathVariable String id) {
        return transformationService.getById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<FlowTransformationDTO> create(@PathVariable String flowId, @RequestBody FlowTransformationDTO transformation) {
        transformation.setFlowId(flowId); // Ensure flow ID is set
        return ResponseEntity.ok(transformationService.save(transformation));
    }

    @PutMapping("/{id}")
    public ResponseEntity<FlowTransformationDTO> update(@PathVariable String flowId, @PathVariable String id, @RequestBody FlowTransformationDTO transformation) {
        transformation.setId(id);
        transformation.setFlowId(flowId);
        return ResponseEntity.ok(transformationService.save(transformation));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable String flowId, @PathVariable String id) {
        transformationService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
