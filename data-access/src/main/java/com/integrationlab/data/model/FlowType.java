package com.integrationlab.data.model;

/**
 * Enumeration representing the type of integration flow.
 */
public enum FlowType {
    /**
     * Direct mapping flow - simple source to target mapping
     */
    DIRECT_MAPPING,
    
    /**
     * Orchestration flow - complex flow with multiple steps
     */
    ORCHESTRATION
}