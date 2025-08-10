import { useState, useEffect } from 'react';
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '@/components/ui/card';
import { Button } from '@/components/ui/button';
import { Input } from '@/components/ui/input';
import { Label } from '@/components/ui/label';
import { Textarea } from '@/components/ui/textarea';
import { Badge } from '@/components/ui/badge';
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from '@/components/ui/select';
import { Dialog, DialogContent, DialogDescription, DialogHeader, DialogTitle, DialogTrigger } from '@/components/ui/dialog';
import { useToast } from '@/hooks/use-toast';
import { useAuth } from '@/contexts/AuthContext';
import { userService, CreateUserRequest, UpdateUserRequest } from '@/services/userService';
import { User } from '@/types/admin';
import { 
  Settings as SettingsIcon, 
  Save, 
  RefreshCw,
  Bell,
  Shield,
  Database,
  Network,
  Palette,
  Monitor,
  Users,
  Plus,
  Edit,
  Trash2,
  Eye,
  EyeOff,
  UserPlus
} from 'lucide-react';
import { TransformationPreview } from '@/components/fieldMapping/TransformationPreview';

export const Settings = () => {
  const { user } = useAuth();
  const { toast } = useToast();
  const [users, setUsers] = useState<User[]>([]);
  const [isCreateUserOpen, setIsCreateUserOpen] = useState(false);
  const [isEditUserOpen, setIsEditUserOpen] = useState(false);
  const [selectedUser, setSelectedUser] = useState<User | null>(null);
  const [showPasswords, setShowPasswords] = useState(false);
  const [isLoading, setIsLoading] = useState(false);
  const [isCreating, setIsCreating] = useState(false);
  const [isUpdating, setIsUpdating] = useState(false);
  const [isDeleting, setIsDeleting] = useState<string | null>(null);
  
  // New user form state
  const [newUser, setNewUser] = useState({
    username: '',
    email: '',
    first_name: '',
    last_name: '',
    role: 'viewer' as 'administrator' | 'integrator' | 'viewer',
  });

  // Load users on component mount
  useEffect(() => {
    loadUsers();
  }, []);

  const loadUsers = async () => {
    try {
      setIsLoading(true);
      const response = await userService.getAllUsers();
      if (response.success && response.data) {
        setUsers(response.data.users);
      } else {
        toast({
          title: "Error",
          description: response.error || "Failed to load users",
          variant: "destructive",
        });
      }
    } catch (error) {
      toast({
        title: "Error",
        description: "Failed to load users",
        variant: "destructive",
      });
    } finally {
      setIsLoading(false);
    }
  };

  // Mock passwords for display purposes (will be removed when backend provides proper password management)
  const getUserPassword = (username: string) => {
    return '••••••••'; // Always show masked password
  };

  const handleCreateUser = async () => {
    if (!newUser.username || !newUser.email) {
      toast({
        title: "Validation Error",
        description: "Please fill in all required fields",
        variant: "destructive",
      });
      return;
    }

    try {
      setIsCreating(true);
      
      const userData: CreateUserRequest = {
        username: newUser.username,
        email: newUser.email,
        firstName: newUser.first_name || newUser.username,
        lastName: newUser.last_name || '',
        role: newUser.role,
        status: 'active'
      };

      const response = await userService.createUser(userData);
      
      if (response.success) {
        toast({
          title: "User Created",
          description: `User "${newUser.username}" has been created successfully`,
        });
        
        // Reset form and close dialog
        setNewUser({ username: '', email: '', first_name: '', last_name: '', role: 'viewer' });
        setIsCreateUserOpen(false);
        
        // Reload users list
        await loadUsers();
      } else {
        toast({
          title: "Error",
          description: response.error || "Failed to create user",
          variant: "destructive",
        });
      }
    } catch (error) {
      toast({
        title: "Error",
        description: "An unexpected error occurred while creating the user",
        variant: "destructive",
      });
    } finally {
      setIsCreating(false);
    }
  };

  const handleEditUser = async () => {
    if (!selectedUser) return;

    try {
      setIsUpdating(true);
      
      const updates: UpdateUserRequest = {
        email: selectedUser.email,
        firstName: selectedUser.first_name,
        lastName: selectedUser.last_name,
        role: selectedUser.role,
        permissions: userService.getRolePermissions(selectedUser.role)
      };

      const response = await userService.updateUser(selectedUser.id, updates);
      
      if (response.success) {
        toast({
          title: "User Updated",
          description: `User "${selectedUser.username}" has been updated successfully`,
        });
        
        setIsEditUserOpen(false);
        setSelectedUser(null);
        
        // Reload users list
        await loadUsers();
      } else {
        toast({
          title: "Error",
          description: response.error || "Failed to update user",
          variant: "destructive",
        });
      }
    } catch (error) {
      toast({
        title: "Error",
        description: "An unexpected error occurred while updating the user",
        variant: "destructive",
      });
    } finally {
      setIsUpdating(false);
    }
  };

  const handleDeleteUser = async (userId: string, username: string) => {
    if (user?.id === userId) {
      toast({
        title: "Cannot Delete",
        description: "You cannot delete your own account",
        variant: "destructive",
      });
      return;
    }

    try {
      setIsDeleting(userId);
      
      const response = await userService.deleteUser(userId);
      
      if (response.success) {
        toast({
          title: "User Deleted",
          description: `User "${username}" has been deleted`,
        });
        
        // Reload users list
        await loadUsers();
      } else {
        toast({
          title: "Error",
          description: response.error || "Failed to delete user",
          variant: "destructive",
        });
      }
    } catch (error) {
      toast({
        title: "Error",
        description: "An unexpected error occurred while deleting the user",
        variant: "destructive",
      });
    } finally {
      setIsDeleting(null);
    }
  };

  const getRoleBadgeVariant = (role: string) => {
    switch (role) {
      case 'administrator': return 'default';
      case 'integrator': return 'secondary';
      case 'viewer': return 'outline';
      default: return 'outline';
    }
  };

  return (
    <div className="p-6 space-y-6 animate-fade-in">
      <div className="animate-slide-up">
        <h1 className="text-3xl font-bold text-foreground flex items-center gap-3">
          <SettingsIcon className="h-8 w-8" />
          Settings
        </h1>
        <p className="text-muted-foreground">Configure your IntegrixLab platform preferences</p>
      </div>

      <div className="grid grid-cols-1 xl:grid-cols-3 gap-6">
        {/* Transformation Functions Demo */}
        <div className="xl:col-span-3 space-y-6">
          <Card className="animate-scale-in">
            <CardHeader>
              <CardTitle className="flex items-center gap-2">
                <Monitor className="h-5 w-5" />
                Transformation Functions Demo
              </CardTitle>
              <CardDescription>
                Test the transformation functions that can be used in field mappings
              </CardDescription>
            </CardHeader>
            <CardContent>
              <div className="grid grid-cols-1 lg:grid-cols-2 xl:grid-cols-3 gap-4">
                <TransformationPreview selectedFunctionName="add" />
                <TransformationPreview selectedFunctionName="concat" />
                <TransformationPreview selectedFunctionName="if" />
              </div>
            </CardContent>
          </Card>
        </div>

        {/* User Management Section */}
        <div className="xl:col-span-2 space-y-6">
          <Card className="animate-scale-in">
            <CardHeader>
              <div className="flex items-center justify-between">
                <div className="flex items-center gap-2">
                  <Users className="h-5 w-5" />
                  <CardTitle>User Management</CardTitle>
                </div>
                <Dialog open={isCreateUserOpen} onOpenChange={setIsCreateUserOpen}>
                  <DialogTrigger asChild>
                    <Button size="sm">
                      <UserPlus className="h-4 w-4 mr-2" />
                      Add User
                    </Button>
                  </DialogTrigger>
                  <DialogContent>
                    <DialogHeader>
                      <DialogTitle>Create New User</DialogTitle>
                      <DialogDescription>
                        Add a new user to the system with specified permissions
                      </DialogDescription>
                    </DialogHeader>
                    <div className="space-y-4">
                      <div className="space-y-2">
                        <Label htmlFor="username">Username *</Label>
                        <Input
                          id="username"
                          value={newUser.username}
                          onChange={(e) => setNewUser({...newUser, username: e.target.value})}
                          placeholder="Enter username"
                          disabled={isCreating}
                        />
                      </div>
                      <div className="space-y-2">
                        <Label htmlFor="email">Email *</Label>
                        <Input
                          id="email"
                          type="email"
                          value={newUser.email}
                          onChange={(e) => setNewUser({...newUser, email: e.target.value})}
                          placeholder="Enter email address"
                          disabled={isCreating}
                        />
                      </div>
                      <div className="space-y-2">
                        <Label htmlFor="first_name">First Name</Label>
                        <Input
                          id="first_name"
                          value={newUser.first_name}
                          onChange={(e) => setNewUser({...newUser, first_name: e.target.value})}
                          placeholder="Enter first name"
                          disabled={isCreating}
                        />
                      </div>
                      <div className="space-y-2">
                        <Label htmlFor="last_name">Last Name</Label>
                        <Input
                          id="last_name"
                          value={newUser.last_name}
                          onChange={(e) => setNewUser({...newUser, last_name: e.target.value})}
                          placeholder="Enter last name"
                          disabled={isCreating}
                        />
                      </div>
                      <div className="space-y-2">
                        <Label htmlFor="role">Role</Label>
                        <Select 
                          value={newUser.role} 
                          onValueChange={(value) => setNewUser({...newUser, role: value as any})}
                          disabled={isCreating}
                        >
                          <SelectTrigger>
                            <SelectValue />
                          </SelectTrigger>
                          <SelectContent>
                                          <SelectItem value="administrator">Administrator</SelectItem>
                            <SelectItem value="integrator">Integrator</SelectItem>
                            <SelectItem value="viewer">Viewer</SelectItem>
                          </SelectContent>
                        </Select>
                      </div>
                      <div className="space-y-2">
                        <Label>Permissions</Label>
                        <div className="flex flex-wrap gap-1">
                          {Object.entries(userService.getRolePermissions(newUser.role)).map(([resource, actions]) => 
                            actions.map(action => (
                              <Badge key={`${resource}:${action}`} variant="outline" className="text-xs">
                                {resource}:{action}
                              </Badge>
                            ))
                          )}
                        </div>
                      </div>
                      <div className="flex justify-end gap-2">
                        <Button variant="outline" onClick={() => setIsCreateUserOpen(false)} disabled={isCreating}>
                          Cancel
                        </Button>
                        <Button onClick={handleCreateUser} disabled={isCreating}>
                          {isCreating ? 'Creating...' : 'Create User'}
                        </Button>
                      </div>
                    </div>
                  </DialogContent>
                </Dialog>
              </div>
              <CardDescription>Manage user accounts and permissions</CardDescription>
            </CardHeader>
            <CardContent>
              <div className="space-y-4">
                <div className="flex items-center justify-between">
                  <Label className="text-sm font-medium">
                    System Users ({users.length})
                    {isLoading && <span className="text-muted-foreground"> - Loading...</span>}
                  </Label>
                  <div className="flex items-center gap-2">
                    <Button
                      variant="ghost"
                      size="sm"
                      onClick={loadUsers}
                      disabled={isLoading}
                    >
                      <RefreshCw className={`h-4 w-4 mr-2 ${isLoading ? 'animate-spin' : ''}`} />
                      Refresh
                    </Button>
                    <Button
                      variant="ghost"
                      size="sm"
                      onClick={() => setShowPasswords(!showPasswords)}
                    >
                      {showPasswords ? <EyeOff className="h-4 w-4 mr-2" /> : <Eye className="h-4 w-4 mr-2" />}
                      {showPasswords ? 'Hide' : 'Show'} Passwords
                    </Button>
                  </div>
                </div>
                
                <div className="space-y-3">
                  {users.map((userItem) => (
                    <div key={userItem.id} className="border border-border rounded-lg p-4">
                      <div className="flex items-start justify-between">
                        <div className="flex-1">
                          <div className="flex items-center gap-3 mb-2">
                            <span className="font-medium">{userItem.username}</span>
                            <Badge variant={getRoleBadgeVariant(userItem.role)} className="text-xs">
                              {userItem.role.toUpperCase()}
                            </Badge>
                            {userItem.id === user?.id && (
                              <Badge variant="outline" className="text-xs">Current User</Badge>
                            )}
                          </div>
                          <div className="text-sm text-muted-foreground space-y-1">
                            <div>Email: {userItem.email}</div>
                            {showPasswords && (
                              <div>Password: <code className="bg-muted px-1 rounded">{getUserPassword(userItem.username)}</code></div>
                            )}
                            <div>Created: {userItem.created_at}</div>
                            {userItem.last_login_at && (
                              <div>Last Login: {new Date(userItem.last_login_at).toLocaleDateString()}</div>
                            )}
                          </div>
                          <div className="flex flex-wrap gap-1 mt-2">
                            {userItem.permissions && Object.entries(userItem.permissions).map(([resource, actions]) => 
                              actions.map(action => (
                                <Badge key={`${resource}:${action}`} variant="outline" className="text-xs">
                                  {resource}:{action}
                                </Badge>
                              ))
                            )}
                          </div>
                        </div>
                        <div className="flex items-center gap-2">
                          <Dialog open={isEditUserOpen && selectedUser?.id === userItem.id} onOpenChange={(open) => {
                            setIsEditUserOpen(open);
                            if (!open) setSelectedUser(null);
                          }}>
                            <DialogTrigger asChild>
                              <Button
                                variant="ghost"
                                size="sm"
                                onClick={() => setSelectedUser({...userItem})}
                              >
                                <Edit className="h-4 w-4" />
                              </Button>
                            </DialogTrigger>
                            <DialogContent>
                              <DialogHeader>
                                <DialogTitle>Edit User</DialogTitle>
                                <DialogDescription>
                                  Update user information and permissions
                                </DialogDescription>
                              </DialogHeader>
                              {selectedUser && (
                                <div className="space-y-4">
                                  <div className="space-y-2">
                                    <Label>Username</Label>
                                    <Input value={selectedUser.username} disabled />
                                  </div>
                                   <div className="space-y-2">
                                     <Label htmlFor="edit-email">Email</Label>
                                     <Input
                                       id="edit-email"
                                       type="email"
                                       value={selectedUser.email}
                                       onChange={(e) => setSelectedUser({...selectedUser, email: e.target.value})}
                                       disabled={isUpdating}
                                     />
                                   </div>
                                   <div className="space-y-2">
                                     <Label htmlFor="edit-first_name">First Name</Label>
                                     <Input
                                       id="edit-first_name"
                                       value={selectedUser.first_name || ''}
                                       onChange={(e) => setSelectedUser({...selectedUser, first_name: e.target.value})}
                                       disabled={isUpdating}
                                     />
                                   </div>
                                   <div className="space-y-2">
                                     <Label htmlFor="edit-last_name">Last Name</Label>
                                     <Input
                                       id="edit-last_name"
                                       value={selectedUser.last_name || ''}
                                       onChange={(e) => setSelectedUser({...selectedUser, last_name: e.target.value})}
                                       disabled={isUpdating}
                                     />
                                   </div>
                                  <div className="space-y-2">
                                    <Label htmlFor="edit-role">Role</Label>
                                      <Select 
                                        value={selectedUser.role} 
                                        onValueChange={(value) => setSelectedUser({...selectedUser, role: value as 'administrator' | 'integrator' | 'viewer'})}
                                        disabled={isUpdating}
                                      >
                                       <SelectTrigger>
                                         <SelectValue />
                                       </SelectTrigger>
                                       <SelectContent>
                                         <SelectItem value="administrator">Administrator</SelectItem>
                                         <SelectItem value="integrator">Integrator</SelectItem>
                                         <SelectItem value="viewer">Viewer</SelectItem>
                                       </SelectContent>
                                     </Select>
                                  </div>
                                  <div className="space-y-2">
                                    <Label>Permissions</Label>
                                      <div className="flex flex-wrap gap-1">
                                        {Object.entries(userService.getRolePermissions(selectedUser.role)).map(([resource, actions]) => 
                                          actions.map(action => (
                                            <Badge key={`${resource}:${action}`} variant="outline" className="text-xs">
                                              {resource}:{action}
                                            </Badge>
                                          ))
                                        )}
                                      </div>
                                  </div>
                                   <div className="flex justify-end gap-2">
                                     <Button variant="outline" onClick={() => setIsEditUserOpen(false)} disabled={isUpdating}>
                                       Cancel
                                     </Button>
                                     <Button onClick={handleEditUser} disabled={isUpdating}>
                                       {isUpdating ? 'Updating...' : 'Update User'}
                                     </Button>
                                   </div>
                                </div>
                              )}
                            </DialogContent>
                          </Dialog>
                          <Button
                            variant="ghost"
                            size="sm"
                            className="text-destructive hover:text-destructive"
                            onClick={() => handleDeleteUser(userItem.id, userItem.username)}
                            disabled={userItem.id === user?.id || isDeleting === userItem.id}
                          >
                            {isDeleting === userItem.id ? (
                              <RefreshCw className="h-4 w-4 animate-spin" />
                            ) : (
                              <Trash2 className="h-4 w-4" />
                            )}
                          </Button>
                        </div>
                      </div>
                    </div>
                  ))}
                </div>
              </div>
            </CardContent>
          </Card>
        </div>

        {/* System Settings Sidebar */}
        <div className="space-y-6">
          <Card className="animate-scale-in" style={{ animationDelay: '0.1s' }}>
            <CardHeader>
              <CardTitle className="flex items-center gap-2">
                <Bell className="h-5 w-5" />
                Notifications
              </CardTitle>
            </CardHeader>
            <CardContent className="space-y-4">
              <div className="space-y-2">
                <div className="flex items-center justify-between">
                  <span className="text-sm">Flow failures</span>
                  <Badge variant="default">Enabled</Badge>
                </div>
                <div className="flex items-center justify-between">
                  <span className="text-sm">Daily reports</span>
                  <Badge variant="secondary">Disabled</Badge>
                </div>
              </div>
            </CardContent>
          </Card>

          <Card className="animate-scale-in" style={{ animationDelay: '0.2s' }}>
            <CardHeader>
              <CardTitle className="flex items-center gap-2">
                <Monitor className="h-5 w-5" />
                Interface
              </CardTitle>
            </CardHeader>
            <CardContent className="space-y-4">
              <div className="space-y-2">
                <div className="flex items-center justify-between">
                  <span className="text-sm">Dark mode</span>
                  <Badge variant="default">Active</Badge>
                </div>
                <div className="flex items-center justify-between">
                  <span className="text-sm">Compact view</span>
                  <Badge variant="secondary">Disabled</Badge>
                </div>
              </div>
            </CardContent>
          </Card>

          <Card className="animate-scale-in" style={{ animationDelay: '0.3s' }}>
            <CardHeader>
              <CardTitle className="flex items-center gap-2">
                <Shield className="h-5 w-5" />
                Security
              </CardTitle>
            </CardHeader>
            <CardContent className="space-y-4">
              <div className="space-y-2">
                <div className="flex items-center justify-between">
                  <span className="text-sm">Session timeout</span>
                  <span className="text-sm text-muted-foreground">24 hours</span>
                </div>
                <div className="flex items-center justify-between">
                  <span className="text-sm">Multi-factor auth</span>
                  <Badge variant="secondary">Disabled</Badge>
                </div>
              </div>
            </CardContent>
          </Card>
        </div>
      </div>

      <div className="flex justify-end space-x-3">
        <Button variant="outline">
          <RefreshCw className="h-4 w-4 mr-2" />
          Reset to Defaults
        </Button>
        <Button className="bg-gradient-primary hover:opacity-90">
          <Save className="h-4 w-4 mr-2" />
          Save Settings
        </Button>
      </div>
    </div>
  );
};