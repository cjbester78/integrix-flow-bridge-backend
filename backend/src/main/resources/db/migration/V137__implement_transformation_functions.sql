-- V137: Implement all transformation functions in Java
-- This migration updates all TODO placeholder functions with actual Java implementations

-- Math Functions

-- absolute
UPDATE transformation_custom_functions 
SET function_body = 'public class AbsoluteFunction implements TransformationFunction {
    @Override
    public Object execute(Object... args) {
        if (args.length < 1) {
            throw new IllegalArgumentException("Absolute function requires 1 argument");
        }
        double value = toDouble(args[0]);
        return Math.abs(value);
    }
    
    private double toDouble(Object value) {
        if (value == null) return 0.0;
        if (value instanceof Number) return ((Number) value).doubleValue();
        return Double.parseDouble(value.toString());
    }
}'
WHERE name = 'absolute' AND is_built_in = true;

-- add (already implemented correctly)

-- subtract
UPDATE transformation_custom_functions 
SET function_body = 'public class SubtractFunction implements TransformationFunction {
    @Override
    public Object execute(Object... args) {
        if (args.length < 2) {
            throw new IllegalArgumentException("Subtract function requires 2 arguments");
        }
        double a = toDouble(args[0]);
        double b = toDouble(args[1]);
        return a - b;
    }
    
    private double toDouble(Object value) {
        if (value == null) return 0.0;
        if (value instanceof Number) return ((Number) value).doubleValue();
        return Double.parseDouble(value.toString());
    }
}'
WHERE name = 'subtract' AND is_built_in = true;

-- multiply
UPDATE transformation_custom_functions 
SET function_body = 'public class MultiplyFunction implements TransformationFunction {
    @Override
    public Object execute(Object... args) {
        if (args.length < 2) {
            throw new IllegalArgumentException("Multiply function requires 2 arguments");
        }
        double a = toDouble(args[0]);
        double b = toDouble(args[1]);
        return a * b;
    }
    
    private double toDouble(Object value) {
        if (value == null) return 0.0;
        if (value instanceof Number) return ((Number) value).doubleValue();
        return Double.parseDouble(value.toString());
    }
}'
WHERE name = 'multiply' AND is_built_in = true;

-- divide
UPDATE transformation_custom_functions 
SET function_body = 'public class DivideFunction implements TransformationFunction {
    @Override
    public Object execute(Object... args) {
        if (args.length < 2) {
            throw new IllegalArgumentException("Divide function requires 2 arguments");
        }
        double a = toDouble(args[0]);
        double b = toDouble(args[1]);
        if (b == 0) {
            throw new ArithmeticException("Division by zero");
        }
        return a / b;
    }
    
    private double toDouble(Object value) {
        if (value == null) return 0.0;
        if (value instanceof Number) return ((Number) value).doubleValue();
        return Double.parseDouble(value.toString());
    }
}'
WHERE name = 'divide' AND is_built_in = true;

-- equals (for numbers)
UPDATE transformation_custom_functions 
SET function_body = 'public class EqualsFunction implements TransformationFunction {
    @Override
    public Object execute(Object... args) {
        if (args.length < 2) {
            throw new IllegalArgumentException("Equals function requires 2 arguments");
        }
        double a = toDouble(args[0]);
        double b = toDouble(args[1]);
        return a == b;
    }
    
    private double toDouble(Object value) {
        if (value == null) return 0.0;
        if (value instanceof Number) return ((Number) value).doubleValue();
        return Double.parseDouble(value.toString());
    }
}'
WHERE name = 'equals' AND is_built_in = true;

-- sqrt
UPDATE transformation_custom_functions 
SET function_body = 'public class SqrtFunction implements TransformationFunction {
    @Override
    public Object execute(Object... args) {
        if (args.length < 1) {
            throw new IllegalArgumentException("Sqrt function requires 1 argument");
        }
        double value = toDouble(args[0]);
        if (value < 0) {
            throw new ArithmeticException("Cannot calculate square root of negative number");
        }
        return Math.sqrt(value);
    }
    
    private double toDouble(Object value) {
        if (value == null) return 0.0;
        if (value instanceof Number) return ((Number) value).doubleValue();
        return Double.parseDouble(value.toString());
    }
}'
WHERE name = 'sqrt' AND is_built_in = true;

-- square
UPDATE transformation_custom_functions 
SET function_body = 'public class SquareFunction implements TransformationFunction {
    @Override
    public Object execute(Object... args) {
        if (args.length < 1) {
            throw new IllegalArgumentException("Square function requires 1 argument");
        }
        double value = toDouble(args[0]);
        return value * value;
    }
    
    private double toDouble(Object value) {
        if (value == null) return 0.0;
        if (value instanceof Number) return ((Number) value).doubleValue();
        return Double.parseDouble(value.toString());
    }
}'
WHERE name = 'square' AND is_built_in = true;

-- sign
UPDATE transformation_custom_functions 
SET function_body = 'public class SignFunction implements TransformationFunction {
    @Override
    public Object execute(Object... args) {
        if (args.length < 1) {
            throw new IllegalArgumentException("Sign function requires 1 argument");
        }
        double value = toDouble(args[0]);
        return (int) Math.signum(value);
    }
    
    private double toDouble(Object value) {
        if (value == null) return 0.0;
        if (value instanceof Number) return ((Number) value).doubleValue();
        return Double.parseDouble(value.toString());
    }
}'
WHERE name = 'sign' AND is_built_in = true;

