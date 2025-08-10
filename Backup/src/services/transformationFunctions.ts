// Client-side transformation functions for preview and validation
export interface TransformationFunction {
  name: string;
  category: string;
  description: string;
  parameters: Array<{
    name: string;
    type: 'string' | 'number' | 'boolean' | 'array';
    required: boolean;
    description?: string;
  }>;
  execute: (...args: any[]) => any;
  javaTemplate: string;
}

// Math Functions
const mathFunctions: TransformationFunction[] = [
  {
    name: 'add',
    category: 'math',
    description: 'Add two numbers',
    parameters: [
      { name: 'a', type: 'number', required: true, description: 'First number' },
      { name: 'b', type: 'number', required: true, description: 'Second number' }
    ],
    execute: (a: number, b: number) => a + b,
    javaTemplate: 'add({0}, {1})'
  },
  {
    name: 'subtract',
    category: 'math',
    description: 'Subtract two numbers',
    parameters: [
      { name: 'a', type: 'number', required: true, description: 'First number' },
      { name: 'b', type: 'number', required: true, description: 'Second number' }
    ],
    execute: (a: number, b: number) => a - b,
    javaTemplate: 'subtract({0}, {1})'
  },
  {
    name: 'multiply',
    category: 'math',
    description: 'Multiply two numbers',
    parameters: [
      { name: 'a', type: 'number', required: true },
      { name: 'b', type: 'number', required: true }
    ],
    execute: (a: number, b: number) => a * b,
    javaTemplate: 'multiply({0}, {1})'
  },
  {
    name: 'divide',
    category: 'math',
    description: 'Divide two numbers',
    parameters: [
      { name: 'a', type: 'number', required: true },
      { name: 'b', type: 'number', required: true }
    ],
    execute: (a: number, b: number) => a / b,
    javaTemplate: 'divide({0}, {1})'
  },
  {
    name: 'equals',
    category: 'math',
    description: 'Check if two numbers are equal',
    parameters: [
      { name: 'a', type: 'number', required: true },
      { name: 'b', type: 'number', required: true }
    ],
    execute: (a: number, b: number) => a === b,
    javaTemplate: 'equals({0}, {1})'
  },
  {
    name: 'absolute',
    category: 'math',
    description: 'Get absolute value of a number',
    parameters: [
      { name: 'value', type: 'number', required: true }
    ],
    execute: (value: number) => Math.abs(value),
    javaTemplate: 'absolute({0})'
  },
  {
    name: 'sqrt',
    category: 'math',
    description: 'Get square root of a number',
    parameters: [
      { name: 'value', type: 'number', required: true }
    ],
    execute: (value: number) => Math.sqrt(value),
    javaTemplate: 'sqrt({0})'
  },
  {
    name: 'square',
    category: 'math',
    description: 'Square a number',
    parameters: [
      { name: 'value', type: 'number', required: true }
    ],
    execute: (value: number) => value * value,
    javaTemplate: 'square({0})'
  },
  {
    name: 'sign',
    category: 'math',
    description: 'Get sign of a number (-1, 0, or 1)',
    parameters: [
      { name: 'value', type: 'number', required: true }
    ],
    execute: (value: number) => Math.sign(value),
    javaTemplate: 'sign({0})'
  },
  {
    name: 'neg',
    category: 'math',
    description: 'Negate a number',
    parameters: [
      { name: 'value', type: 'number', required: true }
    ],
    execute: (value: number) => -value,
    javaTemplate: 'neg({0})'
  },
  {
    name: 'inv',
    category: 'math',
    description: 'Get inverse of a number (1/x)',
    parameters: [
      { name: 'value', type: 'number', required: true }
    ],
    execute: (value: number) => 1 / value,
    javaTemplate: 'inv({0})'
  },
  {
    name: 'power',
    category: 'math',
    description: 'Raise number to power',
    parameters: [
      { name: 'base', type: 'number', required: true },
      { name: 'exponent', type: 'number', required: true }
    ],
    execute: (base: number, exponent: number) => Math.pow(base, exponent),
    javaTemplate: 'power({0}, {1})'
  },
  {
    name: 'lesser',
    category: 'math',
    description: 'Check if first number is less than second',
    parameters: [
      { name: 'a', type: 'number', required: true },
      { name: 'b', type: 'number', required: true }
    ],
    execute: (a: number, b: number) => a < b,
    javaTemplate: 'lesser({0}, {1})'
  },
  {
    name: 'greater',
    category: 'math',
    description: 'Check if first number is greater than second',
    parameters: [
      { name: 'a', type: 'number', required: true },
      { name: 'b', type: 'number', required: true }
    ],
    execute: (a: number, b: number) => a > b,
    javaTemplate: 'greater({0}, {1})'
  },
  {
    name: 'max',
    category: 'math',
    description: 'Get maximum of two numbers',
    parameters: [
      { name: 'a', type: 'number', required: true },
      { name: 'b', type: 'number', required: true }
    ],
    execute: (a: number, b: number) => Math.max(a, b),
    javaTemplate: 'max({0}, {1})'
  },
  {
    name: 'min',
    category: 'math',
    description: 'Get minimum of two numbers',
    parameters: [
      { name: 'a', type: 'number', required: true },
      { name: 'b', type: 'number', required: true }
    ],
    execute: (a: number, b: number) => Math.min(a, b),
    javaTemplate: 'min({0}, {1})'
  },
  {
    name: 'ceil',
    category: 'math',
    description: 'Round number up to nearest integer',
    parameters: [
      { name: 'value', type: 'number', required: true }
    ],
    execute: (value: number) => Math.ceil(value),
    javaTemplate: 'ceil({0})'
  },
  {
    name: 'floor',
    category: 'math',
    description: 'Round number down to nearest integer',
    parameters: [
      { name: 'value', type: 'number', required: true }
    ],
    execute: (value: number) => Math.floor(value),
    javaTemplate: 'floor({0})'
  },
  {
    name: 'round',
    category: 'math',
    description: 'Round number to nearest integer',
    parameters: [
      { name: 'value', type: 'number', required: true }
    ],
    execute: (value: number) => Math.round(value),
    javaTemplate: 'round({0})'
  },
  {
    name: 'counter',
    category: 'math',
    description: 'Generate incremental counter',
    parameters: [
      { name: 'start', type: 'number', required: false, description: 'Starting value' },
      { name: 'step', type: 'number', required: false, description: 'Increment step' }
    ],
    execute: (start: number = 1, step: number = 1) => start + step,
    javaTemplate: 'counter({0}, {1})'
  },
  {
    name: 'formatNumber',
    category: 'math',
    description: 'Format number with specified total digits and decimal places',
    parameters: [
      { name: 'value', type: 'number', required: true, description: 'Number to format' },
      { name: 'totalDigits', type: 'number', required: true, description: 'Total number of digits required (adds trailing zeros)' },
      { name: 'decimals', type: 'number', required: false, description: 'Number of decimal places' }
    ],
    execute: (value: number, totalDigits: number, decimals: number = 2) => {
      const formatted = Number(value.toFixed(decimals));
      const str = formatted.toString();
      return str.padStart(totalDigits, '0');
    },
    javaTemplate: 'formatNumber({0}, {1}, {2})'
  },
  {
    name: 'sum',
    category: 'math',
    description: 'Sum multiple numbers',
    parameters: [
      { name: 'values', type: 'array', required: true, description: 'Array of numbers to sum' }
    ],
    execute: (values: number[]) => values.reduce((acc, val) => acc + val, 0),
    javaTemplate: 'sum({0})'
  },
  {
    name: 'average',
    category: 'math',
    description: 'Calculate average of numbers',
    parameters: [
      { name: 'values', type: 'array', required: true, description: 'Array of numbers' }
    ],
    execute: (values: number[]) => values.reduce((acc, val) => acc + val, 0) / values.length,
    javaTemplate: 'average({0})'
  },
  {
    name: 'count',
    category: 'math',
    description: 'Count elements in array',
    parameters: [
      { name: 'values', type: 'array', required: true, description: 'Array to count' }
    ],
    execute: (values: any[]) => values.length,
    javaTemplate: 'count({0})'
  },
  {
    name: 'index',
    category: 'math',
    description: 'Get current index in iteration',
    parameters: [
      { name: 'position', type: 'number', required: true, description: 'Current position' }
    ],
    execute: (position: number) => position,
    javaTemplate: 'index({0})'
  }
];

