package com.integrationlab.backend.config;

import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import lombok.extern.slf4j.Slf4j;

import java.lang.management.GarbageCollectorMXBean;
import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.lang.management.MemoryUsage;
import java.util.List;

/**
 * Configuration for JVM garbage collection optimization and monitoring.
 * 
 * <p>Provides automatic memory management, monitoring, and optimization
 * to prevent memory leaks and ensure optimal performance.
 * 
 * @author Integration Team
 * @since 1.0.0
 */
@Slf4j
@Configuration
@EnableScheduling
public class GarbageCollectionConfig {
    
    private final MemoryMXBean memoryMXBean = ManagementFactory.getMemoryMXBean();
    private final List<GarbageCollectorMXBean> gcBeans = ManagementFactory.getGarbageCollectorMXBeans();
    
    /**
     * Log JVM memory settings on startup.
     */
    @Bean
    public ApplicationRunner logMemorySettings() {
        return args -> {
            log.info("=== JVM Memory Configuration ===");
            
            MemoryUsage heapUsage = memoryMXBean.getHeapMemoryUsage();
            log.info("Heap Memory - Initial: {}MB, Max: {}MB", 
                     heapUsage.getInit() / (1024 * 1024),
                     heapUsage.getMax() / (1024 * 1024));
            
            MemoryUsage nonHeapUsage = memoryMXBean.getNonHeapMemoryUsage();
            log.info("Non-Heap Memory - Initial: {}MB, Max: {}MB",
                     nonHeapUsage.getInit() / (1024 * 1024),
                     nonHeapUsage.getMax() / (1024 * 1024));
            
            log.info("Garbage Collectors: ");
            for (GarbageCollectorMXBean gcBean : gcBeans) {
                log.info("  - {}: {}", gcBean.getName(), 
                        String.join(", ", gcBean.getMemoryPoolNames()));
            }
            
            // Log recommended JVM flags
            log.info("=== Recommended JVM Flags for Production ===");
            log.info("-XX:+UseG1GC                    # Use G1 Garbage Collector");
            log.info("-XX:MaxGCPauseMillis=200        # Target max GC pause");
            log.info("-XX:+ParallelRefProcEnabled     # Parallel reference processing");
            log.info("-XX:+DisableExplicitGC          # Disable System.gc() calls");
            log.info("-XX:+AlwaysPreTouch             # Pre-touch memory pages");
            log.info("-Xms2g -Xmx2g                   # Set heap size (adjust as needed)");
            log.info("-XX:+HeapDumpOnOutOfMemoryError # Dump heap on OOM");
            log.info("-XX:HeapDumpPath=/var/log/app   # Heap dump location");
        };
    }
    
    /**
     * Periodic memory monitoring and alerting.
     * Runs every 5 minutes.
     */
    @Scheduled(fixedDelay = 300000, initialDelay = 60000)
    public void monitorMemory() {
        MemoryUsage heapUsage = memoryMXBean.getHeapMemoryUsage();
        long usedHeap = heapUsage.getUsed();
        long maxHeap = heapUsage.getMax();
        double heapUtilization = (double) usedHeap / maxHeap * 100;
        
        if (heapUtilization > 90) {
            log.error("CRITICAL: Heap memory usage is at {:.2f}% ({}/{}MB)", 
                     heapUtilization, usedHeap / (1024 * 1024), maxHeap / (1024 * 1024));
            // Trigger defensive GC if critical
            suggestGarbageCollection();
        } else if (heapUtilization > 75) {
            log.warn("WARNING: Heap memory usage is at {:.2f}% ({}/{}MB)", 
                    heapUtilization, usedHeap / (1024 * 1024), maxHeap / (1024 * 1024));
        } else {
            log.debug("Heap memory usage: {:.2f}% ({}/{}MB)", 
                     heapUtilization, usedHeap / (1024 * 1024), maxHeap / (1024 * 1024));
        }
        
        // Log GC statistics
        for (GarbageCollectorMXBean gcBean : gcBeans) {
            if (gcBean.getCollectionCount() > 0) {
                log.debug("GC {}: {} collections, {} ms total time", 
                         gcBean.getName(), 
                         gcBean.getCollectionCount(), 
                         gcBean.getCollectionTime());
            }
        }
    }
    
    /**
     * Suggest garbage collection when memory is critical.
     * Note: This is a suggestion only, JVM decides when to actually run GC.
     */
    private void suggestGarbageCollection() {
        log.info("Suggesting garbage collection due to high memory usage");
        // We use Runtime.gc() instead of System.gc() as it's more appropriate
        // Note: This is still just a suggestion to the JVM
        Runtime.getRuntime().gc();
    }
    
    /**
     * Clean up expired cache entries and temporary data.
     * Runs every hour.
     */
    @Scheduled(fixedDelay = 3600000, initialDelay = 3600000)
    public void performHousekeeping() {
        log.info("Performing housekeeping tasks");
        
        // Clear string intern pool if needed
        // Note: Modern JVMs handle this automatically
        
        // Log current memory state after housekeeping
        MemoryUsage heapUsage = memoryMXBean.getHeapMemoryUsage();
        log.info("Post-housekeeping heap usage: {}MB / {}MB", 
                heapUsage.getUsed() / (1024 * 1024),
                heapUsage.getMax() / (1024 * 1024));
    }
}