package com.integrixs.shared.context;

import java.util.UUID;

/**
 * Thread-local storage for current user context.
 * This allows audit information to be passed to the data-access layer
 * without direct dependency on Spring Security.
 * 
 * @author Integration Team
 * @since 1.0.0
 */
public class UserContext {
    
    private static final ThreadLocal<UserInfo> userHolder = new ThreadLocal<>();
    
    /**
     * Sets the current user context
     */
    public static void setCurrentUser(UUID userId, String username) {
        userHolder.set(new UserInfo(userId, username));
    }
    
    /**
     * Gets the current user ID
     */
    public static UUID getCurrentUserId() {
        UserInfo info = userHolder.get();
        return info != null ? info.userId : null;
    }
    
    /**
     * Gets the current username
     */
    public static String getCurrentUsername() {
        UserInfo info = userHolder.get();
        return info != null ? info.username : null;
    }
    
    /**
     * Clears the current user context
     */
    public static void clear() {
        userHolder.remove();
    }
    
    /**
     * Internal class to hold user information
     */
    private static class UserInfo {
        private final UUID userId;
        private final String username;
        
        UserInfo(UUID userId, String username) {
            this.userId = userId;
            this.username = username;
        }
    }
}