// Text Functions
const textFunctions: TransformationFunction[] = [
  {
    name: 'concat',
    category: 'text',
    description: 'Concatenate two strings with optional delimiter',
    parameters: [
      { name: 'string1', type: 'string', required: true, description: 'First string to concatenate' },
      { name: 'string2', type: 'string', required: true, description: 'Second string to concatenate' },
      { name: 'delimiter', type: 'string', required: false, description: 'Optional delimiter between strings' }
    ],
    execute: (string1: string, string2: string, delimiter?: string) => {
      if (delimiter) {
        return string1 + delimiter + string2;
      }
      return string1 + string2;
    },
    javaTemplate: 'concat({0}, {1}, {2})'
  },
  {
    name: 'substring',
    category: 'text',
    description: 'Extract substring from text',
    parameters: [
      { name: 'text', type: 'string', required: true },
      { name: 'start', type: 'number', required: true },
      { name: 'end', type: 'number', required: false }
    ],
    execute: (text: string, start: number, end?: number) => text.substring(start, end),
    javaTemplate: 'substring({0}, {1}, {2})'
  },
  {
    name: 'equals',
    category: 'text',
    description: 'Check if two strings are equal',
    parameters: [
      { name: 'string1', type: 'string', required: true },
      { name: 'string2', type: 'string', required: true }
    ],
    execute: (string1: string, string2: string) => string1 === string2,
    javaTemplate: 'equals({0}, {1})'
  },
  {
    name: 'indexOf',
    category: 'text',
    description: 'Find index of substring',
    parameters: [
      { name: 'text', type: 'string', required: true },
      { name: 'searchValue', type: 'string', required: true }
    ],
    execute: (text: string, searchValue: string) => text.indexOf(searchValue),
    javaTemplate: 'indexOf({0}, {1})'
  },
  {
    name: 'lastIndexOf',
    category: 'text',
    description: 'Find last index of substring',
    parameters: [
      { name: 'text', type: 'string', required: true },
      { name: 'searchValue', type: 'string', required: true }
    ],
    execute: (text: string, searchValue: string) => text.lastIndexOf(searchValue),
    javaTemplate: 'lastIndexOf({0}, {1})'
  },
  {
    name: 'compare',
    category: 'text',
    description: 'Compare two strings lexicographically',
    parameters: [
      { name: 'string1', type: 'string', required: true },
      { name: 'string2', type: 'string', required: true }
    ],
    execute: (string1: string, string2: string) => string1.localeCompare(string2),
    javaTemplate: 'compare({0}, {1})'
  },
  {
    name: 'replaceString',
    category: 'text',
    description: 'Replace substring in text',
    parameters: [
      { name: 'text', type: 'string', required: true },
      { name: 'searchValue', type: 'string', required: true },
      { name: 'replaceValue', type: 'string', required: true }
    ],
    execute: (text: string, searchValue: string, replaceValue: string) => text.replace(searchValue, replaceValue),
    javaTemplate: 'replaceString({0}, {1}, {2})'
  },
  {
    name: 'length',
    category: 'text',
    description: 'Get length of string',
    parameters: [
      { name: 'text', type: 'string', required: true }
    ],
    execute: (text: string) => text.length,
    javaTemplate: 'length({0})'
  },
  {
    name: 'endsWith',
    category: 'text',
    description: 'Check if string ends with specified suffix',
    parameters: [
      { name: 'text', type: 'string', required: true },
      { name: 'suffix', type: 'string', required: true }
    ],
    execute: (text: string, suffix: string) => text.endsWith(suffix),
    javaTemplate: 'endsWith({0}, {1})'
  },
  {
    name: 'startsWith',
    category: 'text',
    description: 'Check if string starts with specified prefix',
    parameters: [
      { name: 'text', type: 'string', required: true },
      { name: 'prefix', type: 'string', required: true }
    ],
    execute: (text: string, prefix: string) => text.startsWith(prefix),
    javaTemplate: 'startsWith({0}, {1})'
  },
  {
    name: 'toUpperCase',
    category: 'text',
    description: 'Convert text to uppercase',
    parameters: [
      { name: 'text', type: 'string', required: true }
    ],
    execute: (text: string) => text.toUpperCase(),
    javaTemplate: 'toUpperCase({0})'
  },
  {
    name: 'toLowerCase',
    category: 'text',
    description: 'Convert text to lowercase',
    parameters: [
      { name: 'text', type: 'string', required: true }
    ],
    execute: (text: string) => text.toLowerCase(),
    javaTemplate: 'toLowerCase({0})'
  },
  {
    name: 'trim',
    category: 'text',
    description: 'Remove whitespace from both ends',
    parameters: [
      { name: 'text', type: 'string', required: true }
    ],
    execute: (text: string) => text.trim(),
    javaTemplate: 'trim({0})'
  }
];

