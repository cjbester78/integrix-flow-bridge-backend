package com.integrationlab.service;

import com.integrationlab.backend.test.BaseUnitTest;
import com.integrationlab.backend.test.TestDataBuilder;
import com.integrationlab.model.User;
import com.integrationlab.model.UserSession;
import com.integrationlab.repository.UserRepository;
import com.integrationlab.repository.UserSessionRepository;
import com.integrationlab.shared.dto.user.UpdateUserRequestDTO;
import com.integrationlab.shared.dto.user.UserDTO;
import com.integrationlab.shared.dto.user.UserRegisterResponseDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

/**
 * Unit tests for UserService.
 * 
 * @author Integration Team
 * @since 1.0.0
 */
class UserServiceTest extends BaseUnitTest {
    
    @Mock
    private UserRepository userRepository;
    
    @Mock
    private UserSessionRepository userSessionRepository;
    
    @Mock
    private BCryptPasswordEncoder passwordEncoder;
    
    @InjectMocks
    private UserService userService;
    
    private User testUser;
    private UserRegisterResponseDTO registerRequest;
    
    @BeforeEach
    void setUp() {
        super.setUp();
        
        testUser = new User();
        testUser.setId("user-123");
        testUser.setUsername("testuser");
        testUser.setEmail("test@example.com");
        testUser.setFirstName("Test");
        testUser.setLastName("User");
        testUser.setPasswordHash("hashed-password");
        testUser.setRole("viewer");
        testUser.setStatus("active");
        testUser.setEmailVerified(true);
        testUser.setPermissions("{}");
        testUser.setCreatedAt(LocalDateTime.now());
        testUser.setUpdatedAt(LocalDateTime.now());
        
        registerRequest = new UserRegisterResponseDTO();
        registerRequest.setUsername("newuser");
        registerRequest.setEmail("new@example.com");
        registerRequest.setFirstName("New");
        registerRequest.setLastName("User");
        registerRequest.setPassword("password123");
        registerRequest.setRole("viewer");
        registerRequest.setStatus("active");
    }
    
    @Test
    void testRefreshUserSession_Success() {
        // Given
        String refreshToken = "valid-refresh-token";
        UserSession session = new UserSession();
        session.setId("session-123");
        session.setUserId("user-123");
        session.setRefreshToken(refreshToken);
        session.setExpiresAt(LocalDateTime.now().plusDays(7));
        session.setCreatedAt(LocalDateTime.now());
        session.setLastUsedAt(LocalDateTime.now().minusMinutes(30));
        
        when(userSessionRepository.findByRefreshToken(refreshToken))
                .thenReturn(Optional.of(session));
        
        // When
        UserSession result = userService.refreshUserSession(refreshToken);
        
        // Then
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo("session-123");
        assertThat(result.getLastUsedAt()).isAfter(session.getLastUsedAt());
    }
    
    @Test
    void testRefreshUserSession_NotFound() {
        // Given
        String refreshToken = "invalid-token";
        when(userSessionRepository.findByRefreshToken(refreshToken))
                .thenReturn(Optional.empty());
        
        // When/Then
        assertThatThrownBy(() -> userService.refreshUserSession(refreshToken))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Session not found");
    }
    
    @Test
    void testRefreshUserSession_Expired() {
        // Given
        String refreshToken = "expired-token";
        UserSession expiredSession = new UserSession();
        expiredSession.setId("session-123");
        expiredSession.setRefreshToken(refreshToken);
        expiredSession.setExpiresAt(LocalDateTime.now().minusDays(1)); // Expired
        
        when(userSessionRepository.findByRefreshToken(refreshToken))
                .thenReturn(Optional.of(expiredSession));
        
        // When/Then
        assertThatThrownBy(() -> userService.refreshUserSession(refreshToken))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Session expired");
        
        verify(userSessionRepository).delete(expiredSession);
    }
    
