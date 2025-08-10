import { Input } from '@/components/ui/input';
import { Label } from '@/components/ui/label';
import { Alert, AlertDescription } from '@/components/ui/alert';
import { Search } from 'lucide-react';
import { FieldNode, FieldMapping } from './types';
import { FieldTree } from './FieldTree';
import { DataStructureSelector } from './DataStructureSelector';

interface TargetPanelProps {
  fields: FieldNode[];
  mappings: FieldMapping[];
  selectedService: string;
  searchValue: string;
  showSelector: boolean;
  selectedField?: FieldNode | null;
  onSearchChange: (value: string) => void;
  onShowSelectorChange: (show: boolean) => void;
  onSelectService: (service: string) => void;
  onToggleExpanded: (nodeId: string, isSource: boolean) => void;
  onDragOver: (e: React.DragEvent) => void;
  onDrop: (field: FieldNode) => void;
  onSelectField?: (field: FieldNode) => void;
}

export function TargetPanel({ 
  fields, 
  mappings, 
  selectedService, 
  searchValue, 
  showSelector,
  selectedField,
  onSearchChange,
  onShowSelectorChange,
  onSelectService,
  onToggleExpanded,
  onDragOver,
  onDrop,
  onSelectField
}: TargetPanelProps) {
  return (
    <div className="w-1/3 border-l bg-muted/20 animate-fade-in">
      <div className="p-4 border-b bg-background">
        <div className="flex items-center justify-between mb-3">
          <Label className="font-semibold">Target Message</Label>
          <DataStructureSelector
            isOpen={showSelector}
            onOpenChange={onShowSelectorChange}
            selectedService={selectedService}
            onSelectService={onSelectService}
            title="Select Target Message"
            usage="target"
          />
        </div>
        
        <div className="text-sm text-muted-foreground mb-3 p-2 bg-background border rounded">
          {selectedService || 'No target selected'}
        </div>
        
        <div className="relative">
          <Search className="h-4 w-4 absolute left-3 top-3 text-muted-foreground" />
          <Input
            placeholder="Search target fields..."
            value={searchValue}
            onChange={(e) => onSearchChange(e.target.value)}
            className="pl-10"
          />
        </div>
      </div>
      
      <div className="p-4 h-[calc(100%-140px)] overflow-y-auto">
        {fields.length === 0 ? (
          <Alert>
            <AlertDescription>
              Select a target data structure to view fields
            </AlertDescription>
          </Alert>
        ) : (
          <FieldTree
            fields={fields}
            mappings={mappings}
            side="target"
            selectedField={selectedField}
            onToggleExpanded={onToggleExpanded}
            onDragOver={onDragOver}
            onDrop={onDrop}
            onSelectField={onSelectField}
          />
        )}
      </div>
    </div>
  );
}