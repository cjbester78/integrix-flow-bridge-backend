import { useState, useEffect } from 'react';
import { Tabs, TabsContent, TabsList, TabsTrigger } from '@/components/ui/tabs';
import { Users, Shield, Key, FileArchive, ScrollText, Network, Settings } from 'lucide-react';
import { AdminStats } from '@/components/admin/AdminStats';
import { UserManagement } from '@/components/admin/UserManagement';
import { RoleManagement } from '@/components/admin/RoleManagement';
import { CertificateManagement } from '@/components/admin/CertificateManagement';
import { JarFileManagement } from '@/components/admin/JarFileManagement';
import { SystemLogs } from '@/components/admin/SystemLogs';
import { AdapterTypesManagement } from '@/components/admin/AdapterTypesManagement';
import { SystemSettings } from '@/components/admin/SystemSettings';
import { User, Role, Certificate, JarFile } from '@/types/admin';
import { userService } from '@/services/userService';
import { roleService } from '@/services/roleService';
import { certificateService } from '@/services/certificateService';
import { jarFileService } from '@/services/jarFileService';
import { useToast } from '@/hooks/use-toast';


export const Admin = () => {
  const { toast } = useToast();
  const [users, setUsers] = useState<User[]>([]);
  const [isLoadingUsers, setIsLoadingUsers] = useState(true);
  const [roles, setRoles] = useState<Role[]>([]);
  const [isLoadingRoles, setIsLoadingRoles] = useState(true);
  const [certificates, setCertificates] = useState<Certificate[]>([]);
  const [isLoadingCertificates, setIsLoadingCertificates] = useState(true);
  const [jarFiles, setJarFiles] = useState<JarFile[]>([]);
  const [isLoadingJarFiles, setIsLoadingJarFiles] = useState(true);
  const [activeTab, setActiveTab] = useState('users');

  // Fetch users from backend
  const fetchUsers = async () => {
    try {
      setIsLoadingUsers(true);
      const response = await userService.getAllUsers();
      console.log('User fetch response:', response); // Debug log
      
      if (response.success && response.data) {
        // Handle different possible response structures
        const userData = Array.isArray(response.data) ? response.data : response.data.users;
        setUsers(userData || []);
      } else {
        console.error('Failed to fetch users:', response.error);
        // Show empty list when API fails
        console.log('API not available, showing empty user list');
        setUsers([]);
        toast({ title: "Error", description: 'Failed to load users', variant: "destructive" });
      }
    } catch (error) {
      console.error('Error fetching users:', error);
      // Show empty list when API fails
      console.log('API error, showing empty user list');
      setUsers([]);
      toast({ title: "Error", description: 'Failed to load users', variant: "destructive" });
    } finally {
      setIsLoadingUsers(false);
    }
  };

  // Fetch roles from backend
  const fetchRoles = async () => {
    try {
      setIsLoadingRoles(true);
      const response = await roleService.getAllRoles();
      console.log('Role fetch response:', response);
      
      if (response.success && response.data) {
        // Backend returns array directly, not wrapped in {roles: [...]}
        const roleData = Array.isArray(response.data) ? response.data : [];
        setRoles(roleData);
      } else {
        console.error('Failed to fetch roles:', response.error);
        setRoles([]);
        toast({ title: "Error", description: 'Failed to load roles', variant: "destructive" });
      }
    } catch (error) {
      console.error('Error fetching roles:', error);
      setRoles([]);
      toast({ title: "Error", description: 'Failed to load roles', variant: "destructive" });
    } finally {
      setIsLoadingRoles(false);
    }
  };

  // Fetch certificates from backend
  const fetchCertificates = async () => {
    try {
      setIsLoadingCertificates(true);
      const response = await certificateService.getAllCertificates();
      console.log('Certificate fetch response:', response);
      
      if (response.success && response.data) {
        setCertificates(response.data);
      } else {
        console.error('Failed to fetch certificates:', response.error);
        setCertificates([]);
        toast({ title: "Error", description: 'Failed to load certificates', variant: "destructive" });
      }
    } catch (error) {
      console.error('Error fetching certificates:', error);
      setCertificates([]);
      toast({ title: "Error", description: 'Failed to load certificates', variant: "destructive" });
    } finally {
      setIsLoadingCertificates(false);
    }
  };

  // Fetch JAR files from backend
  const fetchJarFiles = async () => {
    try {
      setIsLoadingJarFiles(true);
      const response = await jarFileService.getAllJarFiles();
      console.log('JAR files fetch response:', response);
      
      if (response.success && response.data) {
        setJarFiles(response.data);
      } else {
        console.error('Failed to fetch JAR files:', response.error);
        setJarFiles([]);
        toast({ title: "Error", description: 'Failed to load JAR files', variant: "destructive" });
      }
    } catch (error) {
      console.error('Error fetching JAR files:', error);
      setJarFiles([]);
      toast({ title: "Error", description: 'Failed to load JAR files', variant: "destructive" });
    } finally {
      setIsLoadingJarFiles(false);
    }
  };

  useEffect(() => {
    fetchUsers();
    fetchRoles();
    fetchCertificates();
    fetchJarFiles();
  }, []);

  const handleJarFileAdded = (jarFile: JarFile) => {
    setJarFiles(prev => [...prev, jarFile]);
  };

  const handleJarFileDeleted = (id: string) => {
    setJarFiles(prev => prev.filter(jar => jar.id !== id));
  };

  return (
    <div className="p-6 space-y-6">
      <div>
        <h1 className="text-3xl font-bold text-foreground flex items-center gap-3">
          <Users className="h-8 w-8" />
          Admin Panel
        </h1>
        <p className="text-muted-foreground">Manage users, roles, and system security</p>
      </div>

      <AdminStats 
        users={users}
        roles={roles}
        certificates={certificates}
        jarFiles={jarFiles}
      />

      <Tabs value={activeTab} onValueChange={setActiveTab} className="space-y-6 animate-fade-in" style={{ animationDelay: '0.1s' }}>
        <TabsList className="grid w-full grid-cols-7">
          <TabsTrigger value="users" className="flex items-center gap-2">
            <Users className="h-4 w-4" />
            Users
          </TabsTrigger>
          <TabsTrigger value="roles" className="flex items-center gap-2">
            <Shield className="h-4 w-4" />
            Roles
          </TabsTrigger>
          <TabsTrigger value="certificates" className="flex items-center gap-2">
            <Key className="h-4 w-4" />
            Certificates
          </TabsTrigger>
          <TabsTrigger value="jar-files" className="flex items-center gap-2">
            <FileArchive className="h-4 w-4" />
            Connection Drivers
          </TabsTrigger>
          <TabsTrigger value="adapter-types" className="flex items-center gap-2">
            <Network className="h-4 w-4" />
            Adapter Types
          </TabsTrigger>
          <TabsTrigger value="system-settings" className="flex items-center gap-2">
            <Settings className="h-4 w-4" />
            System Settings
          </TabsTrigger>
          <TabsTrigger value="system-logs" className="flex items-center gap-2">
            <ScrollText className="h-4 w-4" />
            System Logs
          </TabsTrigger>
        </TabsList>

        <TabsContent value="users" className="space-y-4">
          <UserManagement users={users} isLoading={isLoadingUsers} onRefresh={fetchUsers} />
        </TabsContent>

        <TabsContent value="roles" className="space-y-4">
          <RoleManagement roles={roles} isLoading={isLoadingRoles} onRefresh={fetchRoles} />
        </TabsContent>

        <TabsContent value="certificates" className="space-y-4">
          <CertificateManagement certificates={certificates} isLoading={isLoadingCertificates} onRefresh={fetchCertificates} />
        </TabsContent>

        <TabsContent value="jar-files" className="space-y-4">
          <JarFileManagement 
            jarFiles={jarFiles}
            isLoading={isLoadingJarFiles}
            onRefresh={fetchJarFiles}
            onJarFileAdded={handleJarFileAdded}
            onJarFileDeleted={handleJarFileDeleted}
          />
        </TabsContent>

        <TabsContent value="adapter-types" className="space-y-4">
          <AdapterTypesManagement />
        </TabsContent>

        <TabsContent value="system-settings" className="space-y-4">
          <SystemSettings />
        </TabsContent>

        <TabsContent value="system-logs" className="space-y-4">
          <SystemLogs />
        </TabsContent>
      </Tabs>
    </div>
  );
};