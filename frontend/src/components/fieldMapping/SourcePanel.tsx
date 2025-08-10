import { Input } from '@/components/ui/input';
import { Label } from '@/components/ui/label';
import { Alert, AlertDescription } from '@/components/ui/alert';
import { Search, Loader2 } from 'lucide-react';
import { FieldNode, FieldMapping } from './types';
import { FieldTree } from './FieldTree';
import { DataStructureSelector } from './DataStructureSelector';

interface SourcePanelProps {
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
  onDragStart: (field: FieldNode) => void;
  onDragEnd?: () => void;
  onSelectField?: (field: FieldNode) => void;
  isLoading?: boolean;
  hideSelector?: boolean;
}

export function SourcePanel({ 
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
  onDragStart,
  onDragEnd,
  onSelectField,
  isLoading = false,
  hideSelector = false
}: SourcePanelProps) {
  return (
    <div className="w-1/3 border-r bg-muted/20 animate-fade-in">
      <div className="p-4 border-b bg-background">
        <div className="flex items-center justify-between mb-3">
          <Label className="font-semibold">Source Message</Label>
          {!hideSelector && (
            <DataStructureSelector
              isOpen={showSelector}
              onOpenChange={onShowSelectorChange}
              selectedService={selectedService}
              onSelectService={onSelectService}
              title="Select Source Message"
              usage="source"
            />
          )}
        </div>
        
        <div className="text-sm text-muted-foreground mb-3 p-2 bg-background border rounded">
          {selectedService || 'No source selected'}
        </div>
        
        <div className="relative">
          <Search className="h-4 w-4 absolute left-3 top-3 text-muted-foreground" />
          <Input
            placeholder="Search source fields..."
            value={searchValue}
            onChange={(e) => onSearchChange(e.target.value)}
            className="pl-10"
          />
        </div>
      </div>
      
      <div className="p-4 h-[calc(100%-140px)] overflow-y-auto">
        {isLoading ? (
          <div className="flex items-center justify-center h-full">
            <div className="text-center space-y-3">
              <Loader2 className="h-8 w-8 animate-spin mx-auto text-muted-foreground" />
              <p className="text-sm text-muted-foreground">Converting structure to XML...</p>
            </div>
          </div>
        ) : fields.length === 0 ? (
          <Alert>
            <AlertDescription>
              Select a source data structure to view fields
            </AlertDescription>
          </Alert>
        ) : (
          <FieldTree
            fields={fields}
            mappings={mappings}
            side="source"
            selectedField={selectedField}
            onToggleExpanded={onToggleExpanded}
            onDragStart={onDragStart}
            onDragEnd={onDragEnd}
            onSelectField={onSelectField}
          />
        )}
      </div>
    </div>
  );
}