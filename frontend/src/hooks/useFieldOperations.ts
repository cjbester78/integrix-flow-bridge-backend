import { useCallback } from 'react';
import { Field } from '@/types/dataStructures';

export const useFieldOperations = (
  field: Field,
  path: number[],
  onUpdate: (path: number[], updates: Partial<Field>) => void,
  onRemove: (path: number[]) => void,
  onAddChild: (path: number[]) => void
) => {
  const handleUpdate = useCallback((updates: Partial<Field>) => {
    // Auto-set type to array if maxOccurs > 1
    if (updates.maxOccurs !== undefined) {
      const maxOccurs = updates.maxOccurs;
      if ((typeof maxOccurs === 'number' && maxOccurs > 1) || maxOccurs === 'unbounded') {
        updates.type = 'array';
      } else if (maxOccurs === 1 && field.type === 'array') {
        // Reset to string if changing from array back to single occurrence
        updates.type = 'string';
      }
    }
    
    onUpdate(path, updates);
  }, [path, onUpdate, field.type]);

  const handleAddChild = useCallback(() => {
    // Mark as complex type when adding children
    if (!field.isComplexType && field.type !== 'object' && field.type !== 'array') {
      onUpdate(path, { isComplexType: true });
    }
    onAddChild(path);
  }, [field.isComplexType, field.type, path, onUpdate, onAddChild]);

  const handleRemoveChild = useCallback((childPath: number[]) => {
    onRemove(childPath);
  }, [onRemove]);

  const handleUpdateChild = useCallback((childPath: number[], updates: Partial<Field>) => {
    onUpdate(childPath, updates);
  }, [onUpdate]);

  const handleAddGrandChild = useCallback((childPath: number[]) => {
    onAddChild(childPath);
  }, [onAddChild]);

  return {
    handleUpdate,
    handleAddChild,
    handleRemoveChild,
    handleUpdateChild,
    handleAddGrandChild
  };
};