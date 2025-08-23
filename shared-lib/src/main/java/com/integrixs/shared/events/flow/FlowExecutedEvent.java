package com.integrixs.shared.events.flow;

import com.integrixs.shared.events.AbstractDomainEvent;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.time.Duration;
import java.time.LocalDateTime;

/**
 * Event raised when an integration flow is executed.
 * 
 * @author Integration Team
 * @since 1.0.0
 */
@Data
@SuperBuilder
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class FlowExecutedEvent extends AbstractDomainEvent {
    
    private String flowId;
    private String executionId;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private boolean success;
    private String errorMessage;
    private Long recordsProcessed;
    private String triggeredBy;
    
    public FlowExecutedEvent(String flowId, String executionId, LocalDateTime startTime,
                            LocalDateTime endTime, boolean success, String triggeredBy) {
        super(flowId, triggeredBy);
        this.flowId = flowId;
        this.executionId = executionId;
        this.startTime = startTime;
        this.endTime = endTime;
        this.success = success;
        this.triggeredBy = triggeredBy;
    }
    
    /**
     * Gets the execution duration.
     * 
     * @return duration between start and end time
     */
    public Duration getExecutionDuration() {
        if (startTime != null && endTime != null) {
            return Duration.between(startTime, endTime);
        }
        return Duration.ZERO;
    }
}