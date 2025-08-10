import React, { useState } from 'react';
import { Dialog, DialogContent, DialogHeader, DialogTitle } from '@/components/ui/dialog';
import { Button } from '@/components/ui/button';
import { Input } from '@/components/ui/input';
import { ScrollArea } from '@/components/ui/scroll-area';
import { FieldNode } from './types';
import { ChevronDown, ChevronRight, Database, Search, X } from 'lucide-react';
import { Badge } from '@/components/ui/badge';

interface FieldSelectorDialogProps {
  open: boolean;
  onClose: () => void;
  sourceFields: FieldNode[];
  onSelectField: (field: FieldNode) => void;
  excludeFields?: string[]; // Field IDs to exclude from selection
}

export const FieldSelectorDialog: React.FC<FieldSelectorDialogProps> = ({
  open,
  onClose,
  sourceFields,
  onSelectField,
  excludeFields = []
}) => {
  const [searchTerm, setSearchTerm] = useState('');
  const [expandedNodes, setExpandedNodes] = useState<Set<string>>(new Set());

  const toggleExpanded = (nodeId: string) => {
    setExpandedNodes(prev => {
      const newSet = new Set(prev);
      if (newSet.has(nodeId)) {
        newSet.delete(nodeId);
      } else {
        newSet.add(nodeId);
      }
      return newSet;
    });
  };

  const handleFieldSelect = (field: FieldNode) => {
    onSelectField(field);
    onClose();
    setSearchTerm('');
    setExpandedNodes(new Set());
  };

  const renderField = (field: FieldNode, level: number = 0): JSX.Element | null => {
    const isLeaf = !field.children || field.children.length === 0;
    const isExpanded = expandedNodes.has(field.id);
    const isExcluded = excludeFields.includes(field.id);
    
    // Filter based on search term
    const matchesSearch = !searchTerm || 
      field.name.toLowerCase().includes(searchTerm.toLowerCase()) ||
      field.path.toLowerCase().includes(searchTerm.toLowerCase()) ||
      field.type.toLowerCase().includes(searchTerm.toLowerCase());

    // Check if any children match the search
    const hasMatchingChildren = field.children?.some(child => 
      child.name.toLowerCase().includes(searchTerm.toLowerCase()) ||
      child.path.toLowerCase().includes(searchTerm.toLowerCase()) ||
      child.type.toLowerCase().includes(searchTerm.toLowerCase())
    );

    if (!matchesSearch && !hasMatchingChildren) {
      return null;
    }

    return (
      <div key={field.id} className="w-full">
        <div
          className={`flex items-center p-2 border rounded-md transition-colors ${
            isExcluded ? 'bg-muted/50 opacity-50' : 
            isLeaf ? 'hover:bg-primary/10 cursor-pointer border-dashed' : 
            'hover:bg-muted/50'
          }`}
          style={{ marginLeft: `${level * 16}px` }}
          onClick={() => {
            if (isLeaf && !isExcluded) {
              handleFieldSelect(field);
            } else if (!isLeaf) {
              toggleExpanded(field.id);
            }
          }}
        >
          {!isLeaf && (
            <Button
              variant="ghost"
              size="sm"
              onClick={(e) => {
                e.stopPropagation();
                toggleExpanded(field.id);
              }}
              className="mr-2 h-auto w-auto p-1 hover:bg-muted rounded"
            >
              {isExpanded ? <ChevronDown className="h-3 w-3" /> : <ChevronRight className="h-3 w-3" />}
            </Button>
          )}
          
          <div className="flex-1 flex items-center gap-2">
            <Database className="h-3 w-3 text-blue-500 flex-shrink-0" />
            <span className="font-medium text-sm">{field.name}</span>
            <Badge variant="outline" className="text-xs">{field.type}</Badge>
            {isExcluded && <Badge variant="secondary" className="text-xs">Already added</Badge>}
          </div>
          
          {isLeaf && !isExcluded && (
            <span className="text-xs text-muted-foreground">Click to select</span>
          )}
        </div>

        {!isLeaf && isExpanded && field.children && (
          <div className="ml-2">
            {field.children.map(child => renderField(child, level + 1))}
          </div>
        )}
      </div>
    );
  };

  // Collect all leaf fields for easy searching
  const getAllLeafFields = (fields: FieldNode[]): FieldNode[] => {
    const leafFields: FieldNode[] = [];
    
    const traverse = (field: FieldNode) => {
      if (!field.children || field.children.length === 0) {
        leafFields.push(field);
      } else {
        field.children.forEach(traverse);
      }
    };
    
    fields.forEach(traverse);
    return leafFields;
  };

  const availableFields = sourceFields.filter(field => 
    !excludeFields.includes(field.id)
  );

  const totalLeafFields = getAllLeafFields(sourceFields).length;
  const availableLeafFields = getAllLeafFields(availableFields).length;

  return (
    <Dialog open={open} onOpenChange={onClose}>
      <DialogContent className="max-w-2xl h-[80vh] flex flex-col">
        <DialogHeader className="flex-shrink-0">
          <DialogTitle className="flex items-center justify-between">
            <div className="flex items-center gap-3">
              <span>Select Source Field</span>
              <Badge variant="outline" className="text-xs">
                {availableLeafFields} of {totalLeafFields} available
              </Badge>
            </div>
            <Button variant="ghost" size="sm" onClick={onClose}>
              <X className="h-4 w-4" />
            </Button>
          </DialogTitle>
        </DialogHeader>

        <div className="flex-shrink-0 mb-4">
          <div className="relative">
            <Search className="absolute left-3 top-3 h-4 w-4 text-muted-foreground" />
            <Input
              placeholder="Search fields by name, path, or type..."
              value={searchTerm}
              onChange={(e) => setSearchTerm(e.target.value)}
              className="pl-10"
            />
          </div>
        </div>

         <ScrollArea className="flex-1">
          <div className="space-y-2 pr-4">
            {availableFields.length === 0 ? (
              <div className="text-center py-8 text-muted-foreground">
                <Database className="h-8 w-8 mx-auto mb-2 opacity-50" />
                <p>No available source fields to add</p>
              </div>
            ) : (
              availableFields.map(field => renderField(field))
            )}
          </div>
        </ScrollArea>

        <div className="flex-shrink-0 pt-4 border-t">
          <div className="text-sm text-muted-foreground">
            Click on any leaf field (shown with dashed border) to add it to your flow
          </div>
        </div>
      </DialogContent>
    </Dialog>
  );
};