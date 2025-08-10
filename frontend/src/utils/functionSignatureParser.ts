export interface FunctionParameter {
  name: string;
  type: 'string' | 'number' | 'boolean' | 'array' | 'any';
  required: boolean;
  description?: string;
}

export interface ParsedSignature {
  functionName: string;
  parameters: FunctionParameter[];
}

/**
 * Parse a function signature like "add(a, b)" or "substring(text, start, end)"
 * into a structured format with parameter information
 */
export function parseFunctionSignature(signature: string): ParsedSignature | null {
  try {
    // Match function name and parameters
    const match = signature.match(/^(\w+)\s*\((.*)\)$/);
    if (!match) {
      return null;
    }

    const functionName = match[1];
    const paramsString = match[2].trim();

    // If no parameters
    if (!paramsString) {
      return {
        functionName,
        parameters: []
      };
    }

    // Split parameters by comma, handling nested parentheses
    const parameters: FunctionParameter[] = [];
    let current = '';
    let depth = 0;
    
    for (let i = 0; i < paramsString.length; i++) {
      const char = paramsString[i];
      
      if (char === '(') depth++;
      else if (char === ')') depth--;
      
      if (char === ',' && depth === 0) {
        const param = parseParameter(current.trim());
        if (param) parameters.push(param);
        current = '';
      } else {
        current += char;
      }
    }
    
    // Don't forget the last parameter
    if (current.trim()) {
      const param = parseParameter(current.trim());
      if (param) parameters.push(param);
    }

    return {
      functionName,
      parameters
    };
  } catch (error) {
    console.error('Error parsing function signature:', error);
    return null;
  }
}

/**
 * Parse a single parameter, potentially with type annotation
 * Examples: "value", "text: string", "amount: number"
 */
function parseParameter(param: string): FunctionParameter | null {
  if (!param) return null;

  // Check for type annotation (e.g., "value: number")
  const typeMatch = param.match(/^(\w+)\s*:\s*(\w+)$/);
  
  if (typeMatch) {
    const [, name, type] = typeMatch;
    return {
      name,
      type: mapToParameterType(type),
      required: true // Default to required
    };
  }

  // No type annotation, just parameter name
  return {
    name: param,
    type: 'any', // Default type
    required: true
  };
}

/**
 * Map type strings to our parameter types
 */
function mapToParameterType(type: string): FunctionParameter['type'] {
  const typeMap: Record<string, FunctionParameter['type']> = {
    'string': 'string',
    'number': 'number',
    'boolean': 'boolean',
    'array': 'array',
    'bool': 'boolean',
    'int': 'number',
    'float': 'number',
    'double': 'number',
    'text': 'string',
    'str': 'string'
  };

  return typeMap[type.toLowerCase()] || 'any';
}

/**
 * Generate a function signature string from parameters
 */
export function generateSignature(functionName: string, parameters: FunctionParameter[]): string {
  const paramNames = parameters.map(p => p.name).join(', ');
  return `${functionName}(${paramNames})`;
}

/**
 * Infer parameter types from function body (basic heuristics)
 */
export function inferParameterTypes(
  functionBody: string, 
  parameters: FunctionParameter[]
): FunctionParameter[] {
  return parameters.map(param => {
    const paramName = param.name;
    
    // Look for numeric operations
    if (functionBody.includes(`${paramName} +`) || 
        functionBody.includes(`${paramName} -`) ||
        functionBody.includes(`${paramName} *`) ||
        functionBody.includes(`${paramName} /`) ||
        functionBody.includes(`Math.`) && functionBody.includes(paramName)) {
      return { ...param, type: 'number' };
    }
    
    // Look for string operations
    if (functionBody.includes(`${paramName}.length`) ||
        functionBody.includes(`${paramName}.substring`) ||
        functionBody.includes(`${paramName}.indexOf`) ||
        functionBody.includes(`${paramName}.toUpperCase`) ||
        functionBody.includes(`${paramName}.toLowerCase`)) {
      return { ...param, type: 'string' };
    }
    
    // Look for boolean operations
    if (functionBody.includes(`!${paramName}`) ||
        functionBody.includes(`${paramName} &&`) ||
        functionBody.includes(`${paramName} ||`) ||
        functionBody.includes(`${paramName} ?`)) {
      return { ...param, type: 'boolean' };
    }
    
    // Look for array operations
    if (functionBody.includes(`${paramName}.length`) ||
        functionBody.includes(`${paramName}[`) ||
        functionBody.includes(`${paramName}.map`) ||
        functionBody.includes(`${paramName}.filter`) ||
        functionBody.includes(`${paramName}.reduce`)) {
      return { ...param, type: 'array' };
    }
    
    return param;
  });
}