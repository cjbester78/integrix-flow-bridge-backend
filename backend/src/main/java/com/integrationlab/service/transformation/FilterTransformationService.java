package com.integrationlab.service.transformation;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.integrationlab.shared.dto.transformation.FilterTransformationConfigDTO;

import org.springframework.stereotype.Service;

import javax.script.Invocable;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import java.util.Map;

@Service
public class FilterTransformationService {

    private final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * Applies a filter transformation on input JSON array string.
     *
     * @param inputJson Input JSON string (expected to be an array of JSON objects)
     * @param config    Filter configuration DTO containing the filter expression
     * @return Filtered JSON string with only elements passing the filter
     */
    public String applyFilter(String inputJson, FilterTransformationConfigDTO config) {
        try {
            JsonNode arrayNode = objectMapper.readTree(inputJson);
            if (!arrayNode.isArray()) {
                throw new IllegalArgumentException("Input JSON must be an array");
            }

            ScriptEngine engine = new ScriptEngineManager().getEngineByName("nashorn");
            String script = "var filterFunc = " + config.getFilterExpression() + ";";
            engine.eval(script);
            Invocable invocable = (Invocable) engine;

            var filteredArray = objectMapper.createArrayNode();

            for (JsonNode item : arrayNode) {
                Map<String, Object> itemMap = objectMapper.convertValue(item, Map.class);
                Object result = invocable.invokeFunction("filterFunc", itemMap);
                if (result instanceof Boolean && (Boolean) result) {
                    filteredArray.add(item);
                }
            }

            return objectMapper.writeValueAsString(filteredArray);

        } catch (Exception e) {
            throw new RuntimeException("Failed to apply filter transformation", e);
        }
    }
}
