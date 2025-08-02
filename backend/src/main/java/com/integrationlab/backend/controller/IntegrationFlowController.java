package com.integrationlab.backend.controller;

import com.integrationlab.model.IntegrationFlow;
import com.integrationlab.backend.service.IntegrationFlowService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/flows")
public class IntegrationFlowController {

    @Autowired
    private IntegrationFlowService integrationFlowService;

    @GetMapping
    public ResponseEntity<List<IntegrationFlow>> getAllFlows() {
        return ResponseEntity.ok(integrationFlowService.getAllFlows());
    }

    @GetMapping("/{id}")
    public ResponseEntity<IntegrationFlow> getFlowById(@PathVariable String id) {
        return integrationFlowService.getFlowById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<IntegrationFlow> createFlow(@RequestBody IntegrationFlow flow) {
        return ResponseEntity.ok(integrationFlowService.createFlow(flow));
    }

    @PutMapping("/{id}")
    public ResponseEntity<IntegrationFlow> updateFlow(@PathVariable String id, @RequestBody IntegrationFlow updatedFlow) {
        IntegrationFlow updated = integrationFlowService.updateFlow(id, updatedFlow);
        return updated != null ? ResponseEntity.ok(updated) : ResponseEntity.notFound().build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteFlow(@PathVariable String id) {
        integrationFlowService.deleteFlow(id);
        return ResponseEntity.noContent().build();
    }
}
