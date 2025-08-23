package com.integrixs.backend.config;

import io.github.bucket4j.Bucket;
import io.github.bucket4j.ConsumptionProbe;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.servlet.HandlerInterceptor;

import java.time.Duration;

/**
 * Interceptor for rate limiting API requests.
 * 
 * <p>Implements per-IP and per-user rate limiting with
 * different limits based on authentication status.
 * 
 * @author Integration Team
 * @since 1.0.0
 */
@Slf4j
@RequiredArgsConstructor
public class RateLimitingInterceptor implements HandlerInterceptor {
    
    private final RateLimitingConfig.RateLimiterService rateLimiterService;
    
    // Rate limit configurations
    private static final int ANONYMOUS_CAPACITY = 20;
    private static final int ANONYMOUS_REFILL = 20;
    private static final Duration ANONYMOUS_PERIOD = Duration.ofMinutes(1);
    
    private static final int AUTHENTICATED_CAPACITY = 100;
    private static final int AUTHENTICATED_REFILL = 100;
    private static final Duration AUTHENTICATED_PERIOD = Duration.ofMinutes(1);
    
    private static final int ADMIN_CAPACITY = 1000;
    private static final int ADMIN_REFILL = 1000;
    private static final Duration ADMIN_PERIOD = Duration.ofMinutes(1);
    
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, 
                           Object handler) throws Exception {
        
        String key = resolveKey(request);
        Bucket bucket = resolveBucket(key, request);
        
        ConsumptionProbe probe = bucket.tryConsumeAndReturnRemaining(1);
        
        if (probe.isConsumed()) {
            // Add rate limit headers
            response.addHeader("X-Rate-Limit-Limit", String.valueOf(bucket.getAvailableTokens() + 1));
            response.addHeader("X-Rate-Limit-Remaining", String.valueOf(probe.getRemainingTokens()));
            response.addHeader("X-Rate-Limit-Reset", 
                String.valueOf(System.currentTimeMillis() + probe.getNanosToWaitForRefill() / 1_000_000));
            return true;
        } else {
            // Rate limit exceeded
            long waitForRefill = probe.getNanosToWaitForRefill() / 1_000_000_000;
            
            response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
            response.addHeader("X-Rate-Limit-Retry-After-Seconds", String.valueOf(waitForRefill));
            response.setContentType("application/json");
            response.getWriter().write(String.format(
                "{\"error\":\"Rate limit exceeded\",\"message\":\"Too many requests. Please retry after %d seconds\"}",
                waitForRefill
            ));
            
            log.warn("Rate limit exceeded for key: {}", key);
            return false;
        }
    }
    
    /**
     * Resolve the rate limiting key based on user identity.
     */
    private String resolveKey(HttpServletRequest request) {
        // First try to get authenticated user
        String username = request.getRemoteUser();
        if (username != null) {
            return "user:" + username;
        }
        
        // Fall back to IP address
        String clientIp = getClientIp(request);
        return "ip:" + clientIp;
    }
    
    /**
     * Get the real client IP considering proxies.
     */
    private String getClientIp(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
            return xForwardedFor.split(",")[0].trim();
        }
        
        String xRealIp = request.getHeader("X-Real-IP");
        if (xRealIp != null && !xRealIp.isEmpty()) {
            return xRealIp;
        }
        
        return request.getRemoteAddr();
    }
    
    /**
     * Resolve the appropriate bucket based on user role.
     */
    private Bucket resolveBucket(String key, HttpServletRequest request) {
        // Check if admin
        if (request.isUserInRole("ADMINISTRATOR")) {
            return rateLimiterService.resolveBucket(key, 
                ADMIN_CAPACITY, ADMIN_REFILL, ADMIN_PERIOD);
        }
        
        // Check if authenticated
        if (request.getRemoteUser() != null) {
            return rateLimiterService.resolveBucket(key, 
                AUTHENTICATED_CAPACITY, AUTHENTICATED_REFILL, AUTHENTICATED_PERIOD);
        }
        
        // Anonymous user
        return rateLimiterService.resolveBucket(key, 
            ANONYMOUS_CAPACITY, ANONYMOUS_REFILL, ANONYMOUS_PERIOD);
    }
}