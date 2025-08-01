package com.integrationlab.backend.test;

import com.integrationlab.model.*;
import com.integrationlab.shared.dto.adapter.AdapterConfigDTO;
import com.integrationlab.shared.dto.business.BusinessComponentDTO;
import com.integrationlab.shared.dto.flow.FlowCreateRequestDTO;
import com.integrationlab.shared.dto.flow.IntegrationFlowDTO;
import com.integrationlab.shared.dto.user.LoginRequestDTO;
import com.integrationlab.shared.dto.user.UserDTO;
import com.integrationlab.shared.dto.certificate.CertificateDTO;
import com.integrationlab.shared.dto.certificate.CertificateUploadRequestDTO;
import com.integrationlab.shared.dto.mapping.FieldMappingDTO;
import com.integrationlab.shared.dto.mapping.FlowTransformationDTO;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Test data builder for creating test objects.
 * 
 * <p>Provides fluent API for creating test data with sensible defaults.
 * 
 * @author Integration Team
 * @since 1.0.0
 */
public class TestDataBuilder {
    
    /**
     * Create a test UserDTO.
     */
    public static UserDTOBuilder aUser() {
        return new UserDTOBuilder();
    }
    
    /**
     * Create a test BusinessComponentDTO.
     */
    public static BusinessComponentDTOBuilder aBusinessComponent() {
        return new BusinessComponentDTOBuilder();
    }
    
    /**
     * Create a test IntegrationFlowDTO.
     */
    public static IntegrationFlowDTOBuilder anIntegrationFlow() {
        return new IntegrationFlowDTOBuilder();
    }
    
    /**
     * Create a test AdapterConfigDTO.
     */
    public static AdapterConfigDTOBuilder anAdapterConfig() {
        return new AdapterConfigDTOBuilder();
    }
    
    /**
     * Create a test LoginRequestDTO.
     */
    public static LoginRequestDTOBuilder aLoginRequest() {
        return new LoginRequestDTOBuilder();
    }
    
    /**
     * Create a test FlowCreateRequestDTO.
     */
    public static FlowCreateRequestDTOBuilder aFlowCreateRequest() {
        return new FlowCreateRequestDTOBuilder();
    }
    
    /**
     * Create a test CertificateDTO.
     */
    public static CertificateDTOBuilder aCertificate() {
        return new CertificateDTOBuilder();
    }
    
    /**
     * Create a test CertificateUploadRequestDTO.
     */
    public static CertificateUploadRequestDTOBuilder aCertificateUploadRequest() {
        return new CertificateUploadRequestDTOBuilder();
    }
    
    /**
     * Create a test FieldMappingDTO.
     */
    public static FieldMappingDTOBuilder aFieldMapping() {
        return new FieldMappingDTOBuilder();
    }
    
    /**
     * Create a test FlowTransformationDTO.
     */
    public static FlowTransformationDTOBuilder aFlowTransformation() {
        return new FlowTransformationDTOBuilder();
    }
    
    public static class UserDTOBuilder {
        private String id = UUID.randomUUID().toString();
        private String username = "testuser";
        private String email = "test@example.com";
        private String firstName = "Test";
        private String lastName = "User";
        private String role = "viewer";
        private String status = "active";
        
        public UserDTOBuilder withId(String id) {
            this.id = id;
            return this;
        }
        
        public UserDTOBuilder withUsername(String username) {
            this.username = username;
            return this;
        }
        
        public UserDTOBuilder withEmail(String email) {
            this.email = email;
            return this;
        }
        
        public UserDTOBuilder withRole(String role) {
            this.role = role;
            return this;
        }
        
        public UserDTOBuilder asAdministrator() {
            this.role = "administrator";
            return this;
        }
        
        public UserDTOBuilder asIntegrator() {
            this.role = "integrator";
            return this;
        }
        
        public UserDTO build() {
            return UserDTO.builder()
                    .id(id)
                    .username(username)
                    .email(email)
                    .firstName(firstName)
                    .lastName(lastName)
                    .role(role)
                    .status(status)
                    .createdAt(LocalDateTime.now())
                    .build();
        }
    }
    
