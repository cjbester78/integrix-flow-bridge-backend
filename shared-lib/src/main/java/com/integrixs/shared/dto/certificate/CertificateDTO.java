package com.integrixs.shared.dto.certificate;

import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * DTO for certificate information.
 * 
 * <p>Represents SSL/TLS certificates used for secure communications
 * in adapters and integrations.
 * 
 * @author Integration Team
 * @since 1.0.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class CertificateDTO {
    
    /**
     * Unique identifier for the certificate
     */
    private String id;
    
    /**
     * Certificate name/alias
     */
    @NotBlank(message = "Certificate name is required")
    @Size(min = 3, max = 100, message = "Name must be between 3 and 100 characters")
    private String name;
    
    /**
     * Certificate format (PEM, DER, PKCS12, JKS)
     */
    @NotBlank(message = "Certificate format is required")
    @Pattern(regexp = "^(PEM|DER|PKCS12|JKS)$", 
             message = "Format must be PEM, DER, PKCS12, or JKS")
    private String format;
    
    /**
     * Certificate type (SERVER, CLIENT, CA, SELF_SIGNED)
     */
    @NotBlank(message = "Certificate type is required")
    @Pattern(regexp = "^(SERVER|CLIENT|CA|SELF_SIGNED)$", 
             message = "Type must be SERVER, CLIENT, CA, or SELF_SIGNED")
    private String type;
    
    /**
     * Username of who uploaded the certificate
     */
    @NotBlank(message = "Uploaded by is required")
    private String uploadedBy;
    
    /**
     * Timestamp when certificate was uploaded
     */
    private LocalDateTime uploadedAt;
    
    /**
     * Certificate expiry date
     */
    private LocalDateTime expiresAt;
    
    /**
     * Certificate issuer DN (Distinguished Name)
     */
    private String issuer;
    
    /**
     * Certificate subject DN
     */
    private String subject;
    
    /**
     * Certificate thumbprint/fingerprint
     */
    private String thumbprint;
    
    /**
     * Whether the certificate is currently valid
     */
    private Boolean isValid;
    
    /**
     * File size in bytes
     */
    private Long sizeInBytes;
}