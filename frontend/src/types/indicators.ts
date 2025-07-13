import { ProcessedIndicator } from './csvProcessing';
import { DataType } from './dataType';
import { Dimension } from './dimensions';
import { SubareaIndicator } from './subareas';

export enum IndicatorDirection {
  INPUT = 'input',
  OUTPUT = 'output',
}

export interface ManagedIndicator {
  id: string;
  name: string;
  description?: string;
  code?: string; // Auto-generated or manual
  unit?: string; // unit code (for display)
  unitId?: number; // unit id (for backend)
  unitPrefix?: string;
  unitSuffix?: string;
  source?: string;
  dataType?: string; // 'integer', 'decimal', 'percentage', etc.
  
  // Relationships
  subareaId?: string;
  subareaName?: string; // For display
  direction?: IndicatorDirection;
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

export interface Indicator {
  id: string;
  code: string;
  name: string;
  isComposite: boolean;
  createdAt: Date;
  unit: string;
  unitId: string;
  dataType: DataType;
  subareaIndicators: SubareaIndicator[];
  valueCount: number;
  dimensions: string[];
  subareaId: string;
  subareaName: string;
  direction: IndicatorDirection;
  unitPrefix: string;
  unitSuffix: string;
}

export interface IndicatorDimensions {
  indicatorId: string;
  availableDimensions: Dimension[];
}

export interface IndicatorFormData {
  name: string;
  description?: string;
  unit?: string;
  unitId?: number;
  unitPrefix?: string;
  unitSuffix?: string;
  source?: string;
  dataType?: string;
  subareaId?: string;
  direction?: IndicatorDirection;
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

export interface IndicatorDataPoint {
  timestamp: string;
  value: number;
  dimensions?: Record<string, string>;
}

export interface IndicatorChartData {
  label: string;
  value: number;
  name?: string;
}

export interface IndicatorDataResponse {
  timeSeries?: IndicatorChartData[];
  originalDataPoints?: IndicatorDataPoint[];
  chartData?: IndicatorChartData[];
  dimensions?: string[];
  startDate?: string;
  endDate?: string;
  [key: string]: any; // For backward compatibility
}

export interface IndicatorDimensionValuesResponse {
  dimensionColumns: string[];
  rows?: Array<{
    dimensions: Record<string, string>;
    value?: number;
    isEmpty: boolean;
  }>;
}

export interface IndicatorAggregatedData {
  data: Record<string, number>;
  aggregatedValue?: number;
  dimension?: string;
  availableDimensions?: string[];
}

export interface UnitResponse {
  id: number;
  code: string;
  description?: string;
  group?: string;
  createdAt?: string;
}

// Conversion utilities
export const convertProcessedToManaged = (processed: ProcessedIndicator): ManagedIndicator => ({
  id: processed.id,
  name: processed.name,
  description: processed.description,
  unit: processed.unit,
  source: processed.source,
  subareaId: processed.subareaId,
  direction: processed.direction as IndicatorDirection,
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
  unitId: indicator.unitId,
  unitPrefix: indicator.unitPrefix,
  unitSuffix: indicator.unitSuffix,
  source: indicator.source,
  dataType: indicator.dataType,
  subareaId: indicator.subareaId,
  direction: indicator.direction,
  aggregationWeight: indicator.aggregationWeight,
}); 