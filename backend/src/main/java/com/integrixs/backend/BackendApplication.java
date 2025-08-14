package com.integrixs.backend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication
@ComponentScan(basePackages = {"com.integrixs", "com.integrixs.backend"})
@EnableJpaRepositories(basePackages = {"com.integrixs.data.repository", "com.integrixs.monitoring.repository"})
@EntityScan(basePackages = {"com.integrixs.data.model", "com.integrixs.monitoring.model"})
public class BackendApplication {

    public static void main(String[] args) {
        SpringApplication.run(BackendApplication.class, args);
    }
}