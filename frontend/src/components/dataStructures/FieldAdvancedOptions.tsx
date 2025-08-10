import React from 'react';
import { Input } from '@/components/ui/input';
import { Label } from '@/components/ui/label';
import { Button } from '@/components/ui/button';
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from '@/components/ui/select';
import { Field } from '@/types/dataStructures';
import { Plus } from 'lucide-react';

interface FieldAdvancedOptionsProps {
  field: Field;
  canHaveChildren: boolean;
  onUpdate: (updates: Partial<Field>) => void;
  onAddChild: () => void;
}

export const FieldAdvancedOptions: React.FC<FieldAdvancedOptionsProps> = ({
  field,
  canHaveChildren,
  onUpdate,
  onAddChild
}) => {
  return (
    <div className="grid grid-cols-12 gap-2 items-end animate-fade-in">
      <div className="col-span-1"></div>
      <div className="col-span-2">
        <label className="flex items-center space-x-1 text-xs">
          <input
            type="checkbox"
            checked={field.isComplexType || false}
            onChange={(e) => onUpdate({ isComplexType: e.target.checked })}
            className="rounded"
          />
          <span>Complex Type</span>
        </label>
      </div>
      <div className="col-span-2">
        <Label className="text-xs">Min Occurs</Label>
        <Input
          type="number"
          min="0"
          placeholder="0"
          value={field.minOccurs || 0}
          onChange={(e) => onUpdate({ minOccurs: parseInt(e.target.value) || 0 })}
          className="text-sm"
        />
      </div>
      <div className="col-span-3">
        <Label className="text-xs">Max Occurs</Label>
        <Select 
          value={field.maxOccurs?.toString() || '1'} 
          onValueChange={(value) => onUpdate({ maxOccurs: value === 'unbounded' ? 'unbounded' : parseInt(value) })}
        >
          <SelectTrigger className="text-sm">
            <SelectValue />
          </SelectTrigger>
          <SelectContent>
            <SelectItem value="1">1</SelectItem>
            <SelectItem value="2">2</SelectItem>
            <SelectItem value="5">5</SelectItem>
            <SelectItem value="10">10</SelectItem>
            <SelectItem value="unbounded">Unbounded</SelectItem>
          </SelectContent>
        </Select>
      </div>
      <div className="col-span-3">
        {canHaveChildren && (
          <Button
            onClick={onAddChild}
            size="sm"
            variant="outline"
            className="w-full hover-scale"
          >
            <Plus className="h-3 w-3 mr-1" />
            Add Child
          </Button>
        )}
      </div>
    </div>
  );
};