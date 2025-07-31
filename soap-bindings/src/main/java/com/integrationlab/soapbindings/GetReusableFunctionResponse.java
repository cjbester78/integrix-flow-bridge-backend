package com.integrationlab.soapbindings;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;
import java.time.LocalDateTime;

/**
 * Represents a SOAP response containing a reusable function's metadata and logic.
 * JAXB annotations ensure correct XML serialization and deserialization.
 *
 * <p>Example XML:
 * <pre>{@code
 * <GetReusableFunctionResponse>
 *     <Function>
 *         <Id>...</Id>
 *         <Name>...</Name>
 *         ...
 *     </Function>
 * </GetReusableFunctionResponse>
 * }</pre>
 */
@XmlRootElement(name = "GetReusableFunctionResponse")
@XmlAccessorType(XmlAccessType.FIELD)
public class GetReusableFunctionResponse {

    /** The reusable function details returned in the response */
    @XmlElement(name = "Function", required = true)
    private ReusableFunction function;

    /** Default constructor required for JAXB */
    public GetReusableFunctionResponse() {
    }

    /**
     * Constructs the response with a reusable function object.
     *
     * @param function the reusable function to return
     */
    public GetReusableFunctionResponse(ReusableFunction function) {
        this.function = function;
    }

    /**
     * Gets the reusable function.
     *
     * @return the reusable function
     */
    public ReusableFunction getFunction() {
        return function;
    }

    /**
     * Sets the reusable function.
     *
     * @param function the reusable function to set
     */
    public void setFunction(ReusableFunction function) {
        this.function = function;
    }

    /**
     * Represents the structure of a reusable function.
     * Typically returned as part of the SOAP response payload.
     */
    @XmlAccessorType(XmlAccessType.FIELD)
    public static class ReusableFunction {

        @XmlElement(name = "Id")
        private String id;

        @XmlElement(name = "Name")
        private String name;

        @XmlElement(name = "Version")
        private String version;

        @XmlElement(name = "FunctionBody")
        private String functionBody;

        @XmlElement(name = "InputTypes")
        private String inputTypes;

        @XmlElement(name = "OutputType")
        private String outputType;

        @XmlElement(name = "Description")
        private String description;

        @XmlElement(name = "CreatedAt")
        private LocalDateTime createdAt;

        @XmlElement(name = "UpdatedAt")
        private LocalDateTime updatedAt;

        /** Default constructor required for JAXB */
        public ReusableFunction() {}

        // Getters and setters

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getVersion() {
            return version;
        }

        public void setVersion(String version) {
            this.version = version;
        }

        public String getFunctionBody() {
            return functionBody;
        }

        public void setFunctionBody(String functionBody) {
            this.functionBody = functionBody;
        }

        public String getInputTypes() {
            return inputTypes;
        }

        public void setInputTypes(String inputTypes) {
            this.inputTypes = inputTypes;
        }

        public String getOutputType() {
            return outputType;
        }

        public void setOutputType(String outputType) {
            this.outputType = outputType;
        }

        public String getDescription() {
            return description;
        }

        public void setDescription(String description) {
            this.description = description;
        }

        public LocalDateTime getCreatedAt() {
            return createdAt;
        }

        public void setCreatedAt(LocalDateTime createdAt) {
            this.createdAt = createdAt;
        }

        public LocalDateTime getUpdatedAt() {
            return updatedAt;
        }

        public void setUpdatedAt(LocalDateTime updatedAt) {
            this.updatedAt = updatedAt;
        }
    }
}
