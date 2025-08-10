import React from 'react';
import { Input } from '@/components/ui/input';
import { Label } from '@/components/ui/label';
import { Button } from '@/components/ui/button';
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from '@/components/ui/select';
import { Field } from '@/types/dataStructures';
import { ChevronDown, ChevronRight, Trash2 } from 'lucide-react';
import { fieldTypes } from '@/constants/fieldTypes';

interface FieldConfigurationProps {
  field: Field;
  canHaveChildren: boolean;
  isExpanded: boolean;
  onToggleExpanded: () => void;
  onUpdate: (updates: Partial<Field>) => void;
  onRemove: () => void;
  depth: number;
}

export const FieldConfiguration: React.FC<FieldConfigurationProps> = ({
  field,
  canHaveChildren,
  isExpanded,
  onToggleExpanded,
  onUpdate,
  onRemove,
  depth
}) => {
  return (
    <div className="grid grid-cols-12 gap-2 items-end">
      <div className="col-span-1 flex items-center">
        {canHaveChildren && (
          <Button
            onClick={onToggleExpanded}
            size="sm"
            variant="ghost"
            className="h-6 w-6 p-0 hover-scale"
          >
            {isExpanded ? <ChevronDown className="h-3 w-3" /> : <ChevronRight className="h-3 w-3" />}
          </Button>
        )}
      </div>
      <div className="col-span-3">
        <Label className="text-xs">Field Name</Label>
        <Input
          placeholder="fieldName"
          value={field.name}
          onChange={(e) => onUpdate({ name: e.target.value })}
          className="text-sm animate-fade-in"
        />
      </div>
      <div className="col-span-2">
        <Label className="text-xs">Type</Label>
        <Select value={field.type} onValueChange={(value) => onUpdate({ type: value })}>
          <SelectTrigger className="text-sm">
            <SelectValue />
          </SelectTrigger>
          <SelectContent>
            {fieldTypes.map((type) => (
              <SelectItem key={type} value={type}>{type}</SelectItem>
            ))}
          </SelectContent>
        </Select>
      </div>
      <div className="col-span-3">
        <Label className="text-xs">Description</Label>
        <Input
          placeholder="Field description..."
          value={field.description || ''}
          onChange={(e) => onUpdate({ description: e.target.value })}
          className="text-sm animate-fade-in"
        />
      </div>
      <div className="col-span-2 flex items-center space-x-2">
        <label className="flex items-center space-x-1 text-xs">
          <input
            type="checkbox"
            checked={field.required}
            onChange={(e) => onUpdate({ required: e.target.checked })}
            className="rounded"
          />
          <span>Required</span>
        </label>
      </div>
      <div className="col-span-1">
        <Button
          onClick={onRemove}
          size="sm"
          variant="ghost"
          className="text-destructive hover:text-destructive h-6 w-6 p-0 hover-scale"
        >
          <Trash2 className="h-3 w-3" />
        </Button>
      </div>
    </div>
  );
};