import { useState, useEffect } from 'react';
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '@/components/ui/card';
import { Button } from '@/components/ui/button';
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from '@/components/ui/select';
import { Badge } from '@/components/ui/badge';
import { Label } from '@/components/ui/label';
import { Textarea } from '@/components/ui/textarea';
import { 
  Plus, 
  Settings, 
  CheckCircle,
  Trash2,
  Code,
  Link,
  X,
  Save
} from 'lucide-react';
import { DataStructure } from '@/types/dataStructures';
import { FieldMapping } from '@/hooks/useFlowState';
import { useBusinessComponentAdapters } from '@/hooks/useBusinessComponentAdapters';

interface Transformation {
  id: string;
  name: string;
  description: string;
}


interface TransformationConfigurationCardProps {
  transformations: Transformation[];
  selectedTransformations: string[];
  showFieldMapping: boolean;
  sourceBusinessComponent: string;
  targetBusinessComponent: string;
  sourceStructure: string;
  targetStructure: string;
  fieldMappings: FieldMapping[];
  selectedTargetField: string | null;
  javaFunction: string;
  mappingName: string;
  sampleStructures: DataStructure[];
  onAddTransformation: (transformationId: string) => void;
  onRemoveTransformation: (transformationId: string) => void;
  onShowMappingScreen: () => void;
  onSourceStructureChange: (value: string) => void;
  onTargetStructureChange: (value: string) => void;
  onAddMapping: () => void;
  onRemoveMapping: (index: number) => void;
  onMappingChange: (index: number, field: 'targetField', value: string) => void;
  onAddSourceField: (mappingIndex: number, sourceField: string) => void;
  onRemoveSourceField: (mappingIndex: number, sourceFieldIndex: number) => void;
  onTargetFieldSelect: (fieldPath: string, mappingIndex: number) => void;
  onJavaFunctionChange: (value: string) => void;
  onSaveJavaFunction: () => void;
  onCloseJavaEditor: () => void;
}

