import { BusinessComponent, CreateBusinessComponentRequest, UpdateBusinessComponentRequest } from '@/types/businessComponent';

class BusinessComponentService {
  private businessComponents: BusinessComponent[] = [
    {
      id: '1',
      name: 'ACME Corporation',
      description: 'Large enterprise client',
      contactEmail: 'contact@acme.com',
      contactPhone: '+1-555-0123',
      createdAt: '2024-01-15T10:00:00Z',
      updatedAt: '2024-01-15T10:00:00Z'
    },
    {
      id: '2',
      name: 'TechStart Inc',
      description: 'Growing technology startup',
      contactEmail: 'hello@techstart.com',
      contactPhone: '+1-555-0456',
      createdAt: '2024-01-16T14:30:00Z',
      updatedAt: '2024-01-16T14:30:00Z'
    }
  ];

  async getAllBusinessComponents(): Promise<{ success: boolean; data?: BusinessComponent[]; error?: string }> {
    try {
      // Simulate API delay
      await new Promise(resolve => setTimeout(resolve, 300));
      return { success: true, data: [...this.businessComponents] };
    } catch (error) {
      return { success: false, error: 'Failed to fetch business components' };
    }
  }

  async getBusinessComponentById(id: string): Promise<{ success: boolean; data?: BusinessComponent; error?: string }> {
    try {
      await new Promise(resolve => setTimeout(resolve, 200));
      const businessComponent = this.businessComponents.find(c => c.id === id);
      if (!businessComponent) {
        return { success: false, error: 'Business component not found' };
      }
      return { success: true, data: businessComponent };
    } catch (error) {
      return { success: false, error: 'Failed to fetch business component' };
    }
  }

  async createBusinessComponent(data: CreateBusinessComponentRequest): Promise<{ success: boolean; data?: BusinessComponent; error?: string }> {
    try {
      await new Promise(resolve => setTimeout(resolve, 300));
      
      const newBusinessComponent: BusinessComponent = {
        id: Date.now().toString(),
        ...data,
        createdAt: new Date().toISOString(),
        updatedAt: new Date().toISOString()
      };

      this.businessComponents.push(newBusinessComponent);
      return { success: true, data: newBusinessComponent };
    } catch (error) {
      return { success: false, error: 'Failed to create business component' };
    }
  }

  async updateBusinessComponent(data: UpdateBusinessComponentRequest): Promise<{ success: boolean; data?: BusinessComponent; error?: string }> {
    try {
      await new Promise(resolve => setTimeout(resolve, 300));
      
      const index = this.businessComponents.findIndex(c => c.id === data.id);
      if (index === -1) {
        return { success: false, error: 'Business component not found' };
      }

      const updatedBusinessComponent: BusinessComponent = {
        ...this.businessComponents[index],
        ...data,
        updatedAt: new Date().toISOString()
      };

      this.businessComponents[index] = updatedBusinessComponent;
      return { success: true, data: updatedBusinessComponent };
    } catch (error) {
      return { success: false, error: 'Failed to update business component' };
    }
  }

  async deleteBusinessComponent(id: string): Promise<{ success: boolean; error?: string }> {
    try {
      await new Promise(resolve => setTimeout(resolve, 300));
      
      const index = this.businessComponents.findIndex(c => c.id === id);
      if (index === -1) {
        return { success: false, error: 'Business component not found' };
      }

      this.businessComponents.splice(index, 1);
      return { success: true };
    } catch (error) {
      return { success: false, error: 'Failed to delete business component' };
    }
  }
}

export const businessComponentService = new BusinessComponentService();