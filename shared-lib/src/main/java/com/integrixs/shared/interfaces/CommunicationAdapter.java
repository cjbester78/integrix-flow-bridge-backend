package com.integrixs.shared.interfaces;

import com.integrixs.shared.dto.adapter.AdapterTestResultDTO;

/**
 * Common interface for all communication adapters.
 * Implementations should validate their connectivity or functionality
 * by returning a standardized AdapterTestResultDTO.
 */
public interface CommunicationAdapter {

    /**
     * Executes a test operation using the given payload.
     *
     * @param payload The test payload.
     * @return The result of the adapter test.
     */
    AdapterTestResultDTO test(String payload);

    /**
     * Returns the strongly-typed configuration for this adapter.
     * @param configClass The class object of the expected config type
     * @param <T> The type of the configuration
     * @return The configuration object cast to the given type
     */
    <T> T getConfig(Class<T> configClass);
}
