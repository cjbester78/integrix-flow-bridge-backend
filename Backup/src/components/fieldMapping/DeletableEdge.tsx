import React from 'react';
import {
  BaseEdge,
  EdgeLabelRenderer,
  getBezierPath,
  useReactFlow,
  Edge
} from '@xyflow/react';
import { Button } from '@/components/ui/button';
import { X } from 'lucide-react';

interface DeletableEdgeProps {
  id: string;
  sourceX: number;
  sourceY: number;
  targetX: number;
  targetY: number;
  sourcePosition: any;
  targetPosition: any;
  style?: React.CSSProperties;
  markerEnd?: any;
  selected?: boolean;
}

export const DeletableEdge: React.FC<DeletableEdgeProps> = ({
  id,
  sourceX,
  sourceY,
  targetX,
  targetY,
  sourcePosition,
  targetPosition,
  style = {},
  markerEnd,
  selected = false,
}) => {
  const { setEdges } = useReactFlow();
  const [edgePath, labelX, labelY] = getBezierPath({
    sourceX,
    sourceY,
    sourcePosition,
    targetX,
    targetY,
    targetPosition,
  });

  const onEdgeClick = (e: React.MouseEvent) => {
    e.stopPropagation();
    setEdges((edges) => edges.filter((edge) => edge.id !== id));
  };

  return (
    <>
      <BaseEdge path={edgePath} markerEnd={markerEnd} style={style} />
      {selected && (
        <EdgeLabelRenderer>
          <div
            className="absolute pointer-events-auto"
            style={{
              transform: `translate(-50%, -50%) translate(${labelX}px,${labelY}px)`,
            }}
          >
            <Button
              variant="ghost"
              size="sm"
              onClick={onEdgeClick}
              className="h-6 w-6 p-0 bg-destructive text-destructive-foreground hover:bg-destructive/80 rounded-full shadow-md"
              title="Delete connection"
            >
              <X className="h-3 w-3" />
            </Button>
          </div>
        </EdgeLabelRenderer>
      )}
    </>
  );
};