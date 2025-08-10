import { Customer, CreateCustomerRequest, UpdateCustomerRequest } from '@/types/customer';

class CustomerService {
  private customers: Customer[] = [
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

  async getAllCustomers(): Promise<{ success: boolean; data?: Customer[]; error?: string }> {
    try {
      // Simulate API delay
      await new Promise(resolve => setTimeout(resolve, 300));
      return { success: true, data: [...this.customers] };
    } catch (error) {
      return { success: false, error: 'Failed to fetch customers' };
    }
  }

  async getCustomerById(id: string): Promise<{ success: boolean; data?: Customer; error?: string }> {
    try {
      await new Promise(resolve => setTimeout(resolve, 200));
      const customer = this.customers.find(c => c.id === id);
      if (!customer) {
        return { success: false, error: 'Customer not found' };
      }
      return { success: true, data: customer };
    } catch (error) {
      return { success: false, error: 'Failed to fetch customer' };
    }
  }

  async createCustomer(data: CreateCustomerRequest): Promise<{ success: boolean; data?: Customer; error?: string }> {
    try {
      await new Promise(resolve => setTimeout(resolve, 300));
      
      const newCustomer: Customer = {
        id: Date.now().toString(),
        ...data,
        createdAt: new Date().toISOString(),
        updatedAt: new Date().toISOString()
      };

      this.customers.push(newCustomer);
      return { success: true, data: newCustomer };
    } catch (error) {
      return { success: false, error: 'Failed to create customer' };
    }
  }

  async updateCustomer(data: UpdateCustomerRequest): Promise<{ success: boolean; data?: Customer; error?: string }> {
    try {
      await new Promise(resolve => setTimeout(resolve, 300));
      
      const index = this.customers.findIndex(c => c.id === data.id);
      if (index === -1) {
        return { success: false, error: 'Customer not found' };
      }

      const updatedCustomer: Customer = {
        ...this.customers[index],
        ...data,
        updatedAt: new Date().toISOString()
      };

      this.customers[index] = updatedCustomer;
      return { success: true, data: updatedCustomer };
    } catch (error) {
      return { success: false, error: 'Failed to update customer' };
    }
  }

  async deleteCustomer(id: string): Promise<{ success: boolean; error?: string }> {
    try {
      await new Promise(resolve => setTimeout(resolve, 300));
      
      const index = this.customers.findIndex(c => c.id === id);
      if (index === -1) {
        return { success: false, error: 'Customer not found' };
      }

      this.customers.splice(index, 1);
      return { success: true };
    } catch (error) {
      return { success: false, error: 'Failed to delete customer' };
    }
  }
}

export const customerService = new CustomerService();