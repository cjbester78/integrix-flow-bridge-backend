package com.integrixs.data.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.GenericGenerator;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Entity representing custom transformation functions that can be used in visual flow and orchestration editors
 */
@Entity
@Table(name = "transformation_custom_functions")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class TransformationCustomFunction {

    @Id
    @GeneratedValue(generator = "uuid2")
    @GenericGenerator(name = "uuid2", strategy = "org.hibernate.id.UUIDGenerator")
    @Column(name = "function_id", columnDefinition = "char(36)")
    @EqualsAndHashCode.Include
    private String functionId;

    @Column(name = "name", nullable = false, unique = true, length = 100)
    private String name;

    @Column(name = "description", length = 500)
    private String description;
    
    @Column(name = "category", length = 50)
    private String category;

    @Enumerated(EnumType.STRING)
    @Column(name = "language", nullable = false, length = 20)
    private FunctionLanguage language;

    @Column(name = "function_signature", nullable = false, length = 500)
    private String functionSignature;
    
    @Column(name = "parameters", columnDefinition = "json")
    private String parameters; // JSON representation of function parameters

    @Column(name = "function_body", nullable = false, columnDefinition = "TEXT")
    private String functionBody;

    @ElementCollection
    @CollectionTable(name = "function_dependencies", joinColumns = @JoinColumn(name = "function_id"))
    @Column(name = "dependency")
    private List<String> dependencies;

    @ElementCollection
    @CollectionTable(name = "function_test_cases", joinColumns = @JoinColumn(name = "function_id"))
    private List<TestCase> testCases;

    @Column(name = "is_safe", nullable = false)
    private boolean isSafe = false;

    @Column(name = "is_public", nullable = false)
    private boolean isPublic = true;
    
    @Column(name = "is_built_in", nullable = false)
    private boolean isBuiltIn = false;

    @Enumerated(EnumType.STRING)
    @Column(name = "performance_class", nullable = false, length = 20)
    private PerformanceClass performanceClass = PerformanceClass.NORMAL;

    @Column(name = "version", nullable = false)
    private int version = 1;

    @Column(name = "created_by", length = 100)
    private String createdBy;

    @Column(name = "created_at", nullable = false)
    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    public enum FunctionLanguage {
        JAVA,
        JAVASCRIPT,
        GROOVY,
        PYTHON
    }

    public enum PerformanceClass {
        FAST,
        NORMAL,
        SLOW
    }

    @Embeddable
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TestCase {
        @Column(name = "test_name", length = 100)
        private String testName;

        @Column(name = "input_data", columnDefinition = "TEXT")
        private String inputData;

        @Column(name = "expected_output", columnDefinition = "TEXT")
        private String expectedOutput;

        @Column(name = "test_description", length = 500)
        private String testDescription;
    }
}