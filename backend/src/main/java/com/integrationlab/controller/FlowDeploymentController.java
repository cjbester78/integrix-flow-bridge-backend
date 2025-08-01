package com.integrationlab.controller;

import com.integrationlab.service.FlowDeploymentService;
import com.integrationlab.shared.dto.DeploymentInfoDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/flows/{flowId}/deployment")
@CrossOrigin(origins = "*")
public class FlowDeploymentController {
    
    @Autowired
    private FlowDeploymentService deploymentService;
    
    @PostMapping("/deploy")
    public ResponseEntity<DeploymentInfoDTO> deployFlow(
            @PathVariable String flowId,
            Authentication authentication) {
        try {
            String userId = authentication != null ? authentication.getName() : "system";
            DeploymentInfoDTO deploymentInfo = deploymentService.deployFlow(flowId, userId);
            return ResponseEntity.ok(deploymentInfo);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }
    
    @PostMapping("/undeploy")
    public ResponseEntity<Void> undeployFlow(
            @PathVariable String flowId,
            Authentication authentication) {
        try {
            String userId = authentication != null ? authentication.getName() : "system";
            deploymentService.undeployFlow(flowId, userId);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }
    
    @GetMapping
    public ResponseEntity<DeploymentInfoDTO> getDeploymentInfo(@PathVariable String flowId) {
        try {
            DeploymentInfoDTO deploymentInfo = deploymentService.getDeploymentInfo(flowId);
            if (deploymentInfo != null) {
                return ResponseEntity.ok(deploymentInfo);
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }
}