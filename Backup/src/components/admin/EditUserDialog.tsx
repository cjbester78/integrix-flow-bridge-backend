import { useState, useEffect } from 'react';
import { Dialog, DialogContent, DialogDescription, DialogFooter, DialogHeader, DialogTitle } from '@/components/ui/dialog';
import { Button } from '@/components/ui/button';
import { Input } from '@/components/ui/input';
import { Label } from '@/components/ui/label';
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from '@/components/ui/select';
import { Checkbox } from '@/components/ui/checkbox';
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '@/components/ui/card';
import { Separator } from '@/components/ui/separator';
import { useToast } from '@/hooks/use-toast';
import { userService, UpdateUserRequest } from '@/services/userService';
import { User } from '@/types/admin';
import { Loader2, Shield, Settings, Database, Users, MessageSquare, Radio } from 'lucide-react';

interface EditUserDialogProps {
  open: boolean;
  onOpenChange: (open: boolean) => void;
  user: User | null;
  onUserUpdated?: () => void;
}

interface PermissionCategory {
  key: string;
  label: string;
  icon: typeof Shield;
  permissions: string[];
}

const permissionCategories: PermissionCategory[] = [
  {
    key: 'flows',
    label: 'Flows',
    icon: Settings,
    permissions: ['create', 'read', 'update', 'delete', 'execute']
  },
  {
    key: 'adapters',
    label: 'Adapters',
    icon: Radio,
    permissions: ['create', 'read', 'update', 'delete', 'test']
  },
  {
    key: 'structures',
    label: 'Data Structures',
    icon: Database,
    permissions: ['create', 'read', 'update', 'delete']
  },
  {
    key: 'users',
    label: 'User Management',
    icon: Users,
    permissions: ['create', 'read', 'update', 'delete']
  },
  {
    key: 'system',
    label: 'System',
    icon: Shield,
    permissions: ['create', 'read', 'update', 'delete']
  },
  {
    key: 'messages',
    label: 'Messages',
    icon: MessageSquare,
    permissions: ['read']
  },
  {
    key: 'channels',
    label: 'Channels',
    icon: Radio,
    permissions: ['read']
  },
  {
    key: 'certificates',
    label: 'Certificates',
    icon: Shield,
    permissions: ['create', 'read', 'update', 'delete']
  }
];

