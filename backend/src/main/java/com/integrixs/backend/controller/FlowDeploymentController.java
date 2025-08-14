package com.integrixs.backend.controller;

import com.integrixs.backend.service.FlowDeploymentService;
import com.integrixs.shared.dto.DeploymentInfoDTO;
import com.integrixs.data.model.User;
import com.integrixs.data.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import java.util.Map;
import java.util.HashMap;
import org.springframework.http.HttpStatus;

@RestController
@RequestMapping("/api/flows/{flowId}/deployment")
@CrossOrigin(origins = "*")
public class FlowDeploymentController {
    
    private static final Logger logger = LoggerFactory.getLogger(FlowDeploymentController.class);
    
    @Autowired
    private FlowDeploymentService deploymentService;
    
    @Autowired
    private UserRepository userRepository;
    
    @PostMapping("/deploy")
    public ResponseEntity<?> deployFlow(
            @PathVariable String flowId,
            Authentication authentication) {
        try {
            logger.info("Deploy flow request for flowId: {}", flowId);
            
            String userId = null;
            if (authentication != null) {
                String username = authentication.getName();
                logger.info("Username from authentication: {}", username);
                
                User user = userRepository.findByUsername(username);
                if (user != null) {
                    userId = user.getId();
                    logger.info("Found user with ID: {}", userId);
                } else {
                    logger.error("User not found for username: {}", username);
                }
            }
            
            if (userId == null) {
                return ResponseEntity.badRequest().body(Map.of("error", "User not found"));
            }
            
            DeploymentInfoDTO deploymentInfo = deploymentService.deployFlow(flowId, userId);
            logger.info("Deployment successful, returning response for flowId: {}", flowId);
            return ResponseEntity.ok(deploymentInfo);
        } catch (IllegalStateException e) {
            logger.error("IllegalStateException during deployment: ", e);
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            logger.error("Exception during deployment for flowId {}: ", flowId, e);
            e.printStackTrace(); // Add stack trace to console
            
            // Return a more detailed error response
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", e.getMessage() != null ? e.getMessage() : "Failed to deploy flow");
            errorResponse.put("type", e.getClass().getSimpleName());
            errorResponse.put("flowId", flowId);
            
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }
    
    @PostMapping("/undeploy")
    public ResponseEntity<?> undeployFlow(
            @PathVariable String flowId,
            Authentication authentication) {
        try {
            String userId = null;
            if (authentication != null) {
                String username = authentication.getName();
                User user = userRepository.findByUsername(username);
                if (user != null) {
                    userId = user.getId();
                }
            }
            
            if (userId == null) {
                return ResponseEntity.badRequest().body(Map.of("error", "User not found"));
            }
            
            deploymentService.undeployFlow(flowId, userId);
            return ResponseEntity.ok().build();
        } catch (IllegalStateException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", 
                e.getMessage() != null ? e.getMessage() : "Failed to undeploy flow"));
        }
    }
    
    @GetMapping
    public ResponseEntity<?> getDeploymentInfo(@PathVariable String flowId) {
        try {
            DeploymentInfoDTO deploymentInfo = deploymentService.getDeploymentInfo(flowId);
            if (deploymentInfo != null) {
                return ResponseEntity.ok(deploymentInfo);
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", 
                e.getMessage() != null ? e.getMessage() : "Failed to get deployment info"));
        }
    }
}