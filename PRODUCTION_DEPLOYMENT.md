# Integrix Flow Bridge - Production Deployment Guide

## Production Readiness Checklist

### ✅ Completed Items
1. **Backend API Endpoints** - All dashboard, message, and channel endpoints implemented
2. **Database Schema** - Complete MySQL schema with all tables and relationships
3. **Security** - JWT authentication with Spring Security configured
4. **Frontend-Backend Integration** - All mock data removed, using real APIs

### ⚠️ Items Requiring Configuration

#### 1. Database Configuration
- Update `application-prod.yml` with production database credentials:
  ```yaml
  spring:
    datasource:
      url: jdbc:mysql://YOUR_PROD_DB_HOST:3306/integrixflowbridge
      username: YOUR_PROD_USER
      password: YOUR_SECURE_PASSWORD
  ```

#### 2. JWT Secret Configuration
- Generate a secure JWT secret for production
- Configure in environment variable or secure configuration management

#### 3. SSL/TLS Configuration
- Configure HTTPS for production deployment
- Update certificate storage path in `application.yml`

#### 4. External Service Credentials
- Configure credentials for external adapters (SAP, SMTP, FTP, etc.)
- Use environment variables or secure vault for sensitive data

## Build and Deployment Steps

### 1. Build the Application
```bash
# From project root
mvn clean package -Pprod -DskipTests

# The executable JAR will be in:
# backend/target/backend-0.0.1-SNAPSHOT.jar
```

### 2. Database Setup
```sql
CREATE DATABASE integrixflowbridge DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
-- Run the schema from db/src/main/resources/db/schema/schema.sql
```

### 3. Run the Application
```bash
java -jar backend-0.0.1-SNAPSHOT.jar \
  --spring.profiles.active=prod \
  --spring.datasource.password=YOUR_SECURE_PASSWORD
```

### 4. Production Environment Variables
```bash
export SPRING_PROFILES_ACTIVE=prod
export DB_HOST=your-db-host
export DB_PASSWORD=your-secure-password
export JWT_SECRET=your-jwt-secret
export CERTIFICATE_PATH=/opt/integrixflowbridge/certs
```

## Docker Deployment (Optional)

### Create Dockerfile
```dockerfile
FROM openjdk:21-jdk-slim
VOLUME /tmp
COPY backend/target/backend-0.0.1-SNAPSHOT.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java","-jar","/app.jar","--spring.profiles.active=prod"]
```

### Docker Compose
```yaml
version: '3.8'
services:
  app:
    build: .
    ports:
      - "8080:8080"
    environment:
      - SPRING_PROFILES_ACTIVE=prod
      - SPRING_DATASOURCE_URL=jdbc:mysql://db:3306/integrixflowbridge
      - SPRING_DATASOURCE_USERNAME=root
      - SPRING_DATASOURCE_PASSWORD=rootpassword
    depends_on:
      - db
      
  db:
    image: mysql:8
    environment:
      - MYSQL_ROOT_PASSWORD=rootpassword
      - MYSQL_DATABASE=integrixflowbridge
    volumes:
      - mysql_data:/var/lib/mysql
      
volumes:
  mysql_data:
```

## Monitoring and Health Checks

### Health Check Endpoint
- Available at: `http://localhost:8080/actuator/health`
- Provides database connectivity status

### Metrics Endpoints
- All actuator endpoints exposed at `/actuator/*`
- Monitor JVM metrics, thread pools, HTTP metrics

## Performance Tuning

### Database Connection Pool
```yaml
spring:
  datasource:
    hikari:
      maximum-pool-size: 20
      minimum-idle: 5
      connection-timeout: 30000
```

### Thread Pool Configuration
```yaml
engine:
  worker:
    thread-pool-size: 8  # Adjust based on CPU cores
    retry-attempts: 3
```

## Security Hardening

1. **Disable unnecessary actuator endpoints** in production
2. **Configure CORS** appropriately for your domain
3. **Enable HTTPS** with valid SSL certificates
4. **Implement rate limiting** for API endpoints
5. **Regular security updates** for dependencies

## Backup and Recovery

1. **Database Backups**
   - Schedule regular MySQL backups
   - Test restore procedures

2. **Application Logs**
   - Configure log rotation
   - Centralize logs for monitoring

3. **Certificate Backups**
   - Backup certificates stored in `/opt/integrixflowbridge/certs`

## Troubleshooting

### Common Issues

1. **Database Connection Failed**
   - Check database host and credentials
   - Verify network connectivity
   - Check MySQL user permissions

2. **JWT Authentication Issues**
   - Verify JWT secret is configured
   - Check token expiration settings

3. **Adapter Connection Failures**
   - Verify external service credentials
   - Check firewall rules
   - Review adapter-specific logs

## Support

For production support issues:
1. Check application logs
2. Review system logs in database
3. Monitor actuator health endpoints