    public static class BusinessComponentDTOBuilder {
        private String id = UUID.randomUUID().toString();
        private String name = "Test Component";
        private String description = "Test business component";
        private String contactEmail = "contact@example.com";
        private String contactPhone = "+1234567890";
        
        public BusinessComponentDTOBuilder withId(String id) {
            this.id = id;
            return this;
        }
        
        public BusinessComponentDTOBuilder withName(String name) {
            this.name = name;
            return this;
        }
        
        public BusinessComponentDTOBuilder withContactEmail(String email) {
            this.contactEmail = email;
            return this;
        }
        
        public BusinessComponentDTO build() {
            return BusinessComponentDTO.builder()
                    .id(id)
                    .name(name)
                    .description(description)
                    .contactEmail(contactEmail)
                    .contactPhone(contactPhone)
                    .createdAt(LocalDateTime.now())
                    .build();
        }
    }
    
    public static class IntegrationFlowDTOBuilder {
        private String id = UUID.randomUUID().toString();
        private String name = "Test Flow";
        private String description = "Test integration flow";
        private String sourceAdapterId = UUID.randomUUID().toString();
        private String targetAdapterId = UUID.randomUUID().toString();
        private String status = "INACTIVE";
        private boolean isActive = false;
        
        public IntegrationFlowDTOBuilder withId(String id) {
            this.id = id;
            return this;
        }
        
        public IntegrationFlowDTOBuilder withName(String name) {
            this.name = name;
            return this;
        }
        
        public IntegrationFlowDTOBuilder withSourceAdapter(String adapterId) {
            this.sourceAdapterId = adapterId;
            return this;
        }
        
        public IntegrationFlowDTOBuilder withTargetAdapter(String adapterId) {
            this.targetAdapterId = adapterId;
            return this;
        }
        
        public IntegrationFlowDTOBuilder asActive() {
            this.status = "ACTIVE";
            this.isActive = true;
            return this;
        }
        
        public IntegrationFlowDTO build() {
            return IntegrationFlowDTO.builder()
                    .id(id)
                    .name(name)
                    .description(description)
                    .sourceAdapterId(sourceAdapterId)
                    .targetAdapterId(targetAdapterId)
                    .status(status)
                    .isActive(isActive)
                    .executionCount(0)
                    .successCount(0)
                    .errorCount(0)
                    .createdAt(LocalDateTime.now())
                    .build();
        }
    }
    
    public static class AdapterConfigDTOBuilder {
        private String name = "Test Adapter";
        private String type = "HTTP";
        private String mode = "SENDER";
        private boolean active = true;
        private String description = "Test adapter configuration";
        
        public AdapterConfigDTOBuilder withName(String name) {
            this.name = name;
            return this;
        }
        
        public AdapterConfigDTOBuilder withType(String type) {
            this.type = type;
            return this;
        }
        
        public AdapterConfigDTOBuilder asSender() {
            this.mode = "SENDER";
            return this;
        }
        
        public AdapterConfigDTOBuilder asReceiver() {
            this.mode = "RECEIVER";
            return this;
        }
        
        public AdapterConfigDTOBuilder asHttpAdapter() {
            this.type = "HTTP";
            return this;
        }
        
        public AdapterConfigDTOBuilder asJdbcAdapter() {
            this.type = "JDBC";
            return this;
        }
        
        public AdapterConfigDTO build() {
            Map<String, Object> configMap = new HashMap<>();
            
            // Add type-specific configuration
            switch (type) {
                case "HTTP":
                    configMap.put("url", "http://example.com/api");
                    configMap.put("method", "POST");
                    configMap.put("timeout", 30000);
                    break;
                case "JDBC":
                    configMap.put("jdbcUrl", "jdbc:mysql://localhost:3306/testdb");
                    configMap.put("username", "testuser");
                    configMap.put("password", "testpass");
                    configMap.put("driverClassName", "com.mysql.cj.jdbc.Driver");
                    break;
            }
            
            return AdapterConfigDTO.builder()
                    .name(name)
                    .type(type)
                    .mode(mode)
                    .active(active)
                    .description(description)
                    .configJson(configMap.toString())
                    .build();
        }
    }
    
