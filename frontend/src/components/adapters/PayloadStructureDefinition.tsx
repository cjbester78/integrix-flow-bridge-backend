import { FC, useState } from 'react';
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '@/components/ui/card';
import { Tabs, TabsContent, TabsList, TabsTrigger } from '@/components/ui/tabs';
import { Textarea } from '@/components/ui/textarea';
import { Label } from '@/components/ui/label';
import { Button } from '@/components/ui/button';
import { Alert, AlertDescription } from '@/components/ui/alert';
import { Code2, FileJson, AlertCircle, CheckCircle } from 'lucide-react';

export interface PayloadStructureConfig {
  format: 'JSON' | 'XML';
  samplePayload?: string;
  jsonSchema?: string;
  xmlSchema?: string;
  convertedXml?: string;
  validationErrors?: string[];
}

interface PayloadStructureDefinitionProps {
  config: PayloadStructureConfig;
  onChange: (config: PayloadStructureConfig) => void;
  adapterType: string;
  mode: 'sender' | 'receiver';
  disabled?: boolean;
}

export const PayloadStructureDefinition: FC<PayloadStructureDefinitionProps> = ({
  config,
  onChange,
  adapterType,
  mode,
  disabled = false
}) => {
  const [conversionPreview, setConversionPreview] = useState<string>('');

  const validateJsonPayload = (payload: string): string[] => {
    const errors: string[] = [];
    try {
      JSON.parse(payload);
    } catch (error: any) {
      errors.push(`Invalid JSON: ${error.message}`);
    }
    return errors;
  };

  const validateXmlPayload = (payload: string): string[] => {
    const errors: string[] = [];
    try {
      const parser = new DOMParser();
      const doc = parser.parseFromString(payload, 'text/xml');
      const parserError = doc.querySelector('parsererror');
      if (parserError) {
        errors.push('Invalid XML format');
      }
    } catch (error: any) {
      errors.push(`XML parsing error: ${error.message}`);
    }
    return errors;
  };

  const handleSamplePayloadChange = (payload: string) => {
    const newConfig = { ...config, samplePayload: payload };
    
    // Validate based on format
    if (payload) {
      const errors = config.format === 'JSON' 
        ? validateJsonPayload(payload)
        : validateXmlPayload(payload);
      newConfig.validationErrors = errors;
      
      // If valid JSON, try to convert to XML preview
      if (config.format === 'JSON' && errors.length === 0) {
        try {
          const jsonObj = JSON.parse(payload);
          const xmlPreview = convertJsonToXmlPreview(jsonObj);
          setConversionPreview(xmlPreview);
          newConfig.convertedXml = xmlPreview;
        } catch (e) {
          console.error('Error converting to XML preview:', e);
        }
      }
    }
    
    onChange(newConfig);
  };

  const convertJsonToXmlPreview = (jsonObj: any): string => {
    // Simple JSON to XML conversion for preview
    const convert = (obj: any, indent: string = ''): string => {
      let xml = '';
      
      if (Array.isArray(obj)) {
        obj.forEach(item => {
          xml += `${indent}<item>\n${convert(item, indent + '  ')}${indent}</item>\n`;
        });
      } else if (typeof obj === 'object' && obj !== null) {
        Object.entries(obj).forEach(([key, value]) => {
          const validKey = key.replace(/[^a-zA-Z0-9_-]/g, '_');
          if (typeof value === 'object' && value !== null) {
            xml += `${indent}<${validKey}>\n${convert(value, indent + '  ')}${indent}</${validKey}>\n`;
          } else {
            xml += `${indent}<${validKey}>${escapeXml(String(value))}</${validKey}>\n`;
          }
        });
      } else {
        xml += `${indent}${escapeXml(String(obj))}\n`;
      }
      
      return xml;
    };
    
    return `<?xml version="1.0" encoding="UTF-8"?>\n<Message>\n${convert(jsonObj, '  ')}</Message>`;
  };

  const escapeXml = (unsafe: string): string => {
    return unsafe
      .replace(/&/g, '&amp;')
      .replace(/</g, '&lt;')
      .replace(/>/g, '&gt;')
      .replace(/"/g, '&quot;')
      .replace(/'/g, '&apos;');
  };

  const generateJsonSchema = () => {
    if (!config.samplePayload || config.format !== 'JSON') return;
    
    try {
      const jsonObj = JSON.parse(config.samplePayload);
      const schema = generateSchemaFromObject(jsonObj);
      onChange({
        ...config,
        jsonSchema: JSON.stringify(schema, null, 2)
      });
    } catch (e) {
      console.error('Error generating schema:', e);
    }
  };

  const generateSchemaFromObject = (obj: any): any => {
    const schema: any = {
      "$schema": "http://json-schema.org/draft-07/schema#",
      "type": "object",
      "properties": {}
    };

    const processValue = (value: any): any => {
      if (value === null) return { type: "null" };
      if (typeof value === "string") return { type: "string" };
      if (typeof value === "number") return { type: "number" };
      if (typeof value === "boolean") return { type: "boolean" };
      if (Array.isArray(value)) {
        if (value.length > 0) {
          return {
            type: "array",
            items: processValue(value[0])
          };
        }
        return { type: "array" };
      }
      if (typeof value === "object") {
        const objSchema: any = { type: "object", properties: {} };
        Object.entries(value).forEach(([key, val]) => {
          objSchema.properties[key] = processValue(val);
        });
        return objSchema;
      }
      return { type: "string" };
    };

    Object.entries(obj).forEach(([key, value]) => {
      schema.properties[key] = processValue(value);
    });

    return schema;
  };

  return (
    <Card>
      <CardHeader>
        <CardTitle>Payload Structure Definition</CardTitle>
        <CardDescription>
          Define the structure of {mode === 'sender' ? 'incoming' : 'outgoing'} payloads for your {adapterType} adapter
        </CardDescription>
      </CardHeader>
      <CardContent className="space-y-4">
        <Alert>
          <AlertCircle className="h-4 w-4" />
          <AlertDescription>
            For REST/HTTP adapters, the payload structure will be automatically converted to XML for mapping. 
            {config.format === 'JSON' && ' JSON payloads will be transformed to XML with appropriate element names.'}
          </AlertDescription>
        </Alert>

        <Tabs defaultValue="sample" className="w-full">
          <TabsList className="grid w-full grid-cols-3">
            <TabsTrigger value="sample">Sample Payload</TabsTrigger>
            <TabsTrigger value="schema">Schema Definition</TabsTrigger>
            <TabsTrigger value="preview" disabled={!config.samplePayload || config.format !== 'JSON'}>
              XML Preview
            </TabsTrigger>
          </TabsList>

          <TabsContent value="sample" className="space-y-4">
            <div className="space-y-2">
              <Label>Sample {config.format} Payload</Label>
              <Textarea
                placeholder={config.format === 'JSON' 
                  ? '{\n  "orderId": "12345",\n  "customer": {\n    "name": "John Doe",\n    "email": "john@example.com"\n  },\n  "items": [...]\n}'
                  : '<Order>\n  <OrderId>12345</OrderId>\n  <Customer>\n    <Name>John Doe</Name>\n  </Customer>\n</Order>'
                }
                className="font-mono text-sm min-h-[300px]"
                value={config.samplePayload || ''}
                onChange={(e) => handleSamplePayloadChange(e.target.value)}
                disabled={disabled}
              />
              {config.validationErrors && config.validationErrors.length > 0 && (
                <div className="space-y-1">
                  {config.validationErrors.map((error, index) => (
                    <p key={index} className="text-sm text-destructive">
                      {error}
                    </p>
                  ))}
                </div>
              )}
              {config.samplePayload && (!config.validationErrors || config.validationErrors.length === 0) && (
                <p className="text-sm text-success flex items-center gap-1">
                  <CheckCircle className="h-3 w-3" />
                  Valid {config.format} payload
                </p>
              )}
            </div>

            {config.format === 'JSON' && config.samplePayload && (!config.validationErrors || config.validationErrors.length === 0) && (
              <Button 
                variant="outline" 
                size="sm" 
                onClick={generateJsonSchema}
                disabled={disabled}
              >
                <FileJson className="h-4 w-4 mr-2" />
                Generate JSON Schema
              </Button>
            )}
          </TabsContent>

          <TabsContent value="schema" className="space-y-4">
            <div className="space-y-2">
              <Label>{config.format} Schema</Label>
              <Textarea
                placeholder={config.format === 'JSON' 
                  ? 'JSON Schema (optional)'
                  : 'XSD Schema (optional)'
                }
                className="font-mono text-sm min-h-[300px]"
                value={config.format === 'JSON' ? config.jsonSchema : config.xmlSchema}
                onChange={(e) => onChange({
                  ...config,
                  [config.format === 'JSON' ? 'jsonSchema' : 'xmlSchema']: e.target.value
                })}
                disabled={disabled}
              />
              <p className="text-sm text-muted-foreground">
                Schema definition is optional but helps with validation and documentation
              </p>
            </div>
          </TabsContent>

          <TabsContent value="preview" className="space-y-4">
            <Alert>
              <Code2 className="h-4 w-4" />
              <AlertDescription>
                This preview shows how your JSON payload will be converted to XML for mapping
              </AlertDescription>
            </Alert>
            
            <div className="space-y-2">
              <Label>XML Conversion Preview</Label>
              <Textarea
                className="font-mono text-sm min-h-[300px] bg-muted"
                value={conversionPreview}
                readOnly
              />
              <p className="text-sm text-muted-foreground">
                This XML structure will be used in the mapping screen
              </p>
            </div>
          </TabsContent>
        </Tabs>
      </CardContent>
    </Card>
  );
};