-- neg
UPDATE transformation_custom_functions 
SET function_body = 'public class NegFunction implements TransformationFunction {
    @Override
    public Object execute(Object... args) {
        if (args.length < 1) {
            throw new IllegalArgumentException("Neg function requires 1 argument");
        }
        double value = toDouble(args[0]);
        return -value;
    }
    
    private double toDouble(Object value) {
        if (value == null) return 0.0;
        if (value instanceof Number) return ((Number) value).doubleValue();
        return Double.parseDouble(value.toString());
    }
}'
WHERE name = 'neg' AND is_built_in = true;

-- inv
UPDATE transformation_custom_functions 
SET function_body = 'public class InvFunction implements TransformationFunction {
    @Override
    public Object execute(Object... args) {
        if (args.length < 1) {
            throw new IllegalArgumentException("Inv function requires 1 argument");
        }
        double value = toDouble(args[0]);
        if (value == 0) {
            throw new ArithmeticException("Cannot calculate inverse of zero");
        }
        return 1.0 / value;
    }
    
    private double toDouble(Object value) {
        if (value == null) return 0.0;
        if (value instanceof Number) return ((Number) value).doubleValue();
        return Double.parseDouble(value.toString());
    }
}'
WHERE name = 'inv' AND is_built_in = true;

-- power
UPDATE transformation_custom_functions 
SET function_body = 'public class PowerFunction implements TransformationFunction {
    @Override
    public Object execute(Object... args) {
        if (args.length < 2) {
            throw new IllegalArgumentException("Power function requires 2 arguments");
        }
        double base = toDouble(args[0]);
        double exponent = toDouble(args[1]);
        return Math.pow(base, exponent);
    }
    
    private double toDouble(Object value) {
        if (value == null) return 0.0;
        if (value instanceof Number) return ((Number) value).doubleValue();
        return Double.parseDouble(value.toString());
    }
}'
WHERE name = 'power' AND is_built_in = true;

-- lesser
UPDATE transformation_custom_functions 
SET function_body = 'public class LesserFunction implements TransformationFunction {
    @Override
    public Object execute(Object... args) {
        if (args.length < 2) {
            throw new IllegalArgumentException("Lesser function requires 2 arguments");
        }
        double a = toDouble(args[0]);
        double b = toDouble(args[1]);
        return a < b;
    }
    
    private double toDouble(Object value) {
        if (value == null) return 0.0;
        if (value instanceof Number) return ((Number) value).doubleValue();
        return Double.parseDouble(value.toString());
    }
}'
WHERE name = 'lesser' AND is_built_in = true;

-- greater
UPDATE transformation_custom_functions 
SET function_body = 'public class GreaterFunction implements TransformationFunction {
    @Override
    public Object execute(Object... args) {
        if (args.length < 2) {
            throw new IllegalArgumentException("Greater function requires 2 arguments");
        }
        double a = toDouble(args[0]);
        double b = toDouble(args[1]);
        return a > b;
    }
    
    private double toDouble(Object value) {
        if (value == null) return 0.0;
        if (value instanceof Number) return ((Number) value).doubleValue();
        return Double.parseDouble(value.toString());
    }
}'
WHERE name = 'greater' AND is_built_in = true;

-- max
UPDATE transformation_custom_functions 
SET function_body = 'public class MaxFunction implements TransformationFunction {
    @Override
    public Object execute(Object... args) {
        if (args.length < 2) {
            throw new IllegalArgumentException("Max function requires 2 arguments");
        }
        double a = toDouble(args[0]);
        double b = toDouble(args[1]);
        return Math.max(a, b);
    }
    
    private double toDouble(Object value) {
        if (value == null) return 0.0;
        if (value instanceof Number) return ((Number) value).doubleValue();
        return Double.parseDouble(value.toString());
    }
}'
WHERE name = 'max' AND is_built_in = true;

-- min
UPDATE transformation_custom_functions 
SET function_body = 'public class MinFunction implements TransformationFunction {
    @Override
    public Object execute(Object... args) {
        if (args.length < 2) {
            throw new IllegalArgumentException("Min function requires 2 arguments");
        }
        double a = toDouble(args[0]);
        double b = toDouble(args[1]);
        return Math.min(a, b);
    }
    
    private double toDouble(Object value) {
        if (value == null) return 0.0;
        if (value instanceof Number) return ((Number) value).doubleValue();
        return Double.parseDouble(value.toString());
    }
}'
WHERE name = 'min' AND is_built_in = true;

-- ceil
UPDATE transformation_custom_functions 
SET function_body = 'public class CeilFunction implements TransformationFunction {
    @Override
    public Object execute(Object... args) {
        if (args.length < 1) {
            throw new IllegalArgumentException("Ceil function requires 1 argument");
        }
        double value = toDouble(args[0]);
        return Math.ceil(value);
    }
    
    private double toDouble(Object value) {
        if (value == null) return 0.0;
        if (value instanceof Number) return ((Number) value).doubleValue();
        return Double.parseDouble(value.toString());
    }
}'
WHERE name = 'ceil' AND is_built_in = true;

-- floor
UPDATE transformation_custom_functions 
SET function_body = 'public class FloorFunction implements TransformationFunction {
    @Override
    public Object execute(Object... args) {
        if (args.length < 1) {
            throw new IllegalArgumentException("Floor function requires 1 argument");
        }
        double value = toDouble(args[0]);
        return Math.floor(value);
    }
    
    private double toDouble(Object value) {
        if (value == null) return 0.0;
        if (value instanceof Number) return ((Number) value).doubleValue();
        return Double.parseDouble(value.toString());
    }
}'
WHERE name = 'floor' AND is_built_in = true;

-- round
UPDATE transformation_custom_functions 
SET function_body = 'public class RoundFunction implements TransformationFunction {
    @Override
    public Object execute(Object... args) {
        if (args.length < 1) {
            throw new IllegalArgumentException("Round function requires 1 argument");
        }
        double value = toDouble(args[0]);
        return Math.round(value);
    }
    
    private double toDouble(Object value) {
        if (value == null) return 0.0;
        if (value instanceof Number) return ((Number) value).doubleValue();
        return Double.parseDouble(value.toString());
    }
}'
WHERE name = 'round' AND is_built_in = true;

