package com.integrationlab.backend.test;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

/**
 * Base class for integration tests.
 * 
 * <p>Provides common configuration for integration testing including
 * database setup with Testcontainers and MockMvc configuration.
 * 
 * @author Integration Team
 * @since 1.0.0
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@ActiveProfiles({"test", "integration-test"})
@Testcontainers
@Transactional
public abstract class BaseIntegrationTest {
    
    @Container
    protected static MySQLContainer<?> mysqlContainer = new MySQLContainer<>("mysql:8.0")
            .withDatabaseName("testdb")
            .withUsername("test")
            .withPassword("test")
            .withReuse(true);
    
    @Autowired
    protected MockMvc mockMvc;
    
    @Autowired
    protected ObjectMapper objectMapper;
    
    @DynamicPropertySource
    static void properties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", mysqlContainer::getJdbcUrl);
        registry.add("spring.datasource.username", mysqlContainer::getUsername);
        registry.add("spring.datasource.password", mysqlContainer::getPassword);
    }
    
    @BeforeEach
    protected void setUp() {
        // Common setup for all integration tests
    }
    
    /**
     * Helper method to convert object to JSON string.
     */
    protected String toJson(Object obj) throws Exception {
        return objectMapper.writeValueAsString(obj);
    }
    
    /**
     * Helper method to parse JSON to object.
     */
    protected <T> T fromJson(String json, Class<T> clazz) throws Exception {
        return objectMapper.readValue(json, clazz);
    }
}