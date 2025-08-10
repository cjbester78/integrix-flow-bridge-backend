// @ts-nocheck
import { apiClient } from '@/lib/api-client';

export interface FunctionParameter {
  name: string;
  type: string;
  required: boolean;
  description?: string;
}

export interface BuiltInFunction {
  name: string;
  category: string;
  description: string;
  signature: string;
  parameters?: FunctionParameter[];
}

export interface DevelopmentFunctionsResponse {
  developmentMode: boolean;
  builtInFunctions: BuiltInFunction[];
  customFunctions: {
    content: any[];
    totalElements: number;
    totalPages: number;
    number: number;
  };
}

export interface TransformationFunctionWithParams {
  name: string;
  category: string;
  description: string;
  parameters: FunctionParameter[];
  execute?: Function;
  javaTemplate?: string;
  isBuiltIn?: boolean;
}

class DevelopmentFunctionsService {
  private functionsCache: TransformationFunctionWithParams[] | null = null;
  private cacheExpiry: number = 0;
  private cacheDuration = 5 * 60 * 1000; // 5 minutes

  async getAllFunctions(): Promise<TransformationFunctionWithParams[]> {
    // Check cache
    if (this.functionsCache && Date.now() < this.cacheExpiry) {
      return this.functionsCache;
    }

    try {
      const response = await apiClient.get<DevelopmentFunctionsResponse>('/development/functions');
      
      // Convert built-in functions to our format
      const transformedFunctions: TransformationFunctionWithParams[] = response.builtInFunctions.map(func => {
        // Parse parameters from the database or use empty array
        let parameters: FunctionParameter[] = [];
        
        if (func.parameters) {
          parameters = func.parameters;
        } else {
          // If no parameters in DB, try to parse from signature
          const paramMatch = func.signature.match(/\((.*)\)/);
          if (paramMatch && paramMatch[1]) {
            const paramNames = paramMatch[1].split(',').map(p => p.trim());
            parameters = paramNames.map(name => ({
              name,
              type: 'any',
              required: true
            }));
          }
        }

        return {
          name: func.name,
          category: func.category,
          description: func.description,
          parameters,
          isBuiltIn: true,
          // Map function names to their JavaScript implementations
          execute: this.getExecuteFunction(func.name),
          javaTemplate: this.getJavaTemplate(func.name, parameters)
        };
      });

      // Cache the results
      this.functionsCache = transformedFunctions;
      this.cacheExpiry = Date.now() + this.cacheDuration;

      return transformedFunctions;
    } catch (error) {
      console.error('Failed to fetch functions from API:', error);
      // Return empty array on error
      return [];
    }
  }

  async getBuiltInFunctionByName(name: string): Promise<BuiltInFunction | null> {
    try {
      const response = await apiClient.get<any>(`/development/functions/built-in/${name}`);
      
      // Parse parameters if they exist
      if (response.parameters && typeof response.parameters === 'string') {
        try {
          response.parameters = JSON.parse(response.parameters);
        } catch (e) {
          console.error('Failed to parse parameters:', e);
          response.parameters = [];
        }
      }
      
      return response;
    } catch (error) {
      console.error('Failed to fetch function by name:', error);
      return null;
    }
  }

  clearCache() {
    this.functionsCache = null;
    this.cacheExpiry = 0;
  }

  private getExecuteFunction(name: string): Function | undefined {
    // Map of function names to their JavaScript implementations
    const functionImplementations: Record<string, Function> = {
      // Math functions
      add: (a: number, b: number) => a + b,
      subtract: (a: number, b: number) => a - b,
      multiply: (a: number, b: number) => a * b,
      divide: (a: number, b: number) => a / b,
      equals: (a: number, b: number) => a === b,
      absolute: (value: number) => Math.abs(value),
      sqrt: (value: number) => Math.sqrt(value),
      square: (value: number) => value * value,
      sign: (value: number) => Math.sign(value),
      neg: (value: number) => -value,
      inv: (value: number) => 1 / value,
      power: (base: number, exponent: number) => Math.pow(base, exponent),
      lesser: (a: number, b: number) => a < b,
      greater: (a: number, b: number) => a > b,
      max: (a: number, b: number) => Math.max(a, b),
      min: (a: number, b: number) => Math.min(a, b),
      ceil: (value: number) => Math.ceil(value),
      floor: (value: number) => Math.floor(value),
      round: (value: number) => Math.round(value),
      counter: (start: number = 0, step: number = 1) => start + step,
      formatNumber: (value: number, totalDigits: number, decimals?: number) => {
        const formatted = value.toFixed(decimals || 0);
        return formatted.padStart(totalDigits, '0');
      },
      sum: (values: number[]) => values.reduce((a, b) => a + b, 0),
      average: (values: number[]) => values.reduce((a, b) => a + b, 0) / values.length,
      count: (values: any[]) => values.length,
      index: (position: number) => position,

      // Text functions
      concat: (string1: string, string2: string, delimiter?: string) => 
        delimiter ? `${string1}${delimiter}${string2}` : `${string1}${string2}`,
      substring: (text: string, start: number, end?: number) => text.substring(start, end),
      indexOf: (text: string, searchValue: string) => text.indexOf(searchValue),
      lastIndexOf: (text: string, searchValue: string) => text.lastIndexOf(searchValue),
      compare: (string1: string, string2: string) => string1.localeCompare(string2),
      replaceString: (text: string, searchValue: string, replaceValue: string) => 
        text.replace(new RegExp(searchValue, 'g'), replaceValue),
      length: (text: string) => text.length,
      endsWith: (text: string, suffix: string) => text.endsWith(suffix),
      startsWith: (text: string, prefix: string) => text.startsWith(prefix),
      toUpperCase: (text: string) => text.toUpperCase(),
      toLowerCase: (text: string) => text.toLowerCase(),
      trim: (text: string) => text.trim(),

      // Boolean functions
      and: (a: boolean, b: boolean) => a && b,
      or: (a: boolean, b: boolean) => a || b,
      not: (value: boolean) => !value,
      notEquals: (a: any, b: any) => a !== b,
      if: (condition: boolean, trueValue: any, falseValue: any) => 
        condition ? trueValue : falseValue,
      ifWithoutElse: (condition: boolean, trueValue: any) => 
        condition ? trueValue : undefined,
      isNil: (value: any) => value === null || value === undefined,

      // Other functions can be added as needed
      constant: (value: any) => value,
      fixValues: (value: any, format: string) => value, // Simplified
    };

    return functionImplementations[name];
  }

  private getJavaTemplate(name: string, parameters: FunctionParameter[]): string {
    // Generate Java template based on parameters
    const paramPlaceholders = parameters.map((_, index) => `{${index}}`).join(', ');
    return `${name}(${paramPlaceholders})`;
  }
}

export const developmentFunctionsService = new DevelopmentFunctionsService();