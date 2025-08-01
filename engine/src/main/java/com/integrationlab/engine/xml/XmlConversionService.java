package com.integrationlab.engine.xml;

/**
 * Interface for converting various data formats to XML
 */
public interface XmlConversionService {
    
    /**
     * Convert data to XML format
     * 
     * @param data The input data
     * @param config Configuration for XML conversion
     * @return XML string representation
     * @throws XmlConversionException if conversion fails
     */
    String convertToXml(Object data, Object config) throws XmlConversionException;
    
    /**
     * Check if this converter supports the given data type
     * 
     * @param dataType The type of data to check
     * @return true if supported, false otherwise
     */
    boolean supports(Class<?> dataType);
}