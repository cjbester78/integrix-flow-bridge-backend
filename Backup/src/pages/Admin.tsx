import { useState, useEffect } from 'react';
import { Tabs, TabsContent, TabsList, TabsTrigger } from '@/components/ui/tabs';
import { Users, Shield, Key, FileArchive, ScrollText, Network } from 'lucide-react';
import { AdminStats } from '@/components/admin/AdminStats';
import { UserManagement } from '@/components/admin/UserManagement';
import { RoleManagement } from '@/components/admin/RoleManagement';
import { CertificateManagement } from '@/components/admin/CertificateManagement';
import { JarFileManagement } from '@/components/admin/JarFileManagement';
import { SystemLogs } from '@/components/admin/SystemLogs';
import { AdapterTypesManagement } from '@/components/admin/AdapterTypesManagement';
import { User, Role, Certificate, JarFile } from '@/types/admin';
import { userService } from '@/services/userService';
import { toast } from 'sonner';

const initialUsers: User[] = [
  {
    id: '1',
    username: 'admin',
    email: 'admin@integrixlab.com',
    first_name: 'System',
    last_name: 'Administrator',
    role: 'administrator',
    status: 'active',
    permissions: {
      flows: ['create', 'read', 'update', 'delete', 'execute'],
      adapters: ['create', 'read', 'update', 'delete', 'test'],
      structures: ['create', 'read', 'update', 'delete'],
      users: ['create', 'read', 'update', 'delete'],
      system: ['create', 'read', 'update', 'delete']
    },
    email_verified: true,
    created_at: '2024-01-01 09:00:00',
    updated_at: '2024-01-01 09:00:00',
    last_login_at: '2024-01-15 14:30:25'
  },
  {
    id: '2',
    username: 'integrator1',
    email: 'integrator1@company.com',
    first_name: 'John',
    last_name: 'Integrator',
    role: 'integrator',
    status: 'active',
    permissions: {
      flows: ['create', 'read', 'update', 'execute'],
      adapters: ['create', 'read', 'update', 'test'],
      structures: ['create', 'read', 'update']
    },
    email_verified: true,
    created_at: '2024-01-05 10:30:00',
    updated_at: '2024-01-05 10:30:00',
    last_login_at: '2024-01-15 12:15:30'
  },
  {
    id: '3',
    username: 'viewer1',
    email: 'viewer1@company.com',
    first_name: 'Jane',
    last_name: 'Viewer',
    role: 'viewer',
    status: 'inactive',
    permissions: {
      flows: ['read'],
      adapters: ['read'],
      structures: ['read']
    },
    email_verified: true,
    created_at: '2024-01-08 14:20:00',
    updated_at: '2024-01-08 14:20:00',
    last_login_at: '2024-01-10 16:45:12'
  }
];

const initialRoles: Role[] = [
  {
    id: '1',
    name: 'administrator',
    description: 'Full system access with user management capabilities',
    permissions: ['read', 'write', 'admin', 'user_management'],
    userCount: 1
  },
  {
    id: '2',
    name: 'integrator',
    description: 'Can create and manage integration flows',
    permissions: ['read', 'write', 'execute'],
    userCount: 1
  },
  {
    id: '3',
    name: 'viewer',
    description: 'Read-only access to monitoring and logs',
    permissions: ['read'],
    userCount: 1
  }
];

const initialCertificates: Certificate[] = [
  {
    id: '1',
    name: 'SAP Production SSL',
    type: 'SSL Certificate',
    issuer: 'DigiCert Inc',
    validFrom: '2024-01-01',
    validTo: '2025-01-01',
    status: 'active',
    usage: 'SAP ERP Connection'
  },
  {
    id: '2',
    name: 'Salesforce OAuth',
    type: 'OAuth Certificate',
    issuer: 'Salesforce.com',
    validFrom: '2024-01-01',
    validTo: '2024-12-31',
    status: 'active',
    usage: 'Salesforce API Authentication'
  },
  {
    id: '3',
    name: 'Legacy System Cert',
    type: 'Client Certificate',
    issuer: 'Internal CA',
    validFrom: '2023-06-01',
    validTo: '2024-02-01',
    status: 'expiring',
    usage: 'Legacy SOAP Service'
  }
];

