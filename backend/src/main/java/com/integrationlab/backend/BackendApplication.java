package com.integrationlab.backend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication
@ComponentScan(basePackages = {"com.integrationlab", "com.integrationlab.backend"})
@EnableJpaRepositories(basePackages = {"com.integrationlab.data.repository", "com.integrationlab.monitoring.repository"})
@EntityScan(basePackages = {"com.integrationlab.data.model", "com.integrationlab.monitoring.model"})
public class BackendApplication {

    public static void main(String[] args) {
        SpringApplication.run(BackendApplication.class, args);
    }
}