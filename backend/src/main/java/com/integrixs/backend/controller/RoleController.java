package com.integrixs.backend.controller;

import com.integrixs.backend.service.RoleService;
import com.integrixs.shared.dto.RoleDTO;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/roles")
@CrossOrigin(origins = "*")
@RequiredArgsConstructor
public class RoleController {
    
    private static final Logger logger = LoggerFactory.getLogger(RoleController.class);
    
    private final RoleService roleService;
    
    /**
     * Get all roles with pagination
     */
    @GetMapping
    public ResponseEntity<List<RoleDTO>> getAllRoles(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "50") int limit) {
        logger.debug("Getting all roles - page: {}, limit: {}", page, limit);
        List<RoleDTO> roles = roleService.getAllRoles(page, limit);
        return ResponseEntity.ok(roles);
    }
    
    /**
     * Get role by ID
     */
    @GetMapping("/{id}")
    public ResponseEntity<RoleDTO> getRoleById(@PathVariable String id) {
        logger.debug("Getting role by id: {}", id);
        RoleDTO role = roleService.getRoleById(id);
        return ResponseEntity.ok(role);
    }
}