package com.integrationlab.backend.test;

import com.integrationlab.model.*;
import com.integrationlab.shared.dto.adapter.AdapterConfigDTO;
import com.integrationlab.shared.dto.business.BusinessComponentDTO;
import com.integrationlab.shared.dto.flow.IntegrationFlowDTO;
import com.integrationlab.shared.dto.user.UserDTO;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

/**
 * Test fixtures providing pre-configured test data scenarios.
 * 
 * <p>Provides common test data scenarios that can be reused across multiple tests.
 * 
 * @author Integration Team
 * @since 1.0.0
 */
public class TestFixtures {
    
    /**
     * Creates a complete integration flow scenario with HTTP to Database.
     */
    public static class HttpToDatabaseFlowFixture {
        public final BusinessComponentDTO sourceComponent;
        public final BusinessComponentDTO targetComponent;
        public final AdapterConfigDTO httpSenderAdapter;
        public final AdapterConfigDTO jdbcReceiverAdapter;
        public final IntegrationFlowDTO integrationFlow;
        
        public HttpToDatabaseFlowFixture() {
            // Source business component
            sourceComponent = TestDataBuilder.aBusinessComponent()
                    .withName("External API System")
                    .withContactEmail("api@external.com")
                    .build();
            
            // Target business component
            targetComponent = TestDataBuilder.aBusinessComponent()
                    .withName("Internal Database")
                    .withContactEmail("db@internal.com")
                    .build();
            
            // HTTP Sender adapter (receives from external)
            httpSenderAdapter = TestDataBuilder.anAdapterConfig()
                    .withName("HTTP API Receiver")
                    .asHttpAdapter()
                    .asSender()
                    .build();
            
            // JDBC Receiver adapter (sends to database)
            jdbcReceiverAdapter = TestDataBuilder.anAdapterConfig()
                    .withName("Database Writer")
                    .asJdbcAdapter()
                    .asReceiver()
                    .build();
            
            // Integration flow
            integrationFlow = TestDataBuilder.anIntegrationFlow()
                    .withName("API to Database Flow")
                    .withSourceAdapter(httpSenderAdapter.getId())
                    .withTargetAdapter(jdbcReceiverAdapter.getId())
                    .asActive()
                    .build();
        }
    }
    
    /**
     * Creates a complete user hierarchy with different roles.
     */
    public static class UserHierarchyFixture {
        public final UserDTO administrator;
        public final UserDTO integrator;
        public final UserDTO viewer;
        public final List<UserDTO> allUsers;
        
        public UserHierarchyFixture() {
            administrator = TestDataBuilder.aUser()
                    .withUsername("admin")
                    .withEmail("admin@example.com")
                    .asAdministrator()
                    .build();
            
            integrator = TestDataBuilder.aUser()
                    .withUsername("integrator1")
                    .withEmail("integrator@example.com")
                    .asIntegrator()
                    .build();
            
            viewer = TestDataBuilder.aUser()
                    .withUsername("viewer1")
                    .withEmail("viewer@example.com")
                    .withRole("viewer")
                    .build();
            
            allUsers = Arrays.asList(administrator, integrator, viewer);
        }
    }
    
    /**
     * Creates a complete orchestration flow with multiple transformations.
     */
    public static class OrchestrationFlowFixture {
        public final IntegrationFlowDTO orchestrationFlow;
        public final List<FlowTransformation> transformations;
        public final List<FieldMapping> fieldMappings;
        
