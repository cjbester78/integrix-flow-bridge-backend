// @ts-nocheck
import React, { useState, useMemo, useEffect } from 'react';
import { useParams, useNavigate, useLocation } from 'react-router-dom';
import { Field } from '@/types/dataStructures';
import { BusinessComponent } from '@/types/businessComponent';
import { useDataStructures } from '@/hooks/useDataStructures';
import { useToast } from '@/hooks/use-toast';
import { StructureCreationForm } from '@/components/dataStructures/StructureCreationForm';
import { BusinessComponentSelectionCard } from '@/components/dataStructures/BusinessComponentSelectionCard';
import { StructureDefinitionTabs } from '@/components/dataStructures/StructureDefinitionTabs';
import { StructurePreview } from '@/components/dataStructures/StructurePreview';
import { parseJsonStructure, parseWsdlStructure, buildNestedStructure } from '@/utils/structureParsers';
import { Layers } from 'lucide-react';
import { structureService } from '@/services/structureService';

export const CreateDataStructure = () => {
  try {
    const { id } = useParams();
    const navigate = useNavigate();
    const location = useLocation();
    const {
      saveStructure,
      updateStructure,
      loading
    } = useDataStructures();
    
    console.log('CreateDataStructure component rendering, loading:', loading);
    const { toast } = useToast();
    
    // Edit mode state
    const [isEditMode, setIsEditMode] = useState(false);
    const [editingStructureId, setEditingStructureId] = useState<string | null>(null);
    const [structureUsage, setStructureUsage] = useState<'source' | 'target'>('source');

  // Form state
  const [structureName, setStructureName] = useState('');
  const [structureDescription, setStructureDescription] = useState('');
  const [selectedBusinessComponent, setSelectedBusinessComponent] = useState<BusinessComponent | null>(null);
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
  
  // Load structure data in edit mode
  useEffect(() => {
    const loadStructure = async () => {
      if (id) {
        try {
          console.log('[CreateDataStructure] Loading structure for edit:', id);
          const response = await structureService.getStructure(id);
          
          if (response.success && response.data) {
            const structure = response.data;
            setIsEditMode(true);
            setEditingStructureId(structure.id);
            setStructureName(structure.name || '');
            setStructureDescription(structure.description || '');
            setStructureUsage(structure.usage || 'source');
            setSelectedStructureType(structure.type || 'json');
            
            // Set the appropriate input based on type
            // For XML-based formats (WSDL, XSD), prefer original content if available
            if (structure.type === 'json') {
              setJsonInput(JSON.stringify(structure.structure, null, 2));
            } else if (structure.type === 'wsdl') {
              // Use original content for WSDL if available, otherwise fall back to JSON
              if (structure.originalContent && structure.originalFormat === 'xml') {
                setWsdlInput(structure.originalContent);
              } else {
                setWsdlInput(JSON.stringify(structure.structure, null, 2));
              }
            } else if (structure.type === 'xsd') {
              // Use original content for XSD if available, otherwise fall back to JSON
              if (structure.originalContent && structure.originalFormat === 'xml') {
                setXsdInput(structure.originalContent);
              } else {
                setXsdInput(JSON.stringify(structure.structure, null, 2));
              }
            } else if (structure.type === 'custom') {
              // Convert structure back to custom fields format
              // This would need to be implemented based on your needs
            }
            
            // Set namespace if available
            if (structure.namespace) {
              setNamespaceConfig(structure.namespace);
            }
            
            // Set business component if available
            if (structure.businessComponent) {
              setSelectedBusinessComponent({
                id: structure.businessComponent.id,
                name: structure.businessComponent.name,
                description: structure.businessComponent.description || ''
              });
            }
          }
        } catch (error) {
          console.error('Error loading structure:', error);
          toast({
            title: "Error",
            description: "Failed to load data structure",
            variant: "destructive",
          });
          navigate('/data-structures');
        }
      } else if (location.state?.structure && location.state?.isEdit) {
        // Handle navigation from list page with structure data
        const structure = location.state.structure;
        console.log('[CreateDataStructure] Loading structure from navigation state:', structure);
        
        setIsEditMode(true);
        setEditingStructureId(structure.id);
        setStructureName(structure.name || '');
        setStructureDescription(structure.description || '');
        setStructureUsage(structure.usage || 'source');
        setSelectedStructureType(structure.type || 'json');
        
        // Set the appropriate input based on type
        if (structure.type === 'json') {
          setJsonInput(JSON.stringify(structure.structure, null, 2));
        } else if (structure.type === 'wsdl') {
          setWsdlInput(JSON.stringify(structure.structure, null, 2));
        } else if (structure.type === 'xsd') {
          setXsdInput(JSON.stringify(structure.structure, null, 2));
        }
        
        // Set namespace if available
        if (structure.namespace) {
          setNamespaceConfig(structure.namespace);
        }
        
        // Set business component if available
        if (structure.businessComponent) {
          setSelectedBusinessComponent({
            id: structure.businessComponent.id,
            name: structure.businessComponent.name,
            description: structure.businessComponent.description || ''
          });
        }
      }
    };
    
    loadStructure();
  }, [id, location.state, navigate, toast]);

  // Create a preview structure based on current inputs
  const previewStructure = useMemo(() => {
    if (!structureName) return null;

    let structure: any = {};
    
    if (selectedStructureType === 'json' && jsonInput) {
      structure = parseJsonStructure(jsonInput);
    } else if (selectedStructureType === 'wsdl' && wsdlInput) {
      const wsdlResult = parseWsdlStructure(wsdlInput);
      structure = wsdlResult?.structure || wsdlResult;
    } else if (selectedStructureType === 'xsd' && xsdInput) {
      structure = { message: 'XSD parsing not fully implemented yet' };
    } else if (selectedStructureType === 'edmx' && edmxInput) {
      structure = { message: 'EDMX parsing not fully implemented yet' };
    } else if (selectedStructureType === 'custom' && customFields.length > 0) {
      structure = buildNestedStructure(customFields);
    }

    if (!structure || Object.keys(structure).length === 0) return null;

    return {
      id: 'preview',
      name: structureName,
      type: selectedStructureType as 'json' | 'xsd' | 'wsdl' | 'edmx' | 'custom',
      description: structureDescription,
      structure,
      createdAt: new Date().toISOString().split('T')[0],
      usage: structureUsage,
      namespace: (selectedStructureType === 'xsd' || selectedStructureType === 'wsdl' || selectedStructureType === 'edmx') && namespaceConfig.uri ? namespaceConfig : undefined
    };
  }, [structureName, structureDescription, selectedStructureType, jsonInput, xsdInput, edmxInput, wsdlInput, customFields, namespaceConfig]);

  // Helper function to add suffix to structure name
  const addUsageSuffix = (name: string) => {
    if (!name) return name;
    
    // Remove existing suffix if present
    let baseName = name;
    if (baseName.endsWith('_Out') || baseName.endsWith('_In')) {
      baseName = baseName.substring(0, baseName.length - 4);
    }
    
    // Add new suffix
    const suffix = structureUsage === 'source' ? '_Out' : '_In';
    return baseName + suffix;
  };
  
  const handleWsdlAnalyzed = (extractedName: string | null, namespaceInfo: any) => {
    if (extractedName && !structureName) {
      // Add suffix based on usage
      setStructureName(addUsageSuffix(extractedName));
    }
    if (namespaceInfo) {
      setNamespaceConfig(namespaceInfo);
    }
  };

  const handleSave = async () => {
    if (!selectedBusinessComponent) {
      // Handle validation - business component is required
      toast({
        title: "Validation Error",
        description: "Please select a business component",
        variant: "destructive",
      });
      return;
    }
    
    let success = false;
    
    if (isEditMode && editingStructureId) {
      success = await updateStructure(
        editingStructureId,
        structureName,
        structureDescription,
        structureUsage,
        jsonInput,
        xsdInput,
        edmxInput,
        wsdlInput,
        customFields,
        selectedStructureType,
        namespaceConfig,
        selectedBusinessComponent?.id
      );
    } else {
      success = await saveStructure(
        structureName,
        structureDescription,
        structureUsage,
        jsonInput,
        xsdInput,
        edmxInput,
        wsdlInput,
        customFields,
        selectedStructureType,
        namespaceConfig,
        selectedBusinessComponent?.id
      );
    }

    if (success) {
      if (isEditMode) {
        // Navigate back to the list page after successful update
        navigate('/data-structures');
      } else {
        // Reset form for create mode
        setStructureName('');
        setStructureDescription('');
        setSelectedBusinessComponent(null);
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
    }
  };

  const handleResetAllFields = () => {
    setStructureName('');
    setStructureDescription('');
    setSelectedBusinessComponent(null);
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

  // Show loading state
  if (loading) {
    return (
      <div className="p-6 space-y-6 animate-fade-in">
        <div className="animate-slide-up">
          <h1 className="text-3xl font-bold text-foreground flex items-center gap-3">
            <Layers className="h-8 w-8" />
            Data Structures
          </h1>
          <p className="text-muted-foreground">Loading data structures...</p>
        </div>
        <div className="grid grid-cols-1 lg:grid-cols-3 gap-6">
          <div className="lg:col-span-2 space-y-6">
            <div className="animate-pulse">
              <div className="h-32 bg-muted rounded-lg"></div>
            </div>
          </div>
        </div>
      </div>
    );
  }

  return (
    <div className="p-6 space-y-6 animate-fade-in">
      <div className="animate-slide-up">
        <h1 className="text-3xl font-bold text-foreground flex items-center gap-3">
          <Layers className="h-8 w-8" />
          Data Structures
        </h1>
        <p className="text-muted-foreground">Define and manage data structures for source and target messages</p>
      </div>

      <div className="space-y-6">
          <BusinessComponentSelectionCard
            selectedBusinessComponent={selectedBusinessComponent}
            setSelectedBusinessComponent={setSelectedBusinessComponent}
          />
          
          <StructureCreationForm
            structureName={structureName}
            setStructureName={setStructureName}
            structureDescription={structureDescription}
            setStructureDescription={setStructureDescription}
            structureUsage={structureUsage}
            setStructureUsage={setStructureUsage}
            isEditMode={isEditMode}
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
            onFileUploaded={(fileName: string) => {
              if (!structureName && fileName) {
                // Extract base name without extension
                const baseName = fileName.replace(/\.[^/.]+$/, '');
                setStructureName(addUsageSuffix(baseName));
              }
            }}
          />

          {previewStructure && (
            <div className="animate-fade-in">
              <StructurePreview selectedStructure={previewStructure} />
            </div>
          )}
      </div>
    </div>
  );
  } catch (error) {
    console.error('Error rendering DataStructures component:', error);
    return (
      <div className="p-6 space-y-6">
        <div className="text-center">
          <h1 className="text-3xl font-bold text-foreground flex items-center justify-center gap-3">
            <Layers className="h-8 w-8" />
            Data Structures
          </h1>
          <p className="text-muted-foreground mt-4">
            There was an error loading the data structures page. Please check the browser console for details.
          </p>
          <button 
            onClick={() => window.location.reload()} 
            className="mt-4 px-4 py-2 bg-primary text-primary-foreground rounded-md hover:bg-primary/90"
          >
            Reload Page
          </button>
        </div>
      </div>
    );
  }
};