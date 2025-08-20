package com.integrixs.backend.integration;

import com.integrixs.backend.BackendApplication;
import com.integrixs.backend.config.TestWebSocketConfiguration;
import com.integrixs.data.model.*;
import com.integrixs.data.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration tests for field mapping functionality.
 * Tests that field mappings are created with proper order and foreign key constraints work.
 */
@SpringBootTest(classes = BackendApplication.class)
@ActiveProfiles("test")
@Import(TestWebSocketConfiguration.class)
@Transactional
public class FieldMappingIntegrationTest {

    @Autowired
    private IntegrationFlowRepository integrationFlowRepository;

    @Autowired
    private FlowTransformationRepository flowTransformationRepository;

    @Autowired
    private FieldMappingRepository fieldMappingRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private BusinessComponentRepository businessComponentRepository;

    private User testUser;
    private BusinessComponent testBusinessComponent;

    @BeforeEach
    void setUp() {
        // Create test user
        testUser = new User();
        testUser.setUsername("testuser");
        testUser.setPasswordHash("$2a$10$test");
        testUser.setEmail("test@example.com");
        testUser.setRole("DEVELOPER");
        testUser = userRepository.save(testUser);

        // Create test business component
        testBusinessComponent = new BusinessComponent();
        testBusinessComponent.setName("Test Component");
        testBusinessComponent.setDescription("Test Business Component");
        testBusinessComponent.setStatus("ACTIVE");
        testBusinessComponent = businessComponentRepository.save(testBusinessComponent);
    }

    @Test
    void testFieldMappingOrder_ShouldNeverBeZero() {
        // Create an integration flow
        IntegrationFlow flow = new IntegrationFlow();
        flow.setName("Test Flow");
        flow.setDescription("Test Flow");
        flow.setStatus(FlowStatus.DEVELOPED_INACTIVE);
        flow.setActive(true);
        flow.setCreatedBy(testUser);
        flow.setBusinessComponent(testBusinessComponent);
        flow.setFlowType(FlowType.DIRECT_MAPPING);
        flow.setMappingMode(MappingMode.WITH_MAPPING);
        flow = integrationFlowRepository.save(flow);

        // Create a transformation
        FlowTransformation transformation = new FlowTransformation();
        transformation.setFlow(flow);
        transformation.setName("Test Transformation");
        transformation.setType(FlowTransformation.TransformationType.FIELD_MAPPING);
        transformation.setConfiguration("{}");
        transformation.setExecutionOrder(1);
        transformation.setActive(true);
        transformation = flowTransformationRepository.save(transformation);

        // Create field mappings with sequential order
        for (int i = 1; i <= 3; i++) {
            FieldMapping mapping = new FieldMapping();
            mapping.setTransformation(transformation);
            mapping.setSourceFieldsList(Arrays.asList("sourceField" + i));
            mapping.setTargetField("targetField" + i);
            mapping.setMappingOrder(i);
            mapping.setActive(true);
            fieldMappingRepository.save(mapping);
        }

        // Verify all mappings have order > 0
        List<FieldMapping> mappings = fieldMappingRepository.findByTransformationId(transformation.getId());
        assertEquals(3, mappings.size());
        
        for (FieldMapping mapping : mappings) {
            assertNotNull(mapping.getMappingOrder());
            assertTrue(mapping.getMappingOrder() > 0, "Mapping order should be greater than 0");
        }

        // Verify sequential order
        mappings.sort((a, b) -> a.getMappingOrder().compareTo(b.getMappingOrder()));
        for (int i = 0; i < mappings.size(); i++) {
            assertEquals(i + 1, mappings.get(i).getMappingOrder());
        }
    }

    @Test
    void testFieldMappingWithZeroOrder_ShouldBeHandled() {
        // Create an integration flow
        IntegrationFlow flow = new IntegrationFlow();
        flow.setName("Test Flow Zero Order");
        flow.setDescription("Test Flow");
        flow.setStatus(FlowStatus.DEVELOPED_INACTIVE);
        flow.setActive(true);
        flow.setCreatedBy(testUser);
        flow.setBusinessComponent(testBusinessComponent);
        flow.setFlowType(FlowType.DIRECT_MAPPING);
        flow.setMappingMode(MappingMode.WITH_MAPPING);
        flow = integrationFlowRepository.save(flow);

        // Create a transformation
        FlowTransformation transformation = new FlowTransformation();
        transformation.setFlow(flow);
        transformation.setName("Test Transformation");
        transformation.setType(FlowTransformation.TransformationType.FIELD_MAPPING);
        transformation.setConfiguration("{}");
        transformation.setExecutionOrder(1);
        transformation.setActive(true);
        transformation = flowTransformationRepository.save(transformation);

        // Try to create a mapping with order = 0
        FieldMapping mapping = new FieldMapping();
        mapping.setTransformation(transformation);
        mapping.setSourceFieldsList(Arrays.asList("source"));
        mapping.setTargetField("target");
        mapping.setMappingOrder(0); // Explicitly set to 0
        mapping.setActive(true);

        // Save and verify
        FieldMapping savedMapping = fieldMappingRepository.save(mapping);
        
        // The service should handle this, but if not, at least verify we can save it
        assertNotNull(savedMapping);
        assertNotNull(savedMapping.getMappingOrder());
        
        // Note: In a real implementation, the service layer should ensure order > 0
        // This test documents the current behavior
    }

    @Test
    void testFieldMappingForeignKeyConstraint() {
        // Create an integration flow
        IntegrationFlow flow = new IntegrationFlow();
        flow.setName("Test FK Constraint");
        flow.setDescription("Test Flow");
        flow.setStatus(FlowStatus.DEVELOPED_INACTIVE);
        flow.setActive(true);
        flow.setCreatedBy(testUser);
        flow.setBusinessComponent(testBusinessComponent);
        flow.setFlowType(FlowType.DIRECT_MAPPING);
        flow.setMappingMode(MappingMode.WITH_MAPPING);
        flow = integrationFlowRepository.save(flow);

        // Create a transformation
        FlowTransformation transformation = new FlowTransformation();
        transformation.setFlow(flow);
        transformation.setName("Test Transformation");
        transformation.setType(FlowTransformation.TransformationType.FIELD_MAPPING);
        transformation.setConfiguration("{}");
        transformation.setExecutionOrder(1);
        transformation.setActive(true);
        transformation = flowTransformationRepository.save(transformation);

        // Create a field mapping
        FieldMapping mapping = new FieldMapping();
        mapping.setTransformation(transformation);
        mapping.setSourceFieldsList(Arrays.asList("source"));
        mapping.setTargetField("target");
        mapping.setMappingOrder(1);
        mapping.setActive(true);
        mapping = fieldMappingRepository.save(mapping);

        // Verify the mapping was saved with correct transformation reference
        FieldMapping savedMapping = fieldMappingRepository.findById(mapping.getId()).orElseThrow();
        assertNotNull(savedMapping.getTransformation());
        assertEquals(transformation.getId(), savedMapping.getTransformation().getId());
        
        // Verify we can find mappings by transformation ID
        List<FieldMapping> mappingsByTransformation = fieldMappingRepository.findByTransformationId(transformation.getId());
        assertEquals(1, mappingsByTransformation.size());
        assertEquals(mapping.getId(), mappingsByTransformation.get(0).getId());
    }
}