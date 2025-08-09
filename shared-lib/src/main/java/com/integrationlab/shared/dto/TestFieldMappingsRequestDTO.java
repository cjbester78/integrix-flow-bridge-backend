package com.integrationlab.shared.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Request DTO for testing field mappings
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TestFieldMappingsRequestDTO {
    
    private String inputXml;
    private List<TestMappingDTO> mappings;
    private String mappingType; // request, response, or fault
    private String sourceStructureXml;
    private String targetStructureXml;
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TestMappingDTO {
        private List<String> sourceFields;
        private String targetField;
        private List<String> sourcePaths;
        private String targetPath;
        private String javaFunction;
        private Object functionNode;
        private boolean requiresTransformation;
        private VisualFlowData visualFlowData;
    }
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class VisualFlowData {
        private List<VisualFlowNode> nodes;
        private List<VisualFlowEdge> edges;
        private int nodeIdCounter;
    }
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class VisualFlowNode {
        private String id;
        private String type;
        private Object data;
        private Position position;
    }
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class VisualFlowEdge {
        private String id;
        private String source;
        private String target;
        private String type;
        private String sourceHandle;
        private String targetHandle;
    }
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Position {
        private double x;
        private double y;
    }
}