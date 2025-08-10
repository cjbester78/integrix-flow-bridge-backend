import React, { useState } from 'react';
import { Handle, Position, useReactFlow } from '@xyflow/react';
import { Button } from '@/components/ui/button';
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card';
import { Dialog, DialogContent, DialogHeader, DialogTitle } from '@/components/ui/dialog';
import { Badge } from '@/components/ui/badge';
import { Settings, ArrowRightLeft, Code, Filter, Plus, X } from 'lucide-react';
import { FieldMappingScreen } from '@/components/FieldMappingScreen';

interface TransformationNodeProps {
  id: string;
  data: {
    transformationType: string;
    transformationConfig: any;
    showDeleteButton?: boolean;
    onConfigChange: (config: any) => void;
  };
  selected?: boolean;
}

const getTransformationIcon = (type: string) => {
  switch (type) {
    case 'field-mapping':
      return ArrowRightLeft;
    case 'custom-function':
      return Code;
    case 'filter':
      return Filter;
    case 'enrichment':
      return Plus;
    default:
      return ArrowRightLeft;
  }
};

const getTransformationName = (type: string) => {
  switch (type) {
    // Mapping & Transformation
    case 'field-mapping':
      return 'Field Mapping';
    case 'data-transformation':
      return 'Data Transformation';
    case 'format-conversion':
      return 'Format Conversion';
    case 'value-mapping':
      return 'Value Mapping';
    case 'structure-mapping':
      return 'Structure Mapping';
    case 'conditional-mapping':
      return 'Conditional Mapping';
    case 'aggregation':
      return 'Aggregation';
    case 'split-mapping':
      return 'Split Mapping';
    
    // File Transformations
    case 'csv-to-xml':
      return 'CSV to XML';
    case 'json-to-xml':
      return 'JSON to XML';
    case 'xml-to-json':
      return 'XML to JSON';
    case 'excel-to-csv':
      return 'Excel to CSV';
    case 'pdf-extractor':
      return 'PDF Extractor';
    case 'file-splitter':
      return 'File Splitter';
    case 'file-merger':
      return 'File Merger';
    case 'file-validator':
      return 'File Validator';
    
    // Scripts & Functions
    case 'javascript-script':
      return 'JavaScript Script';
    case 'groovy-script':
      return 'Groovy Script';
    case 'xslt-transformation':
      return 'XSLT Transformation';
    case 'custom-function':
      return 'Custom Function';
    case 'lookup-function':
      return 'Lookup Function';
    case 'calculation-function':
      return 'Calculation Function';
    case 'validation-function':
      return 'Validation Function';
    case 'enrichment-function':
      return 'Enrichment Function';
    
    // BPMN Activities
    case 'task':
      return 'Task';
    case 'service-task':
      return 'Service Task';
    case 'user-task':
      return 'User Task';
    case 'script-task':
      return 'Script Task';
    case 'manual-task':
      return 'Manual Task';
    case 'business-rule-task':
      return 'Business Rule Task';
    case 'receive-task':
      return 'Receive Task';
    case 'send-task':
      return 'Send Task';
    
    // Legacy cases
    case 'filter':
      return 'Message Filter';
    case 'enrichment':
      return 'Data Enrichment';
    
    default:
      return 'Transformation';
  }
};

export const TransformationNode: React.FC<TransformationNodeProps> = ({ id, data, selected }) => {
  const [configOpen, setConfigOpen] = useState(false);
  const { setNodes, setEdges } = useReactFlow();
  const Icon = getTransformationIcon(data.transformationType);
  const transformationName = getTransformationName(data.transformationType);
  const isConfigured = data.transformationConfig && Object.keys(data.transformationConfig).length > 0;

  const handleDelete = () => {
    setNodes((nodes) => nodes.filter((node) => node.id !== id));
    setEdges((edges) => edges.filter((edge) => edge.source !== id && edge.target !== id));
  };

  return (
    <>
      <Card 
        className="min-w-[200px] shadow-lg border-2 hover:border-primary/20 transition-colors bg-black text-white relative group"
      >
        {/* Delete button - only visible on double-click */}
        {data.showDeleteButton && (
          <Button
            variant="ghost"
            size="sm"
            onClick={handleDelete}
            className="absolute -top-2 -right-2 h-6 w-6 p-0 bg-destructive text-destructive-foreground opacity-100 transition-opacity rounded-full shadow-md hover:bg-destructive/80"
            title="Delete transformation"
          >
            <X className="h-3 w-3" />
          </Button>
        )}
        
        <CardHeader className="pb-2">
          <div className="flex items-center justify-between">
            <div className="flex items-center gap-2">
              <Icon className="h-5 w-5 text-white" />
              <CardTitle className="text-sm font-medium text-white">{transformationName}</CardTitle>
            </div>
            <Badge variant={isConfigured ? "default" : "secondary"} className="text-xs bg-white text-black">
              {isConfigured ? "Configured" : "Setup Required"}
            </Badge>
          </div>
        </CardHeader>
        <CardContent className="pt-0">
          <Button
            size="sm"
            variant="outline"
            className="w-full bg-gray-800 text-white border-gray-700 hover:bg-gray-700"
            onClick={(e) => {
              e.stopPropagation();
              e.preventDefault();
              console.log('[TransformationNode] Configure clicked:', { 
                id, 
                transformationType: data.transformationType, 
                config: data.transformationConfig,
                isFieldMapping: data.transformationType === 'field-mapping'
              });
              setConfigOpen(true);
            }}
            onMouseDown={(e) => e.stopPropagation()}
          >
            <Settings className="h-4 w-4 mr-2" />
            Configure
          </Button>
        </CardContent>
        
        {/* Connection handles */}
        <Handle
          type="target"
          position={Position.Left}
          className="w-3 h-3 bg-purple-500 border-2 border-white"
        />
        <Handle
          type="source"
          position={Position.Right}
          className="w-3 h-3 bg-purple-500 border-2 border-white"
        />
      </Card>

      {/* Configuration Dialog */}
      {data.transformationType === 'field-mapping' ? (
        <Dialog open={configOpen} onOpenChange={setConfigOpen}>
          <DialogContent className="max-w-[95vw] max-h-[95vh] p-0 z-[9999]" style={{ zIndex: 9999 }}>
            <FieldMappingScreen
              onClose={() => setConfigOpen(false)}
              onSave={(mappings) => {
                data.onConfigChange({ fieldMappings: mappings });
                setConfigOpen(false);
              }}
              initialMappingName={`${transformationName} - ${id}`}
              sampleStructures={[]}
            />
          </DialogContent>
        </Dialog>
      ) : (
        <Dialog open={configOpen} onOpenChange={setConfigOpen}>
          <DialogContent className="max-w-2xl z-[9999]" style={{ zIndex: 9999 }}>
            <DialogHeader>
              <DialogTitle>Configure {transformationName}</DialogTitle>
            </DialogHeader>
            <div className="mt-4 p-4 border border-dashed border-muted-foreground rounded-lg text-center">
              <p className="text-sm text-muted-foreground">
                {transformationName} configuration panel coming soon...
              </p>
            </div>
          </DialogContent>
        </Dialog>
      )}
    </>
  );
};