// Boolean Functions
const booleanFunctions: TransformationFunction[] = [
  {
    name: 'and',
    category: 'boolean',
    description: 'Logical AND operation',
    parameters: [
      { name: 'a', type: 'boolean', required: true },
      { name: 'b', type: 'boolean', required: true }
    ],
    execute: (a: boolean, b: boolean) => a && b,
    javaTemplate: 'and({0}, {1})'
  },
  {
    name: 'or',
    category: 'boolean',
    description: 'Logical OR operation',
    parameters: [
      { name: 'a', type: 'boolean', required: true },
      { name: 'b', type: 'boolean', required: true }
    ],
    execute: (a: boolean, b: boolean) => a || b,
    javaTemplate: 'or({0}, {1})'
  },
  {
    name: 'not',
    category: 'boolean',
    description: 'Logical NOT operation',
    parameters: [
      { name: 'value', type: 'boolean', required: true }
    ],
    execute: (value: boolean) => !value,
    javaTemplate: 'not({0})'
  },
  {
    name: 'equals',
    category: 'boolean',
    description: 'Check if two boolean values are equal',
    parameters: [
      { name: 'a', type: 'boolean', required: true },
      { name: 'b', type: 'boolean', required: true }
    ],
    execute: (a: boolean, b: boolean) => a === b,
    javaTemplate: 'equals({0}, {1})'
  },
  {
    name: 'notEquals',
    category: 'boolean',
    description: 'Check if two values are not equal',
    parameters: [
      { name: 'a', type: 'string', required: true },
      { name: 'b', type: 'string', required: true }
    ],
    execute: (a: any, b: any) => a !== b,
    javaTemplate: 'notEquals({0}, {1})'
  },
  {
    name: 'if',
    category: 'boolean',
    description: 'Conditional logic with true/false branches',
    parameters: [
      { name: 'condition', type: 'boolean', required: true },
      { name: 'trueValue', type: 'string', required: true },
      { name: 'falseValue', type: 'string', required: true }
    ],
    execute: (condition: boolean, trueValue: any, falseValue: any) => condition ? trueValue : falseValue,
    javaTemplate: 'if({0}, {1}, {2})'
  },
  {
    name: 'ifWithoutElse',
    category: 'boolean',
    description: 'Conditional logic without else branch',
    parameters: [
      { name: 'condition', type: 'boolean', required: true },
      { name: 'trueValue', type: 'string', required: true }
    ],
    execute: (condition: boolean, trueValue: any) => condition ? trueValue : null,
    javaTemplate: 'ifWithoutElse({0}, {1})'
  },
  {
    name: 'isNil',
    category: 'boolean',
    description: 'Check if value is null or undefined',
    parameters: [
      { name: 'value', type: 'string', required: true }
    ],
    execute: (value: any) => value == null,
    javaTemplate: 'isNil({0})'
  }
];

