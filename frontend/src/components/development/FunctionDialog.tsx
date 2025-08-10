// @ts-nocheck - Temporary suppression for unused imports/variables
import React, { useState, useEffect } from 'react';
import {
  Dialog,
  DialogContent,
  DialogDescription,
  DialogHeader,
  DialogTitle,
  DialogFooter,
} from '@/components/ui/dialog';
import { Button } from '@/components/ui/button';
import { Input } from '@/components/ui/input';
import { Label } from '@/components/ui/label';
import { Textarea } from '@/components/ui/textarea';
import {
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from '@/components/ui/select';
import { FunctionEditor } from './FunctionEditor';
import { Badge } from '@/components/ui/badge';
import { Tabs, TabsContent, TabsList, TabsTrigger } from '@/components/ui/tabs';
import { ScrollArea } from '@/components/ui/scroll-area';
import { Alert, AlertDescription } from '@/components/ui/alert';
import { Loader2, AlertCircle, Info } from 'lucide-react';
import { parseFunctionSignature, inferParameterTypes, generateSignature, FunctionParameter } from '@/utils/functionSignatureParser';
import { ParameterEditor } from './ParameterEditor';
import { FunctionTestPanel } from './FunctionTestPanel';

interface FunctionDialogProps {
  open: boolean;
  onOpenChange: (open: boolean) => void;
  function?: any;
  mode: 'view' | 'edit' | 'create';
  onSave?: (data: any) => void;
  onCompile?: (code: string) => Promise<any>;
  onTest?: (functionId: string, inputs: any) => Promise<any>;
  isBuiltIn?: boolean;
  developmentMode?: boolean;
}

export function FunctionDialog({
  open,
  onOpenChange,
  function: functionData,
  mode,
  onSave,
  onCompile,
  onTest,
  isBuiltIn = false,
  developmentMode = false
}: FunctionDialogProps) {
  const [formData, setFormData] = useState({
    name: '',
    description: '',
    category: 'general',
    language: 'JAVA' as 'JAVA' | 'JAVASCRIPT' | 'GROOVY' | 'PYTHON',
    functionSignature: '',
    functionBody: '',
    isSafe: true,
    isPublic: true,
    performanceClass: 'NORMAL' as 'FAST' | 'NORMAL' | 'SLOW'
  });
  const [functionParameters, setFunctionParameters] = useState<FunctionParameter[]>([]);

  const [errors, setErrors] = useState<string[]>([]);
  const [warnings, setWarnings] = useState<string[]>([]);
  const [isCompiling, setIsCompiling] = useState(false);
  const [isSaving, setIsSaving] = useState(false);
  const [hasCompiled, setHasCompiled] = useState(false);
  const [parsedParameters, setParsedParameters] = useState<FunctionParameter[]>([]);

  useEffect(() => {
    if (functionData) {
      setFormData({
        name: functionData.name || '',
        description: functionData.description || '',
        category: functionData.category || 'general',
        language: functionData.language || 'JAVA',
        functionSignature: functionData.functionSignature || '',
        functionBody: functionData.functionBody || '',
        isSafe: functionData.isSafe ?? true,
        isPublic: functionData.isPublic ?? true,
        performanceClass: functionData.performanceClass || 'NORMAL'
      });
      setHasCompiled(false);
      setErrors([]);
      setWarnings([]);
      
      // Parse parameters from database if available
      if (functionData.parameters) {
        try {
          const params = typeof functionData.parameters === 'string' 
            ? JSON.parse(functionData.parameters) 
            : functionData.parameters;
          setFunctionParameters(params);
          setParsedParameters(params);
        } catch (e) {
          console.error('Failed to parse parameters:', e);
          // Fallback to parsing from signature
          if (functionData.functionSignature) {
            const parsed = parseFunctionSignature(functionData.functionSignature);
            if (parsed) {
              const parametersWithTypes = inferParameterTypes(functionData.functionBody || '', parsed.parameters);
              setFunctionParameters(parametersWithTypes);
              setParsedParameters(parametersWithTypes);
            }
          }
        }
      } else if (functionData.functionSignature) {
        // Parse from signature if no parameters in DB
        const parsed = parseFunctionSignature(functionData.functionSignature);
        if (parsed) {
          const parametersWithTypes = inferParameterTypes(functionData.functionBody || '', parsed.parameters);
          setFunctionParameters(parametersWithTypes);
          setParsedParameters(parametersWithTypes);
        }
      }
    }
  }, [functionData]);

  const handleCompile = async () => {
    if (!onCompile || formData.language !== 'JAVA') return;
    
    setIsCompiling(true);
    setErrors([]);
    setWarnings([]);
    
    try {
      const result = await onCompile(formData.functionBody);
      if (result.success) {
        setHasCompiled(true);
        setWarnings(result.warnings || []);
      } else {
        setErrors(result.errors || ['Compilation failed']);
      }
    } catch (error) {
      setErrors(['Failed to compile: ' + error]);
    } finally {
      setIsCompiling(false);
    }
  };

  const handleSave = async () => {
    if (!onSave) return;
    
    // For Java functions, require compilation before saving
    if (formData.language === 'JAVA' && !hasCompiled && mode !== 'view') {
      setErrors(['Please compile the Java code before saving']);
      return;
    }
    
    setIsSaving(true);
    try {
      // Update signature based on parameters
      const updatedSignature = generateFunctionSignature(formData.name, functionParameters);
      
      // Include parsed parameters in the save data
      const dataToSave = {
        ...formData,
        functionSignature: updatedSignature,
        parameters: functionParameters // This will be stored in a new column
      };
      await onSave(dataToSave);
      onOpenChange(false);
    } catch (error) {
      console.error('Failed to save:', error);
    } finally {
      setIsSaving(false);
    }
  };

  const canEdit = mode === 'edit' || mode === 'create';
  const showSaveButton = canEdit && developmentMode;
  
  const handleParametersChange = (newParameters: FunctionParameter[]) => {
    setFunctionParameters(newParameters);
    setParsedParameters(newParameters);
    setHasCompiled(false); // Require recompilation when parameters change
  };
  
  const generateFunctionSignature = (functionName: string, params: FunctionParameter[]): string => {
    if (params.length === 0) {
      return `${functionName}()`;
    }
    const paramStr = params.map(p => `${p.type} ${p.name}`).join(', ');
    return `${functionName}(${paramStr})`;
  };

  return (
    <Dialog open={open} onOpenChange={onOpenChange}>
      <DialogContent className="max-w-6xl max-h-[90vh] p-0">
        <DialogHeader className="px-6 pt-6">
          <DialogTitle>
            {mode === 'create' ? 'Create New Function' : 
             mode === 'edit' ? 'Edit Function' : 'View Function'}
          </DialogTitle>
          <DialogDescription>
            {mode === 'create' ? 'Create a new transformation function' :
             mode === 'edit' ? 'Modify the function implementation' :
             'View function details and implementation'}
          </DialogDescription>
        </DialogHeader>

        <Tabs defaultValue="details" className="flex-1">
          <TabsList className="mx-6">
            <TabsTrigger value="details">Details</TabsTrigger>
            <TabsTrigger value="code">Code</TabsTrigger>
            {functionData && <TabsTrigger value="test">Test</TabsTrigger>}
          </TabsList>

          <ScrollArea className="h-[60vh]">
            <TabsContent value="details" className="px-6 space-y-4">
              <div className="grid gap-4">
                <div className="grid grid-cols-2 gap-4">
                  <div className="space-y-2">
                    <Label htmlFor="name">Function Name</Label>
                    <Input
                      id="name"
                      value={formData.name}
                      onChange={(e) => setFormData({ ...formData, name: e.target.value })}
                      readOnly={!canEdit || (isBuiltIn && mode === 'edit')}
                      placeholder="e.g., calculateTax"
                    />
                  </div>
                  <div className="space-y-2">
                    <Label htmlFor="category">Category</Label>
                    <Select
                      value={formData.category}
                      onValueChange={(value) => setFormData({ ...formData, category: value })}
                      disabled={!canEdit}
                    >
                      <SelectTrigger id="category">
                        <SelectValue />
                      </SelectTrigger>
                      <SelectContent>
                        <SelectItem value="math">Math</SelectItem>
                        <SelectItem value="text">Text</SelectItem>
                        <SelectItem value="date">Date</SelectItem>
                        <SelectItem value="boolean">Boolean</SelectItem>
                        <SelectItem value="conversion">Conversion</SelectItem>
                        <SelectItem value="node">Node</SelectItem>
                        <SelectItem value="constants">Constants</SelectItem>
                        <SelectItem value="general">General</SelectItem>
                      </SelectContent>
                    </Select>
                  </div>
                </div>

                <div className="space-y-2">
                  <Label htmlFor="description">Description</Label>
                  <Textarea
                    id="description"
                    value={formData.description}
                    onChange={(e) => setFormData({ ...formData, description: e.target.value })}
                    readOnly={!canEdit}
                    placeholder="Describe what this function does..."
                    rows={3}
                  />
                </div>

                <div className="space-y-4">
                  <ParameterEditor
                    parameters={functionParameters}
                    onChange={handleParametersChange}
                    readOnly={!canEdit || (isBuiltIn && !developmentMode)}
                  />
                  
                  {canEdit && mode === 'create' && (
                    <Alert>
                      <Info className="h-4 w-4" />
                      <AlertDescription className="text-xs">
                        Define your function parameters above. The signature will be generated automatically.
                      </AlertDescription>
                    </Alert>
                  )}
                </div>

                <div className="grid grid-cols-3 gap-4">
                  <div className="space-y-2">
                    <Label htmlFor="language">Language</Label>
                    <Select
                      value={formData.language}
                      onValueChange={(value: any) => {
                        setFormData({ ...formData, language: value });
                        setHasCompiled(false);
                        setErrors([]);
                        setWarnings([]);
                      }}
                      disabled={!canEdit || (isBuiltIn && mode === 'edit')}
                    >
                      <SelectTrigger id="language">
                        <SelectValue />
                      </SelectTrigger>
                      <SelectContent>
                        <SelectItem value="JAVA">Java</SelectItem>
                        {!isBuiltIn && (
                          <>
                            <SelectItem value="GROOVY">Groovy</SelectItem>
                            <SelectItem value="JAVASCRIPT">JavaScript</SelectItem>
                            <SelectItem value="PYTHON">Python</SelectItem>
                          </>
                        )}
                      </SelectContent>
                    </Select>
                  </div>

                  <div className="space-y-2">
                    <Label htmlFor="performance">Performance Class</Label>
                    <Select
                      value={formData.performanceClass}
                      onValueChange={(value: any) => setFormData({ ...formData, performanceClass: value })}
                      disabled={!canEdit}
                    >
                      <SelectTrigger id="performance">
                        <SelectValue />
                      </SelectTrigger>
                      <SelectContent>
                        <SelectItem value="FAST">Fast</SelectItem>
                        <SelectItem value="NORMAL">Normal</SelectItem>
                        <SelectItem value="SLOW">Slow</SelectItem>
                      </SelectContent>
                    </Select>
                  </div>

                  <div className="space-y-2">
                    <Label>Flags</Label>
                    <div className="flex gap-2 mt-2">
                      {formData.isPublic && <Badge variant="secondary">Public</Badge>}
                      {formData.isSafe && <Badge variant="secondary">Safe</Badge>}
                      {isBuiltIn && <Badge>Built-in</Badge>}
                    </div>
                  </div>
                </div>
              </div>
            </TabsContent>

            <TabsContent value="code" className="h-full">
              <div className="h-[50vh] px-6">
                <FunctionEditor
                  functionName={formData.name}
                  code={formData.functionBody}
                  language={formData.language}
                  onChange={(value) => {
                    setFormData({ ...formData, functionBody: value });
                    setHasCompiled(false);
                  }}
                  onCompile={formData.language === 'JAVA' ? handleCompile : undefined}
                  readOnly={!canEdit}
                  errors={errors}
                  warnings={warnings}
                  isCompiling={isCompiling}
                />
              </div>
            </TabsContent>

            <TabsContent value="test" className="px-6">
              <FunctionTestPanel
                functionId={functionData?.functionId || ''}
                functionName={formData.name}
                parameters={functionParameters}
                onTest={onTest || (async () => ({ success: false, error: 'Test handler not provided' }))}
              />
            </TabsContent>
          </ScrollArea>
        </Tabs>

        <DialogFooter className="px-6 pb-6">
          <Button variant="outline" onClick={() => onOpenChange(false)}>
            Cancel
          </Button>
          {showSaveButton && (
            <Button onClick={handleSave} disabled={isSaving || errors.length > 0}>
              {isSaving ? (
                <>
                  <Loader2 className="h-4 w-4 mr-2 animate-spin" />
                  Saving...
                </>
              ) : (
                'Save Function'
              )}
            </Button>
          )}
        </DialogFooter>
      </DialogContent>
    </Dialog>
  );
}