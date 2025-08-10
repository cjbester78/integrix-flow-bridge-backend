import React from 'react';
import { Card } from '@/components/ui/card';
import { Input } from '@/components/ui/input';
import { Label } from '@/components/ui/label';
import { Settings } from 'lucide-react';

interface NamespaceConfig {
  uri: string;
  prefix: string;
  targetNamespace: string;
  schemaLocation: string;
}

interface NamespaceConfigurationProps {
  type: 'xml' | 'wsdl';
  namespaceConfig: NamespaceConfig;
  setNamespaceConfig: (config: NamespaceConfig) => void;
}

export const NamespaceConfiguration: React.FC<NamespaceConfigurationProps> = ({
  type,
  namespaceConfig,
  setNamespaceConfig
}) => {
  const isWsdl = type === 'wsdl';
  
  return (
    <Card className="p-4 bg-muted/30">
      <h4 className="font-medium mb-3 flex items-center gap-2">
        <Settings className="h-4 w-4" />
        {isWsdl ? 'WSDL' : 'XML'} Namespace Configuration
      </h4>
      <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
        <div className="space-y-2">
          <Label className="text-xs">
            {isWsdl ? 'Service Namespace *' : 'Namespace URI *'}
          </Label>
          <Input
            placeholder={isWsdl ? "http://example.com/service" : "http://example.com/namespace"}
            value={namespaceConfig.uri}
            onChange={(e) => setNamespaceConfig({...namespaceConfig, uri: e.target.value})}
            className="text-sm"
          />
        </div>
        <div className="space-y-2">
          <Label className="text-xs">
            {isWsdl ? 'Service Prefix' : 'Namespace Prefix'}
          </Label>
          <Input
            placeholder={isWsdl ? "svc" : "ns1"}
            value={namespaceConfig.prefix}
            onChange={(e) => setNamespaceConfig({...namespaceConfig, prefix: e.target.value})}
            className="text-sm"
          />
        </div>
        <div className="space-y-2">
          <Label className="text-xs">Target Namespace</Label>
          <Input
            placeholder="http://example.com/target"
            value={namespaceConfig.targetNamespace}
            onChange={(e) => setNamespaceConfig({...namespaceConfig, targetNamespace: e.target.value})}
            className="text-sm"
          />
        </div>
        <div className="space-y-2">
          <Label className="text-xs">
            {isWsdl ? 'WSDL Location' : 'Schema Location'}
          </Label>
          <Input
            placeholder={isWsdl ? "http://example.com/service.wsdl" : "http://example.com/schema.xsd"}
            value={namespaceConfig.schemaLocation}
            onChange={(e) => setNamespaceConfig({...namespaceConfig, schemaLocation: e.target.value})}
            className="text-sm"
          />
        </div>
      </div>
    </Card>
  );
};