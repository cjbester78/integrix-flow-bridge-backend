// @ts-nocheck
import React from 'react';
import { Button } from '@/components/ui/button';
import { Input } from '@/components/ui/input';
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from '@/components/ui/select';
import { Label } from '@/components/ui/label';
import { Plus, X } from 'lucide-react';
import { Card } from '@/components/ui/card';

export interface FunctionParameter {
  name: string;
  type: 'string' | 'number' | 'boolean' | 'array' | 'any';
  required: boolean;
  description?: string;
}

interface ParameterEditorProps {
  parameters: FunctionParameter[];
  onChange: (parameters: FunctionParameter[]) => void;
  readOnly?: boolean;
}

const JAVA_TYPES = [
  { value: 'string', label: 'String' },
  { value: 'number', label: 'Number (int/double/float)' },
  { value: 'boolean', label: 'Boolean' },
  { value: 'array', label: 'Array/List' },
  { value: 'any', label: 'Any (Object)' }
];

export function ParameterEditor({ parameters, onChange, readOnly = false }: ParameterEditorProps) {
  const handleAddParameter = () => {
    const newParam: FunctionParameter = {
      name: `param${parameters.length + 1}`,
      type: 'string',
      required: true
    };
    onChange([...parameters, newParam]);
  };

  const handleRemoveParameter = (index: number) => {
    const newParams = parameters.filter((_, i) => i !== index);
    onChange(newParams);
  };

  const handleParameterChange = (index: number, field: keyof FunctionParameter, value: any) => {
    const newParams = [...parameters];
    newParams[index] = { ...newParams[index], [field]: value };
    onChange(newParams);
  };

  const generateSignature = () => {
    if (parameters.length === 0) return 'functionName()';
    const params = parameters.map(p => `${p.type} ${p.name}`).join(', ');
    return `functionName(${params})`;
  };

  return (
    <div className="space-y-4">
      <div className="flex items-center justify-between">
        <Label>Function Parameters</Label>
        {!readOnly && (
          <Button
            type="button"
            variant="outline"
            size="sm"
            onClick={handleAddParameter}
            className="h-8"
          >
            <Plus className="h-3 w-3 mr-1" />
            Add Parameter
          </Button>
        )}
      </div>

      {parameters.length === 0 ? (
        <Card className="p-4 border-dashed">
          <p className="text-sm text-muted-foreground text-center">
            No parameters defined. Click "Add Parameter" to add one.
          </p>
        </Card>
      ) : (
        <div className="space-y-2">
          {parameters.map((param, index) => (
            <Card key={index} className="p-3">
              <div className="flex items-start gap-3">
                <div className="flex-1 grid grid-cols-3 gap-3">
                  <div className="space-y-1">
                    <Label className="text-xs">Parameter Name</Label>
                    <Input
                      value={param.name}
                      onChange={(e) => handleParameterChange(index, 'name', e.target.value)}
                      placeholder="paramName"
                      disabled={readOnly}
                      className="h-8 text-sm"
                    />
                  </div>
                  
                  <div className="space-y-1">
                    <Label className="text-xs">Java Type</Label>
                    <Select
                      value={param.type}
                      onValueChange={(value) => handleParameterChange(index, 'type', value)}
                      disabled={readOnly}
                    >
                      <SelectTrigger className="h-8 text-sm">
                        <SelectValue />
                      </SelectTrigger>
                      <SelectContent>
                        {JAVA_TYPES.map((type) => (
                          <SelectItem key={type.value} value={type.value} className="text-sm">
                            {type.label}
                          </SelectItem>
                        ))}
                      </SelectContent>
                    </Select>
                  </div>
                  
                  <div className="space-y-1">
                    <Label className="text-xs">Required</Label>
                    <Select
                      value={param.required ? 'true' : 'false'}
                      onValueChange={(value) => handleParameterChange(index, 'required', value === 'true')}
                      disabled={readOnly}
                    >
                      <SelectTrigger className="h-8 text-sm">
                        <SelectValue />
                      </SelectTrigger>
                      <SelectContent>
                        <SelectItem value="true" className="text-sm">Required</SelectItem>
                        <SelectItem value="false" className="text-sm">Optional</SelectItem>
                      </SelectContent>
                    </Select>
                  </div>
                </div>
                
                {!readOnly && (
                  <Button
                    type="button"
                    variant="ghost"
                    size="sm"
                    onClick={() => handleRemoveParameter(index)}
                    className="h-8 w-8 p-0 text-destructive hover:text-destructive"
                  >
                    <X className="h-4 w-4" />
                  </Button>
                )}
              </div>
              
              <div className="mt-2">
                <Input
                  value={param.description || ''}
                  onChange={(e) => handleParameterChange(index, 'description', e.target.value)}
                  placeholder="Parameter description (optional)"
                  disabled={readOnly}
                  className="h-8 text-sm"
                />
              </div>
            </Card>
          ))}
        </div>
      )}

      <div className="mt-4 p-3 bg-muted rounded-md">
        <Label className="text-xs text-muted-foreground mb-1 block">Generated Signature Preview</Label>
        <code className="text-sm font-mono">{generateSignature()}</code>
      </div>
    </div>
  );
}