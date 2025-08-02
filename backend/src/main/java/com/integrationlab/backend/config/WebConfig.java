package com.integrationlab.backend.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.servlet.resource.PathResourceResolver;

import java.io.IOException;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // Handle React routes - serve index.html for all non-static routes
        registry.addResourceHandler("/**")
                .addResourceLocations("classpath:/public/", "classpath:/static/")
                .resourceChain(true)
                .addResolver(new PathResourceResolver() {
                    @Override
                    protected Resource getResource(String resourcePath, Resource location) throws IOException {
                        Resource requestedResource = location.createRelative(resourcePath);
                        
                        // If the resource exists and is readable, serve it
                        if (requestedResource.exists() && requestedResource.isReadable()) {
                            return requestedResource;
                        }
                        
                        // Skip API routes, actuator, and other backend routes
                        if (resourcePath.startsWith("api/") || 
                            resourcePath.startsWith("actuator/") ||
                            resourcePath.startsWith("swagger-ui/") ||
                            resourcePath.startsWith("v3/api-docs") ||
                            resourcePath.startsWith("webjars/")) {
                            return null;
                        }
                        
                        // For all other routes, return index.html (React app)
                        return new ClassPathResource("/public/index.html");
                    }
                });
    }
}