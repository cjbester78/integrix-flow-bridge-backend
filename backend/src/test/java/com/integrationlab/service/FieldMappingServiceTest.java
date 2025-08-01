package com.integrationlab.service;

import com.integrationlab.backend.test.BaseUnitTest;
import com.integrationlab.model.FieldMapping;
import com.integrationlab.model.FlowTransformation;
import com.integrationlab.repository.FieldMappingRepository;
import com.integrationlab.repository.FlowTransformationRepository;
import com.integrationlab.shared.dto.mapping.FieldMappingDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Unit tests for FieldMappingService.
 * 
 * @author Integration Team
 * @since 1.0.0
 */
class FieldMappingServiceTest extends BaseUnitTest {
    
    @Mock
    private FieldMappingRepository mappingRepository;
    
    @Mock
    private FlowTransformationRepository transformationRepository;
    
    @InjectMocks
    private FieldMappingService fieldMappingService;
    
    private FieldMapping testMapping;
    private FlowTransformation testTransformation;
    private FieldMappingDTO testMappingDTO;
    
    @BeforeEach
    void setUp() {
        super.setUp();
        
        // Initialize test transformation
        testTransformation = new FlowTransformation();
        testTransformation.setId("transform-123");
        testTransformation.setName("Test Transformation");
        
        // Initialize test mapping
        testMapping = new FieldMapping();
        testMapping.setId("mapping-123");
        testMapping.setTransformation(testTransformation);
        testMapping.setSourceFields("[\"field1\", \"field2\"]");
        testMapping.setTargetField("targetField");
        testMapping.setJavaFunction("function concatenate(field1, field2) { return field1 + ' ' + field2; }");
        testMapping.setMappingRule("CONCATENATE");
        testMapping.setInputTypes("[\"string\", \"string\"]");
        testMapping.setOutputType("string");
        testMapping.setDescription("Test field mapping");
        testMapping.setVersion(1);
        testMapping.setFunctionName("concatenate");
        testMapping.setActive(true);
        testMapping.setCreatedAt(LocalDateTime.now());
        testMapping.setUpdatedAt(LocalDateTime.now());
        
        // Initialize test DTO
        testMappingDTO = new FieldMappingDTO();
        testMappingDTO.setId("mapping-123");
        testMappingDTO.setTransformationId("transform-123");
        testMappingDTO.setSourceFields("[\"field1\", \"field2\"]");
        testMappingDTO.setTargetField("targetField");
        testMappingDTO.setJavaFunction("function concatenate(field1, field2) { return field1 + ' ' + field2; }");
        testMappingDTO.setMappingRule("CONCATENATE");
        testMappingDTO.setInputTypes("[\"string\", \"string\"]");
        testMappingDTO.setOutputType("string");
        testMappingDTO.setDescription("Test field mapping");
        testMappingDTO.setVersion(1);
        testMappingDTO.setFunctionName("concatenate");
        testMappingDTO.setActive(true);
    }
    
    @Test
    void testGetByTransformationId() {
        // Given
        FieldMapping mapping2 = new FieldMapping();
        mapping2.setId("mapping-456");
        mapping2.setTransformation(testTransformation);
        mapping2.setSourceFields("[\"field3\"]");
        mapping2.setTargetField("targetField2");
        mapping2.setMappingRule("DIRECT");
        mapping2.setActive(true);
        
        when(mappingRepository.findByTransformationId("transform-123"))
                .thenReturn(Arrays.asList(testMapping, mapping2));
        
        // When
        List<FieldMappingDTO> result = fieldMappingService.getByTransformationId("transform-123");
        
        // Then
        assertThat(result).hasSize(2);
        assertThat(result.get(0).getId()).isEqualTo("mapping-123");
        assertThat(result.get(0).getTransformationId()).isEqualTo("transform-123");
        assertThat(result.get(0).getTargetField()).isEqualTo("targetField");
        assertThat(result.get(1).getId()).isEqualTo("mapping-456");
        assertThat(result.get(1).getTargetField()).isEqualTo("targetField2");
    }
    
    @Test
    void testGetByTransformationId_Empty() {
        // Given
        when(mappingRepository.findByTransformationId("non-existent"))
                .thenReturn(Arrays.asList());
        
        // When
        List<FieldMappingDTO> result = fieldMappingService.getByTransformationId("non-existent");
        
        // Then
        assertThat(result).isEmpty();
    }
    
    @Test
    void testSave_NewMapping() {
        // Given
        FieldMappingDTO newMappingDTO = new FieldMappingDTO();
        newMappingDTO.setTransformationId("transform-123");
        newMappingDTO.setSourceFields("[\"newField\"]");
        newMappingDTO.setTargetField("newTarget");
        newMappingDTO.setMappingRule("DIRECT");
        newMappingDTO.setActive(true);
        
        when(transformationRepository.findById("transform-123"))
                .thenReturn(Optional.of(testTransformation));
        when(mappingRepository.save(any(FieldMapping.class)))
                .thenAnswer(invocation -> {
                    FieldMapping mapping = invocation.getArgument(0);
                    mapping.setId("new-mapping-id");
                    mapping.setCreatedAt(LocalDateTime.now());
                    mapping.setUpdatedAt(LocalDateTime.now());
                    return mapping;
                });
        
        // When
        FieldMappingDTO result = fieldMappingService.save(newMappingDTO);
        
        // Then
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo("new-mapping-id");
        assertThat(result.getTransformationId()).isEqualTo("transform-123");
        assertThat(result.getSourceFields()).isEqualTo("[\"newField\"]");
        assertThat(result.getTargetField()).isEqualTo("newTarget");
        
        ArgumentCaptor<FieldMapping> mappingCaptor = ArgumentCaptor.forClass(FieldMapping.class);
        verify(mappingRepository).save(mappingCaptor.capture());
        FieldMapping savedMapping = mappingCaptor.getValue();
        assertThat(savedMapping.getTransformation()).isEqualTo(testTransformation);
    }
    
