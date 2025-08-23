package com.integrixs.backend.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class XsdImportResult {
    private String fileName;
    private String structureName;
    private boolean success;
    private String message;
}