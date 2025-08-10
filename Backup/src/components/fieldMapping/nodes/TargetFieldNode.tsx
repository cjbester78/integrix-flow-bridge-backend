import React from 'react';
import { Handle, Position } from '@xyflow/react';
import { FieldNode } from '../types';
import { Target } from 'lucide-react';

interface TargetFieldNodeProps {
  data: {
    field: FieldNode;
  };
}

export const TargetFieldNode: React.FC<TargetFieldNodeProps> = ({ data }) => {
  const { field } = data;

  return (
    <div className="bg-card border-2 border-purple-200 rounded-lg p-3 min-w-[160px] shadow-sm">
      <div className="flex items-center gap-2 mb-2">
        <Target className="h-4 w-4 text-purple-500" />
      </div>
      
      <div className="space-y-1">
        <div className="text-sm font-semibold text-primary">{field.name}</div>
        <div className="text-xs text-muted-foreground">{field.type}</div>
        <div className="text-xs text-muted-foreground truncate max-w-[140px]" title={field.path}>
          {field.path}
        </div>
      </div>

      <Handle
        type="target"
        position={Position.Left}
        className="w-3 h-3 bg-purple-500 border-2 border-white"
      />
    </div>
  );
};