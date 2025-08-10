// @ts-nocheck
import React, { useState, useEffect } from 'react';
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { apiClient } from '@/lib/api-client';
import { Button } from '@/components/ui/button';
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '@/components/ui/card';
import { Tabs, TabsContent, TabsList, TabsTrigger } from '@/components/ui/tabs';
import { Badge } from '@/components/ui/badge';
import { Alert, AlertDescription, AlertTitle } from '@/components/ui/alert';
import { Input } from '@/components/ui/input';
import { ScrollArea } from '@/components/ui/scroll-area';
import { Separator } from '@/components/ui/separator';
import { FunctionDialog } from '@/components/development/FunctionDialog';
import {
  Code,
  Plus,
  Search,
  Edit,
  Trash2,
  Play,
  AlertCircle,
  Lock,
  Unlock,
  Zap,
  Clock,
  CheckCircle2,
  Eye
} from 'lucide-react';
import { useToast } from '@/hooks/use-toast';

interface BuiltInFunction {
  name: string;
  category: string;
  description: string;
  signature: string;
}

interface CustomFunction {
  functionId: string;
  name: string;
  description: string;
  language: 'JAVA' | 'JAVASCRIPT' | 'GROOVY' | 'PYTHON';
  functionSignature: string;
  functionBody: string;
  dependencies?: string[];
  testCases?: any[];
  isSafe: boolean;
  isPublic: boolean;
  isBuiltIn: boolean;
  performanceClass: 'FAST' | 'NORMAL' | 'SLOW';
  version: number;
  createdBy: string;
  createdAt: string;
  updatedAt: string;
  category?: string;
}

interface DevelopmentFunctionsResponse {
  developmentMode: boolean;
  builtInFunctions: BuiltInFunction[];
  customFunctions: {
    content: CustomFunction[];
    totalElements: number;
    totalPages: number;
    number: number;
  };
}

