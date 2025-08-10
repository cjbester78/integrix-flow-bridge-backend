
import { 
  LayoutDashboard, 
  MessageSquare, 
  Activity, 
  Users, 
  Settings,
  Send,
  ChevronLeft,
  ChevronRight,
  Layers,
  Building2,
  ScrollText,
  RefreshCw,
  CheckCircle2,
  Code
} from 'lucide-react';
import { NavLink } from 'react-router-dom';
import { useState, useMemo } from 'react';
import { Button } from '@/components/ui/button';
import { cn } from '@/lib/utils';
import { useAuth } from '@/contexts/AuthContext';
import { useEnvironmentPermissions } from '@/hooks/useEnvironmentPermissions-no-query';

const navigation = [
  { 
    name: 'Dashboard', 
    href: '/dashboard', 
    icon: LayoutDashboard, 
    roles: ['administrator', 'integrator', 'viewer'],
    requiresDevEnv: false
  },
  { 
    name: 'Data Structures', 
    href: '/data-structures', 
    icon: Layers, 
    roles: ['administrator', 'integrator'],
    requiresDevEnv: false // Can view in all environments, create only in dev
  },
  { 
    name: 'Communication Adapters', 
    href: '/communication-adapters', 
    icon: Send, 
    roles: ['administrator', 'integrator'],
    requiresDevEnv: false // Can view in all environments, create only in dev
  },
  { 
    name: 'Business Components', 
    href: '/business-components', 
    icon: Building2, 
    roles: ['administrator', 'integrator'],
    requiresDevEnv: false // Can view but not create in QA/Prod
  },
  { 
    name: 'Interface Management', 
    href: '/all-interfaces', 
    icon: ScrollText, 
    roles: ['administrator', 'integrator'],
    requiresDevEnv: false // Can view and manage in all environments
  },
  { 
    name: 'Message Monitor', 
    href: '/messages', 
    icon: MessageSquare, 
    roles: ['administrator', 'integrator', 'viewer'],
    requiresDevEnv: false
  },
  { 
    name: 'Adapter Monitoring', 
    href: '/adapter-monitoring', 
    icon: Activity, 
    roles: ['administrator', 'integrator', 'viewer'],
    requiresDevEnv: false
  },
  { 
    name: 'Retry Management', 
    href: '/retry-management', 
    icon: RefreshCw, 
    roles: ['administrator', 'integrator'],
    requiresDevEnv: false
  },
  { 
    name: 'Message Acknowledgments', 
    href: '/message-acknowledgments', 
    icon: CheckCircle2, 
    roles: ['administrator', 'integrator'],
    requiresDevEnv: false
  },
  { 
    name: 'Development Functions', 
    href: '/development-functions', 
    icon: Code, 
    roles: ['administrator', 'integrator'],
    requiresDevEnv: false // Can view in all environments, create/edit only in dev
  },
  { 
    name: 'Admin Panel', 
    href: '/admin', 
    icon: Users, 
    roles: ['administrator'],
    requiresDevEnv: false // Always available for admins
  },
  { 
    name: 'Settings', 
    href: '/settings', 
    icon: Settings, 
    roles: ['administrator'],
    requiresDevEnv: false // Always available for admins
  },
];

export const Sidebar = () => {
  const [collapsed, setCollapsed] = useState(false);
  const { user } = useAuth();
  const { isDevelopment, permissions } = useEnvironmentPermissions();

  // Debug logging

  // Filter navigation items based on user role and environment
  const filteredNavigation = useMemo(() => {
    // Ensure navigation is an array before filtering
    if (!Array.isArray(navigation)) {
      console.error('Navigation is not an array:', navigation);
      return [];
    }
    
    try {
      return navigation.filter(item => {
        
        // Check role permission
        // Fix for corrupted role data (missing first letter)
        const userRole = user?.role === 'administrator' ? 'administrator' : user?.role;
        
        if (!user || !userRole || !item.roles.includes(userRole)) {
          return false;
        }
        
        // Check environment restriction
        if (item.requiresDevEnv && !isDevelopment) {
          return false;
        }
        
        // Admin panel and settings are always visible for admins
        if (item.name === 'Admin Panel' || item.name === 'Settings') {
          return permissions?.isAdmin || userRole === 'administrator';
        }
        
        return true;
      });
    } catch (error) {
      console.error('Error filtering navigation:', error);
      return [];
    }
  }, [user, isDevelopment, permissions]);


  return (
    <div className={cn(
      "h-full bg-card/80 backdrop-blur-md border-r border-border transition-all duration-300 ease-in-out",
      collapsed ? "w-16" : "w-64"
    )}>
      <div className="flex flex-col h-full">
        <div className="p-4 border-b border-border">
          <Button
            variant="ghost"
            size="sm"
            onClick={() => setCollapsed(!collapsed)}
            className="ml-auto flex transition-all duration-300 hover:scale-110 hover:bg-accent/50"
          >
            {collapsed ? (
              <ChevronRight className="h-4 w-4 transition-transform duration-300" />
            ) : (
              <ChevronLeft className="h-4 w-4 transition-transform duration-300" />
            )}
          </Button>
        </div>

        <nav className="flex-1 p-4 space-y-2">
          {filteredNavigation.map((item) => (
            <NavLink
              key={item.name}
              to={item.href}
              className={({ isActive }) =>
                cn(
                  "flex items-center px-3 py-2 rounded-lg text-sm font-medium transition-all duration-300 group",
                  "hover:bg-accent hover:text-accent-foreground hover:scale-[1.02] hover:shadow-soft",
                  isActive
                    ? "bg-primary text-primary-foreground shadow-elegant"
                    : "text-muted-foreground hover:text-foreground"
                )
              }
            >
              <item.icon className={cn(
                "h-4 w-4 transition-all duration-300 group-hover:scale-110", 
                !collapsed && "mr-3"
              )} />
              {!collapsed && (
                <span className="transition-all duration-300">{item.name}</span>
              )}
            </NavLink>
          ))}
        </nav>

        <div className="p-4 border-t border-border">
          <div className={cn(
            "text-xs text-muted-foreground",
            collapsed ? "text-center" : "space-y-1"
          )}>
            {!collapsed && (
              <>
                <div>Version 1.0.0</div>
                <div>© 2024 Integrix Flow Bridge</div>
              </>
            )}
          </div>
        </div>
      </div>
    </div>
  );
};
