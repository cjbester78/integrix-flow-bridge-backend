import { useCallback } from 'react';
import { Field } from '@/types/dataStructures';

export const useCustomFields = (customFields: Field[], setCustomFields: (fields: Field[]) => void) => {
  const addCustomField = useCallback((fieldIndex?: number, parentIndex?: number) => {
    const newField: Field = {
      name: '',
      type: 'string',
      required: false,
      description: '',
      isComplexType: false,
      minOccurs: 0,
      maxOccurs: 1,
      children: []
    };

    if (parentIndex !== undefined && fieldIndex !== undefined) {
      // Adding child to a nested field
      const updated = [...customFields];
      if (!updated[parentIndex].children![fieldIndex].children) {
        updated[parentIndex].children![fieldIndex].children = [];
      }
      updated[parentIndex].children![fieldIndex].children!.push(newField);
      setCustomFields(updated);
    } else if (fieldIndex !== undefined) {
      // Adding child to a root level field
      const updated = [...customFields];
      if (!updated[fieldIndex].children) {
        updated[fieldIndex].children = [];
      }
      updated[fieldIndex].children!.push(newField);
      setCustomFields(updated);
    } else {
      // Adding new root level field
      setCustomFields([...customFields, newField]);
    }
  }, [customFields, setCustomFields]);

  const updateFieldAtPath = useCallback((path: number[], fieldUpdates: Partial<Field>) => {
    console.log('updateFieldAtPath called:', { path, field: fieldUpdates });
    
    const updated = [...customFields];
    let current = updated;
    
    // Navigate to the parent of the field we want to update
    for (let i = 0; i < path.length - 1; i++) {
      const index = path[i];
      if (!current[index].children) {
        current[index].children = [];
      }
      current = current[index].children!;
    }
    
    // Update the final field
    const finalIndex = path[path.length - 1];
    if (current[finalIndex]) {
      // Auto-set type to array if maxOccurs > 1
      if (fieldUpdates.maxOccurs !== undefined) {
        const maxOccurs = fieldUpdates.maxOccurs;
        if ((typeof maxOccurs === 'number' && maxOccurs > 1) || maxOccurs === 'unbounded') {
          fieldUpdates.type = 'array';
        } else if (maxOccurs === 1 && current[finalIndex].type === 'array') {
          // Reset to string if changing from array back to single occurrence
          fieldUpdates.type = 'string';
        }
      }
      
      current[finalIndex] = { ...current[finalIndex], ...fieldUpdates };
      console.log('Updated field at path:', path, current[finalIndex]);
    }
    
    setCustomFields(updated);
  }, [customFields, setCustomFields]);

  const removeFieldAtPath = useCallback((path: number[]) => {
    const updated = [...customFields];
    
    if (path.length === 1) {
      // Remove root level field
      setCustomFields(updated.filter((_, i) => i !== path[0]));
    } else {
      // Navigate to parent and remove child
      let current = updated;
      for (let i = 0; i < path.length - 2; i++) {
        const index = path[i];
        if (!current[index].children) return;
        current = current[index].children!;
      }
      
      const parentIndex = path[path.length - 2];
      const childIndex = path[path.length - 1];
      
      if (current[parentIndex] && current[parentIndex].children) {
        current[parentIndex].children = current[parentIndex].children!.filter((_, i) => i !== childIndex);
      }
      
      setCustomFields(updated);
    }
  }, [customFields, setCustomFields]);

  const addChildAtPath = useCallback((path: number[]) => {
    const newField: Field = {
      name: '',
      type: 'string',
      required: false,
      description: '',
      isComplexType: false,
      minOccurs: 0,
      maxOccurs: 1,
      children: []
    };

    const updated = [...customFields];
    let current = updated;
    
    // Navigate to the target field
    for (let i = 0; i < path.length; i++) {
      const index = path[i];
      if (!current[index].children) {
        current[index].children = [];
      }
      if (i === path.length - 1) {
        // Add child to this field
        current[index].children!.push(newField);
      } else {
        current = current[index].children!;
      }
    }
    
    setCustomFields(updated);
  }, [customFields, setCustomFields]);

  return {
    addCustomField,
    updateFieldAtPath,
    removeFieldAtPath,
    addChildAtPath
  };
};