export const EditUserDialog = ({ open, onOpenChange, user, onUserUpdated }: EditUserDialogProps) => {
  const [isLoading, setIsLoading] = useState(false);
  const [formData, setFormData] = useState<UpdateUserRequest>({});
  const [customPermissions, setCustomPermissions] = useState<Record<string, string[]>>({});
  const [useCustomPermissions, setUseCustomPermissions] = useState(false);
  const { toast } = useToast();

  // Initialize form data when user changes
  useEffect(() => {
    if (user) {
      setFormData({
        email: user.email,
        firstName: user.first_name,
        lastName: user.last_name,
        role: user.role,
        status: user.status
      });
      
      // Check if user has custom permissions
      if (user.permissions && Object.keys(user.permissions).length > 0) {
        setCustomPermissions(user.permissions);
        setUseCustomPermissions(true);
      } else {
        // Load default role permissions
        const rolePermissions = userService.getRolePermissions(user.role);
        setCustomPermissions(rolePermissions);
        setUseCustomPermissions(false);
      }
    }
  }, [user]);

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!user) return;
    
    setIsLoading(true);

    try {
      // Prepare update data
      const updateData: UpdateUserRequest = {
        ...formData,
        permissions: useCustomPermissions ? customPermissions : undefined
      };

      console.log('Updating user with data:', updateData);
      const response = await userService.updateUser(user.id, updateData);
      
      if (response.success) {
        toast({
          title: "User Updated Successfully",
          description: `User ${user.username} has been updated.`,
        });
        
        onOpenChange(false);
        onUserUpdated?.();
      } else {
        toast({
          title: "Error Updating User",
          description: response.error || "An error occurred while updating the user.",
          variant: "destructive",
        });
      }
    } catch (error: any) {
      console.error('User update error:', error);
      toast({
        title: "Error Updating User",
        description: error.message || "An unexpected error occurred.",
        variant: "destructive",
      });
    } finally {
      setIsLoading(false);
    }
  };

  const handleInputChange = (field: keyof UpdateUserRequest) => (value: string) => {
    setFormData(prev => ({ ...prev, [field]: value }));
    
    // If role changes, update default permissions
    if (field === 'role') {
      const rolePermissions = userService.getRolePermissions(value);
      if (!useCustomPermissions) {
        setCustomPermissions(rolePermissions);
      }
    }
  };

  const handlePermissionToggle = (category: string, permission: string) => {
    setCustomPermissions(prev => {
      const categoryPerms = prev[category] || [];
      const hasPermission = categoryPerms.includes(permission);
      
      return {
        ...prev,
        [category]: hasPermission 
          ? categoryPerms.filter(p => p !== permission)
          : [...categoryPerms, permission]
      };
    });
  };

  const resetToRoleDefaults = () => {
    if (formData.role) {
      const rolePermissions = userService.getRolePermissions(formData.role);
      setCustomPermissions(rolePermissions);
      setUseCustomPermissions(false);
    }
  };

  if (!user) return null;

  return (
    <Dialog open={open} onOpenChange={onOpenChange}>
      <DialogContent className="sm:max-w-[600px] max-h-[80vh] overflow-y-auto">
        <DialogHeader>
          <DialogTitle>Edit User: {user.username}</DialogTitle>
          <DialogDescription>
            Update user information and customize their permissions.
          </DialogDescription>
        </DialogHeader>
        
        <form onSubmit={handleSubmit} className="space-y-6">
          {/* Basic Information */}
          <div className="space-y-4">
            <h3 className="text-lg font-medium">Basic Information</h3>
            
              <div className="grid grid-cols-2 gap-4">
                <div className="space-y-2">
                  <Label htmlFor="firstName">First Name</Label>
                  <Input
                    id="firstName"
                    value={formData.firstName || ''}
                    onChange={(e) => handleInputChange('firstName')(e.target.value)}
                    disabled={isLoading}
                  />
                </div>
                <div className="space-y-2">
                  <Label htmlFor="lastName">Last Name</Label>
                  <Input
                    id="lastName"
                    value={formData.lastName || ''}
                    onChange={(e) => handleInputChange('lastName')(e.target.value)}
                    disabled={isLoading}
                  />
                </div>
              </div>

            <div className="space-y-2">
              <Label htmlFor="email">Email</Label>
              <Input
                id="email"
                type="email"
                value={formData.email || ''}
                onChange={(e) => handleInputChange('email')(e.target.value)}
                disabled={isLoading}
              />
            </div>

            <div className="grid grid-cols-2 gap-4">
              <div className="space-y-2">
                <Label htmlFor="role">Role</Label>
                <Select 
                  value={formData.role || ''} 
                  onValueChange={handleInputChange('role')}
                  disabled={isLoading}
                >
                  <SelectTrigger>
                    <SelectValue placeholder="Select a role" />
                  </SelectTrigger>
                  <SelectContent>
                    <SelectItem value="administrator">Administrator</SelectItem>
                    <SelectItem value="integrator">Integrator</SelectItem>
                    <SelectItem value="viewer">Viewer</SelectItem>
                  </SelectContent>
                </Select>
              </div>
              <div className="space-y-2">
                <Label htmlFor="status">Status</Label>
                <Select 
                  value={formData.status || ''} 
                  onValueChange={handleInputChange('status')}
                  disabled={isLoading}
                >
                  <SelectTrigger>
                    <SelectValue placeholder="Select status" />
                  </SelectTrigger>
                  <SelectContent>
                    <SelectItem value="active">Active</SelectItem>
                    <SelectItem value="inactive">Inactive</SelectItem>
                    <SelectItem value="pending">Pending</SelectItem>
                  </SelectContent>
                </Select>
              </div>
            </div>
          </div>

          <Separator />

          {/* Permissions */}
          <div className="space-y-4">
            <div className="flex items-center justify-between">
              <h3 className="text-lg font-medium">Permissions</h3>
              <div className="flex items-center gap-4">
                <div className="flex items-center space-x-2">
                  <Checkbox
                    id="useCustomPermissions"
                    checked={useCustomPermissions}
                    onCheckedChange={(checked) => setUseCustomPermissions(checked === true)}
                    disabled={isLoading}
                  />
                  <Label htmlFor="useCustomPermissions" className="text-sm">
                    Use custom permissions
                  </Label>
                </div>
                {useCustomPermissions && (
                  <Button
                    type="button"
                    variant="outline"
                    size="sm"
                    onClick={resetToRoleDefaults}
                    disabled={isLoading}
                  >
                    Reset to Role Defaults
                  </Button>
                )}
              </div>
            </div>

            <Card>
              <CardHeader>
                <CardDescription>
                  {useCustomPermissions 
                    ? "Customize individual permissions for this user"
                    : `Using default permissions for ${formData.role || 'selected'} role`
                  }
                </CardDescription>
              </CardHeader>
              <CardContent>
                <div className="grid gap-4">
                  {permissionCategories.map((category) => {
                    const categoryPermissions = customPermissions[category.key] || [];
                    const Icon = category.icon;
                    
                    return (
                      <div key={category.key} className="space-y-2">
                        <div className="flex items-center gap-2">
                          <Icon className="h-4 w-4 text-muted-foreground" />
                          <Label className="font-medium">{category.label}</Label>
                        </div>
                        <div className="flex flex-wrap gap-4 pl-6">
                          {category.permissions.map((permission) => (
                            <div key={permission} className="flex items-center space-x-2">
                              <Checkbox
                                id={`${category.key}-${permission}`}
                                checked={categoryPermissions.includes(permission)}
                                onCheckedChange={() => handlePermissionToggle(category.key, permission)}
                                disabled={isLoading || !useCustomPermissions}
                              />
                              <Label 
                                htmlFor={`${category.key}-${permission}`}
                                className="text-sm capitalize"
                              >
                                {permission}
                              </Label>
                            </div>
                          ))}
                        </div>
                      </div>
                    );
                  })}
                </div>
              </CardContent>
            </Card>
          </div>

          <DialogFooter>
            <Button 
              type="button" 
              variant="outline" 
              onClick={() => onOpenChange(false)}
              disabled={isLoading}
            >
              Cancel
            </Button>
            <Button type="submit" disabled={isLoading}>
              {isLoading && <Loader2 className="h-4 w-4 mr-2 animate-spin" />}
              Update User
            </Button>
          </DialogFooter>
        </form>
      </DialogContent>
    </Dialog>
  );
};