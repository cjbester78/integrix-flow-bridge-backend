import { useState, useEffect } from 'react';
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '@/components/ui/card';
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from '@/components/ui/select';
import { Badge } from '@/components/ui/badge';
import { Label } from '@/components/ui/label';
import { Checkbox } from '@/components/ui/checkbox';
import { ArrowRight } from 'lucide-react';
import { useBusinessComponentAdapters } from '@/hooks/useBusinessComponentAdapters';

interface Adapter {
  id: string;
  name: string;
  icon: React.ComponentType<{ className?: string }>;
  category: string;
}

interface AdapterConfigurationCardProps {
  adapters: Adapter[];
  sourceBusinessComponent: string;
  targetBusinessComponent: string;
  sourceAdapter: string;
  targetAdapter: string;
  sourceAdapterActive: boolean;
  targetAdapterActive: boolean;
  onSourceBusinessComponentChange: (value: string) => void;
  onTargetBusinessComponentChange: (value: string) => void;
  onSourceAdapterChange: (value: string) => void;
  onTargetAdapterChange: (value: string) => void;
  onSourceAdapterActiveChange: (active: boolean) => void;
  onTargetAdapterActiveChange: (active: boolean) => void;
}

export const AdapterConfigurationCard = ({
  adapters,
  sourceBusinessComponent,
  targetBusinessComponent,
  sourceAdapter,
  targetAdapter,
  sourceAdapterActive,
  targetAdapterActive,
  onSourceBusinessComponentChange,
  onTargetBusinessComponentChange,
  onSourceAdapterChange,
  onTargetAdapterChange,
  onSourceAdapterActiveChange,
  onTargetAdapterActiveChange,
}: AdapterConfigurationCardProps) => {
  console.log('[AdapterConfigurationCard] Component rendered with props:', {
    adapters: adapters.length,
    sourceBusinessComponent,
    targetBusinessComponent,
    sourceAdapter,
    targetAdapter,
    sourceAdapterActive,
    targetAdapterActive
  });

  const { businessComponents, loading, getAdaptersForBusinessComponent } = useBusinessComponentAdapters();
  
  console.log('[AdapterConfigurationCard] Hook data:', {
    businessComponents: businessComponents.length,
    loading
  });
  
  const getAdapterById = (id: string) => adapters.find(adapter => adapter.id === id);

  const [businessComponentAdapters, setBusinessComponentAdapters] = useState<string[]>([]);

  useEffect(() => {
    if (sourceBusinessComponent) {
      loadBusinessComponentAdapters(sourceBusinessComponent);
    }
  }, [sourceBusinessComponent]);

  const loadBusinessComponentAdapters = async (businessComponentId: string) => {
    const allowedAdapterIds = await getAdaptersForBusinessComponent(businessComponentId);
    setBusinessComponentAdapters(allowedAdapterIds);
  };

  const getFilteredAdapters = (businessComponentId: string) => {
    if (!businessComponentId) return adapters;
    return adapters.filter(adapter => businessComponentAdapters.includes(adapter.id));
  };

  const handleSourceBusinessComponentChange = async (businessComponentId: string) => {
    console.log('[AdapterConfigurationCard] Source business component changing:', businessComponentId);
    try {
      onSourceBusinessComponentChange(businessComponentId);
      // Reset source adapter if it's not available for the new business component
      const adaptersForBusinessComponent = await getAdaptersForBusinessComponent(businessComponentId);
      console.log('[AdapterConfigurationCard] Available adapters for business component:', adaptersForBusinessComponent);
      if (sourceAdapter && !adaptersForBusinessComponent.includes(sourceAdapter)) {
        console.log('[AdapterConfigurationCard] Resetting source adapter - not available for new business component');
        onSourceAdapterChange('');
      }
    } catch (error) {
      console.error('[AdapterConfigurationCard] Error handling source business component change:', error);
    }
  };

  const handleTargetBusinessComponentChange = async (businessComponentId: string) => {
    onTargetBusinessComponentChange(businessComponentId);
    // Reset target adapter if it's not available for the new business component
    const adaptersForBusinessComponent = await getAdaptersForBusinessComponent(businessComponentId);
    if (targetAdapter && !adaptersForBusinessComponent.includes(targetAdapter)) {
      onTargetAdapterChange('');
    }
  };

  return (
    <Card className="animate-scale-in" style={{ animationDelay: '0.1s' }}>
      <CardHeader>
        <CardTitle>Source & Target Configuration</CardTitle>
        <CardDescription>Select the source and target systems for your integration</CardDescription>
      </CardHeader>
      <CardContent className="space-y-6">
        <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
          {/* Source Business Component & Adapter */}
          <div className="space-y-4">
            <div className="space-y-3">
              <Label>Source Business Component *</Label>
              <Select value={sourceBusinessComponent} onValueChange={handleSourceBusinessComponentChange} disabled={loading}>
                <SelectTrigger className="transition-all duration-300 hover:bg-accent/50">
                  <SelectValue placeholder="Select source business component" />
                </SelectTrigger>
                <SelectContent>
                  {businessComponents.map((businessComponent) => (
                    <SelectItem key={businessComponent.id} value={businessComponent.id}>
                      <div className="flex items-center gap-2">
                        <span>{businessComponent.name}</span>
                        {businessComponent.description && (
                          <Badge variant="outline" className="text-xs">{businessComponent.description}</Badge>
                        )}
                      </div>
                    </SelectItem>
                  ))}
                </SelectContent>
              </Select>
            </div>
            
            <div className="space-y-3">
              <Label>Source Adapter *</Label>
              <Select 
                value={sourceAdapter} 
                onValueChange={onSourceAdapterChange}
                disabled={!sourceBusinessComponent}
              >
                <SelectTrigger className="transition-all duration-300 hover:bg-accent/50">
                  <SelectValue placeholder="Select source system" />
                </SelectTrigger>
                <SelectContent>
                  {getFilteredAdapters(sourceBusinessComponent).map((adapter) => (
                    <SelectItem key={adapter.id} value={adapter.id}>
                      <div className="flex items-center gap-2">
                        <adapter.icon className="h-4 w-4" />
                        <span>{adapter.name}</span>
                        <Badge variant="outline" className="text-xs">{adapter.category}</Badge>
                      </div>
                    </SelectItem>
                  ))}
                </SelectContent>
              </Select>
              {sourceAdapter && (
                <div className="p-3 bg-muted/50 rounded-lg">
                  <div className="flex items-center gap-2">
                    {getAdapterById(sourceAdapter) && (
                      <>
                        {(() => {
                          const adapter = getAdapterById(sourceAdapter)!;
                          const IconComponent = adapter.icon;
                          return (
                            <>
                              <IconComponent className="h-4 w-4 text-primary" />
                              <span className="font-medium">{adapter.name}</span>
                            </>
                          );
                        })()}
                      </>
                    )}
                  </div>
                </div>
              )}
            </div>
            
            {/* Source Adapter Active Status */}
            {sourceAdapter && (
              <div className="flex items-center space-x-2">
                <Checkbox
                  id="sourceAdapterActive"
                  checked={sourceAdapterActive}
                  onCheckedChange={(checked) => onSourceAdapterActiveChange(checked === true)}
                />
                <Label htmlFor="sourceAdapterActive" className="text-sm font-normal flex items-center gap-1">
                  Active source adapter
                  <Badge variant={sourceAdapterActive ? "default" : "secondary"} className="text-xs">
                    {sourceAdapterActive ? "Active" : "Inactive"}
                  </Badge>
                </Label>
              </div>
            )}
          </div>

          {/* Target Business Component & Adapter */}
          <div className="space-y-4">
            <div className="space-y-3">
              <Label>Target Business Component *</Label>
              <Select value={targetBusinessComponent} onValueChange={handleTargetBusinessComponentChange} disabled={loading}>
                <SelectTrigger className="transition-all duration-300 hover:bg-accent/50">
                  <SelectValue placeholder="Select target business component" />
                </SelectTrigger>
                <SelectContent>
                  {businessComponents.map((businessComponent) => (
                    <SelectItem key={businessComponent.id} value={businessComponent.id}>
                      <div className="flex items-center gap-2">
                        <span>{businessComponent.name}</span>
                        {businessComponent.description && (
                          <Badge variant="outline" className="text-xs">{businessComponent.description}</Badge>
                        )}
                      </div>
                    </SelectItem>
                  ))}
                </SelectContent>
              </Select>
            </div>
            
            <div className="space-y-3">
              <Label>Target Adapter *</Label>
              <Select 
                value={targetAdapter} 
                onValueChange={onTargetAdapterChange}
                disabled={!targetBusinessComponent}
              >
                <SelectTrigger className="transition-all duration-300 hover:bg-accent/50">
                  <SelectValue placeholder="Select target system" />
                </SelectTrigger>
                <SelectContent>
                  {getFilteredAdapters(targetBusinessComponent).map((adapter) => (
                    <SelectItem key={adapter.id} value={adapter.id}>
                      <div className="flex items-center gap-2">
                        <adapter.icon className="h-4 w-4" />
                        <span>{adapter.name}</span>
                        <Badge variant="outline" className="text-xs">{adapter.category}</Badge>
                      </div>
                    </SelectItem>
                  ))}
                </SelectContent>
              </Select>
              {targetAdapter && (
                <div className="p-3 bg-muted/50 rounded-lg">
                  <div className="flex items-center gap-2">
                    {getAdapterById(targetAdapter) && (
                      <>
                        {(() => {
                          const adapter = getAdapterById(targetAdapter)!;
                          const IconComponent = adapter.icon;
                          return (
                            <>
                              <IconComponent className="h-4 w-4 text-primary" />
                              <span className="font-medium">{adapter.name}</span>
                            </>
                          );
                        })()}
                      </>
                    )}
                  </div>
                </div>
              )}
            </div>
            
            {/* Target Adapter Active Status */}
            {targetAdapter && (
              <div className="flex items-center space-x-2">
                <Checkbox
                  id="targetAdapterActive"
                  checked={targetAdapterActive}
                  onCheckedChange={(checked) => onTargetAdapterActiveChange(checked === true)}
                />
                <Label htmlFor="targetAdapterActive" className="text-sm font-normal flex items-center gap-1">
                  Active target adapter
                  <Badge variant={targetAdapterActive ? "default" : "secondary"} className="text-xs">
                    {targetAdapterActive ? "Active" : "Inactive"}
                  </Badge>
                </Label>
              </div>
            )}
          </div>
        </div>

        {/* Flow Visualization */}
        {sourceAdapter && targetAdapter && (
          <div className="mt-6 p-4 bg-gradient-secondary rounded-lg">
            <div className="flex items-center justify-center gap-4">
              <div className="flex items-center gap-2 bg-card p-3 rounded-lg shadow-soft">
                {getAdapterById(sourceAdapter) && (
                  <>
                    {(() => {
                      const adapter = getAdapterById(sourceAdapter)!;
                      const IconComponent = adapter.icon;
                      return (
                        <>
                          <IconComponent className="h-5 w-5 text-primary" />
                          <span className="font-medium">{adapter.name}</span>
                        </>
                      );
                    })()}
                  </>
                )}
              </div>
              <ArrowRight className="h-6 w-6 text-primary animate-pulse" />
              <div className="flex items-center gap-2 bg-card p-3 rounded-lg shadow-soft">
                {getAdapterById(targetAdapter) && (
                  <>
                    {(() => {
                      const adapter = getAdapterById(targetAdapter)!;
                      const IconComponent = adapter.icon;
                      return (
                        <>
                          <IconComponent className="h-5 w-5 text-primary" />
                          <span className="font-medium">{adapter.name}</span>
                        </>
                      );
                    })()}
                  </>
                )}
              </div>
            </div>
          </div>
        )}
      </CardContent>
    </Card>
  );
};