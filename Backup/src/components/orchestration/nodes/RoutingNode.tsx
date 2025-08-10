import React, { useState } from 'react';
import { Handle, Position, useReactFlow } from '@xyflow/react';
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card';
import { Button } from '@/components/ui/button';
import { Badge } from '@/components/ui/badge';
import { Dialog, DialogContent, DialogHeader, DialogTitle } from '@/components/ui/dialog';
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from '@/components/ui/select';
import { Input } from '@/components/ui/input';
import { Label } from '@/components/ui/label';
import { Route, Settings, X } from 'lucide-react';

interface RoutingNodeProps {
  id: string;
  data: {
    routingType?: string;
    conditions?: Array<{ field: string; operator: string; value: string; route: string }>;
    showDeleteButton?: boolean;
    onConfigChange?: (id: string, config: any) => void;
  };
  selected?: boolean;
}

export const RoutingNode: React.FC<RoutingNodeProps> = ({ id, data, selected }) => {
  const [configOpen, setConfigOpen] = useState(false);
  const [routingType, setRoutingType] = useState(data.routingType || 'conditional');
  const [conditions, setConditions] = useState(data.conditions || []);
  const { setNodes, setEdges } = useReactFlow();

  const handleDelete = () => {
    setNodes((nodes) => nodes.filter((node) => node.id !== id));
    setEdges((edges) => edges.filter((edge) => edge.source !== id && edge.target !== id));
  };

  const addCondition = () => {
    setConditions([...conditions, { field: '', operator: 'equals', value: '', route: '' }]);
  };

  const updateCondition = (index: number, field: string, value: string) => {
    const newConditions = [...conditions];
    newConditions[index] = { ...newConditions[index], [field]: value };
    setConditions(newConditions);
  };

  const removeCondition = (index: number) => {
    setConditions(conditions.filter((_, i) => i !== index));
  };

  const saveConfig = () => {
    data.onConfigChange?.(id, { routingType, conditions });
    setConfigOpen(false);
  };

  const isConfigured = data.conditions && data.conditions.length > 0;

  return (
    <>
      <Card className="min-w-[200px] shadow-lg border-2 hover:border-primary/20 transition-colors bg-black text-white relative group">
        {/* Delete button - only visible on click */}
        {data.showDeleteButton && (
          <Button
            variant="ghost"
            size="sm"
            onClick={handleDelete}
            className="absolute -top-2 -right-2 h-6 w-6 p-0 bg-destructive text-destructive-foreground opacity-100 transition-opacity rounded-full shadow-md hover:bg-destructive/80"
            title="Delete routing node"
          >
            <X className="h-3 w-3" />
          </Button>
        )}

        <CardHeader className="pb-2">
          <div className="flex items-center justify-between">
            <div className="flex items-center gap-2">
              <Route className="h-5 w-5 text-white" />
              <CardTitle className="text-sm font-medium text-white">Routing Logic</CardTitle>
            </div>
            <Badge variant={isConfigured ? "default" : "secondary"} className="text-xs bg-white text-black">
              {isConfigured ? "Configured" : "Setup Required"}
            </Badge>
          </div>
        </CardHeader>
        
        <CardContent className="pt-0">
          <Button
            variant="outline"
            size="sm"
            className="w-full bg-gray-800 text-white border-gray-700 hover:bg-gray-700"
            onClick={(e) => {
              e.stopPropagation();
              e.preventDefault();
              setConfigOpen(true);
            }}
            onMouseDown={(e) => e.stopPropagation()}
          >
            <Settings className="h-4 w-4 mr-2" />
            Configure
          </Button>
        </CardContent>

        <Handle
          type="target"
          position={Position.Left}
          className="w-3 h-3 bg-orange-500 border-2 border-white"
        />
        
        <Handle
          type="source"
          position={Position.Right}
          id="default"
          className="w-3 h-3 bg-orange-500 border-2 border-white"
          style={{ top: '30%' }}
        />
        
        <Handle
          type="source"
          position={Position.Right}
          id="route1"
          className="w-3 h-3 bg-orange-500 border-2 border-white"
          style={{ top: '50%' }}
        />
        
        <Handle
          type="source"
          position={Position.Right}
          id="route2"
          className="w-3 h-3 bg-orange-500 border-2 border-white"
          style={{ top: '70%' }}
        />
      </Card>

      <Dialog open={configOpen} onOpenChange={setConfigOpen}>
        <DialogContent className="max-w-2xl z-[9999]" style={{ zIndex: 9999 }}>
          <DialogHeader>
            <DialogTitle>Configure Routing Logic</DialogTitle>
          </DialogHeader>
          
          <div className="space-y-4">
            <div className="space-y-2">
              <Label htmlFor="routingType">Routing Type</Label>
              <Select value={routingType} onValueChange={setRoutingType}>
                <SelectTrigger>
                  <SelectValue placeholder="Select routing type" />
                </SelectTrigger>
                <SelectContent>
                  <SelectItem value="conditional">Conditional Routing</SelectItem>
                  <SelectItem value="content-based">Content-Based Routing</SelectItem>
                  <SelectItem value="load-balance">Load Balancing</SelectItem>
                  <SelectItem value="round-robin">Round Robin</SelectItem>
                </SelectContent>
              </Select>
            </div>

            {routingType === 'conditional' && (
              <div className="space-y-3">
                <div className="flex items-center justify-between">
                  <Label>Routing Conditions</Label>
                  <Button onClick={addCondition} size="sm" variant="outline">
                    Add Condition
                  </Button>
                </div>
                
                {conditions.map((condition, index) => (
                  <div key={index} className="grid grid-cols-5 gap-2 items-end">
                    <div>
                      <Label className="text-xs">Field</Label>
                      <Input
                        placeholder="Field name"
                        value={condition.field}
                        onChange={(e) => updateCondition(index, 'field', e.target.value)}
                      />
                    </div>
                    <div>
                      <Label className="text-xs">Operator</Label>
                      <Select
                        value={condition.operator}
                        onValueChange={(value) => updateCondition(index, 'operator', value)}
                      >
                        <SelectTrigger>
                          <SelectValue />
                        </SelectTrigger>
                        <SelectContent>
                          <SelectItem value="equals">Equals</SelectItem>
                          <SelectItem value="contains">Contains</SelectItem>
                          <SelectItem value="starts-with">Starts With</SelectItem>
                          <SelectItem value="greater-than">Greater Than</SelectItem>
                          <SelectItem value="less-than">Less Than</SelectItem>
                        </SelectContent>
                      </Select>
                    </div>
                    <div>
                      <Label className="text-xs">Value</Label>
                      <Input
                        placeholder="Condition value"
                        value={condition.value}
                        onChange={(e) => updateCondition(index, 'value', e.target.value)}
                      />
                    </div>
                    <div>
                      <Label className="text-xs">Route</Label>
                      <Select
                        value={condition.route}
                        onValueChange={(value) => updateCondition(index, 'route', value)}
                      >
                        <SelectTrigger>
                          <SelectValue />
                        </SelectTrigger>
                        <SelectContent>
                          <SelectItem value="default">Default</SelectItem>
                          <SelectItem value="route1">Route 1</SelectItem>
                          <SelectItem value="route2">Route 2</SelectItem>
                        </SelectContent>
                      </Select>
                    </div>
                    <Button
                      variant="outline"
                      size="sm"
                      onClick={() => removeCondition(index)}
                    >
                      <X className="h-3 w-3" />
                    </Button>
                  </div>
                ))}
              </div>
            )}
            
            <div className="flex justify-end space-x-2 pt-4">
              <Button variant="outline" onClick={() => setConfigOpen(false)}>
                Cancel
              </Button>
              <Button onClick={saveConfig}>
                Save Configuration
              </Button>
            </div>
          </div>
        </DialogContent>
      </Dialog>
    </>
  );
};