        public OrchestrationFlowFixture() {
            // Create orchestration flow
            orchestrationFlow = TestDataBuilder.anIntegrationFlow()
                    .withName("Complex Orchestration Flow")
                    .build();
            
            // Create transformations
            FlowTransformation filterTransform = new FlowTransformation();
            filterTransform.setId(UUID.randomUUID().toString());
            filterTransform.setName("Filter Active Records");
            filterTransform.setType("FILTER");
            filterTransform.setOrder(1);
            filterTransform.setActive(true);
            
            FlowTransformation mappingTransform = new FlowTransformation();
            mappingTransform.setId(UUID.randomUUID().toString());
            mappingTransform.setName("Map Fields");
            mappingTransform.setType("FIELD_MAPPING");
            mappingTransform.setOrder(2);
            mappingTransform.setActive(true);
            
            FlowTransformation enrichmentTransform = new FlowTransformation();
            enrichmentTransform.setId(UUID.randomUUID().toString());
            enrichmentTransform.setName("Enrich Data");
            enrichmentTransform.setType("ENRICHMENT");
            enrichmentTransform.setOrder(3);
            enrichmentTransform.setActive(true);
            
            transformations = Arrays.asList(filterTransform, mappingTransform, enrichmentTransform);
            
            // Create field mappings
            FieldMapping nameMapping = new FieldMapping();
            nameMapping.setId(UUID.randomUUID().toString());
            nameMapping.setTransformation(mappingTransform);
            nameMapping.setSourceFields("[\"firstName\", \"lastName\"]");
            nameMapping.setTargetField("fullName");
            nameMapping.setMappingRule("CONCATENATE");
            nameMapping.setJavaFunction("function(firstName, lastName) { return firstName + ' ' + lastName; }");
            nameMapping.setActive(true);
            
            FieldMapping dateMapping = new FieldMapping();
            dateMapping.setId(UUID.randomUUID().toString());
            dateMapping.setTransformation(mappingTransform);
            dateMapping.setSourceFields("[\"timestamp\"]");
            dateMapping.setTargetField("processedDate");
            dateMapping.setMappingRule("DATE_FORMAT");
            dateMapping.setJavaFunction("function(timestamp) { return new Date(timestamp).toISOString(); }");
            dateMapping.setActive(true);
            
            fieldMappings = Arrays.asList(nameMapping, dateMapping);
        }
    }
    
    /**
     * Creates test certificates with various states.
     */
    public static class CertificateFixture {
        public final Certificate validSSLCert;
        public final Certificate expiredCert;
        public final Certificate clientCert;
        public final Certificate caCert;
        
        public CertificateFixture() {
            validSSLCert = new Certificate();
            validSSLCert.setId(UUID.randomUUID().toString());
            validSSLCert.setName("Valid SSL Certificate");
            validSSLCert.setFormat("PEM");
            validSSLCert.setType("SSL");
            validSSLCert.setUploadedBy("admin");
            validSSLCert.setFileName("valid-ssl.crt");
            validSSLCert.setContent(generateTestCertContent("SSL"));
            validSSLCert.setUploadedAt(LocalDateTime.now());
            
            expiredCert = new Certificate();
            expiredCert.setId(UUID.randomUUID().toString());
            expiredCert.setName("Expired Certificate");
            expiredCert.setFormat("PEM");
            expiredCert.setType("SSL");
            expiredCert.setUploadedBy("admin");
            expiredCert.setFileName("expired.crt");
            expiredCert.setContent(generateTestCertContent("EXPIRED"));
            expiredCert.setUploadedAt(LocalDateTime.now().minusYears(2));
            
            clientCert = new Certificate();
            clientCert.setId(UUID.randomUUID().toString());
            clientCert.setName("Client Certificate");
            clientCert.setFormat("PEM");
            clientCert.setType("CLIENT");
            clientCert.setUploadedBy("integrator");
            clientCert.setFileName("client.crt");
            clientCert.setContent(generateTestCertContent("CLIENT"));
            clientCert.setUploadedAt(LocalDateTime.now());
            clientCert.setPassword("client-password");
            
            caCert = new Certificate();
            caCert.setId(UUID.randomUUID().toString());
            caCert.setName("CA Certificate");
            caCert.setFormat("PEM");
            caCert.setType("CA");
            caCert.setUploadedBy("admin");
            caCert.setFileName("ca.crt");
            caCert.setContent(generateTestCertContent("CA"));
            caCert.setUploadedAt(LocalDateTime.now());
        }
        
