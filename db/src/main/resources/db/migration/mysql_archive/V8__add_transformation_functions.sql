-- V8__add_transformation_functions.sql
-- Create transformation_custom_functions table and populate with built-in functions

CREATE TABLE IF NOT EXISTS transformation_custom_functions (
    function_id VARCHAR(36) PRIMARY KEY,
    name VARCHAR(100) NOT NULL UNIQUE,
    description VARCHAR(500),
    language VARCHAR(20) NOT NULL,
    function_signature VARCHAR(500) NOT NULL,
    function_body TEXT NOT NULL,
    category VARCHAR(50),
    is_safe BOOLEAN NOT NULL DEFAULT FALSE,
    is_public BOOLEAN NOT NULL DEFAULT TRUE,
    is_built_in BOOLEAN NOT NULL DEFAULT FALSE,
    performance_class VARCHAR(20) NOT NULL DEFAULT 'NORMAL',
    version INT NOT NULL DEFAULT 1,
    created_by VARCHAR(100),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NULL,
    INDEX idx_function_name (name),
    INDEX idx_function_category (category),
    INDEX idx_function_built_in (is_built_in)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Create function_dependencies table
CREATE TABLE IF NOT EXISTS function_dependencies (
    function_id VARCHAR(36) NOT NULL,
    dependency VARCHAR(255),
    FOREIGN KEY (function_id) REFERENCES transformation_custom_functions(function_id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Create function_test_cases table
CREATE TABLE IF NOT EXISTS function_test_cases (
    function_id VARCHAR(36) NOT NULL,
    test_name VARCHAR(100),
    input_data TEXT,
    expected_output TEXT,
    test_description VARCHAR(500),
    FOREIGN KEY (function_id) REFERENCES transformation_custom_functions(function_id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Insert built-in Math functions
INSERT INTO transformation_custom_functions (function_id, name, description, language, function_signature, function_body, category, is_safe, is_public, is_built_in, created_by) VALUES
(UUID(), 'add', 'Add two numbers', 'JAVASCRIPT', 'add(a, b)', 'return a + b;', 'math', TRUE, TRUE, TRUE, 'system'),
(UUID(), 'subtract', 'Subtract two numbers', 'JAVASCRIPT', 'subtract(a, b)', 'return a - b;', 'math', TRUE, TRUE, TRUE, 'system'),
(UUID(), 'multiply', 'Multiply two numbers', 'JAVASCRIPT', 'multiply(a, b)', 'return a * b;', 'math', TRUE, TRUE, TRUE, 'system'),
(UUID(), 'divide', 'Divide two numbers', 'JAVASCRIPT', 'divide(a, b)', 'return a / b;', 'math', TRUE, TRUE, TRUE, 'system'),
(UUID(), 'equals', 'Check if two numbers are equal', 'JAVASCRIPT', 'equals(a, b)', 'return a === b;', 'math', TRUE, TRUE, TRUE, 'system'),
(UUID(), 'absolute', 'Get absolute value of a number', 'JAVASCRIPT', 'absolute(value)', 'return Math.abs(value);', 'math', TRUE, TRUE, TRUE, 'system'),
(UUID(), 'sqrt', 'Get square root of a number', 'JAVASCRIPT', 'sqrt(value)', 'return Math.sqrt(value);', 'math', TRUE, TRUE, TRUE, 'system'),
(UUID(), 'square', 'Square a number', 'JAVASCRIPT', 'square(value)', 'return value * value;', 'math', TRUE, TRUE, TRUE, 'system'),
(UUID(), 'sign', 'Get sign of a number (-1, 0, or 1)', 'JAVASCRIPT', 'sign(value)', 'return Math.sign(value);', 'math', TRUE, TRUE, TRUE, 'system'),
(UUID(), 'neg', 'Negate a number', 'JAVASCRIPT', 'neg(value)', 'return -value;', 'math', TRUE, TRUE, TRUE, 'system'),
(UUID(), 'inv', 'Get inverse of a number (1/x)', 'JAVASCRIPT', 'inv(value)', 'return 1 / value;', 'math', TRUE, TRUE, TRUE, 'system'),
(UUID(), 'power', 'Raise number to power', 'JAVASCRIPT', 'power(base, exponent)', 'return Math.pow(base, exponent);', 'math', TRUE, TRUE, TRUE, 'system'),
(UUID(), 'lesser', 'Check if first number is less than second', 'JAVASCRIPT', 'lesser(a, b)', 'return a < b;', 'math', TRUE, TRUE, TRUE, 'system'),
(UUID(), 'greater', 'Check if first number is greater than second', 'JAVASCRIPT', 'greater(a, b)', 'return a > b;', 'math', TRUE, TRUE, TRUE, 'system'),
(UUID(), 'max', 'Get maximum of two numbers', 'JAVASCRIPT', 'max(a, b)', 'return Math.max(a, b);', 'math', TRUE, TRUE, TRUE, 'system'),
(UUID(), 'min', 'Get minimum of two numbers', 'JAVASCRIPT', 'min(a, b)', 'return Math.min(a, b);', 'math', TRUE, TRUE, TRUE, 'system'),
(UUID(), 'ceil', 'Round number up to nearest integer', 'JAVASCRIPT', 'ceil(value)', 'return Math.ceil(value);', 'math', TRUE, TRUE, TRUE, 'system'),
(UUID(), 'floor', 'Round number down to nearest integer', 'JAVASCRIPT', 'floor(value)', 'return Math.floor(value);', 'math', TRUE, TRUE, TRUE, 'system'),
(UUID(), 'round', 'Round number to nearest integer', 'JAVASCRIPT', 'round(value)', 'return Math.round(value);', 'math', TRUE, TRUE, TRUE, 'system'),
(UUID(), 'counter', 'Generate incremental counter', 'JAVASCRIPT', 'counter(start, step)', 'return (start || 1) + (step || 1);', 'math', TRUE, TRUE, TRUE, 'system'),
(UUID(), 'formatNumber', 'Format number with specified total digits and decimal places', 'JAVASCRIPT', 'formatNumber(value, totalDigits, decimals)', 'var formatted = Number(value.toFixed(decimals || 2)); return formatted.toString().padStart(totalDigits, "0");', 'math', TRUE, TRUE, TRUE, 'system'),
(UUID(), 'sum', 'Sum multiple numbers', 'JAVASCRIPT', 'sum(values)', 'return values.reduce(function(acc, val) { return acc + val; }, 0);', 'math', TRUE, TRUE, TRUE, 'system'),
(UUID(), 'average', 'Calculate average of numbers', 'JAVASCRIPT', 'average(values)', 'return values.reduce(function(acc, val) { return acc + val; }, 0) / values.length;', 'math', TRUE, TRUE, TRUE, 'system'),
(UUID(), 'count', 'Count elements in array', 'JAVASCRIPT', 'count(values)', 'return values.length;', 'math', TRUE, TRUE, TRUE, 'system'),
(UUID(), 'index', 'Get current index in iteration', 'JAVASCRIPT', 'index(position)', 'return position;', 'math', TRUE, TRUE, TRUE, 'system');

-- Insert built-in Text functions
INSERT INTO transformation_custom_functions (function_id, name, description, language, function_signature, function_body, category, is_safe, is_public, is_built_in, created_by) VALUES
(UUID(), 'concat', 'Concatenate two strings with optional delimiter', 'JAVASCRIPT', 'concat(string1, string2, delimiter)', 'return delimiter ? string1 + delimiter + string2 : string1 + string2;', 'text', TRUE, TRUE, TRUE, 'system'),
(UUID(), 'substring', 'Extract substring from text', 'JAVASCRIPT', 'substring(text, start, end)', 'return text.substring(start, end);', 'text', TRUE, TRUE, TRUE, 'system'),
(UUID(), 'indexOf', 'Find index of substring', 'JAVASCRIPT', 'indexOf(text, searchValue)', 'return text.indexOf(searchValue);', 'text', TRUE, TRUE, TRUE, 'system'),
(UUID(), 'lastIndexOf', 'Find last index of substring', 'JAVASCRIPT', 'lastIndexOf(text, searchValue)', 'return text.lastIndexOf(searchValue);', 'text', TRUE, TRUE, TRUE, 'system'),
(UUID(), 'compare', 'Compare two strings lexicographically', 'JAVASCRIPT', 'compare(string1, string2)', 'return string1.localeCompare(string2);', 'text', TRUE, TRUE, TRUE, 'system'),
(UUID(), 'replaceString', 'Replace substring in text', 'JAVASCRIPT', 'replaceString(text, searchValue, replaceValue)', 'return text.replace(searchValue, replaceValue);', 'text', TRUE, TRUE, TRUE, 'system'),
(UUID(), 'length', 'Get length of string', 'JAVASCRIPT', 'length(text)', 'return text.length;', 'text', TRUE, TRUE, TRUE, 'system'),
(UUID(), 'endsWith', 'Check if string ends with specified suffix', 'JAVASCRIPT', 'endsWith(text, suffix)', 'return text.endsWith(suffix);', 'text', TRUE, TRUE, TRUE, 'system'),
(UUID(), 'startsWith', 'Check if string starts with specified prefix', 'JAVASCRIPT', 'startsWith(text, prefix)', 'return text.startsWith(prefix);', 'text', TRUE, TRUE, TRUE, 'system'),
(UUID(), 'toUpperCase', 'Convert text to uppercase', 'JAVASCRIPT', 'toUpperCase(text)', 'return text.toUpperCase();', 'text', TRUE, TRUE, TRUE, 'system'),
(UUID(), 'toLowerCase', 'Convert text to lowercase', 'JAVASCRIPT', 'toLowerCase(text)', 'return text.toLowerCase();', 'text', TRUE, TRUE, TRUE, 'system'),
(UUID(), 'trim', 'Remove whitespace from both ends', 'JAVASCRIPT', 'trim(text)', 'return text.trim();', 'text', TRUE, TRUE, TRUE, 'system');

-- Insert built-in Boolean functions
INSERT INTO transformation_custom_functions (function_id, name, description, language, function_signature, function_body, category, is_safe, is_public, is_built_in, created_by) VALUES
(UUID(), 'and', 'Logical AND operation', 'JAVASCRIPT', 'and(a, b)', 'return a && b;', 'boolean', TRUE, TRUE, TRUE, 'system'),
(UUID(), 'or', 'Logical OR operation', 'JAVASCRIPT', 'or(a, b)', 'return a || b;', 'boolean', TRUE, TRUE, TRUE, 'system'),
(UUID(), 'not', 'Logical NOT operation', 'JAVASCRIPT', 'not(value)', 'return !value;', 'boolean', TRUE, TRUE, TRUE, 'system'),
(UUID(), 'notEquals', 'Check if two values are not equal', 'JAVASCRIPT', 'notEquals(a, b)', 'return a !== b;', 'boolean', TRUE, TRUE, TRUE, 'system'),
(UUID(), 'if', 'Conditional logic with true/false branches', 'JAVASCRIPT', 'if(condition, trueValue, falseValue)', 'return condition ? trueValue : falseValue;', 'boolean', TRUE, TRUE, TRUE, 'system'),
(UUID(), 'ifWithoutElse', 'Conditional logic without else branch', 'JAVASCRIPT', 'ifWithoutElse(condition, trueValue)', 'return condition ? trueValue : null;', 'boolean', TRUE, TRUE, TRUE, 'system'),
(UUID(), 'isNil', 'Check if value is null or undefined', 'JAVASCRIPT', 'isNil(value)', 'return value == null;', 'boolean', TRUE, TRUE, TRUE, 'system');

-- Insert built-in Conversion functions
INSERT INTO transformation_custom_functions (function_id, name, description, language, function_signature, function_body, category, is_safe, is_public, is_built_in, created_by) VALUES
(UUID(), 'fixValues', 'Fix and validate values according to specified format', 'JAVASCRIPT', 'fixValues(value, format)', 'return value; // Implementation depends on format requirements', 'conversion', TRUE, TRUE, TRUE, 'system');

-- Insert built-in Date functions
INSERT INTO transformation_custom_functions (function_id, name, description, language, function_signature, function_body, category, is_safe, is_public, is_built_in, created_by) VALUES
(UUID(), 'currentDate', 'Get current date', 'JAVASCRIPT', 'currentDate(format)', 'return new Date().toISOString();', 'date', TRUE, TRUE, TRUE, 'system'),
(UUID(), 'dateTrans', 'Transform date format', 'JAVASCRIPT', 'dateTrans(date, fromFormat, toFormat, firstWeekday, minDays, lenient)', 'return date; // Complex implementation required', 'date', TRUE, TRUE, TRUE, 'system'),
(UUID(), 'dateBefore', 'Check if first date is before second date', 'JAVASCRIPT', 'dateBefore(date1, date2)', 'return new Date(date1) < new Date(date2);', 'date', TRUE, TRUE, TRUE, 'system'),
(UUID(), 'dateAfter', 'Check if first date is after second date', 'JAVASCRIPT', 'dateAfter(date1, date2)', 'return new Date(date1) > new Date(date2);', 'date', TRUE, TRUE, TRUE, 'system'),
(UUID(), 'compareDates', 'Compare two dates (-1, 0, 1)', 'JAVASCRIPT', 'compareDates(date1, date2)', 'var d1 = new Date(date1); var d2 = new Date(date2); return d1 < d2 ? -1 : d1 > d2 ? 1 : 0;', 'date', TRUE, TRUE, TRUE, 'system');

-- Insert built-in Node functions
INSERT INTO transformation_custom_functions (function_id, name, description, language, function_signature, function_body, category, is_safe, is_public, is_built_in, created_by) VALUES
(UUID(), 'createIf', 'Create conditional node structure', 'JAVASCRIPT', 'createIf(condition, value)', 'return condition ? value : null;', 'node', TRUE, TRUE, TRUE, 'system'),
(UUID(), 'removeContexts', 'Remove context from node structure', 'JAVASCRIPT', 'removeContexts(node)', 'return node; // Implementation depends on node structure', 'node', TRUE, TRUE, TRUE, 'system'),
(UUID(), 'replaceValue', 'Replace value in node structure', 'JAVASCRIPT', 'replaceValue(node, oldValue, newValue)', 'return node.replace(oldValue, newValue);', 'node', TRUE, TRUE, TRUE, 'system'),
(UUID(), 'exists', 'Check if node exists', 'JAVASCRIPT', 'exists(node)', 'return node != null && node !== "";', 'node', TRUE, TRUE, TRUE, 'system'),
(UUID(), 'getHeader', 'Get header value from message', 'JAVASCRIPT', 'getHeader(headerName)', 'return "header_" + headerName; // Runtime implementation required', 'node', TRUE, TRUE, TRUE, 'system'),
(UUID(), 'getProperty', 'Get property value', 'JAVASCRIPT', 'getProperty(propertyName)', 'return "property_" + propertyName; // Runtime implementation required', 'node', TRUE, TRUE, TRUE, 'system'),
(UUID(), 'splitByValue', 'Split node by delimiter', 'JAVASCRIPT', 'splitByValue(value, delimiter)', 'return value.split(delimiter);', 'node', TRUE, TRUE, TRUE, 'system'),
(UUID(), 'collapseContexts', 'Collapse multiple contexts into one', 'JAVASCRIPT', 'collapseContexts(contexts)', 'return contexts.join("");', 'node', TRUE, TRUE, TRUE, 'system'),
(UUID(), 'useOneAsMany', 'Replicate a field that occurs once to pair with fields that occur multiple times', 'JAVASCRIPT', 'useOneAsMany(singleField, countField, multipleField)', 'return [singleField]; // Complex implementation required', 'node', TRUE, TRUE, TRUE, 'system'),
(UUID(), 'sort', 'Sort array of values', 'JAVASCRIPT', 'sort(values)', 'return values.slice().sort();', 'node', TRUE, TRUE, TRUE, 'system'),
(UUID(), 'sortByKey', 'Sort array by specific key', 'JAVASCRIPT', 'sortByKey(values, key)', 'return values.slice().sort(function(a, b) { return a[key] - b[key]; });', 'node', TRUE, TRUE, TRUE, 'system'),
(UUID(), 'mapWithDefault', 'Map value with default fallback', 'JAVASCRIPT', 'mapWithDefault(value, defaultValue)', 'return value || defaultValue;', 'node', TRUE, TRUE, TRUE, 'system'),
(UUID(), 'formatByExample', 'Format value using example pattern', 'JAVASCRIPT', 'formatByExample(value, example)', 'return value; // Complex implementation required', 'node', TRUE, TRUE, TRUE, 'system');

-- Insert built-in Constants functions
INSERT INTO transformation_custom_functions (function_id, name, description, language, function_signature, function_body, category, is_safe, is_public, is_built_in, created_by) VALUES
(UUID(), 'constant', 'Set a fixed value', 'JAVASCRIPT', 'constant(value)', 'return value;', 'constants', TRUE, TRUE, TRUE, 'system');