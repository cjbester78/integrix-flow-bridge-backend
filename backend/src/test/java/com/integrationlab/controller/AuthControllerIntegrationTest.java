package com.integrationlab.controller;

import com.integrationlab.backend.test.BaseIntegrationTest;
import com.integrationlab.backend.test.TestDataBuilder;
import com.integrationlab.model.User;
import com.integrationlab.model.UserSession;
import com.integrationlab.repository.UserRepository;
import com.integrationlab.repository.UserSessionRepository;
import com.integrationlab.security.JwtUtil;
import com.integrationlab.shared.dto.user.UserRegisterResponseDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.test.web.servlet.MvcResult;

import java.time.LocalDateTime;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Integration tests for AuthController.
 * 
 * @author Integration Team
 * @since 1.0.0
 */
class AuthControllerIntegrationTest extends BaseIntegrationTest {
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private UserSessionRepository userSessionRepository;
    
    @Autowired
    private BCryptPasswordEncoder passwordEncoder;
    
    @Autowired
    private JwtUtil jwtUtil;
    
    private User testUser;
    
    @BeforeEach
    protected void setUp() {
        super.setUp();
        
        // Clean up database
        userSessionRepository.deleteAll();
        userRepository.deleteAll();
        
        // Create test user
        testUser = new User();
        testUser.setUsername("testuser");
        testUser.setEmail("test@example.com");
        testUser.setFirstName("Test");
        testUser.setLastName("User");
        testUser.setPasswordHash(passwordEncoder.encode("password123"));
        testUser.setRole("viewer");
        testUser.setStatus("active");
        testUser.setEmailVerified(true);
        testUser.setPermissions("{}");
        testUser.setCreatedAt(LocalDateTime.now());
        testUser.setUpdatedAt(LocalDateTime.now());
        testUser = userRepository.save(testUser);
    }
    
    @Test
    void testLogin_Success() throws Exception {
        // Given
        Map<String, String> loginRequest = Map.of(
            "username", "testuser",
            "password", "password123"
        );
        
        // When & Then
        MvcResult result = mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(toJson(loginRequest))
                .header("User-Agent", "Test Agent")
                .header("X-Forwarded-For", "127.0.0.1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").exists())
                .andExpect(jsonPath("$.refreshToken").exists())
                .andExpect(jsonPath("$.expiresIn").isNumber())
                .andExpect(jsonPath("$.user.username").value("testuser"))
                .andExpect(jsonPath("$.user.email").value("test@example.com"))
                .andExpect(jsonPath("$.user.role").value("viewer"))
                .andReturn();
        