const initialJarFiles: JarFile[] = [
  {
    id: '1',
    name: 'MySQL JDBC Driver',
    version: '8.0.33',
    description: 'MySQL Connector/J JDBC Driver for database connectivity',
    file_name: 'mysql-connector-java-8.0.33.jar',
    size_bytes: 2456789,
    upload_date: '2024-01-15',
    driver_type: 'Database',
    is_active: true,
    created_at: '2024-01-15 10:30:00',
    updated_at: '2024-01-15 10:30:00'
  },
  {
    id: '2',
    name: 'PostgreSQL JDBC Driver',
    version: '42.6.0',
    description: 'PostgreSQL JDBC Driver for database operations',
    file_name: 'postgresql-42.6.0.jar',
    size_bytes: 1234567,
    upload_date: '2024-01-10',
    driver_type: 'Database',
    is_active: true,
    created_at: '2024-01-10 11:20:00',
    updated_at: '2024-01-10 11:20:00'
  },
  {
    id: '3',
    name: 'ActiveMQ Client',
    version: '5.18.3',
    description: 'ActiveMQ JMS Client for message queue operations',
    file_name: 'activemq-client-5.18.3.jar',
    size_bytes: 987654,
    upload_date: '2024-01-08',
    driver_type: 'Message Queue',
    is_active: true,
    created_at: '2024-01-08 09:15:00',
    updated_at: '2024-01-08 09:15:00'
  }
];

export const Admin = () => {
  const [users, setUsers] = useState<User[]>([]);
  const [isLoadingUsers, setIsLoadingUsers] = useState(true);
  const [roles] = useState<Role[]>(initialRoles);
  const [certificates] = useState<Certificate[]>(initialCertificates);
  const [jarFiles, setJarFiles] = useState<JarFile[]>(initialJarFiles);
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
        // Use initial mock users as fallback when API fails
        console.log('API not available, using fallback user data');
        setUsers(initialUsers);
        toast.error('API temporarily unavailable - showing offline data');
      }
    } catch (error) {
      console.error('Error fetching users:', error);
      // Use initial mock users as fallback when API fails
      console.log('API error, using fallback user data');
      setUsers(initialUsers);
      toast.error('API temporarily unavailable - showing offline data');
    } finally {
      setIsLoadingUsers(false);
    }
  };

  useEffect(() => {
    fetchUsers();
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
        <TabsList className="grid w-full grid-cols-6">
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
          <TabsTrigger value="system-logs" className="flex items-center gap-2">
            <ScrollText className="h-4 w-4" />
            System Logs
          </TabsTrigger>
        </TabsList>

        <TabsContent value="users" className="space-y-4">
          <UserManagement users={users} isLoading={isLoadingUsers} onRefresh={fetchUsers} />
        </TabsContent>

        <TabsContent value="roles" className="space-y-4">
          <RoleManagement roles={roles} />
        </TabsContent>

        <TabsContent value="certificates" className="space-y-4">
          <CertificateManagement certificates={certificates} />
        </TabsContent>

        <TabsContent value="jar-files" className="space-y-4">
          <JarFileManagement 
            jarFiles={jarFiles}
            onJarFileAdded={handleJarFileAdded}
            onJarFileDeleted={handleJarFileDeleted}
          />
        </TabsContent>

        <TabsContent value="adapter-types" className="space-y-4">
          <AdapterTypesManagement />
        </TabsContent>

        <TabsContent value="system-logs" className="space-y-4">
          <SystemLogs />
        </TabsContent>
      </Tabs>
    </div>
  );
};