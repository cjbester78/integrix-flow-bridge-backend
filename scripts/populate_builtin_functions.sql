-- Populate built-in transformation functions
-- This script creates the initial built-in functions if they don't exist

-- First, check if functions already exist
SELECT COUNT(*) as existing_functions FROM transformation_custom_functions WHERE is_built_in = true;

-- Math functions
INSERT IGNORE INTO transformation_custom_functions (
    function_id, name, description, category, language, function_signature, 
    function_body, is_safe, is_public, is_built_in, performance_class, 
    version, created_by, created_at
) VALUES 
-- Add function
(UUID(), 'add', 'Add two numbers', 'math', 'JAVA', 'add(a, b)',
'public class AddFunction implements TransformationFunction {
    public Object execute(Object... args) {
        validateArgs(args);
        Number a = (Number) args[0];
        Number b = (Number) args[1];
        if (a instanceof Double || b instanceof Double) {
            return a.doubleValue() + b.doubleValue();
        }
        return a.longValue() + b.longValue();
    }
    
    public String getName() { return "add"; }
    public int getRequiredArgCount() { return 2; }
    public Class<?>[] getArgTypes() { return new Class<?>[] {Number.class, Number.class}; }
}', true, true, true, 'FAST', 1, 'system', NOW()),

-- Subtract function
(UUID(), 'subtract', 'Subtract two numbers', 'math', 'JAVA', 'subtract(a, b)',
'public class SubtractFunction implements TransformationFunction {
    public Object execute(Object... args) {
        validateArgs(args);
        Number a = (Number) args[0];
        Number b = (Number) args[1];
        if (a instanceof Double || b instanceof Double) {
            return a.doubleValue() - b.doubleValue();
        }
        return a.longValue() - b.longValue();
    }
    
    public String getName() { return "subtract"; }
    public int getRequiredArgCount() { return 2; }
    public Class<?>[] getArgTypes() { return new Class<?>[] {Number.class, Number.class}; }
}', true, true, true, 'FAST', 1, 'system', NOW()),

-- Multiply function
(UUID(), 'multiply', 'Multiply two numbers', 'math', 'JAVA', 'multiply(a, b)',
'public class MultiplyFunction implements TransformationFunction {
    public Object execute(Object... args) {
        validateArgs(args);
        Number a = (Number) args[0];
        Number b = (Number) args[1];
        if (a instanceof Double || b instanceof Double) {
            return a.doubleValue() * b.doubleValue();
        }
        return a.longValue() * b.longValue();
    }
    
    public String getName() { return "multiply"; }
    public int getRequiredArgCount() { return 2; }
    public Class<?>[] getArgTypes() { return new Class<?>[] {Number.class, Number.class}; }
}', true, true, true, 'FAST', 1, 'system', NOW()),

-- Text functions
-- Concat function
(UUID(), 'concat', 'Concatenate two strings with optional delimiter', 'text', 'JAVA', 'concat(string1, string2, delimiter?)',
'public class ConcatFunction implements TransformationFunction {
    public Object execute(Object... args) {
        if (args.length < 2) {
            throw new IllegalArgumentException("concat requires at least 2 arguments");
        }
        String str1 = String.valueOf(args[0]);
        String str2 = String.valueOf(args[1]);
        if (args.length > 2 && args[2] != null) {
            String delimiter = String.valueOf(args[2]);
            return str1 + delimiter + str2;
        }
        return str1 + str2;
    }
    
    public String getName() { return "concat"; }
    public int getRequiredArgCount() { return -1; } // Variable args
}', true, true, true, 'FAST', 1, 'system', NOW()),

-- ToUpperCase function
(UUID(), 'toUpperCase', 'Convert text to uppercase', 'text', 'JAVA', 'toUpperCase(text)',
'public class ToUpperCaseFunction implements TransformationFunction {
    public Object execute(Object... args) {
        validateArgs(args);
        String text = String.valueOf(args[0]);
        return text.toUpperCase();
    }
    
    public String getName() { return "toUpperCase"; }
    public int getRequiredArgCount() { return 1; }
    public Class<?>[] getArgTypes() { return new Class<?>[] {String.class}; }
}', true, true, true, 'FAST', 1, 'system', NOW()),

-- Boolean functions
-- And function
(UUID(), 'and', 'Logical AND operation', 'boolean', 'JAVA', 'and(a, b)',
'public class AndFunction implements TransformationFunction {
    public Object execute(Object... args) {
        validateArgs(args);
        Boolean a = (Boolean) args[0];
        Boolean b = (Boolean) args[1];
        return a && b;
    }
    
    public String getName() { return "and"; }
    public int getRequiredArgCount() { return 2; }
    public Class<?>[] getArgTypes() { return new Class<?>[] {Boolean.class, Boolean.class}; }
}', true, true, true, 'FAST', 1, 'system', NOW()),

-- Date functions
-- CurrentDate function
(UUID(), 'currentDate', 'Get current date', 'date', 'JAVA', 'currentDate(format?)',
'public class CurrentDateFunction implements TransformationFunction {
    public Object execute(Object... args) {
        java.time.LocalDateTime now = java.time.LocalDateTime.now();
        if (args.length > 0 && args[0] != null) {
            String format = String.valueOf(args[0]);
            java.time.format.DateTimeFormatter formatter = java.time.format.DateTimeFormatter.ofPattern(format);
            return now.format(formatter);
        }
        return now.toString();
    }
    
    public String getName() { return "currentDate"; }
    public int getRequiredArgCount() { return -1; } // Variable args
}', true, true, true, 'FAST', 1, 'system', NOW());

-- Show count of functions after insert
SELECT COUNT(*) as total_functions FROM transformation_custom_functions WHERE is_built_in = true;