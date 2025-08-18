package com.integrixs.backend.controller;

import com.integrixs.data.model.User;
import com.integrixs.data.repository.UserRepository;
import com.integrixs.backend.service.UserService;
import com.integrixs.shared.dto.transformation.*;
import com.integrixs.shared.dto.user.PagedUserResponseDTO;
import com.integrixs.shared.dto.user.RegisterResponseDTO;
import com.integrixs.shared.dto.user.UserRegisterResponseDTO;
import com.integrixs.shared.dto.user.UpdateUserRequestDTO;
import com.integrixs.shared.dto.user.UserDTO;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/users")
public class UserController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserService userService;

    @PostMapping("/register")
    public ResponseEntity<RegisterResponseDTO> register(@RequestBody UserRegisterResponseDTO request) {
        if (userRepository.findByUsername(request.getUsername()) != null) {
            return ResponseEntity
                .badRequest()
                .body(new RegisterResponseDTO("Username already exists", null));
        }

        User user = userService.createUser(request);
        return ResponseEntity.ok(new RegisterResponseDTO("User registered successfully", user.getId().toString()));
    }

    @GetMapping
    public ResponseEntity<PagedUserResponseDTO> getUsers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int limit
    ) {
        Page<User> usersPage = userRepository.findAll(PageRequest.of(page, limit));

        PagedUserResponseDTO response = new PagedUserResponseDTO();
        response.setUsers(
            usersPage.getContent()
                .stream()
                .map(userService::mapToDTO)
                .collect(Collectors.toList())
        );
        response.setPage(page);
        response.setTotalPages(usersPage.getTotalPages());
        response.setTotalElements(usersPage.getTotalElements());

        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<UserDTO> getUser(@PathVariable String id) {
        Optional<User> userOpt = userRepository.findById(UUID.fromString(id));
        return userOpt
                .map(user -> ResponseEntity.ok(userService.mapToDTO(user)))
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PutMapping("/{id}")
    public ResponseEntity<UserDTO> updateUser(
            @PathVariable String id,
            @RequestBody UpdateUserRequestDTO request
    ) {
        Optional<User> userOpt = userRepository.findById(UUID.fromString(id));
        if (userOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        User updated = userService.updateUser(userOpt.get(), request);
        return ResponseEntity.ok(userService.mapToDTO(updated));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteUser(@PathVariable String id) {
        if (!userRepository.existsById(UUID.fromString(id))) {
            return ResponseEntity.notFound().build();
        }
        userRepository.deleteById(UUID.fromString(id));
        return ResponseEntity.ok().build();
    }
}