// Conversion Functions
const conversionFunctions: TransformationFunction[] = [
  {
    name: 'fixValues',
    category: 'conversion',
    description: 'Fix and validate values according to specified format',
    parameters: [
      { name: 'value', type: 'string', required: true, description: 'Value to fix' },
      { name: 'format', type: 'string', required: true, description: 'Target format' }
    ],
    execute: (value: string, format: string) => value, // Simplified for preview
    javaTemplate: 'fixValues({0}, {1})'
  }
];

// Date Functions
const dateFunctions: TransformationFunction[] = [
  {
    name: 'currentDate',
    category: 'date',
    description: 'Get current date',
    parameters: [
      { name: 'format', type: 'string', required: false, description: 'Date format (optional)' }
    ],
    execute: (format?: string) => new Date().toISOString(),
    javaTemplate: 'currentDate({0})'
  },
  {
    name: 'dateTrans',
    category: 'date',
    description: 'Transform date format',
    parameters: [
      { name: 'date', type: 'string', required: true, description: 'Input date' },
      { name: 'fromFormat', type: 'string', required: true, description: 'Input date format' },
      { name: 'toFormat', type: 'string', required: true, description: 'Output date format' },
      { name: 'firstWeekday', type: 'string', required: false, description: 'First day of week (Sunday, Monday, etc.)' },
      { name: 'minDays', type: 'number', required: false, description: 'Minimum days in first week' },
      { name: 'lenient', type: 'boolean', required: false, description: 'Enable lenient parsing' }
    ],
    execute: (date: string, fromFormat: string, toFormat: string, firstWeekday?: string, minDays?: number, lenient?: boolean) => date, // Simplified for preview
    javaTemplate: 'dateTrans({0}, {1}, {2}, {3}, {4}, {5})'
  },
  {
    name: 'dateBefore',
    category: 'date',
    description: 'Check if first date is before second date',
    parameters: [
      { name: 'date1', type: 'string', required: true },
      { name: 'date2', type: 'string', required: true }
    ],
    execute: (date1: string, date2: string) => new Date(date1) < new Date(date2),
    javaTemplate: 'dateBefore({0}, {1})'
  },
  {
    name: 'dateAfter',
    category: 'date',
    description: 'Check if first date is after second date',
    parameters: [
      { name: 'date1', type: 'string', required: true },
      { name: 'date2', type: 'string', required: true }
    ],
    execute: (date1: string, date2: string) => new Date(date1) > new Date(date2),
    javaTemplate: 'dateAfter({0}, {1})'
  },
  {
    name: 'compareDates',
    category: 'date',
    description: 'Compare two dates (-1, 0, 1)',
    parameters: [
      { name: 'date1', type: 'string', required: true },
      { name: 'date2', type: 'string', required: true }
    ],
    execute: (date1: string, date2: string) => {
      const d1 = new Date(date1);
      const d2 = new Date(date2);
      return d1 < d2 ? -1 : d1 > d2 ? 1 : 0;
    },
    javaTemplate: 'compareDates({0}, {1})'
  }
];

