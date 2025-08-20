package com.integrixs.data.listener;

import com.integrixs.data.model.User;
import com.integrixs.data.repository.UserRepository;
import com.integrixs.shared.context.UserContext;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * JPA entity listener for automatically populating audit fields.
 * 
 * @author Integration Team
 * @since 1.0.0
 */
@Component
public class AuditEntityListener {
    
    private static UserRepository userRepository;
    
    @Autowired
    public void setUserRepository(UserRepository userRepository) {
        AuditEntityListener.userRepository = userRepository;
    }
    
    @PrePersist
    public void setCreatedFields(Object entity) {
        try {
            // Set created timestamp
            setFieldValue(entity, "createdAt", LocalDateTime.now());
            
            // Set created by user
            User currentUser = getCurrentUser();
            if (currentUser != null) {
                setFieldValue(entity, "createdBy", currentUser);
            }
            
            // Also set updated fields on create
            setFieldValue(entity, "updatedAt", LocalDateTime.now());
            if (currentUser != null) {
                setFieldValue(entity, "updatedBy", currentUser);
            }
        } catch (Exception e) {
            // Log but don't fail the operation
            e.printStackTrace();
        }
    }
    
    @PreUpdate
    public void setUpdatedFields(Object entity) {
        try {
            // Only update the updated fields
            setFieldValue(entity, "updatedAt", LocalDateTime.now());
            
            User currentUser = getCurrentUser();
            if (currentUser != null) {
                setFieldValue(entity, "updatedBy", currentUser);
            }
        } catch (Exception e) {
            // Log but don't fail the operation
            e.printStackTrace();
        }
    }
    
    /**
     * Get the current authenticated user from the UserContext
     */
    private User getCurrentUser() {
        try {
            UUID userId = UserContext.getCurrentUserId();
            if (userId != null && userRepository != null) {
                return userRepository.findById(userId).orElse(null);
            }
        } catch (Exception e) {
            // Log but don't fail
            e.printStackTrace();
        }
        return null;
    }
    
    /**
     * Set field value using reflection
     */
    private void setFieldValue(Object entity, String fieldName, Object value) {
        try {
            // Try setter method first
            String setterName = "set" + Character.toUpperCase(fieldName.charAt(0)) + fieldName.substring(1);
            Method setter = entity.getClass().getMethod(setterName, value.getClass());
            setter.invoke(entity, value);
        } catch (NoSuchMethodException e) {
            // Try direct field access if setter not found
            try {
                java.lang.reflect.Field field = entity.getClass().getDeclaredField(fieldName);
                field.setAccessible(true);
                field.set(entity, value);
            } catch (Exception ex) {
                // Field doesn't exist, ignore
            }
        } catch (Exception e) {
            // Ignore other exceptions
        }
    }
}