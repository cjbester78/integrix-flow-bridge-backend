-- Update all built-in functions from JAVASCRIPT to JAVA language
UPDATE transformation_custom_functions 
SET language = 'JAVA',
    function_body = CONCAT('// TODO: Implement Java version of ', name, '\n// Original JavaScript:\n// ', function_body),
    updated_at = NOW()
WHERE is_built_in = true 
  AND language = 'JAVASCRIPT';

-- Add Java class template for each function category
UPDATE transformation_custom_functions 
SET function_body = CASE 
    -- Math functions
    WHEN category = 'math' AND name = 'add' THEN 
'public class AddFunction implements TransformationFunction {
    @Override
    public Object execute(Object... args) {
        if (args.length < 2) throw new IllegalArgumentException("add requires 2 arguments");
        double a = toDouble(args[0]);
        double b = toDouble(args[1]);
        return a + b;
    }
}'
    WHEN category = 'math' AND name = 'subtract' THEN 
'public class SubtractFunction implements TransformationFunction {
    @Override
    public Object execute(Object... args) {
        if (args.length < 2) throw new IllegalArgumentException("subtract requires 2 arguments");
        double a = toDouble(args[0]);
        double b = toDouble(args[1]);
        return a - b;
    }
}'
    WHEN category = 'math' AND name = 'multiply' THEN 
'public class MultiplyFunction implements TransformationFunction {
    @Override
    public Object execute(Object... args) {
        if (args.length < 2) throw new IllegalArgumentException("multiply requires 2 arguments");
        double a = toDouble(args[0]);
        double b = toDouble(args[1]);
        return a * b;
    }
}'
    WHEN category = 'math' AND name = 'divide' THEN 
'public class DivideFunction implements TransformationFunction {
    @Override
    public Object execute(Object... args) {
        if (args.length < 2) throw new IllegalArgumentException("divide requires 2 arguments");
        double a = toDouble(args[0]);
        double b = toDouble(args[1]);
        if (b == 0) throw new ArithmeticException("Division by zero");
        return a / b;
    }
}'
    WHEN category = 'math' AND name = 'absolute' THEN 
'public class AbsoluteFunction implements TransformationFunction {
    @Override
    public Object execute(Object... args) {
        if (args.length < 1) throw new IllegalArgumentException("absolute requires 1 argument");
        double value = toDouble(args[0]);
        return Math.abs(value);
    }
}'
    WHEN category = 'math' AND name = 'sqrt' THEN 
'public class SqrtFunction implements TransformationFunction {
    @Override
    public Object execute(Object... args) {
        if (args.length < 1) throw new IllegalArgumentException("sqrt requires 1 argument");
        double value = toDouble(args[0]);
        return Math.sqrt(value);
    }
}'
    WHEN category = 'math' AND name = 'power' THEN 
'public class PowerFunction implements TransformationFunction {
    @Override
    public Object execute(Object... args) {
        if (args.length < 2) throw new IllegalArgumentException("power requires 2 arguments");
        double base = toDouble(args[0]);
        double exponent = toDouble(args[1]);
        return Math.pow(base, exponent);
    }
}'
    WHEN category = 'math' AND name = 'max' THEN 
'public class MaxFunction implements TransformationFunction {
    @Override
    public Object execute(Object... args) {
        if (args.length < 2) throw new IllegalArgumentException("max requires 2 arguments");
        double a = toDouble(args[0]);
        double b = toDouble(args[1]);
        return Math.max(a, b);
    }
}'
    WHEN category = 'math' AND name = 'min' THEN 
'public class MinFunction implements TransformationFunction {
    @Override
    public Object execute(Object... args) {
        if (args.length < 2) throw new IllegalArgumentException("min requires 2 arguments");
        double a = toDouble(args[0]);
        double b = toDouble(args[1]);
        return Math.min(a, b);
    }
}'
    -- Text functions
    WHEN category = 'text' AND name = 'concat' THEN 
'public class ConcatFunction implements TransformationFunction {
    @Override
    public Object execute(Object... args) {
        if (args.length < 2) throw new IllegalArgumentException("concat requires at least 2 arguments");
        String string1 = toString(args[0]);
        String string2 = toString(args[1]);
        String delimiter = args.length > 2 ? toString(args[2]) : "";
        return delimiter.isEmpty() ? string1 + string2 : string1 + delimiter + string2;
    }
}'
    WHEN category = 'text' AND name = 'substring' THEN 
