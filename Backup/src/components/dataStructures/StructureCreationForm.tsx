import React from 'react';
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '@/components/ui/card';
import { Input } from '@/components/ui/input';
import { Label } from '@/components/ui/label';
import { Textarea } from '@/components/ui/textarea';
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from '@/components/ui/select';

interface StructureCreationFormProps {
  structureName: string;
  setStructureName: (name: string) => void;
  structureDescription: string;
  setStructureDescription: (description: string) => void;
}

export const StructureCreationForm: React.FC<StructureCreationFormProps> = ({
  structureName,
  setStructureName,
  structureDescription,
  setStructureDescription
}) => {
  return (
    <Card className="animate-scale-in">
      <CardHeader>
        <CardTitle>Create Data Structure</CardTitle>
        <CardDescription>Import existing schemas or create custom structures</CardDescription>
      </CardHeader>
      <CardContent>
        <div className="space-y-4">
          <div className="space-y-2">
            <Label htmlFor="structureName">Structure Name *</Label>
            <Input
              id="structureName"
              placeholder="e.g., Customer Order Schema"
              value={structureName}
              onChange={(e) => setStructureName(e.target.value)}
              className="transition-all duration-300 focus:scale-[1.01]"
            />
          </div>
          
          <div className="space-y-2">
            <Label htmlFor="description">Description</Label>
            <Textarea
              id="description"
              placeholder="Describe the purpose and context of this data structure..."
              value={structureDescription}
              onChange={(e) => setStructureDescription(e.target.value)}
              className="transition-all duration-300 focus:scale-[1.01]"
              rows={3}
            />
          </div>
        </div>
      </CardContent>
    </Card>
  );
};