export const TransformationConfigurationCard = ({
  transformations,
  selectedTransformations,
  showFieldMapping,
  sourceBusinessComponent,
  targetBusinessComponent,
  sourceStructure,
  targetStructure,
  fieldMappings,
  selectedTargetField,
  javaFunction,
  mappingName,
  sampleStructures,
  onAddTransformation,
  onRemoveTransformation,
  onShowMappingScreen,
  onSourceStructureChange,
  onTargetStructureChange,
  onAddMapping,
  onRemoveMapping,
  onMappingChange,
  onAddSourceField,
  onRemoveSourceField,
  onTargetFieldSelect,
  onJavaFunctionChange,
  onSaveJavaFunction,
  onCloseJavaEditor,
}: TransformationConfigurationCardProps) => {
  const { businessComponents, loading, getStructuresForBusinessComponent } = useBusinessComponentAdapters();
  const [businessComponentStructures, setBusinessComponentStructures] = useState<string[]>([]);

  useEffect(() => {
    if (sourceBusinessComponent) {
      loadBusinessComponentStructures(sourceBusinessComponent);
    }
  }, [sourceBusinessComponent]);

  const loadBusinessComponentStructures = async (businessComponentId: string) => {
    const allowedStructureIds = await getStructuresForBusinessComponent(businessComponentId);
    setBusinessComponentStructures(allowedStructureIds);
  };

  const getStructureById = (id: string) => sampleStructures.find(s => s.id === id);

  const getFilteredStructures = (businessComponentId: string, usage: 'source' | 'target') => {
    if (!businessComponentId) return sampleStructures.filter(s => s.usage === usage);
    return sampleStructures.filter(s => 
      businessComponentStructures.includes(s.id) && s.usage === usage
    );
  };

  const getFieldsFromStructure = (structure: any, prefix = ''): string[] => {
    if (!structure) return [];
    const fields: string[] = [];
    
    Object.entries(structure).forEach(([key, value]) => {
      const fieldPath = prefix ? `${prefix}.${key}` : key;
      fields.push(fieldPath);
      
      if (typeof value === 'object' && !Array.isArray(value) && typeof value !== 'string') {
        fields.push(...getFieldsFromStructure(value, fieldPath));
      }
    });
    
    return fields;
  };

  return (
    <>
      <Card className="animate-scale-in" style={{ animationDelay: '0.2s' }}>
        <CardHeader>
          <CardTitle>Data Transformations</CardTitle>
          <CardDescription>Configure how data should be transformed during the flow</CardDescription>
        </CardHeader>
        <CardContent className="space-y-4">
          {/* Removed Field Mapping Selection - users can only use Create Mapping button */}

          {/* Field Mapping Interface */}
          {showFieldMapping && (
            <div className="mt-6 space-y-6">
              <div className="flex items-center justify-between">
                <div className="flex items-center gap-2">
                  <CheckCircle className="h-4 w-4 text-success" />
                  <span className="font-medium">Field Mapping</span>
                </div>
                <div className="flex items-center gap-2">
                  <Button 
                    variant="ghost" 
                    size="sm"
                    onClick={() => onRemoveTransformation('field-mapping')}
                  >
                    <Trash2 className="h-4 w-4" />
                  </Button>
                </div>
              </div>

              {/* Show mappings if they exist, otherwise show create button */}
              {fieldMappings.length > 0 ? (
                <div className="border rounded-lg p-4 bg-muted/20">
                  <div className="space-y-3">
                    <div className="flex items-center justify-between">
                      <div>
                        <h4 className="font-medium text-primary">{mappingName || 'Untitled Mapping'}</h4>
                        <p className="text-xs text-muted-foreground">{fieldMappings.length} field mapping(s)</p>
                      </div>
                      <Button 
                        onClick={onShowMappingScreen}
                        variant="outline"
                        size="sm"
                      >
                        <Settings className="h-4 w-4 mr-2" />
                        Edit Mapping
                      </Button>
                    </div>
                  </div>
                </div>
              ) : (
                <div className="border rounded-lg p-4 bg-muted/20">
                  <div className="text-center py-8 text-muted-foreground">
                    <Link className="h-12 w-12 mx-auto mb-4 opacity-50" />
                    <p className="mb-2">Open the graphical mapping interface to configure field mappings</p>
                    {(!sourceBusinessComponent || !targetBusinessComponent) && (
                      <p className="text-xs text-amber-600 mb-4">
                        Please select both source and target business components above to enable mapping
                      </p>
                    )}
                    <Button 
                      onClick={onShowMappingScreen}
                      className="bg-gradient-primary hover:opacity-90 transition-all duration-300"
                      disabled={!sourceBusinessComponent || !targetBusinessComponent}
                    >
                      <Plus className="h-4 w-4 mr-2" />
                      Create Mapping
                    </Button>
                  </div>
                </div>
              )}
            </div>
          )}

          {!showFieldMapping && (
            <div className="text-center py-8 text-muted-foreground">
              <Settings className="h-12 w-12 mx-auto mb-4 opacity-50" />
              <p className="mb-4">Configure data transformations with field mapping</p>
              <p className="text-xs mb-4">Field mapping allows you to connect source and target fields with custom logic</p>
              {(!sourceBusinessComponent || !targetBusinessComponent) && (
                <p className="text-xs text-amber-600 mb-4">
                  Please select both source and target business components above to enable mapping
                </p>
              )}
              <Button 
                onClick={onShowMappingScreen}
                className="bg-gradient-primary hover:opacity-90 transition-all duration-300"
                disabled={!sourceBusinessComponent || !targetBusinessComponent}
              >
                <Plus className="h-4 w-4 mr-2" />
                Create Mapping
              </Button>
            </div>
          )}
        </CardContent>
      </Card>

      {/* Java Function Editor Dialog */}
      {selectedTargetField && (
        <div className="fixed inset-0 bg-black/50 flex items-center justify-center z-50">
          <div className="bg-card border rounded-lg p-6 w-full max-w-2xl mx-4 max-h-[80vh] overflow-y-auto">
            <div className="flex items-center justify-between mb-4">
              <div>
                <h3 className="text-lg font-semibold">Custom Java Function</h3>
                <p className="text-sm text-muted-foreground">
                  Target Field: <Badge variant="outline" className="text-xs">{selectedTargetField}</Badge>
                </p>
              </div>
              <Button 
                variant="ghost" 
                size="sm"
                onClick={onCloseJavaEditor}
              >
                <X className="h-4 w-4" />
              </Button>
            </div>
            
            <div className="space-y-4">
              <div>
                <Label htmlFor="javaFunction">Java Function Code</Label>
                <Textarea
                  id="javaFunction"
                  placeholder={`// Example: Transform source data to target field
public Object transform(Object sourceValue) {
    // Your custom transformation logic here
    return sourceValue.toString().toUpperCase();
}`}
                  value={javaFunction}
                  onChange={(e) => onJavaFunctionChange(e.target.value)}
                  className="mt-2 font-mono text-sm"
                  rows={12}
                />
              </div>
              
              <div className="flex justify-end gap-2">
                <Button 
                  variant="outline" 
                  onClick={onCloseJavaEditor}
                >
                  Cancel
                </Button>
                <Button onClick={onSaveJavaFunction}>
                  <Save className="h-4 w-4 mr-2" />
                  Save Function
                </Button>
              </div>
            </div>
          </div>
        </div>
      )}
    </>
  );
};