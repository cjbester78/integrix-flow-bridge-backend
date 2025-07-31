package com.integrationlab.shared.dto.adapter;

/**
 * DTO for AdapterTestResultDTO.
 * Encapsulates data for transport between layers.
 */
public class AdapterTestResultDTO {
    private String adapter;
    private boolean success;
    private String message;

    public AdapterTestResultDTO() {}

    public AdapterTestResultDTO(String adapter, boolean success, String message) {
        this.adapter = adapter;
        this.success = success;
        this.message = message;
    }

    public String getAdapter() { return adapter; }
    public void setAdapter(String adapter) { this.adapter = adapter; }

    public boolean isSuccess() { return success; }
    public void setSuccess(boolean success) { this.success = success; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
}
