export interface FieldNode {
  id: string;
  name: string;
  type: string;
  path: string;
  children?: FieldNode[];
  expanded?: boolean;
}

export interface FunctionNodeData {
  id: string;
  functionName: string;
  parameters: Record<string, string>; // paramName -> constantValue
  sourceConnections: Record<string, string[]>; // paramName -> sourceFieldPaths[]
  position: { x: number; y: number };
}

export interface FieldMapping {
  id: string;
  name: string;
  sourceFields: string[];
  targetField: string;
  sourcePaths: string[];
  targetPath: string;
  javaFunction?: string;
  requiresTransformation?: boolean;
  functionNode?: FunctionNodeData; // New field for function-based mappings
  visualFlowData?: {
    nodes: any[];
    edges: any[];
    nodeIdCounter: number;
  }; // Store complete React Flow state for persistence
}

export interface WebserviceStructures {
  [key: string]: FieldNode[];
}