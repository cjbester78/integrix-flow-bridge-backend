import { useState, useEffect } from 'react';
import { Plus, Edit, Trash2, Building2, Mail, Phone } from 'lucide-react';
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '@/components/ui/card';
import { Button } from '@/components/ui/button';
import { Input } from '@/components/ui/input';
import { Label } from '@/components/ui/label';
import { Textarea } from '@/components/ui/textarea';
import { Dialog, DialogContent, DialogDescription, DialogHeader, DialogTitle, DialogTrigger } from '@/components/ui/dialog';
import { AlertDialog, AlertDialogAction, AlertDialogCancel, AlertDialogContent, AlertDialogDescription, AlertDialogFooter, AlertDialogHeader, AlertDialogTitle, AlertDialogTrigger } from '@/components/ui/alert-dialog';
import { EmptyState } from '@/components/ui/empty-state';
import { useToast } from '@/hooks/use-toast';
import { BusinessComponent, CreateBusinessComponentRequest } from '@/types/businessComponent';
import { businessComponentService } from '@/services/businessComponentService';

export const BusinessComponents = () => {
  const [businessComponents, setBusinessComponents] = useState<BusinessComponent[]>([]);
  const [loading, setLoading] = useState(true);
  const [isCreateDialogOpen, setIsCreateDialogOpen] = useState(false);
  const [isEditDialogOpen, setIsEditDialogOpen] = useState(false);
  const [editingBusinessComponent, setEditingBusinessComponent] = useState<BusinessComponent | null>(null);
  const [formData, setFormData] = useState<CreateBusinessComponentRequest>({
    name: '',
    description: '',
    contactEmail: '',
    contactPhone: ''
  });
  const { toast } = useToast();

  useEffect(() => {
    loadBusinessComponents();
  }, []);

  const loadBusinessComponents = async () => {
    const response = await businessComponentService.getAllBusinessComponents();
    if (response.success && response.data) {
      setBusinessComponents(response.data);
    } else {
      toast({
        title: "Error",
        description: response.error || "Failed to load business components",
        variant: "destructive",
      });
    }
    setLoading(false);
  };

  const handleCreate = async () => {
    if (!formData.name.trim()) {
      toast({
        title: "Validation Error",
        description: "Business component name is required",
        variant: "destructive",
      });
      return;
    }

    const response = await businessComponentService.createBusinessComponent(formData);
    if (response.success && response.data) {
      setBusinessComponents([...businessComponents, response.data]);
      setIsCreateDialogOpen(false);
      setFormData({ name: '', description: '', contactEmail: '', contactPhone: '' });
      toast({
        title: "Success",
        description: "Business component created successfully",
      });
    } else {
      toast({
        title: "Error",
        description: response.error || "Failed to create business component",
        variant: "destructive",
      });
    }
  };

  const handleEdit = async () => {
    if (!editingBusinessComponent || !formData.name.trim()) {
      toast({
        title: "Validation Error",
        description: "Business component name is required",
        variant: "destructive",
      });
      return;
    }

    const response = await businessComponentService.updateBusinessComponent({
      id: editingBusinessComponent.id,
      ...formData
    });

    if (response.success && response.data) {
      setBusinessComponents(businessComponents.map(c => c.id === editingBusinessComponent.id ? response.data! : c));
      setIsEditDialogOpen(false);
      setEditingBusinessComponent(null);
      setFormData({ name: '', description: '', contactEmail: '', contactPhone: '' });
      toast({
        title: "Success",
        description: "Business component updated successfully",
      });
    } else {
      toast({
        title: "Error",
        description: response.error || "Failed to update business component",
        variant: "destructive",
      });
    }
  };

  const handleDelete = async (businessComponent: BusinessComponent) => {
    const response = await businessComponentService.deleteBusinessComponent(businessComponent.id);
    if (response.success) {
      setBusinessComponents(businessComponents.filter(c => c.id !== businessComponent.id));
      toast({
        title: "Success",
        description: "Business component deleted successfully",
      });
    } else {
      toast({
        title: "Error",
        description: response.error || "Failed to delete business component",
        variant: "destructive",
      });
    }
  };

  const openEditDialog = (businessComponent: BusinessComponent) => {
    setEditingBusinessComponent(businessComponent);
    setFormData({
      name: businessComponent.name,
      description: businessComponent.description || '',
      contactEmail: businessComponent.contactEmail || '',
      contactPhone: businessComponent.contactPhone || ''
    });
    setIsEditDialogOpen(true);
  };

  const resetForm = () => {
    setFormData({ name: '', description: '', contactEmail: '', contactPhone: '' });
    setEditingBusinessComponent(null);
  };

  if (loading) {
    return (
      <div className="p-6">
        <div className="animate-pulse space-y-6">
          <div className="h-8 bg-muted rounded w-1/3"></div>
          <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
            {[1, 2, 3].map(i => (
              <div key={i} className="h-48 bg-muted rounded"></div>
            ))}
          </div>
        </div>
      </div>
    );
  }

  return (
    <div className="p-6 space-y-6 animate-fade-in">
      <div className="flex items-center justify-between">
        <div>
          <h1 className="text-3xl font-bold text-foreground flex items-center gap-3">
            <Building2 className="h-8 w-8" />
            Business Component Management
          </h1>
          <p className="text-muted-foreground">Manage your business components and organizations</p>
        </div>
        
        <Dialog open={isCreateDialogOpen} onOpenChange={setIsCreateDialogOpen}>
          <DialogTrigger asChild>
            <Button onClick={() => resetForm()}>
              <Plus className="h-4 w-4 mr-2" />
              Add Business Component
            </Button>
          </DialogTrigger>
          <DialogContent>
            <DialogHeader>
              <DialogTitle>Create New Business Component</DialogTitle>
              <DialogDescription>Add a new business component to your system</DialogDescription>
            </DialogHeader>
            <div className="space-y-4">
              <div>
                <Label htmlFor="name">Business Component Name *</Label>
                <Input
                  id="name"
                  value={formData.name}
                  onChange={(e) => setFormData({ ...formData, name: e.target.value })}
                  placeholder="Enter business component name"
                />
              </div>
              <div>
                <Label htmlFor="description">Description</Label>
                <Textarea
                  id="description"
                  value={formData.description}
                  onChange={(e) => setFormData({ ...formData, description: e.target.value })}
                  placeholder="Enter business component description"
                  rows={3}
                />
              </div>
              <div>
                <Label htmlFor="email">Contact Email</Label>
                <Input
                  id="email"
                  type="email"
                  value={formData.contactEmail}
                  onChange={(e) => setFormData({ ...formData, contactEmail: e.target.value })}
                  placeholder="contact@example.com"
                />
              </div>
              <div>
                <Label htmlFor="phone">Contact Phone</Label>
                <Input
                  id="phone"
                  value={formData.contactPhone}
                  onChange={(e) => setFormData({ ...formData, contactPhone: e.target.value })}
                  placeholder="+1-555-0123"
                />
              </div>
              <div className="flex justify-end gap-2">
                <Button variant="outline" onClick={() => setIsCreateDialogOpen(false)}>
                  Cancel
                </Button>
                <Button onClick={handleCreate}>
                  Create Business Component
                </Button>
              </div>
            </div>
          </DialogContent>
        </Dialog>
      </div>

      {businessComponents.length === 0 ? (
        <EmptyState
          icon={<Building2 className="h-12 w-12" />}
          title="No business components yet"
          description="Get started by adding your first business component to organize your integrations."
          action={{
            label: "Add Business Component",
            onClick: () => setIsCreateDialogOpen(true)
          }}
        />
      ) : (
        <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
          {businessComponents.map((businessComponent) => (
            <Card key={businessComponent.id} className="hover:shadow-lg transition-shadow">
              <CardHeader>
                <div className="flex items-center justify-between">
                  <CardTitle className="text-lg">{businessComponent.name}</CardTitle>
                  <div className="flex gap-1">
                    <Button
                      variant="ghost"
                      size="sm"
                      onClick={() => openEditDialog(businessComponent)}
                    >
                      <Edit className="h-4 w-4" />
                    </Button>
                    <AlertDialog>
                      <AlertDialogTrigger asChild>
                        <Button variant="ghost" size="sm">
                          <Trash2 className="h-4 w-4" />
                        </Button>
                      </AlertDialogTrigger>
                      <AlertDialogContent>
                        <AlertDialogHeader>
                          <AlertDialogTitle>Delete Business Component</AlertDialogTitle>
                          <AlertDialogDescription>
                            Are you sure you want to delete "{businessComponent.name}"? This action cannot be undone.
                          </AlertDialogDescription>
                        </AlertDialogHeader>
                        <AlertDialogFooter>
                          <AlertDialogCancel>Cancel</AlertDialogCancel>
                          <AlertDialogAction onClick={() => handleDelete(businessComponent)}>
                            Delete
                          </AlertDialogAction>
                        </AlertDialogFooter>
                      </AlertDialogContent>
                    </AlertDialog>
                  </div>
                </div>
                {businessComponent.description && (
                  <CardDescription>{businessComponent.description}</CardDescription>
                )}
              </CardHeader>
              <CardContent>
                <div className="space-y-2">
                  {businessComponent.contactEmail && (
                    <div className="flex items-center gap-2 text-sm text-muted-foreground">
                      <Mail className="h-4 w-4" />
                      {businessComponent.contactEmail}
                    </div>
                  )}
                  {businessComponent.contactPhone && (
                    <div className="flex items-center gap-2 text-sm text-muted-foreground">
                      <Phone className="h-4 w-4" />
                      {businessComponent.contactPhone}
                    </div>
                  )}
                  <div className="text-xs text-muted-foreground pt-2">
                    Created: {new Date(businessComponent.createdAt).toLocaleDateString()}
                  </div>
                </div>
              </CardContent>
            </Card>
          ))}
        </div>
      )}

      {/* Edit Dialog */}
      <Dialog open={isEditDialogOpen} onOpenChange={setIsEditDialogOpen}>
        <DialogContent>
          <DialogHeader>
            <DialogTitle>Edit Business Component</DialogTitle>
            <DialogDescription>Update business component information</DialogDescription>
          </DialogHeader>
          <div className="space-y-4">
            <div>
              <Label htmlFor="edit-name">Business Component Name *</Label>
              <Input
                id="edit-name"
                value={formData.name}
                onChange={(e) => setFormData({ ...formData, name: e.target.value })}
                placeholder="Enter business component name"
              />
            </div>
            <div>
              <Label htmlFor="edit-description">Description</Label>
              <Textarea
                id="edit-description"
                value={formData.description}
                onChange={(e) => setFormData({ ...formData, description: e.target.value })}
                placeholder="Enter business component description"
                rows={3}
              />
            </div>
            <div>
              <Label htmlFor="edit-email">Contact Email</Label>
              <Input
                id="edit-email"
                type="email"
                value={formData.contactEmail}
                onChange={(e) => setFormData({ ...formData, contactEmail: e.target.value })}
                placeholder="contact@example.com"
              />
            </div>
            <div>
              <Label htmlFor="edit-phone">Contact Phone</Label>
              <Input
                id="edit-phone"
                value={formData.contactPhone}
                onChange={(e) => setFormData({ ...formData, contactPhone: e.target.value })}
                placeholder="+1-555-0123"
              />
            </div>
            <div className="flex justify-end gap-2">
              <Button variant="outline" onClick={() => setIsEditDialogOpen(false)}>
                Cancel
              </Button>
              <Button onClick={handleEdit}>
                Update Business Component
              </Button>
            </div>
          </div>
        </DialogContent>
      </Dialog>
    </div>
  );
};