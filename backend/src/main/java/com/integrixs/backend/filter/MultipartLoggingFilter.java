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
                log.error("Error processing multipart request", e);
            }
            throw e;
        }
    }
}