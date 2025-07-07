import { ProcessedIndicator } from './csvProcessing';

export interface ManagedIndicator {
  id: string;
  name: string;
  description?: string;
  code?: string; // Auto-generated or manual
  unit?: string;
  source?: string;
  dataType?: string; // 'integer', 'decimal', 'percentage', etc.
  
  // Relationships
  subareaId?: string;
  subareaName?: string; // For display
  direction?: 'input' | 'output';
  aggregationWeight?: number;
  
  // Data context
  valueCount: number; // Number of data points
  dimensions: string[]; // ['time', 'location', 'custom_dimension']
  sampleValues?: string[]; // Preview of actual values
  
  // Metadata
  isFromCsv: boolean;
  isManual: boolean;
  isModified: boolean;
  createdAt: Date;
  lastModified?: Date;
}

export interface IndicatorFormData {
  name: string;
  description?: string;
  unit?: string;
  source?: string;
  dataType?: string;
  subareaId?: string;
  direction?: 'input' | 'output';
  aggregationWeight?: number;
}

export interface ManualIndicatorData extends IndicatorFormData {
  // For manually added indicators without CSV data
  estimatedValues?: number; // Expected number of data points
  type?: string; // 'input', 'output', etc. (new field for manual modal)
  dimensions?: string[]; // List of dimension keys (new field for manual modal)
  dataRows?: any[]; // Data input rows (optional, for manual modal)
}

export interface IndicatorUpdateRequest {
  id: string;
  updates: Partial<ManagedIndicator>;
}

export interface BulkIndicatorUpdate {
  updates: IndicatorUpdateRequest[];
}

export interface IndicatorValidationResult {
  isValid: boolean;
  errors: string[];
  warnings: string[];
}

// Conversion utilities
export const convertProcessedToManaged = (processed: ProcessedIndicator): ManagedIndicator => ({
  id: processed.id,
  name: processed.name,
  description: processed.description,
  unit: processed.unit,
  source: processed.source,
  subareaId: processed.subareaId,
  direction: processed.direction,
  aggregationWeight: 1.0,
  valueCount: processed.valueCount,
  dimensions: processed.dimensions,
  dataType: 'decimal', // Default data type
  isFromCsv: true,
  isManual: false,
  isModified: false,
  createdAt: new Date(),
});

export const convertManagedToFormData = (indicator: ManagedIndicator): IndicatorFormData => ({
  name: indicator.name,
  description: indicator.description,
  unit: indicator.unit,
  source: indicator.source,
  dataType: indicator.dataType,
  subareaId: indicator.subareaId,
  direction: indicator.direction,
  aggregationWeight: indicator.aggregationWeight,
}); 