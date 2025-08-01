package com.integrationlab.shared.dto.log;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * DTO for submitting a batch of frontend log entries.
 * Used for efficient bulk logging from the frontend.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FrontendLogBatchRequest {
    
    /**
     * List of log entries to submit
     */
    @NotNull
    @Size(min = 1, max = 100, message = "Batch size must be between 1 and 100")
    @Valid
    private List<FrontendLogEntry> logs;
}