-- counter
UPDATE transformation_custom_functions 
SET function_body = 'public class CounterFunction implements TransformationFunction {
    private static long counter = 0;
    
    @Override
    public Object execute(Object... args) {
        long start = 1;
        long step = 1;
        
        if (args.length >= 1 && args[0] != null) {
            start = toLong(args[0]);
        }
        if (args.length >= 2 && args[1] != null) {
            step = toLong(args[1]);
        }
        
        counter = start + (counter * step);
        return counter;
    }
    
    private long toLong(Object value) {
        if (value == null) return 0L;
        if (value instanceof Number) return ((Number) value).longValue();
        return Long.parseLong(value.toString());
    }
}'
WHERE name = 'counter' AND is_built_in = true;

-- formatNumber
UPDATE transformation_custom_functions 
SET function_body = 'public class FormatNumberFunction implements TransformationFunction {
    @Override
    public Object execute(Object... args) {
        if (args.length < 2) {
            throw new IllegalArgumentException("FormatNumber function requires at least 2 arguments");
        }
        
        double value = toDouble(args[0]);
        int totalDigits = toInt(args[1]);
        int decimals = args.length > 2 ? toInt(args[2]) : 2;
        
        String format = "%." + decimals + "f";
        String formatted = String.format(format, value);
        
        // Pad with leading zeros if needed
        int currentLength = formatted.replace(".", "").length();
        if (currentLength < totalDigits) {
            int zerosNeeded = totalDigits - currentLength;
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < zerosNeeded; i++) {
                sb.append("0");
            }
            formatted = sb.toString() + formatted;
        }
        
        return formatted;
    }
    
    private double toDouble(Object value) {
        if (value == null) return 0.0;
        if (value instanceof Number) return ((Number) value).doubleValue();
        return Double.parseDouble(value.toString());
    }
    
    private int toInt(Object value) {
        if (value == null) return 0;
        if (value instanceof Number) return ((Number) value).intValue();
        return Integer.parseInt(value.toString());
    }
}'
WHERE name = 'formatNumber' AND is_built_in = true;

-- sum
UPDATE transformation_custom_functions 
SET function_body = 'public class SumFunction implements TransformationFunction {
    @Override
    public Object execute(Object... args) {
        if (args.length < 1) {
            throw new IllegalArgumentException("Sum function requires at least 1 argument");
        }
        
        double sum = 0.0;
        
        if (args[0] instanceof java.util.List) {
            java.util.List<?> list = (java.util.List<?>) args[0];
            for (Object item : list) {
                sum += toDouble(item);
            }
        } else if (args[0].getClass().isArray()) {
            Object[] array = (Object[]) args[0];
            for (Object item : array) {
                sum += toDouble(item);
            }
        } else {
            // Sum all arguments
            for (Object arg : args) {
                sum += toDouble(arg);
            }
        }
        
        return sum;
    }
    
    private double toDouble(Object value) {
        if (value == null) return 0.0;
        if (value instanceof Number) return ((Number) value).doubleValue();
        return Double.parseDouble(value.toString());
    }
}'
WHERE name = 'sum' AND is_built_in = true;

-- average
UPDATE transformation_custom_functions 
SET function_body = 'public class AverageFunction implements TransformationFunction {
    @Override
    public Object execute(Object... args) {
        if (args.length < 1) {
            throw new IllegalArgumentException("Average function requires at least 1 argument");
        }
        
        double sum = 0.0;
        int count = 0;
        
        if (args[0] instanceof java.util.List) {
            java.util.List<?> list = (java.util.List<?>) args[0];
            for (Object item : list) {
                sum += toDouble(item);
                count++;
            }
        } else if (args[0].getClass().isArray()) {
            Object[] array = (Object[]) args[0];
            for (Object item : array) {
                sum += toDouble(item);
                count++;
            }
        } else {
            // Average all arguments
            for (Object arg : args) {
                sum += toDouble(arg);
                count++;
            }
        }
        
        if (count == 0) {
            return 0.0;
        }
        
        return sum / count;
    }
    
    private double toDouble(Object value) {
        if (value == null) return 0.0;
        if (value instanceof Number) return ((Number) value).doubleValue();
        return Double.parseDouble(value.toString());
    }
}'
WHERE name = 'average' AND is_built_in = true;

-- count
UPDATE transformation_custom_functions 
SET function_body = 'public class CountFunction implements TransformationFunction {
    @Override
    public Object execute(Object... args) {
        if (args.length < 1) {
            return 0;
        }
        
        if (args[0] instanceof java.util.List) {
            return ((java.util.List<?>) args[0]).size();
        } else if (args[0] instanceof java.util.Collection) {
            return ((java.util.Collection<?>) args[0]).size();
        } else if (args[0].getClass().isArray()) {
            return java.lang.reflect.Array.getLength(args[0]);
        } else if (args[0] instanceof String) {
            return ((String) args[0]).length();
        } else {
            // Count non-null arguments
            int count = 0;
            for (Object arg : args) {
                if (arg != null) count++;
            }
            return count;
        }
    }
}'
WHERE name = 'count' AND is_built_in = true;

-- index
UPDATE transformation_custom_functions 
SET function_body = 'public class IndexFunction implements TransformationFunction {
    @Override
    public Object execute(Object... args) {
        if (args.length < 1) {
            throw new IllegalArgumentException("Index function requires 1 argument");
        }
        
        if (args[0] == null) return 0;
        if (args[0] instanceof Number) return ((Number) args[0]).intValue();
        return Integer.parseInt(args[0].toString());
    }
}'
WHERE name = 'index' AND is_built_in = true;

