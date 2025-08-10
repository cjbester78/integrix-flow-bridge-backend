// @ts-nocheck
import React, { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '@/components/ui/card';
import { Button } from '@/components/ui/button';
import { Badge } from '@/components/ui/badge';
import { Input } from '@/components/ui/input';
import { Tabs, TabsContent, TabsList, TabsTrigger } from '@/components/ui/tabs';
import {
  Table,
  TableBody,
  TableCell,
  TableHead,
  TableHeader,
  TableRow
} from '@/components/ui/table';
import {
  DropdownMenu,
  DropdownMenuContent,
  DropdownMenuItem,
  DropdownMenuLabel,
  DropdownMenuSeparator,
  DropdownMenuTrigger,
} from '@/components/ui/dropdown-menu';
import {
  Dialog,
  DialogContent,
  DialogDescription,
  DialogHeader,
  DialogTitle,
  DialogFooter,
} from '@/components/ui/dialog';

import { flowService } from '@/services/flowService';
import { IntegrationFlow } from '@/types/flow';
import { 
  Plus, 
  Search, 
  Eye, 
  Edit, 
  Trash2, 
  MoreVertical,
  Loader2,
  Filter,
  RefreshCw,
  GitBranch,
  Workflow,
  ScrollText,
  Rocket,
  AlertCircle
} from 'lucide-react';
import { useToast } from '@/hooks/use-toast';
import { deploymentService } from '@/services/deploymentService';

export default function AllInterfaces() {
  const navigate = useNavigate();
  const { toast } = useToast();
  const [flows, setFlows] = useState<IntegrationFlow[]>([]);
  const [loading, setLoading] = useState(true);
  const [searchTerm, setSearchTerm] = useState('');
  const [deleteDialogOpen, setDeleteDialogOpen] = useState(false);
  const [flowToDelete, setFlowToDelete] = useState<IntegrationFlow | null>(null);
  const [activeTab, setActiveTab] = useState('all');
  const [deployingFlowId, setDeployingFlowId] = useState<string | null>(null);

  useEffect(() => {
    fetchFlows();
  }, []);

  const fetchFlows = async () => {
    try {
      setLoading(true);
      const response = await flowService.getFlows();
      if (response.success && response.data) {
        // Handle both direct array response and wrapped response
        const flowsData = Array.isArray(response.data) ? response.data : response.data.flows || [];
        setFlows(flowsData);
      }
    } catch (error) {
      toast({
        variant: 'destructive',
        title: 'Error',
        description: 'Failed to fetch interfaces',
      });
    } finally {
      setLoading(false);
    }
  };

  const handleRefresh = () => {
    fetchFlows();
    toast({ title: "Success", description: 'Interfaces refreshed' });
  };

  const handleDelete = (flow: IntegrationFlow) => {
    setFlowToDelete(flow);
    setDeleteDialogOpen(true);
  };

  const confirmDelete = async () => {
    if (!flowToDelete) return;

    try {
      const response = await flowService.deleteFlow(flowToDelete.id);
      if (response.success) {
        toast({
          title: 'Success',
          description: 'Interface deleted successfully',
        });
        fetchFlows();
      } else {
        throw new Error(response.error || 'Failed to delete interface');
      }
    } catch (error) {
      toast({
        variant: 'destructive',
        title: 'Error',
        description: error instanceof Error ? error.message : 'Failed to delete interface',
      });
    } finally {
      setDeleteDialogOpen(false);
      setFlowToDelete(null);
    }
  };

  const handleDeploy = async (flow: IntegrationFlow) => {
    try {
      setDeployingFlowId(flow.id);
      
      const response = await deploymentService.deployFlow(flow.id);
      
      if (response.success) {
        toast({
          title: 'Success',
          description: `Interface "${flow.name}" has been deployed successfully`,
        });
        // Refresh the list
        fetchFlows();
      } else {
        toast({
          variant: 'destructive',
          title: 'Deployment Failed',
          description: response.error || 'Failed to deploy interface',
        });
      }
    } catch (error) {
      toast({
        variant: 'destructive',
        title: 'Error',
        description: 'An error occurred during deployment',
      });
    } finally {
      setDeployingFlowId(null);
    }
  };

  const getFilteredFlows = () => {
    let filtered = flows.filter(flow => 
      flow.name.toLowerCase().includes(searchTerm.toLowerCase()) ||
      flow.description?.toLowerCase().includes(searchTerm.toLowerCase())
    );

    switch (activeTab) {
      case 'undeployed':
        return filtered.filter(flow => 
          flow.status === 'DEVELOPED_INACTIVE' || 
          flow.status === 'DRAFT' || 
          flow.status === 'INACTIVE'
        );
      case 'deployed':
        return filtered.filter(flow => 
          flow.status === 'DEPLOYED_ACTIVE' || 
          flow.status === 'ACTIVE'
        );
      default:
        return filtered;
    }
  };

  const filteredFlows = getFilteredFlows();

  const getStatusBadge = (status: string) => {
    const statusColors = {
      'DEPLOYED_ACTIVE': 'bg-green-500',
      'DRAFT': 'bg-blue-500',
      'INACTIVE': 'bg-gray-500',
      'ERROR': 'bg-red-500',
      'DEVELOPED_INACTIVE': 'bg-yellow-500'
    };

    return (
      <Badge className={`${statusColors[status] || 'bg-gray-500'} text-white`}>
        {status.replace('_', ' ')}
      </Badge>
    );
  };

  const getFlowTypeBadge = (mappingMode: string) => {
    return (
      <Badge variant="outline">
        {mappingMode === 'WITH_MAPPING' ? 'Direct Mapping' : 'Orchestration'}
      </Badge>
    );
  };

  if (loading) {
    return (
      <div className="container mx-auto p-6">
        <div className="flex justify-center items-center p-12">
          <Loader2 className="h-8 w-8 animate-spin" />
        </div>
      </div>
    );
  }

  return (
    <div className="p-8 space-y-6">
      <div className="flex justify-between items-center">
        <div>
          <h1 className="text-3xl font-bold">Interface Management</h1>
          <p className="text-muted-foreground mt-2">
            Manage all your integration flows in one place
          </p>
        </div>
        <div className="flex gap-2">
          <Button onClick={() => navigate('/create-direct-mapping-flow')}>
            <Plus className="h-4 w-4 mr-2" />
            <GitBranch className="h-4 w-4 mr-2" />
            Direct Mapping
          </Button>
          <Button 
            onClick={() => navigate('/create-orchestration-flow')}
            variant="outline"
          >
            <Plus className="h-4 w-4 mr-2" />
            <Workflow className="h-4 w-4 mr-2" />
            Orchestration Flow
          </Button>
        </div>
      </div>

      <Tabs value={activeTab} onValueChange={setActiveTab} className="space-y-4">
        <TabsList className="grid w-full grid-cols-3">
          <TabsTrigger value="all">All Interfaces</TabsTrigger>
          <TabsTrigger value="undeployed">Undeployed</TabsTrigger>
          <TabsTrigger value="deployed">Deployed</TabsTrigger>
        </TabsList>

        <Card>
          <CardHeader>
            <CardTitle className="flex items-center gap-2">
              <Filter className="h-5 w-5" />
              Filters
            </CardTitle>
          </CardHeader>
          <CardContent>
            <div className="relative">
              <Search className="absolute left-3 top-1/2 transform -translate-y-1/2 text-gray-400 h-4 w-4" />
              <Input
                placeholder="Search by name..."
                value={searchTerm}
                onChange={(e) => setSearchTerm(e.target.value)}
                className="pl-10"
              />
            </div>
          </CardContent>
        </Card>

        <TabsContent value={activeTab}>
          <Card>
            <CardHeader>
              <div className="flex justify-between items-center">
                <CardTitle className="flex items-center gap-2">
                  <ScrollText className="h-5 w-5" />
                  {activeTab === 'all' && `All Interfaces (${filteredFlows.length})`}
                  {activeTab === 'undeployed' && `Undeployed Interfaces (${filteredFlows.length})`}
                  {activeTab === 'deployed' && `Deployed Interfaces (${filteredFlows.length})`}
                </CardTitle>
                <Button variant="outline" size="sm" onClick={handleRefresh}>
                  <RefreshCw className="h-4 w-4 mr-2" />
                  Refresh
                </Button>
              </div>
            </CardHeader>
            <CardContent>
              {loading ? (
                <div className="text-center py-8">
                  <p className="text-muted-foreground">Loading interfaces...</p>
                </div>
              ) : filteredFlows.length === 0 ? (
                <div className="text-center py-8">
                  <AlertCircle className="h-12 w-12 text-muted-foreground mx-auto mb-4" />
                  <p className="text-muted-foreground">
                    {searchTerm ? 'No interfaces found matching your search.' : 
                     activeTab === 'deployed' ? 'No deployed interfaces found.' :
                     activeTab === 'undeployed' ? 'No undeployed interfaces found.' :
                     'No interfaces created yet.'}
                  </p>
                </div>
              ) : (
                <div className="overflow-x-auto">
                  <Table>
                    <TableHeader>
                      <TableRow>
                        <TableHead>Name</TableHead>
                        <TableHead>Type</TableHead>
                        <TableHead>Source → Target</TableHead>
                        <TableHead>Status</TableHead>
                        <TableHead>Created</TableHead>
                        <TableHead>Actions</TableHead>
                      </TableRow>
                    </TableHeader>
                    <TableBody>
                      {filteredFlows.map((flow) => {
                        const isDeploying = deployingFlowId === flow.id;
                        return (
                          <TableRow key={flow.id}>
                            <TableCell className="font-medium">{flow.name}</TableCell>
                            <TableCell>{getFlowTypeBadge(flow.mappingMode || 'PASS_THROUGH')}</TableCell>
                            <TableCell>
                              <div className="flex items-center gap-2 text-sm">
                                <Badge variant="outline">
                                  {flow.sourceAdapterType || 'Unknown'}
                                </Badge>
                                <span>→</span>
                                <Badge variant="outline">
                                  {flow.targetAdapterType || 'Unknown'}
                                </Badge>
                              </div>
                            </TableCell>
                            <TableCell>{getStatusBadge(flow.status)}</TableCell>
                            <TableCell>{new Date(flow.createdAt).toLocaleDateString()}</TableCell>
                            <TableCell>
                              <DropdownMenu>
                                <DropdownMenuTrigger asChild>
                                  <Button variant="ghost" size="sm">
                                    <MoreVertical className="h-4 w-4" />
                                  </Button>
                                </DropdownMenuTrigger>
                                <DropdownMenuContent align="end">
                                  <DropdownMenuLabel>Actions</DropdownMenuLabel>
                                  <DropdownMenuSeparator />
                                  {activeTab === 'undeployed' ? (
                                    // Only show Deploy action on Undeployed tab
                                    <DropdownMenuItem 
                                      onClick={() => handleDeploy(flow)}
                                      disabled={isDeploying || flow.status === 'DRAFT'}
                                    >
                                      {isDeploying ? (
                                        <Loader2 className="h-4 w-4 mr-2 animate-spin" />
                                      ) : (
                                        <Rocket className="h-4 w-4 mr-2" />
                                      )}
                                      Deploy
                                    </DropdownMenuItem>
                                  ) : (
                                    // Show all actions on other tabs
                                    <>
                                      <DropdownMenuItem onClick={() => navigate(`/flows/${flow.id}/edit`)}>
                                        <Eye className="h-4 w-4 mr-2" />
                                        View Details
                                      </DropdownMenuItem>
                                      <DropdownMenuItem onClick={() => navigate(`/flows/${flow.id}/edit`)}>
                                        <Edit className="h-4 w-4 mr-2" />
                                        Edit
                                      </DropdownMenuItem>
                                      {activeTab === 'all' && (flow.status === 'DEVELOPED_INACTIVE' || flow.status === 'DRAFT' || flow.status === 'INACTIVE') && (
                                        <>
                                          <DropdownMenuSeparator />
                                          <DropdownMenuItem 
                                            onClick={() => handleDeploy(flow)}
                                            disabled={isDeploying || flow.status === 'DRAFT'}
                                          >
                                            {isDeploying ? (
                                              <Loader2 className="h-4 w-4 mr-2 animate-spin" />
                                            ) : (
                                              <Rocket className="h-4 w-4 mr-2" />
                                            )}
                                            Deploy
                                          </DropdownMenuItem>
                                        </>
                                      )}
                                      <DropdownMenuSeparator />
                                      <DropdownMenuItem
                                        onClick={() => handleDelete(flow)}
                                        className="text-red-600"
                                      >
                                        <Trash2 className="h-4 w-4 mr-2" />
                                        Delete
                                      </DropdownMenuItem>
                                    </>
                                  )}
                                </DropdownMenuContent>
                              </DropdownMenu>
                            </TableCell>
                          </TableRow>
                        );
                      })}
                    </TableBody>
                  </Table>
                </div>
              )}
            </CardContent>
          </Card>
        </TabsContent>
      </Tabs>

      {/* Delete Confirmation Dialog */}
      <Dialog open={deleteDialogOpen} onOpenChange={setDeleteDialogOpen}>
        <DialogContent>
          <DialogHeader>
            <DialogTitle>Delete Interface</DialogTitle>
            <DialogDescription>
              Are you sure you want to delete "{flowToDelete?.name}"? This action cannot be undone.
            </DialogDescription>
          </DialogHeader>
          <DialogFooter>
            <Button
              variant="outline"
              onClick={() => {
                setDeleteDialogOpen(false);
                setFlowToDelete(null);
              }}
            >
              Cancel
            </Button>
            <Button
              variant="destructive"
              onClick={confirmDelete}
            >
              Delete
            </Button>
          </DialogFooter>
        </DialogContent>
      </Dialog>
    </div>
  );
}
