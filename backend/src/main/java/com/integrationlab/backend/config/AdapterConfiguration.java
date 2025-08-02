package com.integrationlab.backend.config;

import com.integrationlab.adapters.config.HttpSenderAdapterConfig;
import com.integrationlab.adapters.factory.AdapterFactory;
import com.integrationlab.adapters.factory.DefaultAdapterFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Spring Configuration for Adapter Beans
 */
@Configuration
public class AdapterConfiguration {

    /**
     * Provides the default AdapterFactory bean
     */
    @Bean
    public AdapterFactory adapterFactory() {
        return new DefaultAdapterFactory();
    }

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