// Node Functions
const nodeFunctions: TransformationFunction[] = [
  {
    name: 'createIf',
    category: 'node',
    description: 'Create conditional node structure',
    parameters: [
      { name: 'condition', type: 'boolean', required: true },
      { name: 'value', type: 'string', required: true }
    ],
    execute: (condition: boolean, value: string) => condition ? value : null,
    javaTemplate: 'createIf({0}, {1})'
  },
  {
    name: 'removeContexts',
    category: 'node',
    description: 'Remove context from node structure',
    parameters: [
      { name: 'node', type: 'string', required: true }
    ],
    execute: (node: string) => node, // Simplified for preview
    javaTemplate: 'removeContexts({0})'
  },
  {
    name: 'replaceValue',
    category: 'node',
    description: 'Replace value in node structure',
    parameters: [
      { name: 'node', type: 'string', required: true },
      { name: 'oldValue', type: 'string', required: true },
      { name: 'newValue', type: 'string', required: true }
    ],
    execute: (node: string, oldValue: string, newValue: string) => node.replace(oldValue, newValue),
    javaTemplate: 'replaceValue({0}, {1}, {2})'
  },
  {
    name: 'exists',
    category: 'node',
    description: 'Check if node exists',
    parameters: [
      { name: 'node', type: 'string', required: true }
    ],
    execute: (node: string) => node != null && node !== '',
    javaTemplate: 'exists({0})'
  },
  {
    name: 'getHeader',
    category: 'node',
    description: 'Get header value from message',
    parameters: [
      { name: 'headerName', type: 'string', required: true }
    ],
    execute: (headerName: string) => `header_${headerName}`, // Simplified for preview
    javaTemplate: 'getHeader({0})'
  },
  {
    name: 'getProperty',
    category: 'node',
    description: 'Get property value',
    parameters: [
      { name: 'propertyName', type: 'string', required: true }
    ],
    execute: (propertyName: string) => `property_${propertyName}`, // Simplified for preview
    javaTemplate: 'getProperty({0})'
  },
  {
    name: 'splitByValue',
    category: 'node',
    description: 'Split node by delimiter',
    parameters: [
      { name: 'value', type: 'string', required: true },
      { name: 'delimiter', type: 'string', required: true }
    ],
    execute: (value: string, delimiter: string) => value.split(delimiter),
    javaTemplate: 'splitByValue({0}, {1})'
  },
  {
    name: 'collapseContexts',
    category: 'node',
    description: 'Collapse multiple contexts into one',
    parameters: [
      { name: 'contexts', type: 'array', required: true }
    ],
    execute: (contexts: any[]) => contexts.join(''),
    javaTemplate: 'collapseContexts({0})'
  },
  {
    name: 'useOneAsMany',
    category: 'node',
    description: 'Replicate a field that occurs once to pair with fields that occur multiple times',
    parameters: [
      { name: 'singleField', type: 'string', required: true, description: 'Field that occurs 1..1 (once)' },
      { name: 'countField', type: 'string', required: true, description: 'Field that determines replication count' },
      { name: 'multipleField', type: 'string', required: true, description: 'Field that occurs multiple times and provides context' }
    ],
    execute: (singleField: string, countField: string, multipleField: string) => {
      // This is a simplified implementation for preview
      // In actual SAP PI/XI, this function replicates the singleField value
      // based on the occurrence count of multipleField within countField context
      return [singleField]; // Simplified return
    },
    javaTemplate: 'useOneAsMany({0}, {1}, {2})'
  },
  {
    name: 'sort',
    category: 'node',
    description: 'Sort array of values',
    parameters: [
      { name: 'values', type: 'array', required: true }
    ],
    execute: (values: any[]) => [...values].sort(),
    javaTemplate: 'sort({0})'
  },
  {
    name: 'sortByKey',
    category: 'node',
    description: 'Sort array by specific key',
    parameters: [
      { name: 'values', type: 'array', required: true },
      { name: 'key', type: 'string', required: true }
    ],
    execute: (values: any[], key: string) => [...values].sort((a, b) => a[key] - b[key]),
    javaTemplate: 'sortByKey({0}, {1})'
  },
  {
    name: 'mapWithDefault',
    category: 'node',
    description: 'Map value with default fallback',
    parameters: [
      { name: 'value', type: 'string', required: true },
      { name: 'defaultValue', type: 'string', required: true }
    ],
    execute: (value: string, defaultValue: string) => value || defaultValue,
    javaTemplate: 'mapWithDefault({0}, {1})'
  },
  {
    name: 'formatByExample',
    category: 'node',
    description: 'Format value using example pattern',
    parameters: [
      { name: 'value', type: 'string', required: true },
      { name: 'example', type: 'string', required: true }
    ],
    execute: (value: string, example: string) => value, // Simplified for preview
    javaTemplate: 'formatByExample({0}, {1})'
  }
];