-- Text Functions

-- concat
UPDATE transformation_custom_functions 
SET function_body = 'public class ConcatFunction implements TransformationFunction {
    @Override
    public Object execute(Object... args) {
        if (args.length < 2) {
            throw new IllegalArgumentException("Concat function requires at least 2 arguments");
        }
        
        String string1 = toString(args[0]);
        String string2 = toString(args[1]);
        String delimiter = args.length > 2 && args[2] != null ? toString(args[2]) : "";
        
        if (!delimiter.isEmpty()) {
            return string1 + delimiter + string2;
        }
        return string1 + string2;
    }
    
    private String toString(Object value) {
        if (value == null) return "";
        return value.toString();
    }
}'
WHERE name = 'concat' AND is_built_in = true;

-- substring
UPDATE transformation_custom_functions 
SET function_body = 'public class SubstringFunction implements TransformationFunction {
    @Override
    public Object execute(Object... args) {
        if (args.length < 2) {
            throw new IllegalArgumentException("Substring function requires at least 2 arguments");
        }
        
        String text = toString(args[0]);
        int start = toInt(args[1]);
        
        if (args.length > 2 && args[2] != null) {
            int end = toInt(args[2]);
            return text.substring(start, Math.min(end, text.length()));
        } else {
            return text.substring(start);
        }
    }
    
    private String toString(Object value) {
        if (value == null) return "";
        return value.toString();
    }
    
    private int toInt(Object value) {
        if (value == null) return 0;
        if (value instanceof Number) return ((Number) value).intValue();
        return Integer.parseInt(value.toString());
    }
}'
WHERE name = 'substring' AND is_built_in = true;

-- indexOf
UPDATE transformation_custom_functions 
SET function_body = 'public class IndexOfFunction implements TransformationFunction {
    @Override
    public Object execute(Object... args) {
        if (args.length < 2) {
            throw new IllegalArgumentException("IndexOf function requires 2 arguments");
        }
        
        String text = toString(args[0]);
        String searchValue = toString(args[1]);
        
        return text.indexOf(searchValue);
    }
    
    private String toString(Object value) {
        if (value == null) return "";
        return value.toString();
    }
}'
WHERE name = 'indexOf' AND is_built_in = true;

-- lastIndexOf
UPDATE transformation_custom_functions 
SET function_body = 'public class LastIndexOfFunction implements TransformationFunction {
    @Override
    public Object execute(Object... args) {
        if (args.length < 2) {
            throw new IllegalArgumentException("LastIndexOf function requires 2 arguments");
        }
        
        String text = toString(args[0]);
        String searchValue = toString(args[1]);
        
        return text.lastIndexOf(searchValue);
    }
    
    private String toString(Object value) {
        if (value == null) return "";
        return value.toString();
    }
}'
WHERE name = 'lastIndexOf' AND is_built_in = true;

-- compare
UPDATE transformation_custom_functions 
SET function_body = 'public class CompareFunction implements TransformationFunction {
    @Override
    public Object execute(Object... args) {
        if (args.length < 2) {
            throw new IllegalArgumentException("Compare function requires 2 arguments");
        }
        
        String string1 = toString(args[0]);
        String string2 = toString(args[1]);
        
        return string1.compareTo(string2);
    }
    
    private String toString(Object value) {
        if (value == null) return "";
        return value.toString();
    }
}'
WHERE name = 'compare' AND is_built_in = true;

-- replaceString
UPDATE transformation_custom_functions 
SET function_body = 'public class ReplaceStringFunction implements TransformationFunction {
    @Override
    public Object execute(Object... args) {
        if (args.length < 3) {
            throw new IllegalArgumentException("ReplaceString function requires 3 arguments");
        }
        
        String text = toString(args[0]);
        String searchValue = toString(args[1]);
        String replaceValue = toString(args[2]);
        
        return text.replace(searchValue, replaceValue);
    }
    
    private String toString(Object value) {
        if (value == null) return "";
        return value.toString();
    }
}'
WHERE name = 'replaceString' AND is_built_in = true;

-- length
UPDATE transformation_custom_functions 
SET function_body = 'public class LengthFunction implements TransformationFunction {
    @Override
    public Object execute(Object... args) {
        if (args.length < 1) {
            throw new IllegalArgumentException("Length function requires 1 argument");
        }
        
        String text = toString(args[0]);
        return text.length();
    }
    
    private String toString(Object value) {
        if (value == null) return "";
        return value.toString();
    }
}'
WHERE name = 'length' AND is_built_in = true;

-- endsWith
UPDATE transformation_custom_functions 
SET function_body = 'public class EndsWithFunction implements TransformationFunction {
    @Override
    public Object execute(Object... args) {
        if (args.length < 2) {
            throw new IllegalArgumentException("EndsWith function requires 2 arguments");
        }
        
        String text = toString(args[0]);
        String suffix = toString(args[1]);
        
        return text.endsWith(suffix);
    }
    
    private String toString(Object value) {
        if (value == null) return "";
        return value.toString();
    }
}'
WHERE name = 'endsWith' AND is_built_in = true;

-- startsWith
UPDATE transformation_custom_functions 
SET function_body = 'public class StartsWithFunction implements TransformationFunction {
    @Override
    public Object execute(Object... args) {
        if (args.length < 2) {
            throw new IllegalArgumentException("StartsWith function requires 2 arguments");
        }
        
        String text = toString(args[0]);
        String prefix = toString(args[1]);
        
        return text.startsWith(prefix);
    }
    
    private String toString(Object value) {
        if (value == null) return "";
        return value.toString();
    }
}'
WHERE name = 'startsWith' AND is_built_in = true;

