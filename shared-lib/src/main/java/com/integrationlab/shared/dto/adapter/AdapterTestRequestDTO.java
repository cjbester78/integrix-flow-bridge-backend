package com.integrationlab.shared.dto.adapter;

/**
 * DTO for AdapterTestRequestDTO.
 * Encapsulates data for transport between layers.
 */
public class AdapterTestRequestDTO {
    private String adapterType;
    private String payload;

    public String getAdapterType() {
        return adapterType;
    }

    public void setAdapterType(String adapterType) {
        this.adapterType = adapterType;
    }

    public String getPayload() {
        return payload;
    }

    public void setPayload(String payload) {
        this.payload = payload;
    }
}