        private byte[] generateTestCertContent(String type) {
            return ("-----BEGIN CERTIFICATE-----\n" +
                    "MIIB" + type + "TCB+wIJAKHHIG...\n" +
                    "Test certificate content for " + type + "\n" +
                    "-----END CERTIFICATE-----").getBytes();
        }
    }
    
    /**
     * Creates a complete error scenario with failed flows and logs.
     */
    public static class ErrorScenarioFixture {
        public final IntegrationFlow failedFlow;
        public final List<SystemLog> errorLogs;
        public final List<FlowExecution> failedExecutions;
        
        public ErrorScenarioFixture() {
            // Create failed flow
            failedFlow = new IntegrationFlow();
            failedFlow.setId(UUID.randomUUID().toString());
            failedFlow.setName("Failed Integration Flow");
            failedFlow.setStatus("ERROR");
            failedFlow.setActive(false);
            failedFlow.setExecutionCount(100);
            failedFlow.setSuccessCount(20);
            failedFlow.setErrorCount(80);
            failedFlow.setCreatedAt(LocalDateTime.now().minusDays(7));
            failedFlow.setUpdatedAt(LocalDateTime.now().minusHours(1));
            
            // Create error logs
            SystemLog connectionError = new SystemLog();
            connectionError.setId(UUID.randomUUID().toString());
            connectionError.setLevel("ERROR");
            connectionError.setMessage("Failed to connect to target system");
            connectionError.setComponent("JdbcReceiver");
            connectionError.setFlowId(failedFlow.getId());
            connectionError.setTimestamp(LocalDateTime.now().minusMinutes(5));
            connectionError.setStackTrace("java.sql.SQLException: Connection refused\n\tat com.integrationlab...");
            
            SystemLog transformationError = new SystemLog();
            transformationError.setId(UUID.randomUUID().toString());
            transformationError.setLevel("ERROR");
            transformationError.setMessage("Transformation failed: Invalid function");
            transformationError.setComponent("TransformationEngine");
            transformationError.setFlowId(failedFlow.getId());
            transformationError.setTimestamp(LocalDateTime.now().minusMinutes(10));
            
            errorLogs = Arrays.asList(connectionError, transformationError);
            
            // Create failed executions
            FlowExecution execution1 = new FlowExecution();
            execution1.setId(UUID.randomUUID().toString());
            execution1.setFlow(failedFlow);
            execution1.setStatus("FAILED");
            execution1.setStartTime(LocalDateTime.now().minusMinutes(5));
            execution1.setEndTime(LocalDateTime.now().minusMinutes(4));
            execution1.setErrorMessage("Connection timeout");
            
            FlowExecution execution2 = new FlowExecution();
            execution2.setId(UUID.randomUUID().toString());
            execution2.setFlow(failedFlow);
            execution2.setStatus("FAILED");
            execution2.setStartTime(LocalDateTime.now().minusMinutes(10));
            execution2.setEndTime(LocalDateTime.now().minusMinutes(9));
            execution2.setErrorMessage("Transformation error");
            
            failedExecutions = Arrays.asList(execution1, execution2);
        }
    }
    
    /**
     * Creates test data for performance testing.
     */
    public static class PerformanceTestFixture {
        public final List<IntegrationFlowDTO> manyFlows;
        public final List<FieldMapping> manyMappings;
        
        public PerformanceTestFixture(int flowCount, int mappingsPerFlow) {
            manyFlows = new java.util.ArrayList<>();
            manyMappings = new java.util.ArrayList<>();
            
            for (int i = 0; i < flowCount; i++) {
                IntegrationFlowDTO flow = TestDataBuilder.anIntegrationFlow()
                        .withName("Performance Test Flow " + i)
                        .build();
                manyFlows.add(flow);
                
                for (int j = 0; j < mappingsPerFlow; j++) {
                    FieldMapping mapping = new FieldMapping();
                    mapping.setId(UUID.randomUUID().toString());
                    mapping.setSourceFields("[\"field" + j + "\"]");
                    mapping.setTargetField("target" + j);
                    mapping.setMappingRule("DIRECT");
                    mapping.setActive(true);
                    manyMappings.add(mapping);
                }
            }
        }
    }
}