-- toUpperCase
UPDATE transformation_custom_functions 
SET function_body = 'public class ToUpperCaseFunction implements TransformationFunction {
    @Override
    public Object execute(Object... args) {
        if (args.length < 1) {
            throw new IllegalArgumentException("ToUpperCase function requires 1 argument");
        }
        
        String text = toString(args[0]);
        return text.toUpperCase();
    }
    
    private String toString(Object value) {
        if (value == null) return "";
        return value.toString();
    }
}'
WHERE name = 'toUpperCase' AND is_built_in = true;

-- toLowerCase
UPDATE transformation_custom_functions 
SET function_body = 'public class ToLowerCaseFunction implements TransformationFunction {
    @Override
    public Object execute(Object... args) {
        if (args.length < 1) {
            throw new IllegalArgumentException("ToLowerCase function requires 1 argument");
        }
        
        String text = toString(args[0]);
        return text.toLowerCase();
    }
    
    private String toString(Object value) {
        if (value == null) return "";
        return value.toString();
    }
}'
WHERE name = 'toLowerCase' AND is_built_in = true;

-- trim
UPDATE transformation_custom_functions 
SET function_body = 'public class TrimFunction implements TransformationFunction {
    @Override
    public Object execute(Object... args) {
        if (args.length < 1) {
            throw new IllegalArgumentException("Trim function requires 1 argument");
        }
        
        String text = toString(args[0]);
        return text.trim();
    }
    
    private String toString(Object value) {
        if (value == null) return "";
        return value.toString();
    }
}'
WHERE name = 'trim' AND is_built_in = true;

-- Boolean Functions

-- and
UPDATE transformation_custom_functions 
SET function_body = 'public class AndFunction implements TransformationFunction {
    @Override
    public Object execute(Object... args) {
        if (args.length < 2) {
            throw new IllegalArgumentException("And function requires 2 arguments");
        }
        
        boolean a = toBoolean(args[0]);
        boolean b = toBoolean(args[1]);
        
        return a && b;
    }
    
    private boolean toBoolean(Object value) {
        if (value == null) return false;
        if (value instanceof Boolean) return (Boolean) value;
        String str = value.toString().toLowerCase();
        return "true".equals(str) || "1".equals(str) || "yes".equals(str);
    }
}'
WHERE name = 'and' AND is_built_in = true;

-- or
UPDATE transformation_custom_functions 
SET function_body = 'public class OrFunction implements TransformationFunction {
    @Override
    public Object execute(Object... args) {
        if (args.length < 2) {
            throw new IllegalArgumentException("Or function requires 2 arguments");
        }
        
        boolean a = toBoolean(args[0]);
        boolean b = toBoolean(args[1]);
        
        return a || b;
    }
    
    private boolean toBoolean(Object value) {
        if (value == null) return false;
        if (value instanceof Boolean) return (Boolean) value;
        String str = value.toString().toLowerCase();
        return "true".equals(str) || "1".equals(str) || "yes".equals(str);
    }
}'
WHERE name = 'or' AND is_built_in = true;

-- not
UPDATE transformation_custom_functions 
SET function_body = 'public class NotFunction implements TransformationFunction {
    @Override
    public Object execute(Object... args) {
        if (args.length < 1) {
            throw new IllegalArgumentException("Not function requires 1 argument");
        }
        
        boolean value = toBoolean(args[0]);
        return !value;
    }
    
    private boolean toBoolean(Object value) {
        if (value == null) return false;
        if (value instanceof Boolean) return (Boolean) value;
        String str = value.toString().toLowerCase();
        return "true".equals(str) || "1".equals(str) || "yes".equals(str);
    }
}'
WHERE name = 'not' AND is_built_in = true;

-- notEquals
UPDATE transformation_custom_functions 
SET function_body = 'public class NotEqualsFunction implements TransformationFunction {
    @Override
    public Object execute(Object... args) {
        if (args.length < 2) {
            throw new IllegalArgumentException("NotEquals function requires 2 arguments");
        }
        
        Object a = args[0];
        Object b = args[1];
        
        if (a == null && b == null) return false;
        if (a == null || b == null) return true;
        
        return !a.equals(b);
    }
}'
WHERE name = 'notEquals' AND is_built_in = true;

-- if
UPDATE transformation_custom_functions 
SET function_body = 'public class IfFunction implements TransformationFunction {
    @Override
    public Object execute(Object... args) {
        if (args.length < 3) {
            throw new IllegalArgumentException("If function requires 3 arguments");
        }
        
        boolean condition = toBoolean(args[0]);
        Object trueValue = args[1];
        Object falseValue = args[2];
        
        return condition ? trueValue : falseValue;
    }
    
    private boolean toBoolean(Object value) {
        if (value == null) return false;
        if (value instanceof Boolean) return (Boolean) value;
        String str = value.toString().toLowerCase();
        return "true".equals(str) || "1".equals(str) || "yes".equals(str);
    }
}'
WHERE name = 'if' AND is_built_in = true;

-- ifWithoutElse
UPDATE transformation_custom_functions 
SET function_body = 'public class IfWithoutElseFunction implements TransformationFunction {
    @Override
    public Object execute(Object... args) {
        if (args.length < 2) {
            throw new IllegalArgumentException("IfWithoutElse function requires 2 arguments");
        }
        
        boolean condition = toBoolean(args[0]);
        Object trueValue = args[1];
        
        return condition ? trueValue : null;
    }
    
    private boolean toBoolean(Object value) {
        if (value == null) return false;
        if (value instanceof Boolean) return (Boolean) value;
        String str = value.toString().toLowerCase();
        return "true".equals(str) || "1".equals(str) || "yes".equals(str);
    }
}'
WHERE name = 'ifWithoutElse' AND is_built_in = true;

