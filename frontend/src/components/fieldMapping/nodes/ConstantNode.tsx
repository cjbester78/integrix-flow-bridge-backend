import React, { useState } from 'react';
import { Handle, Position } from '@xyflow/react';
import { Input } from '@/components/ui/input';
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from '@/components/ui/select';
import { Hash } from 'lucide-react';

interface ConstantNodeProps {
  data: {
    value: string;
    type: 'string' | 'number' | 'boolean';
  };
}

export const ConstantNode: React.FC<ConstantNodeProps> = ({ data }) => {
  const [value, setValue] = useState(data.value);
  const [type, setType] = useState(data.type);

  return (
    <div className="bg-card border-2 border-green-200 rounded-lg p-3 min-w-[160px] shadow-sm">
      <div className="flex items-center gap-2 mb-2">
        <Hash className="h-4 w-4 text-green-500" />
        <span className="text-sm font-medium text-foreground">Constant</span>
      </div>
      
      <div className="space-y-2">
        <Select value={type} onValueChange={(value: any) => setType(value)}>
          <SelectTrigger className="w-full h-7 text-xs">
            <SelectValue />
          </SelectTrigger>
          <SelectContent>
            <SelectItem value="string">String</SelectItem>
            <SelectItem value="number">Number</SelectItem>
            <SelectItem value="boolean">Boolean</SelectItem>
          </SelectContent>
        </Select>

        {type === 'boolean' ? (
          <Select value={value} onValueChange={setValue}>
            <SelectTrigger className="w-full h-7 text-xs">
              <SelectValue />
            </SelectTrigger>
            <SelectContent>
              <SelectItem value="true">true</SelectItem>
              <SelectItem value="false">false</SelectItem>
            </SelectContent>
          </Select>
        ) : (
          <Input
            placeholder={`Enter ${type} value`}
            value={value}
            onChange={(e) => setValue(e.target.value)}
            type={type === 'number' ? 'number' : 'text'}
            className="h-7 text-xs"
          />
        )}

        <div className="text-xs text-muted-foreground">
          Value: <span className="font-mono">{value || 'undefined'}</span>
        </div>
      </div>

      <Handle
        type="source"
        position={Position.Right}
        className="w-3 h-3 bg-green-500 border-2 border-white"
      />
    </div>
  );
};