    @Test
    void testCreateUser() {
        // Given
        when(passwordEncoder.encode("password123")).thenReturn("encoded-password");
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
            User user = invocation.getArgument(0);
            user.setId("new-user-id");
            return user;
        });
        
        // When
        User result = userService.createUser(registerRequest);
        
        // Then
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo("new-user-id");
        assertThat(result.getUsername()).isEqualTo("newuser");
        assertThat(result.getEmail()).isEqualTo("new@example.com");
        assertThat(result.getPasswordHash()).isEqualTo("encoded-password");
        assertThat(result.isEmailVerified()).isFalse();
        assertThat(result.getPermissions()).isEqualTo("{}");
        
        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(userCaptor.capture());
        User savedUser = userCaptor.getValue();
        assertThat(savedUser.getCreatedAt()).isNotNull();
        assertThat(savedUser.getUpdatedAt()).isNotNull();
    }
    
    @Test
    void testMapToDTO() {
        // When
        UserDTO dto = userService.mapToDTO(testUser);
        
        // Then
        assertThat(dto).isNotNull();
        assertThat(dto.getId()).isEqualTo("user-123");
        assertThat(dto.getUsername()).isEqualTo("testuser");
        assertThat(dto.getEmail()).isEqualTo("test@example.com");
        assertThat(dto.getFirstName()).isEqualTo("Test");
        assertThat(dto.getLastName()).isEqualTo("User");
        assertThat(dto.getRole()).isEqualTo("viewer");
        assertThat(dto.getStatus()).isEqualTo("active");
        assertThat(dto.isEmailVerified()).isTrue();
        assertThat(dto.getPermissions()).isNotNull();
        assertThat(dto.getPermissions()).isEmpty();
    }
    
    @Test
    void testMapToDTO_WithInvalidPermissions() {
        // Given
        testUser.setPermissions("invalid-json");
        
        // When
        UserDTO dto = userService.mapToDTO(testUser);
        
        // Then
        assertThat(dto.getPermissions()).isEmpty();
    }
    
    @Test
    void testFindById_Found() {
        // Given
        when(userRepository.findById("user-123")).thenReturn(Optional.of(testUser));
        
        // When
        Optional<UserDTO> result = userService.findById("user-123");
        
        // Then
        assertThat(result).isPresent();
        assertThat(result.get().getId()).isEqualTo("user-123");
        assertThat(result.get().getUsername()).isEqualTo("testuser");
    }
    
    @Test
    void testFindById_NotFound() {
        // Given
        when(userRepository.findById("non-existent")).thenReturn(Optional.empty());
        
        // When
        Optional<UserDTO> result = userService.findById("non-existent");
        
        // Then
        assertThat(result).isEmpty();
    }
    
    @Test
    void testFindAll_WithPagination() {
        // Given
        User user1 = testUser;
        User user2 = new User();
        user2.setId("user-456");
        user2.setUsername("user2");
        user2.setEmail("user2@example.com");
        user2.setPermissions("{}");
        
        User user3 = new User();
        user3.setId("user-789");
        user3.setUsername("user3");
        user3.setEmail("user3@example.com");
        user3.setPermissions("{}");
        
        when(userRepository.findAll()).thenReturn(Arrays.asList(user1, user2, user3));
        
        // When - page 1, limit 2
        List<UserDTO> result = userService.findAll(1, 2);
        
        // Then
        assertThat(result).hasSize(2);
        assertThat(result.get(0).getId()).isEqualTo("user-123");
        assertThat(result.get(1).getId()).isEqualTo("user-456");
        
        // When - page 2, limit 2
        result = userService.findAll(2, 2);
        
        // Then
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getId()).isEqualTo("user-789");
    }
    
    @Test
    void testUpdate_Success() {
        // Given
        UpdateUserRequestDTO updateRequest = new UpdateUserRequestDTO();
        updateRequest.setEmail("updated@example.com");
        updateRequest.setFirstName("Updated");
        updateRequest.setLastName("Name");
        updateRequest.setRole("integrator");
        updateRequest.setStatus("active");
        
        when(userRepository.findById("user-123")).thenReturn(Optional.of(testUser));
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));
        
        // When
        Optional<UserDTO> result = userService.update("user-123", updateRequest);
        
        // Then
        assertThat(result).isPresent();
        assertThat(result.get().getEmail()).isEqualTo("updated@example.com");
        assertThat(result.get().getFirstName()).isEqualTo("Updated");
        assertThat(result.get().getLastName()).isEqualTo("Name");
        assertThat(result.get().getRole()).isEqualTo("integrator");
        
        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(userCaptor.capture());
        User savedUser = userCaptor.getValue();
        assertThat(savedUser.getUpdatedAt()).isAfter(testUser.getUpdatedAt());
    }
    
    @Test
    void testUpdate_NotFound() {
        // Given
        UpdateUserRequestDTO updateRequest = new UpdateUserRequestDTO();
        when(userRepository.findById("non-existent")).thenReturn(Optional.empty());
        
        // When
        Optional<UserDTO> result = userService.update("non-existent", updateRequest);
        
        // Then
        assertThat(result).isEmpty();
        verify(userRepository, never()).save(any());
    }
    
    @Test
    void testDeleteById_Success() {
        // Given
        when(userRepository.existsById("user-123")).thenReturn(true);
        
        // When
        boolean result = userService.deleteById("user-123");
        
        // Then
        assertThat(result).isTrue();
        verify(userRepository).deleteById("user-123");
    }
    
    @Test
    void testDeleteById_NotFound() {
        // Given
        when(userRepository.existsById("non-existent")).thenReturn(false);
        
        // When
        boolean result = userService.deleteById("non-existent");
        
        // Then
        assertThat(result).isFalse();
        verify(userRepository, never()).deleteById(anyString());
    }
    
    @Test
    void testUpdateUser() {
        // Given
        UpdateUserRequestDTO updateRequest = new UpdateUserRequestDTO();
        updateRequest.setEmail("direct-update@example.com");
        updateRequest.setFirstName("Direct");
        updateRequest.setLastName("Update");
        updateRequest.setRole("administrator");
        updateRequest.setStatus("inactive");
        
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));
        
        // When
        User result = userService.updateUser(testUser, updateRequest);
        
        // Then
        assertThat(result).isNotNull();
        assertThat(result.getEmail()).isEqualTo("direct-update@example.com");
        assertThat(result.getFirstName()).isEqualTo("Direct");
        assertThat(result.getLastName()).isEqualTo("Update");
        assertThat(result.getRole()).isEqualTo("administrator");
        assertThat(result.getStatus()).isEqualTo("inactive");
        assertThat(result.getUpdatedAt()).isAfter(testUser.getCreatedAt());
    }
}