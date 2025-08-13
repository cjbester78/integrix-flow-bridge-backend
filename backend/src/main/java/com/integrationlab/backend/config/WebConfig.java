package com.integrationlab.backend.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.servlet.resource.PathResourceResolver;

import java.io.IOException;

@Configuration
@Order(Ordered.LOWEST_PRECEDENCE)
public class WebConfig implements WebMvcConfigurer {
    
    @Override
    public void addViewControllers(ViewControllerRegistry registry) {
        // Don't handle WebSocket paths
        registry.setOrder(Ordered.LOWEST_PRECEDENCE);
    }

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // Explicitly handle static resources first
        registry.addResourceHandler("/assets/**")
                .addResourceLocations("classpath:/public/assets/")
                .setCachePeriod(3600);
                
        registry.addResourceHandler("/*.js", "/*.css", "/*.ico", "/*.png", "/*.jpg", "/*.json")
                .addResourceLocations("classpath:/public/")
                .setCachePeriod(3600);
        
        // Handle React routes - but NOT WebSocket paths
        registry.addResourceHandler("/**")
                .addResourceLocations("classpath:/public/")
                .resourceChain(true)
                .addResolver(new PathResourceResolver() {
                    @Override
                    protected Resource getResource(String resourcePath, Resource location) throws IOException {
                        // NEVER handle WebSocket paths
                        if (resourcePath.startsWith("ws/") || 
                            resourcePath.equals("wstest") || 
                            resourcePath.equals("echo") || 
                            resourcePath.equals("test-ws") ||
                            resourcePath.equals("direct-ws") ||
                            resourcePath.equals("minimal-echo") ||
                            resourcePath.equals("basic")) {
                            return null;
                        }
                        
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