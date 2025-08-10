import { useState } from 'react';

export interface FieldMapping {
  id: string;
  name: string;
  sourceFields: string[];
  targetField: string;
  javaFunction?: string;
}

export interface FlowState {
  flowName: string;
  description: string;
  sourceBusinessComponent: string;
  targetBusinessComponent: string;
  sourceAdapter: string;
  targetAdapter: string;
  sourceAdapterActive: boolean;
  targetAdapterActive: boolean;
  sourceStructure: string;
  targetStructure: string;
  selectedTransformations: string[];
  isConfiguring: boolean;
  showStructurePreview: string | null;
  showFieldMapping: boolean;
  showMappingScreen: boolean;
  fieldMappings: FieldMapping[];
  selectedTargetField: string | null;
  javaFunction: string;
  mappingName: string;
}

export const useFlowState = () => {
  const [flowName, setFlowName] = useState('');
  const [description, setDescription] = useState('');
  const [sourceBusinessComponent, setSourceBusinessComponent] = useState('');
  const [targetBusinessComponent, setTargetBusinessComponent] = useState('');
  const [sourceAdapter, setSourceAdapter] = useState('');
  const [targetAdapter, setTargetAdapter] = useState('');
  const [sourceAdapterActive, setSourceAdapterActive] = useState(true);
  const [targetAdapterActive, setTargetAdapterActive] = useState(true);
  const [sourceStructure, setSourceStructure] = useState('');
  const [targetStructure, setTargetStructure] = useState('');
  const [selectedTransformations, setSelectedTransformations] = useState<string[]>([]);
  const [isConfiguring, setIsConfiguring] = useState(false);
  const [showStructurePreview, setShowStructurePreview] = useState<string | null>(null);
  const [showFieldMapping, setShowFieldMapping] = useState(false);
  const [showMappingScreen, setShowMappingScreen] = useState(false);
  const [fieldMappings, setFieldMappings] = useState<FieldMapping[]>([]);
  const [selectedTargetField, setSelectedTargetField] = useState<string | null>(null);
  const [javaFunction, setJavaFunction] = useState('');
  const [mappingName, setMappingName] = useState('');

  const resetForm = () => {
    setFlowName('');
    setDescription('');
    setSourceBusinessComponent('');
    setTargetBusinessComponent('');
    setSourceAdapter('');
    setTargetAdapter('');
    setSourceAdapterActive(true);
    setTargetAdapterActive(true);
    setSourceStructure('');
    setTargetStructure('');
    setSelectedTransformations([]);
    setFieldMappings([]);
    setShowFieldMapping(false);
    setMappingName('');
  };

  return {
    // State values
    flowName,
    description,
    sourceBusinessComponent,
    targetBusinessComponent,
    sourceAdapter,
    targetAdapter,
    sourceAdapterActive,
    targetAdapterActive,
    sourceStructure,
    targetStructure,
    selectedTransformations,
    isConfiguring,
    showStructurePreview,
    showFieldMapping,
    showMappingScreen,
    fieldMappings,
    selectedTargetField,
    javaFunction,
    mappingName,
    
    // Setters
    setFlowName,
    setDescription,
    setSourceBusinessComponent,
    setTargetBusinessComponent,
    setSourceAdapter,
    setTargetAdapter,
    setSourceAdapterActive,
    setTargetAdapterActive,
    setSourceStructure,
    setTargetStructure,
    setSelectedTransformations,
    setIsConfiguring,
    setShowStructurePreview,
    setShowFieldMapping,
    setShowMappingScreen,
    setFieldMappings,
    setSelectedTargetField,
    setJavaFunction,
    setMappingName,
    
    // Utilities
    resetForm,
  };
};