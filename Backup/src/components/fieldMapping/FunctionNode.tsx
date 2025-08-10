import React from 'react';
import { Badge } from '@/components/ui/badge';
import { Button } from '@/components/ui/button';
import { Input } from '@/components/ui/input';
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from '@/components/ui/select';
import { X, Calculator, Link, Unlink } from 'lucide-react';
import { TransformationFunction } from '@/services/transformationFunctions';
import { FieldNode } from './types';

interface FunctionNodeProps {
  id: string;
  func: TransformationFunction;
  parameterValues: Record<string, string>;
  sourceConnections: Record<string, string[]>; // paramName -> sourceFieldPaths[]
  position: { x: number; y: number };
  availableFields: FieldNode[]; // Available source fields for selection
  onParameterChange: (paramName: string, value: string) => void;
  onParameterFieldSelect: (paramName: string, fieldPath: string) => void;
  onParameterFieldRemove: (paramName: string, fieldPath: string) => void;
  onRemove: () => void;
  onDragOver: (e: React.DragEvent) => void;
  onDropOnParameter: (paramName: string) => void;
}

export const FunctionNode: React.FC<FunctionNodeProps> = ({
  id,
  func,
  parameterValues,
  sourceConnections,
  position,
  availableFields,
  onParameterChange,
  onParameterFieldSelect,
  onParameterFieldRemove,
  onRemove,
  onDragOver,
  onDropOnParameter
}) => {
  return (
    <div 
      className="absolute bg-card border-2 border-primary/20 rounded-lg shadow-lg min-w-[200px] animate-scale-in z-10"
      style={{ left: position.x, top: position.y }}
    >
      {/* Function Header */}
      <div className="flex items-center justify-between p-3 border-b bg-primary/5 rounded-t-lg">
        <div className="flex items-center gap-2">
          <Calculator className="h-4 w-4 text-primary" />
          <span className="font-semibold text-sm">{func.name}</span>
        </div>
        <Button
          variant="ghost"
          size="sm"
          onClick={onRemove}
          className="h-6 w-6 p-0 text-destructive hover:text-destructive"
        >
          <X className="h-3 w-3" />
        </Button>
      </div>

      {/* Function Description */}
      <div className="p-2 text-xs text-muted-foreground border-b">
        {func.description}
      </div>

      {/* Parameters */}
      <div className="p-3 space-y-3">
        {func.parameters.map((param) => (
          <div key={param.name} className="space-y-1">
            <div className="flex items-center gap-2">
              <span className="text-xs font-medium">{param.name}</span>
              {param.required && <span className="text-destructive text-xs">*</span>}
              <Badge variant="outline" className="text-xs">{param.type}</Badge>
            </div>
            
            {/* Drop zone for parameter */}
            <div
              className="relative"
              onDragOver={onDragOver}
              onDrop={(e) => {
                e.preventDefault();
                onDropOnParameter(param.name);
              }}
            >
              {/* Connected source fields */}
              {sourceConnections[param.name]?.length > 0 ? (
                <div className="space-y-1 mb-2">
                  {sourceConnections[param.name].map((sourcePath, index) => (
                    <div key={index} className="flex items-center gap-2 p-2 bg-primary/10 rounded text-xs border border-primary/20">
                      <Link className="w-3 h-3 text-primary" />
                      <span className="flex-1 text-primary font-medium">{sourcePath.split('.').pop()}</span>
                      <Button
                        variant="ghost"
                        size="sm"
                        onClick={() => onParameterFieldRemove(param.name, sourcePath)}
                        className="h-4 w-4 p-0 text-destructive hover:text-destructive"
                      >
                        <Unlink className="h-2 w-2" />
                      </Button>
                    </div>
                  ))}
                  {/* Additional drop zone to add more fields */}
                  <div className="h-8 border-2 border-dashed border-primary/40 rounded text-xs flex items-center justify-center text-primary/60 hover:border-primary/60 transition-colors bg-primary/5">
                    + Drop another field
                  </div>
                </div>
              ) : (
                <div className="space-y-2">
                  {/* Drop zone */}
                  <div className="h-10 border-2 border-dashed border-primary/40 rounded text-xs flex items-center justify-center text-primary/60 hover:border-primary/60 transition-colors bg-primary/5 hover:bg-primary/10">
                    <div className="text-center">
                      <div className="font-medium">Drop source field here</div>
                      <div className="text-xs opacity-70">or select below</div>
                    </div>
                  </div>
                  
                  {/* Field selector */}
                  <Select onValueChange={(value) => onParameterFieldSelect(param.name, value)}>
                    <SelectTrigger className="h-8 text-xs">
                      <SelectValue placeholder="Select source field" />
                    </SelectTrigger>
                    <SelectContent>
                      {availableFields.map((field) => (
                        <SelectItem key={field.path} value={field.path} className="text-xs">
                          {field.name} ({field.type})
                        </SelectItem>
                      ))}
                    </SelectContent>
                  </Select>
                </div>
              )}
              
              {/* Manual input for constants - only show if no fields connected */}
              {!sourceConnections[param.name]?.length && (
                <Input
                  type={param.type === 'number' ? 'number' : 'text'}
                  placeholder={param.description || `Enter ${param.name}`}
                  value={parameterValues[param.name] || ''}
                  onChange={(e) => onParameterChange(param.name, e.target.value)}
                  className="h-8 text-xs"
                />
              )}
            </div>
          </div>
        ))}
      </div>

      {/* Output connector */}
      <div className="p-3 border-t bg-muted/20">
        <div className="flex items-center gap-2">
          <div className="w-2 h-2 bg-success rounded-full"></div>
          <span className="text-xs font-medium">Output</span>
          <Badge variant="secondary" className="text-xs">String</Badge>
        </div>
      </div>
    </div>
  );
};