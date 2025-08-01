package com.integrationlab.shared.validation;

import com.integrationlab.shared.dto.adapter.AdapterConfigDTO;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.util.Map;

/**
 * Validator implementation for adapter configuration validation.
 * 
 * <p>Validates adapter configurations based on adapter type,
 * ensuring all required fields are present and valid.
 * 
 * @author Integration Team
 * @since 1.0.0
 */
public class AdapterConfigValidator implements ConstraintValidator<ValidAdapterConfig, AdapterConfigDTO> {
    
    @Override
    public void initialize(ValidAdapterConfig constraintAnnotation) {
        // No initialization needed
    }
    
    @Override
    public boolean isValid(AdapterConfigDTO config, ConstraintValidatorContext context) {
        if (config == null) {
            return true; // Let @NotNull handle null checks
        }
        
        // Disable default constraint violation
        context.disableDefaultConstraintViolation();
        
        boolean isValid = true;
        
        // Validate based on adapter type
        String adapterType = config.getAdapterType();
        if (adapterType != null) {
            switch (adapterType) {
                case "HTTP":
                case "HTTPS":
                    isValid = validateHttpAdapter(config, context);
                    break;
                case "JDBC":
                    isValid = validateJdbcAdapter(config, context);
                    break;
                case "FTP":
                case "SFTP":
                    isValid = validateFtpAdapter(config, context);
                    break;
                case "SOAP":
                    isValid = validateSoapAdapter(config, context);
                    break;
                case "EMAIL":
                    isValid = validateEmailAdapter(config, context);
                    break;
                case "JMS":
                    isValid = validateJmsAdapter(config, context);
                    break;
                default:
                    // Unknown adapter type is handled by @Pattern annotation
                    break;
            }
        }
        
        // Validate connection type specific fields
        if (config.getConnectionType() != null) {
            switch (config.getConnectionType()) {
                case "sender":
                    isValid = validateSenderConfig(config, context) && isValid;
                    break;
                case "receiver":
                    isValid = validateReceiverConfig(config, context) && isValid;
                    break;
            }
        }
        
        return isValid;
    }
    
    private boolean validateHttpAdapter(AdapterConfigDTO config, ConstraintValidatorContext context) {
        boolean valid = true;
        Map<String, Object> properties = config.getConnectionProperties();
        
        if (properties == null) {
            context.buildConstraintViolationWithTemplate("HTTP adapter requires connection properties")
                   .addConstraintViolation();
            return false;
        }
        
        // Required: url
        if (!properties.containsKey("url") || properties.get("url") == null || 
            properties.get("url").toString().trim().isEmpty()) {
            context.buildConstraintViolationWithTemplate("HTTP adapter requires 'url' property")
                   .addConstraintViolation();
            valid = false;
        }
        
        // Optional but validate if present: method
        if (properties.containsKey("method")) {
            String method = properties.get("method").toString().toUpperCase();
            if (!method.matches("^(GET|POST|PUT|DELETE|PATCH|HEAD|OPTIONS)$")) {
                context.buildConstraintViolationWithTemplate("Invalid HTTP method: " + method)
                       .addConstraintViolation();
                valid = false;
            }
        }
        
        return valid;
    }
    
    private boolean validateJdbcAdapter(AdapterConfigDTO config, ConstraintValidatorContext context) {
        boolean valid = true;
        Map<String, Object> properties = config.getConnectionProperties();
        
        if (properties == null) {
            context.buildConstraintViolationWithTemplate("JDBC adapter requires connection properties")
                   .addConstraintViolation();
            return false;
        }
        
        // Required fields
        String[] requiredFields = {"jdbcUrl", "username", "driverClassName"};
        for (String field : requiredFields) {
            if (!properties.containsKey(field) || properties.get(field) == null || 
                properties.get(field).toString().trim().isEmpty()) {
                context.buildConstraintViolationWithTemplate("JDBC adapter requires '" + field + "' property")
                       .addConstraintViolation();
                valid = false;
            }
        }
        
        return valid;
    }
    
