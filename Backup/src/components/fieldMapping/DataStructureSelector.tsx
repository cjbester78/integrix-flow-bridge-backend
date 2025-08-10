import { Button } from '@/components/ui/button';
import { Input } from '@/components/ui/input';
import { Dialog, DialogContent, DialogHeader, DialogTitle, DialogTrigger } from '@/components/ui/dialog';
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from '@/components/ui/select';
import { Search, Settings } from 'lucide-react';
import { useDataStructures } from '@/hooks/useDataStructures';
import { DataStructure } from '@/types/dataStructures';

interface DataStructureSelectorProps {
  isOpen: boolean;
  onOpenChange: (open: boolean) => void;
  selectedService: string;
  onSelectService: (service: string) => void;
  title: string;
  usage: 'source' | 'target';
  businessComponentId?: string;
}

export function DataStructureSelector({ 
  isOpen, 
  onOpenChange, 
  selectedService, 
  onSelectService, 
  title,
  usage,
  businessComponentId
}: DataStructureSelectorProps) {
  const { structures } = useDataStructures();
  
  // Filter structures by usage type and optionally by business component
  const filteredStructures = structures.filter(structure => {
    const usageMatch = structure.usage === usage;
    const businessComponentMatch = !businessComponentId || structure.customerId === businessComponentId;
    return usageMatch && businessComponentMatch;
  });
  return (
    <Dialog open={isOpen} onOpenChange={onOpenChange}>
      <DialogTrigger asChild>
        <Button variant="outline" size="sm">
          <Settings className="h-4 w-4 mr-2" />
          Select
        </Button>
      </DialogTrigger>
      <DialogContent className="max-w-2xl">
        <DialogHeader>
          <DialogTitle>{title}</DialogTitle>
        </DialogHeader>
        <div className="space-y-4">
          <div className="space-y-2">
            <label className="text-sm font-medium">Select Data Structure</label>
            <Select onValueChange={onSelectService} value={selectedService}>
              <SelectTrigger className="w-full">
                <SelectValue placeholder="Choose a data structure..." />
              </SelectTrigger>
              <SelectContent className="bg-background border shadow-lg z-50">
                {filteredStructures.map((structure) => (
                  <SelectItem key={structure.id} value={structure.name}>
                    <div className="flex flex-col">
                      <span className="font-medium">{structure.name}</span>
                      <span className="text-xs text-muted-foreground">
                        {structure.type.toUpperCase()} • {structure.description || 'No description'}
                      </span>
                    </div>
                  </SelectItem>
                ))}
              </SelectContent>
            </Select>
          </div>
          
          {filteredStructures.length === 0 && (
            <div className="border rounded-md p-3 space-y-2">
              <div className="text-sm text-muted-foreground">
                No data structures found for {usage} usage. Please create data structures on the Data Structures page first.
              </div>
            </div>
          )}
          
          {selectedService && (
            <div className="border rounded-md p-3 space-y-2 bg-muted/20">
              <div className="text-sm font-medium">Selected Structure</div>
              <div className="text-sm text-muted-foreground">
                {filteredStructures.find(s => s.name === selectedService)?.description || selectedService}
              </div>
            </div>
          )}
        </div>
      </DialogContent>
    </Dialog>
  );
}