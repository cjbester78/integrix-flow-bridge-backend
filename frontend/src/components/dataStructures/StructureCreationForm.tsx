import React, { useState } from 'react';
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '@/components/ui/card';
import { Input } from '@/components/ui/input';
import { Label } from '@/components/ui/label';
import { Textarea } from '@/components/ui/textarea';
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from '@/components/ui/select';
import { Button } from '@/components/ui/button';
import { FileCode2 } from 'lucide-react';
import { WSDLGeneratorModal } from '../wsdl/WSDLGeneratorModal';

interface StructureCreationFormProps {
  structureName: string;
  setStructureName: (name: string) => void;
  structureDescription: string;
  setStructureDescription: (description: string) => void;
  structureUsage?: 'source' | 'target';
  setStructureUsage?: (usage: 'source' | 'target') => void;
  isEditMode?: boolean;
}

export const StructureCreationForm: React.FC<StructureCreationFormProps> = ({
  structureName,
  setStructureName,
  structureDescription,
  setStructureDescription,
  structureUsage = 'source',
  setStructureUsage,
  isEditMode = false
}) => {
  const [showWsdlGenerator, setShowWsdlGenerator] = useState(false);

  return (
    <>
      <Card className="animate-scale-in">
        <CardHeader>
          <div className="flex items-center justify-between">
            <div>
              <CardTitle>{isEditMode ? 'Edit' : 'Create'} Data Structure</CardTitle>
              <CardDescription>Import existing schemas or create custom structures</CardDescription>
            </div>
            <Button
              variant="outline"
              size="sm"
              onClick={() => setShowWsdlGenerator(true)}
              className="gap-2"
            >
              <FileCode2 className="h-4 w-4" />
              Generate WSDL
            </Button>
          </div>
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
          
          {setStructureUsage && (
            <div className="space-y-2">
              <Label htmlFor="usage">Usage</Label>
              <Select
                value={structureUsage}
                onValueChange={(value) => {
                  const newUsage = value as 'source' | 'target';
                  setStructureUsage(newUsage);
                  
                  // Update the name suffix when usage changes
                  if (structureName) {
                    let baseName = structureName;
                    // Remove existing suffix if present
                    if (baseName.endsWith('_Out') || baseName.endsWith('_In')) {
                      baseName = baseName.substring(0, baseName.length - 4);
                    }
                    // Add new suffix
                    const suffix = newUsage === 'source' ? '_Out' : '_In';
                    setStructureName(baseName + suffix);
                  }
                }}
              >
                <SelectTrigger id="usage">
                  <SelectValue />
                </SelectTrigger>
                <SelectContent>
                  <SelectItem value="source">Source (Outbound)</SelectItem>
                  <SelectItem value="target">Target (Inbound)</SelectItem>
                </SelectContent>
              </Select>
              <p className="text-xs text-muted-foreground">
                Source structures are used by Sender Adapters (outbound - receive FROM external systems).
                Target structures are used by Receiver Adapters (inbound - send TO external systems)
              </p>
            </div>
          )}
        </div>
      </CardContent>
    </Card>
    
    <WSDLGeneratorModal 
      isOpen={showWsdlGenerator}
      onClose={() => setShowWsdlGenerator(false)}
    />
    </>
  );
};