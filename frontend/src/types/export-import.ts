export interface FlowExportDTO {
  metadata: ExportMetadata;
  flow: any; // FlowDTO
  businessComponent?: any; // BusinessComponentDTO
  sourceAdapter?: any; // CommunicationAdapterDTO
  targetAdapter?: any; // CommunicationAdapterDTO
  transformations?: any[]; // FlowTransformationDTO[]
  fieldMappings?: any[]; // FieldMappingDTO[]
  certificateReferences?: CertificateReferenceDTO[];
  dataStructures?: Record<string, any>;
  additionalConfig?: Record<string, any>;
}

export interface ExportMetadata {
  exportId: string;
  exportVersion: string;
  applicationVersion: string;
  exportDate: string;
  exportedBy: string;
  exportedByUsername: string;
  environment?: string;
  description?: string;
  tags?: Record<string, string>;
}

export interface CertificateReferenceDTO {
  id: string;
  name: string;
  type: string;
  format: string;
  fileName: string;
  passwordProtected: boolean;
  checksum: string;
}

export interface FlowExportRequestDTO {
  flowId: string;
  options?: ExportOptions;
}

export interface ExportOptions {
  includeBusinessComponent?: boolean;
  includeAdapterConfigs?: boolean;
  includeCertificateReferences?: boolean;
  includeSensitiveData?: boolean;
  includeStatistics?: boolean;
  includeAuditInfo?: boolean;
  format?: 'JSON' | 'XML' | 'YAML';
  compress?: boolean;
  environment?: string;
  description?: string;
  tags?: string[];
}

export interface FlowImportRequestDTO {
  flowExport: FlowExportDTO;
  options: ImportOptions;
  idMappings?: Record<string, string>;
}

export interface ImportOptions {
  conflictStrategy?: ConflictStrategy;
  importBusinessComponent?: boolean;
  importAdapters?: boolean;
  importCertificateReferences?: boolean;
  validateReferences?: boolean;
  activateAfterImport?: boolean;
  namePrefix?: string;
  nameSuffix?: string;
  targetBusinessComponentId?: string;
  configOverrides?: Record<string, any>;
}

export type ConflictStrategy = 'FAIL' | 'SKIP' | 'CREATE_NEW' | 'UPDATE_EXISTING' | 'PROMPT';

export interface FlowImportResultDTO {
  success: boolean;
  importedFlowId?: string;
  importedFlowName?: string;
  summary: ImportSummary;
  idMappings: Record<string, string>;
  warnings: ImportMessage[];
  errors: ImportMessage[];
  conflictResolutions: ConflictResolution[];
}

export interface ImportSummary {
  flowImported: boolean;
  businessComponentImported: boolean;
  adaptersImported: number;
  transformationsImported: number;
  fieldMappingsImported: number;
  certificateReferencesCreated: number;
  totalObjectsImported: number;
  importDurationMs: number;
}

export interface ImportMessage {
  code: string;
  message: string;
  objectType?: string;
  objectId?: string;
  objectName?: string;
  details?: Record<string, any>;
}

export interface ConflictResolution {
  objectType: string;
  originalId: string;
  originalName: string;
  newId?: string;
  newName?: string;
  resolution: string;
  reason?: string;
}

export interface FlowImportValidationDTO {
  canImport: boolean;
  isValid: boolean;
  preview: ImportPreview;
  errors: ValidationIssue[];
  warnings: ValidationIssue[];
  conflicts: Conflict[];
  requiredPermissions: string[];
  versionCompatibility: VersionCompatibility;
}

export interface ImportPreview {
  flowName: string;
  flowDescription?: string;
  businessComponentName?: string;
  sourceAdapterName?: string;
  targetAdapterName?: string;
  transformationCount: number;
  fieldMappingCount: number;
  certificateReferenceCount: number;
  objectCounts: Record<string, number>;
}

export interface ValidationIssue {
  code: string;
  message: string;
  field?: string;
  objectType?: string;
  objectId?: string;
  severity: 'ERROR' | 'WARNING' | 'INFO';
  context?: Record<string, any>;
}

export interface Conflict {
  objectType: string;
  importId: string;
  importName: string;
  existingId?: string;
  existingName?: string;
  type: ConflictType;
  resolutionOptions: string[];
}

export type ConflictType = 'NAME_EXISTS' | 'ID_EXISTS' | 'UNIQUE_CONSTRAINT' | 'REFERENCE_MISSING' | 'VERSION_MISMATCH';

export interface VersionCompatibility {
  exportVersion: string;
  currentVersion: string;
  isCompatible: boolean;
  requiresMigration: boolean;
  migrationSteps?: string[];
  breakingChanges?: string[];
}