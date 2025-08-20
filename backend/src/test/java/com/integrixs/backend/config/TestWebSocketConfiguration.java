package com.integrixs.backend.config;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;
import org.springframework.web.socket.server.standard.ServerEndpointExporter;
import jakarta.websocket.server.ServerContainer;

@TestConfiguration
@Profile("test")
public class TestWebSocketConfiguration {

    @MockBean
    private ServerContainer serverContainer;

    @Bean
    @Primary
    public ServerEndpointExporter serverEndpointExporter() {
        // Return a no-op ServerEndpointExporter for tests
        return new ServerEndpointExporter() {
            @Override
            public void afterPropertiesSet() {
                // Do nothing - avoid the ServletContext issue in tests
            }
        };
    }
}