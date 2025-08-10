import React from 'react';
import { Handle, Position, useReactFlow } from '@xyflow/react';
import { FieldNode } from '../types';
import { Database, X } from 'lucide-react';
import { Button } from '@/components/ui/button';

interface SourceFieldNodeProps {
  id: string;
  data: {
    field: FieldNode;
  };
}

export const SourceFieldNode: React.FC<SourceFieldNodeProps> = ({ id, data }) => {
  const { field } = data;
  const { setNodes, setEdges } = useReactFlow();

  const handleDelete = () => {
    // Remove the node
    setNodes((nodes) => nodes.filter((node) => node.id !== id));
    
    // Remove any edges connected to this node
    setEdges((edges) => edges.filter((edge) => edge.source !== id && edge.target !== id));
  };

  return (
    <div className="bg-card border-2 border-muted rounded-lg p-3 min-w-[160px] shadow-sm relative group">
      {/* Delete button - only visible on hover */}
      <Button
        variant="ghost"
        size="sm"
        onClick={handleDelete}
        className="absolute -top-2 -right-2 h-6 w-6 p-0 bg-destructive text-destructive-foreground opacity-0 group-hover:opacity-100 transition-opacity rounded-full shadow-md hover:bg-destructive/80"
        title="Delete source field"
      >
        <X className="h-3 w-3" />
      </Button>

      <div className="flex items-center gap-2 mb-2">
        <Database className="h-4 w-4 text-blue-500" />
      </div>
      
      <div className="space-y-1">
        <div className="text-sm font-semibold text-primary">{field.name}</div>
        <div className="text-xs text-muted-foreground">{field.type}</div>
        <div className="text-xs text-muted-foreground truncate max-w-[140px]" title={field.path}>
          {field.path}
        </div>
      </div>

      <Handle
        type="source"
        position={Position.Right}
        className="w-3 h-3 bg-blue-500 border-2 border-white"
      />
    </div>
  );
};