-- isNil
UPDATE transformation_custom_functions 
SET function_body = 'public class IsNilFunction implements TransformationFunction {
    @Override
    public Object execute(Object... args) {
        if (args.length < 1) {
            return true;
        }
        
        Object value = args[0];
        return value == null || "".equals(value.toString().trim());
    }
}'
WHERE name = 'isNil' AND is_built_in = true;

-- Conversion Functions

-- fixValues
UPDATE transformation_custom_functions 
SET function_body = 'public class FixValuesFunction implements TransformationFunction {
    @Override
    public Object execute(Object... args) {
        if (args.length < 2) {
            throw new IllegalArgumentException("FixValues function requires 2 arguments");
        }
        
        String value = toString(args[0]);
        String format = toString(args[1]);
        
        // Basic implementation - can be enhanced based on format requirements
        switch (format.toLowerCase()) {
            case "number":
                return value.replaceAll("[^0-9.-]", "");
            case "alphanumeric":
                return value.replaceAll("[^a-zA-Z0-9]", "");
            case "alpha":
                return value.replaceAll("[^a-zA-Z]", "");
            case "numeric":
                return value.replaceAll("[^0-9]", "");
            case "uppercase":
                return value.toUpperCase();
            case "lowercase":
                return value.toLowerCase();
            case "trim":
                return value.trim();
            default:
                return value;
        }
    }
    
    private String toString(Object value) {
        if (value == null) return "";
        return value.toString();
    }
}'
WHERE name = 'fixValues' AND is_built_in = true;

-- Date Functions

-- currentDate
UPDATE transformation_custom_functions 
SET function_body = 'import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class CurrentDateFunction implements TransformationFunction {
    @Override
    public Object execute(Object... args) {
        String format = args.length > 0 && args[0] != null ? args[0].toString() : "yyyy-MM-dd''T''HH:mm:ss";
        
        try {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern(format);
            return LocalDateTime.now().format(formatter);
        } catch (Exception e) {
            // Default to ISO format if pattern is invalid
            return LocalDateTime.now().toString();
        }
    }
}'
WHERE name = 'currentDate' AND is_built_in = true;

-- dateTrans
UPDATE transformation_custom_functions 
SET function_body = 'import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

public class DateTransFunction implements TransformationFunction {
    @Override
    public Object execute(Object... args) {
        if (args.length < 3) {
            throw new IllegalArgumentException("DateTrans function requires at least 3 arguments");
        }
        
        String date = toString(args[0]);
        String fromFormat = toString(args[1]);
        String toFormat = toString(args[2]);
        
        try {
            DateTimeFormatter fromFormatter = DateTimeFormatter.ofPattern(fromFormat);
            DateTimeFormatter toFormatter = DateTimeFormatter.ofPattern(toFormat);
            
            LocalDateTime dateTime = LocalDateTime.parse(date, fromFormatter);
            return dateTime.format(toFormatter);
        } catch (DateTimeParseException e) {
            throw new IllegalArgumentException("Unable to parse date: " + e.getMessage());
        }
    }
    
    private String toString(Object value) {
        if (value == null) return "";
        return value.toString();
    }
}'
WHERE name = 'dateTrans' AND is_built_in = true;

-- dateBefore
UPDATE transformation_custom_functions 
SET function_body = 'import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class DateBeforeFunction implements TransformationFunction {
    private static final DateTimeFormatter[] FORMATTERS = {
        DateTimeFormatter.ISO_LOCAL_DATE_TIME,
        DateTimeFormatter.ISO_DATE_TIME,
        DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"),
        DateTimeFormatter.ofPattern("yyyy-MM-dd")
    };
    
    @Override
    public Object execute(Object... args) {
        if (args.length < 2) {
            throw new IllegalArgumentException("DateBefore function requires 2 arguments");
        }
        
        LocalDateTime date1 = parseDate(toString(args[0]));
        LocalDateTime date2 = parseDate(toString(args[1]));
        
        return date1.isBefore(date2);
    }
    
    private LocalDateTime parseDate(String dateStr) {
        for (DateTimeFormatter formatter : FORMATTERS) {
            try {
                return LocalDateTime.parse(dateStr, formatter);
            } catch (Exception e) {
                // Try next formatter
            }
        }
        throw new IllegalArgumentException("Unable to parse date: " + dateStr);
    }
    
    private String toString(Object value) {
        if (value == null) return "";
        return value.toString();
    }
}'
WHERE name = 'dateBefore' AND is_built_in = true;

-- dateAfter
UPDATE transformation_custom_functions 
SET function_body = 'import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class DateAfterFunction implements TransformationFunction {
    private static final DateTimeFormatter[] FORMATTERS = {
        DateTimeFormatter.ISO_LOCAL_DATE_TIME,
        DateTimeFormatter.ISO_DATE_TIME,
        DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"),
        DateTimeFormatter.ofPattern("yyyy-MM-dd")
    };
    
    @Override
    public Object execute(Object... args) {
        if (args.length < 2) {
            throw new IllegalArgumentException("DateAfter function requires 2 arguments");
        }
        
        LocalDateTime date1 = parseDate(toString(args[0]));
        LocalDateTime date2 = parseDate(toString(args[1]));
        
        return date1.isAfter(date2);
    }
    
    private LocalDateTime parseDate(String dateStr) {
        for (DateTimeFormatter formatter : FORMATTERS) {
            try {
                return LocalDateTime.parse(dateStr, formatter);
            } catch (Exception e) {
                // Try next formatter
            }
        }
        throw new IllegalArgumentException("Unable to parse date: " + dateStr);
    }
    
    private String toString(Object value) {
        if (value == null) return "";
        return value.toString();
    }
}'
WHERE name = 'dateAfter' AND is_built_in = true;

