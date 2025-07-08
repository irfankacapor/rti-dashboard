import { useState, useEffect, useMemo } from 'react';
import { useIndicatorData, useIndicatorDimensionValues } from './useApi';
import { processChartData, determineTimeGranularity, buildTimeRangeOptions, generateDisplayLabel } from '../utils/chartDataProcessor';
import { IndicatorChartData } from '../types/indicators';

interface UseIndicatorModalOptions {
  indicatorId: string;
  timeRange: string;
}

export function useIndicatorModal({ indicatorId, timeRange }: UseIndicatorModalOptions) {
  const [chartType, setChartType] = useState<'bar' | 'line'>('bar');
  const [selectedDimension, setSelectedDimension] = useState<string>('');
  const [viewMode, setViewMode] = useState<'chart' | 'table'>('chart');
  
  const { data: dimensionMeta, loading: dimensionMetaLoading } = useIndicatorDimensionValues(indicatorId);

  // Get all available dimensions robustly
  const availableDimensions: string[] = useMemo(() => {
    if (dimensionMeta && dimensionMeta.availableDimensions) {
      // Extract the 'type' field from each dimension info object
      return dimensionMeta.availableDimensions.map((dim: any) => dim.type);
    }
    return ['time'];
  }, [dimensionMeta]);

  // Determine the default dimension (prefer 'time', else first available)
  const defaultDimension = useMemo(() => {
    if (availableDimensions.includes('time')) return 'time';
    return availableDimensions[0] || 'time';
  }, [availableDimensions]);

  // Set default dimension on load
  useEffect(() => {
    if (availableDimensions.length > 0 && !selectedDimension) {
      setSelectedDimension(defaultDimension);
    }
  }, [availableDimensions, selectedDimension, defaultDimension]);

  // Fetch indicator data (raw or aggregated depending on selected dimension)
  const { data, loading, error } = useIndicatorData(
    indicatorId,
    timeRange,
    selectedDimension,
    defaultDimension,
    availableDimensions
  );

  // Process chart data using utility function
  const chartData: IndicatorChartData[] = useMemo(() => {
    return processChartData({
      selectedDimension,
      defaultDimension,
      data
    });
  }, [selectedDimension, defaultDimension, data]);

  // Determine time granularity and build time range options
  const timeGranularity = useMemo(() => 
    determineTimeGranularity(chartData), [chartData]
  );
  
  const timeRanges = useMemo(() => 
    buildTimeRangeOptions(timeGranularity), [timeGranularity]
  );

  // Generate display label
  const displayLabel = useMemo(() => 
    generateDisplayLabel(selectedDimension, chartData), [selectedDimension, chartData]
  );

  // Only show error if not just empty data
  const showError = error && !loading && !chartData.length;

  // Prepare table data from original dataPoints
  const tableData = data?.originalDataPoints || [];

  return {
    // State
    chartType,
    setChartType,
    selectedDimension,
    setSelectedDimension,
    viewMode,
    setViewMode,
    
    // Data
    chartData,
    tableData,
    availableDimensions,
    timeRanges,
    displayLabel,
    
    // Loading and error states
    loading: loading || dimensionMetaLoading,
    error: showError ? error : null,
    
    // Computed values
    hasData: chartData.length > 0 || tableData.length > 0,
  };
} 