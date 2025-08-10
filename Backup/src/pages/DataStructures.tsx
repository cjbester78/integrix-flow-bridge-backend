import React, { useState, useMemo } from 'react';
import { Field } from '@/types/dataStructures';
import { Customer } from '@/types/customer';
import { useDataStructures } from '@/hooks/useDataStructures';
import { StructureCreationForm } from '@/components/dataStructures/StructureCreationForm';
import { CustomerSelectionCard } from '@/components/dataStructures/CustomerSelectionCard';
import { StructureDefinitionTabs } from '@/components/dataStructures/StructureDefinitionTabs';
import { StructureLibrary } from '@/components/dataStructures/StructureLibrary';
import { StructurePreview } from '@/components/dataStructures/StructurePreview';
import { parseJsonStructure, parseWsdlStructure, buildNestedStructure } from '@/utils/structureParsers';
import { Layers } from 'lucide-react';

export const DataStructures = () => {
  const {
    structures,
    selectedStructure,
    setSelectedStructure,
    saveStructure,
    deleteStructure,
    duplicateStructure
  } = useDataStructures();

  // Form state
  const [structureName, setStructureName] = useState('');
  const [structureDescription, setStructureDescription] = useState('');
  const [selectedCustomer, setSelectedCustomer] = useState<Customer | null>(null);
  const [customFields, setCustomFields] = useState<Field[]>([]);
  const [jsonInput, setJsonInput] = useState('');
  const [xsdInput, setXsdInput] = useState('');
  const [edmxInput, setEdmxInput] = useState('');
  const [wsdlInput, setWsdlInput] = useState('');
  const [selectedStructureType, setSelectedStructureType] = useState<string>('json');
  const [namespaceConfig, setNamespaceConfig] = useState({
    uri: '',
    prefix: '',
    targetNamespace: '',
    schemaLocation: ''
  });

  // Create a preview structure based on current inputs
  const previewStructure = useMemo(() => {
    if (!structureName) return null;

    let structure: any = {};
    
    if (selectedStructureType === 'json' && jsonInput) {
      structure = parseJsonStructure(jsonInput);
    } else if (selectedStructureType === 'wsdl' && wsdlInput) {
      structure = parseWsdlStructure(wsdlInput);
    } else if (selectedStructureType === 'xsd' && xsdInput) {
      structure = { message: 'XSD parsing not fully implemented yet' };
    } else if (selectedStructureType === 'edmx' && edmxInput) {
      structure = { message: 'EDMX parsing not fully implemented yet' };
    } else if (selectedStructureType === 'custom' && customFields.length > 0) {
      structure = buildNestedStructure(customFields);
    }

    if (Object.keys(structure).length === 0) return null;

    return {
      id: 'preview',
      name: structureName,
      type: selectedStructureType as 'json' | 'xsd' | 'wsdl' | 'edmx' | 'custom',
      description: structureDescription,
      structure,
      createdAt: new Date().toISOString().split('T')[0],
      usage: 'source' as 'source' | 'target', // Default to source, can be modified later
      namespace: (selectedStructureType === 'xsd' || selectedStructureType === 'wsdl' || selectedStructureType === 'edmx') && namespaceConfig.uri ? namespaceConfig : undefined
    };
  }, [structureName, structureDescription, selectedStructureType, jsonInput, xsdInput, edmxInput, wsdlInput, customFields, namespaceConfig]);

  const handleWsdlAnalyzed = (extractedName: string | null, namespaceInfo: any) => {
    if (extractedName && !structureName) {
      setStructureName(extractedName);
    }
    if (namespaceInfo) {
      setNamespaceConfig(namespaceInfo);
    }
  };

  const handleSave = () => {
    if (!selectedCustomer) {
      // Handle validation - customer is required
      return;
    }
    
    const success = saveStructure(
      structureName,
      structureDescription,
      'source', // Default to source
      jsonInput,
      xsdInput,
      edmxInput,
      wsdlInput,
      customFields,
      selectedStructureType,
      namespaceConfig
    );

    if (success) {
      // Reset form
      setStructureName('');
      setStructureDescription('');
      setSelectedCustomer(null);
      setJsonInput('');
      setXsdInput('');
      setEdmxInput('');
      setWsdlInput('');
      setCustomFields([]);
      setNamespaceConfig({
        uri: '',
        prefix: '',
        targetNamespace: '',
        schemaLocation: ''
      });
    }
  };

  const handleResetAllFields = () => {
    setStructureName('');
    setStructureDescription('');
    setSelectedCustomer(null);
    setJsonInput('');
    setXsdInput('');
    setEdmxInput('');
    setWsdlInput('');
    setCustomFields([]);
    setNamespaceConfig({
      uri: '',
      prefix: '',
      targetNamespace: '',
      schemaLocation: ''
    });
    setSelectedStructureType('json');
  };

  return (
    <div className="p-6 space-y-6 animate-fade-in">
      <div className="animate-slide-up">
        <h1 className="text-3xl font-bold text-foreground flex items-center gap-3">
          <Layers className="h-8 w-8" />
          Data Structures
        </h1>
        <p className="text-muted-foreground">Define and manage data structures for source and target messages</p>
      </div>

      <div className="grid grid-cols-1 lg:grid-cols-3 gap-6">
        {/* Structure Creation */}
        <div className="lg:col-span-2 space-y-6">
          <CustomerSelectionCard
            selectedCustomer={selectedCustomer}
            setSelectedCustomer={setSelectedCustomer}
          />
          
          <StructureCreationForm
            structureName={structureName}
            setStructureName={setStructureName}
            structureDescription={structureDescription}
            setStructureDescription={setStructureDescription}
          />

          <StructureDefinitionTabs
            customFields={customFields}
            setCustomFields={setCustomFields}
            jsonInput={jsonInput}
            setJsonInput={setJsonInput}
            xsdInput={xsdInput}
            setXsdInput={setXsdInput}
            edmxInput={edmxInput}
            setEdmxInput={setEdmxInput}
            wsdlInput={wsdlInput}
            setWsdlInput={setWsdlInput}
            selectedStructureType={selectedStructureType}
            setSelectedStructureType={setSelectedStructureType}
            namespaceConfig={namespaceConfig}
            setNamespaceConfig={setNamespaceConfig}
            onSave={handleSave}
            onWsdlAnalyzed={handleWsdlAnalyzed}
            onResetAllFields={handleResetAllFields}
          />

          {previewStructure && (
            <div className="animate-fade-in">
              <StructurePreview selectedStructure={previewStructure} />
            </div>
          )}
        </div>

        {/* Structure Library */}
        <div className="space-y-6">
          <StructureLibrary
            structures={structures}
            selectedStructure={selectedStructure}
            onSelectStructure={setSelectedStructure}
            onDuplicateStructure={duplicateStructure}
            onDeleteStructure={deleteStructure}
          />

          {selectedStructure && (
            <StructurePreview selectedStructure={selectedStructure} />
          )}
        </div>
      </div>
    </div>
  );
};