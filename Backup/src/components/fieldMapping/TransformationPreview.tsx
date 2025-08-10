import React, { useState, useEffect } from 'react';
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '@/components/ui/card';
import { Badge } from '@/components/ui/badge';
import { Input } from '@/components/ui/input';
import { Label } from '@/components/ui/label';
import { Button } from '@/components/ui/button';
import { Play, AlertCircle } from 'lucide-react';
import { 
  TransformationFunctionService, 
  getTransformationFunction,
  allTransformationFunctions 
} from '@/services/transformationFunctions';

interface TransformationPreviewProps {
  selectedFunctionName?: string;
}

export const TransformationPreview: React.FC<TransformationPreviewProps> = ({ 
  selectedFunctionName = 'add' 
}) => {
  const [functionName, setFunctionName] = useState(selectedFunctionName);
  const [parameters, setParameters] = useState<any[]>([]);
  const [result, setResult] = useState<any>(null);
  const [error, setError] = useState<string>('');

  const currentFunction = getTransformationFunction(functionName);

  useEffect(() => {
    if (currentFunction) {
      // Set default parameter values
      const defaultParams = currentFunction.parameters.map(param => {
        switch (param.type) {
          case 'number': return 0;
          case 'boolean': return false;
          case 'array': return [];
          default: return '';
        }
      });
      setParameters(defaultParams);
      setResult(null);
      setError('');
    }
  }, [functionName, currentFunction]);

  const handleParameterChange = (index: number, value: any) => {
    const newParams = [...parameters];
    
    // Convert value based on parameter type
    if (currentFunction?.parameters[index]) {
      const paramType = currentFunction.parameters[index].type;
      switch (paramType) {
        case 'number':
          newParams[index] = parseFloat(value) || 0;
          break;
        case 'boolean':
          newParams[index] = value === 'true';
          break;
        case 'array':
          newParams[index] = value.split(',').map((v: string) => v.trim());
          break;
        default:
          newParams[index] = value;
      }
    } else {
      newParams[index] = value;
    }
    
    setParameters(newParams);
  };

  const executeFunction = () => {
    if (!currentFunction) return;

    try {
      setError('');
      const validation = TransformationFunctionService.validateParameters(functionName, parameters);
      
      if (!validation.valid) {
        setError(validation.errors.join(', '));
        return;
      }

      const output = TransformationFunctionService.executeFunction(functionName, parameters);
      setResult(output);
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Unknown error occurred');
    }
  };

  if (!currentFunction) {
    return (
      <Card>
        <CardContent className="p-4">
          <p className="text-muted-foreground">Function not found</p>
        </CardContent>
      </Card>
    );
  }

  return (
    <Card className="w-full max-w-md">
      <CardHeader className="pb-3">
        <div className="flex items-center justify-between">
          <CardTitle className="text-lg">{currentFunction.name}</CardTitle>
          <Badge variant="outline">{currentFunction.category}</Badge>
        </div>
        <CardDescription className="text-sm">
          {currentFunction.description}
        </CardDescription>
      </CardHeader>
      
      <CardContent className="space-y-4">
        {/* Function Selector */}
        <div className="space-y-2">
          <Label className="text-sm font-medium">Function:</Label>
          <select 
            value={functionName}
            onChange={(e) => setFunctionName(e.target.value)}
            className="w-full p-2 border rounded text-sm"
          >
            {allTransformationFunctions.map(func => (
              <option key={func.name} value={func.name}>
                {func.name} ({func.category})
              </option>
            ))}
          </select>
        </div>

        {/* Parameters */}
        {currentFunction.parameters.length > 0 && (
          <div className="space-y-3">
            <Label className="text-sm font-medium">Parameters:</Label>
            {currentFunction.parameters.map((param, index) => (
              <div key={param.name} className="space-y-1">
                <Label className="text-xs">
                  {param.name} ({param.type})
                  {param.required && <span className="text-destructive">*</span>}
                </Label>
                {param.type === 'boolean' ? (
                  <select
                    value={parameters[index]?.toString() || 'false'}
                    onChange={(e) => handleParameterChange(index, e.target.value)}
                    className="w-full p-1 border rounded text-sm"
                  >
                    <option value="false">false</option>
                    <option value="true">true</option>
                  </select>
                ) : (
                  <Input
                    type={param.type === 'number' ? 'number' : 'text'}
                    placeholder={param.description || `Enter ${param.name}`}
                    value={param.type === 'array' 
                      ? (parameters[index] || []).join(', ')
                      : parameters[index] || ''
                    }
                    onChange={(e) => handleParameterChange(index, e.target.value)}
                    className="h-8 text-sm"
                  />
                )}
                {param.description && (
                  <p className="text-xs text-muted-foreground">{param.description}</p>
                )}
              </div>
            ))}
          </div>
        )}

        {/* Execute Button */}
        <Button 
          onClick={executeFunction} 
          className="w-full" 
          size="sm"
          disabled={currentFunction.parameters.some(p => p.required && !parameters[currentFunction.parameters.indexOf(p)])}
        >
          <Play className="h-4 w-4 mr-2" />
          Execute Function
        </Button>

        {/* Result Display */}
        {(result !== null || error) && (
          <div className="space-y-2">
            <Label className="text-sm font-medium">Result:</Label>
            {error ? (
              <div className="p-2 bg-destructive/10 border border-destructive/20 rounded flex items-start gap-2">
                <AlertCircle className="h-4 w-4 text-destructive mt-0.5 flex-shrink-0" />
                <p className="text-sm text-destructive">{error}</p>
              </div>
            ) : (
              <div className="p-2 bg-muted rounded">
                <pre className="text-sm font-mono text-primary">
                  {JSON.stringify(result, null, 2)}
                </pre>
              </div>
            )}
          </div>
        )}

        {/* Java Code Preview */}
        <div className="space-y-2">
          <Label className="text-sm font-medium">Java Code:</Label>
          <div className="p-2 bg-muted rounded">
            <code className="text-xs font-mono">
              {currentFunction.javaTemplate.replace(/{(\d+)}/g, (match, index) => {
                const param = parameters[parseInt(index)];
                return typeof param === 'string' ? `"${param}"` : param?.toString() || `{${index}}`;
              })}
            </code>
          </div>
        </div>
      </CardContent>
    </Card>
  );
};