# Integrix Flow Bridge - Testing Guide

## Overview

This directory contains the comprehensive test suite for the Integrix Flow Bridge backend application. The test suite includes unit tests, integration tests, and test utilities designed to ensure code quality and reliability.

## Test Structure

```
src/test/
├── java/com/integrationlab/
│   ├── backend/test/           # Test utilities and base classes
│   │   ├── BaseUnitTest.java
│   │   ├── BaseIntegrationTest.java
│   │   ├── TestDataBuilder.java
│   │   ├── TestFixtures.java
│   │   └── JwtTestUtils.java
│   ├── controller/             # Integration tests for controllers
│   │   ├── AuthControllerIntegrationTest.java
│   │   └── CertificateControllerIntegrationTest.java
│   └── service/                # Unit tests for services
│       ├── CertificateServiceTest.java
│       ├── UserServiceTest.java
│       └── FieldMappingServiceTest.java
└── resources/
    └── application-test.yml    # Test configuration

```

## Test Types

### Unit Tests
- Isolated testing of individual components
- Mocked dependencies using Mockito
- Fast execution
- Located in service test classes

### Integration Tests
- End-to-end testing of API endpoints
- Uses TestContainers for MySQL database
- Tests complete request/response cycle
- Located in controller test classes

## Running Tests

### All Tests
```bash
mvn test
```

### Unit Tests Only
```bash
mvn test -Dtest="*Test"
```

### Integration Tests Only
```bash
mvn test -Dtest="*IntegrationTest"
```

### Specific Test Class
```bash
mvn test -Dtest="UserServiceTest"
```

### Test Coverage Report
```bash
mvn clean test jacoco:report
```
Coverage reports are generated in `target/site/jacoco/index.html`

## Test Utilities

### BaseUnitTest
Base class for all unit tests providing:
- Mockito extension configuration
- Common setup methods
- Test profile activation

### BaseIntegrationTest
Base class for integration tests providing:
- Spring Boot test context
- MockMvc configuration
- TestContainers MySQL setup
- JSON serialization helpers

### TestDataBuilder
Fluent API for creating test data:
```java
UserDTO user = TestDataBuilder.aUser()
    .withUsername("testuser")
    .asAdministrator()
    .build();
```

### TestFixtures
Pre-configured test scenarios:
```java
HttpToDatabaseFlowFixture fixture = new TestFixtures.HttpToDatabaseFlowFixture();
// Access pre-configured components
fixture.sourceComponent
fixture.httpSenderAdapter
fixture.integrationFlow
```

### JwtTestUtils
JWT token generation for testing:
```java
String token = JwtTestUtils.createAdminToken();
String authHeader = JwtTestUtils.createAuthorizationHeader(token);
```

## Test Configuration

### application-test.yml
- H2 in-memory database for unit tests
- TestContainers MySQL for integration tests
- Disabled rate limiting
- Test JWT configuration
- Reduced logging levels

## Best Practices

1. **Test Naming**: Use descriptive names following the pattern `testMethodName_Scenario_ExpectedResult()`

2. **Test Data**: Use TestDataBuilder and TestFixtures for consistent test data

3. **Assertions**: Use AssertJ for readable assertions:
   ```java
   assertThat(result)
       .isNotNull()
       .hasSize(3)
       .extracting("name")
       .containsExactly("A", "B", "C");
   ```

4. **Mocking**: Mock external dependencies but avoid over-mocking

5. **Test Independence**: Each test should be independent and repeatable

6. **Database State**: Use @Transactional for automatic rollback in integration tests

## Coverage Requirements

JaCoCo is configured to enforce:
- 70% line coverage per class
- 60% branch coverage per class
- 70% line coverage per package

Excluded from coverage:
- Configuration classes
- DTOs and model classes
- Exception classes
- Application main class

## Continuous Integration

Tests are automatically run on:
- Every push to the repository
- Pull request creation/update
- Pre-deployment validation

## Troubleshooting

### TestContainers Issues
- Ensure Docker is running
- Check Docker resources (memory/CPU)
- Use `@Testcontainers(disabledWithoutDocker = true)` to skip when Docker unavailable

### Memory Issues
- Increase Maven memory: `export MAVEN_OPTS="-Xmx2048m"`
- Run tests in batches if needed

### Database Connection Issues
- Check test database configuration
- Verify TestContainers MySQL is starting correctly
- Check for port conflicts

## Adding New Tests

1. Choose appropriate base class (BaseUnitTest or BaseIntegrationTest)
2. Use TestDataBuilder for test data creation
3. Follow existing naming conventions
4. Add appropriate assertions
5. Ensure tests are independent
6. Run coverage report to verify coverage

## Future Enhancements

- [ ] Add performance tests
- [ ] Implement contract testing
- [ ] Add mutation testing
- [ ] Create test data generation scripts
- [ ] Add visual regression tests for frontend