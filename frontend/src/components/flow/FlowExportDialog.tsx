// @ts-nocheck
import React, { useState } from 'react';
import { 
  Dialog, 
  DialogContent, 
  DialogDescription, 
  DialogFooter, 
  DialogHeader, 
  DialogTitle 
} from '@/components/ui/dialog';
import { Button } from '@/components/ui/button';
import { Label } from '@/components/ui/label';
import { Switch } from '@/components/ui/switch';
import { Input } from '@/components/ui/input';
import { Textarea } from '@/components/ui/textarea';
import { Alert, AlertDescription } from '@/components/ui/alert';
import { Checkbox } from '@/components/ui/checkbox';
import { Download, Info, Loader2 } from 'lucide-react';
import { downloadFlowExport, flowExportImportService } from '@/services/flowExportImportService';
import { useToast } from '@/hooks/use-toast';
import { ExportOptions } from '@/types/export-import';

interface FlowExportDialogProps {
  flowId: string;
  flowName: string;
  open: boolean;
  onOpenChange: (open: boolean) => void;
}

export const FlowExportDialog: React.FC<FlowExportDialogProps> = ({
  flowId,
  flowName,
  open,
  onOpenChange
}) => {
  const { toast } = useToast();
  const [loading, setLoading] = useState(false);
  const [validating, setValidating] = useState(false);
  const [validation, setValidation] = useState<any>(null);
  
  const [options, setOptions] = useState<ExportOptions>({
    includeBusinessComponent: true,
    includeAdapterConfigs: true,
    includeCertificateReferences: true,
    includeSensitiveData: false,
    includeStatistics: false,
    includeAuditInfo: false,
    format: 'JSON',
    compress: false,
    environment: '',
    description: '',
    tags: []
  });

  const [tagInput, setTagInput] = useState('');

  React.useEffect(() => {
    if (open && flowId) {
      validateExport();
    }
  }, [open, flowId]);

  const validateExport = async () => {
    setValidating(true);
    try {
      const result = await flowExportImportService.validateExport(flowId);
      setValidation(result);
    } catch (error: any) {
      toast({
        title: 'Validation Failed',
        description: error.message || 'Failed to validate export',
        variant: 'destructive'
      });
    } finally {
      setValidating(false);
    }
  };

  const handleExport = async () => {
    if (!validation?.canExport) {
      toast({
        title: 'Cannot Export',
        description: validation?.reason || 'Flow cannot be exported',
        variant: 'destructive'
      });
      return;
    }

    setLoading(true);
    try {
      await downloadFlowExport(flowId, flowName, options);
      
      toast({
        title: 'Export Successful',
        description: `Flow "${flowName}" has been exported successfully.`
      });
      
      onOpenChange(false);
    } catch (error: any) {
      toast({
        title: 'Export Failed',
        description: error.message || 'Failed to export flow',
        variant: 'destructive'
      });
    } finally {
      setLoading(false);
    }
  };

  const addTag = () => {
    if (tagInput.trim()) {
      setOptions({
        ...options,
        tags: [...(options.tags || []), tagInput.trim()]
      });
      setTagInput('');
    }
  };

  const removeTag = (index: number) => {
    setOptions({
      ...options,
      tags: options.tags?.filter((_, i) => i !== index)
    });
  };

  return (
    <Dialog open={open} onOpenChange={onOpenChange}>
      <DialogContent className="max-w-2xl max-h-[90vh] overflow-y-auto">
        <DialogHeader>
          <DialogTitle>Export Integration Flow</DialogTitle>
          <DialogDescription>
            Export "{flowName}" with all its dependencies and configurations.
          </DialogDescription>
        </DialogHeader>

        {validating ? (
          <div className="flex items-center justify-center py-8">
            <Loader2 className="h-8 w-8 animate-spin" />
          </div>
        ) : validation && !validation.canExport ? (
          <Alert variant="destructive">
            <AlertDescription>
              <strong>Cannot export this flow:</strong> {validation.reason}
            </AlertDescription>
          </Alert>
        ) : (
          <div className="space-y-6">
            {/* Export Options */}
            <div className="space-y-4">
              <h3 className="text-sm font-medium">Export Options</h3>
              
              <div className="space-y-3">
                <div className="flex items-center justify-between">
                  <Label htmlFor="include-business-component">Include Business Component</Label>
                  <Switch
                    id="include-business-component"
                    checked={options.includeBusinessComponent}
                    onCheckedChange={(checked) => 
                      setOptions({ ...options, includeBusinessComponent: checked })
                    }
                  />
                </div>

                <div className="flex items-center justify-between">
                  <Label htmlFor="include-adapter-configs">Include Adapter Configurations</Label>
                  <Switch
                    id="include-adapter-configs"
                    checked={options.includeAdapterConfigs}
                    onCheckedChange={(checked) => 
                      setOptions({ ...options, includeAdapterConfigs: checked })
                    }
                  />
                </div>

                <div className="flex items-center justify-between">
                  <Label htmlFor="include-certificate-refs">Include Certificate References</Label>
                  <Switch
                    id="include-certificate-refs"
                    checked={options.includeCertificateReferences}
                    onCheckedChange={(checked) => 
                      setOptions({ ...options, includeCertificateReferences: checked })
                    }
                  />
                </div>

                <div className="flex items-center justify-between">
                  <Label htmlFor="include-sensitive">Include Sensitive Data (Encrypted)</Label>
                  <Switch
                    id="include-sensitive"
                    checked={options.includeSensitiveData}
                    onCheckedChange={(checked) => 
                      setOptions({ ...options, includeSensitiveData: checked })
                    }
                  />
                </div>

                <div className="flex items-center justify-between">
                  <Label htmlFor="include-statistics">Include Execution Statistics</Label>
                  <Switch
                    id="include-statistics"
                    checked={options.includeStatistics}
                    onCheckedChange={(checked) => 
                      setOptions({ ...options, includeStatistics: checked })
                    }
                  />
                </div>

                <div className="flex items-center justify-between">
                  <Label htmlFor="include-audit">Include Audit Information</Label>
                  <Switch
                    id="include-audit"
                    checked={options.includeAuditInfo}
                    onCheckedChange={(checked) => 
                      setOptions({ ...options, includeAuditInfo: checked })
                    }
                  />
                </div>
              </div>
            </div>

            {/* Metadata */}
            <div className="space-y-4">
              <h3 className="text-sm font-medium">Export Metadata</h3>
              
              <div className="space-y-3">
                <div>
                  <Label htmlFor="environment">Environment</Label>
                  <Input
                    id="environment"
                    placeholder="e.g., development, staging, production"
                    value={options.environment || ''}
                    onChange={(e) => setOptions({ ...options, environment: e.target.value })}
                  />
                </div>

                <div>
                  <Label htmlFor="description">Description</Label>
                  <Textarea
                    id="description"
                    placeholder="Add a description for this export..."
                    value={options.description || ''}
                    onChange={(e) => setOptions({ ...options, description: e.target.value })}
                    rows={3}
                  />
                </div>

                <div>
                  <Label htmlFor="tags">Tags</Label>
                  <div className="flex gap-2">
                    <Input
                      id="tags"
                      placeholder="Add tags..."
                      value={tagInput}
                      onChange={(e) => setTagInput(e.target.value)}
                      onKeyPress={(e) => e.key === 'Enter' && (e.preventDefault(), addTag())}
                    />
                    <Button type="button" variant="outline" onClick={addTag}>
                      Add
                    </Button>
                  </div>
                  {options.tags && options.tags.length > 0 && (
                    <div className="flex flex-wrap gap-2 mt-2">
                      {options.tags.map((tag, index) => (
                        <span
                          key={index}
                          className="inline-flex items-center gap-1 px-2 py-1 text-sm bg-secondary rounded-md"
                        >
                          {tag}
                          <button
                            type="button"
                            onClick={() => removeTag(index)}
                            className="text-muted-foreground hover:text-foreground"
                          >
                            ×
                          </button>
                        </span>
                      ))}
                    </div>
                  )}
                </div>
              </div>
            </div>

            {/* Info Alert */}
            <Alert>
              <Info className="h-4 w-4" />
              <AlertDescription>
                The exported file will contain all selected components and can be imported into 
                another environment. Sensitive data like passwords will be encrypted if included.
              </AlertDescription>
            </Alert>
          </div>
        )}

        <DialogFooter>
          <Button variant="outline" onClick={() => onOpenChange(false)}>
            Cancel
          </Button>
          <Button 
            onClick={handleExport} 
            disabled={loading || !validation?.canExport}
          >
            {loading ? (
              <>
                <Loader2 className="mr-2 h-4 w-4 animate-spin" />
                Exporting...
              </>
            ) : (
              <>
                <Download className="mr-2 h-4 w-4" />
                Export Flow
              </>
            )}
          </Button>
        </DialogFooter>
      </DialogContent>
    </Dialog>
  );
};