import { FC } from 'react';
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '@/components/ui/card';
import { SegmentedControl, SegmentedControlOption } from '@/components/ui/segmented-control';
import { Label } from '@/components/ui/label';
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from '@/components/ui/select';
import { Alert, AlertDescription } from '@/components/ui/alert';
import { Info, AlertCircle } from 'lucide-react';

export interface TransformationConfig {
  mode: 'transform' | 'passthrough';
  dataStructureId?: string;
  requiresStructure?: boolean;
  structureType?: 'XML' | 'JSON' | 'WSDL';
}

interface TransformationToggleProps {
  config: TransformationConfig;
  onChange: (config: TransformationConfig) => void;
  adapterType: string;
  dataStructures?: Array<{ id: string; name: string; type: string }>;
  disabled?: boolean;
}

export const TransformationToggle: FC<TransformationToggleProps> = ({ 
  config, 
  onChange, 
  adapterType,
  dataStructures = [],
  disabled = false
}) => {
  const transformationOptions: SegmentedControlOption[] = [
    { value: 'transform', label: 'Transform Data' },
    { value: 'passthrough', label: 'Passthrough' }
  ];

  const handleModeChange = (mode: string) => {
    const newConfig: TransformationConfig = {
      ...config,
      mode: mode as 'transform' | 'passthrough'
    };

    // Clear structure if passthrough mode
    if (mode === 'passthrough') {
      newConfig.dataStructureId = undefined;
      newConfig.requiresStructure = false;
    } else {
      // Set structure requirement based on adapter type
      newConfig.requiresStructure = getStructureRequirement(adapterType);
    }

    onChange(newConfig);
  };

  const handleStructureChange = (structureId: string) => {
    onChange({
      ...config,
      dataStructureId: structureId
    });
  };

  const getStructureRequirement = (type: string): boolean => {
    const requiresStructure = ['FILE', 'FTP', 'SFTP', 'REST', 'HTTP', 'HTTPS', 'JSON', 'XML'];
    return requiresStructure.includes(type.toUpperCase());
  };

  const getAdapterSpecificMessage = () => {
    const upperType = adapterType.toUpperCase();
    
    if (['REST', 'HTTP', 'HTTPS'].includes(upperType)) {
      return (
        <Alert className="mt-4">
          <AlertCircle className="h-4 w-4" />
          <AlertDescription>
            For REST/HTTP adapters with transformation enabled, you must define the payload structure. 
            The payload will be automatically converted to XML for mapping.
          </AlertDescription>
        </Alert>
      );
    }

    if (['FILE', 'FTP', 'SFTP'].includes(upperType)) {
      return (
        <Alert className="mt-4">
          <Info className="h-4 w-4" />
          <AlertDescription>
            File adapters require an XML data structure when transformation is enabled. 
            The file content will be converted to/from XML for processing.
          </AlertDescription>
        </Alert>
      );
    }

    if (upperType === 'SOAP') {
      return (
        <Alert className="mt-4">
          <Info className="h-4 w-4" />
          <AlertDescription>
            SOAP adapters always require a WSDL which defines the data structures.
          </AlertDescription>
        </Alert>
      );
    }

    return null;
  };

  const filteredStructures = dataStructures.filter(structure => {
    if (['REST', 'HTTP', 'HTTPS'].includes(adapterType.toUpperCase())) {
      // For REST/HTTP, allow JSON or XML structures
      return ['JSON', 'XML'].includes(structure.type);
    }
    // For other adapters, show XML structures
    return structure.type === 'XML';
  });

  return (
    <Card>
      <CardHeader>
        <CardTitle>Data Transformation</CardTitle>
        <CardDescription>
          Choose whether to transform data or pass it through unchanged
        </CardDescription>
      </CardHeader>
      <CardContent className="space-y-4">
        <div className="space-y-2">
          <Label>Transformation Mode</Label>
          <SegmentedControl
            value={config.mode}
            onValueChange={handleModeChange}
            options={transformationOptions}
            disabled={disabled}
          />
        </div>

        {config.mode === 'transform' && config.requiresStructure && (
          <div className="space-y-2">
            <Label>Data Structure *</Label>
            <Select 
              value={config.dataStructureId} 
              onValueChange={handleStructureChange}
              disabled={disabled}
            >
              <SelectTrigger>
                <SelectValue placeholder="Select a data structure" />
              </SelectTrigger>
              <SelectContent>
                {filteredStructures.map((structure) => (
                  <SelectItem key={structure.id} value={structure.id}>
                    {structure.name} ({structure.type})
                  </SelectItem>
                ))}
              </SelectContent>
            </Select>
            {filteredStructures.length === 0 && (
              <p className="text-sm text-muted-foreground">
                No compatible data structures found. Please create one first.
              </p>
            )}
          </div>
        )}

        {config.mode === 'passthrough' && (
          <Alert>
            <Info className="h-4 w-4" />
            <AlertDescription>
              Passthrough mode transfers data without any transformation or XML conversion. 
              This is ideal for binary files or when you need to preserve the exact file format.
            </AlertDescription>
          </Alert>
        )}

        {config.mode === 'transform' && getAdapterSpecificMessage()}
      </CardContent>
    </Card>
  );
};