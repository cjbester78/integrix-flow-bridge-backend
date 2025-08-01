package com.integrationlab.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.integrationlab.model.User;
import com.integrationlab.model.UserSession;
import com.integrationlab.repository.UserRepository;
import com.integrationlab.repository.UserSessionRepository;
import com.integrationlab.shared.dto.user.UserRegisterResponseDTO;
import com.integrationlab.shared.dto.user.UpdateUserRequestDTO;
import com.integrationlab.shared.dto.user.UserDTO;

import jakarta.transaction.Transactional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private UserSessionRepository userSessionRepository;

    @Autowired
    private BCryptPasswordEncoder passwordEncoder;

    private final ObjectMapper objectMapper = new ObjectMapper();
    
    @Transactional
    public UserSession refreshUserSession(String refreshToken) {
        Optional<UserSession> sessionOpt = userSessionRepository.findByRefreshToken(refreshToken);
        if (sessionOpt.isEmpty()) {
            throw new RuntimeException("Session not found");
        }

        UserSession session = sessionOpt.get();
        if (session.getExpiresAt().isBefore(LocalDateTime.now())) {
            userSessionRepository.delete(session); // cleanup
            throw new RuntimeException("Session expired");
        }

        session.setLastUsedAt(LocalDateTime.now());
        // No need to explicitly call save() — JPA will flush on commit
        return session;
    }


    public User createUser(UserRegisterResponseDTO request) {
        User user = new User();
        user.setUsername(request.getUsername());
        user.setEmail(request.getEmail());
        user.setFirstName(request.getFirstName());
        user.setLastName(request.getLastName());
        user.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        user.setRole(request.getRole());
        user.setStatus(request.getStatus());
        user.setEmailVerified(false);
        user.setPermissions("{}");
        user.setCreatedAt(LocalDateTime.now());
        user.setUpdatedAt(LocalDateTime.now());

        return userRepository.save(user);
    }

    @Cacheable(value = "users", key = "#user.id")
    public UserDTO mapToDTO(User user) {
        UserDTO dto = new UserDTO();
        dto.setId(user.getId());
        dto.setUsername(user.getUsername());
        dto.setEmail(user.getEmail());
        dto.setFirstName(user.getFirstName());
        dto.setLastName(user.getLastName());
        dto.setRole(user.getRole());
        dto.setStatus(user.getStatus());
        dto.setCreatedAt(user.getCreatedAt());
        dto.setUpdatedAt(user.getUpdatedAt());
        dto.setLastLoginAt(user.getLastLoginAt());
        dto.setEmailVerified(user.isEmailVerified());

        try {
            Map<String, Object> permissions = objectMapper.readValue(
                user.getPermissions(), new TypeReference<Map<String, Object>>() {}
            );
            dto.setPermissions(new HashMap<>());
        } catch (Exception e) {
            dto.setPermissions(Collections.emptyMap());
        }

        return dto;
    }

    @Cacheable(value = "users", key = "#id")
    public Optional<UserDTO> findById(String id) {
        return userRepository.findById(id).map(this::mapToDTO);
    }

    public List<UserDTO> findAll(int page, int limit) {
        return userRepository.findAll()
            .stream()
            .skip((long) (page - 1) * limit)
            .limit(limit)
            .map(this::mapToDTO)
            .collect(Collectors.toList());
    }

    @CachePut(value = "users", key = "#id")
    public Optional<UserDTO> update(String id, UpdateUserRequestDTO dto) {
        return userRepository.findById(id).map(user -> {
            user.setEmail(dto.getEmail());
            user.setFirstName(dto.getFirstName());
            user.setLastName(dto.getLastName());
            user.setRole(dto.getRole());
            user.setStatus(dto.getStatus());
            user.setUpdatedAt(LocalDateTime.now());
            return mapToDTO(userRepository.save(user));
        });
    }

    @CacheEvict(value = "users", key = "#id")
    public boolean deleteById(String id) {
        if (userRepository.existsById(id)) {
            userRepository.deleteById(id);
            return true;
        }
        return false;
    }
    
    public User updateUser(User user, UpdateUserRequestDTO dto) {
        user.setEmail(dto.getEmail());
        user.setFirstName(dto.getFirstName());
        user.setLastName(dto.getLastName());
        user.setRole(dto.getRole());
        user.setStatus(dto.getStatus());
        user.setUpdatedAt(LocalDateTime.now());
        return userRepository.save(user);
    }

}
