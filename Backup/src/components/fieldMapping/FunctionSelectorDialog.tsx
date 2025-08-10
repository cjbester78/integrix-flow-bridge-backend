import React, { useState } from 'react';
import { Dialog, DialogContent, DialogHeader, DialogTitle } from '@/components/ui/dialog';
import { Button } from '@/components/ui/button';
import { Input } from '@/components/ui/input';
import { ScrollArea } from '@/components/ui/scroll-area';
import { Badge } from '@/components/ui/badge';
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '@/components/ui/card';
import { Tabs, TabsContent, TabsList, TabsTrigger } from '@/components/ui/tabs';
import { Calculator, Search, X } from 'lucide-react';
import { 
  functionsByCategory, 
  TransformationFunction 
} from '@/services/transformationFunctions';

interface FunctionSelectorDialogProps {
  open: boolean;
  onClose: () => void;
  onSelectFunction: (func: TransformationFunction) => void;
}

export const FunctionSelectorDialog: React.FC<FunctionSelectorDialogProps> = ({
  open,
  onClose,
  onSelectFunction
}) => {
  const [searchTerm, setSearchTerm] = useState('');

  const filteredFunctions = Object.entries(functionsByCategory).reduce((acc, [category, functions]) => {
    const filtered = functions.filter(fn => 
      fn.name.toLowerCase().includes(searchTerm.toLowerCase()) ||
      fn.description.toLowerCase().includes(searchTerm.toLowerCase())
    );
    if (filtered.length > 0) {
      acc[category] = filtered;
    }
    return acc;
  }, {} as Record<string, TransformationFunction[]>);

  const handleFunctionSelect = (func: TransformationFunction) => {
    onSelectFunction(func);
    onClose();
    setSearchTerm('');
  };

  const totalFunctions = Object.values(functionsByCategory).flat().length;
  const filteredCount = Object.values(filteredFunctions).flat().length;

  return (
    <Dialog open={open} onOpenChange={onClose}>
      <DialogContent className="max-w-4xl h-[80vh] flex flex-col">
        <DialogHeader className="flex-shrink-0">
          <DialogTitle className="flex items-center justify-between">
            <div className="flex items-center gap-3">
              <Calculator className="h-5 w-5 text-primary" />
              <span>Select Function</span>
              <Badge variant="outline" className="text-xs">
                {filteredCount} of {totalFunctions} functions
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
              placeholder="Search functions by name or description..."
              value={searchTerm}
              onChange={(e) => setSearchTerm(e.target.value)}
              className="pl-10"
            />
          </div>
        </div>

        <div className="flex-1 overflow-hidden">
          {Object.keys(filteredFunctions).length === 0 ? (
            <div className="text-center py-8 text-muted-foreground">
              <Calculator className="h-8 w-8 mx-auto mb-2 opacity-50" />
              <p>No functions found matching your search</p>
            </div>
          ) : (
            <Tabs defaultValue={Object.keys(filteredFunctions)[0]} className="h-full flex flex-col">
              <TabsList className="grid w-full flex-shrink-0" style={{ gridTemplateColumns: `repeat(${Object.keys(filteredFunctions).length}, 1fr)` }}>
                {Object.keys(filteredFunctions).map((category) => (
                  <TabsTrigger key={category} value={category} className="capitalize text-xs">
                    {category} ({filteredFunctions[category].length})
                  </TabsTrigger>
                ))}
              </TabsList>

              {Object.entries(filteredFunctions).map(([category, functions]) => (
                <TabsContent key={category} value={category} className="flex-1 mt-4">
                  <ScrollArea className="h-full">
                    <div className="grid grid-cols-1 md:grid-cols-2 gap-3 pr-4">
                      {functions.map((func) => (
                        <Card 
                          key={func.name} 
                          className="cursor-pointer hover:bg-primary/10 transition-colors border-dashed hover:border-primary"
                          onClick={() => handleFunctionSelect(func)}
                        >
                          <CardHeader className="pb-2">
                            <div className="flex items-start justify-between">
                              <div className="flex-1">
                                <CardTitle className="text-sm font-medium flex items-center gap-2">
                                  <Calculator className="h-3 w-3 text-primary" />
                                  {func.name}
                                </CardTitle>
                                <CardDescription className="text-xs mt-1">
                                  {func.description}
                                </CardDescription>
                              </div>
                              <Badge variant="secondary" className="text-xs ml-2">
                                {func.category}
                              </Badge>
                            </div>
                          </CardHeader>
                          <CardContent className="pt-0">
                            <div className="flex items-center justify-between">
                              <span className="text-xs text-muted-foreground">
                                {func.parameters.length} parameter{func.parameters.length !== 1 ? 's' : ''}
                              </span>
                              <span className="text-xs text-primary font-medium">
                                Click to add
                              </span>
                            </div>
                          </CardContent>
                        </Card>
                      ))}
                    </div>
                  </ScrollArea>
                </TabsContent>
              ))}
            </Tabs>
          )}
        </div>

        <div className="flex-shrink-0 pt-4 border-t">
          <div className="text-sm text-muted-foreground">
            Select a function to add it to your flow. You can configure parameters after adding.
          </div>
        </div>
      </DialogContent>
    </Dialog>
  );
};