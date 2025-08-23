package com.integrixs.backend.config;

import org.apache.catalina.connector.Connector;
import org.apache.coyote.http11.AbstractHttp11Protocol;
import org.springframework.boot.web.embedded.tomcat.TomcatServletWebServerFactory;
import org.springframework.boot.web.server.WebServerFactoryCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import lombok.extern.slf4j.Slf4j;

/**
 * Tomcat-specific configuration for handling large multipart requests.
 */
@Configuration
@Slf4j
public class TomcatConfig {
    
    @Bean
    public WebServerFactoryCustomizer<TomcatServletWebServerFactory> tomcatCustomizer() {
        return factory -> {
            factory.addConnectorCustomizers(connector -> {
                if (connector.getProtocolHandler() instanceof AbstractHttp11Protocol) {
                    AbstractHttp11Protocol<?> protocol = (AbstractHttp11Protocol<?>) connector.getProtocolHandler();
                    
                    // Note: setMaxParameterCount is not available in this version
                    // We'll use connector attributes instead
                    
                    // Increase max header size
                    protocol.setMaxHttpHeaderSize(65536); // 64KB
                    
                    // Set max swallow size
                    protocol.setMaxSwallowSize(104857600); // 100MB
                    
                    // Set connector attributes for max parameters
                    connector.setProperty("maxParameterCount", "10000");
                    
                    log.info("Configured Tomcat for large multipart requests:");
                    log.info("  - Max parameter count: 10000");
                    log.info("  - Max HTTP header size: 64KB");
                    log.info("  - Max swallow size: 100MB");
                }
            });
        };
    }
}