package com.integrationlab.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.integrationlab.model.SystemLog;
import com.integrationlab.model.User;
import com.integrationlab.model.UserSession;
import com.integrationlab.monitoring.service.SystemLogService;
import com.integrationlab.repository.UserRepository;
import com.integrationlab.repository.UserSessionRepository;
import com.integrationlab.security.JwtUtil;
import com.integrationlab.service.UserService;
import com.integrationlab.shared.dto.user.RegisterResponseDTO;
import com.integrationlab.shared.dto.user.UserRegisterResponseDTO;
import com.integrationlab.shared.dto.user.UserDTO;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserSessionRepository userSessionRepository;

    @Autowired
    private BCryptPasswordEncoder passwordEncoder;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private UserService userService;

    @Autowired
    private SystemLogService systemLogService;

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody Map<String, String> body, @RequestHeader(value = "User-Agent", required = false) String userAgent, @RequestHeader(value = "X-Forwarded-For", required = false) String ipAddress) {
        String username = body.get("username");
        String password = body.get("password");

        User user = userRepository.findByUsername(username);
        if (user != null && passwordEncoder.matches(password, user.getPasswordHash())) {
            String token = jwtUtil.generateToken(username);
            String refreshToken = jwtUtil.generateRefreshToken(username);
            long expiresIn = jwtUtil.getExpirationMillis() / 1000;

            UserSession session = new UserSession();
            session.setUser(user);
            session.setRefreshToken(refreshToken);
            session.setExpiresAt(LocalDateTime.now().plusDays(7));
            session.setCreatedAt(LocalDateTime.now());
            session.setLastUsedAt(LocalDateTime.now());
            session.setIpAddress(ipAddress);
            session.setUserAgent(userAgent);
            userSessionRepository.save(session);

            SystemLog log = new SystemLog();
            log.setTimestamp(LocalDateTime.now());
            log.setLevel("INFO");
            log.setMessage("User logged in successfully: " + username);
            log.setUserId(user.getId());
            log.setSource("AuthController");
            log.setComponent("Login");
            log.setCreatedAt(LocalDateTime.now());
            systemLogService.log(log);

            return ResponseEntity.ok(Map.of(
                    "token", token,
                    "refreshToken", refreshToken,
                    "expiresIn", expiresIn,
                    "user", userService.mapToDTO(user)
            ));
        } else {
            try {
                String payloadJson = new ObjectMapper().writeValueAsString(body);
                systemLogService.logUserManagementError(
                        "Login",
                        "Failed login attempt for username: " + username,
                        payloadJson,
                        null,
                        "AuthController"
                );
            } catch (Exception innerLogError) {
                System.err.println("Failed to log login error: " + innerLogError.getMessage());
            }

            return ResponseEntity.status(401).body(Map.of("message", "Invalid credentials"));
        }
    }

    @PostMapping("/register")
    public ResponseEntity<RegisterResponseDTO> register(@RequestBody UserRegisterResponseDTO request) {
        try {
            if (userRepository.findByUsername(request.getUsername()) != null) {
                return ResponseEntity.badRequest().body(new RegisterResponseDTO("Username already exists", null));
            }

            User user = userService.createUser(request);

            SystemLog log = new SystemLog();
            log.setTimestamp(LocalDateTime.now());
            log.setLevel("INFO");
            log.setMessage("New user registered: " + user.getUsername());
            log.setUserId(user.getId());
            log.setSource("AuthController");
            log.setComponent("Register");
            log.setCreatedAt(LocalDateTime.now());
            systemLogService.log(log);

            return ResponseEntity.ok(new RegisterResponseDTO("User registered successfully", user.getId()));
        } catch (Exception ex) {
            try {
                String payloadJson = new ObjectMapper().writeValueAsString(request);
                systemLogService.logUserManagementError(
                        "Register",
                        "Registration failed: " + ex.getMessage(),
                        payloadJson,
                        null,
                        "AuthController"
                );
            } catch (Exception innerLogError) {
                System.err.println("Failed to log registration error: " + innerLogError.getMessage());
            }
            return ResponseEntity.internalServerError().body(new RegisterResponseDTO("Error creating user", null));
        }
    }

    @PostMapping("/refresh")
    public ResponseEntity<?> refresh(@RequestBody Map<String, String> body) {
        String refreshToken = body.get("refreshToken");
        if (refreshToken == null || !jwtUtil.validateToken(refreshToken)) {
            return ResponseEntity.status(401).body(Map.of("message", "Invalid refresh token"));
        }

        try {
            UserSession session = userService.refreshUserSession(refreshToken);
            String username = session.getUser().getUsername();
            String token = jwtUtil.generateToken(username);
            long expiresIn = jwtUtil.getExpirationMillis() / 1000;

            SystemLog log = new SystemLog();
            log.setTimestamp(LocalDateTime.now());
            log.setLevel("INFO");
            log.setMessage("Token refreshed for user: " + username);
            log.setUserId(session.getUser().getId());
            log.setSource("AuthController");
            log.setComponent("Refresh");
            log.setCreatedAt(LocalDateTime.now());
            systemLogService.log(log);

            return ResponseEntity.ok(Map.of("token", token, "expiresIn", expiresIn));
        } catch (RuntimeException ex) {
            try {
                String payloadJson = new ObjectMapper().writeValueAsString(body);
                systemLogService.logUserManagementError(
                        "TokenRefresh",
                        "Failed token refresh: " + ex.getMessage(),
                        payloadJson,
                        null,
                        "AuthController"
                );
            } catch (Exception innerLogError) {
                System.err.println("Failed to log refresh error: " + innerLogError.getMessage());
            }

            return ResponseEntity.status(401).body(Map.of("message", ex.getMessage()));
        }
    }

    @GetMapping("/profile")
    public ResponseEntity<?> profile(@RequestHeader("Authorization") String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return ResponseEntity.status(401).body(Map.of("message", "Missing token"));
        }

        String token = authHeader.substring(7);
        if (!jwtUtil.validateToken(token)) {
            return ResponseEntity.status(401).body(Map.of("message", "Invalid token"));
        }

        String username = jwtUtil.extractUsername(token);
        User user = userRepository.findByUsername(username);
        if (user == null) {
            return ResponseEntity.status(404).body(Map.of("message", "User not found"));
        }

        UserDTO userDTO = userService.mapToDTO(user);
        return ResponseEntity.ok(userDTO);
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logout(@RequestBody Map<String, String> body) {
        String refreshToken = body.get("refreshToken");
        if (refreshToken == null || refreshToken.isEmpty()) {
            return ResponseEntity.badRequest().body("Missing refresh token");
        }

        Optional<UserSession> sessionOpt = userSessionRepository.findByRefreshToken(refreshToken);
        if (sessionOpt.isPresent()) {
            UserSession session = sessionOpt.get();
            userSessionRepository.delete(session);

            SystemLog log = new SystemLog();
            log.setTimestamp(LocalDateTime.now());
            log.setLevel("INFO");
            log.setMessage("User logged out");
            log.setUserId(session.getUser().getId());
            log.setSource("AuthController");
            log.setComponent("Logout");
            log.setCreatedAt(LocalDateTime.now());
            systemLogService.log(log);

            return ResponseEntity.ok("Logged out successfully");
        } else {
            try {
                String payloadJson = new ObjectMapper().writeValueAsString(body);
                systemLogService.logUserManagementError(
                        "Logout",
                        "Session not found during logout",
                        payloadJson,
                        null,
                        "AuthController"
                );
            } catch (Exception innerLogError) {
                System.err.println("Failed to log logout error: " + innerLogError.getMessage());
            }
            return ResponseEntity.status(404).body("Session not found");
        }
    }
}
