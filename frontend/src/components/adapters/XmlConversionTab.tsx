import { FC } from 'react';
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '@/components/ui/card';
import { Label } from '@/components/ui/label';
import { Input } from '@/components/ui/input';
import { Switch } from '@/components/ui/switch';
import { Tabs, TabsContent, TabsList, TabsTrigger } from '@/components/ui/tabs';
import { Alert, AlertDescription } from '@/components/ui/alert';
import { Info } from 'lucide-react';

interface XmlConversionConfig {
  // Common settings
  rootElementName?: string;
  encoding?: string;
  includeXmlDeclaration?: boolean;
  prettyPrint?: boolean;
  
  // Namespace settings
  targetNamespace?: string;
  namespacePrefix?: string;
  
  // Receiver-specific settings
  removeRootElement?: boolean;
  handleNamespaces?: boolean;
  preserveAttributes?: boolean;
}

interface XmlConversionTabProps {
  mode: 'SENDER' | 'RECEIVER';
  config: XmlConversionConfig;
  onChange: (config: XmlConversionConfig) => void;
}

export const XmlConversionTab: FC<XmlConversionTabProps> = ({ mode, config, onChange }) => {
  const handleChange = (field: keyof XmlConversionConfig, value: any) => {
    onChange({
      ...config,
      [field]: value
    });
  };

  return (
    <div className="space-y-4">
      <Alert>
        <Info className="h-4 w-4" />
        <AlertDescription>
          {mode === 'SENDER' 
            ? 'Configure how incoming data is converted to XML for mapping'
            : 'Configure how XML is converted back to the target format after mapping'
          }
        </AlertDescription>
      </Alert>

      <Card>
        <CardHeader>
          <CardTitle>Basic Settings</CardTitle>
          <CardDescription>
            Core XML conversion configuration
          </CardDescription>
        </CardHeader>
        <CardContent className="space-y-4">
          <div className="grid gap-4">
            <div className="grid gap-2">
              <Label htmlFor="rootElementName">Root Element Name</Label>
              <Input
                id="rootElementName"
                value={config.rootElementName || 'Message'}
                onChange={(e) => handleChange('rootElementName', e.target.value)}
                placeholder="Message"
              />
              <p className="text-sm text-muted-foreground">
                The root element that wraps the converted data
              </p>
            </div>

            <div className="grid gap-2">
              <Label htmlFor="encoding">Encoding</Label>
              <Input
                id="encoding"
                value={config.encoding || 'UTF-8'}
                onChange={(e) => handleChange('encoding', e.target.value)}
                placeholder="UTF-8"
              />
            </div>

            <div className="flex items-center space-x-2">
              <Switch
                id="includeXmlDeclaration"
                checked={config.includeXmlDeclaration !== false}
                onCheckedChange={(checked) => handleChange('includeXmlDeclaration', checked)}
              />
              <Label htmlFor="includeXmlDeclaration">Include XML Declaration</Label>
            </div>

            <div className="flex items-center space-x-2">
              <Switch
                id="prettyPrint"
                checked={config.prettyPrint !== false}
                onCheckedChange={(checked) => handleChange('prettyPrint', checked)}
              />
              <Label htmlFor="prettyPrint">Pretty Print (Format with indentation)</Label>
            </div>
          </div>
        </CardContent>
      </Card>

      <Card>
        <CardHeader>
          <CardTitle>Namespace Configuration</CardTitle>
          <CardDescription>
            Optional XML namespace settings
          </CardDescription>
        </CardHeader>
        <CardContent className="space-y-4">
          <div className="grid gap-4">
            <div className="grid gap-2">
              <Label htmlFor="targetNamespace">Target Namespace</Label>
              <Input
                id="targetNamespace"
                value={config.targetNamespace || ''}
                onChange={(e) => handleChange('targetNamespace', e.target.value)}
                placeholder="http://example.com/integration"
              />
              <p className="text-sm text-muted-foreground">
                XML namespace URI (optional)
              </p>
            </div>

            <div className="grid gap-2">
              <Label htmlFor="namespacePrefix">Namespace Prefix</Label>
              <Input
                id="namespacePrefix"
                value={config.namespacePrefix || ''}
                onChange={(e) => handleChange('namespacePrefix', e.target.value)}
                placeholder="ns"
              />
              <p className="text-sm text-muted-foreground">
                Prefix for namespace (e.g., ns:Element)
              </p>
            </div>
          </div>
        </CardContent>
      </Card>

      {mode === 'RECEIVER' && (
        <Card>
          <CardHeader>
            <CardTitle>Receiver Options</CardTitle>
            <CardDescription>
              Settings for converting XML back to target format
            </CardDescription>
          </CardHeader>
          <CardContent className="space-y-4">
            <div className="space-y-4">
              <div className="flex items-center space-x-2">
                <Switch
                  id="removeRootElement"
                  checked={config.removeRootElement === true}
                  onCheckedChange={(checked) => handleChange('removeRootElement', checked)}
                />
                <Label htmlFor="removeRootElement">Remove Root Element</Label>
              </div>
              <p className="text-sm text-muted-foreground ml-6">
                Strip the wrapper element when converting from XML
              </p>

              <div className="flex items-center space-x-2">
                <Switch
                  id="handleNamespaces"
                  checked={config.handleNamespaces !== false}
                  onCheckedChange={(checked) => handleChange('handleNamespaces', checked)}
                />
                <Label htmlFor="handleNamespaces">Handle Namespaces</Label>
              </div>
              <p className="text-sm text-muted-foreground ml-6">
                Process namespace declarations and prefixes
              </p>

              <div className="flex items-center space-x-2">
                <Switch
                  id="preserveAttributes"
                  checked={config.preserveAttributes === true}
                  onCheckedChange={(checked) => handleChange('preserveAttributes', checked)}
                />
                <Label htmlFor="preserveAttributes">Preserve Attributes</Label>
              </div>
              <p className="text-sm text-muted-foreground ml-6">
                Keep XML attributes as separate fields (instead of converting to elements)
              </p>
            </div>
          </CardContent>
        </Card>
      )}

      <Card>
        <CardHeader>
          <CardTitle>Example</CardTitle>
          <CardDescription>
            How your data will be converted
          </CardDescription>
        </CardHeader>
        <CardContent>
          <Tabs defaultValue="json" className="w-full">
            <TabsList>
              <TabsTrigger value="json">JSON to XML</TabsTrigger>
              <TabsTrigger value="csv">CSV to XML</TabsTrigger>
              <TabsTrigger value="sql">SQL to XML</TabsTrigger>
            </TabsList>
            <TabsContent value="json" className="space-y-2">
              <div className="grid grid-cols-2 gap-4">
                <div>
                  <Label>Original JSON</Label>
                  <pre className="text-xs bg-muted p-2 rounded">
{`{
  "orderId": "12345",
  "customer": "John Doe",
  "amount": 99.99
}`}
                  </pre>
                </div>
                <div>
                  <Label>Converted XML</Label>
                  <pre className="text-xs bg-muted p-2 rounded">
{`<?xml version="1.0" encoding="${config.encoding || 'UTF-8'}"?>
<${config.rootElementName || 'Message'}${config.targetNamespace ? ` xmlns${config.namespacePrefix ? `:${config.namespacePrefix}` : ''}="${config.targetNamespace}"` : ''}>
  <orderId>12345</orderId>
  <customer>John Doe</customer>
  <amount>99.99</amount>
</${config.rootElementName || 'Message'}>`}
                  </pre>
                </div>
              </div>
            </TabsContent>
            <TabsContent value="csv" className="space-y-2">
              <div className="grid grid-cols-2 gap-4">
                <div>
                  <Label>Original CSV</Label>
                  <pre className="text-xs bg-muted p-2 rounded">
{`orderId,customer,amount
12345,John Doe,99.99
12346,Jane Smith,149.99`}
                  </pre>
                </div>
                <div>
                  <Label>Converted XML</Label>
                  <pre className="text-xs bg-muted p-2 rounded">
{`<?xml version="1.0" encoding="${config.encoding || 'UTF-8'}"?>
<${config.rootElementName || 'Message'}${config.targetNamespace ? ` xmlns${config.namespacePrefix ? `:${config.namespacePrefix}` : ''}="${config.targetNamespace}"` : ''}>
  <record>
    <orderId>12345</orderId>
    <customer>John Doe</customer>
    <amount>99.99</amount>
  </record>
  <record>
    <orderId>12346</orderId>
    <customer>Jane Smith</customer>
    <amount>149.99</amount>
  </record>
</${config.rootElementName || 'Message'}>`}
                  </pre>
                </div>
              </div>
            </TabsContent>
            <TabsContent value="sql" className="space-y-2">
              <div className="grid grid-cols-2 gap-4">
                <div>
                  <Label>SQL Result</Label>
                  <pre className="text-xs bg-muted p-2 rounded">
{`SELECT * FROM orders
-----------------
orderId | customer    | amount
12345   | John Doe    | 99.99
12346   | Jane Smith  | 149.99`}
                  </pre>
                </div>
                <div>
                  <Label>Converted XML</Label>
                  <pre className="text-xs bg-muted p-2 rounded">
{`<?xml version="1.0" encoding="${config.encoding || 'UTF-8'}"?>
<${config.rootElementName || 'Message'}${config.targetNamespace ? ` xmlns${config.namespacePrefix ? `:${config.namespacePrefix}` : ''}="${config.targetNamespace}"` : ''}>
  <row>
    <orderId>12345</orderId>
    <customer>John Doe</customer>
    <amount>99.99</amount>
  </row>
  <row>
    <orderId>12346</orderId>
    <customer>Jane Smith</customer>
    <amount>149.99</amount>
  </row>
</${config.rootElementName || 'Message'}>`}
                  </pre>
                </div>
              </div>
            </TabsContent>
          </Tabs>
        </CardContent>
      </Card>
    </div>
  );
};