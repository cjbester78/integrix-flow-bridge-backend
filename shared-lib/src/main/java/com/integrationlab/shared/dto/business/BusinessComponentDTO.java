package com.integrationlab.shared.dto.business;

import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * DTO for business component information.
 * 
 * <p>Represents a business entity or department that owns
 * integration flows and adapters within the system.
 * 
 * @author Integration Team
 * @since 1.0.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class BusinessComponentDTO {
    
    /**
     * Unique identifier for the business component
     */
    private String id;
    
    /**
     * Name of the business component
     */
    @NotBlank(message = "Business component name is required")
    @Size(min = 3, max = 100, message = "Name must be between 3 and 100 characters")
    private String name;
    
    /**
     * Description of the business component's purpose
     */
    @Size(max = 500, message = "Description cannot exceed 500 characters")
    private String description;
    
    /**
     * Primary contact email for this business component
     */
    @Email(message = "Invalid email format")
    @NotBlank(message = "Contact email is required")
    private String contactEmail;
    
    /**
     * Contact phone number
     */
    @Pattern(regexp = "^\\+?[1-9]\\d{1,14}$", message = "Invalid phone number format")
    private String contactPhone;
    
    /**
     * Timestamp when the component was created
     */
    private LocalDateTime createdAt;
    
    /**
     * Timestamp when the component was last updated
     */
    private LocalDateTime updatedAt;
}