import React, { useState } from 'react';
import { Label } from '@/components/ui/label';
import { Textarea } from '@/components/ui/textarea';
import { Button } from '@/components/ui/button';
import { FileUploadZone } from '../FileUploadZone';
import { NamespaceConfiguration } from '../NamespaceConfiguration';
import { useToast } from '@/hooks/use-toast';
import { FileCode, X } from 'lucide-react';

interface EdmxStructureTabProps {
  edmxInput: string;
  setEdmxInput: (input: string) => void;
  namespaceConfig: any;
  setNamespaceConfig: (config: any) => void;
  onResetAllFields: () => void;
}

export const EdmxStructureTab: React.FC<EdmxStructureTabProps> = ({
  edmxInput,
  setEdmxInput,
  namespaceConfig,
  setNamespaceConfig,
  onResetAllFields
}) => {
  const [dragOver, setDragOver] = useState(false);
  const { toast } = useToast();

  const handleDrop = (e: React.DragEvent) => {
    e.preventDefault();
    setDragOver(false);
    
    const files = e.dataTransfer.files;
    if (files[0]) {
      handleFileSelect(files[0]);
    }
  };

  const handleDragOver = (e: React.DragEvent) => {
    e.preventDefault();
    setDragOver(true);
  };

  const handleDragLeave = () => {
    setDragOver(false);
  };

  const handleFileSelect = (file: File) => {
    const reader = new FileReader();
    reader.onload = (e) => {
      const content = e.target?.result as string;
      setEdmxInput(content);
      toast({
        title: "EDMX File Loaded",
        description: `Successfully loaded ${file.name}`,
      });
    };
    reader.readAsText(file);
  };

  const handleClearEdmx = () => {
    onResetAllFields();
    toast({
      title: "All Fields Cleared",
      description: "All form fields have been reset",
    });
  };

  return (
    <div className="space-y-4">
      {!edmxInput.trim() && (
        <FileUploadZone
          icon={FileCode}
          title="Upload EDMX File"
          description="Drag and drop your EDMX file here, or click to browse"
          acceptTypes=".edmx,.xml"
          dragOver={dragOver}
          onDrop={handleDrop}
          onDragOver={handleDragOver}
          onDragLeave={handleDragLeave}
          onFileSelect={handleFileSelect}
          uploadId="edmx-upload"
          buttonText="Upload EDMX File"
        />
      )}
      
      <div className="space-y-2">
        <div className="flex items-center justify-between">
          <Label htmlFor="edmxInput">EDMX Structure</Label>
          {edmxInput.trim() && (
            <Button 
              variant="outline" 
              size="sm" 
              onClick={handleClearEdmx}
              className="text-destructive hover:text-destructive"
            >
              <X className="h-4 w-4 mr-1" />
              Clear EDMX
            </Button>
          )}
        </div>
        <Textarea
          id="edmxInput"
          placeholder="Paste your EDMX content here or upload a file above..."
          value={edmxInput}
          onChange={(e) => setEdmxInput(e.target.value)}
          className="min-h-[300px] font-mono text-sm"
          rows={15}
        />
      </div>

      <NamespaceConfiguration
        type="xml"
        namespaceConfig={namespaceConfig}
        setNamespaceConfig={setNamespaceConfig}
      />
    </div>
  );
};