    private boolean validateFtpAdapter(AdapterConfigDTO config, ConstraintValidatorContext context) {
        boolean valid = true;
        Map<String, Object> properties = config.getConnectionProperties();
        
        if (properties == null) {
            context.buildConstraintViolationWithTemplate("FTP/SFTP adapter requires connection properties")
                   .addConstraintViolation();
            return false;
        }
        
        // Required fields
        String[] requiredFields = {"host", "port", "username"};
        for (String field : requiredFields) {
            if (!properties.containsKey(field) || properties.get(field) == null || 
                properties.get(field).toString().trim().isEmpty()) {
                context.buildConstraintViolationWithTemplate("FTP/SFTP adapter requires '" + field + "' property")
                       .addConstraintViolation();
                valid = false;
            }
        }
        
        // Validate port
        if (properties.containsKey("port")) {
            try {
                int port = Integer.parseInt(properties.get("port").toString());
                if (port < 1 || port > 65535) {
                    context.buildConstraintViolationWithTemplate("Port must be between 1 and 65535")
                           .addConstraintViolation();
                    valid = false;
                }
            } catch (NumberFormatException e) {
                context.buildConstraintViolationWithTemplate("Port must be a valid number")
                       .addConstraintViolation();
                valid = false;
            }
        }
        
        return valid;
    }
    
    private boolean validateSoapAdapter(AdapterConfigDTO config, ConstraintValidatorContext context) {
        boolean valid = true;
        Map<String, Object> properties = config.getConnectionProperties();
        
        if (properties == null) {
            context.buildConstraintViolationWithTemplate("SOAP adapter requires connection properties")
                   .addConstraintViolation();
            return false;
        }
        
        // Required: wsdlUrl or endpointUrl
        if ((!properties.containsKey("wsdlUrl") || properties.get("wsdlUrl") == null) &&
            (!properties.containsKey("endpointUrl") || properties.get("endpointUrl") == null)) {
            context.buildConstraintViolationWithTemplate("SOAP adapter requires either 'wsdlUrl' or 'endpointUrl'")
                   .addConstraintViolation();
            valid = false;
        }
        
        return valid;
    }
    
    private boolean validateEmailAdapter(AdapterConfigDTO config, ConstraintValidatorContext context) {
        boolean valid = true;
        Map<String, Object> properties = config.getConnectionProperties();
        
        if (properties == null) {
            context.buildConstraintViolationWithTemplate("Email adapter requires connection properties")
                   .addConstraintViolation();
            return false;
        }
        
        // For sender (SMTP)
        if ("receiver".equals(config.getConnectionType())) {
            String[] requiredFields = {"smtpHost", "smtpPort", "username"};
            for (String field : requiredFields) {
                if (!properties.containsKey(field) || properties.get(field) == null) {
                    context.buildConstraintViolationWithTemplate("Email sender requires '" + field + "' property")
                           .addConstraintViolation();
                    valid = false;
                }
            }
        }
        
        // For receiver (IMAP/POP3)
        if ("sender".equals(config.getConnectionType())) {
            String[] requiredFields = {"protocol", "host", "port", "username"};
            for (String field : requiredFields) {
                if (!properties.containsKey(field) || properties.get(field) == null) {
                    context.buildConstraintViolationWithTemplate("Email receiver requires '" + field + "' property")
                           .addConstraintViolation();
                    valid = false;
                }
            }
            
            // Validate protocol
            if (properties.containsKey("protocol")) {
                String protocol = properties.get("protocol").toString().toUpperCase();
                if (!protocol.matches("^(IMAP|POP3|IMAPS|POP3S)$")) {
                    context.buildConstraintViolationWithTemplate("Invalid email protocol: " + protocol)
                           .addConstraintViolation();
                    valid = false;
                }
            }
        }
        
        return valid;
    }
    
    private boolean validateJmsAdapter(AdapterConfigDTO config, ConstraintValidatorContext context) {
        boolean valid = true;
        Map<String, Object> properties = config.getConnectionProperties();
        
        if (properties == null) {
            context.buildConstraintViolationWithTemplate("JMS adapter requires connection properties")
                   .addConstraintViolation();
            return false;
        }
        
        // Required fields
        String[] requiredFields = {"connectionFactory", "destination"};
        for (String field : requiredFields) {
            if (!properties.containsKey(field) || properties.get(field) == null) {
                context.buildConstraintViolationWithTemplate("JMS adapter requires '" + field + "' property")
                       .addConstraintViolation();
                valid = false;
            }
        }
        
        return valid;
    }
    
    private boolean validateSenderConfig(AdapterConfigDTO config, ConstraintValidatorContext context) {
        // Sender-specific validations
        return true;
    }
    
    private boolean validateReceiverConfig(AdapterConfigDTO config, ConstraintValidatorContext context) {
        // Receiver-specific validations
        return true;
    }
}