// Constants Functions
const constantFunctions: TransformationFunction[] = [
  {
    name: 'constant',
    category: 'constants',
    description: 'Set a fixed value',
    parameters: [
      { name: 'value', type: 'string', required: true, description: 'The constant value to return' }
    ],
    execute: (value: any) => value,
    javaTemplate: 'constant("{0}")'
  }
];

// All functions registry
export const allTransformationFunctions = [
  ...mathFunctions,
  ...textFunctions,
  ...booleanFunctions,
  ...conversionFunctions,
  ...dateFunctions,
  ...nodeFunctions,
  ...constantFunctions
];

// Functions grouped by category
export const functionsByCategory = {
  math: mathFunctions,
  text: textFunctions,
  boolean: booleanFunctions,
  conversion: conversionFunctions,
  date: dateFunctions,
  node: nodeFunctions,
  constants: constantFunctions
};

// Helper to get function by name
export const getTransformationFunction = (name: string): TransformationFunction | undefined => {
  return allTransformationFunctions.find(fn => fn.name === name);
};

// Helper to generate Java code from function calls
export const generateJavaFunctionCall = (functionName: string, parameters: any[]): string => {
  const func = getTransformationFunction(functionName);
  if (!func) return '';
  
  let template = func.javaTemplate;
  parameters.forEach((param, index) => {
    const placeholder = `{${index}}`;
    const value = typeof param === 'string' ? `"${param}"` : param;
    template = template.replace(placeholder, value);
  });
  
  return template;
};

// Service class for transformation functions
export class TransformationFunctionService {
  static getAllFunctions(): TransformationFunction[] {
    return allTransformationFunctions;
  }
  
  static getFunctionsByCategory(category: string): TransformationFunction[] {
    return functionsByCategory[category as keyof typeof functionsByCategory] || [];
  }
  
  static executeFunction(functionName: string, parameters: any[]): any {
    const func = getTransformationFunction(functionName);
    if (!func) throw new Error(`Function ${functionName} not found`);
    
    return func.execute(...parameters);
  }
  
  static validateParameters(functionName: string, parameters: any[]): { valid: boolean; errors: string[] } {
    const func = getTransformationFunction(functionName);
    if (!func) return { valid: false, errors: [`Function ${functionName} not found`] };
    
    const errors: string[] = [];
    const requiredParams = func.parameters.filter(p => p.required);
    
    if (parameters.length < requiredParams.length) {
      errors.push(`Function ${functionName} requires ${requiredParams.length} parameters, got ${parameters.length}`);
    }
    
    return { valid: errors.length === 0, errors };
  }
}