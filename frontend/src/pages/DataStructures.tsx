// @ts-nocheck
import { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { Plus, Filter, Search, Database, RefreshCw, Layers, Edit2, Trash2, MoreHorizontal } from 'lucide-react';
import { Button } from '@/components/ui/button';
import { Input } from '@/components/ui/input';
import { PageContainer } from '@/components/ui/page-container';
import { PageHeader } from '@/components/common/PageHeader';
import {
  DropdownMenu,
  DropdownMenuContent,
  DropdownMenuItem,
  DropdownMenuSeparator,
  DropdownMenuTrigger,
} from '@/components/ui/dropdown-menu';
import {
  AlertDialog,
  AlertDialogAction,
  AlertDialogCancel,
  AlertDialogContent,
  AlertDialogDescription,
  AlertDialogFooter,
  AlertDialogHeader,
  AlertDialogTitle,
} from '@/components/ui/alert-dialog';
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card';
import {
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from '@/components/ui/select';
import {
  Table,
  TableBody,
  TableCell,
  TableHead,
  TableHeader,
  TableRow,
} from '@/components/ui/table';
import { Badge } from '@/components/ui/badge';
import { useToast } from '@/hooks/use-toast';
import { api } from '@/services/api';
import { useEnvironmentPermissions } from '@/hooks/useEnvironmentPermissions-no-query';
import { useDocumentTitle } from '@/hooks/useDocumentTitle';
import { useMetaDescription } from '@/hooks/useMetaDescription';

interface DataStructure {
  id: string;
  name: string;
  type: string;
  usage: 'source' | 'target' | 'both';
  description?: string;
  businessComponentId?: string;
  businessComponentName?: string;
  tags?: string[];
  createdAt: string;
  updatedAt: string;
}

interface BusinessComponent {
  id: string;
  name: string;
}

const structureTypes = ['json', 'xml', 'xsd', 'wsdl', 'edmx', 'custom'];

export const DataStructures = () => {
  useDocumentTitle('Data Structures');
  useMetaDescription('Manage data structures for your integration flows. Define JSON, XML, XSD, WSDL, and custom formats for seamless data transformation in Integrix Flow Bridge.');
  const navigate = useNavigate();
  const { isDevelopment } = useEnvironmentPermissions();
  
  const [structures, setStructures] = useState<DataStructure[]>([]);
  const [filteredStructures, setFilteredStructures] = useState<DataStructure[]>([]);
  const [businessComponents, setBusinessComponents] = useState<BusinessComponent[]>([]);
  const [loading, setLoading] = useState(true);
  const [searchTerm, setSearchTerm] = useState('');
  const [selectedBusinessComponent, setSelectedBusinessComponent] = useState('all');
  const [selectedUsage, setSelectedUsage] = useState('all');
  const [deleteDialogOpen, setDeleteDialogOpen] = useState(false);
  const [structureToDelete, setStructureToDelete] = useState<DataStructure | null>(null);

  useEffect(() => {
    fetchStructures();
    fetchBusinessComponents();
  }, []);

  useEffect(() => {
    filterStructures();
  }, [structures, searchTerm, selectedBusinessComponent, selectedUsage]);

  const fetchStructures = async () => {
    try {
      setLoading(true);
      console.log('Fetching data structures...');
      const response = await api.get('/structures');
      console.log('Structures response:', response);
      
      if (response.success && response.data) {
        const structuresData = response.data.structures || response.data || [];
        setStructures(Array.isArray(structuresData) ? structuresData : []);
      } else {
        setStructures([]);
      }
    } catch (error) {
      console.error('Error fetching structures:', error);
      toast({ title: "Error", description: 'Failed to fetch data structures', variant: "destructive" });
      setStructures([]);
    } finally {
      setLoading(false);
    }
  };

  const fetchBusinessComponents = async () => {
    try {
      const response = await api.get('/business-components');
      if (response.success && response.data) {
        setBusinessComponents(Array.isArray(response.data) ? response.data : []);
      }
    } catch (error) {
      console.error('Error fetching business components:', error);
      setBusinessComponents([]);
    }
  };

  const filterStructures = () => {
    let filtered = [...structures];

    // Search filter
    if (searchTerm) {
      filtered = filtered.filter(structure =>
        structure.name.toLowerCase().includes(searchTerm.toLowerCase()) ||
        structure.description?.toLowerCase().includes(searchTerm.toLowerCase()) ||
        structure.tags?.some(tag => tag.toLowerCase().includes(searchTerm.toLowerCase()))
      );
    }

    // Business component filter
    if (selectedBusinessComponent !== 'all') {
      filtered = filtered.filter(structure => 
        structure.businessComponentId === selectedBusinessComponent
      );
    }

    // Usage filter (source/target)
    if (selectedUsage !== 'all') {
      filtered = filtered.filter(structure => 
        structure.usage === selectedUsage || structure.usage === 'both'
      );
    }

    setFilteredStructures(filtered);
  };

  const handleRefresh = () => {
    fetchStructures();
    toast({ title: "Success", description: 'Data structures refreshed' });
  };

  const handleViewStructure = (structureId: string) => {
    navigate(`/data-structures/${structureId}`);
  };
  
  const handleEditStructure = (structure: DataStructure) => {
    navigate(`/data-structures/${structure.id}`, { 
      state: { structure, isEdit: true } 
    });
  };
  
  const handleDeleteClick = (structure: DataStructure) => {
    setStructureToDelete(structure);
    setDeleteDialogOpen(true);
  };
  
  const handleDeleteConfirm = async () => {
    if (!structureToDelete) return;
    
    try {
      const response = await api.delete(`/structures/${structureToDelete.id}`);
      if (response.success) {
        toast({ title: "Success", description: 'Data structure deleted successfully' });
        fetchStructures();
      } else {
        toast({ title: "Error", description: 'Failed to delete data structure', variant: "destructive" });
      }
    } catch (error) {
      console.error('Error deleting structure:', error);
      toast({ title: "Error", description: 'Failed to delete data structure', variant: "destructive" });
    } finally {
      setDeleteDialogOpen(false);
      setStructureToDelete(null);
    }
  };

  const getTypeBadge = (type: string) => {
    const typeColors: Record<string, string> = {
      json: 'bg-green-500',
      xml: 'bg-blue-500',
      xsd: 'bg-purple-500',
      wsdl: 'bg-orange-500',
      edmx: 'bg-pink-500',
      custom: 'bg-gray-500'
    };

    return (
      <Badge className={`${typeColors[type] || 'bg-gray-500'} text-white`}>
        {type.toUpperCase()}
      </Badge>
    );
  };

  const getUsageBadge = (usage: string) => {
    const usageColors = {
      source: 'bg-blue-500',
      target: 'bg-purple-500',
      both: 'bg-green-500'
    };

    const usageLabels = {
      source: 'Source',
      target: 'Target',
      both: 'Source & Target'
    };

    return (
      <Badge variant="outline" className={`${usageColors[usage as keyof typeof usageColors] || ''}`}>
        {usageLabels[usage as keyof typeof usageLabels] || usage}
      </Badge>
    );
  };

  return (
    <PageContainer>
      <PageHeader 
        title="Data Structures"
        description="Manage and view all data structures"
        icon={<Layers />}
        actions={
          isDevelopment && (
            <Button onClick={() => navigate('/create-data-structure')}>
              <Plus className="h-4 w-4 mr-2" />
              Create Structure
            </Button>
          )
        }
      />

      <Card>
        <CardHeader>
          <CardTitle className="flex items-center gap-2">
            <Filter className="h-5 w-5" />
            Filters
          </CardTitle>
        </CardHeader>
        <CardContent>
          <div className="grid grid-cols-1 md:grid-cols-3 gap-4">
            <div className="relative">
              <Search className="absolute left-3 top-1/2 transform -translate-y-1/2 h-4 w-4 text-muted-foreground" />
              <Input
                placeholder="Search by name or tags..."
                value={searchTerm}
                onChange={(e) => setSearchTerm(e.target.value)}
                className="pl-10"
              />
            </div>

            <Select value={selectedBusinessComponent} onValueChange={setSelectedBusinessComponent}>
              <SelectTrigger>
                <SelectValue placeholder="All Business Components" />
              </SelectTrigger>
              <SelectContent>
                <SelectItem value="all">All Business Components</SelectItem>
                {businessComponents.map((bc) => (
                  <SelectItem key={bc.id} value={bc.id}>
                    {bc.name}
                  </SelectItem>
                ))}
              </SelectContent>
            </Select>

            <Select value={selectedUsage} onValueChange={setSelectedUsage}>
              <SelectTrigger>
                <SelectValue placeholder="All Types" />
              </SelectTrigger>
              <SelectContent>
                <SelectItem value="all">All Types</SelectItem>
                <SelectItem value="source">Source</SelectItem>
                <SelectItem value="target">Target</SelectItem>
              </SelectContent>
            </Select>
          </div>
        </CardContent>
      </Card>

      <Card>
        <CardHeader>
          <div className="flex justify-between items-center">
            <CardTitle className="flex items-center gap-2">
              <Database className="h-5 w-5" />
              Structures ({filteredStructures.length})
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
              <p className="text-muted-foreground">Loading data structures...</p>
            </div>
          ) : filteredStructures.length === 0 ? (
            <div className="text-center py-8">
              <Database className="h-12 w-12 text-muted-foreground mx-auto mb-4" />
              <p className="text-muted-foreground">No data structures found</p>
              {isDevelopment && (
                <Button 
                  variant="outline" 
                  className="mt-4"
                  onClick={() => navigate('/create-data-structure')}
                >
                  <Plus className="h-4 w-4 mr-2" />
                  Create your first structure
                </Button>
              )}
            </div>
          ) : (
            <div className="overflow-x-auto">
              <Table>
              <TableHeader>
                <TableRow>
                  <TableHead>Name</TableHead>
                  <TableHead>Type</TableHead>
                  <TableHead>Usage</TableHead>
                  <TableHead>Business Component</TableHead>
                  <TableHead>Tags</TableHead>
                  <TableHead>Created</TableHead>
                  <TableHead>Actions</TableHead>
                </TableRow>
              </TableHeader>
              <TableBody>
                {filteredStructures.map((structure) => (
                  <TableRow 
                    key={structure.id} 
                    className="cursor-pointer hover:bg-muted/50"
                    onClick={() => handleViewStructure(structure.id)}
                  >
                    <TableCell className="font-medium">
                      <div>
                        <p>{structure.name}</p>
                        {structure.description && (
                          <p className="text-sm text-muted-foreground">{structure.description}</p>
                        )}
                      </div>
                    </TableCell>
                    <TableCell>{getTypeBadge(structure.type)}</TableCell>
                    <TableCell>{getUsageBadge(structure.usage)}</TableCell>
                    <TableCell>{structure.businessComponentName || '-'}</TableCell>
                    <TableCell>
                      <div className="flex flex-wrap gap-1">
                        {structure.tags?.map((tag, index) => (
                          <Badge key={index} variant="secondary" className="text-xs">
                            {tag}
                          </Badge>
                        ))}
                      </div>
                    </TableCell>
                    <TableCell>
                      {new Date(structure.createdAt).toLocaleDateString()}
                    </TableCell>
                    <TableCell onClick={(e) => e.stopPropagation()}>
                      <DropdownMenu>
                        <DropdownMenuTrigger asChild>
                          <Button variant="ghost" size="sm">
                            <MoreHorizontal className="h-4 w-4" />
                          </Button>
                        </DropdownMenuTrigger>
                        <DropdownMenuContent align="end">
                          <DropdownMenuItem
                            onClick={() => handleViewStructure(structure.id)}
                          >
                            View Details
                          </DropdownMenuItem>
                          {isDevelopment && (
                            <>
                              <DropdownMenuItem
                                onClick={() => handleEditStructure(structure)}
                              >
                                <Edit2 className="h-4 w-4 mr-2" />
                                Edit
                              </DropdownMenuItem>
                              <DropdownMenuSeparator />
                              <DropdownMenuItem
                                onClick={() => handleDeleteClick(structure)}
                                className="text-destructive"
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
                ))}
              </TableBody>
              </Table>
            </div>
          )}
        </CardContent>
      </Card>
      
      <AlertDialog open={deleteDialogOpen} onOpenChange={setDeleteDialogOpen}>
        <AlertDialogContent>
          <AlertDialogHeader>
            <AlertDialogTitle>Delete Data Structure</AlertDialogTitle>
            <AlertDialogDescription>
              Are you sure you want to delete "{structureToDelete?.name}"? This action cannot be undone.
            </AlertDialogDescription>
          </AlertDialogHeader>
          <AlertDialogFooter>
            <AlertDialogCancel>Cancel</AlertDialogCancel>
            <AlertDialogAction
              onClick={handleDeleteConfirm}
              className="bg-destructive text-destructive-foreground hover:bg-destructive/90"
            >
              Delete
            </AlertDialogAction>
          </AlertDialogFooter>
        </AlertDialogContent>
      </AlertDialog>
    </PageContainer>
  );
};