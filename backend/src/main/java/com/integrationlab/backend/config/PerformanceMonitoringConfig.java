package com.integrationlab.backend.config;

import io.micrometer.core.aop.TimedAspect;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.boot.actuate.autoconfigure.metrics.MeterRegistryCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;

import javax.sql.DataSource;
import java.util.concurrent.TimeUnit;

/**
 * Performance monitoring configuration.
 * 
 * <p>Configures metrics collection for database queries, API endpoints,
 * and custom business operations.
 * 
 * @author Integration Team
 * @since 1.0.0
 */
@Slf4j
@Configuration
@EnableAspectJAutoProxy
public class PerformanceMonitoringConfig {
    
    /**
     * Customizes the meter registry with application-specific tags.
     */
    @Bean
    public MeterRegistryCustomizer<MeterRegistry> metricsCommonTags() {
        return registry -> registry.config()
                .commonTags("application", "integrix-flow-bridge")
                .commonTags("environment", System.getProperty("spring.profiles.active", "default"));
    }
    
    /**
     * Enables @Timed annotation support for method-level metrics.
     */
    @Bean
    public TimedAspect timedAspect(MeterRegistry registry) {
        return new TimedAspect(registry);
    }
    
    /**
     * Monitors database connection pool metrics.
     */
    @Bean
    public DataSourceMetrics dataSourceMetrics(DataSource dataSource, MeterRegistry registry) {
        return new DataSourceMetrics(dataSource, registry);
    }
    
    /**
     * Custom metrics for monitoring database operations.
     */
    public static class DataSourceMetrics {
        private final DataSource dataSource;
        private final MeterRegistry registry;
        
        public DataSourceMetrics(DataSource dataSource, MeterRegistry registry) {
            this.dataSource = dataSource;
            this.registry = registry;
            
            // Register HikariCP metrics if available
            if (dataSource instanceof com.zaxxer.hikari.HikariDataSource) {
                var hikariDataSource = (com.zaxxer.hikari.HikariDataSource) dataSource;
                hikariDataSource.setMetricRegistry(registry);
            }
        }
    }
    
    /**
     * Aspect for monitoring repository method performance.
     */
    @Aspect
    @Configuration
    public static class RepositoryPerformanceAspect {
        
        private final MeterRegistry registry;
        
        public RepositoryPerformanceAspect(MeterRegistry registry) {
            this.registry = registry;
        }
        
        @Around("@within(org.springframework.stereotype.Repository)")
        public Object measureRepositoryPerformance(ProceedingJoinPoint joinPoint) throws Throwable {
            String className = joinPoint.getTarget().getClass().getSimpleName();
            String methodName = joinPoint.getSignature().getName();
            
            Timer.Sample sample = Timer.start(registry);
            
            try {
                Object result = joinPoint.proceed();
                
                sample.stop(Timer.builder("repository.method.duration")
                        .description("Repository method execution time")
                        .tags("class", className, "method", methodName, "status", "success")
                        .register(registry));
                
                return result;
            } catch (Exception e) {
                sample.stop(Timer.builder("repository.method.duration")
                        .description("Repository method execution time")
                        .tags("class", className, "method", methodName, "status", "error")
                        .register(registry));
                
                throw e;
            }
        }
    }
    
    /**
     * Aspect for monitoring service method performance.
     */
    @Aspect
    @Configuration
    public static class ServicePerformanceAspect {
        
        private final MeterRegistry registry;
        
        public ServicePerformanceAspect(MeterRegistry registry) {
            this.registry = registry;
        }
        
