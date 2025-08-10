import React, { useState, useEffect } from 'react';
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '@/components/ui/card';
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from '@/components/ui/select';
import { Label } from '@/components/ui/label';
import { BusinessComponent } from '@/types/businessComponent';
import { businessComponentService } from '@/services/businessComponentService';
import { Users } from 'lucide-react';

interface BusinessComponentSelectionCardProps {
  selectedBusinessComponent: BusinessComponent | null;
  setSelectedBusinessComponent: (businessComponent: BusinessComponent | null) => void;
}

export const BusinessComponentSelectionCard: React.FC<BusinessComponentSelectionCardProps> = ({
  selectedBusinessComponent,
  setSelectedBusinessComponent
}) => {
  const [businessComponents, setBusinessComponents] = useState<BusinessComponent[]>([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    loadBusinessComponents();
  }, []);

  const loadBusinessComponents = async () => {
    try {
      setLoading(true);
      const response = await businessComponentService.getAllBusinessComponents();
      if (response.success && response.data) {
        const components = Array.isArray(response.data) ? response.data : [];
        setBusinessComponents(components);
      }
    } catch (error) {
      console.error('Error loading business components:', error);
      setBusinessComponents([]);
    } finally {
      setLoading(false);
    }
  };

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
              {businessComponents.length === 0 ? (
                <div className="p-2 text-sm text-muted-foreground text-center">
                  No business components available
                </div>
              ) : (
                businessComponents.map((businessComponent) => (
                  <SelectItem key={businessComponent.id} value={businessComponent.id}>
                    {businessComponent.name}
                  </SelectItem>
                ))
              )}
            </SelectContent>
          </Select>
        </div>
      </CardContent>
    </Card>
  );
};