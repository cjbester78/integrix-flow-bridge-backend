package com.integrationlab.backend.test;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.context.ActiveProfiles;

/**
 * Base class for unit tests.
 * 
 * <p>Provides common configuration and utilities for unit testing
 * with Mockito and JUnit 5.
 * 
 * @author Integration Team
 * @since 1.0.0
 */
@ExtendWith(MockitoExtension.class)
@ActiveProfiles("test")
public abstract class BaseUnitTest {
    
    /**
     * Setup method run before each test.
     * Can be overridden by subclasses.
     */
    @BeforeEach
    protected void setUp() {
        // Common setup can be added here
    }
    
    /**
     * Helper method to create test data.
     * Subclasses can override to provide specific test data.
     */
    protected void initializeTestData() {
        // To be implemented by subclasses
    }
}