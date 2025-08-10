import React from 'react';
import { Field } from '@/types/dataStructures';
import { FieldConfiguration } from './dataStructures/FieldConfiguration';
import { FieldAdvancedOptions } from './dataStructures/FieldAdvancedOptions';
import { FieldChildren } from './dataStructures/FieldChildren';
import { useFieldOperations } from '@/hooks/useFieldOperations';

interface FieldBuilderProps {
  field: Field;
  index: number;
  onUpdate: (path: number[], updates: Partial<Field>) => void;
  onRemove: (path: number[]) => void;
  onAddChild: (path: number[]) => void;
  depth: number;
  path: number[];
}

export const FieldBuilder: React.FC<FieldBuilderProps> = ({
  field,
  index,
  onUpdate,
  onRemove,
  onAddChild,
  depth,
  path
}) => {
  const [isExpanded, setIsExpanded] = React.useState(true);
  const hasChildren = field.children && field.children.length > 0;
  const canHaveChildren = field.isComplexType || field.type === 'object' || field.type === 'array';

  const {
    handleUpdate,
    handleAddChild,
    handleRemoveChild,
    handleUpdateChild,
    handleAddGrandChild
  } = useFieldOperations(field, path, onUpdate, onRemove, onAddChild);

  return (
    <div className={`space-y-3 p-4 border rounded-lg animate-scale-in ${depth > 0 ? 'ml-6 border-l-2 border-l-primary/30' : ''}`}>
      <div className="space-y-3">
        <FieldConfiguration
          field={field}
          canHaveChildren={canHaveChildren}
          isExpanded={isExpanded}
          onToggleExpanded={() => setIsExpanded(!isExpanded)}
          onUpdate={handleUpdate}
          onRemove={() => onRemove(path)}
          depth={depth}
        />
        
        <FieldAdvancedOptions
          field={field}
          canHaveChildren={canHaveChildren}
          onUpdate={handleUpdate}
          onAddChild={handleAddChild}
        />
      </div>

      <FieldChildren
        field={field}
        canHaveChildren={canHaveChildren}
        isExpanded={isExpanded}
        hasChildren={hasChildren}
        depth={depth}
        path={path}
        onUpdateChild={handleUpdateChild}
        onRemoveChild={handleRemoveChild}
        onAddGrandChild={handleAddGrandChild}
        FieldBuilderComponent={FieldBuilder}
      />
    </div>
  );
};