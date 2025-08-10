// @ts-nocheck
import React, { useState } from 'react';
import { Label } from '@/components/ui/label';
import { Textarea } from '@/components/ui/textarea';
import { Button } from '@/components/ui/button';
import { FileUploadZone } from '../FileUploadZone';
import { NamespaceConfiguration } from '../NamespaceConfiguration';
import { useToast } from '@/hooks/use-toast';
import { extractWsdlPartName, extractWsdlNamespaceInfo, extractWsdlOperations } from '@/utils/structureParsers';
import { FileCode, X, AlertCircle } from 'lucide-react';
import { Alert, AlertDescription, AlertTitle } from '@/components/ui/alert';

interface WsdlStructureTabProps {
  wsdlInput: string;
  setWsdlInput: (input: string) => void;
  namespaceConfig: any;
  setNamespaceConfig: (config: any) => void;
  onWsdlAnalyzed?: (name: string | null, namespaceInfo: any) => void;
  onResetAllFields: () => void;
  onFileUploaded?: (fileName: string) => void;
}

export const WsdlStructureTab: React.FC<WsdlStructureTabProps> = ({
  wsdlInput,
  setWsdlInput,
  namespaceConfig,
  setNamespaceConfig,
  onWsdlAnalyzed,
  onResetAllFields,
  onFileUploaded
}) => {
  const [dragOver, setDragOver] = useState(false);
  const [multipleOperationsWarning, setMultipleOperationsWarning] = useState<string[] | null>(null);
  const { toast } = useToast();

  const analyzeWsdlContent = (content: string) => {
    // Check for multiple operations
    const { names: operationNames, hasMultiple } = extractWsdlOperations(content);
    
    if (hasMultiple) {
      setMultipleOperationsWarning(operationNames);
      toast({
        title: "Multiple Operations Detected",
        description: `This WSDL contains ${operationNames.length} operations. Please enter a structure name manually.`,
        variant: "default",
      });
    } else {
      setMultipleOperationsWarning(null);
    }
    
    const extractedName = extractWsdlPartName(content);
    const namespaceInfo = extractWsdlNamespaceInfo(content);
    
    if (namespaceInfo) {
      setNamespaceConfig(namespaceInfo);
    }
    
    if (onWsdlAnalyzed) {
      onWsdlAnalyzed(extractedName, namespaceInfo);
    }
  };

  const handleFileUpload = (file: File) => {
    const reader = new FileReader();
    reader.onload = (e) => {
      const content = e.target?.result as string;
      setWsdlInput(content);
      analyzeWsdlContent(content);
      
      // Don't call onFileUploaded if we're handling WSDL - let analyzeWsdlContent handle the naming
      // Only call it for non-WSDL files that need filename-based naming
      
      toast({
        title: "WSDL File Loaded",
        description: `Successfully loaded ${file.name}`,
      });
    };
    reader.readAsText(file);
  };

  const handleDrop = (e: React.DragEvent) => {
    e.preventDefault();
    setDragOver(false);
    
    const files = Array.from(e.dataTransfer.files);
    if (files.length > 0) {
      handleFileUpload(files[0]);
    }
  };

  const handleCancelWsdl = () => {
    onResetAllFields();
    toast({
      title: "All Fields Cleared",
      description: "All form fields have been reset",
    });
  };

  return (
    <div className="space-y-4">
      {multipleOperationsWarning && (
        <Alert>
          <AlertCircle className="h-4 w-4" />
          <AlertTitle>Multiple Operations Found</AlertTitle>
          <AlertDescription>
            This WSDL contains {multipleOperationsWarning.length} operations: {multipleOperationsWarning.join(', ')}.
            Please enter a structure name manually in the form above.
          </AlertDescription>
        </Alert>
      )}
      
      <NamespaceConfiguration
        type="wsdl"
        namespaceConfig={namespaceConfig}
        setNamespaceConfig={setNamespaceConfig}
        hideSchemaLocation={true}
      />
      {!wsdlInput.trim() && (
        <FileUploadZone
          icon={FileCode}
          title="WSDL Upload"
          description="Drag & drop a WSDL file or paste WSDL content below"
          acceptTypes=".wsdl,.xml"
          dragOver={dragOver}
          onDrop={handleDrop}
          onDragOver={(e) => { e.preventDefault(); setDragOver(true); }}
          onDragLeave={() => setDragOver(false)}
          onFileSelect={handleFileUpload}
          uploadId="wsdl-upload"
          buttonText="Upload WSDL File"
        />
      )}
      
      <div className="space-y-2">
        <div className="flex items-center justify-between">
          <Label>WSDL Content</Label>
          {wsdlInput.trim() && (
            <Button 
              variant="outline" 
              size="sm" 
              onClick={handleCancelWsdl}
              className="text-destructive hover:text-destructive"
            >
              <X className="h-4 w-4 mr-1" />
              Clear WSDL
            </Button>
          )}
        </div>
        <Textarea
          placeholder="Paste your WSDL definition here..."
          value={wsdlInput}
          onChange={(e) => {
            const content = e.target.value;
            setWsdlInput(content);
            if (content.trim()) {
              analyzeWsdlContent(content);
            }
          }}
          className="font-mono text-sm"
          rows={8}
        />
      </div>
    </div>
  );
};