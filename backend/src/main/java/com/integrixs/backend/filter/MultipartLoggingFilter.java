package com.integrixs.backend.filter;

import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * Filter to log multipart requests for debugging.
 */
@Slf4j
@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class MultipartLoggingFilter implements Filter {
    
    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) 
            throws IOException, ServletException {
        
        HttpServletRequest httpRequest = (HttpServletRequest) request;
        
        // Log multipart requests
        String contentType = httpRequest.getContentType();
        if (contentType != null && contentType.toLowerCase().startsWith("multipart/")) {
            log.info("=== Multipart Request Debug ===");
            log.info("URL: {}", httpRequest.getRequestURL());
            log.info("Method: {}", httpRequest.getMethod());
            log.info("Content-Type: {}", contentType);
            log.info("Content-Length: {}", httpRequest.getContentLength());
            log.info("Character Encoding: {}", httpRequest.getCharacterEncoding());
            
            // Extract boundary from content type
            String boundary = extractBoundary(contentType);
            if (boundary != null) {
                log.info("Multipart Boundary: {}", boundary);
            }
            
            // Count approximate number of parts based on content
            int partCount = countParts(contentType, httpRequest.getContentLength());
            log.info("Estimated number of parts: {}", partCount);
            
            // Log headers
            httpRequest.getHeaderNames().asIterator().forEachRemaining(headerName -> {
                log.info("Header {}: {}", headerName, httpRequest.getHeader(headerName));
            });
            
            log.info("=== End Multipart Debug ===");
        }
        
        try {
            chain.doFilter(request, response);
        } catch (Exception e) {
            if (contentType != null && contentType.toLowerCase().startsWith("multipart/")) {
                log.error("Error processing multipart request: {}", e.getMessage());
                log.error("Exception type: {}", e.getClass().getName());
                if (e.getCause() != null) {
                    log.error("Root cause: {}", e.getCause().getMessage());
                }
            }
            throw e;
        }
    }
    
    private String extractBoundary(String contentType) {
        if (contentType == null) return null;
        
        String[] parts = contentType.split(";");
        for (String part : parts) {
            String trimmed = part.trim();
            if (trimmed.startsWith("boundary=")) {
                return trimmed.substring(9).replaceAll("\"", "");
            }
        }
        return null;
    }
    
    private int countParts(String contentType, long contentLength) {
        // Rough estimate based on average part size
        if (contentLength > 0) {
            // Assume average part overhead of ~200 bytes per file
            return (int) (contentLength / 2000); // Very rough estimate
        }
        return -1;
    }
}