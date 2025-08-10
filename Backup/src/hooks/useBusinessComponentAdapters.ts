import { useState, useEffect } from 'react';
import { BusinessComponent } from '@/types/businessComponent';
import { businessComponentService } from '@/services/businessComponentService';
import { channelService } from '@/services/channelService';
import { structureService } from '@/services/structureService';

// Mock business component data as fallback
const mockBusinessComponents: BusinessComponent[] = [
  {
    id: '1',
    name: 'ACME Corporation',
    description: 'Manufacturing company specializing in enterprise solutions',
    contactEmail: 'admin@acme.com',
    contactPhone: '+1-555-0123',
    createdAt: '2024-01-15T10:30:00Z',
    updatedAt: '2024-01-20T14:30:00Z'
  },
  {
    id: '2',
    name: 'TechStart Inc',
    description: 'Technology startup focused on digital transformation',
    contactEmail: 'tech@techstart.com',
    contactPhone: '+1-555-0124',
    createdAt: '2024-01-10T09:15:00Z',
    updatedAt: '2024-01-20T16:45:00Z'
  },
  {
    id: '3',
    name: 'Global Retail Ltd',
    description: 'Large retail chain with global operations',
    contactEmail: 'contact@globalretail.com',
    contactPhone: '+1-555-0125',
    createdAt: '2024-01-05T11:20:00Z',
    updatedAt: '2024-01-18T09:15:00Z'
  }
];

export const useBusinessComponentAdapters = () => {
  const [businessComponents, setBusinessComponents] = useState<BusinessComponent[]>([]);
  const [adapters, setAdapters] = useState<any[]>([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    loadData();
  }, []);

  const loadData = async () => {
    try {
      console.log('[useBusinessComponentAdapters] Starting data load...');
      setLoading(true);
      
      const [businessComponentsResponse, adaptersResponse] = await Promise.all([
        businessComponentService.getAllBusinessComponents(),
        channelService.getAvailableAdapters()
      ]);

      console.log('[useBusinessComponentAdapters] Business components response:', businessComponentsResponse);
      console.log('[useBusinessComponentAdapters] Adapters response:', adaptersResponse);

      let hasBusinessComponentData = false;
      if (businessComponentsResponse.success && businessComponentsResponse.data) {
        console.log('[useBusinessComponentAdapters] API business components loaded:', businessComponentsResponse.data);
        setBusinessComponents(businessComponentsResponse.data);
        hasBusinessComponentData = true;
      } else {
        console.warn('[useBusinessComponentAdapters] Business components API failed:', businessComponentsResponse);
      }

      if (adaptersResponse.success && adaptersResponse.data) {
        console.log('[useBusinessComponentAdapters] API adapters loaded:', adaptersResponse.data);
        setAdapters(adaptersResponse.data);
      } else {
        console.warn('[useBusinessComponentAdapters] Adapters API failed:', adaptersResponse);
      }

      // Use mock data as fallback if API fails
      if (!hasBusinessComponentData) {
        console.log('[useBusinessComponentAdapters] Using mock business component data as fallback');
        setBusinessComponents(mockBusinessComponents);
      }
    } catch (error) {
      console.error('[useBusinessComponentAdapters] Error loading data, using mock data:', error);
      setBusinessComponents(mockBusinessComponents);
    } finally {
      console.log('[useBusinessComponentAdapters] Data loading complete');
      setLoading(false);
    }
  };

  const getAdaptersForBusinessComponent = async (businessComponentId: string) => {
    console.log('[useBusinessComponentAdapters] Getting adapters for business component:', businessComponentId);
    try {
      const response = await channelService.getChannels(businessComponentId);
      console.log('[useBusinessComponentAdapters] Channels response:', response);
      
      if (response.success && response.data) {
        const adapterIds = response.data.flatMap(channel => 
          channel.adapters.map(adapter => adapter.id)
        );
        const uniqueAdapterIds = [...new Set(adapterIds)]; // Remove duplicates
        console.log('[useBusinessComponentAdapters] Found adapter IDs:', uniqueAdapterIds);
        return uniqueAdapterIds;
      }
      console.warn('[useBusinessComponentAdapters] No adapters found for business component');
      return [];
    } catch (error) {
      console.error('[useBusinessComponentAdapters] Error getting adapters for business component:', error);
      return [];
    }
  };

  const getStructuresForBusinessComponent = async (businessComponentId: string) => {
    const response = await structureService.getStructures();
    if (response.success && response.data) {
      return response.data.structures.filter(s => s.customerId === businessComponentId).map(structure => structure.id);
    }
    return [];
  };

  return {
    businessComponents,
    adapters,
    loading,
    getAdaptersForBusinessComponent,
    getStructuresForBusinessComponent,
    refreshData: loadData,
  };
};