import React from 'react';
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '@/components/ui/card';
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from '@/components/ui/select';
import { Label } from '@/components/ui/label';
import { useCustomerAdapters } from '@/hooks/useCustomerAdapters';
import { Customer } from '@/types/customer';
import { Users } from 'lucide-react';

interface CustomerSelectionAdapterCardProps {
  selectedCustomer: Customer | null;
  setSelectedCustomer: (customer: Customer | null) => void;
}

export const CustomerSelectionAdapterCard: React.FC<CustomerSelectionAdapterCardProps> = ({
  selectedCustomer,
  setSelectedCustomer
}) => {
  const { customers, loading } = useCustomerAdapters();

  return (
    <Card className="animate-scale-in">
      <CardHeader>
        <CardTitle className="flex items-center gap-2">
          <Users className="h-5 w-5" />
          Customer Selection
        </CardTitle>
        <CardDescription>
          Select the customer this adapter belongs to
        </CardDescription>
      </CardHeader>
      <CardContent>
        <div className="space-y-2">
          <Label htmlFor="customer">Customer *</Label>
          <Select
            value={selectedCustomer?.id || ''}
            onValueChange={(customerId) => {
              const customer = customers.find(c => c.id === customerId) || null;
              setSelectedCustomer(customer);
            }}
            disabled={loading}
          >
            <SelectTrigger>
              <SelectValue placeholder="Select a customer" />
            </SelectTrigger>
            <SelectContent>
              {customers.map((customer) => (
                <SelectItem key={customer.id} value={customer.id}>
                  {customer.name}
                </SelectItem>
              ))}
            </SelectContent>
          </Select>
        </div>
      </CardContent>
    </Card>
  );
};