import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card';
import { Users, Shield, Key, FileArchive } from 'lucide-react';
import { User, Role, Certificate, JarFile } from '@/types/admin';

interface AdminStatsProps {
  users: User[];
  roles: Role[];
  certificates: Certificate[];
  jarFiles: JarFile[];
}

export const AdminStats = ({ users, roles, certificates, jarFiles }: AdminStatsProps) => {
  return (
    <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-4 animate-fade-in">
      <Card className="hover-scale">
        <CardHeader className="pb-2">
          <CardTitle className="text-sm font-medium flex items-center gap-2">
            <Users className="h-4 w-4" />
            Total Users
          </CardTitle>
        </CardHeader>
        <CardContent>
          <div className="text-2xl font-bold">{users.length}</div>
          <p className="text-xs text-muted-foreground">Active users</p>
        </CardContent>
      </Card>
      
      <Card className="hover-scale">
        <CardHeader className="pb-2">
          <CardTitle className="text-sm font-medium flex items-center gap-2">
            <Shield className="h-4 w-4" />
            Roles
          </CardTitle>
        </CardHeader>
        <CardContent>
          <div className="text-2xl font-bold">{roles.length}</div>
          <p className="text-xs text-muted-foreground">Defined roles</p>
        </CardContent>
      </Card>
      
      <Card className="hover-scale">
        <CardHeader className="pb-2">
          <CardTitle className="text-sm font-medium flex items-center gap-2">
            <Key className="h-4 w-4" />
            Certificates
          </CardTitle>
        </CardHeader>
        <CardContent>
          <div className="text-2xl font-bold">{certificates.length}</div>
          <p className="text-xs text-muted-foreground">SSL/Auth certs</p>
        </CardContent>
      </Card>

      <Card className="hover-scale">
        <CardHeader className="pb-2">
          <CardTitle className="text-sm font-medium flex items-center gap-2">
            <FileArchive className="h-4 w-4" />
            Connection Drivers
          </CardTitle>
        </CardHeader>
        <CardContent>
          <div className="text-2xl font-bold">{jarFiles.length}</div>
          <p className="text-xs text-muted-foreground">Driver files</p>
        </CardContent>
      </Card>
    </div>
  );
};