package com.integrationlab.backend.service.transformation;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.integrationlab.shared.dto.transformation.EnrichmentTransformationConfigDTO;
import com.integrationlab.backend.util.JavaFunctionRunner;
import org.springframework.stereotype.Service;

import java.util.Iterator;
import java.util.Map;

@Service
public class EnrichmentTransformationService {

    private final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * Applies enrichment on the input JSON string.
     * It adds static enrichment fields and optionally executes a JavaScript enrichment function.
     *
     * @param inputJson Input JSON string
     * @param config Enrichment configuration DTO
     * @return Enriched JSON string
     */
    public String applyEnrichment(String inputJson, EnrichmentTransformationConfigDTO config) {
        try {
            JsonNode inputNode = objectMapper.readTree(inputJson);

            if (!inputNode.isObject()) {
                throw new IllegalArgumentException("Input JSON must be an object for enrichment.");
            }

            // Convert input JSON to Map<String, Object>
            Map<String, Object> map = objectMapper.convertValue(inputNode, Map.class);

            // Add static enrichment fields if present
            Map<String, Object> enrichmentFields = config.getEnrichmentFields();
            if (enrichmentFields != null) {
                map.putAll(enrichmentFields);
            }

            // Execute enrichment function if provided
            String enrichmentFunction = config.getEnrichmentFunction();
            if (enrichmentFunction != null && !enrichmentFunction.isBlank()) {
                Object result = JavaFunctionRunner.run(
                    enrichmentFunction,
                    java.util.List.of("record"),
                    Map.of("record", map)
                );

                if (result instanceof Map<?, ?>) {
                    @SuppressWarnings("unchecked")
                    Map<String, Object> resultMap = (Map<String, Object>) result;
                    map.putAll(resultMap);
                } else if (result instanceof String) {
                    JsonNode resultNode = objectMapper.readTree((String) result);
                    if (resultNode.isObject()) {
                        Iterator<Map.Entry<String, JsonNode>> fields = resultNode.fields();
                        while (fields.hasNext()) {
                            Map.Entry<String, JsonNode> entry = fields.next();
                            map.put(entry.getKey(), objectMapper.treeToValue(entry.getValue(), Object.class));
                        }
                    }
                }
            }

            return objectMapper.writeValueAsString(map);

        } catch (Exception e) {
            throw new RuntimeException("Failed to apply enrichment transformation", e);
        }
    }
}
