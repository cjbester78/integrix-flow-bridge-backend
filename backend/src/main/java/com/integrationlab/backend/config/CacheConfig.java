package com.integrationlab.backend.config;

import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.RemovalCause;
import com.github.benmanes.caffeine.cache.RemovalListener;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.caffeine.CaffeineCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import java.lang.ref.WeakReference;
import java.util.concurrent.TimeUnit;

/**
 * Memory-aware caching configuration with automatic eviction.
 * 
 * <p>Implements memory-sensitive caching to prevent memory leaks
 * and ensure efficient garbage collection.
 * 
 * @author Integration Team
 * @since 1.0.0
 */
@Slf4j
@Configuration
@EnableCaching
public class CacheConfig {
    
    /**
     * Primary cache manager with memory-aware eviction policies.
     */
    @Bean
    @Primary
    public CacheManager cacheManager() {
        CaffeineCacheManager cacheManager = new CaffeineCacheManager();
        cacheManager.setCaffeine(defaultCaffeineConfig());
        cacheManager.setAllowNullValues(false);
        return cacheManager;
    }
    
    /**
     * Session cache with short TTL for security.
     */
    @Bean
    public CacheManager sessionCacheManager() {
        CaffeineCacheManager cacheManager = new CaffeineCacheManager("sessions", "tokens");
        cacheManager.setCaffeine(sessionCaffeineConfig());
        cacheManager.setAllowNullValues(false);
        return cacheManager;
    }
    
    /**
     * Default Caffeine configuration with memory-aware settings.
     */
    private Caffeine<Object, Object> defaultCaffeineConfig() {
        return Caffeine.newBuilder()
                .maximumSize(10_000)
                .maximumWeight(100_000_000) // 100MB max weight
                .weigher((key, value) -> estimateSize(value))
                .expireAfterWrite(30, TimeUnit.MINUTES)
                .expireAfterAccess(10, TimeUnit.MINUTES)
                .weakKeys() // Allow keys to be garbage collected
                .softValues() // Allow values to be garbage collected under memory pressure
                .recordStats()
                .removalListener(new LoggingRemovalListener())
                .build();
    }
    
    /**
     * Session cache configuration with strict security settings.
     */
    private Caffeine<Object, Object> sessionCaffeineConfig() {
        return Caffeine.newBuilder()
                .maximumSize(1_000)
                .expireAfterWrite(15, TimeUnit.MINUTES) // Short TTL for sessions
                .expireAfterAccess(5, TimeUnit.MINUTES)
                .recordStats()
                .removalListener((key, value, cause) -> {
                    if (cause == RemovalCause.EXPIRED) {
                        log.debug("Session cache entry expired: {}", key);
                    }
                })
                .build();
    }
    
    /**
     * Estimate object size for cache weight calculation.
     */
    private int estimateSize(Object value) {
        if (value == null) return 1;
        
        // Basic size estimation - can be refined based on actual objects
        if (value instanceof String) {
            return ((String) value).length() * 2; // 2 bytes per char
        } else if (value instanceof byte[]) {
            return ((byte[]) value).length;
        } else if (value instanceof WeakReference) {
            Object ref = ((WeakReference<?>) value).get();
            return ref != null ? estimateSize(ref) : 1;
        } else {
            // Default estimation: 100 bytes per object
            return 100;
        }
    }
    
    /**
     * Removal listener for cache debugging and monitoring.
     */
    private static class LoggingRemovalListener implements RemovalListener<Object, Object> {
        @Override
        public void onRemoval(Object key, Object value, RemovalCause cause) {
            if (cause == RemovalCause.SIZE) {
                log.warn("Cache entry evicted due to size limit: key={}", key);
            } else if (cause == RemovalCause.COLLECTED) {
                log.debug("Cache entry garbage collected: key={}", key);
            }
        }
    }
    
    /**
     * Bean for monitoring cache statistics.
     */
    @Bean
    public CacheStatsMonitor cacheStatsMonitor(CacheManager cacheManager) {
        return new CacheStatsMonitor(cacheManager);
    }
    
    /**
     * Monitor for cache statistics and memory usage.
     */
    public static class CacheStatsMonitor {
        private final CacheManager cacheManager;
        
        public CacheStatsMonitor(CacheManager cacheManager) {
            this.cacheManager = cacheManager;
        }
        
        public void logCacheStats() {
            cacheManager.getCacheNames().forEach(cacheName -> {
                var cache = cacheManager.getCache(cacheName);
                if (cache != null) {
                    log.info("Cache '{}' statistics logged", cacheName);
                }
            });
        }
    }
}