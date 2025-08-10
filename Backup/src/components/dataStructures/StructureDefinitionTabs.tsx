import React from 'react';
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '@/components/ui/card';
import { Button } from '@/components/ui/button';
import { Tabs, TabsContent, TabsList, TabsTrigger } from '@/components/ui/tabs';
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from '@/components/ui/select';
import { Label } from '@/components/ui/label';
import { JsonStructureTab } from './tabs/JsonStructureTab';
import { XsdStructureTab } from './tabs/XsdStructureTab';
import { WsdlStructureTab } from './tabs/WsdlStructureTab';
import { EdmxStructureTab } from './tabs/EdmxStructureTab';
import { CustomStructureTab } from './tabs/CustomStructureTab';
import { Field } from '@/types/dataStructures';
import { Save } from 'lucide-react';

interface StructureDefinitionTabsProps {
  customFields: Field[];
  setCustomFields: (fields: Field[]) => void;
  jsonInput: string;
  setJsonInput: (input: string) => void;
  xsdInput: string;
  setXsdInput: (input: string) => void;
  edmxInput: string;
  setEdmxInput: (input: string) => void;
  wsdlInput: string;
  setWsdlInput: (input: string) => void;
  selectedStructureType: string;
  setSelectedStructureType: (type: string) => void;
  namespaceConfig: any;
  setNamespaceConfig: (config: any) => void;
  onSave: () => void;
  onWsdlAnalyzed?: (name: string | null, namespaceInfo: any) => void;
  onResetAllFields: () => void;
}

export const StructureDefinitionTabs: React.FC<StructureDefinitionTabsProps> = ({
  customFields,
  setCustomFields,
  jsonInput,
  setJsonInput,
  xsdInput,
  setXsdInput,
  edmxInput,
  setEdmxInput,
  wsdlInput,
  setWsdlInput,
  selectedStructureType,
  setSelectedStructureType,
  namespaceConfig,
  setNamespaceConfig,
  onSave,
  onWsdlAnalyzed,
  onResetAllFields
}) => {

  return (
    <Card className="animate-scale-in" style={{ animationDelay: '0.1s' }}>
      <CardHeader>
        <CardTitle>Define Structure</CardTitle>
        <CardDescription>Choose your preferred method to define the data structure</CardDescription>
      </CardHeader>
      <CardContent>
        <Tabs value={selectedStructureType} className="w-full" onValueChange={(value) => setSelectedStructureType(value)}>
          <TabsList className="grid w-full grid-cols-5">
            <TabsTrigger value="json">JSON Schema</TabsTrigger>
            <TabsTrigger value="xsd">XSD/XML</TabsTrigger>
            <TabsTrigger value="wsdl">WSDL</TabsTrigger>
            <TabsTrigger value="edmx">EDMX</TabsTrigger>
            <TabsTrigger value="custom">Custom Builder</TabsTrigger>
          </TabsList>
          
          <TabsContent value="json">
            <JsonStructureTab
              jsonInput={jsonInput}
              setJsonInput={setJsonInput}
              onResetAllFields={onResetAllFields}
            />
          </TabsContent>
          
          <TabsContent value="xsd">
            <XsdStructureTab
              xsdInput={xsdInput}
              setXsdInput={setXsdInput}
              namespaceConfig={namespaceConfig}
              setNamespaceConfig={setNamespaceConfig}
              onResetAllFields={onResetAllFields}
            />
          </TabsContent>
          
          <TabsContent value="wsdl">
            <WsdlStructureTab
              wsdlInput={wsdlInput}
              setWsdlInput={setWsdlInput}
              namespaceConfig={namespaceConfig}
              setNamespaceConfig={setNamespaceConfig}
              onWsdlAnalyzed={onWsdlAnalyzed}
              onResetAllFields={onResetAllFields}
            />
          </TabsContent>
          
          <TabsContent value="edmx">
            <EdmxStructureTab
              edmxInput={edmxInput}
              setEdmxInput={setEdmxInput}
              namespaceConfig={namespaceConfig}
              setNamespaceConfig={setNamespaceConfig}
              onResetAllFields={onResetAllFields}
            />
          </TabsContent>
          
          <TabsContent value="custom">
            <CustomStructureTab
              customFields={customFields}
              setCustomFields={setCustomFields}
              selectedStructureType={selectedStructureType}
              setSelectedStructureType={setSelectedStructureType}
              onResetAllFields={onResetAllFields}
            />
          </TabsContent>
        </Tabs>
        
        <div className="mt-6 pt-4 border-t">
          <Button onClick={onSave} className="w-full bg-gradient-primary hover:opacity-90">
            <Save className="h-4 w-4 mr-2" />
            Save Data Structure
          </Button>
        </div>
      </CardContent>
    </Card>
  );
};