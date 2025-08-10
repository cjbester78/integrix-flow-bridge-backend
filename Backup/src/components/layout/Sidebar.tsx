
import { 
  LayoutDashboard, 
  MessageSquare, 
  Activity, 
  Users, 
  Settings,
  Plus,
  Send,
  ChevronLeft,
  ChevronRight,
  Layers,
  Building2,
  ScrollText
} from 'lucide-react';
import { NavLink } from 'react-router-dom';
import { useState } from 'react';
import { Button } from '@/components/ui/button';
import { cn } from '@/lib/utils';
import { useAuth } from '@/contexts/AuthContext';

const navigation = [
  { 
    name: 'Dashboard', 
    href: '/dashboard', 
    icon: LayoutDashboard, 
    roles: ['administrator', 'integrator', 'viewer'] 
  },
  { 
    name: 'Create Flow', 
    href: '/create-flow', 
    icon: Plus, 
    roles: ['administrator', 'integrator'] 
  },
  { 
    name: 'Create Data Structures', 
    href: '/data-structures', 
    icon: Layers, 
    roles: ['administrator', 'integrator'] 
  },
  { 
    name: 'Create Communication Adapter', 
    href: '/create-communication-adapter', 
    icon: Send, 
    roles: ['administrator', 'integrator'] 
  },
  { 
    name: 'Business Components', 
    href: '/business-components', 
    icon: Building2, 
    roles: ['administrator', 'integrator'] 
  },
  { 
    name: 'Message Monitor', 
    href: '/messages', 
    icon: MessageSquare, 
    roles: ['administrator', 'integrator', 'viewer'] 
  },
  { 
    name: 'Channel Monitor', 
    href: '/channels', 
    icon: Activity, 
    roles: ['administrator', 'integrator', 'viewer'] 
  },
  { 
    name: 'Admin Panel', 
    href: '/admin', 
    icon: Users, 
    roles: ['administrator'] 
  },
  { 
    name: 'Settings', 
    href: '/settings', 
    icon: Settings, 
    roles: ['administrator'] 
  },
];

export const Sidebar = () => {
  const [collapsed, setCollapsed] = useState(false);
  const { user } = useAuth();

  // Filter navigation items based on user role
  const filteredNavigation = navigation.filter(item => 
    user && item.roles.includes(user.role)
  );

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
                <div>© 2024 IntegrixLab</div>
              </>
            )}
          </div>
        </div>
      </div>
    </div>
  );
};