        @Around("@within(org.springframework.stereotype.Service) && " +
                "execution(* com.integrationlab.service.*.*(..))")
        public Object measureServicePerformance(ProceedingJoinPoint joinPoint) throws Throwable {
            String className = joinPoint.getTarget().getClass().getSimpleName();
            String methodName = joinPoint.getSignature().getName();
            
            // Skip trivial getter/setter methods
            if (methodName.startsWith("get") || methodName.startsWith("set") || 
                methodName.startsWith("is") || methodName.equals("toString")) {
                return joinPoint.proceed();
            }
            
            Timer.Sample sample = Timer.start(registry);
            
            try {
                Object result = joinPoint.proceed();
                
                sample.stop(Timer.builder("service.method.duration")
                        .description("Service method execution time")
                        .tags("class", className, "method", methodName, "status", "success")
                        .publishPercentiles(0.5, 0.95, 0.99)
                        .register(registry));
                
                return result;
            } catch (Exception e) {
                sample.stop(Timer.builder("service.method.duration")
                        .description("Service method execution time")
                        .tags("class", className, "method", methodName, "status", "error")
                        .register(registry));
                
                throw e;
            }
        }
    }
    
    /**
     * Configuration for slow query detection.
     */
    @Bean
    public SlowQueryDetector slowQueryDetector(MeterRegistry registry) {
        return new SlowQueryDetector(registry, 1000); // 1 second threshold
    }
    
    /**
     * Detector for slow database queries.
     */
    public static class SlowQueryDetector {
        private final MeterRegistry registry;
        private final long thresholdMillis;
        
        public SlowQueryDetector(MeterRegistry registry, long thresholdMillis) {
            this.registry = registry;
            this.thresholdMillis = thresholdMillis;
        }
        
        public void recordQueryTime(String query, long durationMillis) {
            if (durationMillis > thresholdMillis) {
                registry.counter("database.slow.queries", 
                        "query", truncateQuery(query),
                        "duration_bucket", getDurationBucket(durationMillis))
                        .increment();
                
                log.warn("Slow query detected ({}ms): {}", durationMillis, truncateQuery(query));
            }
            
            registry.timer("database.query.duration", "type", getQueryType(query))
                    .record(durationMillis, TimeUnit.MILLISECONDS);
        }
        
        private String truncateQuery(String query) {
            return query.length() > 50 ? query.substring(0, 50) + "..." : query;
        }
        
        private String getDurationBucket(long millis) {
            if (millis < 1000) return "under_1s";
            if (millis < 5000) return "1s_to_5s";
            if (millis < 10000) return "5s_to_10s";
            return "over_10s";
        }
        
        private String getQueryType(String query) {
            String upperQuery = query.toUpperCase().trim();
            if (upperQuery.startsWith("SELECT")) return "SELECT";
            if (upperQuery.startsWith("INSERT")) return "INSERT";
            if (upperQuery.startsWith("UPDATE")) return "UPDATE";
            if (upperQuery.startsWith("DELETE")) return "DELETE";
            return "OTHER";
        }
    }
    
    /**
     * Bean for exposing performance metrics via JMX.
     */
    @Bean
    public PerformanceMetricsExporter performanceMetricsExporter(MeterRegistry registry) {
        return new PerformanceMetricsExporter(registry);
    }
    
    /**
     * Exports performance metrics for monitoring.
     */
    public static class PerformanceMetricsExporter {
        private final MeterRegistry registry;
        
        public PerformanceMetricsExporter(MeterRegistry registry) {
            this.registry = registry;
            
            // Register custom gauges
            registry.gauge("jvm.memory.heap.percentage", this, 
                    exporter -> getHeapUsagePercentage());
            
            registry.gauge("system.cpu.usage.percentage", this,
                    exporter -> getCpuUsagePercentage());
        }
        
        private double getHeapUsagePercentage() {
            Runtime runtime = Runtime.getRuntime();
            long maxHeap = runtime.maxMemory();
            long usedHeap = runtime.totalMemory() - runtime.freeMemory();
            return (double) usedHeap / maxHeap * 100;
        }
        
        private double getCpuUsagePercentage() {
            // This is a simplified version - in production, use proper CPU metrics
            return ((com.sun.management.OperatingSystemMXBean) 
                    java.lang.management.ManagementFactory.getOperatingSystemMXBean())
                    .getProcessCpuLoad() * 100;
        }
    }
}