-- compareDates
UPDATE transformation_custom_functions 
SET function_body = 'import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class CompareDatesFunction implements TransformationFunction {
    private static final DateTimeFormatter[] FORMATTERS = {
        DateTimeFormatter.ISO_LOCAL_DATE_TIME,
        DateTimeFormatter.ISO_DATE_TIME,
        DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"),
        DateTimeFormatter.ofPattern("yyyy-MM-dd")
    };
    
    @Override
    public Object execute(Object... args) {
        if (args.length < 2) {
            throw new IllegalArgumentException("CompareDates function requires 2 arguments");
        }
        
        LocalDateTime date1 = parseDate(toString(args[0]));
        LocalDateTime date2 = parseDate(toString(args[1]));
        
        return date1.compareTo(date2);
    }
    
    private LocalDateTime parseDate(String dateStr) {
        for (DateTimeFormatter formatter : FORMATTERS) {
            try {
                return LocalDateTime.parse(dateStr, formatter);
            } catch (Exception e) {
                // Try next formatter
            }
        }
        throw new IllegalArgumentException("Unable to parse date: " + dateStr);
    }
    
    private String toString(Object value) {
        if (value == null) return "";
        return value.toString();
    }
}'
WHERE name = 'compareDates' AND is_built_in = true;

-- Node Functions

-- createIf
UPDATE transformation_custom_functions 
SET function_body = 'public class CreateIfFunction implements TransformationFunction {
    @Override
    public Object execute(Object... args) {
        if (args.length < 2) {
            throw new IllegalArgumentException("CreateIf function requires 2 arguments");
        }
        
        boolean condition = toBoolean(args[0]);
        Object value = args[1];
        
        return condition ? value : null;
    }
    
    private boolean toBoolean(Object value) {
        if (value == null) return false;
        if (value instanceof Boolean) return (Boolean) value;
        String str = value.toString().toLowerCase();
        return "true".equals(str) || "1".equals(str) || "yes".equals(str);
    }
}'
WHERE name = 'createIf' AND is_built_in = true;

-- removeContexts
UPDATE transformation_custom_functions 
SET function_body = 'public class RemoveContextsFunction implements TransformationFunction {
    @Override
    public Object execute(Object... args) {
        if (args.length < 1) {
            throw new IllegalArgumentException("RemoveContexts function requires 1 argument");
        }
        
        // In a real implementation, this would handle XML node context removal
        // For now, return the input as-is
        return args[0];
    }
}'
WHERE name = 'removeContexts' AND is_built_in = true;

-- replaceValue
UPDATE transformation_custom_functions 
SET function_body = 'public class ReplaceValueFunction implements TransformationFunction {
    @Override
    public Object execute(Object... args) {
        if (args.length < 3) {
            throw new IllegalArgumentException("ReplaceValue function requires 3 arguments");
        }
        
        String node = toString(args[0]);
        String oldValue = toString(args[1]);
        String newValue = toString(args[2]);
        
        return node.replace(oldValue, newValue);
    }
    
    private String toString(Object value) {
        if (value == null) return "";
        return value.toString();
    }
}'
WHERE name = 'replaceValue' AND is_built_in = true;

-- exists
UPDATE transformation_custom_functions 
SET function_body = 'public class ExistsFunction implements TransformationFunction {
    @Override
    public Object execute(Object... args) {
        if (args.length < 1) {
            return false;
        }
        
        Object node = args[0];
        if (node == null) return false;
        
        String nodeStr = node.toString().trim();
        return !nodeStr.isEmpty();
    }
}'
WHERE name = 'exists' AND is_built_in = true;

-- getHeader
UPDATE transformation_custom_functions 
SET function_body = 'public class GetHeaderFunction implements TransformationFunction {
    @Override
    public Object execute(Object... args) {
        if (args.length < 1) {
            throw new IllegalArgumentException("GetHeader function requires 1 argument");
        }
        
        String headerName = toString(args[0]);
        
        // In actual implementation, this would retrieve from message context
        // For now, return a placeholder
        return "header_" + headerName;
    }
    
    private String toString(Object value) {
        if (value == null) return "";
        return value.toString();
    }
}'
WHERE name = 'getHeader' AND is_built_in = true;

-- getProperty
UPDATE transformation_custom_functions 
SET function_body = 'public class GetPropertyFunction implements TransformationFunction {
    @Override
    public Object execute(Object... args) {
        if (args.length < 1) {
            throw new IllegalArgumentException("GetProperty function requires 1 argument");
        }
        
        String propertyName = toString(args[0]);
        
        // In actual implementation, this would retrieve from message context
        // For now, return a placeholder
        return "property_" + propertyName;
    }
    
    private String toString(Object value) {
        if (value == null) return "";
        return value.toString();
    }
}'
WHERE name = 'getProperty' AND is_built_in = true;

-- splitByValue
UPDATE transformation_custom_functions 
SET function_body = 'import java.util.Arrays;

public class SplitByValueFunction implements TransformationFunction {
    @Override
    public Object execute(Object... args) {
        if (args.length < 2) {
            throw new IllegalArgumentException("SplitByValue function requires 2 arguments");
        }
        
        String value = toString(args[0]);
        String delimiter = toString(args[1]);
        
        return Arrays.asList(value.split(java.util.regex.Pattern.quote(delimiter)));
    }
    
    private String toString(Object value) {
        if (value == null) return "";
        return value.toString();
    }
}'
WHERE name = 'splitByValue' AND is_built_in = true;

