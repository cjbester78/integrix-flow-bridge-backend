package com.integrixs.backend.config;

import org.flywaydb.core.Flyway;
import org.springframework.boot.autoconfigure.flyway.FlywayMigrationStrategy;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Flyway configuration for database migrations.
 * Handles automatic database schema migration on application startup.
 */
@Configuration
public class FlywayConfig {

    private static final Logger logger = LoggerFactory.getLogger(FlywayConfig.class);

    /**
     * Custom Flyway migration strategy with enhanced logging.
     */
    @Bean
    public FlywayMigrationStrategy flywayMigrationStrategy() {
        return flyway -> {
            logger.info("Starting Flyway migration...");
            logger.info("Flyway baseline version: {}", flyway.getConfiguration().getBaselineVersion());
            
            var locations = flyway.getConfiguration().getLocations();
            if (locations != null && locations.length > 0) {
                var locationStrings = new String[locations.length];
                for (int i = 0; i < locations.length; i++) {
                    locationStrings[i] = locations[i].toString();
                }
                logger.info("Flyway locations: {}", String.join(", ", locationStrings));
            }
            
            try {
                // Repair the schema history table if needed
                flyway.repair();
                
                // Run the migrations
                var result = flyway.migrate();
                
                logger.info("Flyway migration completed successfully!");
                logger.info("Initial schema version: {}", result.initialSchemaVersion);
                logger.info("Target schema version: {}", result.targetSchemaVersion);
                logger.info("Migrations executed: {}", result.migrationsExecuted);
                logger.info("Success: {}", result.success);
                
                if (!result.success) {
                    logger.error("Flyway migration failed!");
                    throw new RuntimeException("Database migration failed");
                }
                
            } catch (Exception e) {
                logger.error("Error during Flyway migration: {}", e.getMessage(), e);
                throw new RuntimeException("Failed to migrate database", e);
            }
        };
    }
}