import React from 'react';
import { useNavigate } from 'react-router-dom';
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card';
import { Alert, AlertDescription } from '@/components/ui/alert';
import { Button } from '@/components/ui/button';
import { Info, Construction, Save, X } from 'lucide-react';
import { VisualOrchestrationEditor } from '@/components/orchestration/VisualOrchestrationEditor';
import { useToast } from '@/hooks/use-toast';

export function CreateOrchestrationFlow() {
  const navigate = useNavigate();
  const { toast } = useToast();

  const handleSave = () => {
    // TODO: Implement flow saving logic
    toast({
      title: "Flow Saved",
      description: "Your orchestration flow has been saved successfully.",
    });
  };

  const handleCancel = () => {
    navigate('/create-flow');
  };

  return (
    <div className="h-screen bg-background overflow-hidden">
      {/* Compact header */}
      <div className="h-20 border-b border-border px-6 flex items-center justify-between">
        <div>
          <h1 className="text-2xl font-bold tracking-tight">Create Orchestration Flow</h1>
          <p className="text-sm text-muted-foreground">
            Design complex workflows with multiple systems, routing, and business logic
          </p>
        </div>
        
        <div className="flex items-center gap-4">
          {/* Action buttons */}
          <div className="flex gap-2">
            <Button
              onClick={handleSave}
              className="flex items-center gap-2"
            >
              <Save className="h-4 w-4" />
              Save Flow
            </Button>
            <Button
              variant="outline"
              onClick={handleCancel}
              className="flex items-center gap-2"
            >
              <X className="h-4 w-4" />
              Cancel
            </Button>
          </div>

          <Alert className="max-w-md">
            <Construction className="h-4 w-4" />
            <AlertDescription className="text-xs">
              The Visual Orchestration Flow Editor is currently under development. This includes
              drag-and-drop workflow design, message routing, and error handling capabilities.
            </AlertDescription>
          </Alert>
        </div>
      </div>

      {/* Full height flow designer */}
      <div className="h-[calc(100vh-80px)]">
        <VisualOrchestrationEditor />
      </div>
    </div>
  );
}