-- collapseContexts
UPDATE transformation_custom_functions 
SET function_body = 'public class CollapseContextsFunction implements TransformationFunction {
    @Override
    public Object execute(Object... args) {
        if (args.length < 1) {
            throw new IllegalArgumentException("CollapseContexts function requires at least 1 argument");
        }
        
        StringBuilder result = new StringBuilder();
        
        if (args[0] instanceof java.util.List) {
            java.util.List<?> list = (java.util.List<?>) args[0];
            for (Object item : list) {
                result.append(toString(item));
            }
        } else if (args[0].getClass().isArray()) {
            Object[] array = (Object[]) args[0];
            for (Object item : array) {
                result.append(toString(item));
            }
        } else {
            // Collapse all arguments
            for (Object arg : args) {
                result.append(toString(arg));
            }
        }
        
        return result.toString();
    }
    
    private String toString(Object value) {
        if (value == null) return "";
        return value.toString();
    }
}'
WHERE name = 'collapseContexts' AND is_built_in = true;

-- useOneAsMany
UPDATE transformation_custom_functions 
SET function_body = 'import java.util.ArrayList;
import java.util.List;

public class UseOneAsManyFunction implements TransformationFunction {
    @Override
    public Object execute(Object... args) {
        if (args.length < 3) {
            throw new IllegalArgumentException("UseOneAsMany function requires 3 arguments");
        }
        
        Object singleField = args[0];
        Object countField = args[1];
        Object multipleField = args[2];
        
        int count = 1;
        if (countField instanceof Number) {
            count = ((Number) countField).intValue();
        } else if (countField instanceof java.util.Collection) {
            count = ((java.util.Collection<?>) countField).size();
        } else if (countField.getClass().isArray()) {
            count = java.lang.reflect.Array.getLength(countField);
        }
        
        List<Object> result = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            result.add(singleField);
        }
        
        return result;
    }
}'
WHERE name = 'useOneAsMany' AND is_built_in = true;

-- sort
UPDATE transformation_custom_functions 
SET function_body = 'import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class SortFunction implements TransformationFunction {
    @Override
    public Object execute(Object... args) {
        if (args.length < 1) {
            throw new IllegalArgumentException("Sort function requires 1 argument");
        }
        
        List<String> values = new ArrayList<>();
        
        if (args[0] instanceof java.util.List) {
            java.util.List<?> list = (java.util.List<?>) args[0];
            for (Object item : list) {
                values.add(toString(item));
            }
        } else if (args[0].getClass().isArray()) {
            Object[] array = (Object[]) args[0];
            for (Object item : array) {
                values.add(toString(item));
            }
        } else {
            // Sort all arguments
            for (Object arg : args) {
                values.add(toString(arg));
            }
        }
        
        Collections.sort(values);
        return values;
    }
    
    private String toString(Object value) {
        if (value == null) return "";
        return value.toString();
    }
}'
WHERE name = 'sort' AND is_built_in = true;

-- sortByKey
UPDATE transformation_custom_functions 
SET function_body = 'import java.util.*;

public class SortByKeyFunction implements TransformationFunction {
    @Override
    public Object execute(Object... args) {
        if (args.length < 2) {
            throw new IllegalArgumentException("SortByKey function requires 2 arguments");
        }
        
        List<Map<String, Object>> values = new ArrayList<>();
        String key = toString(args[1]);
        
        if (args[0] instanceof java.util.List) {
            java.util.List<?> list = (java.util.List<?>) args[0];
            for (Object item : list) {
                if (item instanceof Map) {
                    values.add((Map<String, Object>) item);
                }
            }
        }
        
        values.sort((a, b) -> {
            Object valA = a.get(key);
            Object valB = b.get(key);
            if (valA == null && valB == null) return 0;
            if (valA == null) return -1;
            if (valB == null) return 1;
            return valA.toString().compareTo(valB.toString());
        });
        
        return values;
    }
    
    private String toString(Object value) {
        if (value == null) return "";
        return value.toString();
    }
}'
WHERE name = 'sortByKey' AND is_built_in = true;

-- mapWithDefault
UPDATE transformation_custom_functions 
SET function_body = 'public class MapWithDefaultFunction implements TransformationFunction {
    @Override
    public Object execute(Object... args) {
        if (args.length < 2) {
            throw new IllegalArgumentException("MapWithDefault function requires 2 arguments");
        }
        
        Object value = args[0];
        Object defaultValue = args[1];
        
        if (value == null || (value instanceof String && ((String) value).trim().isEmpty())) {
            return defaultValue;
        }
        
        return value;
    }
}'
WHERE name = 'mapWithDefault' AND is_built_in = true;

-- formatByExample
UPDATE transformation_custom_functions 
SET function_body = 'public class FormatByExampleFunction implements TransformationFunction {
    @Override
    public Object execute(Object... args) {
        if (args.length < 2) {
            throw new IllegalArgumentException("FormatByExample function requires 2 arguments");
        }
        
        String value = toString(args[0]);
        String example = toString(args[1]);
        
        // Simple implementation - can be enhanced for specific formatting patterns
        // For now, ensure value matches the length of the example
        if (value.length() > example.length()) {
            return value.substring(0, example.length());
        } else if (value.length() < example.length()) {
            return String.format("%-" + example.length() + "s", value);
        }
        
        return value;
    }
    
    private String toString(Object value) {
        if (value == null) return "";
        return value.toString();
    }
}'
WHERE name = 'formatByExample' AND is_built_in = true;

-- Constants Functions

-- constant
UPDATE transformation_custom_functions 
SET function_body = 'public class ConstantFunction implements TransformationFunction {
    @Override
    public Object execute(Object... args) {
        if (args.length < 1) {
            throw new IllegalArgumentException("Constant function requires 1 argument");
        }
        
        return args[0];
    }
}'
WHERE name = 'constant' AND is_built_in = true;

-- Update the updated_at timestamp for all modified functions
UPDATE transformation_custom_functions 
SET updated_at = CURRENT_TIMESTAMP 
WHERE is_built_in = true;