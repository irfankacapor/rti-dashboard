import { Indicator, IndicatorDimensions, IndicatorDirection } from "./indicators";

export interface Subarea {
  id: string;
  code: string;
  name: string;
  description: string;
  createdAt: Date;
  areaId: string;
  areaName?: string;
  indicatorCount?: number;
}

export interface SubareaFormData {
  name: string;
  description: string;
  areaId: string; // Required, either real area or default area
} 

// Time series data point for a specific year
export interface TimeSeriesDataPoint {
  year: string;
  indicators: Record<string, number | null>; // indicatorName -> value
}

// Individual indicator time series data point
export interface IndicatorTimeSeriesDataPoint {
  year: string;
  value: number;
}

// Individual indicator dimension data point
export interface IndicatorDimensionDataPoint {
  dimensionValue: string;
  allDimensions: Record<string, string>;
  value: number;
}

export interface SubareaData {
  subarea?: Subarea;
  indicators: Indicator[];
  aggregatedData: Record<string, Record<string, number>>;
  totalAggregatedValue: number;
  dimensionMetadata: Record<string, IndicatorDimensions>;
  timeSeriesData: TimeSeriesDataPoint[];
  indicatorTimeSeriesData: Record<string, IndicatorTimeSeriesDataPoint[]>;
  indicatorDimensionData: Record<string, Record<string, IndicatorDimensionDataPoint[]>>;
  errors: Record<string, string>;
}

export interface SubareaIndicator {
  subareaId: string;
  subareaCode: string;
  subareaName: string;
  direction: IndicatorDirection;
  aggregationWeight: number;
  createdAt: Date;
}