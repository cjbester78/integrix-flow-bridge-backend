package com.integrationlab.config;

import com.integrationlab.adapters.config.HttpSenderAdapterConfig;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Spring Configuration for Adapter Beans
 */
@Configuration
public class AdapterConfiguration {

    /**
     * Provides a default HttpSenderAdapterConfig bean for the HTTP adapter controller
     */
    @Bean
    public HttpSenderAdapterConfig httpSenderAdapterConfig() {
        HttpSenderAdapterConfig config = new HttpSenderAdapterConfig();
        // Set default values for development
        config.setEndpointUrl("/api/http-adapter/receive");
        config.setConnectionTimeout(30);
        config.setReadTimeout(60);
        config.setValidateIncomingData(true);
        return config;
    }
}