    public static class LoginRequestDTOBuilder {
        private String username = "testuser";
        private String password = "testpassword123";
        
        public LoginRequestDTOBuilder withUsername(String username) {
            this.username = username;
            return this;
        }
        
        public LoginRequestDTOBuilder withPassword(String password) {
            this.password = password;
            return this;
        }
        
        public LoginRequestDTO build() {
            return LoginRequestDTO.builder()
                    .username(username)
                    .password(password)
                    .build();
        }
    }
    
    public static class FlowCreateRequestDTOBuilder {
        private String name = "Test Flow";
        private String description = "Test flow description";
        private String sourceAdapterId = UUID.randomUUID().toString();
        private String targetAdapterId = UUID.randomUUID().toString();
        private String sourceBusinessComponentId = UUID.randomUUID().toString();
        private String targetBusinessComponentId = UUID.randomUUID().toString();
        private Map<String, Object> configuration = new HashMap<>();
        
        public FlowCreateRequestDTOBuilder withName(String name) {
            this.name = name;
            return this;
        }
        
        public FlowCreateRequestDTOBuilder withSourceAdapter(String adapterId) {
            this.sourceAdapterId = adapterId;
            return this;
        }
        
        public FlowCreateRequestDTOBuilder withTargetAdapter(String adapterId) {
            this.targetAdapterId = adapterId;
            return this;
        }
        
        public FlowCreateRequestDTO build() {
            return FlowCreateRequestDTO.builder()
                    .name(name)
                    .description(description)
                    .sourceAdapterId(sourceAdapterId)
                    .targetAdapterId(targetAdapterId)
                    .sourceBusinessComponentId(sourceBusinessComponentId)
                    .targetBusinessComponentId(targetBusinessComponentId)
                    .configuration(configuration)
                    .build();
        }
    }
    
    public static class CertificateDTOBuilder {
        private String id = UUID.randomUUID().toString();
        private String name = "Test Certificate";
        private String format = "PEM";
        private String type = "SSL";
        private String uploadedBy = "testuser";
        private String uploadedAt = LocalDateTime.now().toString();
        private String issuer = "Test CA";
        private String thumbprint = "AB:CD:EF:12:34:56:78:90";
        private LocalDateTime validFrom = LocalDateTime.now();
        private LocalDateTime validTo = LocalDateTime.now().plusYears(1);
        
        public CertificateDTOBuilder withId(String id) {
            this.id = id;
            return this;
        }
        
        public CertificateDTOBuilder withName(String name) {
            this.name = name;
            return this;
        }
        
        public CertificateDTOBuilder withFormat(String format) {
            this.format = format;
            return this;
        }
        
        public CertificateDTOBuilder asExpired() {
            this.validFrom = LocalDateTime.now().minusYears(2);
            this.validTo = LocalDateTime.now().minusDays(1);
            return this;
        }
        
        public CertificateDTO build() {
            return CertificateDTO.builder()
                    .id(id)
                    .name(name)
                    .format(format)
                    .type(type)
                    .uploadedBy(uploadedBy)
                    .uploadedAt(uploadedAt)
                    .issuer(issuer)
                    .thumbprint(thumbprint)
                    .validFrom(validFrom)
                    .validTo(validTo)
                    .build();
        }
    }
    
    public static class CertificateUploadRequestDTOBuilder {
        private String name = "Test Certificate";
        private String format = "PEM";
        private String type = "SSL";
        private String password = null;
        private String uploadedBy = "testuser";
        
        public CertificateUploadRequestDTOBuilder withName(String name) {
            this.name = name;
            return this;
        }
        
        public CertificateUploadRequestDTOBuilder withPassword(String password) {
            this.password = password;
            return this;
        }
        
