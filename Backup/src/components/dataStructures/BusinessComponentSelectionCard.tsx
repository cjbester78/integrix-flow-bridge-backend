import React from 'react';
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '@/components/ui/card';
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from '@/components/ui/select';
import { Label } from '@/components/ui/label';
import { useBusinessComponentAdapters } from '@/hooks/useBusinessComponentAdapters';
import { BusinessComponent } from '@/types/businessComponent';
import { Users } from 'lucide-react';

interface BusinessComponentSelectionCardProps {
  selectedBusinessComponent: BusinessComponent | null;
  setSelectedBusinessComponent: (businessComponent: BusinessComponent | null) => void;
}

export const BusinessComponentSelectionCard: React.FC<BusinessComponentSelectionCardProps> = ({
  selectedBusinessComponent,
  setSelectedBusinessComponent
}) => {
  const { businessComponents, loading } = useBusinessComponentAdapters();

  return (
    <Card className="animate-scale-in">
      <CardHeader>
        <CardTitle className="flex items-center gap-2">
          <Users className="h-5 w-5" />
          Business Component Selection
        </CardTitle>
        <CardDescription>
          Select the business component this data structure belongs to
        </CardDescription>
      </CardHeader>
      <CardContent>
        <div className="space-y-2">
          <Label htmlFor="businessComponent">Business Component *</Label>
          <Select
            value={selectedBusinessComponent?.id || ''}
            onValueChange={(businessComponentId) => {
              const businessComponent = businessComponents.find(c => c.id === businessComponentId) || null;
              setSelectedBusinessComponent(businessComponent);
            }}
            disabled={loading}
          >
            <SelectTrigger>
              <SelectValue placeholder="Select a business component" />
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