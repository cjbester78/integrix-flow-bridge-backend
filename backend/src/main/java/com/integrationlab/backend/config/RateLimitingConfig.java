package com.integrationlab.backend.config;

import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.Refill;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Rate limiting configuration to prevent API abuse and DoS attacks.
 * 
 * <p>Implements token bucket algorithm for efficient rate limiting
 * with memory-aware cleanup.
 * 
 * @author Integration Team
 * @since 1.0.0
 */
@Configuration
public class RateLimitingConfig implements WebMvcConfigurer {
    
    /**
     * Rate limiter service with memory management.
     */
    @Bean
    public RateLimiterService rateLimiterService() {
        return new RateLimiterService();
    }
    
    /**
     * Rate limiting interceptor.
     */
    @Bean
    public RateLimitingInterceptor rateLimitingInterceptor(RateLimiterService rateLimiterService) {
        return new RateLimitingInterceptor(rateLimiterService);
    }
    
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(rateLimitingInterceptor(rateLimiterService()))
                .addPathPatterns("/api/**")
                .excludePathPatterns(
                    "/api/auth/login", 
                    "/api/auth/register",
                    "/api/system/logs/batch",  // Exclude batch logging endpoint
                    "/api/auth/profile"        // Exclude profile endpoint for auth checks
                );
    }
    
    /**
     * Service for managing rate limit buckets with automatic cleanup.
     */
    public static class RateLimiterService {
        
        // Use WeakHashMap alternative with explicit cleanup
        private final Map<String, BucketInfo> buckets = new ConcurrentHashMap<>();
        private static final long CLEANUP_INTERVAL = 300_000; // 5 minutes
        private static final long BUCKET_EXPIRY = 600_000; // 10 minutes
        
        public RateLimiterService() {
            // Schedule periodic cleanup to prevent memory leaks
            scheduleCleanup();
        }
        
        /**
         * Get or create a rate limit bucket for a client.
         */
        public Bucket resolveBucket(String key, int capacity, int refillTokens, Duration refillPeriod) {
            BucketInfo bucketInfo = buckets.compute(key, (k, existing) -> {
                if (existing != null && !existing.isExpired()) {
                    existing.updateLastAccess();
                    return existing;
                }
                
                Bandwidth limit = Bandwidth.classic(capacity, 
                    Refill.intervally(refillTokens, refillPeriod));
                Bucket bucket = Bucket.builder()
                    .addLimit(limit)
                    .build();
                
                return new BucketInfo(bucket);
            });
            
            return bucketInfo.getBucket();
        }
        
        /**
         * Schedule periodic cleanup of expired buckets.
         */
        private void scheduleCleanup() {
            Thread cleanupThread = new Thread(() -> {
                while (!Thread.currentThread().isInterrupted()) {
                    try {
                        Thread.sleep(CLEANUP_INTERVAL);
                        cleanupExpiredBuckets();
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                        break;
                    }
                }
            }, "RateLimiter-Cleanup");
            
            cleanupThread.setDaemon(true);
            cleanupThread.start();
        }
        
        /**
         * Remove expired buckets to free memory.
         */
        private void cleanupExpiredBuckets() {
            int removed = 0;
            for (Map.Entry<String, BucketInfo> entry : buckets.entrySet()) {
                if (entry.getValue().isExpired()) {
                    buckets.remove(entry.getKey());
                    removed++;
                }
            }
            if (removed > 0) {
                // Log cleanup for monitoring
                System.out.println("Rate limiter cleanup: removed " + removed + " expired buckets");
            }
        }
        
        /**
         * Wrapper for bucket with timestamp for cleanup.
         */
        private static class BucketInfo {
            private final Bucket bucket;
            private volatile long lastAccessTime;
            
            public BucketInfo(Bucket bucket) {
                this.bucket = bucket;
                this.lastAccessTime = System.currentTimeMillis();
            }
            
            public Bucket getBucket() {
                return bucket;
            }
            
            public void updateLastAccess() {
                this.lastAccessTime = System.currentTimeMillis();
            }
            
            public boolean isExpired() {
                return System.currentTimeMillis() - lastAccessTime > BUCKET_EXPIRY;
            }
        }
    }
}