        public CertificateUploadRequestDTOBuilder asClientCertificate() {
            this.type = "CLIENT";
            return this;
        }
        
        public CertificateUploadRequestDTO build() {
            return CertificateUploadRequestDTO.builder()
                    .name(name)
                    .format(format)
                    .type(type)
                    .password(password)
                    .uploadedBy(uploadedBy)
                    .build();
        }
    }
    
    public static class FieldMappingDTOBuilder {
        private String id = UUID.randomUUID().toString();
        private String transformationId = UUID.randomUUID().toString();
        private String sourceFields = "[\"field1\", \"field2\"]";
        private String targetField = "targetField";
        private String javaFunction = "function transform(field1, field2) { return field1 + field2; }";
        private String mappingRule = "CONCATENATE";
        private String inputTypes = "[\"string\", \"string\"]";
        private String outputType = "string";
        private String description = "Test field mapping";
        private int version = 1;
        private String functionName = "transform";
        private boolean active = true;
        
        public FieldMappingDTOBuilder withId(String id) {
            this.id = id;
            return this;
        }
        
        public FieldMappingDTOBuilder withTransformationId(String transformationId) {
            this.transformationId = transformationId;
            return this;
        }
        
        public FieldMappingDTOBuilder withSourceFields(String... fields) {
            this.sourceFields = "[" + Arrays.stream(fields)
                    .map(f -> "\"" + f + "\"")
                    .collect(Collectors.joining(", ")) + "]";
            return this;
        }
        
        public FieldMappingDTOBuilder withTargetField(String targetField) {
            this.targetField = targetField;
            return this;
        }
        
        public FieldMappingDTOBuilder asDirectMapping() {
            this.mappingRule = "DIRECT";
            this.sourceFields = "[\"sourceField\"]";
            this.javaFunction = "function transform(sourceField) { return sourceField; }";
            return this;
        }
        
        public FieldMappingDTO build() {
            return FieldMappingDTO.builder()
                    .id(id)
                    .transformationId(transformationId)
                    .sourceFields(sourceFields)
                    .targetField(targetField)
                    .javaFunction(javaFunction)
                    .mappingRule(mappingRule)
                    .inputTypes(inputTypes)
                    .outputType(outputType)
                    .description(description)
                    .version(version)
                    .functionName(functionName)
                    .active(active)
                    .createdAt(LocalDateTime.now())
                    .updatedAt(LocalDateTime.now())
                    .build();
        }
    }
    
    public static class FlowTransformationDTOBuilder {
        private String id = UUID.randomUUID().toString();
        private String flowId = UUID.randomUUID().toString();
        private String name = "Test Transformation";
        private String description = "Test transformation description";
        private String type = "FIELD_MAPPING";
        private int order = 1;
        private Map<String, Object> configuration = new HashMap<>();
        private boolean active = true;
        
        public FlowTransformationDTOBuilder withId(String id) {
            this.id = id;
            return this;
        }
        
        public FlowTransformationDTOBuilder withFlowId(String flowId) {
            this.flowId = flowId;
            return this;
        }
        
        public FlowTransformationDTOBuilder withName(String name) {
            this.name = name;
            return this;
        }
        
        public FlowTransformationDTOBuilder withOrder(int order) {
            this.order = order;
            return this;
        }
        
        public FlowTransformationDTOBuilder asFilterTransformation() {
            this.type = "FILTER";
            this.configuration.put("filterExpression", "status == 'active'");
            return this;
        }
        
        public FlowTransformationDTOBuilder asEnrichmentTransformation() {
            this.type = "ENRICHMENT";
            this.configuration.put("enrichmentSource", "external-api");
            return this;
        }
        
        public FlowTransformationDTO build() {
            return FlowTransformationDTO.builder()
                    .id(id)
                    .flowId(flowId)
                    .name(name)
                    .description(description)
                    .type(type)
                    .order(order)
                    .configuration(configuration)
                    .active(active)
                    .createdAt(LocalDateTime.now())
                    .updatedAt(LocalDateTime.now())
                    .build();
        }
    }
}