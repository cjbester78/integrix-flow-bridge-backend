// @ts-nocheck
import React, { useState } from 'react';
import { Button } from '@/components/ui/button';
import { Input } from '@/components/ui/input';
import { Label } from '@/components/ui/label';
import { Alert, AlertDescription } from '@/components/ui/alert';
import { Card } from '@/components/ui/card';
import { Loader2, Play, CheckCircle, XCircle, AlertCircle } from 'lucide-react';
import { FunctionParameter } from './ParameterEditor';

interface FunctionTestPanelProps {
  functionId: string;
  functionName: string;
  parameters: FunctionParameter[];
  onTest: (functionId: string, inputs: Record<string, any>) => Promise<any>;
}

interface TestResult {
  success: boolean;
  output?: any;
  error?: string;
  executionTime?: number;
}

export function FunctionTestPanel({
  functionId,
  functionName,
  parameters,
  onTest
}: FunctionTestPanelProps) {
  const [inputs, setInputs] = useState<Record<string, string>>({});
  const [isLoading, setIsLoading] = useState(false);
  const [result, setResult] = useState<TestResult | null>(null);

  const handleInputChange = (paramName: string, value: string) => {
    setInputs(prev => ({ ...prev, [paramName]: value }));
  };

  const parseInputValue = (value: string, type: string): any => {
    if (!value && type !== 'String') return null;
    
    try {
      switch (type) {
        case 'int':
        case 'Integer':
          return parseInt(value, 10);
        case 'long':
        case 'Long':
          return parseInt(value, 10);
        case 'double':
        case 'Double':
          return parseFloat(value);
        case 'float':
        case 'Float':
          return parseFloat(value);
        case 'boolean':
        case 'Boolean':
          return value.toLowerCase() === 'true';
        case 'List':
          // Try to parse as JSON array
          try {
            const parsed = JSON.parse(value);
            return Array.isArray(parsed) ? parsed : [value];
          } catch {
            // If not valid JSON, split by comma
            return value.split(',').map(v => v.trim());
          }
        case 'Map':
        case 'Object':
          // Try to parse as JSON
          try {
            return JSON.parse(value);
          } catch {
            return value;
          }
        default:
          return value;
      }
    } catch (error) {
      console.error(`Error parsing ${type} value:`, error);
      return value;
    }
  };

  const handleRunTest = async () => {
    setIsLoading(true);
    setResult(null);

    try {
      // Parse inputs based on parameter types
      const parsedInputs: Record<string, any> = {};
      for (const param of parameters) {
        const rawValue = inputs[param.name] || '';
        parsedInputs[param.name] = parseInputValue(rawValue, param.type);
      }

      const startTime = Date.now();
      const testResult = await onTest(functionId, parsedInputs);
      const executionTime = Date.now() - startTime;

      setResult({
        ...testResult,
        executionTime
      });
    } catch (error) {
      setResult({
        success: false,
        error: error instanceof Error ? error.message : 'Test execution failed'
      });
    } finally {
      setIsLoading(false);
    }
  };

  const getInputPlaceholder = (param: FunctionParameter): string => {
    switch (param.type) {
      case 'number':
        return 'e.g., 42 or 3.14';
      case 'boolean':
        return 'true or false';
      case 'array':
        return 'e.g., [1, 2, 3] or 1,2,3';
      case 'any':
        return 'e.g., {"key": "value"} or any value';
      case 'string':
      default:
        return 'Enter value...';
    }
  };

  const formatOutput = (output: any): string => {
    if (output === null || output === undefined) return 'null';
    if (typeof output === 'object') {
      try {
        return JSON.stringify(output, null, 2);
      } catch {
        return String(output);
      }
    }
    return String(output);
  };

  return (
    <div className="space-y-4">
      <div className="space-y-3">
        <Label>Test Inputs</Label>
        {parameters.length === 0 ? (
          <Alert>
            <AlertCircle className="h-4 w-4" />
            <AlertDescription>
              This function has no parameters. Click "Run Test" to execute it.
            </AlertDescription>
          </Alert>
        ) : (
          <div className="space-y-3">
            {parameters.map((param) => (
              <Card key={param.name} className="p-3">
                <div className="space-y-2">
                  <div className="flex items-start justify-between">
                    <div className="space-y-1 flex-1">
                      <Label className="text-sm">
                        {param.name}
                        {param.required && <span className="text-destructive ml-1">*</span>}
                      </Label>
                      <div className="flex items-center gap-2 text-xs text-muted-foreground">
                        <span className="font-mono bg-muted px-1 py-0.5 rounded">
                          {param.type}
                        </span>
                        {param.description && (
                          <span>{param.description}</span>
                        )}
                      </div>
                    </div>
                  </div>
                  <Input
                    value={inputs[param.name] || ''}
                    onChange={(e) => handleInputChange(param.name, e.target.value)}
                    placeholder={getInputPlaceholder(param)}
                    className="font-mono text-sm"
                  />
                </div>
              </Card>
            ))}
          </div>
        )}
      </div>

      <div className="flex items-center gap-2">
        <Button
          onClick={handleRunTest}
          disabled={isLoading}
          className="w-full sm:w-auto"
        >
          {isLoading ? (
            <>
              <Loader2 className="h-4 w-4 mr-2 animate-spin" />
              Running Test...
            </>
          ) : (
            <>
              <Play className="h-4 w-4 mr-2" />
              Run Test
            </>
          )}
        </Button>
      </div>

      {result && (
        <Card className="p-4">
          <div className="space-y-3">
            <div className="flex items-center justify-between">
              <div className="flex items-center gap-2">
                {result.success ? (
                  <>
                    <CheckCircle className="h-5 w-5 text-success" />
                    <span className="font-semibold text-success">Test Passed</span>
                  </>
                ) : (
                  <>
                    <XCircle className="h-5 w-5 text-destructive" />
                    <span className="font-semibold text-destructive">Test Failed</span>
                  </>
                )}
              </div>
              {result.executionTime !== undefined && (
                <span className="text-xs text-muted-foreground">
                  {result.executionTime}ms
                </span>
              )}
            </div>

            {result.success && result.output !== undefined && (
              <div className="space-y-2">
                <Label className="text-sm">Output</Label>
                <pre className="bg-muted p-3 rounded text-sm overflow-x-auto">
                  <code>{formatOutput(result.output)}</code>
                </pre>
              </div>
            )}

            {!result.success && result.error && (
              <Alert variant="destructive">
                <XCircle className="h-4 w-4" />
                <AlertDescription className="font-mono text-sm">
                  {result.error}
                </AlertDescription>
              </Alert>
            )}
          </div>
        </Card>
      )}
    </div>
  );
}