export const DevelopmentFunctions = () => {
  const [searchTerm, setSearchTerm] = useState('');
  const [selectedCategory, setSelectedCategory] = useState('all');
  const [dialogOpen, setDialogOpen] = useState(false);
  const [selectedFunction, setSelectedFunction] = useState<CustomFunction | null>(null);
  const [dialogMode, setDialogMode] = useState<'view' | 'edit' | 'create'>('view');
  
  const queryClient = useQueryClient();

  // Check development mode
  const { data: devModeData } = useQuery({
    queryKey: ['development-mode'],
    queryFn: async () => {
      const response = await apiClient.get<{ developmentMode: boolean }>('/development/functions/development-mode');
      return response;
    }
  });

  // Fetch all functions
  const { data: functionsData, isLoading } = useQuery({
    queryKey: ['development-functions'],
    queryFn: async () => {
      const response = await apiClient.get<DevelopmentFunctionsResponse>('/development/functions');
      return response;
    }
  });

  // Get function by ID
  const getFunctionById = async (functionId: string) => {
    const response = await apiClient.get<CustomFunction>(`/development/functions/${functionId}`);
    return response;
  };

  // Create function mutation
  const createMutation = useMutation({
    mutationFn: async (data: any) => {
      const response = await apiClient.post('/development/functions', data);
      return response;
    },
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['development-functions'] });
      toast({ title: "Success", description: 'Function created successfully' });
      setDialogOpen(false);
    },
    onError: (error: any) => {
      toast({ title: "Error", description: error.response?.data?.message || 'Failed to create function', variant: "destructive" });
    }
  });

  // Update function mutation
  const updateMutation = useMutation({
    mutationFn: async ({ functionId, data }: { functionId: string; data: any }) => {
      const response = await apiClient.put(`/development/functions/${functionId}`, data);
      return response;
    },
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['development-functions'] });
      toast({ title: "Success", description: 'Function updated successfully' });
      setDialogOpen(false);
    },
    onError: (error: any) => {
      toast({ title: "Error", description: error.response?.data?.message || 'Failed to update function', variant: "destructive" });
    }
  });

  // Delete function mutation
  const deleteMutation = useMutation({
    mutationFn: async (functionId: string) => {
      await apiClient.delete(`/development/functions/${functionId}`);
    },
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['development-functions'] });
      toast({ title: "Success", description: 'Function deleted successfully' });
    },
    onError: (error: any) => {
      toast({ title: "Error", description: error.response?.data?.message || 'Failed to delete function', variant: "destructive" });
    }
  });

  // Compile Java code
  const compileCode = async (code: string) => {
    try {
      const response = await apiClient.post('/development/functions/compile', {
        functionName: selectedFunction?.name || 'TestFunction',
        code
      });
      return response;
    } catch (error: any) {
      throw error.response?.data || error;
    }
  };

  // Test function
  const testFunction = async (functionId: string, inputs: any) => {
    try {
      const response = await apiClient.post(`/development/functions/${functionId}/test`, inputs);
      return response;
    } catch (error: any) {
      throw error.response?.data || error;
    }
  };

  // Filter functions based on search and category
  const filteredBuiltInFunctions = functionsData?.builtInFunctions.filter(func => {
    const matchesSearch = func.name.toLowerCase().includes(searchTerm.toLowerCase()) ||
                         func.description.toLowerCase().includes(searchTerm.toLowerCase());
    const matchesCategory = selectedCategory === 'all' || func.category === selectedCategory;
    return matchesSearch && matchesCategory;
  }) || [];

  const filteredCustomFunctions = functionsData?.customFunctions.content.filter(func => {
    const matchesSearch = func.name.toLowerCase().includes(searchTerm.toLowerCase()) ||
                         func.description.toLowerCase().includes(searchTerm.toLowerCase());
    const matchesCategory = selectedCategory === 'all' || func.category === selectedCategory;
    return matchesSearch && matchesCategory;
  }) || [];

  // Get unique categories
  const categories = ['all', ...new Set(functionsData?.builtInFunctions.map(f => f.category) || [])];

  const isDevelopmentMode = devModeData?.developmentMode || false;

  const handleCreate = () => {
    setSelectedFunction(null);
    setDialogMode('create');
    setDialogOpen(true);
  };

  const handleView = async (functionId: string, isBuiltIn: boolean = false) => {
    try {
      const func = await getFunctionById(functionId);
      setSelectedFunction(func);
      setDialogMode('view');
      setDialogOpen(true);
    } catch (error) {
      toast({ title: "Error", description: 'Failed to load function details', variant: "destructive" });
    }
  };

  const handleEdit = async (functionId: string, isBuiltIn: boolean = false) => {
    try {
      const func = await getFunctionById(functionId);
      setSelectedFunction(func);
      setDialogMode('edit');
      setDialogOpen(true);
    } catch (error) {
      toast({ title: "Error", description: 'Failed to load function details', variant: "destructive" });
    }
  };

  const handleSave = async (data: any) => {
    if (dialogMode === 'create') {
      createMutation.mutate(data);
    } else if (dialogMode === 'edit' && selectedFunction) {
      updateMutation.mutate({ functionId: selectedFunction.functionId, data });
    }
  };

  const handleDelete = async (functionId: string) => {
    if (confirm('Are you sure you want to delete this function?')) {
      deleteMutation.mutate(functionId);
    }
  };

  const getPerformanceIcon = (performanceClass: string) => {
    switch (performanceClass) {
      case 'FAST':
        return <Zap className="h-3 w-3 text-green-500" />;
      case 'SLOW':
        return <Clock className="h-3 w-3 text-orange-500" />;
      default:
        return <CheckCircle2 className="h-3 w-3 text-blue-500" />;
    }
  };

  // Find built-in function details by name
  const findBuiltInFunctionByName = async (name: string) => {
    try {
      // Use the new endpoint to get built-in function by name
      const fullFunction = await apiClient.get<CustomFunction>(`/development/functions/built-in/${name}`);
      return fullFunction;
    } catch (error) {
      console.error('Error finding built-in function:', error);
      return null;
    }
  };

  const handleBuiltInFunctionClick = async (func: BuiltInFunction) => {
    try {
      // Find the full function data
      const fullFunction = await findBuiltInFunctionByName(func.name);
      if (fullFunction) {
        setSelectedFunction(fullFunction);
        setDialogMode(isDevelopmentMode ? 'edit' : 'view');
        setDialogOpen(true);
      } else {
        toast({ title: "Error", description: 'Function details not found', variant: "destructive" });
      }
    } catch (error) {
      toast({ title: "Error", description: 'Failed to load function details', variant: "destructive" });
    }
  };

  if (isLoading) {
    return (
      <div className="p-6">
        <div className="animate-pulse">
          <div className="h-8 bg-muted rounded w-1/4 mb-4"></div>
          <div className="h-64 bg-muted rounded"></div>
        </div>
      </div>
    );
  }

  return (
    <div className="p-6 space-y-6">
      <div className="flex items-center justify-between">
        <div>
          <h1 className="text-3xl font-bold text-foreground flex items-center gap-3">
            <Code className="h-8 w-8" />
            Development Functions
          </h1>
          <p className="text-muted-foreground">
            Manage and test transformation functions for visual flow and orchestration editors
          </p>
        </div>
        {isDevelopmentMode && (
          <Button className="gap-2" onClick={handleCreate}>
            <Plus className="h-4 w-4" />
            Create Function
          </Button>
        )}
      </div>

      {!isDevelopmentMode && (
        <Alert>
          <AlertCircle className="h-4 w-4" />
          <AlertTitle>Read-Only Mode</AlertTitle>
          <AlertDescription>
            Function creation and editing is only available in development mode. 
            Current profile: {devModeData?.developmentMode === false ? 'Production/QA' : 'Unknown'}
          </AlertDescription>
        </Alert>
      )}

      {/* Search and Filters */}
      <Card>
        <CardHeader>
          <CardTitle>Search Functions</CardTitle>
        </CardHeader>
        <CardContent className="space-y-4">
          <div className="relative">
            <Search className="absolute left-3 top-3 h-4 w-4 text-muted-foreground" />
            <Input
              placeholder="Search by name or description..."
              value={searchTerm}
              onChange={(e) => setSearchTerm(e.target.value)}
              className="pl-10"
            />
          </div>
          
          <div className="flex gap-2 flex-wrap">
            {categories.map(category => (
              <Badge
                key={category}
                variant={selectedCategory === category ? 'default' : 'outline'}
                className="cursor-pointer"
                onClick={() => setSelectedCategory(category)}
              >
                {category.charAt(0).toUpperCase() + category.slice(1)}
              </Badge>
            ))}
          </div>
        </CardContent>
      </Card>

      <Tabs defaultValue="builtin" className="space-y-4">
        <TabsList>
          <TabsTrigger value="builtin">
            Built-in Functions ({filteredBuiltInFunctions.length})
          </TabsTrigger>
          <TabsTrigger value="custom">
            Custom Functions ({filteredCustomFunctions.length})
          </TabsTrigger>
        </TabsList>

        <TabsContent value="builtin" className="space-y-4">
          <Card>
            <CardHeader>
              <CardTitle>Built-in Functions</CardTitle>
              <CardDescription>
                Pre-configured functions available in all environments
              </CardDescription>
            </CardHeader>
            <CardContent>
              <ScrollArea className="h-[600px]">
                <div className="space-y-2">
                  {filteredBuiltInFunctions.map((func, index) => (
                    <div
                      key={`${func.category}-${func.name}`}
                      className="p-4 border rounded-lg hover:bg-accent/50 transition-colors cursor-pointer"
                      onClick={() => handleBuiltInFunctionClick(func)}
                    >
                      <div className="flex items-start justify-between">
                        <div className="space-y-1">
                          <div className="flex items-center gap-2">
                            <h4 className="font-medium">{func.name}</h4>
                            <Badge variant="secondary" className="text-xs">
                              {func.category}
                            </Badge>
                            <Badge className="text-xs">
                              Built-in
                            </Badge>
                          </div>
                          <p className="text-sm text-muted-foreground">{func.description}</p>
                          <code className="text-xs bg-muted px-2 py-1 rounded">
                            {func.signature}
                          </code>
                        </div>
                        <div className="flex gap-2">
                          <Button
                            size="sm"
                            variant="ghost"
                            onClick={(e) => {
                              e.stopPropagation();
                              handleBuiltInFunctionClick(func);
                            }}
                          >
                            {isDevelopmentMode ? <Edit className="h-4 w-4" /> : <Eye className="h-4 w-4" />}
                          </Button>
                        </div>
                      </div>
                    </div>
                  ))}
                </div>
              </ScrollArea>
            </CardContent>
          </Card>
        </TabsContent>

        <TabsContent value="custom" className="space-y-4">
          <Card>
            <CardHeader>
              <CardTitle>Custom Functions</CardTitle>
              <CardDescription>
                User-defined functions for specialized transformations
              </CardDescription>
            </CardHeader>
            <CardContent>
              <ScrollArea className="h-[600px]">
                <div className="space-y-2">
                  {filteredCustomFunctions.filter(f => !f.isBuiltIn).map((func) => (
                    <div
                      key={func.functionId}
                      className="p-4 border rounded-lg hover:bg-accent/50 transition-colors cursor-pointer"
                      onClick={() => handleView(func.functionId)}
                    >
                      <div className="flex items-start justify-between">
                        <div className="space-y-1">
                          <div className="flex items-center gap-2">
                            <h4 className="font-medium">{func.name}</h4>
                            <Badge variant="secondary" className="text-xs">
                              {func.language}
                            </Badge>
                            <div className="flex items-center gap-1">
                              {getPerformanceIcon(func.performanceClass)}
                              <span className="text-xs text-muted-foreground">
                                {func.performanceClass}
                              </span>
                            </div>
                            {func.isSafe ? (
                              <Badge variant="outline" className="text-xs gap-1">
                                <CheckCircle2 className="h-3 w-3" />
                                Safe
                              </Badge>
                            ) : (
                              <Badge variant="destructive" className="text-xs gap-1">
                                <AlertCircle className="h-3 w-3" />
                                Unsafe
                              </Badge>
                            )}
                            {func.isPublic ? (
                              <Badge variant="outline" className="text-xs gap-1">
                                <Unlock className="h-3 w-3" />
                                Public
                              </Badge>
                            ) : (
                              <Badge variant="outline" className="text-xs gap-1">
                                <Lock className="h-3 w-3" />
                                Private
                              </Badge>
                            )}
                          </div>
                          <p className="text-sm text-muted-foreground">{func.description}</p>
                          <code className="text-xs bg-muted px-2 py-1 rounded">
                            {func.functionSignature}
                          </code>
                          <div className="text-xs text-muted-foreground">
                            Version {func.version} • Created by {func.createdBy}
                          </div>
                        </div>
                        {isDevelopmentMode && (
                          <div className="flex gap-2">
                            <Button
                              size="sm"
                              variant="ghost"
                              onClick={(e) => {
                                e.stopPropagation();
                                handleView(func.functionId);
                              }}
                            >
                              <Eye className="h-4 w-4" />
                            </Button>
                            <Button
                              size="sm"
                              variant="ghost"
                              onClick={(e) => {
                                e.stopPropagation();
                                handleEdit(func.functionId);
                              }}
                            >
                              <Edit className="h-4 w-4" />
                            </Button>
                            <Button
                              size="sm"
                              variant="ghost"
                              onClick={(e) => {
                                e.stopPropagation();
                                handleDelete(func.functionId);
                              }}
                            >
                              <Trash2 className="h-4 w-4" />
                            </Button>
                          </div>
                        )}
                      </div>
                    </div>
                  ))}
                  {filteredCustomFunctions.filter(f => !f.isBuiltIn).length === 0 && (
                    <div className="text-center py-8 text-muted-foreground">
                      No custom functions found
                    </div>
                  )}
                </div>
              </ScrollArea>
            </CardContent>
          </Card>
        </TabsContent>
      </Tabs>

      {/* Function Dialog */}
      <FunctionDialog
        open={dialogOpen}
        onOpenChange={setDialogOpen}
        function={selectedFunction}
        mode={dialogMode}
        onSave={handleSave}
        onCompile={compileCode}
        onTest={testFunction}
        isBuiltIn={selectedFunction?.isBuiltIn || false}
        developmentMode={isDevelopmentMode}
      />
    </div>
  );
};