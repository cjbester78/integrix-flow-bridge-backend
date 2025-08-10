import { useState, useEffect } from 'react';
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '@/components/ui/card';
import { Button } from '@/components/ui/button';
import { Input } from '@/components/ui/input';
import { Label } from '@/components/ui/label';
import { Textarea } from '@/components/ui/textarea';
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from '@/components/ui/select';
import { Checkbox } from '@/components/ui/checkbox';
import { Badge } from '@/components/ui/badge';
import { Dialog, DialogContent, DialogDescription, DialogFooter, DialogHeader, DialogTitle, DialogTrigger } from '@/components/ui/dialog';
import { Table, TableBody, TableCell, TableHead, TableHeader, TableRow } from '@/components/ui/table';
import { EmptyState } from '@/components/ui/empty-state';
import { useToast } from '@/hooks/use-toast';
import { Pencil, Trash2, Plus, Network, Shield, FolderOpen, MessageSquare, Database, Globe, Mail, FileText, Cpu, Activity, Cloud, Inbox, Download, HardDrive, Zap } from 'lucide-react';

interface AdapterType {
  id: number;
  name: string;
  displayName: string;
  description: string;
  category: 'FILE_TRANSFER' | 'MESSAGING' | 'DATABASE' | 'WEB_SERVICE' | 'MAIL' | 'OTHER';
  iconName: string;
  isActive: boolean;
  supportsSender: boolean;
  supportsReceiver: boolean;
  configurationSchema?: object;
  createdAt: string;
  updatedAt: string;
}

const categoryOptions = [
  { value: 'FILE_TRANSFER', label: 'File Transfer' },
  { value: 'MESSAGING', label: 'Messaging' },
  { value: 'DATABASE', label: 'Database' },
  { value: 'WEB_SERVICE', label: 'Web Service' },
  { value: 'MAIL', label: 'Mail' },
  { value: 'OTHER', label: 'Other' }
];

const iconOptions = [
  { value: 'network', icon: Network },
  { value: 'shield', icon: Shield },
  { value: 'folder', icon: FolderOpen },
  { value: 'message-square', icon: MessageSquare },
  { value: 'database', icon: Database },
  { value: 'globe', icon: Globe },
  { value: 'mail', icon: Mail },
  { value: 'file-text', icon: FileText },
  { value: 'cpu', icon: Cpu },
  { value: 'activity', icon: Activity },
  { value: 'cloud', icon: Cloud },
  { value: 'inbox', icon: Inbox },
  { value: 'download', icon: Download },
  { value: 'hard-drive', icon: HardDrive },
  { value: 'zap', icon: Zap }
];

