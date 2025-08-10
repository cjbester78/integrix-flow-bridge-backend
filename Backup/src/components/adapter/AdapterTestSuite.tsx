import React, { useState, useEffect } from 'react';
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card';
import { Button } from '@/components/ui/button';
import { Badge } from '@/components/ui/badge';
import { Progress } from '@/components/ui/progress';
import { Tabs, TabsContent, TabsList, TabsTrigger } from '@/components/ui/tabs';
import { Alert, AlertDescription } from '@/components/ui/alert';
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from '@/components/ui/select';
import { Input } from '@/components/ui/input';
import { Label } from '@/components/ui/label';
import { CheckCircle, XCircle, AlertTriangle, PlayCircle, Loader2, RotateCcw, Download } from 'lucide-react';
import { adapterValidationService, AdapterTestConfig, ValidationResult } from '@/services/adapter/adapterValidation';
import { toast } from '@/hooks/use-toast';

interface AdapterTestSuiteProps {
  adapterType?: string;
  configuration?: Record<string, any>;
}

export const AdapterTestSuite: React.FC<AdapterTestSuiteProps> = ({
  adapterType: initialAdapterType,
  configuration: initialConfiguration
}) => {
  const [selectedAdapterType, setSelectedAdapterType] = useState(initialAdapterType || '');
  const [testConfiguration, setTestConfiguration] = useState<Record<string, any>>(initialConfiguration || {});
  const [isRunning, setIsRunning] = useState(false);
  const [currentTest, setCurrentTest] = useState('');
  const [progress, setProgress] = useState(0);
  const [testResults, setTestResults] = useState<ValidationResult[]>([]);
  const [testHistory, setTestHistory] = useState<{ timestamp: string; result: ValidationResult; adapterType: string }[]>([]);

  const adapterTypes = [
    'http_sender', 'http_receiver', 'soap_sender', 'soap_receiver',
    'rest_sender', 'rest_receiver', 'ftp', 'sftp', 'file',
    'jdbc_sender', 'jdbc_receiver', 'jms_sender', 'jms_receiver',
    'mail_sender', 'mail_receiver', 'rfc_sender', 'rfc_receiver',
    'idoc_sender', 'idoc_receiver', 'odata_sender', 'odata_receiver'
  ];

  const testScenarios = [
    { name: 'Quick Validation', scenarios: ['connection', 'authentication'] },
    { name: 'Full Validation', scenarios: ['connection', 'authentication', 'data_transmission', 'error_handling'] },
    { name: 'Performance Test', scenarios: ['connection', 'data_transmission', 'load_test'] },
    { name: 'Security Test', scenarios: ['authentication', 'authorization', 'encryption'] }
  ];

  const runTestSuite = async (testType: string) => {
    if (!selectedAdapterType) {
      toast({
        title: "Adapter Type Required",
        description: "Please select an adapter type to test",
        variant: "destructive"
      });
      return;
    }

    setIsRunning(true);
    setProgress(0);
    setCurrentTest('Initializing...');

    try {
      const selectedTest = testScenarios.find(t => t.name === testType);
      if (!selectedTest) return;

      const testConfig: AdapterTestConfig = {
        adapterType: selectedAdapterType,
        configuration: testConfiguration,
        testScenarios: selectedTest.scenarios.map(scenario => ({
          name: scenario.replace('_', ' ').toUpperCase(),
          type: scenario as any,
          expectedResult: 'success'
        }))
      };

      // Simulate test progression
      const totalSteps = selectedTest.scenarios.length + 2; // +2 for setup and cleanup
      let currentStep = 0;

      const updateProgress = (step: string) => {
        currentStep++;
        setCurrentTest(step);
        setProgress((currentStep / totalSteps) * 100);
      };

      updateProgress('Setting up test environment...');
      await new Promise(resolve => setTimeout(resolve, 1000));

      updateProgress('Running validation tests...');
      const result = await adapterValidationService.validateAdapter(testConfig);

      updateProgress('Cleaning up...');
      await new Promise(resolve => setTimeout(resolve, 500));

      setTestResults(prev => [...prev, result]);
      setTestHistory(prev => [...prev, {
        timestamp: new Date().toISOString(),
        result,
        adapterType: selectedAdapterType
      }]);

      toast({
        title: "Test Suite Complete",
        description: `${testType} completed with ${result.testResults?.length || 0} tests`,
        variant: result.isValid ? "default" : "destructive"
      });

    } catch (error) {
      toast({
        title: "Test Suite Failed",
        description: error instanceof Error ? error.message : "Unknown error occurred",
        variant: "destructive"
      });
    } finally {
      setIsRunning(false);
      setCurrentTest('');
      setProgress(0);
    }
  };

  const exportResults = () => {
    const exportData = {
      timestamp: new Date().toISOString(),
      adapterType: selectedAdapterType,
      testResults: testResults,
      testHistory: testHistory
    };

    const blob = new Blob([JSON.stringify(exportData, null, 2)], { type: 'application/json' });
    const url = URL.createObjectURL(blob);
    const a = document.createElement('a');
    a.href = url;
    a.download = `adapter-test-results-${selectedAdapterType}-${new Date().getTime()}.json`;
    document.body.appendChild(a);
    a.click();
    document.body.removeChild(a);
    URL.revokeObjectURL(url);
  };

  const clearResults = () => {
    setTestResults([]);
    setProgress(0);
    setCurrentTest('');
  };

  const getOverallStatus = () => {
    if (testResults.length === 0) return 'pending';
    const hasFailures = testResults.some(result => !result.isValid);
    return hasFailures ? 'failed' : 'passed';
  };

  return (
    <div className="space-y-6">
      {/* Test Configuration */}
      <Card>
        <CardHeader>
          <CardTitle>Test Configuration</CardTitle>
        </CardHeader>
        <CardContent className="space-y-4">
          <div className="grid grid-cols-2 gap-4">
            <div className="space-y-2">
              <Label htmlFor="adapter-type">Adapter Type</Label>
              <Select value={selectedAdapterType} onValueChange={setSelectedAdapterType}>
                <SelectTrigger>
                  <SelectValue placeholder="Select adapter type" />
                </SelectTrigger>
                <SelectContent>
                  {adapterTypes.map(type => (
                    <SelectItem key={type} value={type}>
                      {type.replace('_', ' ').toUpperCase()}
                    </SelectItem>
                  ))}
                </SelectContent>
              </Select>
            </div>

            <div className="space-y-2">
              <Label htmlFor="test-endpoint">Test Endpoint (Optional)</Label>
              <Input
                placeholder="http://localhost:8080/test"
                value={testConfiguration.testEndpoint || ''}
                onChange={(e) => setTestConfiguration(prev => ({
                  ...prev,
                  testEndpoint: e.target.value
                }))}
              />
            </div>
          </div>

          <div className="flex gap-2 flex-wrap">
            {testScenarios.map(scenario => (
              <Button
                key={scenario.name}
                variant="outline"
                size="sm"
                onClick={() => runTestSuite(scenario.name)}
                disabled={isRunning || !selectedAdapterType}
                className="flex items-center gap-2"
              >
                <PlayCircle className="h-4 w-4" />
                {scenario.name}
              </Button>
            ))}
          </div>

          {isRunning && (
            <div className="space-y-2">
              <div className="flex items-center gap-2">
                <Loader2 className="h-4 w-4 animate-spin" />
                <span className="text-sm">{currentTest}</span>
              </div>
              <Progress value={progress} className="h-2" />
            </div>
          )}
        </CardContent>
      </Card>

      {/* Test Results */}
      {testResults.length > 0 && (
        <Card>
          <CardHeader>
            <div className="flex items-center justify-between">
              <CardTitle className="flex items-center gap-2">
                {getOverallStatus() === 'passed' ? (
                  <CheckCircle className="h-5 w-5 text-green-500" />
                ) : (
                  <XCircle className="h-5 w-5 text-red-500" />
                )}
                Test Results
              </CardTitle>
              <div className="flex gap-2">
                <Button variant="outline" size="sm" onClick={exportResults}>
                  <Download className="h-4 w-4 mr-1" />
                  Export
                </Button>
                <Button variant="outline" size="sm" onClick={clearResults}>
                  <RotateCcw className="h-4 w-4 mr-1" />
                  Clear
                </Button>
              </div>
            </div>
          </CardHeader>
          <CardContent>
            <Tabs defaultValue="latest" className="w-full">
              <TabsList>
                <TabsTrigger value="latest">Latest Results</TabsTrigger>
                <TabsTrigger value="history">Test History</TabsTrigger>
                <TabsTrigger value="summary">Summary</TabsTrigger>
              </TabsList>

              <TabsContent value="latest" className="space-y-4">
                {testResults[testResults.length - 1] && (
                  <div className="space-y-4">
                    {testResults[testResults.length - 1].testResults?.map((test, index) => (
                      <Card key={index}>
                        <CardContent className="pt-4">
                          <div className="flex items-center justify-between">
                            <div className="flex items-center gap-3">
                              {test.status === 'passed' ? (
                                <CheckCircle className="h-4 w-4 text-green-500" />
                              ) : test.status === 'failed' ? (
                                <XCircle className="h-4 w-4 text-red-500" />
                              ) : (
                                <AlertTriangle className="h-4 w-4 text-yellow-500" />
                              )}
                              <div>
                                <h4 className="font-medium">{test.testName}</h4>
                                <p className="text-sm text-muted-foreground">{test.message}</p>
                              </div>
                            </div>
                            <div className="flex items-center gap-2">
                              <Badge variant={test.status === 'passed' ? 'default' : test.status === 'failed' ? 'destructive' : 'secondary'}>
                                {test.status}
                              </Badge>
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

                    {testResults[testResults.length - 1].errors.length > 0 && (
                      <Alert variant="destructive">
                        <XCircle className="h-4 w-4" />
                        <AlertDescription>
                          {testResults[testResults.length - 1].errors.join(', ')}
                        </AlertDescription>
                      </Alert>
                    )}
                  </div>
                )}
              </TabsContent>

              <TabsContent value="history" className="space-y-4">
                {testHistory.map((entry, index) => (
                  <Card key={index}>
                    <CardContent className="pt-4">
                      <div className="flex items-center justify-between">
                        <div>
                          <h4 className="font-medium">{entry.adapterType.replace('_', ' ').toUpperCase()}</h4>
                          <p className="text-sm text-muted-foreground">
                            {new Date(entry.timestamp).toLocaleString()}
                          </p>
                        </div>
                        <div className="flex items-center gap-2">
                          <Badge variant={entry.result.isValid ? 'default' : 'destructive'}>
                            {entry.result.isValid ? 'Passed' : 'Failed'}
                          </Badge>
                          <span className="text-sm text-muted-foreground">
                            {entry.result.testResults?.length || 0} tests
                          </span>
                        </div>
                      </div>
                    </CardContent>
                  </Card>
                ))}
              </TabsContent>

              <TabsContent value="summary" className="space-y-4">
                <div className="grid grid-cols-4 gap-4">
                  <Card>
                    <CardContent className="pt-6">
                      <div className="text-2xl font-bold text-center">
                        {testHistory.length}
                      </div>
                      <p className="text-xs text-muted-foreground text-center">Total Tests</p>
                    </CardContent>
                  </Card>
                  <Card>
                    <CardContent className="pt-6">
                      <div className="text-2xl font-bold text-center text-green-500">
                        {testHistory.filter(t => t.result.isValid).length}
                      </div>
                      <p className="text-xs text-muted-foreground text-center">Passed</p>
                    </CardContent>
                  </Card>
                  <Card>
                    <CardContent className="pt-6">
                      <div className="text-2xl font-bold text-center text-red-500">
                        {testHistory.filter(t => !t.result.isValid).length}
                      </div>
                      <p className="text-xs text-muted-foreground text-center">Failed</p>
                    </CardContent>
                  </Card>
                  <Card>
                    <CardContent className="pt-6">
                      <div className="text-2xl font-bold text-center">
                        {Math.round((testHistory.filter(t => t.result.isValid).length / Math.max(testHistory.length, 1)) * 100)}%
                      </div>
                      <p className="text-xs text-muted-foreground text-center">Success Rate</p>
                    </CardContent>
                  </Card>
                </div>
              </TabsContent>
            </Tabs>
          </CardContent>
        </Card>
      )}
    </div>
  );
};