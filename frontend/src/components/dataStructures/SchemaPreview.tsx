import React from 'react';
import { Label } from '@/components/ui/label';
import { Field } from '@/types/dataStructures';
import { generateSchemaPreview } from '@/utils/schemaGenerators';

interface SchemaPreviewProps {
  fields: Field[];
  schemaType: string;
}

export const SchemaPreview: React.FC<SchemaPreviewProps> = ({
  fields,
  schemaType
}) => {
  if (fields.length === 0) return null;

  return (
    <div className="mt-6 p-4 bg-muted/50 rounded-lg">
      <Label className="text-sm font-semibold mb-2 block">
        Live {schemaType.toUpperCase()} Preview:
      </Label>
      <pre className="text-xs bg-background p-3 rounded border overflow-auto max-h-64">
        {generateSchemaPreview(fields, schemaType)}
      </pre>
    </div>
  );
};