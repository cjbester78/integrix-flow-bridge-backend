// @ts-nocheck
import { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { 
  Plus, 
  Filter, 
  Search, 
  Send, 
  RefreshCw, 
  MoreVertical, 
  Edit, 
  Trash2, 
  Copy, 
  TestTube,
  Power,
  PowerOff,
  CheckCircle,
  XCircle,
  Loader2
} from 'lucide-react';
import { Button } from '@/components/ui/button';
import { Input } from '@/components/ui/input';
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
import { Badge } from '@/components/ui/badge';
import { useToast } from '@/hooks/use-toast';
import { api } from '@/services/api';
import { adapterService } from '@/services/adapter';
import { useEnvironmentPermissions } from '@/hooks/useEnvironmentPermissions-no-query';

interface CommunicationAdapter {
  id: string;
  name: string;
  type: string;
  mode: string;
  status?: string;
  active?: boolean;
  businessComponentId: string;
  businessComponentName?: string;
  description?: string;
  createdAt: string;
  updatedAt: string;
}

interface BusinessComponent {
  id: string;
  name: string;
}

const adapterTypes = [
  'FILE', 'FTP', 'HTTP', 'IDOC', 'JDBC', 'JMS', 
  'MAIL', 'ODATA', 'REST', 'RFC', 'SFTP', 'SOAP'
];

const adapterModes = ['SENDER', 'RECEIVER'];

export default function CommunicationAdapters() {
  const navigate = useNavigate();
  const { isDevelopment } = useEnvironmentPermissions();
  const [adapters, setAdapters] = useState<CommunicationAdapter[]>([]);
  const [filteredAdapters, setFilteredAdapters] = useState<CommunicationAdapter[]>([]);
  const [businessComponents, setBusinessComponents] = useState<BusinessComponent[]>([]);
  const [loading, setLoading] = useState(true);
  
  // Filter states
  const [searchTerm, setSearchTerm] = useState('');
  const [selectedBusinessComponent, setSelectedBusinessComponent] = useState<string>('all');
  const [selectedType, setSelectedType] = useState<string>('all');
  const [selectedMode, setSelectedMode] = useState<string>('all');
  
  // Dialog states
  const [deleteDialogOpen, setDeleteDialogOpen] = useState(false);
  const [adapterToDelete, setAdapterToDelete] = useState<CommunicationAdapter | null>(null);
  const [testingAdapter, setTestingAdapter] = useState<string | null>(null);

  useEffect(() => {
    fetchAdapters();
    fetchBusinessComponents();
  }, []);

  useEffect(() => {
    filterAdapters();
  }, [adapters, searchTerm, selectedBusinessComponent, selectedType, selectedMode]);

  const fetchAdapters = async () => {
    try {
      setLoading(true);
      console.log('Fetching adapters...');
      const response = await api.get('/adapters');
      console.log('Adapters response:', response);
      if (response.success && response.data) {
        setAdapters(Array.isArray(response.data) ? response.data : []);
      } else {
        console.warn('No adapter data in response');
        setAdapters([]);
      }
    } catch (error) {
      console.error('Error fetching adapters:', error);
      toast({ title: "Error", description: 'Failed to load communication adapters', variant: "destructive" });
      setAdapters([]);
    } finally {
      setLoading(false);
    }
  };

  const fetchBusinessComponents = async () => {
    try {
      console.log('Fetching business components...');
      const response = await api.get('/business-components');
      console.log('Business components response:', response);
      if (response.success && response.data) {
        setBusinessComponents(Array.isArray(response.data) ? response.data : []);
      } else {
        setBusinessComponents([]);
      }
    } catch (error) {
      console.error('Error fetching business components:', error);
      setBusinessComponents([]);
    }
  };

  const filterAdapters = () => {
    let filtered = [...adapters];

    // Search filter
    if (searchTerm) {
      filtered = filtered.filter(adapter =>
        adapter.name.toLowerCase().includes(searchTerm.toLowerCase()) ||
        adapter.description?.toLowerCase().includes(searchTerm.toLowerCase())
      );
    }

    // Business component filter
    if (selectedBusinessComponent !== 'all') {
      filtered = filtered.filter(adapter => 
        adapter.businessComponentId === selectedBusinessComponent
      );
    }

    // Type filter
    if (selectedType !== 'all') {
      filtered = filtered.filter(adapter => adapter.type === selectedType);
    }

    // Mode filter
    if (selectedMode !== 'all') {
      filtered = filtered.filter(adapter => adapter.mode === selectedMode);
    }

    setFilteredAdapters(filtered);
  };

  const handleRefresh = () => {
    fetchAdapters();
    toast({ title: "Success", description: 'Adapters refreshed' });
  };

  const handleViewAdapter = (adapterId: string) => {
    navigate(`/communication-adapters/${adapterId}`);
  };

  const handleEditAdapter = (adapter: CommunicationAdapter) => {
    navigate(`/create-communication-adapter`, { state: { adapter, isEdit: true } });
  };

  const handleCloneAdapter = async (adapter: CommunicationAdapter) => {
    try {
      const response = await adapterService.cloneAdapter(adapter.id, `${adapter.name} (Copy)`);
      if (response.success) {
        toast({ title: "Success", description: 'Adapter cloned successfully' });
        fetchAdapters(); // Refresh the list
      } else {
        throw new Error(response.error || 'Failed to clone adapter');
      }
    } catch (error) {
      toast({ title: "Error", description: error instanceof Error ? error.message : 'Failed to clone adapter', variant: "destructive" });
    }
  };

  const handleDeleteAdapter = async () => {
    if (!adapterToDelete) return;

    try {
      const response = await adapterService.deleteAdapter(adapterToDelete.id);
      if (response.success) {
        toast({ title: "Success", description: 'Adapter deleted successfully' });
        setAdapters(adapters.filter(a => a.id !== adapterToDelete.id));
        setDeleteDialogOpen(false);
        setAdapterToDelete(null);
      } else {
        throw new Error(response.error || 'Failed to delete adapter');
      }
    } catch (error) {
      toast({ title: "Error", description: error instanceof Error ? error.message : 'Failed to delete adapter', variant: "destructive" });
    }
  };

  const handleTestConnection = async (adapter: CommunicationAdapter) => {
    try {
      setTestingAdapter(adapter.id);
      const response = await adapterService.testAdapter(adapter.id, '{}');
      
      if (response.success && response.data) {
        toast({ title: "Success", description: response.data.message || 'Connection test successful' });
      } else {
        throw new Error(response.error || 'Connection test failed');
      }
    } catch (error) {
      toast({ title: "Error", description: error instanceof Error ? error.message : 'Failed to test adapter connection', variant: "destructive" });
    } finally {
      setTestingAdapter(null);
    }
  };

  const handleToggleActive = async (adapter: CommunicationAdapter) => {
    try {
      const updates = { ...adapter, active: !adapter.active };
      const response = await adapterService.updateAdapter(adapter.id, updates);
      
      if (response.success) {
        toast({ title: "Success", description: `Adapter ${adapter.active ? 'deactivated' : 'activated'} successfully` });
        fetchAdapters(); // Refresh the list
      } else {
        throw new Error(response.error || 'Failed to update adapter');
      }
    } catch (error) {
      toast({ title: "Error", description: error instanceof Error ? error.message : 'Failed to update adapter status', variant: "destructive" });
    }
  };

  const getStatusBadge = (active?: boolean) => {
    return (
      <Badge className={active ? 'bg-green-500' : 'bg-gray-500'}>
        {active ? 'Active' : 'Inactive'}
      </Badge>
    );
  };

  const getModeBadge = (mode: string) => {
    const modeColors = {
      SENDER: 'bg-blue-500',
      RECEIVER: 'bg-purple-500'
    };

    const modeLabels = {
      SENDER: 'Sender (Inbound)',
      RECEIVER: 'Receiver (Outbound)'
    };

    return (
      <Badge variant="outline" className={`${modeColors[mode as keyof typeof modeColors] || ''}`}>
        {modeLabels[mode as keyof typeof modeLabels] || mode}
      </Badge>
    );
  };

  return (
    <div className="p-8 space-y-6">
      <div className="flex justify-between items-center">
        <div>
          <h1 className="text-3xl font-bold">Communication Adapters</h1>
          <p className="text-muted-foreground mt-2">
            Manage and monitor all communication adapters
          </p>
        </div>
        {isDevelopment && (
          <Button onClick={() => navigate('/create-communication-adapter')}>
            <Plus className="h-4 w-4 mr-2" />
            Create Adapter
          </Button>
        )}
      </div>

      <Card>
        <CardHeader>
          <CardTitle className="flex items-center gap-2">
            <Filter className="h-5 w-5" />
            Filters
          </CardTitle>
        </CardHeader>
        <CardContent>
          <div className="grid grid-cols-1 md:grid-cols-4 gap-4">
            <div className="relative">
              <Search className="absolute left-3 top-1/2 transform -translate-y-1/2 text-gray-400 h-4 w-4" />
              <Input
                placeholder="Search by name..."
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

            <Select value={selectedType} onValueChange={setSelectedType}>
              <SelectTrigger>
                <SelectValue placeholder="All Types" />
              </SelectTrigger>
              <SelectContent>
                <SelectItem value="all">All Types</SelectItem>
                {adapterTypes.map((type) => (
                  <SelectItem key={type} value={type}>
                    {type}
                  </SelectItem>
                ))}
              </SelectContent>
            </Select>

            <Select value={selectedMode} onValueChange={setSelectedMode}>
              <SelectTrigger>
                <SelectValue placeholder="All Modes" />
              </SelectTrigger>
              <SelectContent>
                <SelectItem value="all">All Modes</SelectItem>
                {adapterModes.map((mode) => (
                  <SelectItem key={mode} value={mode}>
                    {mode}
                  </SelectItem>
                ))}
              </SelectContent>
            </Select>
          </div>
        </CardContent>
      </Card>

      <Card>
        <CardHeader>
          <div className="flex justify-between items-center">
            <CardTitle className="flex items-center gap-2">
              <Send className="h-5 w-5" />
              Adapters ({filteredAdapters.length})
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
              <p className="text-muted-foreground">Loading adapters...</p>
            </div>
          ) : filteredAdapters.length === 0 ? (
            <div className="text-center py-8">
              <p className="text-muted-foreground">No adapters found</p>
            </div>
          ) : (
            <div className="overflow-x-auto">
              <Table>
                <TableHeader>
                  <TableRow>
                    <TableHead>Name</TableHead>
                    <TableHead>Type</TableHead>
                    <TableHead>Mode</TableHead>
                    <TableHead>Status</TableHead>
                    <TableHead>Business Component</TableHead>
                    <TableHead>Created</TableHead>
                    <TableHead>Actions</TableHead>
                  </TableRow>
                </TableHeader>
                <TableBody>
                  {filteredAdapters.map((adapter) => (
                    <TableRow key={adapter.id}>
                      <TableCell className="font-medium">{adapter.name}</TableCell>
                      <TableCell>
                        <Badge variant="outline">{adapter.type}</Badge>
                      </TableCell>
                      <TableCell>{getModeBadge(adapter.mode)}</TableCell>
                      <TableCell>{getStatusBadge(adapter.active)}</TableCell>
                      <TableCell>
                        {businessComponents.find(bc => bc.id === adapter.businessComponentId)?.name || '-'}
                      </TableCell>
                      <TableCell>
                        {new Date(adapter.createdAt).toLocaleDateString()}
                      </TableCell>
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
                            <DropdownMenuItem onClick={() => handleViewAdapter(adapter.id)}>
                              View Details
                            </DropdownMenuItem>
                            {isDevelopment && (
                              <>
                                <DropdownMenuItem onClick={() => handleEditAdapter(adapter)}>
                                  <Edit className="h-4 w-4 mr-2" />
                                  Edit
                                </DropdownMenuItem>
                                <DropdownMenuItem 
                                  onClick={() => handleTestConnection(adapter)}
                                  disabled={testingAdapter === adapter.id}
                                >
                                  {testingAdapter === adapter.id ? (
                                    <Loader2 className="h-4 w-4 mr-2 animate-spin" />
                                  ) : (
                                    <TestTube className="h-4 w-4 mr-2" />
                                  )}
                                  Test Connection
                                </DropdownMenuItem>
                                <DropdownMenuItem onClick={() => handleToggleActive(adapter)}>
                                  {adapter.active ? (
                                    <>
                                      <PowerOff className="h-4 w-4 mr-2" />
                                      Deactivate
                                    </>
                                  ) : (
                                    <>
                                      <Power className="h-4 w-4 mr-2" />
                                      Activate
                                    </>
                                  )}
                                </DropdownMenuItem>
                                <DropdownMenuItem onClick={() => handleCloneAdapter(adapter)}>
                                  <Copy className="h-4 w-4 mr-2" />
                                  Clone
                                </DropdownMenuItem>
                                <DropdownMenuSeparator />
                                <DropdownMenuItem
                                  onClick={() => {
                                    setAdapterToDelete(adapter);
                                    setDeleteDialogOpen(true);
                                  }}
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
                  ))}
                </TableBody>
              </Table>
            </div>
          )}
        </CardContent>
      </Card>

      {/* Delete Confirmation Dialog */}
      <Dialog open={deleteDialogOpen} onOpenChange={setDeleteDialogOpen}>
        <DialogContent>
          <DialogHeader>
            <DialogTitle>Delete Adapter</DialogTitle>
            <DialogDescription>
              Are you sure you want to delete "{adapterToDelete?.name}"? This action cannot be undone.
            </DialogDescription>
          </DialogHeader>
          <DialogFooter>
            <Button
              variant="outline"
              onClick={() => {
                setDeleteDialogOpen(false);
                setAdapterToDelete(null);
              }}
            >
              Cancel
            </Button>
            <Button
              variant="destructive"
              onClick={handleDeleteAdapter}
            >
              Delete
            </Button>
          </DialogFooter>
        </DialogContent>
      </Dialog>
    </div>
  );
}