export const AdapterTypesManagement = () => {
  const [adapterTypes, setAdapterTypes] = useState<AdapterType[]>([]);
  const [loading, setLoading] = useState(true);
  const [dialogOpen, setDialogOpen] = useState(false);
  const [editingType, setEditingType] = useState<AdapterType | null>(null);
  const { toast } = useToast();

  const [formData, setFormData] = useState({
    name: '',
    displayName: '',
    description: '',
    category: '' as AdapterType['category'],
    iconName: 'network',
    isActive: true,
    supportsSender: true,
    supportsReceiver: true,
    configurationSchema: ''
  });

  useEffect(() => {
    fetchAdapterTypes();
  }, []);

  const fetchAdapterTypes = async () => {
    try {
      setLoading(true);
      // Replace with your actual API endpoint
      const response = await fetch('/api/admin/adapter-types');
      if (response.ok) {
        const data = await response.json();
        setAdapterTypes(data || []);
      } else if (response.status === 404) {
        // No data found, set empty array
        setAdapterTypes([]);
      } else {
        throw new Error('Failed to fetch adapter types');
      }
    } catch (error) {
      // For now, just set empty array and don't show error for missing data
      console.log('API not available yet, starting with empty data:', error);
      setAdapterTypes([]);
    } finally {
      setLoading(false);
    }
  };

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    
    try {
      const payload = {
        ...formData,
        configurationSchema: formData.configurationSchema ? JSON.parse(formData.configurationSchema) : null
      };

      const url = editingType 
        ? `/api/admin/adapter-types/${editingType.id}`
        : '/api/admin/adapter-types';
      
      const method = editingType ? 'PUT' : 'POST';
      
      const response = await fetch(url, {
        method,
        headers: {
          'Content-Type': 'application/json',
        },
        body: JSON.stringify(payload),
      });

      if (response.ok) {
        toast({
          title: "Success",
          description: `Adapter type ${editingType ? 'updated' : 'created'} successfully`,
        });
        
        setDialogOpen(false);
        resetForm();
        fetchAdapterTypes();
      } else {
        throw new Error(`Failed to ${editingType ? 'update' : 'create'} adapter type`);
      }
    } catch (error) {
      toast({
        title: "Error",
        description: `Failed to ${editingType ? 'update' : 'create'} adapter type`,
        variant: "destructive"
      });
      console.error('Error saving adapter type:', error);
    }
  };

  const handleEdit = (adapterType: AdapterType) => {
    setEditingType(adapterType);
    setFormData({
      name: adapterType.name,
      displayName: adapterType.displayName,
      description: adapterType.description,
      category: adapterType.category,
      iconName: adapterType.iconName,
      isActive: adapterType.isActive,
      supportsSender: adapterType.supportsSender,
      supportsReceiver: adapterType.supportsReceiver,
      configurationSchema: adapterType.configurationSchema ? JSON.stringify(adapterType.configurationSchema, null, 2) : ''
    });
    setDialogOpen(true);
  };

  const handleDelete = async (id: number) => {
    if (!confirm('Are you sure you want to delete this adapter type?')) {
      return;
    }

    try {
      const response = await fetch(`/api/admin/adapter-types/${id}`, {
        method: 'DELETE',
      });

      if (response.ok) {
        toast({
          title: "Success",
          description: "Adapter type deleted successfully",
        });
        fetchAdapterTypes();
      } else {
        throw new Error('Failed to delete adapter type');
      }
    } catch (error) {
      toast({
        title: "Error",
        description: "Failed to delete adapter type",
        variant: "destructive"
      });
      console.error('Error deleting adapter type:', error);
    }
  };

  const resetForm = () => {
    setFormData({
      name: '',
      displayName: '',
      description: '',
      category: '' as AdapterType['category'],
      iconName: 'network',
      isActive: true,
      supportsSender: true,
      supportsReceiver: true,
      configurationSchema: ''
    });
    setEditingType(null);
  };

  const getIcon = (iconName: string) => {
    const iconOption = iconOptions.find(option => option.value === iconName);
    return iconOption ? iconOption.icon : Network;
  };

  const getCategoryColor = (category: string) => {
    const colors = {
      FILE_TRANSFER: 'bg-blue-100 text-blue-800',
      MESSAGING: 'bg-green-100 text-green-800',
      DATABASE: 'bg-purple-100 text-purple-800',
      WEB_SERVICE: 'bg-orange-100 text-orange-800',
      MAIL: 'bg-red-100 text-red-800',
      OTHER: 'bg-gray-100 text-gray-800'
    };
    return colors[category as keyof typeof colors] || colors.OTHER;
  };

  return (
    <Card>
      <CardHeader>
        <div className="flex items-center justify-between">
          <div>
            <CardTitle>Adapter Types Management</CardTitle>
            <CardDescription>
              Manage available adapter types for communication channels
            </CardDescription>
          </div>
          
          <Dialog open={dialogOpen} onOpenChange={setDialogOpen}>
            <DialogTrigger asChild>
              <Button onClick={resetForm}>
                <Plus className="h-4 w-4 mr-2" />
                Add Adapter Type
              </Button>
            </DialogTrigger>
            
            <DialogContent className="max-w-2xl max-h-[90vh] overflow-y-auto">
              <DialogHeader>
                <DialogTitle>
                  {editingType ? 'Edit Adapter Type' : 'Add New Adapter Type'}
                </DialogTitle>
                <DialogDescription>
                  Configure the adapter type settings and capabilities
                </DialogDescription>
              </DialogHeader>
              
              <form onSubmit={handleSubmit} className="space-y-4">
                <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
                  <div className="space-y-2">
                    <Label htmlFor="name">Name (Internal)*</Label>
                    <Input
                      id="name"
                      value={formData.name}
                      onChange={(e) => setFormData({ ...formData, name: e.target.value })}
                      placeholder="e.g., sftp, rest, jms"
                      required
                    />
                  </div>
                  
                  <div className="space-y-2">
                    <Label htmlFor="displayName">Display Name*</Label>
                    <Input
                      id="displayName"
                      value={formData.displayName}
                      onChange={(e) => setFormData({ ...formData, displayName: e.target.value })}
                      placeholder="e.g., SFTP Adapter, REST API"
                      required
                    />
                  </div>
                  
                  <div className="space-y-2 md:col-span-2">
                    <Label htmlFor="description">Description</Label>
                    <Textarea
                      id="description"
                      value={formData.description}
                      onChange={(e) => setFormData({ ...formData, description: e.target.value })}
                      placeholder="Brief description of the adapter type"
                      rows={3}
                    />
                  </div>
                  
                  <div className="space-y-2">
                    <Label htmlFor="category">Category*</Label>
                    <Select value={formData.category} onValueChange={(value) => setFormData({ ...formData, category: value as AdapterType['category'] })}>
                      <SelectTrigger>
                        <SelectValue placeholder="Select category" />
                      </SelectTrigger>
                      <SelectContent>
                        {categoryOptions.map(option => (
                          <SelectItem key={option.value} value={option.value}>
                            {option.label}
                          </SelectItem>
                        ))}
                      </SelectContent>
                    </Select>
                  </div>
                  
                  <div className="space-y-2">
                    <Label htmlFor="iconName">Icon</Label>
                    <Select value={formData.iconName} onValueChange={(value) => setFormData({ ...formData, iconName: value })}>
                      <SelectTrigger>
                        <SelectValue placeholder="Select icon" />
                      </SelectTrigger>
                      <SelectContent>
                        {iconOptions.map(option => {
                          const IconComponent = option.icon;
                          return (
                            <SelectItem key={option.value} value={option.value}>
                              <div className="flex items-center gap-2">
                                <IconComponent className="h-4 w-4" />
                                {option.value}
                              </div>
                            </SelectItem>
                          );
                        })}
                      </SelectContent>
                    </Select>
                  </div>
                </div>
                
                <div className="flex gap-6">
                  <div className="flex items-center space-x-2">
                    <Checkbox
                      id="isActive"
                      checked={formData.isActive}
                      onCheckedChange={(checked) => setFormData({ ...formData, isActive: checked === true })}
                    />
                    <Label htmlFor="isActive">Active</Label>
                  </div>
                  
                  <div className="flex items-center space-x-2">
                    <Checkbox
                      id="supportsSender"
                      checked={formData.supportsSender}
                      onCheckedChange={(checked) => setFormData({ ...formData, supportsSender: checked === true })}
                    />
                    <Label htmlFor="supportsSender">Supports Sender</Label>
                  </div>
                  
                  <div className="flex items-center space-x-2">
                    <Checkbox
                      id="supportsReceiver"
                      checked={formData.supportsReceiver}
                      onCheckedChange={(checked) => setFormData({ ...formData, supportsReceiver: checked === true })}
                    />
                    <Label htmlFor="supportsReceiver">Supports Receiver</Label>
                  </div>
                </div>
                
                <div className="space-y-2">
                  <Label htmlFor="configurationSchema">Configuration Schema (JSON)</Label>
                  <Textarea
                    id="configurationSchema"
                    value={formData.configurationSchema}
                    onChange={(e) => setFormData({ ...formData, configurationSchema: e.target.value })}
                    placeholder="Optional JSON schema for adapter configuration"
                    rows={6}
                    className="font-mono text-sm"
                  />
                </div>
                
                <DialogFooter>
                  <Button type="button" variant="outline" onClick={() => setDialogOpen(false)}>
                    Cancel
                  </Button>
                  <Button type="submit">
                    {editingType ? 'Update' : 'Create'} Adapter Type
                  </Button>
                </DialogFooter>
              </form>
            </DialogContent>
          </Dialog>
        </div>
      </CardHeader>
      
      <CardContent>
        {loading ? (
          <div className="text-center py-8">Loading adapter types...</div>
        ) : adapterTypes.length === 0 ? (
          <EmptyState
            icon={<Network className="h-12 w-12" />}
            title="No adapter types found"
            description="Create custom adapter types to extend the platform with new integration capabilities."
            action={{
              label: "Add Adapter Type",
              onClick: () => {/* Add adapter type handler */}
            }}
          />
        ) : (
          <Table>
            <TableHeader>
              <TableRow>
                <TableHead>Icon</TableHead>
                <TableHead>Name</TableHead>
                <TableHead>Category</TableHead>
                <TableHead>Support</TableHead>
                <TableHead>Status</TableHead>
                <TableHead>Actions</TableHead>
              </TableRow>
            </TableHeader>
            <TableBody>
              {adapterTypes.map((adapterType) => {
                const IconComponent = getIcon(adapterType.iconName);
                return (
                  <TableRow key={adapterType.id}>
                    <TableCell>
                      <IconComponent className="h-5 w-5" />
                    </TableCell>
                    <TableCell>
                      <div>
                        <div className="font-medium">{adapterType.displayName}</div>
                        <div className="text-sm text-muted-foreground">{adapterType.name}</div>
                      </div>
                    </TableCell>
                    <TableCell>
                      <Badge className={getCategoryColor(adapterType.category)}>
                        {adapterType.category.replace('_', ' ')}
                      </Badge>
                    </TableCell>
                    <TableCell>
                      <div className="flex gap-1">
                        {adapterType.supportsSender && (
                          <Badge variant="outline" className="text-xs">Sender</Badge>
                        )}
                        {adapterType.supportsReceiver && (
                          <Badge variant="outline" className="text-xs">Receiver</Badge>
                        )}
                      </div>
                    </TableCell>
                    <TableCell>
                      <Badge variant={adapterType.isActive ? "default" : "secondary"}>
                        {adapterType.isActive ? 'Active' : 'Inactive'}
                      </Badge>
                    </TableCell>
                    <TableCell>
                      <div className="flex gap-2">
                        <Button
                          variant="outline"
                          size="sm"
                          onClick={() => handleEdit(adapterType)}
                        >
                          <Pencil className="h-4 w-4" />
                        </Button>
                        <Button
                          variant="outline"
                          size="sm"
                          onClick={() => handleDelete(adapterType.id)}
                        >
                          <Trash2 className="h-4 w-4" />
                        </Button>
                      </div>
                     </TableCell>
                   </TableRow>
                 );
               })}
             </TableBody>
           </Table>
         )}
      </CardContent>
    </Card>
  );
};