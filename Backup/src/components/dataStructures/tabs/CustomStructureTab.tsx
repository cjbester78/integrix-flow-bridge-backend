import React, { useState } from 'react';
import { Button } from '@/components/ui/button';
import { Label } from '@/components/ui/label';
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from '@/components/ui/select';
import { FieldBuilder } from '@/components/FieldBuilder';
import { Field } from '@/types/dataStructures';
import { Plus, Database, X } from 'lucide-react';
import { useCustomFields } from '@/hooks/useCustomFields';
import { SchemaPreview } from '../SchemaPreview';
import { useToast } from '@/hooks/use-toast';

interface CustomStructureTabProps {
  customFields: Field[];
  setCustomFields: (fields: Field[]) => void;
  selectedStructureType: string;
  setSelectedStructureType: (type: string) => void;
  onResetAllFields: () => void;
}

export const CustomStructureTab: React.FC<CustomStructureTabProps> = ({
  customFields,
  setCustomFields,
  selectedStructureType,
  setSelectedStructureType,
  onResetAllFields
}) => {
  const [customStructureType, setCustomStructureType] = useState<string>('');
  const { toast } = useToast();
  const {
    addCustomField,
    updateFieldAtPath,
    removeFieldAtPath,
    addChildAtPath
  } = useCustomFields(customFields, setCustomFields);

  const handleClearCustomFields = () => {
    onResetAllFields();
    toast({
      title: "All Fields Cleared",
      description: "All form fields have been reset",
    });
  };

  return (
    <div className="space-y-4">
      <div className="space-y-2">
        <Label htmlFor="customStructureType">Structure Type</Label>
        <Select value={customStructureType} onValueChange={setCustomStructureType}>
          <SelectTrigger>
            <SelectValue placeholder="Select structure type to build" />
          </SelectTrigger>
          <SelectContent>
            <SelectItem value="json">JSON Schema</SelectItem>
            <SelectItem value="xsd">XSD Schema</SelectItem>
            <SelectItem value="xml">XML Schema</SelectItem>
            <SelectItem value="wsdl">WSDL Schema</SelectItem>
          </SelectContent>
        </Select>
      </div>

      {!customStructureType ? (
        <div className="text-center py-8 space-y-4">
          <Database className="h-12 w-12 mx-auto text-muted-foreground opacity-50" />
          <div>
            <h3 className="text-lg font-semibold mb-2">Custom Structure Builder</h3>
            <p className="text-sm text-muted-foreground">
              Select a structure type above to start building your custom data structure
            </p>
          </div>
        </div>
      ) : customFields.length === 0 ? (
        <div className="text-center py-8 space-y-4">
          <Database className="h-12 w-12 mx-auto text-muted-foreground" />
          <div>
            <h3 className="text-lg font-semibold mb-2">Build {customStructureType.toUpperCase()} Structure</h3>
            <p className="text-sm text-muted-foreground mb-4">
              Add fields to create your {customStructureType.toUpperCase()} data structure
            </p>
            <Button 
              onClick={() => addCustomField()}
              className="w-full max-w-xs bg-gradient-primary hover:opacity-90"
            >
              <Plus className="h-4 w-4 mr-2" />
              Add Field
            </Button>
          </div>
        </div>
      ) : (
        <>
          <div className="flex items-center justify-between">
            <Label>Structure Definition ({customStructureType.toUpperCase()})</Label>
            <div className="flex gap-2">
              <Button onClick={() => addCustomField()} size="sm" variant="outline">
                <Plus className="h-4 w-4 mr-2" />
                Add Field
              </Button>
              <Button 
                variant="outline" 
                size="sm" 
                onClick={handleClearCustomFields}
                className="text-destructive hover:text-destructive"
              >
                <X className="h-4 w-4 mr-1" />
                Clear All
              </Button>
            </div>
          </div>
          
          {customFields.map((field, index) => (
            <FieldBuilder
              key={index}
              field={field}
              index={index}
              onUpdate={updateFieldAtPath}
              onRemove={removeFieldAtPath}
              onAddChild={addChildAtPath}
              depth={0}
              path={[index]}
            />
          ))}
          
          <SchemaPreview fields={customFields} schemaType={customStructureType} />
        </>
      )}
    </div>
  );
};