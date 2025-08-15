package com.integrixs.backend.dto;

import lombok.Builder;
import lombok.Data;
import java.util.List;

@Data
@Builder
public class XsdValidationResult {
    private String fileName;
    private boolean valid;
    private List<String> errors;
    private List<String> dependencies;
    private List<String> resolvedDependencies;
    private List<String> missingDependencies;
}