'public class SubstringFunction implements TransformationFunction {
    @Override
    public Object execute(Object... args) {
        if (args.length < 2) throw new IllegalArgumentException("substring requires at least 2 arguments");
        String text = toString(args[0]);
        int start = toInt(args[1]);
        int end = args.length > 2 ? toInt(args[2]) : text.length();
        return text.substring(start, end);
    }
}'
    WHEN category = 'text' AND name = 'toUpperCase' THEN 
'public class ToUpperCaseFunction implements TransformationFunction {
    @Override
    public Object execute(Object... args) {
        if (args.length < 1) throw new IllegalArgumentException("toUpperCase requires 1 argument");
        String text = toString(args[0]);
        return text.toUpperCase();
    }
}'
    WHEN category = 'text' AND name = 'toLowerCase' THEN 
'public class ToLowerCaseFunction implements TransformationFunction {
    @Override
    public Object execute(Object... args) {
        if (args.length < 1) throw new IllegalArgumentException("toLowerCase requires 1 argument");
        String text = toString(args[0]);
        return text.toLowerCase();
    }
}'
    WHEN category = 'text' AND name = 'trim' THEN 
'public class TrimFunction implements TransformationFunction {
    @Override
    public Object execute(Object... args) {
        if (args.length < 1) throw new IllegalArgumentException("trim requires 1 argument");
        String text = toString(args[0]);
        return text.trim();
    }
}'
    WHEN category = 'text' AND name = 'length' THEN 
'public class LengthFunction implements TransformationFunction {
    @Override
    public Object execute(Object... args) {
        if (args.length < 1) throw new IllegalArgumentException("length requires 1 argument");
        String text = toString(args[0]);
        return text.length();
    }
}'
    -- Boolean functions
    WHEN category = 'boolean' AND name = 'and' THEN 
'public class AndFunction implements TransformationFunction {
    @Override
    public Object execute(Object... args) {
        if (args.length < 2) throw new IllegalArgumentException("and requires 2 arguments");
        boolean a = toBoolean(args[0]);
        boolean b = toBoolean(args[1]);
        return a && b;
    }
}'
    WHEN category = 'boolean' AND name = 'or' THEN 
'public class OrFunction implements TransformationFunction {
    @Override
    public Object execute(Object... args) {
        if (args.length < 2) throw new IllegalArgumentException("or requires 2 arguments");
        boolean a = toBoolean(args[0]);
        boolean b = toBoolean(args[1]);
        return a || b;
    }
}'
    WHEN category = 'boolean' AND name = 'not' THEN 
'public class NotFunction implements TransformationFunction {
    @Override
    public Object execute(Object... args) {
        if (args.length < 1) throw new IllegalArgumentException("not requires 1 argument");
        boolean value = toBoolean(args[0]);
        return !value;
    }
}'
    -- Date functions
    WHEN category = 'date' AND name = 'currentDate' THEN 
'public class CurrentDateFunction implements TransformationFunction {
    @Override
    public Object execute(Object... args) {
        String format = args.length > 0 ? toString(args[0]) : "yyyy-MM-dd''T''HH:mm:ss.SSS''Z''";
        SimpleDateFormat sdf = new SimpleDateFormat(format);
        sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
        return sdf.format(new Date());
    }
}'
    -- Keep original for functions not yet implemented
    ELSE function_body
END,
updated_at = NOW()
WHERE is_built_in = true 
  AND language = 'JAVA';

-- Add common utility methods template
UPDATE transformation_custom_functions 
SET function_body = CONCAT(function_body, '
    
    // Utility methods
    private double toDouble(Object value) {
        if (value instanceof Number) return ((Number) value).doubleValue();
        return Double.parseDouble(value.toString());
    }
    
    private int toInt(Object value) {
        if (value instanceof Number) return ((Number) value).intValue();
        return Integer.parseInt(value.toString());
    }
    
    private String toString(Object value) {
        return value == null ? "" : value.toString();
    }
    
    private boolean toBoolean(Object value) {
        if (value instanceof Boolean) return (Boolean) value;
        return Boolean.parseBoolean(value.toString());
    }')
WHERE is_built_in = true 
  AND language = 'JAVA'
  AND function_body NOT LIKE '%// TODO:%';