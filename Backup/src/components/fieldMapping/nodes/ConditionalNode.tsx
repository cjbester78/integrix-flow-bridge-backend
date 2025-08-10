import React, { useState } from 'react';
import { Handle, Position } from '@xyflow/react';
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from '@/components/ui/select';
import { Input } from '@/components/ui/input';
import { Label } from '@/components/ui/label';
import { GitBranch } from 'lucide-react';

interface ConditionalNodeProps {
  data: {
    condition: 'equals' | 'notEquals' | 'contains' | 'startsWith' | 'endsWith' | 'isEmpty' | 'isNotEmpty';
    compareValue: string;
  };
}

export const ConditionalNode: React.FC<ConditionalNodeProps> = ({ data }) => {
  const [condition, setCondition] = useState(data.condition);
  const [compareValue, setCompareValue] = useState(data.compareValue);

  return (
    <div className="bg-card border-2 border-yellow-200 rounded-lg p-3 min-w-[180px] shadow-sm">
      <div className="flex items-center gap-2 mb-2">
        <GitBranch className="h-4 w-4 text-yellow-500" />
        <span className="text-sm font-medium text-foreground">Conditional</span>
      </div>
      
      <div className="space-y-2">
        <div>
          <Label className="text-xs">Condition</Label>
          <Select value={condition} onValueChange={(value: any) => setCondition(value)}>
            <SelectTrigger className="w-full h-7 text-xs">
              <SelectValue />
            </SelectTrigger>
            <SelectContent>
              <SelectItem value="equals">Equals</SelectItem>
              <SelectItem value="notEquals">Not Equals</SelectItem>
              <SelectItem value="contains">Contains</SelectItem>
              <SelectItem value="startsWith">Starts With</SelectItem>
              <SelectItem value="endsWith">Ends With</SelectItem>
              <SelectItem value="isEmpty">Is Empty</SelectItem>
              <SelectItem value="isNotEmpty">Is Not Empty</SelectItem>
            </SelectContent>
          </Select>
        </div>

        {!['isEmpty', 'isNotEmpty'].includes(condition) && (
          <div>
            <Label className="text-xs">Compare Value</Label>
            <Input
              placeholder="Value to compare"
              value={compareValue}
              onChange={(e) => setCompareValue(e.target.value)}
              className="h-7 text-xs"
            />
          </div>
        )}
      </div>

      {/* Input handle */}
      <Handle
        type="target"
        position={Position.Left}
        id="input"
        className="w-3 h-3 bg-yellow-500 border-2 border-white"
        style={{ top: 40 }}
      />

      {/* True output handle */}
      <Handle
        type="source"
        position={Position.Right}
        id="true"
        className="w-3 h-3 bg-green-500 border-2 border-white"
        style={{ top: 35 }}
      />

      {/* False output handle */}
      <Handle
        type="source"
        position={Position.Right}
        id="false"
        className="w-3 h-3 bg-red-500 border-2 border-white"
        style={{ top: 55 }}
      />

      {/* Labels for outputs */}
      <div className="absolute -right-8 top-7 text-xs text-green-600 font-medium">T</div>
      <div className="absolute -right-8 top-12 text-xs text-red-600 font-medium">F</div>
    </div>
  );
};