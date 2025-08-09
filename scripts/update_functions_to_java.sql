-- Script to update built-in functions to Java
-- Run this manually: mysql -u root -p integrixflowbridge < scripts/update_functions_to_java.sql

-- First, update all built-in functions to Java language
UPDATE transformation_custom_functions 
SET language = 'JAVA',
    updated_at = NOW()
WHERE is_built_in = true;

-- Update specific functions with Java implementations
UPDATE transformation_custom_functions 
SET function_body = 'public class AddFunction implements TransformationFunction {
    @Override
    public Object execute(Object... args) {
        validateArgs(args);
        double a = toDouble(args[0]);
        double b = toDouble(args[1]);
        return a + b;
    }
    
    @Override
    public int getRequiredArgCount() { return 2; }
    
    private double toDouble(Object value) {
        if (value instanceof Number) return ((Number) value).doubleValue();
        return Double.parseDouble(value.toString());
    }
}'
WHERE name = 'add' AND is_built_in = true;

UPDATE transformation_custom_functions 
SET function_body = 'public class SubtractFunction implements TransformationFunction {
    @Override
    public Object execute(Object... args) {
        validateArgs(args);
        double a = toDouble(args[0]);
        double b = toDouble(args[1]);
        return a - b;
    }
    
    @Override
    public int getRequiredArgCount() { return 2; }
    
    private double toDouble(Object value) {
        if (value instanceof Number) return ((Number) value).doubleValue();
        return Double.parseDouble(value.toString());
    }
}'
WHERE name = 'subtract' AND is_built_in = true;

-- Add a note to other functions that they need implementation
UPDATE transformation_custom_functions 
SET function_body = CONCAT('// TODO: Implement ', name, ' function in Java\n',
'public class ', CONCAT(UPPER(SUBSTRING(name, 1, 1)), SUBSTRING(name, 2)), 'Function implements TransformationFunction {\n',
'    @Override\n',
'    public Object execute(Object... args) {\n',
'        // Implementation needed\n',
'        throw new UnsupportedOperationException("Function not yet implemented");\n',
'    }\n',
'}')
WHERE is_built_in = true 
  AND name NOT IN ('add', 'subtract');

-- Show the updated functions
SELECT name, language, LEFT(function_body, 100) as code_preview 
FROM transformation_custom_functions 
WHERE is_built_in = true 
LIMIT 10;