import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '@/components/ui/card';
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from '@/components/ui/select';
import { Label } from '@/components/ui/label';
import { Customer } from '@/types/customer';
import { Users } from 'lucide-react';

interface CustomerFilterProps {
  selectedCustomer: Customer | null;
  customers: Customer[];
  loading: boolean;
  onCustomerChange: (customer: Customer | null) => void;
}

export const CustomerFilter = ({ 
  selectedCustomer, 
  customers, 
  loading, 
  onCustomerChange 
}: CustomerFilterProps) => {
  return (
    <Card className="animate-scale-in">
      <CardHeader>
        <CardTitle className="flex items-center gap-2">
          <Users className="h-5 w-5" />
          Customer Filter
        </CardTitle>
        <CardDescription>
          Filter channels by customer
        </CardDescription>
      </CardHeader>
      <CardContent>
        <div className="space-y-2">
          <Label htmlFor="customer">Customer</Label>
          <Select
            value={selectedCustomer?.id || ''}
            onValueChange={(customerId) => {
              const customer = customers.find(c => c.id === customerId) || null;
              onCustomerChange(customer);
            }}
            disabled={loading}
          >
            <SelectTrigger>
              <SelectValue placeholder="Select a customer to filter channels" />
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