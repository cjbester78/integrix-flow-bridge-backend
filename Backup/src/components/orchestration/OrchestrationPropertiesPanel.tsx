import React from 'react';
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card';
import { Badge } from '@/components/ui/badge';
import { Separator } from '@/components/ui/separator';
import { Node } from '@xyflow/react';
import { Settings, Info, Clock, Zap } from 'lucide-react';

interface OrchestrationPropertiesPanelProps {
  selectedNode: Node | null;
  flowStats: {
    totalNodes: number;
    totalConnections: number;
    estimatedExecutionTime: string;
    complexity: 'Low' | 'Medium' | 'High';
  };
}

export const OrchestrationPropertiesPanel: React.FC<OrchestrationPropertiesPanelProps> = ({ 
  selectedNode, 
  flowStats 
}) => {
  const getNodeTypeName = (type: string) => {
    const typeMap: Record<string, string> = {
      'http-receiver': 'HTTP Receiver',
      'http-sender': 'HTTP Sender',
      'content-router': 'Content Router',
      'data-mapper': 'Data Mapper',
      'message-filter': 'Message Filter',
      'error-handler': 'Error Handler',
      'delay': 'Delay',
      'retry': 'Retry Logic',
      'database-adapter': 'Database Adapter',
      'mail-adapter': 'Mail Adapter',
      'ftp-adapter': 'FTP Adapter',
      'enricher': 'Data Enricher',
      'validator': 'Data Validator',
      'splitter': 'Message Splitter',
      'aggregator': 'Message Aggregator',
      'conditional': 'Conditional Logic',
      'translator': 'Data Translator',
      'circuit-breaker': 'Circuit Breaker',
      'dead-letter': 'Dead Letter Queue',
      'start': 'Start Node',
      'stop': 'Stop Node'
    };
    return typeMap[type] || type;
  };

  const getComplexityColor = (complexity: string) => {
    switch (complexity) {
      case 'Low': return 'bg-green-100 text-green-800';
      case 'Medium': return 'bg-yellow-100 text-yellow-800';
      case 'High': return 'bg-red-100 text-red-800';
      default: return 'bg-gray-100 text-gray-800';
    }
  };

  return (
    <div className="w-64 space-y-4">
      {/* Properties Panel */}
      <Card>
        <CardHeader className="pb-3">
          <CardTitle className="text-sm flex items-center gap-2">
            <Settings className="h-4 w-4" />
            Properties
          </CardTitle>
        </CardHeader>
        
        <CardContent className="space-y-3">
          {selectedNode ? (
            <>
              <div className="space-y-2">
                <div className="text-xs font-medium">Node Type</div>
                <Badge variant="secondary" className="text-xs">
                  {getNodeTypeName(selectedNode.type || 'unknown')}
                </Badge>
              </div>
              
              <div className="space-y-2">
                <div className="text-xs font-medium">Node ID</div>
                <div className="text-xs text-muted-foreground font-mono">
                  {selectedNode.id}
                </div>
              </div>
              
              <div className="space-y-2">
                <div className="text-xs font-medium">Position</div>
                <div className="text-xs text-muted-foreground">
                  X: {Math.round(selectedNode.position.x)}, Y: {Math.round(selectedNode.position.y)}
                </div>
              </div>
              
              {selectedNode.data && Object.keys(selectedNode.data).length > 0 && (
                <div className="space-y-2">
                  <div className="text-xs font-medium">Configuration</div>
                  <div className="text-xs text-muted-foreground">
                    {selectedNode.data.configured ? (
                      <Badge variant="default" className="text-xs">Configured</Badge>
                    ) : (
                      <Badge variant="outline" className="text-xs">Not Configured</Badge>
                    )}
                  </div>
                </div>
              )}
            </>
          ) : (
            <div className="text-xs text-muted-foreground text-center py-4">
              Select a node to view properties
            </div>
          )}
        </CardContent>
      </Card>

      {/* Flow Status */}
      <Card>
        <CardHeader className="pb-3">
          <CardTitle className="text-sm flex items-center gap-2">
            <Info className="h-4 w-4" />
            Flow Status
          </CardTitle>
        </CardHeader>
        
        <CardContent className="space-y-3">
          <div className="grid grid-cols-2 gap-3">
            <div className="space-y-1">
              <div className="text-xs text-muted-foreground">Nodes</div>
              <div className="text-sm font-semibold">{flowStats.totalNodes}</div>
            </div>
            <div className="space-y-1">
              <div className="text-xs text-muted-foreground">Connections</div>
              <div className="text-sm font-semibold">{flowStats.totalConnections}</div>
            </div>
          </div>
          
          <Separator />
          
          <div className="space-y-2">
            <div className="flex items-center gap-2">
              <Clock className="h-3 w-3" />
              <div className="text-xs text-muted-foreground">Est. Execution</div>
            </div>
            <div className="text-xs font-medium">{flowStats.estimatedExecutionTime}</div>
          </div>
          
          <div className="space-y-2">
            <div className="flex items-center gap-2">
              <Zap className="h-3 w-3" />
              <div className="text-xs text-muted-foreground">Complexity</div>
            </div>
            <Badge className={`text-xs ${getComplexityColor(flowStats.complexity)}`}>
              {flowStats.complexity}
            </Badge>
          </div>
        </CardContent>
      </Card>
    </div>
  );
};