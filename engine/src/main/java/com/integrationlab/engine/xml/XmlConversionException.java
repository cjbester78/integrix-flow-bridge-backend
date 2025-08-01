package com.integrationlab.engine.xml;

/**
 * Exception thrown when XML conversion fails
 */
public class XmlConversionException extends Exception {
    
    public XmlConversionException(String message) {
        super(message);
    }
    
    public XmlConversionException(String message, Throwable cause) {
        super(message, cause);
    }
}