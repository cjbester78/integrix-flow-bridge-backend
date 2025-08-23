package com.integrixs.backend.config;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.multipart.MultipartException;
import org.springframework.web.multipart.MultipartHttpServletRequest;
import org.springframework.web.multipart.support.StandardServletMultipartResolver;

/**
 * Custom multipart resolver with enhanced error handling and logging.
 */
@Slf4j
public class CustomMultipartResolver extends StandardServletMultipartResolver {
    
    @Override
    public boolean isMultipart(HttpServletRequest request) {
        boolean isMultipart = super.isMultipart(request);
        if (isMultipart) {
            log.debug("Detected multipart request: {}", request.getRequestURI());
        }
        return isMultipart;
    }
    
    @Override
    public MultipartHttpServletRequest resolveMultipart(HttpServletRequest request) throws MultipartException {
        try {
            log.debug("Resolving multipart request: {}", request.getRequestURI());
            log.debug("Content-Type: {}", request.getContentType());
            log.debug("Content-Length: {}", request.getContentLength());
            
            MultipartHttpServletRequest multipartRequest = super.resolveMultipart(request);
            
            // Log successful resolution
            if (multipartRequest != null) {
                log.debug("Successfully resolved multipart request with {} files", 
                    multipartRequest.getFileMap().size());
                multipartRequest.getFileMap().forEach((name, file) -> {
                    log.debug("  - Parameter '{}': {} ({} bytes)", 
                        name, file.getOriginalFilename(), file.getSize());
                });
            }
            
            return multipartRequest;
        } catch (Exception e) {
            log.error("Failed to resolve multipart request", e);
            throw new MultipartException("Failed to parse multipart request: " + e.getMessage(), e);
        }
    }
}