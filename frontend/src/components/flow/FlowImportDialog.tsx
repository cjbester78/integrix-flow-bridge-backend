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
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from '@/components/ui/select';
import { Alert, AlertDescription, AlertTitle } from '@/components/ui/alert';
import { Tabs, TabsContent, TabsList, TabsTrigger } from '@/components/ui/tabs';
import { 
  Upload, 
  FileJson, 
  AlertCircle, 
  CheckCircle, 
  XCircle,
  Info,
  Loader2,
  Package,
  AlertTriangle
} from 'lucide-react';
import { flowExportImportService } from '@/services/flowExportImportService';
import { useToast } from '@/hooks/use-toast';
import { useNavigate } from 'react-router-dom';
import { 
  ConflictStrategy,
  FlowImportValidationDTO,
  ImportOptions,
  FlowExportDTO,
  FlowImportResultDTO
} from '@/types/export-import';

interface FlowImportDialogProps {
  open: boolean;
  onOpenChange: (open: boolean) => void;
  onImportComplete?: (flowId: string) => void;
}

export const FlowImportDialog: React.FC<FlowImportDialogProps> = ({
  open,
  onOpenChange,
  onImportComplete
}) => {
  const { toast } = useToast();
  const navigate = useNavigate();
  
  const [file, setFile] = useState<File | null>(null);
  const [exportData, setExportData] = useState<FlowExportDTO | null>(null);
  const [validation, setValidation] = useState<FlowImportValidationDTO | null>(null);
  const [importResult, setImportResult] = useState<FlowImportResultDTO | null>(null);
  const [loading, setLoading] = useState(false);
  const [step, setStep] = useState<'upload' | 'validate' | 'import' | 'complete'>('upload');
  
  const [options, setOptions] = useState<ImportOptions>({
    conflictStrategy: 'FAIL',
    importBusinessComponent: true,
    importAdapters: true,
    importCertificateReferences: true,
    validateReferences: true,
    activateAfterImport: false,
    namePrefix: '',
    nameSuffix: ''
  });

  const handleFileChange = async (event: React.ChangeEvent<HTMLInputElement>) => {
    const selectedFile = event.target.files?.[0];
    if (!selectedFile) return;

    if (!selectedFile.name.endsWith('.json')) {
      toast({
        title: 'Invalid File',
        description: 'Please select a JSON file exported from the system.',
        variant: 'destructive'
      });
      return;
    }

    setFile(selectedFile);
    setValidation(null);
    setImportResult(null);
    
    // Read and parse the file
    try {
      const text = await selectedFile.text();
      const data = JSON.parse(text) as FlowExportDTO;
      setExportData(data);
      
      // Automatically validate
      await validateImport(selectedFile);
    } catch (error) {
      toast({
        title: 'Invalid File',
        description: 'Failed to parse the export file. Please ensure it is a valid flow export.',
        variant: 'destructive'
      });
      setFile(null);
      setExportData(null);
    }
  };

  const validateImport = async (fileToValidate?: File) => {
    const targetFile = fileToValidate || file;
    if (!targetFile) return;

    setLoading(true);
    setStep('validate');
    
    try {
      const result = await flowExportImportService.validateImportFromFile(targetFile);
      setValidation(result);
    } catch (error: any) {
      toast({
        title: 'Validation Failed',
        description: error.message || 'Failed to validate import file',
        variant: 'destructive'
      });
      setStep('upload');
    } finally {
      setLoading(false);
    }
  };

  const handleImport = async () => {
    if (!file || !validation?.canImport) return;

    setLoading(true);
    setStep('import');
    
    try {
      const result = await flowExportImportService.importFlowFromFile(file, {
        conflictStrategy: options.conflictStrategy,
        activateAfterImport: options.activateAfterImport,
        namePrefix: options.namePrefix,
        nameSuffix: options.nameSuffix
      });
      
      setImportResult(result);
      setStep('complete');
      
      if (result.success) {
        toast({
          title: 'Import Successful',
          description: `Flow "${result.importedFlowName}" has been imported successfully.`
        });
        
        if (onImportComplete && result.importedFlowId) {
          onImportComplete(result.importedFlowId);
        }
      } else {
        toast({
          title: 'Import Failed',
          description: result.errors[0]?.message || 'Failed to import flow',
          variant: 'destructive'
        });
      }
    } catch (error: any) {
      toast({
        title: 'Import Failed',
        description: error.message || 'Failed to import flow',
        variant: 'destructive'
      });
      setStep('validate');
    } finally {
      setLoading(false);
    }
  };

  const reset = () => {
    setFile(null);
    setExportData(null);
    setValidation(null);
    setImportResult(null);
    setStep('upload');
  };

  const getSeverityIcon = (severity: string) => {
    switch (severity) {
      case 'ERROR':
        return <XCircle className="h-4 w-4 text-destructive" />;
      case 'WARNING':
        return <AlertTriangle className="h-4 w-4 text-warning" />;
      default:
        return <Info className="h-4 w-4 text-info" />;
    }
  };

  return (
    <Dialog open={open} onOpenChange={(open) => {
      if (!open) reset();
      onOpenChange(open);
    }}>
      <DialogContent className="max-w-3xl max-h-[90vh] overflow-y-auto">
        <DialogHeader>
          <DialogTitle>Import Integration Flow</DialogTitle>
          <DialogDescription>
            Import a previously exported integration flow with all its dependencies.
          </DialogDescription>
        </DialogHeader>

        <div className="space-y-6">
          {step === 'upload' && (
            <>
              {/* File Upload */}
              <div className="border-2 border-dashed rounded-lg p-8 text-center">
                <input
                  type="file"
                  accept=".json"
                  onChange={handleFileChange}
                  className="hidden"
                  id="flow-import-file"
                />
                <label
                  htmlFor="flow-import-file"
                  className="cursor-pointer flex flex-col items-center gap-3"
                >
                  <FileJson className="h-12 w-12 text-muted-foreground" />
                  <div>
                    <p className="text-sm font-medium">Click to select a flow export file</p>
                    <p className="text-xs text-muted-foreground">JSON files only</p>
                  </div>
                </label>
              </div>

              {/* Import Options */}
              <div className="space-y-4">
                <h3 className="text-sm font-medium">Import Options</h3>
                
                <div className="grid gap-4">
                  <div>
                    <Label htmlFor="conflict-strategy">Conflict Resolution Strategy</Label>
                    <Select
                      value={options.conflictStrategy}
                      onValueChange={(value) => 
                        setOptions({ ...options, conflictStrategy: value as ConflictStrategy })
                      }
                    >
                      <SelectTrigger id="conflict-strategy">
                        <SelectValue />
                      </SelectTrigger>
                      <SelectContent>
                        <SelectItem value="FAIL">Fail on Conflicts</SelectItem>
                        <SelectItem value="SKIP">Skip Conflicting Objects</SelectItem>
                        <SelectItem value="CREATE_NEW">Create New Objects</SelectItem>
                        <SelectItem value="UPDATE_EXISTING">Update Existing Objects</SelectItem>
                      </SelectContent>
                    </Select>
                  </div>

                  <div className="grid grid-cols-2 gap-4">
                    <div>
                      <Label htmlFor="name-prefix">Name Prefix (Optional)</Label>
                      <Input
                        id="name-prefix"
                        placeholder="e.g., DEV_"
                        value={options.namePrefix || ''}
                        onChange={(e) => setOptions({ ...options, namePrefix: e.target.value })}
                      />
                    </div>
                    <div>
                      <Label htmlFor="name-suffix">Name Suffix (Optional)</Label>
                      <Input
                        id="name-suffix"
                        placeholder="e.g., _COPY"
                        value={options.nameSuffix || ''}
                        onChange={(e) => setOptions({ ...options, nameSuffix: e.target.value })}
                      />
                    </div>
                  </div>

                  <div className="flex items-center justify-between">
                    <Label htmlFor="activate-after-import">Activate Flow After Import</Label>
                    <Switch
                      id="activate-after-import"
                      checked={options.activateAfterImport}
                      onCheckedChange={(checked) => 
                        setOptions({ ...options, activateAfterImport: checked })
                      }
                    />
                  </div>
                </div>
              </div>
            </>
          )}

          {step === 'validate' && validation && (
            <>
              {loading ? (
                <div className="flex items-center justify-center py-8">
                  <Loader2 className="h-8 w-8 animate-spin" />
                  <span className="ml-2">Validating import...</span>
                </div>
              ) : (
                <Tabs defaultValue="preview" className="w-full">
                  <TabsList className="grid w-full grid-cols-3">
                    <TabsTrigger value="preview">Preview</TabsTrigger>
                    <TabsTrigger value="conflicts">
                      Conflicts {validation.conflicts.length > 0 && `(${validation.conflicts.length})`}
                    </TabsTrigger>
                    <TabsTrigger value="issues">
                      Issues {(validation.errors.length + validation.warnings.length) > 0 && 
                        `(${validation.errors.length + validation.warnings.length})`}
                    </TabsTrigger>
                  </TabsList>

                  <TabsContent value="preview" className="space-y-4">
                    <div className="rounded-lg border p-4 space-y-3">
                      <h4 className="font-medium">Flow Information</h4>
                      <div className="grid grid-cols-2 gap-4 text-sm">
                        <div>
                          <span className="text-muted-foreground">Flow Name:</span>
                          <p className="font-medium">{validation.preview.flowName}</p>
                        </div>
                        <div>
                          <span className="text-muted-foreground">Business Component:</span>
                          <p className="font-medium">{validation.preview.businessComponentName || 'None'}</p>
                        </div>
                        <div>
                          <span className="text-muted-foreground">Source Adapter:</span>
                          <p className="font-medium">{validation.preview.sourceAdapterName || 'None'}</p>
                        </div>
                        <div>
                          <span className="text-muted-foreground">Target Adapter:</span>
                          <p className="font-medium">{validation.preview.targetAdapterName || 'None'}</p>
                        </div>
                      </div>
                    </div>

                    <div className="rounded-lg border p-4 space-y-3">
                      <h4 className="font-medium">Objects to Import</h4>
                      <div className="grid grid-cols-3 gap-4 text-sm">
                        <div className="flex items-center gap-2">
                          <Package className="h-4 w-4 text-muted-foreground" />
                          <span>Transformations: {validation.preview.transformationCount}</span>
                        </div>
                        <div className="flex items-center gap-2">
                          <Package className="h-4 w-4 text-muted-foreground" />
                          <span>Field Mappings: {validation.preview.fieldMappingCount}</span>
                        </div>
                        <div className="flex items-center gap-2">
                          <Package className="h-4 w-4 text-muted-foreground" />
                          <span>Certificates: {validation.preview.certificateReferenceCount}</span>
                        </div>
                      </div>
                    </div>

                    {!validation.versionCompatibility.isCompatible && (
                      <Alert variant="destructive">
                        <AlertCircle className="h-4 w-4" />
                        <AlertTitle>Version Incompatibility</AlertTitle>
                        <AlertDescription>
                          Export version ({validation.versionCompatibility.exportVersion}) is not compatible 
                          with current version ({validation.versionCompatibility.currentVersion}).
                        </AlertDescription>
                      </Alert>
                    )}
                  </TabsContent>

                  <TabsContent value="conflicts" className="space-y-3">
                    {validation.conflicts.length === 0 ? (
                      <Alert>
                        <CheckCircle className="h-4 w-4 text-success" />
                        <AlertDescription>No conflicts detected.</AlertDescription>
                      </Alert>
                    ) : (
                      validation.conflicts.map((conflict, index) => (
                        <Alert key={index} variant="destructive">
                          <AlertCircle className="h-4 w-4" />
                          <AlertTitle>{conflict.objectType} Conflict</AlertTitle>
                          <AlertDescription>
                            {conflict.type === 'NAME_EXISTS' && 
                              `An object named "${conflict.importName}" already exists.`}
                            {conflict.type === 'REFERENCE_MISSING' && 
                              `Referenced ${conflict.objectType} "${conflict.importName}" not found.`}
                          </AlertDescription>
                        </Alert>
                      ))
                    )}
                  </TabsContent>

                  <TabsContent value="issues" className="space-y-3">
                    {validation.errors.length === 0 && validation.warnings.length === 0 ? (
                      <Alert>
                        <CheckCircle className="h-4 w-4 text-success" />
                        <AlertDescription>No issues found.</AlertDescription>
                      </Alert>
                    ) : (
                      <>
                        {validation.errors.map((error, index) => (
                          <Alert key={`error-${index}`} variant="destructive">
                            <div className="flex items-start gap-2">
                              {getSeverityIcon(error.severity)}
                              <div>
                                <AlertTitle>{error.code}</AlertTitle>
                                <AlertDescription>{error.message}</AlertDescription>
                              </div>
                            </div>
                          </Alert>
                        ))}
                        {validation.warnings.map((warning, index) => (
                          <Alert key={`warning-${index}`}>
                            <div className="flex items-start gap-2">
                              {getSeverityIcon(warning.severity)}
                              <div>
                                <AlertTitle>{warning.code}</AlertTitle>
                                <AlertDescription>{warning.message}</AlertDescription>
                              </div>
                            </div>
                          </Alert>
                        ))}
                      </>
                    )}
                  </TabsContent>
                </Tabs>
              )}
            </>
          )}

          {step === 'import' && (
            <div className="flex flex-col items-center justify-center py-8 space-y-4">
              <Loader2 className="h-12 w-12 animate-spin" />
              <p className="text-lg font-medium">Importing flow...</p>
              <p className="text-sm text-muted-foreground">This may take a few moments.</p>
            </div>
          )}

          {step === 'complete' && importResult && (
            <div className="space-y-4">
              {importResult.success ? (
                <>
                  <Alert>
                    <CheckCircle className="h-4 w-4 text-success" />
                    <AlertTitle>Import Successful!</AlertTitle>
                    <AlertDescription>
                      Flow "{importResult.importedFlowName}" has been imported successfully.
                    </AlertDescription>
                  </Alert>

                  <div className="rounded-lg border p-4 space-y-3">
                    <h4 className="font-medium">Import Summary</h4>
                    <div className="grid grid-cols-2 gap-3 text-sm">
                      <div>Flow Imported: {importResult.summary.flowImported ? '✓' : '✗'}</div>
                      <div>Business Component: {importResult.summary.businessComponentImported ? '✓' : '✗'}</div>
                      <div>Adapters: {importResult.summary.adaptersImported}</div>
                      <div>Transformations: {importResult.summary.transformationsImported}</div>
                      <div>Field Mappings: {importResult.summary.fieldMappingsImported}</div>
                      <div>Total Objects: {importResult.summary.totalObjectsImported}</div>
                    </div>
                    <p className="text-xs text-muted-foreground">
                      Import completed in {importResult.summary.importDurationMs}ms
                    </p>
                  </div>

                  {importResult.warnings.length > 0 && (
                    <Alert>
                      <AlertTriangle className="h-4 w-4" />
                      <AlertTitle>Import completed with warnings</AlertTitle>
                      <AlertDescription>
                        <ul className="list-disc list-inside mt-2">
                          {importResult.warnings.map((warning, index) => (
                            <li key={index}>{warning.message}</li>
                          ))}
                        </ul>
                      </AlertDescription>
                    </Alert>
                  )}
                </>
              ) : (
                <Alert variant="destructive">
                  <XCircle className="h-4 w-4" />
                  <AlertTitle>Import Failed</AlertTitle>
                  <AlertDescription>
                    {importResult.errors.map((error, index) => (
                      <div key={index}>{error.message}</div>
                    ))}
                  </AlertDescription>
                </Alert>
              )}
            </div>
          )}
        </div>

        <DialogFooter>
          {step === 'upload' && (
            <>
              <Button variant="outline" onClick={() => onOpenChange(false)}>
                Cancel
              </Button>
            </>
          )}

          {step === 'validate' && (
            <>
              <Button variant="outline" onClick={reset}>
                Back
              </Button>
              <Button 
                onClick={handleImport} 
                disabled={!validation?.canImport || loading}
              >
                <Upload className="mr-2 h-4 w-4" />
                Import Flow
              </Button>
            </>
          )}

          {step === 'complete' && importResult?.success && (
            <>
              <Button variant="outline" onClick={() => onOpenChange(false)}>
                Close
              </Button>
              <Button 
                onClick={() => {
                  if (importResult.importedFlowId) {
                    navigate(`/flows/${importResult.importedFlowId}`);
                    onOpenChange(false);
                  }
                }}
              >
                View Flow
              </Button>
            </>
          )}

          {step === 'complete' && !importResult?.success && (
            <>
              <Button variant="outline" onClick={reset}>
                Try Again
              </Button>
              <Button onClick={() => onOpenChange(false)}>
                Close
              </Button>
            </>
          )}
        </DialogFooter>
      </DialogContent>
    </Dialog>
  );
};