        // Verify session was created
        assertThat(userSessionRepository.count()).isEqualTo(1);
        UserSession session = userSessionRepository.findAll().get(0);
        assertThat(session.getUser().getId()).isEqualTo(testUser.getId());
        assertThat(session.getIpAddress()).isEqualTo("127.0.0.1");
        assertThat(session.getUserAgent()).isEqualTo("Test Agent");
    }
    
    @Test
    void testLogin_InvalidPassword() throws Exception {
        // Given
        Map<String, String> loginRequest = Map.of(
            "username", "testuser",
            "password", "wrongpassword"
        );
        
        // When & Then
        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(toJson(loginRequest)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value("Invalid credentials"));
        
        // Verify no session was created
        assertThat(userSessionRepository.count()).isEqualTo(0);
    }
    
    @Test
    void testLogin_UserNotFound() throws Exception {
        // Given
        Map<String, String> loginRequest = Map.of(
            "username", "nonexistent",
            "password", "password123"
        );
        
        // When & Then
        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(toJson(loginRequest)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value("Invalid credentials"));
    }
    
    @Test
    void testRegister_Success() throws Exception {
        // Given
        UserRegisterResponseDTO registerRequest = new UserRegisterResponseDTO();
        registerRequest.setUsername("newuser");
        registerRequest.setEmail("newuser@example.com");
        registerRequest.setFirstName("New");
        registerRequest.setLastName("User");
        registerRequest.setPassword("newpassword123");
        registerRequest.setRole("viewer");
        registerRequest.setStatus("active");
        
        // When & Then
        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(toJson(registerRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("User registered successfully"))
                .andExpect(jsonPath("$.userId").exists());
        
        // Verify user was created
        User newUser = userRepository.findByUsername("newuser");
        assertThat(newUser).isNotNull();
        assertThat(newUser.getEmail()).isEqualTo("newuser@example.com");
        assertThat(newUser.getFirstName()).isEqualTo("New");
        assertThat(newUser.getLastName()).isEqualTo("User");
        assertThat(newUser.getRole()).isEqualTo("viewer");
        assertThat(passwordEncoder.matches("newpassword123", newUser.getPasswordHash())).isTrue();
        assertThat(newUser.isEmailVerified()).isFalse();
    }
    
    @Test
    void testRegister_UsernameAlreadyExists() throws Exception {
        // Given
        UserRegisterResponseDTO registerRequest = new UserRegisterResponseDTO();
        registerRequest.setUsername("testuser"); // Already exists
        registerRequest.setEmail("another@example.com");
        registerRequest.setFirstName("Another");
        registerRequest.setLastName("User");
        registerRequest.setPassword("password123");
        registerRequest.setRole("viewer");
        registerRequest.setStatus("active");
        
        // When & Then
        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(toJson(registerRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Username already exists"))
                .andExpect(jsonPath("$.userId").doesNotExist());
        
        // Verify no new user was created
        assertThat(userRepository.count()).isEqualTo(1);
    }
    
    @Test
    void testRefresh_Success() throws Exception {
        // Given - Create a session
        String refreshToken = jwtUtil.generateRefreshToken(testUser.getUsername());
        UserSession session = new UserSession();
        session.setUser(testUser);
        session.setRefreshToken(refreshToken);
        session.setExpiresAt(LocalDateTime.now().plusDays(7));
        session.setCreatedAt(LocalDateTime.now());
        session.setLastUsedAt(LocalDateTime.now());
        userSessionRepository.save(session);
        
        Map<String, String> refreshRequest = Map.of(
            "refreshToken", refreshToken
        );
        
        // When & Then
        mockMvc.perform(post("/api/auth/refresh")
                .contentType(MediaType.APPLICATION_JSON)
                .content(toJson(refreshRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").exists())
                .andExpect(jsonPath("$.expiresIn").isNumber())
                .andExpect(jsonPath("$.user.username").value("testuser"));
        
        // Verify session was updated
        UserSession updatedSession = userSessionRepository.findById(session.getId()).orElseThrow();
        assertThat(updatedSession.getLastUsedAt()).isAfter(session.getLastUsedAt());
    }
    
    @Test
    void testRefresh_InvalidToken() throws Exception {
        // Given
        Map<String, String> refreshRequest = Map.of(
            "refreshToken", "invalid-token"
        );
        
        // When & Then
        mockMvc.perform(post("/api/auth/refresh")
                .contentType(MediaType.APPLICATION_JSON)
                .content(toJson(refreshRequest)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value("Invalid refresh token"));
    }
    
    @Test
    void testRefresh_ExpiredSession() throws Exception {
        // Given - Create an expired session
        String refreshToken = jwtUtil.generateRefreshToken(testUser.getUsername());
        UserSession expiredSession = new UserSession();
        expiredSession.setUser(testUser);
        expiredSession.setRefreshToken(refreshToken);
        expiredSession.setExpiresAt(LocalDateTime.now().minusDays(1)); // Expired
        expiredSession.setCreatedAt(LocalDateTime.now().minusDays(8));
        expiredSession.setLastUsedAt(LocalDateTime.now().minusDays(1));
        userSessionRepository.save(expiredSession);
        
        Map<String, String> refreshRequest = Map.of(
            "refreshToken", refreshToken
        );
        
        // When & Then
        mockMvc.perform(post("/api/auth/refresh")
                .contentType(MediaType.APPLICATION_JSON)
                .content(toJson(refreshRequest)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value("Session expired"));
        
        // Verify session was deleted
        assertThat(userSessionRepository.findById(expiredSession.getId())).isEmpty();
    }
    
    @Test
    void testLogout_Success() throws Exception {
        // Given - Create a session
        String refreshToken = jwtUtil.generateRefreshToken(testUser.getUsername());
        UserSession session = new UserSession();
        session.setUser(testUser);
        session.setRefreshToken(refreshToken);
        session.setExpiresAt(LocalDateTime.now().plusDays(7));
        session.setCreatedAt(LocalDateTime.now());
        session.setLastUsedAt(LocalDateTime.now());
        userSessionRepository.save(session);
        
        Map<String, String> logoutRequest = Map.of(
            "refreshToken", refreshToken
        );
        
        // When & Then
        mockMvc.perform(post("/api/auth/logout")
                .contentType(MediaType.APPLICATION_JSON)
                .content(toJson(logoutRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Logged out successfully"));
        
        // Verify session was deleted
        assertThat(userSessionRepository.findById(session.getId())).isEmpty();
    }
}