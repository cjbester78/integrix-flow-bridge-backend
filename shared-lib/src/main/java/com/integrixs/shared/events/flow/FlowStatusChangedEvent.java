package com.integrixs.shared.events.flow;

import com.integrixs.shared.events.AbstractDomainEvent;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

/**
 * Event raised when an integration flow status changes.
 * 
 * @author Integration Team
 * @since 1.0.0
 */
@Data
@SuperBuilder
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class FlowStatusChangedEvent extends AbstractDomainEvent {
    
    private String flowId;
    private String oldStatus;
    private String newStatus;
    private String reason;
    private String changedBy;
    
    public FlowStatusChangedEvent(String flowId, String oldStatus, String newStatus,
                                 String reason, String changedBy) {
        super(flowId, changedBy);
        this.flowId = flowId;
        this.oldStatus = oldStatus;
        this.newStatus = newStatus;
        this.reason = reason;
        this.changedBy = changedBy;
    }
    
    /**
     * Checks if the flow was activated.
     * 
     * @return true if flow changed to active status
     */
    public boolean isActivation() {
        return "ACTIVE".equals(newStatus) && !"ACTIVE".equals(oldStatus);
    }
    
    /**
     * Checks if the flow was deactivated.
     * 
     * @return true if flow changed from active status
     */
    public boolean isDeactivation() {
        return "ACTIVE".equals(oldStatus) && !"ACTIVE".equals(newStatus);
    }
}