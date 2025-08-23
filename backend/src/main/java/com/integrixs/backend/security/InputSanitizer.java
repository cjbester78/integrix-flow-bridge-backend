package com.integrixs.backend.security;

import org.owasp.html.HtmlPolicyBuilder;
import org.owasp.html.PolicyFactory;
import org.springframework.stereotype.Component;

import java.util.regex.Pattern;

/**
 * Input sanitization utility to prevent XSS and injection attacks.
 * 
 * <p>Provides methods to sanitize user input and prevent common
 * security vulnerabilities.
 * 
 * @author Integration Team
 * @since 1.0.0
 */
@Component
public class InputSanitizer {
    
    // HTML sanitization policy - very restrictive
    private static final PolicyFactory STRICT_HTML_POLICY = new HtmlPolicyBuilder()
            .toFactory();
    
    // HTML sanitization policy - allows basic formatting
    private static final PolicyFactory BASIC_HTML_POLICY = new HtmlPolicyBuilder()
            .allowElements("b", "i", "u", "strong", "em", "br", "p")
            .toFactory();
    
    // Patterns for dangerous inputs
    private static final Pattern SQL_INJECTION_PATTERN = Pattern.compile(
            ".*\\b(ALTER|CREATE|DELETE|DROP|EXEC(UTE)?|INSERT|SELECT|UNION|UPDATE)\\b.*",
            Pattern.CASE_INSENSITIVE
    );
    
    private static final Pattern SCRIPT_PATTERN = Pattern.compile(
            ".*<script.*?>.*</script>.*",
            Pattern.CASE_INSENSITIVE | Pattern.DOTALL
    );
    
    private static final Pattern PATH_TRAVERSAL_PATTERN = Pattern.compile(
            ".*(\\.\\./|\\.\\\\|\\\\\\.\\.|\\./).*"
    );
    
    /**
     * Sanitize input by removing all HTML tags.
     * 
     * @param input the input string
     * @return sanitized string
     */
    public String sanitizeHtml(String input) {
        if (input == null) {
            return null;
        }
        return STRICT_HTML_POLICY.sanitize(input);
    }
    
    /**
     * Sanitize input allowing basic HTML formatting.
     * 
     * @param input the input string
     * @return sanitized string with basic HTML
     */
    public String sanitizeBasicHtml(String input) {
        if (input == null) {
            return null;
        }
        return BASIC_HTML_POLICY.sanitize(input);
    }
    
    /**
     * Check if input contains potential SQL injection.
     * 
     * @param input the input string
     * @return true if suspicious SQL patterns detected
     */
    public boolean containsSqlInjection(String input) {
        if (input == null || input.isEmpty()) {
            return false;
        }
        return SQL_INJECTION_PATTERN.matcher(input).matches();
    }
    
    /**
     * Check if input contains script tags.
     * 
     * @param input the input string
     * @return true if script tags detected
     */
    public boolean containsScriptTag(String input) {
        if (input == null || input.isEmpty()) {
            return false;
        }
        return SCRIPT_PATTERN.matcher(input).matches();
    }
    
    /**
     * Check if input contains path traversal attempts.
     * 
     * @param input the input string
     * @return true if path traversal patterns detected
     */
    public boolean containsPathTraversal(String input) {
        if (input == null || input.isEmpty()) {
            return false;
        }
        return PATH_TRAVERSAL_PATTERN.matcher(input).matches();
    }
    
    /**
     * Sanitize filename to prevent directory traversal.
     * 
     * @param filename the filename
     * @return sanitized filename
     */
    public String sanitizeFilename(String filename) {
        if (filename == null) {
            return null;
        }
        
        // Remove path separators and special characters
        return filename
                .replaceAll("[/\\\\]", "")
                .replaceAll("\\.\\.", "")
                .replaceAll("[^a-zA-Z0-9._-]", "_");
    }
    
    /**
     * Validate and sanitize JSON string.
     * 
     * @param json the JSON string
     * @return true if valid JSON structure
     */
    public boolean isValidJson(String json) {
        if (json == null || json.isEmpty()) {
            return false;
        }
        
        // Basic JSON structure validation
        json = json.trim();
        return (json.startsWith("{") && json.endsWith("}")) ||
               (json.startsWith("[") && json.endsWith("]"));
    }
    
    /**
     * Truncate string to prevent overflow attacks.
     * 
     * @param input the input string
     * @param maxLength maximum allowed length
     * @return truncated string
     */
    public String truncate(String input, int maxLength) {
        if (input == null || input.length() <= maxLength) {
            return input;
        }
        return input.substring(0, maxLength);
    }
}