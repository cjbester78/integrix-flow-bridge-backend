// @ts-nocheck
import React, { useState, useEffect } from 'react';
import { Card } from '@/components/ui/card';
import { Progress } from '@/components/ui/progress';
import { AdapterCard } from '@/components/adapters/AdapterCard';
import { AdapterFilters } from '@/components/adapters/AdapterFilters';
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from '@/components/ui/select';
import { Label } from '@/components/ui/label';
import { BusinessComponent } from '@/types/businessComponent';
import { useBusinessComponentAdapters } from '@/hooks/useBusinessComponentAdapters';
import { adapterMonitoringService, AdapterMonitoring, AdapterMonitoringStatus } from '@/services/adapterMonitoringService';
import { Loader2 } from 'lucide-react';
import { useToast } from '@/hooks/use-toast';

export default function AdapterMonitoring() {
  const [selectedBusinessComponent, setSelectedBusinessComponent] = useState<BusinessComponent | null>(null);
  const [statusFilter, setStatusFilter] = useState<string | null>(null);
  const [adapters, setAdapters] = useState<AdapterMonitoring[]>([]);
  const [loadingAdapters, setLoadingAdapters] = useState(true);
  const [error, setError] = useState<string | null>(null);
  
  const { businessComponents, loading: loadingComponents } = useBusinessComponentAdapters();
  const { toast } = useToast();

  // Fetch adapters when business component changes
  useEffect(() => {
    const fetchAdapters = async () => {
      try {
        setError(null);
        setLoadingAdapters(true);
        
        const response = await adapterMonitoringService.getAdapters(selectedBusinessComponent?.id);
        
        if (response.success && response.data) {
          setAdapters(response.data);
        } else {
          setError(response.error || 'Failed to fetch adapters');
          toast({
            variant: "destructive",
            title: "Error",
            description: response.error || 'Failed to fetch adapters',
          });
        }
      } catch (err) {
        console.error('Error fetching adapters:', err);
        setError('Failed to load adapters');
        toast({
          variant: "destructive",
          title: "Error",
          description: 'Failed to load adapters',
        });
      } finally {
        setLoadingAdapters(false);
      }
    };

    fetchAdapters();
    
    // Refresh adapters every 30 seconds
    const interval = setInterval(fetchAdapters, 30000);
    return () => clearInterval(interval);
  }, [selectedBusinessComponent, toast]);

  // Handle adapter updates
  const handleUpdateAdapter = async (adapterId: string, updates: Partial<AdapterMonitoring>) => {
    try {
      const response = await adapterMonitoringService.updateAdapter(adapterId, updates);
      
      if (response.success && response.data) {
        setAdapters(prevAdapters => 
          prevAdapters.map(adapter => 
            adapter.id === adapterId ? response.data! : adapter
          )
        );
        toast({
          title: "Success",
          description: "Adapter updated successfully",
        });
      } else {
        toast({
          variant: "destructive",
          title: "Error",
          description: response.error || 'Failed to update adapter',
        });
      }
    } catch (err) {
      console.error('Error updating adapter:', err);
      toast({
        variant: "destructive",
        title: "Error",
        description: 'Failed to update adapter',
      });
    }
  };

  // Handle adapter deletion
  const handleDeleteAdapter = async (adapterId: string) => {
    try {
      const response = await adapterMonitoringService.deleteAdapter(adapterId);
      
      if (response.success) {
        setAdapters(prevAdapters => 
          prevAdapters.filter(adapter => adapter.id !== adapterId)
        );
        toast({
          title: "Success",
          description: "Adapter deleted successfully",
        });
      } else {
        toast({
          variant: "destructive",
          title: "Error",
          description: response.error || 'Failed to delete adapter',
        });
      }
    } catch (err) {
      console.error('Error deleting adapter:', err);
      toast({
        variant: "destructive",
        title: "Error",
        description: 'Failed to delete adapter',
      });
    }
  };

  // Filter adapters based on status
  const filteredAdapters = adapters.filter(adapter => {
    if (statusFilter && adapter.status !== statusFilter) return false;
    return true;
  });

  // Calculate overall metrics
  const totalAdapters = adapters.length;
  const activeAdapters = adapters.filter(a => a.status === 'running' || a.status === 'active').length;
  const adaptersWithErrors = adapters.filter(a => a.status === 'error').length;
  const averageLoad = adapters.length > 0 
    ? Math.round(adapters.reduce((sum, a) => sum + (a.load || 0), 0) / adapters.length)
    : 0;

  return (
    <div className="container mx-auto p-6">
      <div className="mb-6">
        <h1 className="text-3xl font-bold mb-2">Adapter Monitoring</h1>
        <p className="text-gray-600">Monitor and manage your communication adapters</p>
      </div>

      {/* Business Component Filter */}
      <div className="mb-6 flex items-center gap-4">
        <Label htmlFor="business-component">Business Component:</Label>
        <Select
          value={selectedBusinessComponent?.id || 'all'}
          onValueChange={(value) => {
            if (value === 'all') {
              setSelectedBusinessComponent(null);
            } else {
              const component = businessComponents.find(bc => bc.id === value);
              setSelectedBusinessComponent(component || null);
            }
          }}
          disabled={loadingComponents}
        >
          <SelectTrigger id="business-component" className="w-[280px]">
            <SelectValue placeholder="Select a business component" />
          </SelectTrigger>
          <SelectContent>
            <SelectItem value="all">All Components</SelectItem>
            {businessComponents.map((bc) => (
              <SelectItem key={bc.id} value={bc.id}>
                {bc.name}
              </SelectItem>
            ))}
          </SelectContent>
        </Select>
      </div>

      {/* Error Display */}
      {error && (
        <div className="mb-6 bg-destructive/10 text-destructive p-4 rounded-lg">
          {error}
        </div>
      )}

      {/* Overview Cards */}
      <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-4 mb-6">
        <Card className="p-4">
          <h3 className="text-sm font-medium text-gray-500">Total Adapters</h3>
          <p className="text-2xl font-bold">{totalAdapters}</p>
        </Card>
        <Card className="p-4">
          <h3 className="text-sm font-medium text-gray-500">Active Adapters</h3>
          <p className="text-2xl font-bold text-green-600">{activeAdapters}</p>
        </Card>
        <Card className="p-4">
          <h3 className="text-sm font-medium text-gray-500">Adapters with Errors</h3>
          <p className="text-2xl font-bold text-red-600">{adaptersWithErrors}</p>
        </Card>
        <Card className="p-4">
          <h3 className="text-sm font-medium text-gray-500">Average Load</h3>
          <div className="mt-2">
            <Progress value={averageLoad} className="h-2" />
            <p className="text-sm text-gray-600 mt-1">{averageLoad}%</p>
          </div>
        </Card>
      </div>

      {/* Filters */}
      <div className="mb-6">
        <AdapterFilters 
          selectedStatus={statusFilter}
          onStatusChange={setStatusFilter}
        />
      </div>

      {/* Adapters List */}
      {loadingAdapters ? (
        <div className="flex justify-center items-center p-12">
          <Loader2 className="h-8 w-8 animate-spin" />
        </div>
      ) : filteredAdapters.length === 0 ? (
        <Card className="p-12 text-center">
          <p className="text-gray-500">
            {statusFilter 
              ? `No adapters found with status: ${statusFilter}`
              : selectedBusinessComponent 
                ? `No adapters found for ${selectedBusinessComponent.name}`
                : 'No adapters found'
            }
          </p>
        </Card>
      ) : (
        <div className="grid grid-cols-1 gap-6">
          {filteredAdapters.map(adapter => (
            <AdapterCard 
              key={adapter.id} 
              adapter={adapter}
              onUpdate={(updates) => handleUpdateAdapter(adapter.id, updates)}
              onDelete={() => handleDeleteAdapter(adapter.id)}
            />
          ))}
        </div>
      )}
    </div>
  );
}