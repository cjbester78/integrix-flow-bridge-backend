import React, { useState } from 'react';
import { Dialog, DialogContent, DialogHeader, DialogTitle, DialogTrigger } from '@/components/ui/dialog';
import { Button } from '@/components/ui/button';
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card';
import { Badge } from '@/components/ui/badge';
import { Progress } from '@/components/ui/progress';
import { Tabs, TabsContent, TabsList, TabsTrigger } from '@/components/ui/tabs';
import { Alert, AlertDescription } from '@/components/ui/alert';
import { CheckCircle, XCircle, AlertTriangle, PlayCircle, Loader2 } from 'lucide-react';
import { adapterValidationService, AdapterTestConfig, ValidationResult, TestResult } from '@/services/adapter/adapterValidation';
import { toast } from '@/hooks/use-toast';

interface AdapterValidationDialogProps {
  adapterType: string;
  configuration: Record<string, any>;
  children: React.ReactNode;
}

export const AdapterValidationDialog: React.FC<AdapterValidationDialogProps> = ({
  adapterType,
  configuration,
  children
}) => {
  const [isOpen, setIsOpen] = useState(false);
  const [isValidating, setIsValidating] = useState(false);
  const [validationResult, setValidationResult] = useState<ValidationResult | null>(null);
  const [progress, setProgress] = useState(0);

  const defaultTestScenarios = [
    { name: 'Connection Test', type: 'connection' as const, expectedResult: 'success' as const },
    { name: 'Authentication Test', type: 'authentication' as const, expectedResult: 'success' as const },
    { name: 'Data Transmission Test', type: 'data_transmission' as const, expectedResult: 'success' as const },
    { name: 'Error Handling Test', type: 'error_handling' as const, expectedResult: 'success' as const }
  ];

  const runValidation = async () => {
    if (!configuration || Object.keys(configuration).length === 0) {
      toast({
        title: "Validation Error",
        description: "No configuration found to validate",
        variant: "destructive"
      });
      return;
    }

    setIsValidating(true);
    setProgress(0);
    setValidationResult(null);

    try {
      const testConfig: AdapterTestConfig = {
        adapterType,
        configuration,
        testScenarios: defaultTestScenarios
      };

      // Simulate progress updates
      const progressInterval = setInterval(() => {
        setProgress(prev => Math.min(prev + 20, 90));
      }, 500);

      const result = await adapterValidationService.validateAdapter(testConfig);
      
      clearInterval(progressInterval);
      setProgress(100);
      setValidationResult(result);
      
    } catch (error) {
      toast({
        title: "Validation Failed",
        description: error instanceof Error ? error.message : "Unknown error occurred",
        variant: "destructive"
      });
    } finally {
      setIsValidating(false);
      setTimeout(() => setProgress(0), 1000);
    }
  };

  const getStatusIcon = (status: string) => {
    switch (status) {
      case 'passed':
        return <CheckCircle className="h-4 w-4 text-green-500" />;
      case 'failed':
        return <XCircle className="h-4 w-4 text-red-500" />;
      case 'warning':
        return <AlertTriangle className="h-4 w-4 text-yellow-500" />;
      default:
        return null;
    }
  };

  const getStatusBadge = (status: string) => {
    const variants = {
      passed: 'default',
      failed: 'destructive',
      warning: 'secondary'
    } as const;

    return (
      <Badge variant={variants[status as keyof typeof variants] || 'outline'}>
        {status}
      </Badge>
    );
  };

  return (
    <Dialog open={isOpen} onOpenChange={setIsOpen}>
      <DialogTrigger asChild>
        {children}
      </DialogTrigger>
      <DialogContent className="max-w-4xl max-h-[80vh] overflow-y-auto">
        <DialogHeader>
          <DialogTitle className="flex items-center gap-2">
            <PlayCircle className="h-5 w-5" />
            Adapter Validation - {adapterType.replace('_', ' ').toUpperCase()}
          </DialogTitle>
        </DialogHeader>

        <div className="space-y-4">
          {/* Validation Controls */}
          <Card>
            <CardHeader>
              <CardTitle className="text-lg">Validation Controls</CardTitle>
            </CardHeader>
            <CardContent className="space-y-4">
              <div className="flex items-center justify-between">
                <p className="text-sm text-muted-foreground">
                  Run comprehensive validation tests for this adapter configuration
                </p>
                <Button 
                  onClick={runValidation} 
                  disabled={isValidating}
                  className="flex items-center gap-2"
                >
                  {isValidating ? (
                    <>
                      <Loader2 className="h-4 w-4 animate-spin" />
                      Validating...
                    </>
                  ) : (
                    <>
                      <PlayCircle className="h-4 w-4" />
                      Run Validation
                    </>
                  )}
                </Button>
              </div>

              {isValidating && (
                <div className="space-y-2">
                  <div className="flex justify-between text-sm">
                    <span>Validation Progress</span>
                    <span>{progress}%</span>
                  </div>
                  <Progress value={progress} className="h-2" />
                </div>
              )}
            </CardContent>
          </Card>

          {/* Validation Results */}
          {validationResult && (
            <Card>
              <CardHeader>
                <CardTitle className="flex items-center gap-2">
                  {validationResult.isValid ? (
                    <CheckCircle className="h-5 w-5 text-green-500" />
                  ) : (
                    <XCircle className="h-5 w-5 text-red-500" />
                  )}
                  Validation Results
                </CardTitle>
              </CardHeader>
              <CardContent>
                <Tabs defaultValue="summary" className="w-full">
                  <TabsList>
                    <TabsTrigger value="summary">Summary</TabsTrigger>
                    <TabsTrigger value="tests">Test Results</TabsTrigger>
                    <TabsTrigger value="errors">Issues</TabsTrigger>
                  </TabsList>

                  <TabsContent value="summary" className="space-y-4">
                    <div className="grid grid-cols-3 gap-4">
                      <Card>
                        <CardContent className="pt-6">
                          <div className="text-2xl font-bold text-center">
                            {validationResult.testResults?.filter(t => t.status === 'passed').length || 0}
                          </div>
                          <p className="text-xs text-muted-foreground text-center">Passed</p>
                        </CardContent>
                      </Card>
                      <Card>
                        <CardContent className="pt-6">
                          <div className="text-2xl font-bold text-center text-red-500">
                            {validationResult.testResults?.filter(t => t.status === 'failed').length || 0}
                          </div>
                          <p className="text-xs text-muted-foreground text-center">Failed</p>
                        </CardContent>
                      </Card>
                      <Card>
                        <CardContent className="pt-6">
                          <div className="text-2xl font-bold text-center text-yellow-500">
                            {validationResult.warnings.length}
                          </div>
                          <p className="text-xs text-muted-foreground text-center">Warnings</p>
                        </CardContent>
                      </Card>
                    </div>

                    <Alert>
                      <AlertDescription>
                        {validationResult.isValid 
                          ? "All validation tests passed successfully. This adapter configuration is ready for deployment."
                          : "Some validation tests failed. Please review the issues before proceeding."}
                      </AlertDescription>
                    </Alert>
                  </TabsContent>

                  <TabsContent value="tests" className="space-y-4">
                    {validationResult.testResults?.map((test, index) => (
                      <Card key={index}>
                        <CardContent className="pt-4">
                          <div className="flex items-center justify-between">
                            <div className="flex items-center gap-3">
                              {getStatusIcon(test.status)}
                              <div>
                                <h4 className="font-medium">{test.testName}</h4>
                                <p className="text-sm text-muted-foreground">{test.message}</p>
                              </div>
                            </div>
                            <div className="flex items-center gap-2">
                              {getStatusBadge(test.status)}
                              {test.duration && (
                                <span className="text-xs text-muted-foreground">
                                  {test.duration}ms
                                </span>
                              )}
                            </div>
                          </div>
                        </CardContent>
                      </Card>
                    ))}
                  </TabsContent>

                  <TabsContent value="errors" className="space-y-4">
                    {validationResult.errors.length > 0 && (
                      <div className="space-y-2">
                        <h4 className="font-medium text-red-500">Errors</h4>
                        {validationResult.errors.map((error, index) => (
                          <Alert key={index} variant="destructive">
                            <XCircle className="h-4 w-4" />
                            <AlertDescription>{error}</AlertDescription>
                          </Alert>
                        ))}
                      </div>
                    )}

                    {validationResult.warnings.length > 0 && (
                      <div className="space-y-2">
                        <h4 className="font-medium text-yellow-500">Warnings</h4>
                        {validationResult.warnings.map((warning, index) => (
                          <Alert key={index}>
                            <AlertTriangle className="h-4 w-4" />
                            <AlertDescription>{warning}</AlertDescription>
                          </Alert>
                        ))}
                      </div>
                    )}

                    {validationResult.errors.length === 0 && validationResult.warnings.length === 0 && (
                      <Alert>
                        <CheckCircle className="h-4 w-4" />
                        <AlertDescription>
                          No issues found. The adapter configuration is valid.
                        </AlertDescription>
                      </Alert>
                    )}
                  </TabsContent>
                </Tabs>
              </CardContent>
            </Card>
          )}
        </div>
      </DialogContent>
    </Dialog>
  );
};