// @ts-nocheck
import React, { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { useAuth } from '@/contexts/AuthContext';
import { useNavigationHistory } from '@/hooks/useNavigationHistory';
import { usePageReset } from '@/hooks/usePageReset';
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '@/components/ui/card';
import { Alert, AlertDescription } from '@/components/ui/alert';
import { Button } from '@/components/ui/button';
import { Input } from '@/components/ui/input';
import { Label } from '@/components/ui/label';
import { Textarea } from '@/components/ui/textarea';
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from '@/components/ui/select';
import { ArrowLeft, Construction, Save, X, Settings as SettingsIcon, Loader2, Play } from 'lucide-react';
import { VisualOrchestrationEditor } from '@/components/orchestration/VisualOrchestrationEditor';
import { businessComponentService } from '@/services/businessComponentService';
import { api } from '@/services/api';
import { useToast } from '@/hooks/use-toast';

export function CreateOrchestrationFlow() {
  const navigate = useNavigate();
  const { navigateBack } = useNavigationHistory();
  const { toast } = useToast();
  const { user } = useAuth();
  const [showVisualEditor, setShowVisualEditor] = useState(true);
  
  // Flow state
  const [flowName, setFlowName] = useState('');
  const [uniqueFlowId, setUniqueFlowId] = useState('');
  const [description, setDescription] = useState('');
  const [sourceBusinessComponent, setSourceBusinessComponent] = useState('');
  const [targetBusinessComponent, setTargetBusinessComponent] = useState('');
  const [sourceAdapter, setSourceAdapter] = useState('');
  const [targetAdapter, setTargetAdapter] = useState('');
  const [orchestrationSteps, setOrchestrationSteps] = useState([]);
  
  // Data state
  const [businessComponents, setBusinessComponents] = useState([]);
  const [adapters, setAdapters] = useState([]);
  const [loading, setLoading] = useState(true);
  const [saving, setSaving] = useState(false);
  const [testing, setTesting] = useState(false);
  
  // Reset form when navigating directly from menu
  const resetForm = () => {
    setFlowName('');
    setUniqueFlowId('');
    setDescription('');
    setSourceBusinessComponent('');
    setTargetBusinessComponent('');
    setSourceAdapter('');
    setTargetAdapter('');
    setShowVisualEditor(true);
  };
  
  usePageReset(resetForm);
  
  // Load data on component mount
  useEffect(() => {
    loadComponentData();
  }, []);
  
  const loadComponentData = async () => {
    try {
      setLoading(true);
      
      // Load business components
      const businessResult = await businessComponentService.getAllBusinessComponents();
      if (businessResult.success && businessResult.data) {
        setBusinessComponents(businessResult.data);
      }
      
      // Load adapters
      try {
        const adaptersResponse = await api.get('/adapters');
        if (adaptersResponse.data) {
          setAdapters(adaptersResponse.data);
        }
      } catch (error) {
        console.warn('Could not load adapters:', error);
        setAdapters([]);
      }
      
    } catch (error) {
      console.error('Error loading component data:', error);
      toast({
        title: "Error",
        description: "Failed to load component data. Some features may not work properly.",
        variant: "destructive",
      });
    } finally {
      setLoading(false);
    }
  };

  const handleSave = async () => {
    if (!uniqueFlowId.trim()) {
      toast({
        title: "Validation Error",
        description: "Unique Flow ID is required.",
        variant: "destructive",
      });
      return;
    }

    try {
      setSaving(true);
      
      // Prepare the orchestration flow request
      const flowRequest = {
        flowId: uniqueFlowId.trim(),
        flowName: uniqueFlowId.trim(), // Use unique ID as flow name for now
        description: description.trim() || `Orchestration flow with ID: ${uniqueFlowId.trim()}`,
        sourceBusinessComponentId: sourceBusinessComponent || null,
        targetBusinessComponentId: targetBusinessComponent || null,
        sourceAdapterId: sourceAdapter || null,
        targetAdapterId: targetAdapter || null,
        orchestrationSteps: orchestrationSteps,
        createdBy: user?.id || user?.username || 'unknown' // Use user ID from auth context
      };

      // Save the complete orchestration flow using the flow composition API
      const response = await api.post('/flow-composition/orchestration', flowRequest);
      
      if (response.data) {
        toast({
          title: "Success",
          description: `Orchestration flow "${uniqueFlowId}" has been created successfully.`,
        });
        navigateBack('/dashboard');
      }
      
    } catch (error) {
      console.error('Error saving orchestration flow:', error);
      toast({
        title: "Save Failed",
        description: error.response?.data?.message || "Failed to save the orchestration flow. Please try again.",
        variant: "destructive",
      });
    } finally {
      setSaving(false);
    }
  };

  const handleTestFlow = async () => {
    if (!sourceAdapter || !targetAdapter) {
      toast({
        title: "Validation Error",
        description: "Both source and target adapters are required for testing.",
        variant: "destructive",
      });
      return;
    }

    try {
      setTesting(true);
      
      // Validate the orchestration flow configuration before testing
      const validationRequest = {
        flowName: flowName.trim() || 'Test Orchestration Flow',
        sourceAdapterId: sourceAdapter,
        targetAdapterId: targetAdapter,
        sourceBusinessComponentId: sourceBusinessComponent || null,
        targetBusinessComponentId: targetBusinessComponent || null,
        orchestrationSteps: orchestrationSteps
      };

      const response = await api.post('/flow-composition/validate/orchestration', validationRequest);
      
      if (response.data?.valid) {
        toast({
          title: "Validation Passed",
          description: "Orchestration flow configuration is valid and ready for execution.",
        });
      } else {
        const errors = response.data?.errors?.join(', ') || 'Unknown validation errors';
        toast({
          title: "Validation Failed",
          description: errors,
          variant: "destructive",
        });
      }
      
    } catch (error) {
      console.error('Error testing orchestration flow:', error);
      toast({
        title: "Test Failed",
        description: error.response?.data?.message || "Failed to test the orchestration flow configuration.",
        variant: "destructive",
      });
    } finally {
      setTesting(false);
    }
  };

  const handleCancel = () => {
    navigateBack('/dashboard');
  };

  const handleOpenVisualEditor = () => {
    setShowVisualEditor(true);
  };

  if (showVisualEditor) {
    return (
      <div className="h-screen bg-background overflow-hidden">
        {/* Compact header with unique ID input */}
        <div className="h-24 border-b border-border px-6 flex flex-col justify-center space-y-3">
          <div className="flex items-center justify-between">
            <div className="flex items-center gap-4">
              <Button 
                variant="ghost" 
                size="sm" 
                onClick={() => navigateBack('/dashboard')}
                className="gap-2"
              >
                <ArrowLeft className="h-4 w-4" />
                Back to Dashboard
              </Button>
              <div>
                <h1 className="text-2xl font-bold tracking-tight">Visual Orchestration Editor</h1>
                <p className="text-sm text-muted-foreground">
                  Design complex workflows with multiple systems, routing, and business logic
                </p>
              </div>
            </div>
            
            <div className="flex items-center gap-4">
              {/* Action buttons */}
              <div className="flex gap-2">
                <Button
                  onClick={handleSave}
                  disabled={saving || !uniqueFlowId.trim()}
                  className="flex items-center gap-2"
                >
                  {saving ? <Loader2 className="h-4 w-4 animate-spin" /> : <Save className="h-4 w-4" />}
                  Save Flow
                </Button>
                <Button
                  variant="outline"
                  onClick={() => navigateBack('/dashboard')}
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
          
          {/* Unique ID Input */}
          <div className="flex items-center gap-4 max-w-md">
            <Label htmlFor="uniqueFlowId" className="text-sm font-medium whitespace-nowrap">
              Unique Flow ID:
            </Label>
            <Input
              id="uniqueFlowId"
              placeholder="Enter unique orchestration flow ID"
              value={uniqueFlowId}
              onChange={(e) => setUniqueFlowId(e.target.value)}
              className="flex-1"
            />
          </div>
        </div>

        {/* Full height flow designer */}
        <div className="h-[calc(100vh-96px)]">
          <VisualOrchestrationEditor 
            flowId={uniqueFlowId}
            onFlowChange={(flowData) => {
              // Update orchestration steps when flow changes
              setOrchestrationSteps(flowData.steps || []);
            }}
          />
        </div>
      </div>
    );
  }

  // Since we're going directly to visual editor, this should not be reached
  // but keeping as fallback
  return null;
}