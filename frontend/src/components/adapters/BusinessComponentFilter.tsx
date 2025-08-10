import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '@/components/ui/card';
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from '@/components/ui/select';
import { Label } from '@/components/ui/label';
import { BusinessComponent } from '@/types/businessComponent';
import { Users } from 'lucide-react';

interface BusinessComponentFilterProps {
  selectedBusinessComponent: BusinessComponent | null;
  businessComponents: BusinessComponent[];
  loading: boolean;
  onBusinessComponentChange: (businessComponent: BusinessComponent | null) => void;
}

export const BusinessComponentFilter = ({ 
  selectedBusinessComponent, 
  businessComponents, 
  loading, 
  onBusinessComponentChange 
}: BusinessComponentFilterProps) => {
  return (
    <Card className="animate-scale-in">
      <CardHeader>
        <CardTitle className="flex items-center gap-2">
          <Users className="h-5 w-5" />
          Business Component Filter
        </CardTitle>
        <CardDescription>
          Filter channels by business component
        </CardDescription>
      </CardHeader>
      <CardContent>
        <div className="space-y-2">
          <Label htmlFor="businessComponent">Business Component</Label>
          <Select
            value={selectedBusinessComponent?.id || ''}
            onValueChange={(businessComponentId) => {
              const businessComponent = businessComponents.find(c => c.id === businessComponentId) || null;
              onBusinessComponentChange(businessComponent);
            }}
            disabled={loading}
          >
            <SelectTrigger>
              <SelectValue placeholder="Select a business component to filter channels" />
            </SelectTrigger>
            <SelectContent>
              {businessComponents.map((businessComponent) => (
                <SelectItem key={businessComponent.id} value={businessComponent.id}>
                  {businessComponent.name}
                </SelectItem>
              ))}
            </SelectContent>
          </Select>
        </div>
      </CardContent>
    </Card>
  );
};