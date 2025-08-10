import { useState, useEffect } from 'react';
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '@/components/ui/card';
import { Label } from '@/components/ui/label';
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from '@/components/ui/select';
import { Switch } from '@/components/ui/switch';
import { Input } from '@/components/ui/input';
import { Button } from '@/components/ui/button';
import { Badge } from '@/components/ui/badge';
import { Alert, AlertDescription, AlertTitle } from '@/components/ui/alert';
import { Save, RefreshCw, AlertCircle, CheckCircle2, Layers } from 'lucide-react';
import { useToast } from '@/hooks/use-toast';
import { api } from '@/services/api';

interface EnvironmentConfig {
  environmentType: string;
  enforceRestrictions: boolean;
  restrictionMessage: string;
  availableTypes: string[];
}

export const EnvironmentConfiguration = () => {
  const { toast } = useToast();
  const [config, setConfig] = useState<EnvironmentConfig | null>(null);
  const [originalConfig, setOriginalConfig] = useState<EnvironmentConfig | null>(null);
  const [isLoading, setIsLoading] = useState(true);
  const [isSaving, setIsSaving] = useState(false);
  const [hasChanges, setHasChanges] = useState(false);

  // Fetch current configuration
  const fetchConfiguration = async () => {
    try {
      setIsLoading(true);
      const response = await api.get<EnvironmentConfig>('/admin/system-configuration/environment');
      if (response.success && response.data) {
        setConfig(response.data);
        setOriginalConfig(response.data);
        setHasChanges(false);
      } else {
        toast({ title: "Error", description: 'Failed to load environment configuration', variant: "destructive" });
      }
    } catch (error) {
      console.error('Error fetching environment configuration:', error);
      toast({ title: "Error", description: 'Error loading environment configuration', variant: "destructive" });
    } finally {
      setIsLoading(false);
    }
  };

  // Save configuration changes
  const saveConfiguration = async () => {
    if (!config) return;

    try {
      setIsSaving(true);
      
      // Save environment type
      const typeResponse = await api.put('/admin/system-configuration/environment/type', {
        environmentType: config.environmentType
      });
      
      if (!typeResponse.success) {
        toast({ title: "Error", description: 'Failed to update environment type', variant: "destructive" });
        return;
      }

      // Save enforce restrictions
      const restrictionsResponse = await api.put('/admin/system-configuration/environment/enforce-restrictions', {
        enforceRestrictions: config.enforceRestrictions
      });
      
      if (!restrictionsResponse.success) {
        toast({ title: "Error", description: 'Failed to update enforce restrictions', variant: "destructive" });
        return;
      }

      // Save restriction message
      const messageResponse = await api.put('/admin/system-configuration/environment/restriction-message', {
        restrictionMessage: config.restrictionMessage
      });
      
      if (!messageResponse.success) {
        toast({ title: "Error", description: 'Failed to update restriction message', variant: "destructive" });
        return;
      }

      toast({ title: "Success", description: 'Environment configuration saved successfully' });
      setOriginalConfig(config);
      setHasChanges(false);
      
      // Refresh the page after a short delay to apply new restrictions
      setTimeout(() => {
        window.location.reload();
      }, 1500);
      
    } catch (error) {
      console.error('Error saving environment configuration:', error);
      toast({ title: "Error", description: 'Error saving environment configuration', variant: "destructive" });
    } finally {
      setIsSaving(false);
    }
  };

  // Handle configuration changes
  const handleConfigChange = (updates: Partial<EnvironmentConfig>) => {
    if (!config) return;
    
    const newConfig = { ...config, ...updates };
    setConfig(newConfig);
    
    // Check if there are changes
    const changed = JSON.stringify(newConfig) !== JSON.stringify(originalConfig);
    setHasChanges(changed);
  };

  // Get environment type color and icon
  const getEnvironmentStyle = (type: string) => {
    switch (type) {
      case 'DEVELOPMENT':
        return { color: 'text-info', bgColor: 'bg-blue-100', label: 'Development' };
      case 'QUALITY_ASSURANCE':
        return { color: 'text-warning', bgColor: 'bg-yellow-100', label: 'Quality Assurance' };
      case 'PRODUCTION':
        return { color: 'text-destructive', bgColor: 'bg-red-100', label: 'Production' };
      default:
        return { color: 'text-muted-foreground', bgColor: 'bg-gray-100', label: 'Unknown' };
    }
  };

  useEffect(() => {
    fetchConfiguration();
  }, []);

  if (isLoading) {
    return (
      <Card>
        <CardContent className="flex items-center justify-center h-64">
          <RefreshCw className="h-8 w-8 animate-spin" />
        </CardContent>
      </Card>
    );
  }

  if (!config) {
    return (
      <Card>
        <CardContent className="flex items-center justify-center h-64">
          <div className="text-center">
            <AlertCircle className="h-8 w-8 mx-auto mb-2 text-muted-foreground" />
            <p className="text-muted-foreground">Failed to load configuration</p>
            <Button variant="outline" size="sm" onClick={fetchConfiguration} className="mt-2">
              Retry
            </Button>
          </div>
        </CardContent>
      </Card>
    );
  }

  const currentEnvStyle = getEnvironmentStyle(config.environmentType);

  return (
    <Card>
      <CardHeader>
        <div className="flex justify-between items-start">
          <div>
            <CardTitle className="flex items-center gap-2">
              <Layers className="h-5 w-5" />
              Environment Configuration
            </CardTitle>
            <CardDescription>
              Configure the system environment type and restrictions
            </CardDescription>
          </div>
          <Badge className={`${currentEnvStyle.bgColor} ${currentEnvStyle.color}`}>
            {currentEnvStyle.label}
          </Badge>
        </div>
      </CardHeader>
      <CardContent className="space-y-6">
        {/* Environment Type Selection */}
        <div className="space-y-2">
          <Label htmlFor="environment-type">Environment Type</Label>
          <Select
            value={config.environmentType}
            onValueChange={(value) => handleConfigChange({ environmentType: value })}
          >
            <SelectTrigger id="environment-type">
              <SelectValue />
            </SelectTrigger>
            <SelectContent>
              {config.availableTypes.map((type) => {
                const style = getEnvironmentStyle(type);
                return (
                  <SelectItem key={type} value={type}>
                    <span className={style.color}>{style.label}</span>
                  </SelectItem>
                );
              })}
            </SelectContent>
          </Select>
          <p className="text-sm text-muted-foreground">
            Controls which features are available in the system
          </p>
        </div>

        {/* Enforce Restrictions Toggle */}
        <div className="flex items-center justify-between space-x-2">
          <div className="space-y-0.5">
            <Label htmlFor="enforce-restrictions">Enforce Restrictions</Label>
            <p className="text-sm text-muted-foreground">
              When enabled, environment restrictions will be applied
            </p>
          </div>
          <Switch
            id="enforce-restrictions"
            checked={config.enforceRestrictions}
            onCheckedChange={(checked) => handleConfigChange({ enforceRestrictions: checked })}
          />
        </div>

        {/* Restriction Message */}
        <div className="space-y-2">
          <Label htmlFor="restriction-message">Restriction Message</Label>
          <Input
            id="restriction-message"
            value={config.restrictionMessage}
            onChange={(e) => handleConfigChange({ restrictionMessage: e.target.value })}
            placeholder="Enter custom restriction message"
          />
          <p className="text-sm text-muted-foreground">
            Message shown when users try to perform restricted actions. Use %s for environment name.
          </p>
        </div>

        {/* Environment Restrictions Info */}
        <Alert>
          <AlertCircle className="h-4 w-4" />
          <AlertTitle>Environment Restrictions</AlertTitle>
          <AlertDescription className="space-y-2 mt-2">
            <div>
              <strong>Development:</strong> All features enabled
            </div>
            <div>
              <strong>Quality Assurance:</strong> Limited to adapter configuration, import/export, and deployment
            </div>
            <div>
              <strong>Production:</strong> Limited to adapter configuration, import/export, and deployment
            </div>
          </AlertDescription>
        </Alert>

        {/* Save Changes */}
        {hasChanges && (
          <div className="flex items-center justify-between pt-4 border-t">
            <div className="flex items-center gap-2 text-sm text-muted-foreground">
              <AlertCircle className="h-4 w-4" />
              You have unsaved changes
            </div>
            <div className="flex gap-2">
              <Button
                variant="outline"
                size="sm"
                onClick={() => {
                  setConfig(originalConfig);
                  setHasChanges(false);
                }}
              >
                Cancel
              </Button>
              <Button
                size="sm"
                onClick={saveConfiguration}
                disabled={isSaving}
              >
                {isSaving ? (
                  <>
                    <RefreshCw className="h-4 w-4 mr-2 animate-spin" />
                    Saving...
                  </>
                ) : (
                  <>
                    <Save className="h-4 w-4 mr-2" />
                    Save Changes
                  </>
                )}
              </Button>
            </div>
          </div>
        )}

        {/* Success Message */}
        {!hasChanges && originalConfig && JSON.stringify(config) === JSON.stringify(originalConfig) && (
          <div className="flex items-center gap-2 text-sm text-success">
            <CheckCircle2 className="h-4 w-4" />
            Configuration saved successfully
          </div>
        )}
      </CardContent>
    </Card>
  );
};