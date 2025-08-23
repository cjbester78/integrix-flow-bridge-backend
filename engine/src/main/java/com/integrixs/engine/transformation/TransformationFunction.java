package com.integrixs.engine.transformation;

/**
 * Interface for all transformation functions
 */
public interface TransformationFunction {
    
    /**
     * Execute the transformation function with the given arguments
     * @param args Function arguments
     * @return The result of the function execution
     * @throws IllegalArgumentException if arguments are invalid
     */
    Object execute(Object... args) throws IllegalArgumentException;
    
    /**
     * Get the function name
     * @return Function name
     */
    default String getName() {
        return this.getClass().getSimpleName().replace("Function", "").toLowerCase();
    }
    
    /**
     * Get the required number of arguments
     * @return Required argument count (-1 for variable arguments)
     */
    default int getRequiredArgCount() {
        return -1;
    }
    
    /**
     * Get the expected argument types
     * @return Array of expected argument types (null for any type)
     */
    default Class<?>[] getArgTypes() {
        return null;
    }
    
    /**
     * Validate arguments before execution
     * @param args Arguments to validate
     * @throws IllegalArgumentException if validation fails
     */
    default void validateArgs(Object... args) throws IllegalArgumentException {
        int required = getRequiredArgCount();
        if (required >= 0 && args.length < required) {
            throw new IllegalArgumentException(
                String.format("%s requires %d argument(s), but %d provided", 
                    getName(), required, args.length)
            );
        }
    }
}