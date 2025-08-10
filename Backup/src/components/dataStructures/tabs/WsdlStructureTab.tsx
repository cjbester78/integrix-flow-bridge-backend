import React, { useState } from 'react';
import { Label } from '@/components/ui/label';
import { Textarea } from '@/components/ui/textarea';
import { Button } from '@/components/ui/button';
import { FileUploadZone } from '../FileUploadZone';
import { NamespaceConfiguration } from '../NamespaceConfiguration';
import { useToast } from '@/hooks/use-toast';
import { extractWsdlPartName, extractWsdlNamespaceInfo } from '@/utils/structureParsers';
import { FileCode, X } from 'lucide-react';

interface WsdlStructureTabProps {
  wsdlInput: string;
  setWsdlInput: (input: string) => void;
  namespaceConfig: any;
  setNamespaceConfig: (config: any) => void;
  onWsdlAnalyzed?: (name: string | null, namespaceInfo: any) => void;
  onResetAllFields: () => void;
}

export const WsdlStructureTab: React.FC<WsdlStructureTabProps> = ({
  wsdlInput,
  setWsdlInput,
  namespaceConfig,
  setNamespaceConfig,
  onWsdlAnalyzed,
  onResetAllFields
}) => {
  const [dragOver, setDragOver] = useState(false);
  const { toast } = useToast();

  const analyzeWsdlContent = (content: string) => {
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
      <NamespaceConfiguration
        type="wsdl"
        namespaceConfig={namespaceConfig}
        setNamespaceConfig={setNamespaceConfig}
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