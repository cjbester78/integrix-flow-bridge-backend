package com.integrixs.backend.service;

import com.integrixs.data.model.Role;
import com.integrixs.data.repository.RoleRepository;
import com.integrixs.backend.exception.ResourceNotFoundException;
import com.integrixs.shared.dto.RoleDTO;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class RoleService {
    
    private static final Logger logger = LoggerFactory.getLogger(RoleService.class);
    
    private final RoleRepository roleRepository;
    
    /**
     * Get all roles with pagination
     */
    public List<RoleDTO> getAllRoles(int page, int limit) {
        PageRequest pageRequest = PageRequest.of(page, limit);
        Page<Role> rolePage = roleRepository.findAll(pageRequest);
        
        return rolePage.getContent().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }
    
    /**
     * Get role by ID
     */
    public RoleDTO getRoleById(String id) {
        Role role = roleRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Role not found with id: " + id));
        return convertToDTO(role);
    }
    
    /**
     * Convert Role entity to DTO
     */
    private RoleDTO convertToDTO(Role role) {
        return RoleDTO.builder()
                .id(role.getId())
                .name(role.getName())
                .description(role.getPermissions()) // Use permissions as description for now
                .build();
    }
}