    @Test
    void testSave_UpdateExisting() {
        // Given
        testMappingDTO.setTargetField("updatedTarget");
        testMappingDTO.setDescription("Updated description");
        
        when(transformationRepository.findById("transform-123"))
                .thenReturn(Optional.of(testTransformation));
        when(mappingRepository.save(any(FieldMapping.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
        
        // When
        FieldMappingDTO result = fieldMappingService.save(testMappingDTO);
        
        // Then
        assertThat(result.getTargetField()).isEqualTo("updatedTarget");
        assertThat(result.getDescription()).isEqualTo("Updated description");
        
        verify(mappingRepository).save(any(FieldMapping.class));
    }
    
    @Test
    void testSave_WithoutTransformationId() {
        // Given
        FieldMappingDTO mappingDTONoTransform = new FieldMappingDTO();
        mappingDTONoTransform.setSourceFields("[\"field\"]");
        mappingDTONoTransform.setTargetField("target");
        mappingDTONoTransform.setMappingRule("DIRECT");
        mappingDTONoTransform.setActive(true);
        // transformationId is null
        
        when(mappingRepository.save(any(FieldMapping.class)))
                .thenAnswer(invocation -> {
                    FieldMapping mapping = invocation.getArgument(0);
                    mapping.setId("mapping-no-transform");
                    mapping.setCreatedAt(LocalDateTime.now());
                    mapping.setUpdatedAt(LocalDateTime.now());
                    return mapping;
                });
        
        // When
        FieldMappingDTO result = fieldMappingService.save(mappingDTONoTransform);
        
        // Then
        assertThat(result).isNotNull();
        assertThat(result.getTransformationId()).isNull();
        
        ArgumentCaptor<FieldMapping> mappingCaptor = ArgumentCaptor.forClass(FieldMapping.class);
        verify(mappingRepository).save(mappingCaptor.capture());
        FieldMapping savedMapping = mappingCaptor.getValue();
        assertThat(savedMapping.getTransformation()).isNull();
    }
    
    @Test
    void testGetById_Found() {
        // Given
        when(mappingRepository.findById("mapping-123"))
                .thenReturn(Optional.of(testMapping));
        
        // When
        Optional<FieldMappingDTO> result = fieldMappingService.getById("mapping-123");
        
        // Then
        assertThat(result).isPresent();
        assertThat(result.get().getId()).isEqualTo("mapping-123");
        assertThat(result.get().getTransformationId()).isEqualTo("transform-123");
        assertThat(result.get().getFunctionName()).isEqualTo("concatenate");
        assertThat(result.get().isActive()).isTrue();
    }
    
    @Test
    void testGetById_NotFound() {
        // Given
        when(mappingRepository.findById("non-existent"))
                .thenReturn(Optional.empty());
        
        // When
        Optional<FieldMappingDTO> result = fieldMappingService.getById("non-existent");
        
        // Then
        assertThat(result).isEmpty();
    }
    
    @Test
    void testDelete() {
        // When
        fieldMappingService.delete("mapping-123");
        
        // Then
        verify(mappingRepository).deleteById("mapping-123");
    }
    
    @Test
    void testToDTO_WithNullTransformation() {
        // Given
        FieldMapping mappingNoTransform = new FieldMapping();
        mappingNoTransform.setId("mapping-no-transform");
        mappingNoTransform.setTransformation(null); // No transformation
        mappingNoTransform.setSourceFields("[\"field\"]");
        mappingNoTransform.setTargetField("target");
        mappingNoTransform.setMappingRule("DIRECT");
        mappingNoTransform.setActive(true);
        mappingNoTransform.setCreatedAt(LocalDateTime.now());
        mappingNoTransform.setUpdatedAt(LocalDateTime.now());
        
        when(mappingRepository.findById("mapping-no-transform"))
                .thenReturn(Optional.of(mappingNoTransform));
        
        // When
        Optional<FieldMappingDTO> result = fieldMappingService.getById("mapping-no-transform");
        
        // Then
        assertThat(result).isPresent();
        assertThat(result.get().getTransformationId()).isNull();
        assertThat(result.get().getSourceFields()).isEqualTo("[\"field\"]");
    }
    
    @Test
    void testFromDTO_TransformationNotFound() {
        // Given
        testMappingDTO.setTransformationId("non-existent-transform");
        
        when(transformationRepository.findById("non-existent-transform"))
                .thenReturn(Optional.empty());
        when(mappingRepository.save(any(FieldMapping.class)))
                .thenAnswer(invocation -> {
                    FieldMapping mapping = invocation.getArgument(0);
                    mapping.setId("new-mapping");
                    return mapping;
                });
        
        // When
        FieldMappingDTO result = fieldMappingService.save(testMappingDTO);
        
        // Then
        assertThat(result).isNotNull();
        
        ArgumentCaptor<FieldMapping> mappingCaptor = ArgumentCaptor.forClass(FieldMapping.class);
        verify(mappingRepository).save(mappingCaptor.capture());
        FieldMapping savedMapping = mappingCaptor.getValue();
        assertThat(savedMapping.getTransformation()).isNull(); // Transformation not set
    }
}