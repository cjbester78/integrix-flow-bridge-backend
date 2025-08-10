import React, { useState } from 'react';
import { Label } from '@/components/ui/label';
import { Textarea } from '@/components/ui/textarea';
import { Button } from '@/components/ui/button';
import { FileUploadZone } from '../FileUploadZone';
import { useToast } from '@/hooks/use-toast';
import { FileJson, X } from 'lucide-react';

interface JsonStructureTabProps {
  jsonInput: string;
  setJsonInput: (input: string) => void;
  onResetAllFields: () => void;
}

export const JsonStructureTab: React.FC<JsonStructureTabProps> = ({
  jsonInput,
  setJsonInput,
  onResetAllFields
}) => {
  const [dragOver, setDragOver] = useState(false);
  const { toast } = useToast();

  const handleFileUpload = (file: File) => {
    const reader = new FileReader();
    reader.onload = (e) => {
      const content = e.target?.result as string;
      
      try {
        const parsed = JSON.parse(content);
        setJsonInput(JSON.stringify(parsed, null, 2));
        toast({
          title: "JSON File Loaded",
          description: `Successfully loaded ${file.name}`,
        });
      } catch (error) {
        toast({
          title: "Invalid JSON",
          description: "The uploaded file contains invalid JSON",
          variant: "destructive",
        });
      }
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

  const handleClearJson = () => {
    onResetAllFields();
    toast({
      title: "All Fields Cleared",
      description: "All form fields have been reset",
    });
  };

  return (
    <div className="space-y-4">
      {!jsonInput.trim() && (
        <FileUploadZone
          icon={FileJson}
          title="JSON Upload"
          description="Drag & drop a JSON file or paste JSON structure below"
          acceptTypes=".json"
          dragOver={dragOver}
          onDrop={handleDrop}
          onDragOver={(e) => { e.preventDefault(); setDragOver(true); }}
          onDragLeave={() => setDragOver(false)}
          onFileSelect={handleFileUpload}
          uploadId="json-upload"
          buttonText="Upload JSON File"
        />
      )}
      
      <div className="space-y-2">
        <div className="flex items-center justify-between">
          <Label>JSON Structure</Label>
          {jsonInput.trim() && (
            <Button 
              variant="outline" 
              size="sm" 
              onClick={handleClearJson}
              className="text-destructive hover:text-destructive"
            >
              <X className="h-4 w-4 mr-1" />
              Clear JSON
            </Button>
          )}
        </div>
        <Textarea
          placeholder='{"orderId": "string", "amount": 100.50, "items": []}'
          value={jsonInput}
          onChange={(e) => setJsonInput(e.target.value)}
          className="font-mono text-sm"
          rows={8}
        />
      </div>
    </div>
  );
};