import React from 'react';
import { Button } from '@/components/ui/button';
import { Upload } from 'lucide-react';

interface FileUploadZoneProps {
  icon: React.ComponentType<{ className?: string }>;
  title: string;
  description: string;
  acceptTypes: string;
  dragOver: boolean;
  onDrop: (e: React.DragEvent) => void;
  onDragOver: (e: React.DragEvent) => void;
  onDragLeave: () => void;
  onFileSelect: (file: File) => void;
  uploadId: string;
  buttonText: string;
}

export const FileUploadZone: React.FC<FileUploadZoneProps> = ({
  icon: Icon,
  title,
  description,
  acceptTypes,
  dragOver,
  onDrop,
  onDragOver,
  onDragLeave,
  onFileSelect,
  uploadId,
  buttonText
}) => {
  return (
    <div
      className={`border-2 border-dashed rounded-lg p-6 text-center transition-colors ${
        dragOver ? 'border-primary bg-primary/10' : 'border-border'
      }`}
      onDrop={onDrop}
      onDragOver={onDragOver}
      onDragLeave={onDragLeave}
    >
      <Icon className="h-12 w-12 mx-auto mb-4 text-muted-foreground" />
      <p className="text-sm text-muted-foreground mb-4">{description}</p>
      <input
        type="file"
        accept={acceptTypes}
        onChange={(e) => e.target.files?.[0] && onFileSelect(e.target.files[0])}
        className="hidden"
        id={uploadId}
      />
      <Button variant="outline" onClick={() => document.getElementById(uploadId)?.click()}>
        <Upload className="h-4 w-4 mr-2" />
        {buttonText}
      </Button>
    </div>
  );
};