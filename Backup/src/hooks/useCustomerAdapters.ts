import { useState, useEffect } from 'react';
import { Customer } from '@/types/customer';
import { customerService } from '@/services/customerService';
import { channelService } from '@/services/channelService';
import { structureService } from '@/services/structureService';

// Mock customer data as fallback
const mockCustomers: Customer[] = [
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

export const useCustomerAdapters = () => {
  const [customers, setCustomers] = useState<Customer[]>([]);
  const [adapters, setAdapters] = useState<any[]>([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    loadData();
  }, []);

  const loadData = async () => {
    try {
      console.log('Loading customer data...');
      const [customersResponse, adaptersResponse] = await Promise.all([
        customerService.getAllCustomers(),
        channelService.getAvailableAdapters()
      ]);

      let hasCustomerData = false;
      if (customersResponse.success && customersResponse.data) {
        console.log('API customers loaded:', customersResponse.data);
        setCustomers(customersResponse.data);
        hasCustomerData = true;
      }

      if (adaptersResponse.success && adaptersResponse.data) {
        console.log('API adapters loaded:', adaptersResponse.data);
        setAdapters(adaptersResponse.data);
      }

      // Use mock data as fallback if API fails
      if (!hasCustomerData) {
        console.log('Using mock customer data as fallback');
        setCustomers(mockCustomers);
      }
    } catch (error) {
      console.error('Error loading data, using mock data:', error);
      setCustomers(mockCustomers);
    } finally {
      setLoading(false);
    }
  };

  const getAdaptersForCustomer = async (customerId: string) => {
    const response = await channelService.getChannels(customerId);
    if (response.success && response.data) {
      const adapterIds = response.data.flatMap(channel => 
        channel.adapters.map(adapter => adapter.id)
      );
      return [...new Set(adapterIds)]; // Remove duplicates
    }
    return [];
  };

  const getStructuresForCustomer = async (customerId: string) => {
    const response = await structureService.getStructures();
    if (response.success && response.data) {
      return response.data.structures.filter(s => s.customerId === customerId).map(structure => structure.id);
    }
    return [];
  };

  return {
    customers,
    adapters,
    loading,
    getAdaptersForCustomer,
    getStructuresForCustomer,
    refreshData: loadData,
  };
};