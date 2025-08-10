import React from 'react';
import { Label } from '@/components/ui/label';
import { Field } from '@/types/dataStructures';

interface FieldChildrenProps {
  field: Field;
  canHaveChildren: boolean;
  isExpanded: boolean;
  hasChildren: boolean;
  depth: number;
  path: number[];
  onUpdateChild: (childPath: number[], updates: Partial<Field>) => void;
  onRemoveChild: (childPath: number[]) => void;
  onAddGrandChild: (childPath: number[]) => void;
  FieldBuilderComponent: React.ComponentType<any>;
}

export const FieldChildren: React.FC<FieldChildrenProps> = ({
  field,
  canHaveChildren,
  isExpanded,
  hasChildren,
  depth,
  path,
  onUpdateChild,
  onRemoveChild,
  onAddGrandChild,
  FieldBuilderComponent
}) => {
  if (!canHaveChildren || !isExpanded || !hasChildren) {
    return null;
  }

  return (
    <div className="space-y-3 pl-4 border-l border-border/50 animate-accordion-down">
      <Label className="text-xs text-muted-foreground">Child Fields:</Label>
      {field.children!.map((childField, childIndex) => (
        <FieldBuilderComponent
          key={childIndex}
          field={childField}
          index={childIndex}
          onUpdate={onUpdateChild}
          onRemove={onRemoveChild}
          onAddChild={onAddGrandChild}
          depth={depth + 1}
          path={[...path, childIndex]}
        />
      ))}
    </div>
  );
};