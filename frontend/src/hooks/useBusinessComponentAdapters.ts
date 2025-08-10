// @ts-nocheck
import { useState, useEffect } from 'react';
import { BusinessComponent } from '@/types/businessComponent';
import { businessComponentService } from '@/services/businessComponentService';
import { adapterMonitoringService } from '@/services/adapterMonitoringService';
import { structureService } from '@/services/structureService';

// Mock businessComponent data as fallback
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
      console.log('Loading businessComponent data...');
      const [businessComponentsResponse, adaptersResponse] = await Promise.all([
        businessComponentService.getAllBusinessComponents(),
        adapterMonitoringService.getAvailableAdapterTypes()
      ]);

      let hasBusinessComponentData = false;
      if (businessComponentsResponse.success && businessComponentsResponse.data) {
        console.log('API businessComponents loaded:', businessComponentsResponse.data);
        const businessComponents = Array.isArray(businessComponentsResponse.data) 
          ? businessComponentsResponse.data 
          : [];
        
        // Set business components from API, even if empty
        console.log(`API returned ${businessComponents.length} business components`);
        setBusinessComponents(businessComponents);
        hasBusinessComponentData = true;
      }

      if (adaptersResponse.success && adaptersResponse.data) {
        console.log('API adapters loaded:', adaptersResponse.data);
        // Check if response is HTML (endpoint doesn't exist)
        if (typeof adaptersResponse.data === 'string' && adaptersResponse.data.includes('<!DOCTYPE html>')) {
          console.log('Adapters API endpoint not implemented, using empty array');
          setAdapters([]);
        } else {
          const adapters = Array.isArray(adaptersResponse.data) 
            ? adaptersResponse.data 
            : [];
          setAdapters(adapters);
        }
      } else {
        setAdapters([]);
      }

      // If API fails, show empty list
      if (!hasBusinessComponentData) {
        console.log('API failed, showing empty business components list');
        setBusinessComponents([]);
      }
    } catch (error) {
      console.error('Error loading data, showing empty list:', error);
      setBusinessComponents([]);
      setAdapters([]);
    } finally {
      setLoading(false);
    }
  };

  const getAdaptersForBusinessComponent = async (businessComponentId: string) => {
    try {
      const response = await adapterMonitoringService.getAdapters(businessComponentId);
      if (response.success && response.data && Array.isArray(response.data)) {
        // response.data is already an array of AdapterMonitoring objects
        const adapterIds = response.data
          .map(adapter => adapter?.id)
          .filter(id => id !== null && id !== undefined);
        return [...new Set(adapterIds)]; // Remove duplicates
      }
    } catch (error) {
      console.error('Error getting adapters for business component:', error);
    }
    return [];
  };

  const getStructuresForBusinessComponent = async (businessComponentId: string) => {
    try {
      const response = await structureService.getStructures();
      if (response.success && response.data) {
        // Handle both { structures: [...] } and direct array formats
        const structures = response.data.structures || response.data;
        
        // Ensure structures is an array
        if (!Array.isArray(structures)) {
          console.warn('Structures is not an array:', structures);
          return [];
        }
        
        // Filter and map structures safely
        return structures
          .filter(s => {
            // Ensure each structure is a valid object with required properties
            if (!s || typeof s !== 'object') {
              console.warn('Invalid structure object:', s);
              return false;
            }
            return s.businessComponentId === businessComponentId;
          })
          .map(structure => structure.id)
          .filter(id => id !== null && id !== undefined);
      }
    } catch (error) {
      console.error('Error getting structures for business component:', error);
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