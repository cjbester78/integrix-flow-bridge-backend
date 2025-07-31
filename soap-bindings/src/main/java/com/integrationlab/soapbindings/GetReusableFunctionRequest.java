package com.integrationlab.soapbindings;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlRootElement;
import jakarta.xml.bind.annotation.XmlType;

/**
 * Represents a SOAP request to retrieve a reusable function by its ID.
 * This class is serialized into an XML request using JAXB annotations.
 * 
 * <p>Example XML:
 * <pre>{@code
 * <GetReusableFunctionRequest>
 *     <id>function-uuid</id>
 * </GetReusableFunctionRequest>
 * }</pre>
 */
@XmlRootElement(name = "GetReusableFunctionRequest")
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = {"id"})
public class GetReusableFunctionRequest {

    // The unique identifier of the reusable function being requested
    private String id;

    /** Default no-args constructor required for JAXB serialization */
    public GetReusableFunctionRequest() {
    }

    /**
     * Parameterized constructor for initializing the request with an ID.
     *
     * @param id The ID of the reusable function
     */
    public GetReusableFunctionRequest(String id) {
        this.id = id;
    }

    /**
     * Returns the reusable function ID.
     *
     * @return function ID
     */
    public String getId() {
        return id;
    }

    /**
     * Sets the reusable function ID.
     *
     * @param id function ID to set
     */
    public void setId(String id) {
        this.id = id;
    }
}
