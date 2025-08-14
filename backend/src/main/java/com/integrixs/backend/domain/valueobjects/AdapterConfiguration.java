package com.integrixs.backend.domain.valueobjects;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.integrixs.shared.exceptions.ValidationException;
import lombok.Value;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Value object representing adapter configuration.
 * 
 * <p>Encapsulates and validates adapter configuration data.
 * 
 * @author Integration Team
 * @since 1.0.0
 */
@Value
public class AdapterConfiguration {
    
    Map<String, Object> properties;
    
    private static final ObjectMapper objectMapper = new ObjectMapper();
    
    /**
     * Creates a new AdapterConfiguration.
     * 
     * @param properties the configuration properties
     */
    private AdapterConfiguration(Map<String, Object> properties) {
        this.properties = Collections.unmodifiableMap(new HashMap<>(properties));
    }
    
    /**
     * Creates an AdapterConfiguration from a map.
     * 
     * @param properties the properties map
     * @return new AdapterConfiguration instance
     */
    public static AdapterConfiguration of(Map<String, Object> properties) {
        if (properties == null) {
            return new AdapterConfiguration(Collections.emptyMap());
        }
        return new AdapterConfiguration(properties);
    }
    
    /**
     * Creates an AdapterConfiguration from JSON string.
     * 
     * @param json the JSON string
     * @return new AdapterConfiguration instance
     * @throws ValidationException if JSON is invalid
     */
    public static AdapterConfiguration fromJson(String json) {
        if (json == null || json.trim().isEmpty()) {
            return new AdapterConfiguration(Collections.emptyMap());
        }
        
        try {
            Map<String, Object> properties = objectMapper.readValue(json, Map.class);
            return new AdapterConfiguration(properties);
        } catch (JsonProcessingException e) {
            throw new ValidationException("Invalid configuration JSON: " + e.getMessage());
        }
    }
    
    /**
     * Gets a property value.
     * 
     * @param key the property key
     * @return the property value or null
     */
    public Object getProperty(String key) {
        return properties.get(key);
    }
    
    /**
     * Gets a property value with type casting.
     * 
     * @param key the property key
     * @param type the expected type
     * @param <T> the type parameter
     * @return the typed value or null
     */
    @SuppressWarnings("unchecked")
    public <T> T getProperty(String key, Class<T> type) {
        Object value = properties.get(key);
        if (value == null) {
            return null;
        }
        
        if (!type.isInstance(value)) {
            throw new ValidationException(
                String.format("Property '%s' is not of type %s", key, type.getSimpleName()));
        }
        
        return (T) value;
    }
    
    /**
     * Gets a required property value.
     * 
     * @param key the property key
     * @param type the expected type
     * @param <T> the type parameter
     * @return the typed value
     * @throws ValidationException if property is missing or wrong type
     */
    public <T> T getRequiredProperty(String key, Class<T> type) {
        T value = getProperty(key, type);
        if (value == null) {
            throw new ValidationException(String.format("Required property '%s' is missing", key));
        }
        return value;
    }
    
    /**
     * Checks if a property exists.
     * 
     * @param key the property key
     * @return true if property exists
     */
    public boolean hasProperty(String key) {
        return properties.containsKey(key);
    }
    
    /**
     * Converts to JSON string.
     * 
     * @return JSON representation
     */
    public String toJson() {
        try {
            return objectMapper.writeValueAsString(properties);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Failed to serialize configuration", e);
        }
    }
    
    /**
     * Creates a builder for modifying configuration.
     * 
     * @return new builder with current properties
     */
    public Builder toBuilder() {
        return new Builder(new HashMap<>(properties));
    }
    
    /**
     * Creates a new builder.
     * 
     * @return new empty builder
     */
    public static Builder builder() {
        return new Builder();
    }
    
    /**
     * Builder for AdapterConfiguration.
     */
    public static class Builder {
        private final Map<String, Object> properties;
        
        private Builder() {
            this.properties = new HashMap<>();
        }
        
        private Builder(Map<String, Object> properties) {
            this.properties = properties;
        }
        
        public Builder property(String key, Object value) {
            properties.put(key, value);
            return this;
        }
        
        public Builder properties(Map<String, Object> props) {
            properties.putAll(props);
            return this;
        }
        
        public Builder removeProperty(String key) {
            properties.remove(key);
            return this;
        }
        
        public AdapterConfiguration build() {
            return new AdapterConfiguration(properties);
        }
    }
}