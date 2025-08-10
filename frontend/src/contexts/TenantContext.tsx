// @ts-nocheck
import React, { createContext, useContext, useState, useEffect, ReactNode } from 'react';
import { apiClient } from '@/lib/api-client';
import { useAuth } from '@/contexts/AuthContext';
import { useToast } from '@/hooks/use-toast';

interface Tenant {
  id: string;
  name: string;
  displayName: string;
  subdomain?: string;
  status: string;
  planId: string;
  userRole?: string;
  primary?: boolean;
}

interface TenantSubscription {
  id: number;
  planId: string;
  planName: string;
  status: string;
  features: string[];
  quotas: Record<string, number>;
  daysRemaining: number;
}

interface TenantUsage {
  executions: number;
  messages: number;
  apiCalls: number;
  storageGb: number;
  users: number;
  flows: number;
  quotas: Record<string, number>;
}

interface TenantContextType {
  currentTenant: Tenant | null;
  userTenants: Tenant[];
  subscription: TenantSubscription | null;
  usage: TenantUsage | null;
  loading: boolean;
  switchTenant: (tenantId: string) => Promise<void>;
  refreshTenants: () => Promise<void>;
  refreshSubscription: () => Promise<void>;
  refreshUsage: () => Promise<void>;
  hasFeature: (feature: string) => boolean;
  isOverQuota: (metric: string) => boolean;
  getQuotaPercentage: (metric: string) => number;
}

const TenantContext = createContext<TenantContextType | undefined>(undefined);

export function TenantProvider({ children }: { children: ReactNode }) {
  const { toast } = useToast();
  const { isAuthenticated } = useAuth();
  const [currentTenant, setCurrentTenant] = useState<Tenant | null>(null);
  const [userTenants, setUserTenants] = useState<Tenant[]>([]);
  const [subscription, setSubscription] = useState<TenantSubscription | null>(null);
  const [usage, setUsage] = useState<TenantUsage | null>(null);
  const [loading, setLoading] = useState(true);

  // Load current tenant
  const loadCurrentTenant = async () => {
    try {
      const response = await apiClient.get('/tenants/current');
      setCurrentTenant(response);
      
      // Store in localStorage for persistence
      localStorage.setItem('currentTenantId', response.id);
      
      // TODO: Set tenant header for future requests when apiClient supports it
      // For now, the header can be included in individual requests if needed
    } catch (error) {
      console.error('Failed to load current tenant:', error);
    }
  };

  // Load user's tenants
  const loadUserTenants = async () => {
    try {
      const response = await apiClient.get('/tenants/my-tenants');
      setUserTenants(response);
    } catch (error) {
      console.error('Failed to load user tenants:', error);
    }
  };

  // Load subscription info
  const loadSubscription = async () => {
    if (!currentTenant) return;
    
    try {
      const response = await apiClient.get(`/tenants/${currentTenant.id}/subscription`);
      setSubscription(response);
    } catch (error) {
      console.error('Failed to load subscription:', error);
    }
  };

  // Load usage info
  const loadUsage = async () => {
    if (!currentTenant) return;
    
    try {
      const response = await apiClient.get(`/tenants/${currentTenant.id}/usage`);
      setUsage(response);
    } catch (error) {
      console.error('Failed to load usage:', error);
    }
  };

  // Switch tenant
  const switchTenant = async (tenantId: string) => {
    try {
      await apiClient.post(`/tenants/switch/${tenantId}`);
      
      // Reload tenant data
      await loadCurrentTenant();
      await loadSubscription();
      await loadUsage();
      
      toast({ title: "Success", description: 'Switched tenant successfully' });
      
      // Reload the page to ensure all data is refreshed
      window.location.reload();
    } catch (error) {
      toast({ title: "Error", description: 'Failed to switch tenant', variant: "destructive" });
      throw error;
    }
  };

  // Refresh functions
  const refreshTenants = async () => {
    await loadUserTenants();
  };

  const refreshSubscription = async () => {
    await loadSubscription();
  };

  const refreshUsage = async () => {
    await loadUsage();
  };

  // Feature checking
  const hasFeature = (feature: string): boolean => {
    return subscription?.features?.includes(feature) ?? false;
  };

  // Quota checking
  const isOverQuota = (metric: string): boolean => {
    if (!usage || !usage.quotas) return false;
    
    const quota = usage.quotas[metric];
    if (!quota || quota === -1) return false; // Unlimited
    
    const usageValue = getUsageValue(metric);
    return usageValue > quota;
  };

  const getQuotaPercentage = (metric: string): number => {
    if (!usage || !usage.quotas) return 0;
    
    const quota = usage.quotas[metric];
    if (!quota || quota === -1) return 0; // Unlimited
    
    const usageValue = getUsageValue(metric);
    return Math.min((usageValue / quota) * 100, 100);
  };

  const getUsageValue = (metric: string): number => {
    if (!usage) return 0;
    
    switch (metric) {
      case 'executions_per_month':
        return usage.executions;
      case 'messages_per_month':
        return usage.messages;
      case 'api_calls_per_month':
        return usage.apiCalls;
      case 'storage_gb':
        return usage.storageGb;
      case 'users':
        return usage.users;
      case 'flows':
        return usage.flows;
      default:
        return 0;
    }
  };

  // Initialize on mount
  useEffect(() => {
    if (isAuthenticated) {
      const initialize = async () => {
        setLoading(true);
        
        // Check for saved tenant ID
        const savedTenantId = localStorage.getItem('currentTenantId');
        if (savedTenantId) {
          // TODO: Set tenant header when apiClient supports it
          // For now, tenant ID is stored in localStorage and can be sent with individual requests
        }
        
        await Promise.all([
          loadCurrentTenant(),
          loadUserTenants()
        ]);
        
        setLoading(false);
      };
      
      initialize();
    }
  }, [isAuthenticated]);

  // Load subscription and usage when tenant changes
  useEffect(() => {
    if (currentTenant) {
      loadSubscription();
      loadUsage();
    }
  }, [currentTenant]);

  const value: TenantContextType = {
    currentTenant,
    userTenants,
    subscription,
    usage,
    loading,
    switchTenant,
    refreshTenants,
    refreshSubscription,
    refreshUsage,
    hasFeature,
    isOverQuota,
    getQuotaPercentage
  };

  return (
    <TenantContext.Provider value={value}>
      {children}
    </TenantContext.Provider>
  );
}

export function useTenant() {
  const context = useContext(TenantContext);
  if (context === undefined) {
    throw new Error('useTenant must be used within a TenantProvider');
  }
  return context;
}