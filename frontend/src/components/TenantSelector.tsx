import { useTenant } from '@/contexts/TenantContext';
import {
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from '@/components/ui/select';
import { Building2, Crown, User, Eye } from 'lucide-react';
import { Badge } from '@/components/ui/badge';
import { Skeleton } from '@/components/ui/skeleton';

export function TenantSelector() {
  const { currentTenant, userTenants, loading, switchTenant } = useTenant();

  if (loading) {
    return <Skeleton className="h-10 w-48" />;
  }

  if (!currentTenant || userTenants.length === 0) {
    return null;
  }

  const getRoleIcon = (role?: string) => {
    switch (role) {
      case 'TENANT_OWNER':
        return <Crown className="h-3 w-3" />;
      case 'TENANT_ADMIN':
        return <User className="h-3 w-3" />;
      case 'VIEWER':
        return <Eye className="h-3 w-3" />;
      default:
        return null;
    }
  };

  const getRoleBadgeVariant = (role?: string): "default" | "secondary" | "outline" => {
    switch (role) {
      case 'TENANT_OWNER':
      case 'TENANT_ADMIN':
        return "default";
      case 'VIEWER':
        return "secondary";
      default:
        return "outline";
    }
  };

  return (
    <div className="flex items-center gap-2">
      <Building2 className="h-4 w-4 text-muted-foreground" />
      <Select value={currentTenant.id} onValueChange={switchTenant}>
        <SelectTrigger className="w-48">
          <SelectValue placeholder="Select tenant" />
        </SelectTrigger>
        <SelectContent>
          {userTenants.map((tenant) => (
            <SelectItem key={tenant.id} value={tenant.id}>
              <div className="flex items-center justify-between w-full">
                <div className="flex items-center gap-2">
                  <span>{tenant.displayName || tenant.name}</span>
                  {tenant.primary && (
                    <Badge variant="outline" className="text-xs">
                      Primary
                    </Badge>
                  )}
                </div>
                {tenant.userRole && (
                  <Badge 
                    variant={getRoleBadgeVariant(tenant.userRole)}
                    className="ml-2 text-xs"
                  >
                    {getRoleIcon(tenant.userRole)}
                    <span className="ml-1">
                      {tenant.userRole.replace('TENANT_', '')}
                    </span>
                  </Badge>
                )}
              </div>
            </SelectItem>
          ))}
        </SelectContent>
      </Select>
    </div>
  );
}