package com.integrationlab.shared.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO representing a function parameter with type information
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FunctionParameterDTO {
    
    /**
     * Parameter name (e.g., "amount", "rate")
     */
    private String name;
    
    /**
     * Java type of the parameter (e.g., "String", "int", "Double")
     */
    private String type;
    
    /**
     * Whether this parameter is required
     */
    private boolean required;
    
    /**
     * Optional description of the parameter
     */
    private String description;
}