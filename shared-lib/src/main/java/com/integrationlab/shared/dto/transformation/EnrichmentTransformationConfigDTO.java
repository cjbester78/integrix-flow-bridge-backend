package com.integrationlab.shared.dto.transformation;

import java.util.Map;

/**
 * DTO for EnrichmentTransformationConfigDTO.
 * Encapsulates data for transport between layers.
 */
public class EnrichmentTransformationConfigDTO {

    /**
     * Map of enrichment fields and their static or dynamic values.
     * Example: { "country": "South Africa", "region": "Gauteng" }
     */
    private Map<String, Object> enrichmentFields;

    /**
     * Optionally, a JavaScript/Java function body for dynamic enrichment logic.
     */
    private String enrichmentFunction;

    public Map<String, Object> getEnrichmentFields() {
        return enrichmentFields;
    }

    public void setEnrichmentFields(Map<String, Object> enrichmentFields) {
        this.enrichmentFields = enrichmentFields;
    }

    public String getEnrichmentFunction() {
        return enrichmentFunction;
    }

    public void setEnrichmentFunction(String enrichmentFunction) {
        this.enrichmentFunction = enrichmentFunction;
    }
}